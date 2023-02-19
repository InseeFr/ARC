package fr.insee.arc.core.service.thread;

import java.text.SimpleDateFormat;
import java.util.Date;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.service.ApiService;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.structure.GenericBean;

public class ArcThreadGenericDao {
	
	private static SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	
	private ScalableConnection connexion;

	private String tablePilotageGlobale;
	
	private String tablePilotagePhase;
	
	private String tablePilotageThread;

	private String tablePrevious;
	
	private String paramBatch;	

	private String idSource;

	
	/**
	 * instaciate parameters requires to build a common dao threads between phases 
	 * @param connexion
	 * @param tablePilotageGlobale
	 * @param tablePilotagePhase
	 * @param tablePilotageThread
	 * @param tablePrevious
	 * @param paramBatch
	 * @param idSource
	 */
	public ArcThreadGenericDao(ScalableConnection connexion, String tablePilotageGlobale, String tablePilotagePhase, String tablePilotageThread,
			String tablePrevious, String paramBatch, String idSource) {
		super();
		this.connexion = connexion;
		this.tablePilotageGlobale = tablePilotageGlobale;
		this.tablePilotagePhase = tablePilotagePhase;
		this.tablePilotageThread = tablePilotageThread;
		this.tablePrevious = tablePrevious;
		this.paramBatch = paramBatch;
		this.idSource = idSource;
	}

	/**
	 * execute dao and returns default query to prepare thread depending if the connection is horizontally scalable or not
	 * @return
	 * @throws ArcException 
	 */
	public ArcPreparedStatementBuilder preparationDefaultDao() throws ArcException {
		
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		
		// nettoyage des objets base de données du thread
		query.append(ArcThreadGenericDao.cleanThread());

		// création des tables temporaires de données
		query.append(ArcThreadGenericDao.createTablePilotageIdSource(tablePilotagePhase, tablePilotageThread, idSource));

		// enregistrement de la date de traitement du fichier
		query.append("UPDATE "+tablePilotageThread+" set date_traitement=to_timestamp('" + formatter.format(new Date()) + "','" + ApiService.bdDateFormat+"');" );
		
		// if scalable thread
		if (connexion.isScaled()) {		
			// create the pilotage table of the file on the coordinator nod
			UtilitaireDao.get("arc").executeBlock(connexion.getCoordinatorConnection(), query.getQueryWithParameters());
		
			query = new ArcPreparedStatementBuilder();

			// nettoyage des objets base de données du thread
			query.append(ArcThreadGenericDao.cleanThread());

			// création des tables temporaires de données
			// get the records from tablePilotageThread in coordinator
			GenericBean gb = new GenericBean(
					UtilitaireDao.get("arc").executeRequest(connexion.getCoordinatorConnection(),
							new ArcPreparedStatementBuilder("SELECT * FROM " + tablePilotageThread)));
			// copy them in the table tablePilotageThread lcoated on the executor nod
			query.append(query.copyFromGenericBean(tablePilotageThread, gb));

		}
		return query;
	}

	/**
	 * 
	 * @param connexion
	 * @param tablePilotageGlobal
	 * @param tablePilotageThread
	 * @param id_source
	 * @return
	 * @throws ArcException 
	 */
	public void marquageFinalDefaultDao(ArcPreparedStatementBuilder query) throws ArcException {

		if (!connexion.isScaled()) {
			query.append(marquageFinal(tablePilotageGlobale, tablePilotageThread, idSource));
		}

		if (paramBatch != null) {
			query.append("DROP TABLE IF EXISTS "+ApiService.tableOfIdSource(this.tablePrevious,idSource)+";");
		}
		
		UtilitaireDao.get("arc").executeBlock(connexion.getExecutorConnection(), query.getQueryWithParameters());

		query = new ArcPreparedStatementBuilder();
		if (connexion.isScaled()) {
			GenericBean gb = new GenericBean(UtilitaireDao.get("arc").executeRequest(connexion.getExecutorConnection(),
					new ArcPreparedStatementBuilder("SELECT * FROM " + tablePilotageThread)));
			// copy them on the nod
			query.append(query.copyFromGenericBean(tablePilotageThread, gb));

			query.append(marquageFinal(tablePilotageGlobale, tablePilotageThread, idSource));

			UtilitaireDao.get("arc").executeBlock(connexion.getCoordinatorConnection(), query.getQueryWithParameters());
		}
	}

	/**
	 * clean temporary thread objects connexion
	 * 
	 * @return
	 */
	public static String cleanThread() {
		return "DISCARD SEQUENCES; DISCARD TEMP;";
	}

	/**
	 * Créer une table {@param TableOut} à partir des enregistrements de la table
	 * source {@param TableIn} filtrés sur un fichier particulier {@param idSource}
	 * 
	 * @param TableIn
	 * @param TableOut
	 * @param idSource
	 * @return
	 */
	public static String createTablePilotageIdSource(String tableIn, String tableOut, String idSource) {
		StringBuilder requete = new StringBuilder();
		requete.append("\n CREATE ");
		if (!tableOut.contains(".")) {
			requete.append("TEMPORARY ");
		} else {
			requete.append("UNLOGGED ");
		}
		requete.append(
				"TABLE " + tableOut + " with (autovacuum_enabled = false, toast.autovacuum_enabled = false) AS ");
		requete.append("\n SELECT * FROM " + tableIn + " ");
		requete.append("\n WHERE " + ColumnEnum.ID_SOURCE.getColumnName() + " ='" + idSource + "' ");
		requete.append("\n AND etape = 1 ");
		requete.append("\n ; ");
		return requete.toString();
	}
	

	/**
	 * return the query that marks the file processed by the phase in the persistent
	 * pilotage table @param tablePil
	 * 
	 * @param tablePil
	 * @param tablePilTemp
	 * @param idSource
	 * @return
	 */
	private static String marquageFinal(String tablePil, String tablePilTemp, String idSource) {
		StringBuilder requete = new StringBuilder();
		
		requete.append("\n set enable_hashjoin=off; ");
		requete.append("\n UPDATE " + tablePil + " a ");
		requete.append("\n SET etat_traitement =  b.etat_traitement, ");
		requete.append("\n id_norme = b.id_norme, ");
		requete.append("\n validite = b.validite, ");
		requete.append("\n periodicite = b.periodicite, ");
		requete.append("\n taux_ko = b.taux_ko, ");
		requete.append("\n date_traitement = to_timestamp('" + formatter.format(new Date()) + "','" + ApiService.bdDateFormat
				+ "'), ");
		requete.append("\n nb_enr = b.nb_enr, ");
		requete.append("\n rapport = b.rapport, ");
		requete.append("\n validite_inf = b.validite_inf, ");
		requete.append("\n validite_sup = b.validite_sup, ");
		requete.append("\n version = b.version, ");
		requete.append("\n etape = case when b.etat_traitement='{" + TraitementEtat.KO + "}' then 2 else b.etape end, ");
		requete.append("\n jointure = b.jointure, ");
		requete.append("\n generation_composite = b.date_traitement::text ");

		// Si on dispose d'un id source on met à jour seulement celui ci
		requete.append("\n FROM " + tablePilTemp + " as b ");
		requete.append("\n WHERE a."+ColumnEnum.ID_SOURCE.getColumnName()+" = '" + idSource + "' ");
		requete.append("\n AND a.etape = 1 ; ");

		requete.append(ApiService.resetPreviousPhaseMark(tablePil, idSource, null));

		requete.append("\n set enable_hashjoin = on; ");
		return requete.toString();

	}

}
