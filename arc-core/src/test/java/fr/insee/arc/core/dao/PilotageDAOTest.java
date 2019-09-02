package fr.insee.arc.core.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlProducer;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;
import org.xml.sax.InputSource;

import fr.insee.arc.core.dbUnitExt.CustomPostgresqlDataTypeFactory;
import fr.insee.arc.core.model.BddTable;
import fr.insee.arc.core.model.PilotageEntity;
import fr.insee.arc.core.model.TraitementState;
import fr.insee.arc.utils.dao.AbstractDAO.DAOException;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.queryhandler.BatchQueryHandler;
import fr.insee.arc.utils.queryhandler.UtilitaireDAOQueryHandler.AbstractQueryHandlerException;
import fr.insee.arc.utils.utils.FormatSQL;

@Ignore("Need a proper management of the test DB")
public class PilotageDAOTest {
    private static final String ARC_TEST = "arc_test";
    private static final String ARC_BAS8 = "arc_bas8";
    private Connection connection;
    private PilotageDAO pilotageDAO;
    private BddTable bddTableTest;

    @Before
    public void createTestDb() {
	StringBuilder query = new StringBuilder();
	query.append(FormatSQL.createSchema(ARC_TEST, ""));
	bddTableTest = new BddTable(ARC_TEST);
	BddTable bddTableBAS = new BddTable(ARC_BAS8);

	query.append(FormatSQL.createAsSelectFrom(bddTableTest.getQualifedName(BddTable.ID_TABLE_PILOTAGE_FICHIER)//
		, bddTableBAS.getQualifedName(BddTable.ID_TABLE_PILOTAGE_FICHIER), "false", true));

	try {
	    UtilitaireDao.get("arc").executeImmediate(null, query.toString());
	} catch (SQLException e) {
	    System.err.println("error when create the tabe for ProcessPhaseDAOTest");
	    e.printStackTrace();
	}

    }

    /**
     * Init the database for testing
     * 
     * @throws Exception
     * @throws DatabaseUnitException
     * @throws DataSetException
     * @throws SQLException
     */
    private void initDatabase() throws Exception, DatabaseUnitException, DataSetException, SQLException {
	connection = UtilitaireDao.get("arc").getDriverConnexion();
	IDatabaseConnection iDatabaseConnection = new DatabaseConnection(connection, ARC_TEST);

	iDatabaseConnection.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY,
		new CustomPostgresqlDataTypeFactory());

	FlatXmlProducer producer = new FlatXmlProducer(
		new InputSource("src/test/resources/dbUnit/pilotage_fichier.xml"));
	producer.setColumnSensing(true);
	IDataSet mySetUpDataset = new FlatXmlDataSet(producer);
	DatabaseOperation.CLEAN_INSERT.execute(iDatabaseConnection, mySetUpDataset);

	pilotageDAO = new PilotageDAO(new BatchQueryHandler(connection),
		bddTableTest.getContextName(BddTable.ID_TABLE_PILOTAGE_FICHIER));
    }

    /**
     * Test the nominal case.
     * 
     * @throws Exception
     */
    @Test
    public void testGetFilesToProcess() throws Exception {
	// GIVEN
	initDatabase();
	// WHEN
	List<PilotageEntity> actualListToProcess = pilotageDAO.getFilesToProcess("IDENTIFY",
		TraitementState.KO.toString());

	// THEN
	Assert.assertEquals("More than 1 element x_x", 1, actualListToProcess.size());
	Assert.assertEquals("Not the good file x_x", "file2", actualListToProcess.get(0).getIdSource());
	Assert.assertEquals("Not the good norm x_x", "PHASE2v1_v2", actualListToProcess.get(0).getIdNorme());

	pilotageDAO.close();
	connection.close();

    }

    /**
     * Check the exception throw in case of missing column
     * 
     * @throws Exception
     */
    @Test(expected = DAOException.class)
    public void testGetFilesToProcessColumnError() throws Exception {
	initDatabase();
	StringBuilder query = new StringBuilder();

	query.append(FormatSQL.dropColonne(bddTableTest.getQualifedName(BddTable.ID_TABLE_PILOTAGE_FICHIER),
		PilotageDAO.ID_SOURCE));

	try {
	    UtilitaireDao.get("arc").executeImmediate(null, query.toString());
	} catch (SQLException e) {
	    System.err.println("error when create the tabe for ProcessPhaseDAOTest");
	    e.printStackTrace();
	}

	// GIVEN
	pilotageDAO = new PilotageDAO(new BatchQueryHandler(connection),
		bddTableTest.getContextName(BddTable.ID_TABLE_PILOTAGE_FICHIER));
	// WHEN

	List<PilotageEntity> actualListToProcess = pilotageDAO.getFilesToProcess("IDENTIFY",
		TraitementState.KO.toString());


	pilotageDAO.close();
	connection.close();

    }

    /**
     * Check the exception throw in case of missing table
     * 
     * @throws Exception
     */
    @Test(expected = AbstractQueryHandlerException.class)
    public void testGetFilesToProcessTableDoesNotExist() throws Exception {
	initDatabase();

	StringBuilder query = new StringBuilder();

	query.append(FormatSQL.dropTable(bddTableTest.getQualifedName(BddTable.ID_TABLE_PILOTAGE_FICHIER)));

	try {
	    UtilitaireDao.get("arc").executeImmediate(null, query.toString());
	} catch (SQLException e) {
	    System.err.println("error when create the tabe for PilotageDAO");
	    e.printStackTrace();
	}

	// GIVEN
	pilotageDAO = new PilotageDAO(new BatchQueryHandler(connection),
		bddTableTest.getContextName(BddTable.ID_TABLE_PILOTAGE_FICHIER));
	// WHEN
	pilotageDAO.getFilesToProcess("IDENTIFY", TraitementState.ENCOURS.toString());

	pilotageDAO.close();
	connection.close();

    }
}
