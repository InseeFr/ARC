package fr.insee.arc.core.service.global.dao;

import java.text.SimpleDateFormat;
import java.util.Date;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.model.BatchMode;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.bo.ArcDateFormat;
import fr.insee.arc.core.service.global.scalability.ScalableConnection;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.security.SqlInjectionChecked;
import fr.insee.arc.utils.structure.GenericBean;

public class ThreadOperations {
		
	private ScalableConnection connexion;

	private String tablePilotageGlobale;
	
	private String tablePilotagePhase;
	
	private String tablePilotageThread;

	private String tablePrevious;
	
	private String paramBatch;	

	private String idSource;
	
	private TraitementPhase currentExecutedPhase;

	private boolean beginNextPhase;
	
	private boolean cleanPhase;

	
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
	public ThreadOperations(TraitementPhase currentExecutedPhase, boolean beginNextPhase, boolean cleanPhase, ScalableConnection connexion, String tablePilotageGlobale, String tablePilotagePhase, String tablePilotageThread,
			String tablePrevious, String paramBatch, String idSource) {
		super();
		this.currentExecutedPhase = currentExecutedPhase;
		this.beginNextPhase = beginNextPhase;
		this.cleanPhase = cleanPhase;
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
		if (cleanPhase)
		{
			query.append(ThreadOperations.cleanThread());
		}

		// création des tables temporaires de données
		query.append(createTablePilotageIdSource(tablePilotagePhase, tablePilotageThread, idSource));

		// enregistrement de la date de traitement du fichier
		// marquer par défaut la phase et que le traitement s'est bien déroulé
		
		query.append(markDefaultValuesOnTablePilotageIdSource());

		
		// if scalable thread
		if (connexion.isScaled()) {		
			// create the pilotage table of the file on the coordinator nod
			UtilitaireDao.get(0).executeRequest(connexion.getCoordinatorConnection(), query);
		
			query = new ArcPreparedStatementBuilder();

			// nettoyage des objets base de données du thread
			if (cleanPhase)
			{
				query.append(ThreadOperations.cleanThread());
			}

			// création des tables temporaires de données
			// get the records from tablePilotageThread in coordinator
			GenericBean gb = new GenericBean(
					UtilitaireDao.get(0).executeRequest(connexion.getCoordinatorConnection(),
							new ArcPreparedStatementBuilder("SELECT * FROM " + tablePilotageThread)));
			// copy them in the table tablePilotageThread located on the executor nod
			query.append(query.copyFromGenericBean(tablePilotageThread, gb));

		}
		return query;
	}

	private ArcPreparedStatementBuilder markDefaultValuesOnTablePilotageIdSource() {
		
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();

		query.append("UPDATE "+tablePilotageThread);
		query.append(" set date_traitement=to_timestamp('" + new SimpleDateFormat(ArcDateFormat.TIMESTAMP_FORMAT_CONVERSION.getApplicationFormat()).format(new Date()) + "','" + ArcDateFormat.TIMESTAMP_FORMAT_CONVERSION.getDatastoreFormat()+"')" );
		query.append(",etat_traitement = '{" + TraitementEtat.OK + "}'");
		query.append(",phase_traitement = '" + this.currentExecutedPhase + "'");
		query.append(";");
		
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

		if (paramBatch != null && !paramBatch.equals(BatchMode.KEEP_INTERMEDIATE_DATA)) {
			query.append("DROP TABLE IF EXISTS "+HashFileNameConversion.tableOfIdSource(this.tablePrevious,idSource)+";");
		}
		UtilitaireDao.get(0).executeBlock(connexion.getExecutorConnection(), query);

		query = new ArcPreparedStatementBuilder();
		if (connexion.isScaled()) {
			GenericBean gb = new GenericBean(UtilitaireDao.get(0).executeRequest(connexion.getExecutorConnection(),
					new ArcPreparedStatementBuilder("SELECT * FROM " + tablePilotageThread)));
			// copy them on the nod
			query.append(query.copyFromGenericBean(tablePilotageThread, gb));

			query.append(marquageFinal(tablePilotageGlobale, tablePilotageThread, idSource));

			UtilitaireDao.get(0).executeBlock(connexion.getCoordinatorConnection(), query);
		}
	}
	
	
	/**
	 * Test batch mode to know if the data output of the previous phase must be dropped
	 * @param paramBatch
	 * @return
	 */
	protected static boolean checkPreviousPhaseDataDropCondition(String paramBatch)
	{
		return paramBatch != null && !paramBatch.equals(BatchMode.KEEP_INTERMEDIATE_DATA);
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
	public static ArcPreparedStatementBuilder createTablePilotageIdSource(String tableIn, String tableOut, String idSource) {
		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
		requete.append("\n CREATE ");
		if (!tableOut.contains(".")) {
			requete.append("TEMPORARY ");
		} else {
			requete.append("UNLOGGED ");
		}
		requete.append(
				"TABLE IF NOT EXISTS " + tableOut + " with (autovacuum_enabled = false, toast.autovacuum_enabled = false) AS ");
		requete.append("\n SELECT * FROM " + tableIn + " ");
		requete.append("\n WHERE " + ColumnEnum.ID_SOURCE.getColumnName() + " = ").appendText(idSource);
		requete.append("\n AND etape = 1 ");
		requete.append("\n ; ");
		return requete;
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
	@SqlInjectionChecked
	private ArcPreparedStatementBuilder marquageFinal(String tablePil, String tablePilTemp, String idSource) {
		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
		
		requete.append("\n set enable_hashjoin=off; ");
		
		requete.append(PilotageOperations.queryResetPreviousPhaseMark(tablePil, idSource, null));
		
		requete.append("\n UPDATE " + tablePil + " a ");
		
		// si la phase suivante n'est pas à commencer
		requete.append("\n SET etat_traitement =  b.etat_traitement, ");
		requete.append("\n phase_traitement = b.phase_traitement, ");
		requete.append("\n id_norme = b.id_norme, ");
		requete.append("\n validite = b.validite, ");
		requete.append("\n periodicite = b.periodicite, ");
		requete.append("\n date_traitement = to_timestamp('" + new SimpleDateFormat(ArcDateFormat.TIMESTAMP_FORMAT_CONVERSION.getApplicationFormat()).format(new Date()) + "','" + ArcDateFormat.TIMESTAMP_FORMAT_CONVERSION.getDatastoreFormat()
				+ "'), ");
		requete.append("\n nb_enr = b.nb_enr, ");
		requete.append("\n rapport = b.rapport, ");
		requete.append("\n validite_inf = b.validite_inf, ");
		requete.append("\n validite_sup = b.validite_sup, ");
		requete.append("\n version = b.version, ");
		requete.append("\n etape = case when b.etat_traitement='{" + TraitementEtat.KO + "}' then 2 else ")
			.append(beginNextPhase?"3":"b.etape").append(" end, ");
		requete.append("\n jointure = b.jointure, ");
		requete.append("\n generation_composite = b.date_traitement::text ");

		// Si on dispose d'un id source on met à jour seulement celui ci
		requete.append("\n FROM " + tablePilTemp + " b ");
		requete.append("\n WHERE a."+ColumnEnum.ID_SOURCE.getColumnName()+" = ").appendText(idSource);
		requete.append("\n AND a.etape = 1 ; ");
		
		
		if (beginNextPhase)
		{
			Date date = new Date();
			requete.append("\n INSERT INTO " + tablePil + " ");
			requete.append("\n (container, " + ColumnEnum.ID_SOURCE.getColumnName()
					+ ", date_entree, id_norme, validite, periodicite, phase_traitement, etat_traitement, date_traitement, nb_enr, etape, jointure) ");
			requete.append("\n SELECT container, " + ColumnEnum.ID_SOURCE.getColumnName()
					+ ", date_entree, id_norme, validite, periodicite, '" + this.currentExecutedPhase.nextPhase() + "' as phase_traitement, '{"
					+ TraitementEtat.ENCOURS + "}' as etat_traitement ");
			requete.append("\n , to_timestamp('" + new SimpleDateFormat(ArcDateFormat.TIMESTAMP_FORMAT_CONVERSION.getApplicationFormat()).format(date) + "','" + ArcDateFormat.TIMESTAMP_FORMAT_CONVERSION.getDatastoreFormat()
					+ "') , nb_enr, 1 as etape, jointure ");
			requete.append("\n FROM " + tablePilTemp + " ");
			requete.append("\n WHERE "+ColumnEnum.ID_SOURCE.getColumnName()+" = ").appendText(idSource);
			requete.append("\n AND etape = 1 ; ");
		}
		requete.append("\n set enable_hashjoin = on; ");

		return requete;

	}

}
