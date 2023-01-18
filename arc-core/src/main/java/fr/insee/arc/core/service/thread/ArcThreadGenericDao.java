package fr.insee.arc.core.service.thread;

import java.text.SimpleDateFormat;
import java.util.Date;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.service.ApiService;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.structure.GenericBean;

public class ArcThreadGenericDao {
	
	private ScalableConnection connexion;

	private String tablePilotageGlobale;
	
	private String tablePilotagePhase;
	
	private String tablePilotageThread;
	
	private String idSource;
	
	

	public ArcThreadGenericDao(ScalableConnection connexion, String tablePilotageGlobale, String tablePilotagePhase, String tablePilotageThread,
			String idSource) {
		super();
		this.connexion = connexion;
		this.tablePilotageGlobale = tablePilotageGlobale;
		this.tablePilotagePhase = tablePilotagePhase;
		this.tablePilotageThread = tablePilotageThread;
		this.idSource = idSource;
	}

	/**
	 * execute dao and returns default query to prepare thread depending if the connection is horizontally scalable or not
	 * @return
	 */
	public ArcPreparedStatementBuilder preparationDefaultDao() {
		
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		
		// nettoyage des objets base de données du thread
		query.append(ArcThreadGenericDao.cleanThread());

		// création des tables temporaires de données
		query.append(ArcThreadGenericDao.createTablePilotageIdSource(tablePilotagePhase, tablePilotageThread, idSource));

		// if scalable thread
		if (connexion.isScaled()) {

			System.out.println("§§§§§§§§§§§§§§§§ 1");
			System.out.println(query.getQueryWithParameters());

			
			// create the pilotage table of the file on the coordinator nod
			UtilitaireDao.get("arc").executeBlock(connexion.getCoordinatorConnection(), query.getQueryWithParameters());

			System.out.println("§§§§§§§§§§§§§§ 1 OK");
			
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
		
		System.out.println("§§§§§§§§§§§§§§§§ 1.5");
		System.out.println(query.getQueryWithParameters());
		
		return query;
	}

	/**
	 * 
	 * @param connexion
	 * @param tablePilotageGlobal
	 * @param tablePilotageThread
	 * @param id_source
	 * @return
	 */
	public void marquageFinalDefaultDao(ArcPreparedStatementBuilder query) {

		if (!connexion.isScaled()) {
			query.append(marquageFinal(tablePilotageGlobale, tablePilotageThread, idSource));
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
		Date date = new Date();

		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

		requete.append("\n set enable_hashjoin=off; ");
		requete.append("\n UPDATE " + tablePil + " a ");
		requete.append("\n \t SET etat_traitement =  b.etat_traitement, ");
		requete.append("\n \t   id_norme = b.id_norme, ");
		requete.append("\n \t   validite = b.validite, ");
		requete.append("\n \t   periodicite = b.periodicite, ");
		requete.append("\n \t   taux_ko = b.taux_ko, ");
		requete.append("\n \t   date_traitement = to_timestamp('" + formatter.format(date) + "','" + ApiService.bdDateFormat
				+ "'), ");
		requete.append("\n \t   nb_enr = b.nb_enr, ");
		requete.append("\n \t   rapport = b.rapport, ");
		requete.append("\n \t   validite_inf = b.validite_inf, ");
		requete.append("\n \t   validite_sup = b.validite_sup, ");
		requete.append("\n \t   version = b.version, ");
		requete.append(
				"\n \t   etape = case when b.etat_traitement='{" + TraitementEtat.KO + "}' then 2 else b.etape end, ");
		requete.append("\n \t   jointure = b.jointure ");

		// Si on dispose d'un id source on met à jour seulement celui ci
		requete.append("\n \t FROM " + tablePilTemp + " as b ");
		requete.append("\n \t WHERE a."+ColumnEnum.ID_SOURCE.getColumnName()+" = '" + idSource + "' ");
		requete.append("\n \t AND a.etape = 1 ; ");

		requete.append(ApiService.resetPreviousPhaseMark(tablePil, idSource, null));

		requete.append("\n set enable_hashjoin = on; ");
		return requete.toString();

	}

}
