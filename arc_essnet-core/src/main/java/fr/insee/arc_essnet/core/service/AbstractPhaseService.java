package fr.insee.arc_essnet.core.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import fr.insee.arc_essnet.core.factory.ApiServiceFactory;
import fr.insee.arc_essnet.core.model.BddTable;
import fr.insee.arc_essnet.core.model.DbConstant;
import fr.insee.arc_essnet.core.model.ServiceReporting;
import fr.insee.arc_essnet.core.model.TraitementState;
import fr.insee.arc_essnet.core.model.TraitementTableExecution;
import fr.insee.arc_essnet.core.model.TypeTraitementPhase;
import fr.insee.arc_essnet.core.util.EDateFormat;
import fr.insee.arc_essnet.utils.dao.UtilitaireDao;
import fr.insee.arc_essnet.utils.utils.FormatSQL;
import fr.insee.arc_essnet.utils.utils.LoggerDispatcher;
import fr.insee.arc_essnet.utils.utils.LoggerHelper;
import fr.insee.arc_essnet.utils.utils.ManipString;
import fr.insee.arc_essnet.utils.utils.SQLExecutor;

@Component
public abstract class AbstractPhaseService extends AbstractService implements IPhaseService {
    public static final String ANY_ETAT_TRAITEMENT = "'=ANY(etat_traitement) ";
    private AbstractXmlApplicationContext context;
    protected static final Logger LOGGER = Logger.getLogger(AbstractPhaseService.class);
    protected String phaseName;

    public AbstractPhaseService() {
	super();
    }

    public AbstractPhaseService(String aCurrentPhase, String aParametersEnvironment, String aEnvExecution,
	    String aDirectoryRoot, Integer aNbEnr, String... paramBatch) {
	super(aCurrentPhase, aParametersEnvironment, aEnvExecution, aDirectoryRoot, aNbEnr, paramBatch);
	LoggerDispatcher.info(String.format("**Phase %s**", aCurrentPhase), LOGGER);

	this.phaseName = aCurrentPhase;
	this.bddTable.addTable(BddTable.ID_TABLE_OUT_KO, bddTable.getSchema(),
		(this.phaseName + "_" + TraitementState.KO).toLowerCase());

    }

    public AbstractPhaseService(Connection connexion, String aCurrentPhase, String aParametersEnvironment,
	    String aEnvExecution, String aDirectoryRoot, Integer aNbEnr, String... paramBatch) {
	super(connexion, aCurrentPhase, aParametersEnvironment, aEnvExecution, aDirectoryRoot, aNbEnr, paramBatch);
	LoggerDispatcher.info(String.format("** Constructeur %s**", aCurrentPhase), LOGGER);
	this.phaseName = aCurrentPhase;

    }

    /**
     * Initialize variables and tables' name
     *
     * @param aEnvExecution
     * @param aPreviousPhase
     * @param aCurrentPhase
     * @param aNbEnr
     */
    public boolean initialize() {
	LoggerDispatcher.info("** initialize **", LOGGER);
	// Check if there is something to process
	if (this.todo) {
	    try {
		UtilitaireDao.get(DbConstant.POOL_NAME).executeBlock(this.connection, connectionConfig());
	    } catch (SQLException ex) {
		LoggerHelper.error(LOGGER, AbstractPhaseService.class, "initialiser()", ex);
	    }
	    register(this.connection, this.phaseName, this.getTablePil(), this.getTablePilTemp(), this.nbEnr);
	}
	// Because of the factory, we wave to manulay run the autowired context

	this.context = new ClassPathXmlApplicationContext("applicationContext.xml");
	AutowireCapableBeanFactory acbFactory = this.context.getAutowireCapableBeanFactory();
	acbFactory.autowireBean(this);
	return this.todo;
    }

    /**
     * Configure the connection
     */
    public StringBuilder connectionConfig() {
	StringBuilder requete = new StringBuilder();
	requete.append(FormatSQL
		.modeParallel(ManipString.substringBeforeFirst(AbstractPhaseService.dbEnv(this.executionEnv), ".")));
	return requete;

    }

    /**
     * Check if there is something to process
     *
     * @param aPilTable
     * @param anOldPhase
     * @return true if there is file to process, false otherwise
     */
    public boolean checkTodo(String aPilTable, String anOldPhase, String aNewPhase) {
	StringBuilder request = new StringBuilder();
	boolean todo = false;
	request.append("SELECT 1 FROM " + aPilTable + " a ");
	request.append(
		"WHERE phase_traitement='" + anOldPhase + "' AND '" + TraitementState.OK + ANY_ETAT_TRAITEMENT);
	request.append("and etape=1 ");
	request.append("UNION ALL ");
	request.append("SELECT 1 FROM " + aPilTable + " a ");
	request.append("WHERE phase_traitement='" + aNewPhase + "' AND '" + TraitementState.ENCOURS
		+ ANY_ETAT_TRAITEMENT);
	request.append("and etape=1 ");
	request.append("limit 1 ");
	try {
	    todo = UtilitaireDao.get(DbConstant.POOL_NAME).hasResults(this.connection, request);
	} catch (Exception ex) {
	    LoggerHelper.error(LOGGER, AbstractPhaseService.class, "checkTodo()", ex);
	}
	return todo;
    }

    /**
     * Tag in {@code aPilTable} the idSource which will be process. If some are
     * already processing the method doesn't tag new one.
     * 
     * Copy the pilotage table in a temporary one {@code aTempPilTable}. This table
     * will be modify in the phase process
     * 
     * An idSource has to be process if and only if it last phase is the previous of
     * this one.
     * 
     * @param connection
     * @param phaseIn
     * @param theActualPhase
     * @param aPilTable
     * @param aTempPilTable
     * @param nbEnr
     * @throws SQLException
     */
    public void register(Connection connection, String theActualPhase, String aPilTable, String aTempPilTable,
	    Integer nbEnr) {
	LoggerDispatcher.info("** register **", LOGGER);
	try {
	    StringBuilder blocInit = new StringBuilder();
	    // If there is no "processing" source, we tag the idSource to process
	    if (!UtilitaireDao.get(DbConstant.POOL_NAME).hasResults(connection,
		    "select 1 from " + aPilTable + " where phase_traitement='" + theActualPhase + "' AND '"
			    + TraitementState.ENCOURS +ANY_ETAT_TRAITEMENT+" and etape=1 limit 1")) {
		blocInit.append(sourceSelection(theActualPhase, nbEnr));
	    }
	    blocInit.append(copieTablePilotage(theActualPhase, aPilTable, aTempPilTable));

	    UtilitaireDao.get(DbConstant.POOL_NAME).executeBlock(connection, blocInit);
	} catch (Exception ex) {
	    LoggerHelper.error(LOGGER, AbstractPhaseService.class, "register()", ex);
	}
    }

    /**
     * Select a batch of idSource to process. The idSource selected are the one of
     * the the previous phase with state == 1. Those idSource are updated in the
     * previous phase, their state are set to 0, and a new line in the current phase
     * is create for each idSource with state = 1
     * 
     * 
     * @param newPhase
     *            , the current phase
     * @param nbEnr
     *            , the max number of process reccord
     * @return a SQL request
     */
    private String sourceSelection(String newPhase, Integer nbEnr) {
	StringBuilder request = new StringBuilder();
	Date date = new Date();
	SimpleDateFormat formatter = new SimpleDateFormat(EDateFormat.DATE_FORMAT_WITH_SECOND.getValue());
	// requete.append("WITH prep as (SELECT a.*, sum(nb_enr) OVER (ORDER BY
	// date_traitement, nb_essais, id_source) as cum_enr ");
	request.append("WITH prep as");
	request.append(
		"\n\t (SELECT pil.*, count(1) OVER (ORDER BY date_traitement, nb_essais, id_source) as cum_enr ");
	request.append("\n\t FROM " + this.bddTable.getQualifedName(BddTable.ID_TABLE_PILOTAGE_FICHIER) + " pil ");
	request.append("\n\t WHERE '" + TraitementState.OK + ANY_ETAT_TRAITEMENT);
	request.append("\n\t AND etape=1");
//	request.append("\n\t AND EXISTS (");
//	request.append(
//		"\n\t\t SELECT 1 FROM " + this.bddTable.getQualifedName(BddTable.ID_TABLE_PHASE_ORDER) + " as phase");
//	request.append("\n\t\t WHERE ");
//	// condition pour savoir si la norme du fichier dispose de la phase que l'on
//	// veux lancer
//	request.append("\n\t\t (pil.id_norme = phase.id_norme");
//	request.append("\n\t\t AND pil.periodicite = phase.periodicite");
//	request.append("\n\t\t AND pil.validite::date > phase.validite_inf");
//	request.append("\n\t\t AND pil.validite::date < phase.validite_sup");
//	request.append("\n\t\t AND pil.phase_traitement = (SELECT phase_precedente from "
//		+ this.bddTable.getQualifedName(BddTable.ID_TABLE_PHASE_ORDER) + " as previous where nom_phase ='" + newPhase
//		+ "' AND previous.id_norme = pil.id_norme))");
//	// condition dans le cas que l'on veuille lancer une phase obligatoire
//	request.append("\n\t OR (pil.phase_traitement = (SELECT phase_precedente from "
//		+ this.bddTable.getQualifedName(BddTable.ID_TABLE_PHASE_ORDER) + " where nom_phase ='" + newPhase
//		+ "' AND IS_NEEDED)");
//	request.append("\n\t )) ");
	request.append("\n\t ) ");
	request.append("\n\t , mark AS (SELECT a.*    FROM prep a WHERE cum_enr<" + nbEnr + " ");
	request.append("\n\t UNION   (SELECT a.* FROM prep a LIMIT 1)) ");
	request.append("\n\t , update as ( UPDATE " + this.bddTable.getQualifedName(BddTable.ID_TABLE_PILOTAGE_FICHIER)
		+ " pil set etape=0 from mark b where pil.id_source=b.id_source and pil.etape=1) ");
	request.append("\n\t INSERT INTO " + this.bddTable.getQualifedName(BddTable.ID_TABLE_PILOTAGE_FICHIER) + " ");
	request.append(
		"(container, id_source, date_entree, id_norme, validite, periodicite, phase_traitement, etat_traitement, date_traitement, rapport, taux_ko, nb_enr, nb_essais, etape ");
	if (newPhase.equals(TypeTraitementPhase.STRUCTURIZE_XML.toString())) {
	    request.append(",jointure");
	}
	request.append(") ");
	request.append("\n\t select container, id_source, date_entree, id_norme, validite, periodicite, '" + newPhase
		+ "' as phase_traitement, '{" + TraitementState.ENCOURS + "}' as etat_traitement ");
	request.append(", '" + formatter.format(date) + "', rapport, taux_ko, nb_enr, nb_essais, 1 as etape");
	if (newPhase.equals(TypeTraitementPhase.STRUCTURIZE_XML.toString())) {
	    request.append(",jointure ");
	}
	request.append("\n\t from mark; ");

	return request.toString();
    }

    /**
     * Copy the pilotage table with only processing records of the phase
     * 
     *
     * @param tablePil
     * @param tablePilTemp
     * @return a SQL request
     */
    private String copieTablePilotage(String phase, String tablePil, String tablePilTemp) {
	StringBuilder request = new StringBuilder();
	request.append("\n DROP TABLE IF EXISTS " + tablePilTemp + "; ");
	request.append("\n CREATE ");
	if (!tablePilTemp.contains(".")) {
	    request.append("TEMPORARY ");
	} else {
	    request.append(" ");
	}
	request.append(
		"TABLE " + tablePilTemp + " with (autovacuum_enabled = false, toast.autovacuum_enabled = false) AS ( ");
	request.append("\n SELECT *  ");
	request.append("\n FROM " + tablePil + " ");
	request.append("\n WHERE etape=1 and phase_traitement='" + phase + "' AND etat_traitement='{"
		+ TraitementState.ENCOURS + "}'); ");
	request.append("\n analyze " + tablePilTemp + ";");
	return request.toString();
    }

    /**
     * 
     * Finalize the process of the phase.
     * <ul>
     * <li>Tag ine the global pilotage table the idSource processed</li>
     * <li>Delete temporary sql object</li>
     * </ul>
     * 
     */
    public void finalizePhase() {
	LoggerDispatcher.info("Finalize phase", LOGGER);

	if (this.todo) {

	    StringBuilder requete = new StringBuilder();
	    requete.append(FormatSQL.dropTable(this.getTablePilTemp()));
	    try {
		UtilitaireDao.get(DbConstant.POOL_NAME).executeBlock(this.connection, requete);
	    } catch (Exception ex) {
		LoggerHelper.error(LOGGER, AbstractPhaseService.class, "finalizePhase()", ex);
	    }

	    if (this.phaseName.equals(TypeTraitementPhase.LOAD.toString())) {
		AbstractPhaseService.pgCatalogMaintenance(this.connection, "freeze analyze");
		AbstractPhaseService.pilotageTMaintenance(this.connection, this.executionEnv, "freeze analyze");
	    }

	}

	try {
	    if (this.connection != null) {
		this.connection.close();
		this.connection = null;
	    }

	} catch (Exception ex) {
	    LoggerHelper.error(LOGGER, AbstractPhaseService.class, "finalizePhase()", ex);
	}

	if (this.context != null) {
	    this.context.close();
	}
    }

    /**
     * Excecute a vacumm on the pilotage table
     * 
     * @param aConnection
     * @param envExecution
     * @param type
     */
    public static void pilotageMaintenance(Connection aConnection, String aProcessEnv, String type) {
	String tablePil = dbEnv(aProcessEnv) + TraitementTableExecution.PILOTAGE_FICHIER;
	LoggerDispatcher.info("** Pilotage Maintenance**", LOGGER);

	try {
	    UtilitaireDao.get(DbConstant.POOL_NAME).executeImmediate(aConnection, "vacuum " + type + " " + tablePil + ";");
	} catch (Exception e) {
	 	LoggerDispatcher.error("Error in pilotageMaintenance()", e, LOGGER);
	}
    }

    /**
     * Excecute a vacumm on Pilotage table transposed (pilotage_t) 
     * @param connection
     * @param envExecution
     * @param type
     */
    public static void pilotageTMaintenance(Connection connection, String envExecution, String type) {

	String tablePilT = dbEnv(envExecution) + TraitementTableExecution.PILOTAGE_FICHIER + "_t";

	LoggerDispatcher.info("** Maintenance Pilotage T **", LOGGER);

	try {
	    UtilitaireDao.get(DbConstant.POOL_NAME).executeImmediate(connection, FormatSQL.vacuumSecured(tablePilT, type));
	} catch (Exception e) {
	    LoggerDispatcher.error("Error in pilotageTMaintenance()", e, LOGGER);
	}
    }

    /**
     * Excecute a vacumm on Meta model
     * 
     * @param type
     */
    public static void pgCatalogMaintenance(Connection connection, String type) {
	// postgres have trouble to empty table space when there is too much operation on column
	// need a vaccum full
	LoggerDispatcher.info("** Catalogue Maintenance **", LOGGER);
	UtilitaireDao.get(DbConstant.POOL_NAME).maintenancePgCatalog(connection, type);
    }


    /**
     *
     * @return processing time
     */
    @SQLExecutor
    public ServiceReporting invokeApi() {
	double start = System.currentTimeMillis();
	int nbRow = 0;
	LoggerDispatcher.info("****** " + this.phaseName + " execution *******", LOGGER);
	try {

	    // set schema
	    if (this.phaseName.equals(TypeTraitementPhase.INITIALIZE.toString())) {
		ApiInitialisationService apiInitialisationService = (ApiInitialisationService) ApiServiceFactory
			.getService(this.connection, TypeTraitementPhase.INITIALIZE.toString(), this.parameterEnv,
				this.executionEnv, this.directoryRoot, this.nbEnr.toString(), this.paramBatch);
		apiInitialisationService.bddScript();
	    }

	    this.todo = true;
	    LoggerDispatcher.info("Todo - " + this.phaseName + " : " + this.todo, LOGGER);

	    if (this.initialize()) {
		try {
		    this.process();
		} catch (Exception ex) {
		    LoggerDispatcher.error("Error in" + this.phaseName + ". ", ex, LOGGER);
		    try {
			this.errorRecovery(this.phaseName, this.getTablePil(), ex, "aucuneTableADroper");
		    } catch (Exception ex2) {
			LoggerDispatcher.error("Error on errorRecovery" + this.phaseName + ". ", ex, LOGGER);
		    }
		}
	    }
	} finally {
	    if (this.todo && !this.phaseName.equals(TypeTraitementPhase.INITIALIZE.toString())) {

		if (this.reporting > 0) {
		    nbRow = this.reporting;
		} else {
		    try {
			UtilitaireDao.get(DbConstant.POOL_NAME).executeRequest(this.connection,
				"CREATE TABLE IF NOT EXISTS " + this.getTablePilTemp() + " (nb_enr int); ");
		    } catch (SQLException e) {
			LoggerDispatcher.error("Error in initialization", LOGGER);
		    }
		    nbRow = UtilitaireDao.get(DbConstant.POOL_NAME).getInt(this.connection,
			    "select coalesce(sum(nb_enr),0) from " + this.getTablePilTemp());
		}
	    }
	    this.finalizePhase();
	}

	LoggerDispatcher.info("****** The end " + this.phaseName + " *******", LOGGER);

	return new ServiceReporting(nbRow, System.currentTimeMillis() - start);

    }
    



    public String getPhaseName() {
	return phaseName;
    }

    public void setPhaseName(String phaseName) {
	this.phaseName = phaseName;
    }

}
