package fr.insee.arc.core.service.thread;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import fr.insee.arc.core.dao.ProcessPhaseDAO;
import fr.insee.arc.core.exception.TheadServiceException;
import fr.insee.arc.core.model.BddTable;
import fr.insee.arc.core.model.DbConstant;
import fr.insee.arc.core.model.TraitementPhaseEntity;
import fr.insee.arc.core.model.TraitementState;
import fr.insee.arc.core.model.TraitementTableExecution;
import fr.insee.arc.core.model.TypeTraitementPhase;
import fr.insee.arc.core.service.AbstractPhaseService;
import fr.insee.arc.core.service.AbstractService;
import fr.insee.arc.core.util.EDateFormat;
import fr.insee.arc.utils.dao.IQueryHandler;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.queryhandler.BatchQueryHandler;
import fr.insee.arc.utils.sqlengine.ContextName;
import fr.insee.arc.utils.sqlengine.Namings;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerDispatcher;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.SQLExecutor;
import lombok.Getter;
import lombok.Setter;

/**
 * Abstract class that all threads have to extends. Provide variables and
 * methods used by all threads
 * 
 * @author Rémi Pépin
 *
 */
@Getter
@Setter
public abstract class AbstractThreadService extends AbstractService implements Runnable {

    private static final String THREAD_ERROR_MESG = "Thread %s encounter a error in %s ";

    protected static final Logger LOGGER = Logger.getLogger(AbstractThreadService.class);

    protected Thread actualThread;

    // the id of the thread
    protected int threadId;

    protected String tokenInputPhaseName;

    protected TraitementPhaseEntity actualPhaseEntity;
    protected IQueryHandler queyHandler;

    public AbstractThreadService(int indice, AbstractPhaseService theApi, Connection aConnexion) {
	super(aConnexion, theApi.getPhaseName(), theApi.getParameterEnv(), theApi.getExecutionEnv(),
		theApi.getDirectoryRoot(), theApi.getNbEnr(), theApi.getParamBatch());

	this.threadId = indice;
	this.idSource = theApi.getFilesToProcess().get(indice).getIdSource();
	this.executionEnv = theApi.getExecutionEnv();
	this.tokenInputPhaseName = theApi.getPhaseName();

	this.connection = aConnexion;
	UtilitaireDao.setConnectionName(this.connection, tokenInputPhaseName + " fichier " + idSource);

	this.bddTable.setNames(new HashMap<String, ContextName>(theApi.getBddTable().getNames()));

	// general table
	this.bddTable.addTable(BddTable.ID_TABLE_OUT_OK, this.executionEnv,
		String.join("_", theApi.getPhaseName(), TraitementState.OK.toString()));
	this.bddTable.addTable(BddTable.ID_TABLE_OUT_KO, this.executionEnv,
		String.join("_", theApi.getPhaseName(), TraitementState.KO.toString()));

	// thread's table
	addTableToBeCleandService(BddTable.ID_TABLE_POOL_DATA,
		FormatSQL.temporaryTableName(String.join("_", theApi.getPhaseName(), "data")));
	addTableToBeCleandService(BddTable.ID_WORKING_TABLE_OK,
		String.join("_", theApi.getPhaseName(), FormatSQL.temporaryTableName("ok_Temp")));
	addTableToBeCleandService(BddTable.ID_WORKING_TABLE_KO,
		String.join("_", theApi.getPhaseName(), FormatSQL.temporaryTableName("ko_Temp")));
	addTableToBeCleandService(BddTable.ID_TABLE_PILOTAGE_TEMP_THREAD,
		temporaryTableName2(tokenInputPhaseName, TraitementTableExecution.PILOTAGE_FICHIER.toString(),
			Integer.toString(indice)));
	

	queyHandler = new BatchQueryHandler(connection);

	getPreviousPhase();

	this.paramBatch = theApi.getParamBatch();

    }

    private void getPreviousPhase() {
	@SuppressWarnings("resource")
	ProcessPhaseDAO processPhaseDAO = new ProcessPhaseDAO(queyHandler,
//		this.bddTable.getNaming(BddTable.ID_TABLE_PHASE_ORDER));
			this.bddTable.getNaming(BddTable.ID_TABLE_IHM_PARAMETTRAGE_ORDRE_PHASE));
	try {
	    this.actualPhaseEntity = processPhaseDAO.getPreviousPhaseOfNorme(getTablePil(),
		    this.tokenInputPhaseName.toUpperCase());
	    this.bddTable.addTable(BddTable.ID_TABLE_PREVIOUS_PHASE,
		    Namings.GET_TABLE_NAME.apply(executionEnv, actualPhaseEntity.getPreviousPhase() + "_OK"));
	} catch (Exception e) {
	    LoggerDispatcher.error("Error when getting the previous phase", e, LOGGER);
	}
    }

    public void start() {
	LoggerDispatcher.info(
		String.format("Starting Thread %s for idSource %s", this.tokenInputPhaseName, this.idSource), LOGGER);
	if (actualThread == null) {
	    actualThread = new Thread(this, threadId + "");
	    actualThread.start();
	}
    }

    /**
     * Three steps:
     * <ol>
     * <li>initialization() :</li>
     * <li>process() :</li>
     * <li>finalization() :</li>
     * </ol>
     */
    public void run() {

	LoggerDispatcher.info("****** Start Thread id_source" + this.getIdSource() + " *******", LOGGER);

	double start = System.currentTimeMillis();
	try {
	    if (this.initialization()) {
		this.process();
	    }
	} catch (Exception ex) {
	    LoggerDispatcher.error(
		    String.format(THREAD_ERROR_MESG, this.tokenInputPhaseName, "initialisation() or process()"), ex,
		    LOGGER);
	    try {
		this.errorRecovery(this.tokenInputPhaseName, this.getTablePilTempThread(), ex, "aucuneTableADroper");
	    } catch (Exception ex2) {
		LoggerDispatcher.error(String.format(THREAD_ERROR_MESG, this.tokenInputPhaseName, "errorRecover()"), ex,
			LOGGER);

	    }

	} finally {
	    try {
		finalization();
	    } catch (Exception ex) {
		LoggerDispatcher.error(String.format(THREAD_ERROR_MESG, this.tokenInputPhaseName, "finalization()"), ex,
			LOGGER);
	    }

	    double finalTime = System.currentTimeMillis() - start;
	    LoggerDispatcher.info("****** End Thread id_source" + this.getIdSource() + " *******"
		    + "\n  process time : " + finalTime + " ms", LOGGER);

	}

    }

    /**
     * Method call at the end of thread process.
     * <ol>
     * <li>finalizePhase ;</li>
     * <li>marquagePilotageFinal :</li>
     * <li>cleanTable : drop all tables in tableToClean</li>
     * </ol>
     * 
     * @throws Exception
     */
    private void finalization() throws Exception {
	finalizePhase();
	tagPilotageFinal();
	cleanTable();
    }

    /**
     * Drop all tables in tableToClean
     */
    private void cleanTable() {
	LoggerDispatcher.info("Clean tables", LOGGER);

	List<String> tableName = this.idTableToClean.stream()//
		.map(id -> getBddTable().getQualifedName(id))//
		.collect(Collectors.toList());

	UtilitaireDao.get("arc").dropTable(this.connection, tableName);

    }

    /**
     * Tag in the final pilotage table all the state of the process file. Is it ok ?
     * ko ? ... Add as well other information taht depend of the phase
     * 
     * @throws SQLException
     */
    @SQLExecutor
    private void tagPilotageFinal() throws SQLException {
	StringBuilder request = new StringBuilder();
	request.append(this.marquageFinal(getTablePil(), this.getTablePilTempThread()));
	request.append(FormatSQL.dropTable(this.getTablePilTempThread()));
	UtilitaireDao.get("arc").executeBlock(this.connection, request);
    }

    /**
     * Each thread have it own initialization process. It's the thread that
     * impletement initialisationTodo(), and initialization() wrap it to catch the
     * exception
     * 
     * @return
     * @throws TheadServiceException
     */
    public boolean initialization() throws TheadServiceException {

	boolean returned = true;
	try {
	    initialisationTodo();
	} catch (Exception e) {
	    throw new TheadServiceException(
		    String.format("Error in initialization() of %s : %s", getIdSource(), e.getMessage()));
	}
	return returned;

    }

    public abstract void initialisationTodo() throws Exception;

    /**
     * Get a SQL request to create the working table use by the thread
     * 
     * @param tableIn
     *            : the table to extract the data
     * @param tableOut
     *            : the output table
     * @param idSource
     *            : the idSource we want
     * @param extraCols
     *            : some extra column (optionnal)
     * @return a sql request
     */
    public String getRequestToCreateWorkingTable(String tableIn, String tableOut, String idSource,
	    String... extraCols) {
	StringBuilder request = new StringBuilder();
	request.append("\n CREATE ");
	if (!tableOut.contains(".")) {
	    request.append("TEMPORARY ");
	} else {
	    request.append("UNLOGGED ");
	}
	request.append("TABLE " + tableOut + FormatSQL.WITH_NO_VACUUM + " AS ");

	request.append("\n SELECT * ");

	if (extraCols.length > 0) {
	    request.append(", " + extraCols[0]);
	}

	request.append("\n FROM " + tableOfIdSource(tableIn, idSource) + "; ");

	return request.toString();
    }

    /**
     * Update the number of reccord in the temporary piloatge table
     * 
     * @param tablePilTemp
     *            : the temporary piloatge table
     * @param threadWorkingTable
     *            : the thread working table
     * @throws SQLException
     */

    public void updateNbEnr(String tablePilTemp, String threadWorkingTable) throws SQLException {
	StringBuilder query = new StringBuilder();

	LoggerDispatcher.info("** updateNbEnr **", LOGGER);

	query.append("\n UPDATE " + tablePilTemp + " a ");
    query.append("\n \t SET nb_enr=(select count(*) from " + threadWorkingTable + ") ");
    query.append("\n \t , etat_traitement='{" + TraitementState.OK + "}'; ");
    

	UtilitaireDao.get(DbConstant.POOL_NAME).executeBlock(this.getConnection(), query);

    }

    /**
     * Copy line from a given idsource in a pilotage table
     * 
     * @param tableIn
     *            : the input pilotage table
     * @param tableOut
     *            : the output pilotage table
     * @param idSource
     *            : the idsource that we want
     * @return
     */
    public String getRequestTocreateTablePilotageIdSource(String tableIn, String tableOut, String idSource) {
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
	requete.append("\n WHERE id_source ='" + idSource + "' ");
	requete.append("\n AND etape = 1 ");
	requete.append("\n ; ");
	return requete.toString();
    }

    /**
     * Clear the proceed files in the previous phase 'todo' table
     * 
     * @param connexion
     * @param tablePilTemp
     * @param tablePrevious
     */
    @SQLExecutor
    public static void deleteTodo(Connection connexion, String tablePilTemp, String tablePrevious, String paramBatch) {
	try {

	    // batch => drop source table
	    // otherwise , supress link with inherited table

		StringBuilder query = new StringBuilder();
		HashMap<String, ArrayList<String>> m = new GenericBean(
				UtilitaireDao.get(DbConstant.POOL_NAME).executeRequest(connexion, "select id_source from " + tablePilTemp + ""))
						.mapContent();
		int count = 0;
		for (String z : m.get("id_source")) {

			count++;
			if (paramBatch == null) {
				query.append("ALTER TABLE " + tableOfIdSource(tablePrevious, z) + " NO INHERIT " + tablePrevious
						+ "_todo ;");
			} else {
				query.append("DROP TABLE IF EXISTS " + tableOfIdSource(tablePrevious, z) + ";");

			}
			
			if (count > FormatSQL.MAX_LOCK_PER_TRANSACTION) {
				UtilitaireDao.get(DbConstant.POOL_NAME).executeBlock(connexion, query);
				query.setLength(0);
				count = 0;
			}
		}
		UtilitaireDao.get(DbConstant.POOL_NAME).executeBlock(connexion, query);

	}catch(

	Exception ex)
	{
		LoggerHelper.error(LOGGER, AbstractPhaseService.class, "deleteTodo()", ex);
	}

    }

    /**
     * Tag the temporary pilotage table with the good rule sets
     *
     * @return an sql query
     */
    protected String marqueJeuDeRegleApplique(String pilTemp) {
	StringBuilder query = new StringBuilder();
	query.append("WITH ");
	query.append(
		"prep AS (SELECT a.id_source, a.id_norme, a.periodicite, b.validite_inf, b.validite_sup, b.version ");
	query.append("	FROM " + pilTemp + " a  ");
	query.append("	INNER JOIN " + this.bddTable.getQualifedName(BddTable.ID_TABLE_RULESETS_BAS)
		+ " b ON a.id_norme=b.id_norme AND a.periodicite=b.periodicite AND b.validite_inf <=a.validite::date AND b.validite_sup>=a.validite::date ");
	query.append("	WHERE phase_traitement='" + this.tokenInputPhaseName + "') ");
	query.append("UPDATE " + pilTemp + " AS a ");
	query.append("SET validite_inf=prep.validite_inf, validite_sup=prep.validite_sup, version=prep.version ");
	query.append("FROM prep ");
	query.append("WHERE a.id_source=prep.id_source AND a.phase_traitement='" + this.tokenInputPhaseName + "'; ");
	return query.toString();
    }

    /**
     * Update all the reccord of tablePil with the reccord of tablePilTemp with the
     * same idSource
     *
     * @param tablePil
     * @param tablePilTemp
     * @return
     */
    public String marquageFinal(String tablePil, String tablePilTemp) {
	return marquageFinal(tablePil, tablePilTemp, "");
    }

    /**
     * Update all the reccord of tablePil with the reccord of tablePilTemp with a
     * specified idSource
     * 
     * @param tablePil
     * @param tablePilTemp
     * @param idSource
     * @return
     */
    public String marquageFinal(String tablePil, String tablePilTemp, String idSource) {
	StringBuilder query = new StringBuilder();
	Date date = new Date();
	SimpleDateFormat formatter = new SimpleDateFormat(EDateFormat.DATE_FORMAT_WITH_SECOND.getValue());
	query.append("\n set enable_hashjoin=off; ");
	query.append("\n UPDATE " + tablePil + " AS a ");
	query.append("\n \t SET etat_traitement =  b.etat_traitement, ");
	query.append("\n \t   id_norme = b.id_norme, ");
	query.append("\n \t   validite = b.validite, ");
	query.append("\n \t   periodicite = b.periodicite, ");
	query.append("\n \t   taux_ko = b.taux_ko, ");
	query.append("\n \t   date_traitement = '" + formatter.format(date) + "', ");
	query.append("\n \t   nb_enr = b.nb_enr, ");
	query.append("\n \t   rapport = b.rapport, ");
	query.append("\n \t   validite_inf = b.validite_inf, ");
	query.append("\n \t   validite_sup = b.validite_sup, ");
	query.append("\n \t   version = b.version, ");
	query.append(
		"\n \t   etape = case when b.etat_traitement='{" + TraitementState.KO + "}' then 2 else b.etape end ");

	if (tokenInputPhaseName.equals(TypeTraitementPhase.LOAD.toString())) {
	    query.append("\n \t   , jointure = b.jointure ");
	}

	// Remove of column jointure after STRUCTURIZE_XML, to big and not usefull
	if (tokenInputPhaseName.equals(TypeTraitementPhase.STRUCTURIZE.toString())) {
	    query.append("\n \t   , jointure = null ");

	}

	// If we don't have an idSource, we update all matching id
	// Otherwise we update only the given one
	query.append("\n \t FROM " + tablePilTemp + " as b ");
	if (idSource.isEmpty()) {
	    query.append("\n \t WHERE b.id_source =  a.id_source");
	} else {
	    query.append("\n \t WHERE b.id_source = '" + idSource + "' ");
	    query.append("\n \t AND a.id_source = '" + idSource + "' ");
	}
	query.append("\n \t AND a.etape = 1 ; ");
	query.append("\n set enable_hashjoin = on; ");
	return query.toString();

    }

    /**
     * Generate a sql request to create a copy of a table
     * 
     * @param extraColumns
     *            : custon columns not in the tableIn table
     * @param tableIn
     *            : the input table
     * @param tableOut
     *            : the output table (can be temporary, or in a schema)
     * @param tablePilTemp
     *            : the pilotage table used to know what idSource do we want
     * @param processState
     *            : specify the processState that we want
     * @return
     */
    public String getRequestToCreateWorkingTableWithState(String extraColumns, String tableIn, String tableOut,
	    String tablePilTemp, String... processState) {
	StringBuilder requete = new StringBuilder();

	if (tableIn.toLowerCase().contains("_todo")) {
	    requete.append(FormatSQL.lock(tableIn));
	}
	requete.append("\n DROP TABLE IF EXISTS " + tableOut + " CASCADE; \n");

	requete.append("\n CREATE ");
	if (!tableOut.contains(".")) {
	    requete.append("TEMPORARY ");
	} else {
	    requete.append("UNLOGGED ");
	}

	requete.append(
		"TABLE " + tableOut + " with (autovacuum_enabled = false, toast.autovacuum_enabled = false) AS ");
	requete.append("( ");
	requete.append("\n    SELECT * " + extraColumns);
	requete.append("\n    FROM " + tableIn + " stk ");
	requete.append("\n    WHERE exists ( SELECT 1  ");
	requete.append("\n            FROM " + tablePilTemp + " pil  ");
	requete.append("\n  where pil.id_source=stk.id_source ");
	if (processState.length > 0) {
	    requete.append(" AND '" + processState[0] + "'=ANY(pil.etat_traitement) ");
	}
	requete.append(" ) ");
	requete.append(");\n");

	return requete.toString();
    }

    /**
     * Create the mother table
     * 
     * @param tableIn
     * @param tableIdSource
     * @throws Exception
     */
    public void createTableInherit(String tableIn, String tableIdSource) throws Exception {

	// Create if the table have reccords
	if (UtilitaireDao.get(DbConstant.POOL_NAME).hasResults(this.connection,
		"SELECT 1 FROM " + tableIn + " LIMIT 1")) {

	    StringBuilder query = new StringBuilder();

	    LoggerDispatcher.info("** createTableOK ** : " + tableIdSource, LOGGER);
	    java.util.Date beginDate = new java.util.Date();

	    query.append("DROP TABLE IF EXISTS " + tableIdSource + ";");
	    query.append("CREATE TABLE " + tableIdSource + " " + FormatSQL.WITH_NO_VACUUM + " AS SELECT * FROM "
		    + tableIn + ";");

	    UtilitaireDao.get("arc").executeBlock(this.connection, query);

	    java.util.Date endDate = new java.util.Date();
	    LoggerDispatcher.info("** createTableOK ** temps : " + (endDate.getTime() - beginDate.getTime()) + " ms",
		    LOGGER);
	}

    }

    /**
     * Create a hash name for a child table
     * 
     * @param tableName
     *            : the inheritated table
     * @param idSource
     *            : the idSource that the table will contain
     * @return a name generate with SHA1 hash
     */
    public static String tableOfIdSource(String tableName, String idSource) {
	String hashText = "";
	MessageDigest m;
	try {
	    m = MessageDigest.getInstance("SHA1");
	    m.update(idSource.getBytes(), 0, idSource.length());
	    hashText = String.format("%1$032x", new BigInteger(1, m.digest()));
	} catch (NoSuchAlgorithmException e) {
	    LoggerDispatcher.error("tableOfIdSource", e, LOGGER);
	}
	return tableName + "_"+CHILD_TABLE_TOKEN+"_" + hashText;
    }

    public String globalTableName(String aExecutionEnv, String aCurrentPhase, String tableName) {
	return dbEnv(aExecutionEnv) + aCurrentPhase + "_" + tableName;
    }

	public Thread getActualThread() {
		return actualThread;
	}

	public void setActualThread(Thread actualThread) {
		this.actualThread = actualThread;
	}

	public String getTablePilTempThread() {
		return this.bddTable.getQualifedName(BddTable.ID_TABLE_PILOTAGE_TEMP_THREAD);
	}

	public String getTokenInputPhaseName() {
		return tokenInputPhaseName;
	}

	public void setTokenInputPhaseName(String tokenInputPhaseName) {
		this.tokenInputPhaseName = tokenInputPhaseName;
	}

	public String getTablePrevious() {
		return bddTable.getQualifedName(BddTable.ID_TABLE_PREVIOUS_PHASE);
	}
}
