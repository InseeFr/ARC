package fr.insee.arc_essnet.core.dao;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.SQLException;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlProducer;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.InputSource;

import fr.insee.arc_essnet.core.model.BddTable;
import fr.insee.arc_essnet.core.model.TraitementPhaseContainer;
import fr.insee.arc_essnet.utils.dao.AbstractDAO.DAOException;
import fr.insee.arc_essnet.utils.dao.UtilitaireDao;
import fr.insee.arc_essnet.utils.queryhandler.BatchQueryHandler;
import fr.insee.arc_essnet.utils.queryhandler.UtilitaireDAOQueryHandler.AbstractQueryHandlerException;
import fr.insee.arc_essnet.utils.utils.FormatSQL;

public class ProcessPhaseDAOTest {

    private static final String ARC_TEST = "arc_test";
    private static final String ARC_BAS8 = "arc_bas8";
    private Connection connection ;
    private ProcessPhaseDAO processPhaseDAO;
    private BddTable bddTableTest;

    @Before
    public void createTestDb() {
	StringBuilder query = new StringBuilder();
	query.append(FormatSQL.createSchema(ARC_TEST, ""));
	BddTable bddTableTest = new BddTable(ARC_TEST);
	BddTable bddTableBAS = new BddTable(ARC_BAS8);

	query.append(FormatSQL.createAsSelectFrom(bddTableTest.getQualifedName(BddTable.ID_TABLE_PHASE_ORDER)//
		, bddTableBAS.getQualifedName(BddTable.ID_TABLE_PHASE_ORDER), "false", true));

	try {
	    UtilitaireDao.get("arc").executeImmediate(null, query.toString());
	} catch (SQLException e) {
	    System.err.println("error when create the tabe for ProcessPhaseDAOTest");
	    e.printStackTrace();
	}

    }

    /**
     * Init the database for testing
     * @throws Exception
     * @throws DatabaseUnitException
     * @throws DataSetException
     * @throws SQLException
     */
    private void initDatabase() throws Exception, DatabaseUnitException, DataSetException, SQLException {
	connection = UtilitaireDao.get("arc").getDriverConnexion();
	DatabaseConnection myDbConnection = new DatabaseConnection(connection,ARC_TEST );
	FlatXmlProducer producer = new FlatXmlProducer(
		new InputSource("src/test/resources/dbUnit/paramattrage_ordre_phase.xml"));
	IDataSet mySetUpDataset = new FlatXmlDataSet(producer);
	DatabaseOperation.CLEAN_INSERT.execute(myDbConnection, mySetUpDataset);
	
	bddTableTest = new BddTable(ARC_TEST);
	processPhaseDAO = new ProcessPhaseDAO(new BatchQueryHandler(connection), bddTableTest.getNaming(BddTable.ID_TABLE_PHASE_ORDER));
    }
    
    /**
     * Test the nominal case.
     * @throws Exception
     */
    @Test
    public void testGetAllPhaseOfNormeNominal() throws Exception {
	// GIVEN
	initDatabase();
	// WHEN
	TraitementPhaseContainer traitementPhaseContainer = processPhaseDAO.getAllPhaseOfNorme();
	
	
	// THEN
	assertEquals(7, traitementPhaseContainer.getTraitementPhaseEntities().size());
	
	processPhaseDAO.close();
	connection.close();
	
    }

    
    /**
     * Check the exception throw in case of missing column
     * @throws Exception
     */
    @Test(expected = DAOException.class)
    public void testGetAllPhaseOfNormeColumnError() throws Exception {
	initDatabase();
	StringBuilder query = new StringBuilder();

	query.append(FormatSQL.dropColonne(bddTableTest.getQualifedName(BddTable.ID_TABLE_PHASE_ORDER), ProcessPhaseDAO.NOM_PHASE));

	try {
	    UtilitaireDao.get("arc").executeImmediate(null, query.toString());
	} catch (SQLException e) {
	    System.err.println("error when create the tabe for ProcessPhaseDAOTest");
	    e.printStackTrace();
	}
	
	// GIVEN
	ProcessPhaseDAO processPhaseDAO = new ProcessPhaseDAO(new BatchQueryHandler(connection), bddTableTest.getNaming(BddTable.ID_TABLE_PHASE_ORDER));
	// WHEN

	TraitementPhaseContainer traitementPhaseContainer =processPhaseDAO.getAllPhaseOfNorme();
	
	// THEN

	
	processPhaseDAO.close();
	connection.close();
	
    }
    
    /**
     * Check the exception throw in case of missing table
     * @throws Exception
     */
    @Test(expected = AbstractQueryHandlerException.class)
    public void testGetAllPhaseOfNormeTableDoesNotExist() throws Exception {
	
	StringBuilder query = new StringBuilder();
	BddTable bddTableTest = new BddTable(ARC_TEST);

	query.append(FormatSQL.dropTable(bddTableTest.getQualifedName(BddTable.ID_TABLE_PHASE_ORDER)));

	try {
	    UtilitaireDao.get("arc").executeImmediate(null, query.toString());
	} catch (SQLException e) {
	    System.err.println("error when create the tabe for ProcessPhaseDAOTest");
	    e.printStackTrace();
	}
	
	// GIVEN
	Connection connection = UtilitaireDao.get("arc").getDriverConnexion();
	ProcessPhaseDAO processPhaseDAO = new ProcessPhaseDAO(new BatchQueryHandler(connection), bddTableTest.getNaming(BddTable.ID_TABLE_PHASE_ORDER));
	// WHEN

	processPhaseDAO.getAllPhaseOfNorme();
	
	// THEN

	
	processPhaseDAO.close();
	connection.close();
	
    }


}
