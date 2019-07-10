package fr.insee.arc.core.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;

import fr.insee.arc.core.dao.NormeDAO;
import fr.insee.arc.core.model.BddTable;
import fr.insee.arc.core.model.DbConstant;
import fr.insee.arc.core.model.Norme;
import fr.insee.arc.core.model.PilotageEntity;
import fr.insee.arc.core.model.TraitementState;
import fr.insee.arc.core.model.TraitementTableExecution;
import fr.insee.arc.core.util.EDateFormat;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.queryhandler.BatchQueryHandler;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.utils.sqlengine.Namings;
import fr.insee.arc.utils.textUtils.IConstanteNumerique;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerDispatcher;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.SQLExecutor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class AbstractService implements IApiService, IConstanteNumerique {

    public static final String IS_DEV = "fr.insee.arc.environnement.is.dev";
    protected static final Logger LOGGER = Logger.getLogger(AbstractService.class);

    public static final String PRODUCTION_FILE = "production.dummy";
    public static final int PRODUCTION_START_TIME = 22;
    protected int nbThread;
    public static final String ID_SOURCE = "id_source";

    protected Connection connection;
    protected String executionEnv;
    protected String parameterEnv;

    protected Integer nbEnr;

    // Not use at this time
    protected String calendarTable;

    protected String directoryRoot;
    protected String nullString = "[[[#NULL VALUE#]]]";
    protected String paramBatch = null;
    protected int reporting = 0;
    protected String idSource;

    public static final String XML_JOIN_SEPARATOR = "\n";
    protected Boolean todo = false;
    protected List<PilotageEntity> filesToProcess ;
    
    

    /**
     * Simple counter to check excecution loop
     */
    protected Integer cptBoucle = 0;
    protected String tableOutKo;

    protected BddTable bddTable;
    protected List<String> idTableToClean = new ArrayList<>();

    public  PropertiesHandler properties;
    
    public AbstractService(String aCurrentPhase, String aParametersEnvironment, String aEnvExecution,
	    String aDirectoryRoot, Integer aNbEnr, String... paramBatch) {
	LoggerDispatcher.info(String.format("** Service constructor %s**", aCurrentPhase), LOGGER);
	

	createConnectionToDB();
	
	setValues(aCurrentPhase, aParametersEnvironment, aEnvExecution, aDirectoryRoot, aNbEnr, paramBatch);

	LoggerDispatcher.info("** END ApiService constructor **", LOGGER);
    }


    public AbstractService(Connection connexion, String aCurrentPhase, String aParametersEnvironment,
	    String aEnvExecution, String aDirectoryRoot, Integer aNbEnr, String... paramBatch) {

	LoggerDispatcher.info(String.format("** Constructeur service %s**", aCurrentPhase), LOGGER);
	if (connexion != null) {
	    this.connection = connexion;

	} else {

	    createConnectionToDB();
	}

	setValues(aCurrentPhase, aParametersEnvironment, aEnvExecution, aDirectoryRoot, aNbEnr, paramBatch);

	LoggerDispatcher.info("** End  ApiService constructor**", LOGGER);

    }

    public AbstractService() {
    }
    
    /**
     * Set all the values other than connection of an {@link AbstractService}
     * @param aCurrentPhase
     * @param aParametersEnvironment
     * @param aEnvExecution
     * @param aDirectoryRoot
     * @param aNbEnr
     * @param paramBatch
     */
    private void setValues(String aCurrentPhase, String aParametersEnvironment, String aEnvExecution,
	    String aDirectoryRoot, Integer aNbEnr, String... paramBatch) {
	
	properties = PropertiesHandler.getInstance();
	if (paramBatch != null && paramBatch.length > 0) {
	    this.paramBatch = paramBatch[0];
	}
	
	// Phase initialization
	
	this.executionEnv = aEnvExecution;
	
	this.bddTable = new BddTable(aEnvExecution);
	
	// Input table
	this.parameterEnv = aParametersEnvironment;
	this.directoryRoot = aDirectoryRoot;
	
	// Temporary pilotage table and pilotage table
	this.bddTable.addTable(BddTable.ID_TABLE_PILOTAGE_TEMP, Namings.GET_TABLE_NAME.apply(bddTable.getSchema(),
		temporaryTableName2(aCurrentPhase, TraitementTableExecution.PILOTAGE_FICHIER.toString(), "0")));
	
	this.nbEnr = aNbEnr;
    }
    
    /**
     * Open and set the connection to the DB
     */
    private void createConnectionToDB() {
	try {
	    this.connection = UtilitaireDao.get(DbConstant.POOL_NAME).getDriverConnexion();
	} catch (Exception ex) {
	    LoggerHelper.error(LOGGER, AbstractPhaseService.class, "ApiService()", ex);
	}
    }

    /**
     * 
     * Rehabilitation of the pilotage table
     * @param aPhase
     *            : the current phase
     * @param aPilTable
     *            : the pilotage table
     * @param aException
     *            : the exception witch lead here
     * @param sometablesToDrop
     *            : some table to drop (optionnal)
     * 
     * @throws SQLException
     */
    public void errorRecovery(String aPhase, String aPilTable, Exception aException, String... sometablesToDrop) throws SQLException {
	// Cleaning of the connection
	// Getting here means there is an error, so the database is waiting for a
	// transaction end

	this.connection.setAutoCommit(false);
	this.connection.rollback();
	StringBuilder request = new StringBuilder();
	for (int i = 0; i < sometablesToDrop.length; i++) {
	    request.append("DROP TABLE IF EXISTS " + sometablesToDrop[i] + ";");
	}

	request.append("UPDATE " + aPilTable + " set etape=2, etat_traitement= '{" + TraitementState.KO
		+ "}', rapport='" + aException.toString().replace("'", "''").replaceAll("\r", "") + "' ");
	request.append(
		"WHERE phase_traitement='" + aPhase + "' AND etat_traitement='{" + TraitementState.ENCOURS + "}' ;");

	UtilitaireDao.get(DbConstant.POOL_NAME).executeBlock(this.connection, request);
    }

    /**
     * Create {@code tableToBeCreated}, an empty copy table of {@code tableIn}. If
     * {@code tableIn} has a schema, {@code tableToBeCreated} has the same. Else
     * {@code tableToBeCreated} is temporary
     * 
     * @param tableIn
     *            : the table to copy
     * @param tableToBeCreated
     *            : the output table
     * @return an SQL request to copy the table
     */
    public static String creationTableResultat(String tableIn, String tableToBeCreated, Boolean... image) {
	StringBuilder request = new StringBuilder();
	request.append("\n CREATE ");
	if (!tableToBeCreated.contains(".")) {
	    request.append("TEMPORARY ");
	} else {
	    request.append(" ");
	}
	request.append("TABLE " + tableToBeCreated + " ");
	request.append("" + FormatSQL.WITH_NO_VACUUM + " ");
	request.append("as SELECT * FROM " + tableIn + " ");
	if (image.length == 0 || !image[0]) {
	    request.append("where 1=0 ");
	}
	request.append("; ");
	return request.toString();
    }

    /**
     * Initialize the production at declared time
     */
    @SQLExecutor
    public static void startProductionInitialization() {
	LoggerDispatcher.info("startProductionInitialization()", LOGGER);
	
	DateFormat dateFormat = new SimpleDateFormat(EDateFormat.DATE_FORMAT_WITH_HOUR.getValue());
	Date dNow = DateUtils.setHours(new Date(), PRODUCTION_START_TIME);

	try {
	    UtilitaireDao.get("arc").executeRequest(null, "update arc.pilotage_batch set last_init='"
		    + dateFormat.format(dNow) + "', operation=case when operation='R' then 'O' else operation end;");

	} catch (SQLException e) {
	    LoggerDispatcher.error("Error in startProductionInitialization", e, LOGGER);
	}
    }

    /**
     * 
     * Tag the phase ({@code aNewPhase}) and state ({@code aNewState}) of one
     * idSource ({@code anIdSource}) in the pilotage table ({@code aPilotageTable})
     * 
     * @param aPilotageTable
     * @param anIdSource
     * @param aNewPhase
     * @param aNewState
     * @return an SQL request to do that
     * 
     */
    public static StringBuilder pilotageMarkIdsource(String aPilotageTable, String anIdSource, String aNewPhase,
	    String aNewState, String rapport, String... jointure) {
	StringBuilder request = new StringBuilder();
	request.append("UPDATE " + aPilotageTable + " ");
	request.append("SET phase_traitement= '" + aNewPhase + "' ");
	request.append(", etat_traitement= '{" + aNewState + "}' ");
	if (rapport == null) {
	    request.append(", rapport= null ");
	} else {
	    request.append(", rapport= '" + rapport + "' ");
	}

	if (jointure.length > 0) {
	    request.append(", jointure= '" + jointure[0] + "'");
	}

	request.append("WHERE id_source='" + anIdSource + "';\n");
	return request;
    }
    
    /**
     * Get all {@link Norme} in database. Use the {@link NormeDAO} to get them
     * 
     * @return
     */
    protected List<Norme> getAllNorms() {
	List<Norme> listNorm = new ArrayList<>();
	try (NormeDAO normeDAO = new NormeDAO(new BatchQueryHandler(this.connection),
		this.bddTable.getContextName(BddTable.ID_TABLE_NORME_SPECIFIC))) {
	    listNorm = normeDAO.getList();
	} catch (Exception e) {
	    LoggerHelper.error(LOGGER, "Error when requesting the norm list", e);
	}
	return listNorm;
    }

    /**
     * 
     * Return a random generate name with some set text
     * 
     * FIXME
     * 
     * @param aEnvExecution
     * @param aCurrentPhase
     * @param tableName
     * @param suffix
     * @return
     */
    public static String temporaryTableName(String aEnvExecution, String aCurrentPhase, String tableName,
	    String... suffix) {

	if (suffix != null && suffix.length > 0) {
		String suffixJoin = String.join(FormatSQL.DOLLAR, suffix);
    	return FormatSQL.temporaryTableName(dbEnv(aEnvExecution) + aCurrentPhase + "_" + tableName, suffixJoin);
	} else {
	    return FormatSQL.temporaryTableName(dbEnv(aEnvExecution) + aCurrentPhase + "_" + tableName);
	}
    }

    public static String temporaryTableName2(String aCurrentPhase, String tableName, String... suffix) {

	if (suffix != null && suffix.length > 0) {
	    return FormatSQL.temporaryTableName(aCurrentPhase + "_" + tableName, suffix[0]);
	} else {
	    return FormatSQL.temporaryTableName(aCurrentPhase + "_" + tableName);
	}
    }

    /**
     * Add a table to the tables to clean at the end of the service
     * 
     * @param tableId
     * @param tableName
     */
    protected void addTableToBeCleandService(String tableId, String tableName) {
	this.bddTable.addTable(tableId, this.executionEnv, tableName);
	this.idTableToClean.add(tableId);
    }

    /**
     * 
     * Allow the retro compatibility with old database structure
     * 
     * @param anEnv
     * @return the corrected env name
     */
    public static String dbEnv(String env) {
	return env.replace(".", "_") + ".";
    }

    public Connection getConnection() {
	return connection;
    }

    public void setConnection(Connection connexion) {
	this.connection = connexion;
    }

    public String getExecutionEnv() {
	return executionEnv;
    }

    public void setExecutionEnv(String executionEnv) {
	this.executionEnv = executionEnv;
    }

    public String getParameterEnv() {
	return parameterEnv;
    }

    public void setParameterEnv(String parameterEnv) {
	this.parameterEnv = parameterEnv;
    }

    public String getTablePil() {
	return bddTable.getQualifedName(BddTable.ID_TABLE_PILOTAGE_FICHIER);
    }

    public String getTablePilTemp() {
	return bddTable.getQualifedName(BddTable.ID_TABLE_PILOTAGE_TEMP);
    }

    public String getTableCalendrier() {
	return calendarTable;
    }

    public void setTableCalendrier(String tableCalendrier) {
	this.calendarTable = tableCalendrier;
    }

    public String getDirectoryRoot() {
	return directoryRoot;
    }

    public void setDirectoryRoot(String directoryRoot) {
	this.directoryRoot = directoryRoot;
    }

    public String getParamBatch() {
	return paramBatch;
    }

    public void setParamBatch(String paramBatch) {
	this.paramBatch = paramBatch;
    }

    public String getIdSource() {
	return idSource;
    }

    public void setIdSource(String idSource) {
	this.idSource = idSource;
    }


    public String getTableOutKo() {
	return tableOutKo;
    }

    public void setTableOutKo(String tableOutKo) {
	this.tableOutKo = tableOutKo;
    }

    public Integer getNbEnr() {
	return nbEnr;
    }

    public void setNbEnr(Integer nbEnr) {
	this.nbEnr = nbEnr;
    }

    public List<PilotageEntity> getFilesToProcess() {
        return filesToProcess;
    }

    public void setFilesToProcess(List<PilotageEntity> filesToProcess) {
        this.filesToProcess = filesToProcess;
    }

    public BddTable getBddTable() {
	return bddTable;
    }

    public void setBddTable(BddTable bddTable) {
	this.bddTable = bddTable;
    }
}
