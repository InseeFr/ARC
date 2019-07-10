package fr.insee.arc.core.service.thread;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import fr.insee.arc.core.model.BddTable;
import fr.insee.arc.core.model.DbConstant;
import fr.insee.arc.core.model.RuleSets;
import fr.insee.arc.core.model.TraitementState;
import fr.insee.arc.core.service.AbstractPhaseService;
import fr.insee.arc.core.service.ApiControleService;
import fr.insee.arc.core.service.ServiceRuleSets;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerDispatcher;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.SQLExecutor;

/**
 * 
 * 
 * @author S4LWO8
 *
 */
public class ThreadControleService extends AbstractThreadService {
    private static final Logger LOGGER = Logger.getLogger(ThreadControleService.class);

    private String tableTempControleFoo;
    private ServiceRuleSets sjdr;

    protected List<RuleSets> listRuleSets;

    private static final String ALTER_TABLE_INHERIT = "alter table %s inherit %s;";
    public ThreadControleService(int currentIndice, ApiControleService theApi, Connection connexion) {
	super(currentIndice, theApi, connexion);

	this.nbEnr = theApi.getNbEnr();

	this.sjdr = new ServiceRuleSets();

	this.tableTempControleFoo = FormatSQL.temporaryTableName("controle_foo_temp");

    }

    @Override
    public void initialisationTodo() throws Exception {
	// Clean the connection
	UtilitaireDao.get("arc").executeImmediate(this.connection, "DISCARD TEMP;");

	preparation();

    }

    @Override
    public void process() throws Exception {
	execute();

    }

    @Override
    public void finalizePhase() throws Exception {
	finControle();

    }

    /**
     * Prepare data and get usefull rule sets
     *
     * @throws SQLException
     */
    public void preparation() throws SQLException {
	LoggerDispatcher.info("** preparation **", LOGGER);

	StringBuilder blocPrep = new StringBuilder();

	LoggerDispatcher.info("Create thread specific pilotage table", LOGGER);
	blocPrep.setLength(0);
	blocPrep.append(
		getRequestTocreateTablePilotageIdSource(this.getTablePilTemp(), this.getTablePilTempThread(), this.idSource));
	blocPrep.append(resetPilotageTable(this.getTablePilTempThread()));
	UtilitaireDao.get("arc").executeBlock(this.connection, blocPrep);

	LoggerDispatcher.info("Tag rule sets to apply ", LOGGER);
	blocPrep.setLength(0);
	blocPrep.append(marqueJeuDeRegleApplique(this.getTablePilTempThread()));
	UtilitaireDao.get("arc").executeBlock(this.connection, blocPrep);

	blocPrep.setLength(0);
	LoggerDispatcher.info("Create thread specific work table", LOGGER);
	blocPrep.append(getRequestToCreateWorkingTable(this.getTablePrevious(),
		this.bddTable.getQualifedName(BddTable.ID_TABLE_POOL_DATA), this.idSource,
		"'0'::text collate \"C\" as controle, null::text[] collate \"C\" as brokenrules"));

	UtilitaireDao.get("arc").executeBlock(this.connection, blocPrep);

	LoggerDispatcher.info("Get rule sets bind to data", LOGGER);
	this.setListJdr(
		this.sjdr.recupJeuDeRegle(this.connection, this.bddTable.getQualifedName(BddTable.ID_TABLE_POOL_DATA),
			this.bddTable.getQualifedName(BddTable.ID_TABLE_RULESETS_BAS)));

	LoggerDispatcher.info("Get control rules bind to rule sets", LOGGER);
	for (RuleSets jdr : this.getListJdr()) {
	    this.sjdr.fillRegleControle(this.connection, jdr,
		    this.bddTable.getQualifedName(BddTable.ID_TABLE_CONTROLE_REGLE));
	}
    }

    /**
     * 
     * @param tablePilTempThread : pilotage table to rese
     * @return sql query
     */
    protected String resetPilotageTable(String tablePilTempThread) {
	StringBuilder requete = new StringBuilder();
	requete.append("\n UPDATE " + tablePilTempThread + " set etat_traitement=NULL ");
	requete.append(";");
	return requete.toString();
    }

    /**
     * Method to control a table
     * Méthode pour controler une table
     *
     * @throws SQLException
     */
    public void execute() throws Exception {
	LoggerDispatcher.info(String.format("** process CONTROL on table :%s **",
		this.bddTable.getQualifedName(BddTable.ID_TABLE_POOL_DATA) ), LOGGER);

	for (RuleSets jdr : this.getListJdr()) {
	    this.sjdr.executeJeuDeRegle(this.connection, jdr,
		    this.bddTable.getQualifedName(BddTable.ID_TABLE_POOL_DATA));
	}

    }

    /**
     * Tag controle='1' controled table's reccords
     *
     * @param tableControle
     * @return sql query
     */
    public String marqueEnregistrementSansRegle(String tableControle) {
	StringBuilder requete = new StringBuilder();
	requete.append("UPDATE " + tableControle + " a ");
	requete.append("\n SET controle='1' ");
	requete.append("\n WHERE EXISTS(    SELECT 1 ");
	requete.append("\n      FROM " + this.getTablePilTempThread() + " b ");
	requete.append("\n      WHERE phase_traitement='CONTROLE' ");
	requete.append("\n          AND 'KO'=ANY(etat_traitement) ");
	requete.append("\n          AND a.id_source=b.id_source); ");
	return requete.toString();
    }


    /**
     * Tag a state for a batch of files containt in pilotageTable
     * Methode grade for the temporary piloage table. No check on the phase
     * because the table contain only control
     *
     * @param newState
     * @param pilotageTable
     * @return an sql query
     */
    public String stateChange (String newState, String pilotageTable) {
	StringBuilder query = new StringBuilder();
	query.append(" UPDATE " + this.getTablePilTempThread() + " b ");
	query.append(" SET  etat_traitement=etat_traitement||'{" + newState + "}' ");
	query.append(" WHERE exists (select 1 from " + pilotageTable + " a where a.id_source=b.id_source); ");
	return query.toString();
    }

    /**
     * Méthode à passer après les controles
     *
     * @throws Exception
     */
    @SQLExecutor
    public void finControle() throws Exception {
	LoggerDispatcher.info("finControle", LOGGER);

	LoggerDispatcher.info("Initialize some table name", LOGGER);
	String tableOutOkTemp = FormatSQL.temporaryTableName(
		dbEnv(this.getExecutionEnv()) + this.tokenInputPhaseName + "_" + TraitementState.OK + FormatSQL.DOLLAR + threadId);
	String tableOutKoTemp = FormatSQL.temporaryTableName(
		dbEnv(this.getExecutionEnv()) + this.tokenInputPhaseName + "_" + TraitementState.KO + FormatSQL.DOLLAR + threadId);

	String tableOutOk = dbEnv(this.getExecutionEnv()) + this.tokenInputPhaseName + "_" + TraitementState.OK;
	String tableOutKo = dbEnv(this.getExecutionEnv()) + this.tokenInputPhaseName + "_" + TraitementState.KO;

	StringBuilder blocFin = new StringBuilder();

	LoggerDispatcher.info("Create temporary ok and ko table", LOGGER);
	blocFin.append(FormatSQL.dropTable(tableOutOkTemp).toString());
	blocFin.append(FormatSQL.dropTable(tableOutKoTemp).toString());
	blocFin.append(AbstractPhaseService
		.creationTableResultat(this.bddTable.getQualifedName(BddTable.ID_TABLE_POOL_DATA), tableOutOkTemp));
	blocFin.append(AbstractPhaseService
		.creationTableResultat(this.bddTable.getQualifedName(BddTable.ID_TABLE_POOL_DATA), tableOutKoTemp));


	LoggerDispatcher.info("Compute error rate", LOGGER);
	blocFin.append(computeErrorRate(this.bddTable.getQualifedName(BddTable.ID_TABLE_POOL_DATA),
		this.getTablePilTempThread()));
	UtilitaireDao.get("arc").executeBlock(this.connection, blocFin);

	LoggerDispatcher.info("Tag ko if error rate > error step", LOGGER);
	blocFin.append(marquageControleKoSeuil(this.getTablePilTempThread(),
		this.bddTable.getQualifedName(BddTable.ID_TABLE_SEUIL)));
	UtilitaireDao.get("arc").executeBlock(this.connection, blocFin);

	UtilitaireDao.get("arc").executeImmediate(this.connection,
		"vacuum analyze " + this.getTablePilTempThread() + ";");

	LoggerDispatcher.info("Add reccors in final table", LOGGER);
	String listColTableIn = listeColonne(tableOutOkTemp);


	//Insert in table ok if final not completly ko brcause of error_step(process_state is null)
	blocFin.setLength(0);
	LoggerDispatcher.info("Insert in OK", LOGGER);
	blocFin.append(ajoutTableControle(listColTableIn, this.bddTable.getQualifedName(BddTable.ID_TABLE_POOL_DATA),
		tableOutOkTemp, this.getTablePilTempThread(), "etat_traitement is null", "controle='0' AND "));

	// Insert in table KO reccords with controle!=0 and ko file
	LoggerDispatcher.info("Insert in KO", LOGGER);
	blocFin.append(ajoutTableControle(listColTableIn, this.bddTable.getQualifedName(BddTable.ID_TABLE_POOL_DATA),
		tableOutKoTemp, this.getTablePilTempThread(), "etat_traitement='{" + TraitementState.KO + "}'",
		"controle!='0' OR "));

	LoggerDispatcher.info("Reset pilotage table, all state = null", LOGGER);
	blocFin.append(resetPilotageTable(this.getTablePilTempThread()));
	blocFin.append(stateChange( TraitementState.OK.toString(), tableOutOkTemp));
	blocFin.append(stateChange( TraitementState.KO.toString(), tableOutKoTemp));
	UtilitaireDao.get("arc").executeBlock(this.connection, blocFin);

	LoggerDispatcher.info("insert temp table in real tables", LOGGER);
	LoggerDispatcher.info("Create inherited table", LOGGER);
	String tableIdSourceOK = tableOfIdSource(tableOutOk, this.idSource);
	createTableInherit(tableOutOkTemp, tableIdSourceOK);
	String tableIdSourceKO = tableOfIdSource(tableOutKo, this.idSource);
	createTableInherit(tableOutKoTemp, tableIdSourceKO);

	StringBuilder requete = new StringBuilder();

	requete.append(FormatSQL.tryQuery(String.format(ALTER_TABLE_INHERIT,tableIdSourceOK, tableOutOk + "_todo")));
	requete.append(FormatSQL.tryQuery(String.format(ALTER_TABLE_INHERIT,tableIdSourceOK, tableOutOk)));
	if (paramBatch == null) {
	    requete.append(FormatSQL.tryQuery(String.format(ALTER_TABLE_INHERIT,tableIdSourceKO, tableOutKo)));
	    
	}

	requete.append(this.marquageFinal(this.getTablePil(), this.getTablePilTempThread()));
	UtilitaireDao.get("arc").executeBlock(connection, requete);

	blocFin.setLength(0);
	LoggerDispatcher.info("Clean temporary table", LOGGER);
	blocFin.append(FormatSQL.dropTable(tableOutOkTemp).toString());
	blocFin.append(FormatSQL.dropTable(tableOutKoTemp).toString());
	blocFin.append(FormatSQL.dropTable(this.bddTable.getQualifedName(BddTable.ID_TABLE_POOL_DATA)).toString());
	blocFin.append("\nDISCARD SEQUENCES; DISCARD TEMP;");

	UtilitaireDao.get("arc").executeBlock(this.connection, blocFin);

    }

    /**
     * Tag CONTROLE_KO if too many errors
     *
     * @param ID_TABLE_PILOTAGE_TEMP
     *            , table de pilotage des fichiers
     * @param tableSeuil
     *            , table de seuil pour la comparaison
     * @return a sql query
     */
    public String marquageControleKoSeuil(String tablePilTempThread, String tableSeuil) {
	StringBuilder request = new StringBuilder();
	request.append("WITH ");
	request.append("seuil AS (  SELECT valeur ");
	request.append("        FROM " + tableSeuil + " ");
	request.append("        WHERE nom='s_taux_erreur'), ");
	request.append("prep AS (   SELECT id_source,taux_ko,valeur ");
	request.append("        FROM " + tablePilTempThread + ", seuil ");
	request.append("        WHERE taux_ko > valeur) ");
	request.append("UPDATE " + tablePilTempThread + " ");
	request.append("\n\t SET etat_traitement='{" + TraitementState.KO.toString() + "}', ");
	request.append("\n\t\t rapport='Fichier avec trop d''erreur' ");
	request.append("\n\t WHERE id_source in (SELECT distinct id_source FROM prep); ");
	return request.toString();
    }

    /**
     * Compute error rate (row with controle !=0) ans update pilotage table
     * @param tableIn
     *            , la table ayant subi des controles
     * @param tablePil
     *            , la table de pilotage des fichiers (à mettre à jour)
     * @return sql query
     */
    public String computeErrorRate(String tableIn, String tablePil) {
	StringBuilder request = new StringBuilder();
	request.append("\n DROP TABLE IF EXISTS " + this.tableTempControleFoo + " CASCADE; ");
	request.append("\n CREATE ");
	if (!this.tableTempControleFoo.contains(".")) {
	    request.append("\n TEMPORARY ");
	}
	request.append("\n TABLE " + this.tableTempControleFoo
		+ " with (autovacuum_enabled = false, toast.autovacuum_enabled = false) as select * FROM (");
	request.append("\n \t with erreur AS(   SELECT id_source, count(id_source)::numeric as nb_erreur ");
	request.append("\n          FROM " + tableIn + " ");
	request.append("\n          WHERE controle != '0' ");
	request.append("\n          GROUP BY id_source), ");
	request.append("\n tot AS (SELECT id_source, count(id_source)::numeric AS nb_enr ");
	request.append("\n      FROM " + tableIn + " ");
	request.append("\n      GROUP BY id_source), ");
	request.append("\n maj AS (SELECT   tot.id_source,");
	request.append("\n              (CASE   WHEN erreur.nb_erreur is null ");
	request.append("\n                  THEN 0 ");
	request.append("\n                  ELSE erreur.nb_erreur/tot.nb_enr ");
	request.append("\n              END)::numeric as taux_ko ");
	request.append(
		"\n      FROM tot LEFT JOIN (SELECT * FROM erreur) AS erreur ON tot.id_source=erreur.id_source) ");
	request.append("\n select * from maj) foo; ");

	request.append("\n UPDATE " + tablePil + " a SET taux_ko = b.taux_ko from " + this.tableTempControleFoo
		+ " b WHERE a.id_source = b.id_source; ");
	request.append("\n DROP TABLE IF EXISTS " + this.tableTempControleFoo + " CASCADE; ");
	return request.toString();
    }

    /**
     * Insert data from a table to another with selection condition
     *
     * @param listColTableIn
     *
     * @param phase
     *
     * @param tableIn
     *            la table des données à insérer
     * @param tableOut
     *            la table réceptacle
     * @param tablePilTempThread
     *            la table de pilotage des fichiers
     * @param etatNull
     *            pour sélectionner certains fichiers
     * @param condEnregistrement
     *            la condition pour filtrer la recopie
     * @return sql query
     */
    public String ajoutTableControle(String listColTableIn, String tableIn, String tableOut, String tablePilTempThread,
	    String condFichier, String condEnregistrement) {

	StringBuilder request = new StringBuilder();
	request.append("\n INSERT INTO " + tableOut + "(" + listColTableIn + ")");
	request.append("\n \t   SELECT " + listColTableIn + " ");
	request.append("\n \t   FROM " + tableIn + " a ");
	request.append("\n \t   WHERE " + condEnregistrement + " ");
	request.append("\n \t   EXISTS (select 1 from  " + tablePilTempThread + " b where a.id_source=b.id_source and "
		+ condFichier + ");");
	return request.toString();
    }

    /**
     * Return the table column list, with an , separator
     *
     * @param connexion
     * @param tableIn
     * @return a string like col1,col2,col3 ...
     */
    @SQLExecutor
    public String listeColonne( String tableIn) {
	ArrayList<ArrayList<String>> result = new ArrayList<>();
	try {
	    result = UtilitaireDao.get(DbConstant.POOL_NAME).executeRequest(this.connection, FormatSQL.listeColonne(tableIn));

	} catch (SQLException ex) {
	    LoggerHelper.error(LOGGER, AbstractPhaseService.class, "listeColonne()", ex);
	}
	StringBuilder listCol = new StringBuilder();
	if (result.size() >= 2) {// les données ne sont qu'à partir du 3e
	    // élement (1er noms, 2e types)
	    for (int i = 2; i < result.size(); i++) {
		if (i == 2) {// initialisation de la liste (pas de virgule)
		    listCol.append(result.get(i).get(0));
		} else {
		    listCol.append("," + result.get(i).get(0));
		}
	    }
	}
	return listCol.toString();
    }

    // Getter et Setter
    public ServiceRuleSets getSjdr() {
	return this.sjdr;
    }

    public void setSjdr(ServiceRuleSets sjdr) {
	this.sjdr = sjdr;
    }

    public List<RuleSets> getListJdr() {
	return this.listRuleSets;
    }

    public void setListJdr(List<RuleSets> listJdr) {
	this.listRuleSets = listJdr;
    }

    @Override
    public boolean initialize() {
	return false;
    }

}
