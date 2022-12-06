package fr.insee.arc.core.service.thread;

import java.sql.Connection;
import java.sql.SQLClientInfoException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementRapport;
import fr.insee.arc.core.service.ApiFiltrageService;
import fr.insee.arc.core.service.ApiService;
import fr.insee.arc.core.service.engine.ServiceCommunFiltrageMapping;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.format.Format;
import fr.insee.arc.utils.structure.tree.HierarchicalView;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.Pair;
import fr.insee.arc.utils.utils.Sleep;

/**
 * Thread de filtrage. Comme pour le normage, on parallélise et chaque filtrage
 * s'éxécute dans un thread
 * 
 * @author S4LWO8
 *
 */
public class ThreadFiltrageService extends ApiFiltrageService implements Runnable {

	private static final Logger logger = LogManager.getLogger(ThreadFiltrageService.class);
	private int indice;

	private String tableFiltragePilTemp;
	private String tableTempFiltrageOk;
	private String tableFiltrageDataTemp;
	private String tableTempFiltrageKo;
	private String tableFiltrageKo;
	private String tableFiltrageOk;
	
    public static final String REGEX_SELECTION_RUBRIQUE = "\\{[^\\{:\\}]*\\}";


	public ThreadFiltrageService(Connection connexion, int currentIndice, ApiFiltrageService theApi) {

		this.indice = currentIndice;
		this.setEnvExecution(theApi.getEnvExecution());
		this.idSource = theApi.getTabIdSource().get(ColumnEnum.ID_SOURCE.getColumnName()).get(indice);
		this.setPreviousPhase(theApi.getPreviousPhase());
		this.setCurrentPhase(theApi.getCurrentPhase());
		this.connexion = connexion;
		try {
			this.connexion.setClientInfo("ApplicationName", "Filtrage fichier " + idSource);
		} catch (SQLClientInfoException e) {
			StaticLoggerDispatcher.error(e, LOGGER_APISERVICE);
		}

		this.tableFiltrageDataTemp = "filtrage_data_temp";
		this.tableFiltragePilTemp = "filtrage_pil_Temp";

		this.tableTempFiltrageKo = "tableTempFiltrageKo";
		this.tableTempFiltrageOk = "tableTempFiltrageOk";

		this.tableFiltrageKo = ApiService.globalTableName(this.getEnvExecution(), this.getCurrentPhase(), "ko");
		this.tableFiltrageOk = ApiService.globalTableName(this.getEnvExecution(), this.getCurrentPhase(), "ok");

		this.setTableFiltrageRegle(theApi.getTableFiltrageRegle());
		this.setTableJeuDeRegle(theApi.getTableJeuDeRegle());
		this.setTablePil(theApi.getTablePil());
		this.tablePilTemp = theApi.getTablePilTemp();
		this.setNbEnr(theApi.getNbEnr());
		this.setTablePrevious(theApi.getTablePrevious());
		this.setTabIdSource(theApi.getTabIdSource());
		this.setTableNorme(theApi.getTableNorme());
		this.setTableNormageRegle(theApi.getTableNormageRegle());
		this.setTableSeuil(theApi.getTableSeuil());
		this.setParamBatch(theApi.getParamBatch());

	}

	@Override
	public void run() {
		try {

			this.initialiserBatchFiltrage();

			this.filtrer();

			this.insertionFinale();

		} catch (ArcException e) {
			StaticLoggerDispatcher.error(e, LOGGER_APISERVICE);
			try {
				this.repriseSurErreur(this.connexion, this.getCurrentPhase(), this.tablePil, this.idSource, e,
						"aucuneTableADroper");
			} catch (ArcException e2) {
				StaticLoggerDispatcher.error(e2, LOGGER_APISERVICE);
			}
			Sleep.sleep(PREVENT_ERROR_SPAM_DELAY);
		}

	}

	public void start() {
		StaticLoggerDispatcher.debug("Starting ThreadFiltrageService", LOGGER_APISERVICE);
		if (t == null) {
			t = new Thread(this, indice + "");
			t.start();
		}

	}

	/**
	 * Cette méthode initialise le contexte d'exécution du filtrage.<br/>
	 * Récupère dans la table de pilotage la liste des fichiers à traiter et les
	 * priorise.<br/>
	 *
	 *
	 * @throws ArcException
	 */
	private void initialiserBatchFiltrage() throws ArcException {

		StringBuilder query = new StringBuilder();

		// nettoyage des objets base de données du thread
		query.append(cleanThread());

		// création de la table de pilotage temporaire
		query.append(createTablePilotageIdSource(this.tablePilTemp, this.tableFiltragePilTemp, this.idSource));

		// marquer le jeu de regle
		query.append(marqueJeuDeRegleApplique(this.tableFiltragePilTemp));

		// création des tables temporaires de données
		query.append(createTableTravailIdSource(this.getTablePrevious(), this.tableFiltrageDataTemp, this.idSource));
		StaticLoggerDispatcher.info("Création de la table temporaire filtrage_ko", logger);
		query.append(FormatSQL.createAsSelectFrom(this.tableTempFiltrageKo, this.tableFiltrageDataTemp, "false"));
		StaticLoggerDispatcher.info("Création de la table temporaire filtrage_ok", logger);
		query.append(FormatSQL.createAsSelectFrom(this.tableTempFiltrageOk, this.tableFiltrageDataTemp, "false"));

		UtilitaireDao.get("arc").executeBlock(this.connexion, query);

		// récupération du paramètre de filtrage
		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
		requete.append("SELECT valeur FROM " + this.tableSeuil + " WHERE nom = "
				+ requete.quoteText("filtrage_taux_exclusion_accepte"));
		this.seuilExclusion = UtilitaireDao.get("arc").getString(this.connexion, requete);

	}

	/**
	 * "id_norme", "periodicite", "validite_inf", "validite_sup", "variable_metier",
	 * "expr_regle_col"
	 *
	 * @param aRegleActive
	 * @return
	 */

	private static HierarchicalView calculerNormeToPeriodiciteToValiditeInfToValiditeSupToRegle(
			List<List<String>> aRegleActive) {
		return HierarchicalView.asRelationalToHierarchical(
				// Une description sommaire de cette hiérarchie
				"Norme -> ... -> Variable -> Règle",
				// Les colonnes ordonnées
				Arrays.asList("id_regle", "id_norme", "validite_inf", "validite_sup", "version", "periodicite",
						"expr_regle_filtre", "commentaire"),
				aRegleActive);
	}

	/**
	 * Effectue l'ensemble des opérations qui permettent de transférer l'ensemble
	 * des lignes de la table {@code controle_ok} vers deux tables : <br/>
	 * 1. La table {@code filtrage_ko} retient les lignes sans intérêt métier. Les
	 * règles de décision sont codées dans la table
	 * {@code mapping_filtrage_regle}<br/>
	 * 2. La table {@code filtrage_ok} retient les lignes ayant un intérêt métier.
	 * Les règles de décision sont la négation des règles codées dans la table
	 * {@code mapping_filtrage_regle}<br/>
	 *
	 * @throws ArcException
	 */
	private void filtrer() throws ArcException {
		StaticLoggerDispatcher.info("Table des données à filtrer utilisée : " + this.tableFiltrageDataTemp, logger);

		List<List<String>> regleActive = Format
				.patch(UtilitaireDao.get("arc").executeRequestWithoutMetadata(this.connexion,
						/**
						 * La requête de sélection de la relation
						 */
						new ArcPreparedStatementBuilder(getRegles(this.tableFiltrageRegle, this.tableFiltragePilTemp))));

		this.normeToPeriodiciteToValiditeInfToValiditeSupToRegle = calculerNormeToPeriodiciteToValiditeInfToValiditeSupToRegle(
				regleActive);

		ServiceCommunFiltrageMapping.parserRegleGlobale(this.connexion, this.envExecution,
				this.normeToPeriodiciteToValiditeInfToValiditeSupToRegle, "expr_regle_filtre");

		StaticLoggerDispatcher.info("calculerListeColonnes", logger);
		Set<String> listeRubrique = ServiceCommunFiltrageMapping.calculerListeColonnes(this.connexion,
				this.tableFiltrageDataTemp);
		StaticLoggerDispatcher.info("Fin calculerListeColonnes", logger);

		StaticLoggerDispatcher.info("parserRegleCorrespondanceFonctionnelle", logger);
		parserRegleCorrespondanceFonctionnelle(this.normeToPeriodiciteToValiditeInfToValiditeSupToRegle, listeRubrique,
				"expr_regle_filtre");
		StaticLoggerDispatcher.info("Fin parserRegleCorrespondanceFonctionnelle", logger);

		StaticLoggerDispatcher.info("Exécution du filtrage : insertion dans les tables de travail.", logger);
		StringBuilder requete = getRequeteFiltrageIntermediaire(this.envExecution, this.tableFiltrageDataTemp,
				this.tableTempFiltrageOk, this.tableTempFiltrageKo,
				this.normeToPeriodiciteToValiditeInfToValiditeSupToRegle, this.seuilExclusion,
				this.tableFiltragePilTemp);

		UtilitaireDao.get("arc").executeImmediate(this.connexion,
				"set enable_nestloop=off;" + requete + "set enable_nestloop=on;");

	}

	/**
	 * On sort les données des tables temporaires du module vers : - les tables
	 * définitives du filtrage (filtrage_ok, filtrage_ok_to_do et filtrage_ko) - la
	 * table de piltage globale
	 *
	 * IMPORTANT : les ajouts ou mise à jours de données sur les tables de
	 * l'application doivent avoir lieu dans un même bloc de transaction (ACID)
	 * 
	 * @throws ArcException
	 *
	 */
	private void insertionFinale() throws ArcException {
		
		StringBuilder query=new StringBuilder();
		
		// promote the application user account to full right
		query.append(switchToFullRightRole());

		// créer les tables héritées
		String tableIdSourceOK = tableOfIdSource(this.tableFiltrageOk, this.idSource);
		query.append(createTableInherit(this.tableTempFiltrageOk, tableIdSourceOK));
		String tableIdSourceKO = tableOfIdSource(this.tableFiltrageKo, this.idSource);
		query.append(createTableInherit(this.tableTempFiltrageKo, tableIdSourceKO));

		
		if (paramBatch == null) {
			query.append(FormatSQL.tryQuery("DROP TABLE IF EXISTS " + tableIdSourceKO + ";"));
		}

		//marquage dans la table de pilotage
		query.append(this.marquageFinal(this.tablePil, this.tableFiltragePilTemp, this.idSource));
		
		UtilitaireDao.get("arc").executeBlock(connexion, query);
	}

	private StringBuilder getRequeteFiltrageIntermediaire(String envExecution, String aTableControleOk,
			String aTableFiltrageOk, String aTableFiltrageKo,
			HierarchicalView aNormeToPeriodiciteToValiditeInfToValiditeSupToRegle, String excludedRate,
			String aTablePilotage) throws ArcException {
		StringBuilder requete = new StringBuilder();

		/**
		 * Sont traités tous les fichiers pour lesquels un jeu de règles est trouvé.
		 */
		String aTableControleCount = aTableControleOk.replace(".", "_") + "_COUNT";
		requete.append(FormatSQL.dropTable(aTableControleCount));

		requete.append("\n BEGIN; ");
		requete.append("\n create temporary table " + aTableControleCount + " " + FormatSQL.WITH_NO_VACUUM
				+ " AS SELECT "+ColumnEnum.ID_SOURCE.getColumnName()+", id, (");
		requete.append("\n CASE ");
		boolean hasRegle = false;
		for (HierarchicalView expr : aNormeToPeriodiciteToValiditeInfToValiditeSupToRegle
				.getLevel("expr_regle_filtre")) {
			hasRegle = true;
			requete.append("\n WHEN " + expr.getLocalRoot() + " THEN 1 ELSE 0  ");
		}

		// pas de règle : on garde tout le monde
		if (!hasRegle) {
			requete.append("\n WHEN true THEN 0 ");
		}
		requete.append("\n END ");
		requete.append(")::bigint as check_against_rule ");
		requete.append("\n FROM " + aTableControleOk + " ctrl; ");
		requete.append("\n COMMIT; ");

		requete.append("\n BEGIN; ");
		requete.append("\n ANALYZE " + aTableControleCount + ";");
		/**
		 * Calcul du taux de filtrage, id_norme, periodicite, validite_inf, validite_sup
		 */
		requete.append("\n UPDATE " + aTablePilotage + " pil SET taux_ko = rate.excluded_rate ");
		requete.append("\n FROM (SELECT "+ColumnEnum.ID_SOURCE.getColumnName()+", avg(check_against_rule) as excluded_rate FROM " + aTableControleCount
				+ " GROUP BY "+ColumnEnum.ID_SOURCE.getColumnName()+") rate ");
		requete.append("\n WHERE rate."+ColumnEnum.ID_SOURCE.getColumnName()+" = pil."+ColumnEnum.ID_SOURCE.getColumnName()+";\n");
		requete.append("\n COMMIT; ");

		/**
		 * A partir du taux de filtrage écriture de l'état et du rapport
		 */
		requete.append("\n BEGIN; ");
		requete.append("\n UPDATE " + aTablePilotage + " ");
		requete.append("\n SET etat_traitement= CASE WHEN taux_ko = 0 THEN '{OK}'::text[] ");
		requete.append("\n              WHEN 0<taux_ko AND taux_ko<" + excludedRate + " THEN '{OK,KO}' ::text[] ");
		requete.append("\n              WHEN " + excludedRate + "<=taux_ko THEN '{KO}'::text[] ");
		requete.append("\n          END ");
		requete.append("\n  , rapport = CASE    WHEN " + excludedRate + "<=taux_ko THEN '"
				+ TraitementRapport.TOUTE_PHASE_TAUX_ERREUR_SUPERIEUR_SEUIL + "'  ");
		requete.append("\n              ELSE null ");
		requete.append("\n              END; ");
		requete.append("\n COMMIT; ");

		/**
		 * Vont en KO les fichiers sans aucun enregistrement
		 */
		requete.append("\n BEGIN; ");
		requete.append("\n UPDATE " + aTablePilotage + " pil SET etat_traitement='{KO}',");
		requete.append("\n rapport='" + TraitementRapport.TOUTE_PHASE_AUCUN_ENREGISTREMENT + "'");
		requete.append("\n  WHERE etat_traitement='{ENCOURS}';\n");
		requete.append("\n COMMIT; ");

		requete.append("\n BEGIN; ");
		requete.append("\n ANALYZE " + aTablePilotage + ";");
		requete.append("\n ANALYZE " + aTableControleOk + ";");
		requete.append("\n COMMIT; ");

		/**
		 * Insertion dans filtrage_ko
		 */
		requete.append("\n INSERT INTO " + aTableFiltrageKo + " SELECT * ");
		requete.append("\n FROM " + aTableControleOk + " a ");
		requete.append("\n WHERE exists (SELECT 1 FROM " + aTablePilotage + " b WHERE etat_traitement='{"
				+ TraitementEtat.KO + "}' and a."+ColumnEnum.ID_SOURCE.getColumnName()+"=b."+ColumnEnum.ID_SOURCE.getColumnName()+") ");
		requete.append("\n ; ");

		requete.append("\n INSERT INTO " + aTableFiltrageKo + " SELECT * ");
		requete.append("\n FROM " + aTableControleOk + " a ");
		requete.append("\n WHERE not exists (SELECT * FROM " + aTablePilotage + " b WHERE etat_traitement='{"
				+ TraitementEtat.KO + "}' and a."+ColumnEnum.ID_SOURCE.getColumnName()+"=b."+ColumnEnum.ID_SOURCE.getColumnName()+") ");
		requete.append("\n AND EXISTS (SELECT * FROM " + aTableControleCount
				+ " b where a."+ColumnEnum.ID_SOURCE.getColumnName()+"=b."+ColumnEnum.ID_SOURCE.getColumnName()+" and a.id=b.id and b.check_against_rule=1) ");
		requete.append("\n ; ");

		/**
		 * Insertion dans filtrage_ok
		 */
		requete.append("\n INSERT INTO " + aTableFiltrageOk + " SELECT * ");
		requete.append("\n FROM " + aTableControleOk + " a ");
		requete.append("\n WHERE exists (SELECT * FROM " + aTableControleCount
				+ " b where a."+ColumnEnum.ID_SOURCE.getColumnName()+"=b."+ColumnEnum.ID_SOURCE.getColumnName()+" and a.id=b.id and b.check_against_rule=0) ");
		requete.append(
				"\n AND exists (SELECT * FROM " + aTablePilotage + " b WHERE etat_traitement in ('{" + TraitementEtat.OK
						+ "}','{" + TraitementEtat.OK + "," + TraitementEtat.KO + "}')  and a."+ColumnEnum.ID_SOURCE.getColumnName()+"=b."+ColumnEnum.ID_SOURCE.getColumnName()+");");

		requete.append(FormatSQL.dropTable(aTableControleCount));

		return requete;
	}

	private static void parserRegleCorrespondanceFonctionnelle(
			HierarchicalView aNormeToPeriodiciteToValiditeInfToValiditeSupToRegle, Set<String> aListeRubrique,
			String aNiveauRegle) {
		for (int i = 0; i < aNormeToPeriodiciteToValiditeInfToValiditeSupToRegle.getLevel(aNiveauRegle).size(); i++) {
			Pair<Boolean, String> traitementRegle = remplacerRubriqueDansRegle(
					aNormeToPeriodiciteToValiditeInfToValiditeSupToRegle.getLevel(aNiveauRegle).get(i).getLocalRoot(),
					aListeRubrique);
			aNormeToPeriodiciteToValiditeInfToValiditeSupToRegle.getLevel(aNiveauRegle).get(i)
					.setLocalRoot(traitementRegle.getSecond());
		}
	}

	/**
	 * {@code onlyNull} = l'expression de la règle de filtrage comporte-t-elle
	 * uniquement des rubriques inexistantes.<br/>
	 * {@code regle} = la règle instanciée
	 *
	 * @param exprRegle
	 * @param aListeRubrique
	 * @return {@code (onlyNull)}
	 */
	private static Pair<Boolean, String> remplacerRubriqueDansRegle(String exprRegle, Set<String> aListeRubrique) {
		String returned = exprRegle;
		for (String rubrique : aListeRubrique) {
			returned = returned.replaceAll("(?i)\\{" + rubrique + "\\}", rubrique);
		}
		boolean onlyNull = returned.equalsIgnoreCase(exprRegle);
		returned = returned.replaceAll(REGEX_SELECTION_RUBRIQUE, "null");
		onlyNull &= !returned.equals(exprRegle);
		return new Pair<>(onlyNull, returned);
	}

	/**
	 * @return the normeToPeriodiciteToValiditeInfToValiditeSupToRegle
	 */
	public final HierarchicalView getNormeToPeriodiciteToValiditeInfToValiditeSupToRegle() {
		return this.normeToPeriodiciteToValiditeInfToValiditeSupToRegle;
	}

	/**
	 * @param normeToPeriodiciteToValiditeInfToValiditeSupToRegle the
	 *                                                            normeToPeriodiciteToValiditeInfToValiditeSupToRegle
	 *                                                            to set
	 */
	public final void setNormeToPeriodiciteToValiditeInfToValiditeSupToRegle(
			HierarchicalView normeToPeriodiciteToValiditeInfToValiditeSupToRegle) {
		this.normeToPeriodiciteToValiditeInfToValiditeSupToRegle = normeToPeriodiciteToValiditeInfToValiditeSupToRegle;
	}

	@Override
	public Thread getT() {
		return t;
	}

	public void setT(Thread t) {
		this.t = t;
	}

	@Override
	public Connection getConnexion() {
		return connexion;
	}

	public void setConnexion(Connection connexion) {
		this.connexion = connexion;
	}

}
