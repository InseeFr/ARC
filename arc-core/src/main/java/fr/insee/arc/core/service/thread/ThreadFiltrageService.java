package fr.insee.arc.core.service.thread;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import fr.insee.arc.core.model.BddTable;
import fr.insee.arc.core.model.DbConstant;
import fr.insee.arc.core.model.TraitementRapport;
import fr.insee.arc.core.model.TraitementState;
import fr.insee.arc.core.service.ApiFiltrageService;
import fr.insee.arc.core.service.IMappingServiceConstanteToken;
import fr.insee.arc.core.service.ServiceCommunFiltrageMapping;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.format.Format;
import fr.insee.arc.utils.structure.tree.HierarchicalView;
import fr.insee.arc.utils.textUtils.IConstanteCaractere;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerDispatcher;
import fr.insee.arc.utils.utils.Pair;

/**
 * Thread de filtrage. Comme pour le normage, on parallélise et chaque filtrage
 * s'éxécute dans un thread
 * 
 * @author S4LWO8
 *
 */
public class ThreadFiltrageService extends AbstractThreadService
	implements IConstanteCaractere, IMappingServiceConstanteToken, IRulesUserService {

    private static final Logger LOGGER_THREAD = Logger.getLogger(ThreadFiltrageService.class);

    protected String tableTempFiltrageOk;
    protected String tableFiltrageDataTemp;
    protected String tableTempFiltrageKo;
    protected String tableFiltrageKo;
    protected String tableFiltrageOk;

    private String seuilExclusion;
    private HierarchicalView normeToPeriodiciteToValiditeInfToValiditeSupToRegle;

    public ThreadFiltrageService(int currentIndice, ApiFiltrageService theApi, Connection connexion) {
	super(currentIndice, theApi, connexion);

	this.tableFiltrageDataTemp = FormatSQL.temporaryTableName("filtrage_data_temp");

	this.tableTempFiltrageKo = FormatSQL
		.temporaryTableName(dbEnv(this.getExecutionEnv()) + this.tokenInputPhaseName + "_" + TraitementState.KO);
	this.tableTempFiltrageOk = FormatSQL
		.temporaryTableName(dbEnv(this.getExecutionEnv()) + this.tokenInputPhaseName + "_" + TraitementState.OK);
	this.tableFiltrageKo = globalTableName(this.getExecutionEnv(), this.tokenInputPhaseName, "ko");
	this.tableFiltrageOk = globalTableName(this.getExecutionEnv(), this.tokenInputPhaseName, "ok");

	this.nbEnr = theApi.getNbEnr();

    }

    @Override
    public void initialisationTodo() throws Exception {
	// nettoyer la connexion
	UtilitaireDao.get("arc").executeImmediate(this.connection, "DISCARD TEMP;");

	this.initialiserBatchFiltrage();

    }

    @Override
    public void process() throws Exception {
	this.filtrer();

    }

    @Override
    public void finalizePhase() throws Exception {
	this.insertionFinale();

    }


    /**
     * Cette méthode initialise le contexte d'exécution du filtrage.<br/>
     * Récupère dans la table de pilotage la liste des fichiers à traiter et les
     * priorise.<br/>
     *
     *
     * @throws SQLException
     */
    public void initialiserBatchFiltrage() throws SQLException {

	// création de la table de pilotage temporaire
	UtilitaireDao.get(DbConstant.POOL_NAME).executeBlock(this.connection,
		getRequestTocreateTablePilotageIdSource(this.getTablePilTemp(), this.getTablePilTempThread(), this.idSource));

	// marquer le jeu de regle
	UtilitaireDao.get("arc").executeBlock(this.connection, marqueJeuDeRegleApplique(this.getTablePilTempThread()));

	UtilitaireDao.get("arc").dropTable(this.connection, this.tableTempFiltrageOk, this.tableFiltrageDataTemp,
		this.tableTempFiltrageKo);
	this.seuilExclusion = UtilitaireDao.get("arc").getString(this.connection, new StringBuilder(
		"SELECT valeur FROM " +  this.bddTable.getQualifedName(BddTable.ID_TABLE_SEUIL) + " WHERE NOM = 'filtrage_taux_exclusion_accepte'"));

	// Fabrication de la table de filtrage temporaire
	UtilitaireDao.get("arc").executeBlock(this.connection,
		getRequestToCreateWorkingTable(this.getTablePrevious(), this.tableFiltrageDataTemp, this.idSource));

	LoggerDispatcher.info("Création de la table temporaire filtrage_ko", LOGGER_THREAD);
	UtilitaireDao.get("arc").executeBlock(this.connection,
		FormatSQL.createAsSelectFrom(this.tableTempFiltrageKo, this.tableFiltrageDataTemp, "false"));

	LoggerDispatcher.info("Création de la table temporaire filtrage_ok", LOGGER_THREAD);
	UtilitaireDao.get("arc").executeBlock(this.connection,
		FormatSQL.createAsSelectFrom(this.tableTempFiltrageOk, this.tableFiltrageDataTemp, "false"));

	// PAS DELETE THIS
	// if (logger.isInfoEnabled()) {
	// LoggerDispatcher.info("Création des indexes sur la table de contrôle
	// temporaire", logger);
	// }
	// creerIndexTableControleTemporaire(this.connexion, this.tableTempControleOk);
    }

    /**
     * "id_norme", "periodicite", "validite_inf", "validite_sup", "variable_metier",
     * "expr_regle_col"
     *
     * @param aRegleActive
     * @return
     */

    public static HierarchicalView calculerNormeToPeriodiciteToValiditeInfToValiditeSupToRegle(
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
     * @throws SQLException
     */
    public void filtrer() throws SQLException {
	// if (logger.isInfoEnabled()) {
	// LoggerDispatcher.info("Nombre de lignes à traiter : " +
	// UtilitaireDao.get("arc").getCount(this.connexion,
	// this.tableTempControleOk), logger);
	// }
	if (LOGGER_THREAD.isInfoEnabled()) {
	    LoggerDispatcher.info("Table des données à filtrer utilisée : " + this.tableFiltrageDataTemp, LOGGER_THREAD);
	}

	List<List<String>> regleActive = Format
		.patch(UtilitaireDao.get("arc").executeRequestWithoutMetadata(this.connection,
			/**
			 * La requête de sélection de la relation
			 */
			getRegles(this.idSource, this.bddTable.getQualifedName(BddTable.ID_TABLE_FILTRAGE_REGLE), this.getTablePilTempThread())));

	this.normeToPeriodiciteToValiditeInfToValiditeSupToRegle = calculerNormeToPeriodiciteToValiditeInfToValiditeSupToRegle(
		regleActive);

	ServiceCommunFiltrageMapping.parserRegleGlobale(this.connection, this.executionEnv,
		this.normeToPeriodiciteToValiditeInfToValiditeSupToRegle, "expr_regle_filtre");

	if (LOGGER_THREAD.isInfoEnabled()) {
	    LoggerDispatcher.info("calculerListeColonnes", LOGGER_THREAD);
	}
	Set<String> listeRubrique = ServiceCommunFiltrageMapping.calculerListeColonnes(this.connection,
		this.tableFiltrageDataTemp);
	if (LOGGER_THREAD.isInfoEnabled()) {
	    LoggerDispatcher.info("Fin calculerListeColonnes", LOGGER_THREAD);
	}

	/**
	 * UtilitaireDao.get("arc").getColumns(this.connexion, new ArrayList<String>(),
	 * this.tableTempControleOk);
	 */

	if (LOGGER_THREAD.isInfoEnabled()) {
	    LoggerDispatcher.info("parserRegleCorrespondanceFonctionnelle", LOGGER_THREAD);
	}
	parserRegleCorrespondanceFonctionnelle(this.normeToPeriodiciteToValiditeInfToValiditeSupToRegle, listeRubrique,
		"expr_regle_filtre");
	if (LOGGER_THREAD.isInfoEnabled()) {
	    LoggerDispatcher.info("Fin parserRegleCorrespondanceFonctionnelle", LOGGER_THREAD);
	}

	if (LOGGER_THREAD.isInfoEnabled()) {
	    LoggerDispatcher.info("Exécution du filtrage : insertion dans les tables de travail.", LOGGER_THREAD);
	}

	StringBuilder requete = getRequeteFiltrageIntermediaire(this.executionEnv, this.tableFiltrageDataTemp,
		this.tableTempFiltrageOk, this.tableTempFiltrageKo,
		this.normeToPeriodiciteToValiditeInfToValiditeSupToRegle, this.seuilExclusion, this.getTablePilTempThread());

	UtilitaireDao.get("arc").executeBlock(this.connection,
		"set enable_nestloop=off;" + requete + "set enable_nestloop=on;");

    }

    /**
     * On sort les données des tables temporaires du module vers : - les tables
     * définitives du filtrage (filtrage_ok, filtrage_ok_todo et filtrage_ko) - la
     * table de piltage globale
     *
     * IMPORTANT : les ajouts ou mise à jours de données sur les tables de
     * l'application doivent avoir lieu dans un même bloc de transaction (ACID)
     * 
     * @throws Exception
     *
     */
    public void insertionFinale() throws Exception {

	// créer les tables héritées
	String tableIdSourceOK = tableOfIdSource(this.tableFiltrageOk, this.idSource);
	createTableInherit( this.tableTempFiltrageOk, tableIdSourceOK);
	String tableIdSourceKO = tableOfIdSource(this.tableFiltrageKo, this.idSource);
	createTableInherit( this.tableTempFiltrageKo, tableIdSourceKO);

	StringBuilder requete = new StringBuilder();

	if (paramBatch == null) {
	    requete.append(FormatSQL
		    .tryQuery("alter table " + tableIdSourceOK + " inherit " + this.tableFiltrageOk + "_todo;"));
	    requete.append(
		    FormatSQL.tryQuery("alter table " + tableIdSourceOK + " inherit " + this.tableFiltrageOk + ";"));
	    requete.append(
		    FormatSQL.tryQuery("alter table " + tableIdSourceKO + " inherit " + this.tableFiltrageKo + ";"));
	} else {
	    requete.append(FormatSQL
		    .tryQuery("alter table " + tableIdSourceOK + " inherit " + this.tableFiltrageOk + "_todo;"));
	    requete.append(
		    FormatSQL.tryQuery("alter table " + tableIdSourceKO + " inherit " + this.tableFiltrageKo + ";"));
	}

	requete.append(this.marquageFinal(this.getTablePilTemp(), this.getTablePilTempThread()));
	UtilitaireDao.get("arc").executeBlock(connection, requete);

	UtilitaireDao.get("arc").dropTable(this.connection, this.tableTempFiltrageOk, this.tableFiltrageDataTemp,
		this.tableTempFiltrageKo);
    }

    public StringBuilder getRequeteFiltrageIntermediaire(String envExecution, String aTableControleOk,
	    String aTableFiltrageOk, String aTableFiltrageKo,
	    HierarchicalView aNormeToPeriodiciteToValiditeInfToValiditeSupToRegle, String excludedRate,
	    String aTablePilotage) throws SQLException {
	StringBuilder requete = new StringBuilder();

	/**
	 * Sont traités tous les fichiers pour lesquels un jeu de règles est trouvé.
	 */
	String aTableControleCount = aTableControleOk.replace(".", "_") + "_COUNT";
	requete.append(FormatSQL.dropTable(aTableControleCount));

	requete.append("\n create temporary table " + aTableControleCount + " " + FormatSQL.WITH_NO_VACUUM
		+ " AS SELECT id_source, id, (");
	// requete.append("CASE WHEN false THEN 2 ");

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

	// for (HierarchicalView idNorme :
	// aNormeToPeriodiciteToValiditeInfToValiditeSupToRegle.children()) {
	// for (HierarchicalView periodicite : idNorme.children()) {
	// for (HierarchicalView validiteInf : periodicite.children()) {
	// for (HierarchicalView validiteSup : validiteInf.children()) {
	// requete.append("\n WHEN EXISTS(SELECT 1 FROM " + aTablePilotage + " pil");
	// requete.append("\n WHERE pil.id_norme='" + idNorme.getLocalRoot() + "' ");
	// requete.append("\n AND pil.periodicite='" + periodicite.getLocalRoot() + "'
	// ");
	// requete.append("\n AND pil.validite_inf='" + validiteInf.getLocalRoot() + "'
	// ");
	// requete.append("\n AND pil.validite_sup='" + validiteSup.getLocalRoot() + "'
	// ");
	// requete.append("\n AND pil.id_source = ctrl.id_source) ");
	// requete.append("\n THEN CASE WHEN " +
	// validiteSup.getUniqueChild().getLocalRoot() + " THEN 1 ELSE 0 END ");
	// }
	// }
	// }
	// }
	// requete.append("\n ELSE 2 ");
	// requete.append("END)::bigint as check_against_rule ");
	requete.append(")::bigint as check_against_rule ");
	requete.append("\n from " + aTableControleOk + " ctrl; ");
	requete.append("\n COMMIT; ");
	requete.append("\n ANALYZE " + aTableControleCount + ";");
	/**
	 * Calcul du taux de filtrage, id_norme, periodicite, validite_inf, validite_sup
	 */
	requete.append("\n UPDATE " + aTablePilotage + " pil SET taux_ko = rate.excluded_rate ");
	requete.append("\n FROM (SELECT id_source, avg(check_against_rule) as excluded_rate FROM " + aTableControleCount
		+ " GROUP BY id_source) rate ");
	requete.append("\n WHERE rate.id_source = pil.id_source;\n");

	/**
	 * A partir du taux de filtrage écriture de l'état et du rapport
	 */
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
	 * Vont en KO les fichiers sans règles
	 */
	// requete.append("\n UPDATE " + aTablePilotage + " pil SET
	// etat_traitement='{KO}', rapport='" +
	// TraitementRapport.TOUTE_PHASE_AUCUNE_REGLE
	// + "'");
	// requete.append("\n WHERE EXISTS (SELECT 1 FROM " + aTableControleCount
	// + " ctrl WHERE pil.id_source=ctrl.id_source and
	// 2=ctrl.check_against_rule);\n");
	// requete.append("\n COMMIT; ");

	/**
	 * Vont en KO les fichiers sans aucun enregistrement
	 */
	requete.append("\n UPDATE " + aTablePilotage + " pil SET etat_traitement='{KO}',");
	requete.append("\n rapport='" + TraitementRapport.TOUTE_PHASE_AUCUN_ENREGISTREMENT + "'");
	requete.append("\n  WHERE etat_traitement='{ENCOURS}';\n");

	requete.append("\n COMMIT; ");
	requete.append("\n ANALYZE " + aTablePilotage + ";");
	requete.append("\n ANALYZE " + aTableControleOk + ";");
	requete.append("\n COMMIT; ");

	/**
	 * Insertion dans filtrage_ko
	 */
	requete.append("\n INSERT INTO " + aTableFiltrageKo + " SELECT * ");
	requete.append("\n FROM " + aTableControleOk + " a ");
	requete.append("\n WHERE exists (SELECT 1 FROM " + aTablePilotage + " b WHERE etat_traitement='{"
		+ TraitementState.KO + "}' and a.id_source=b.id_source) ");
	requete.append("\n ; ");

	requete.append("\n INSERT INTO " + aTableFiltrageKo + " SELECT * ");
	requete.append("\n FROM " + aTableControleOk + " a ");
	requete.append("\n WHERE not exists (SELECT  FROM " + aTablePilotage + " b WHERE etat_traitement='{"
		+ TraitementState.KO + "}' and a.id_source=b.id_source) ");
	requete.append("\n AND EXISTS (select from " + aTableControleCount
		+ " b where a.id_source=b.id_source and a.id=b.id and b.check_against_rule=1) ");
	requete.append("\n ; ");

	/**
	 * Insertion dans filtrage_ok
	 */
	requete.append("\n INSERT INTO " + aTableFiltrageOk + " SELECT * ");
	requete.append("\n FROM " + aTableControleOk + " a ");
	requete.append("\n WHERE exists (select from " + aTableControleCount
		+ " b where a.id_source=b.id_source and a.id=b.id and b.check_against_rule=0) ");
	requete.append(
		"\n AND exists (SELECT FROM " + aTablePilotage + " b WHERE etat_traitement in ('{" + TraitementState.OK
			+ "}','{" + TraitementState.OK + "," + TraitementState.KO + "}')  and a.id_source=b.id_source);");

	requete.append(FormatSQL.dropTable(aTableControleCount));

	return requete;
    }

    public static void parserRegleCorrespondanceFonctionnelle(
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
	returned = returned.replaceAll(regexSelectionRubrique, "null");
	onlyNull &= !returned.equals(exprRegle);
	// if (logger.isTraceEnabled()) {
	// LoggerDispatcher.trace("Valeur trouvée pour " + exprRegle + " : " + returned,
	// logger);
	// }
	return new Pair<Boolean, String>(onlyNull, returned);
    }

    /**
     * @return the normeToPeriodiciteToValiditeInfToValiditeSupToRegle
     */
    public final HierarchicalView getNormeToPeriodiciteToValiditeInfToValiditeSupToRegle() {
	return this.normeToPeriodiciteToValiditeInfToValiditeSupToRegle;
    }

    /**
     * @param normeToPeriodiciteToValiditeInfToValiditeSupToRegle
     *            the normeToPeriodiciteToValiditeInfToValiditeSupToRegle to set
     */
    public final void setNormeToPeriodiciteToValiditeInfToValiditeSupToRegle(
	    HierarchicalView normeToPeriodiciteToValiditeInfToValiditeSupToRegle) {
	this.normeToPeriodiciteToValiditeInfToValiditeSupToRegle = normeToPeriodiciteToValiditeInfToValiditeSupToRegle;
    }

    @Override
    public boolean initialize() {
	// TODO Auto-generated method stub
	return false;
    }





}
