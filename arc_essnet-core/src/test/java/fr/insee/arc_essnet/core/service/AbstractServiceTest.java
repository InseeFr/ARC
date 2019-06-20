package fr.insee.arc_essnet.core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.assertj.core.util.Arrays;
import org.dbunit.Assertion;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.filter.DefaultColumnFilter;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.dataset.xml.FlatXmlProducer;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Test;
import org.mockito.Mockito;
import org.xml.sax.InputSource;

import fr.insee.arc_essnet.core.dbUnitExt.CustomPostgresqlDataTypeFactory;
import fr.insee.arc_essnet.core.model.BddTable;
import fr.insee.arc_essnet.core.model.Norme;
import fr.insee.arc_essnet.core.model.TraitementState;
import fr.insee.arc_essnet.core.model.TypeTraitementPhase;
import fr.insee.arc_essnet.utils.dao.UtilitaireDao;
import fr.insee.arc_essnet.utils.utils.FormatSQL;

public class AbstractServiceTest {
    private static final String XML_FILE_PILOTAGE_TABLE = "src/test/resources/dbUnit/pilotage_fichier.xml";
    private static final String XML_FILE_NORME_TABLE = "src/test/resources/dbUnit/norme.xml";
    private static final String ARC_TEST = "arc_test";
    private static final String ARC_BAS8 = "arc_bas8";
    private Connection connection;
    private BddTable bddTableTest = new BddTable(ARC_TEST);;
    IDatabaseConnection iDatabaseConnection;

    /**
     * Create the pilotage table in the test schema
     */
    public void createTestTable(String idTableToCopy) {
	StringBuilder query = new StringBuilder();
	query.append(FormatSQL.createSchema(ARC_TEST, ""));
	bddTableTest = new BddTable(ARC_TEST);
	BddTable bddTableBAS = new BddTable(ARC_BAS8);

	query.append(FormatSQL.createAsSelectFrom(bddTableTest.getQualifedName(idTableToCopy)//
		, bddTableBAS.getQualifedName(idTableToCopy), "false", true));

	try {
	    UtilitaireDao.get("arc").executeImmediate(null, query.toString());
	} catch (SQLException e) {
	    System.err.println("error when create the tabe for AbstractServiceTest");
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
    private void fillTableDBunit(String xmlfileToFillTable)
	    throws Exception, DatabaseUnitException, DataSetException, SQLException {
	connection = UtilitaireDao.get("arc").getDriverConnexion();
	iDatabaseConnection = new DatabaseConnection(connection, ARC_TEST);

	iDatabaseConnection.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY,
		new CustomPostgresqlDataTypeFactory());

	FlatXmlProducer producer = new FlatXmlProducer(new InputSource(xmlfileToFillTable));
	producer.setColumnSensing(true);
	IDataSet mySetUpDataset = new FlatXmlDataSet(producer);
	DatabaseOperation.CLEAN_INSERT.execute(iDatabaseConnection, mySetUpDataset);

    }

    @Test
    public void testErrorRecovery() throws Exception {
	// GIVEN
	createTestTable(BddTable.ID_TABLE_PILOTAGE_FICHIER);
	fillTableDBunit(XML_FILE_PILOTAGE_TABLE);

	AbstractService abstractService = Mockito.mock(AbstractService.class, Mockito.CALLS_REAL_METHODS);
	abstractService.setConnection(connection);

	String actualPilTable = bddTableTest.getQualifedName(BddTable.ID_TABLE_PILOTAGE_FICHIER);
	Exception actualException = new Exception("test exception");
	String actualSometablesToDrop = "pilotage_temp";
	// WHEN
	abstractService.errorRecovery("IDENTIFY", actualPilTable, actualException, actualSometablesToDrop);

	// THEN
	// Fetch database data after executing the error recovery
	IDataSet databaseDataSet = iDatabaseConnection.createDataSet();
	ITable actualTable = databaseDataSet.getTable("pilotage_fichier");

	// Load expected data from an XML dataset
	IDataSet expectedDataSet = new FlatXmlDataSetBuilder()
		.build(new File("src/test/resources/dbUnitExpected/pilotage_fichier_repriseError.xml"));
	ITable expectedTable = expectedDataSet.getTable("pilotage_fichier");

	ITable filteredTable = DefaultColumnFilter.includedColumnsTable(actualTable,
		expectedTable.getTableMetaData().getColumns());

	// Check if the line is set to KO
	Assertion.assertEquals(expectedTable, filteredTable);

    }

    @Test
    public void testCreationTableResultatTemporaryNotEmpty() throws Exception {
	// GIVEN
	// WHEN
	String actualRequest = AbstractService.creationTableResultat("test_in", "test_out", true);
	// THEN
	String expectedReqest = "\n CREATE TEMPORARY TABLE test_out  WITH (autovacuum_enabled = false, toast.autovacuum_enabled = false)  as SELECT * FROM test_in ; ";
	assertEquals("the request are differents", expectedReqest, actualRequest);
    }

    @Test
    public void testCreationTableResultatEmpty() throws Exception {
	// GIVEN
	// WHEN
	String actualRequest = AbstractService.creationTableResultat("test_in", "arc.test_out", false);
	// THEN
	String expectedReqest = "\n CREATE  TABLE arc.test_out  WITH (autovacuum_enabled = false, toast.autovacuum_enabled = false)  as SELECT * FROM test_in where 1=0 ; ";
	assertEquals("the request are differents", expectedReqest, actualRequest);
    }

    @Test
    public void testPilotageMarkIdsourceNoRapportNoJointure() throws Exception {
	// GIVEN
	// WHEN
	String actualRequest = AbstractService.pilotageMarkIdsource("test", "file1",
		TypeTraitementPhase.IDENTIFY.toString(), TraitementState.OK.toString(), null).toString();
	// THEN
	String expectedReqest = "UPDATE test SET phase_traitement= 'IDENTIFY' , etat_traitement= '{OK}' , rapport= null WHERE id_source='file1';\n";
	assertEquals("the request are differents", expectedReqest, actualRequest);

    }

    @Test
    public void testPilotageMarkIdsource() throws Exception {
	// GIVEN
	// WHEN
	String actualRequest = AbstractService.pilotageMarkIdsource("test", "file1",
		TypeTraitementPhase.IDENTIFY.toString(), TraitementState.OK.toString(), "rapport test", "jointure")
		.toString();
	// THEN
	String expectedReqest = "UPDATE test SET phase_traitement= 'IDENTIFY' , etat_traitement= '{OK}' , rapport= 'rapport test' , jointure= 'jointure'WHERE id_source='file1';\n";
	assertEquals("the request are differents", expectedReqest, actualRequest);

    }

    @Test
    public void testGetAllNorms() throws Exception {
	// GIVEN
	createTestTable(BddTable.ID_TABLE_NORME_SPECIFIC);
	fillTableDBunit(XML_FILE_NORME_TABLE);
	AbstractService abstractService = Mockito.mock(AbstractService.class, Mockito.CALLS_REAL_METHODS);
	abstractService.setConnection(connection);
	abstractService.setBddTable(bddTableTest);

	// WHEN
	List<Norme> actualListNorme = abstractService.getAllNorms();
	// THEN
	Norme expectedNomeGF = new Norme("G_F", "A", "select 1 from alias_table where  id_source LIKE '%GF%'",
		"SELECT '2015-01-01'", "1", "1", "DSN", null);
	Norme expectedNomePHAS3v1_v = new Norme("PHAS3v1_v2", "M", "{V_S10_G00_00_006} IN ('P03V01')",
		"{v_s20_g00_05_005}", "2", "1", "DSN", null);
	Norme expectedNomePHASE2V1 = new Norme("PHASE2V1", "M", "{V_S10_G00_00_006} IN ('P02V00','P02V01')",
		"{v_s20_g00_05_005}", "3", "1", "DSN", null);
	Norme expectedNomeN4DS = new Norme("n4ds", "A", "sselect 1 from alias_table where id_source like '%.n4ds%' ",
		"SELECT max(to_char(to_date(replace(split_part(ligne, ',',2),'''',''),'ddmmyyyy'),'yyyy-mm-dd')::text) FROM alias_table WHERE ligne LIKE '%S20_G01_00_003_001%' ",
		"4", "1", "DSN", null);

	assertThat(actualListNorme).hasSize(4).contains(expectedNomeGF,expectedNomePHAS3v1_v,expectedNomePHASE2V1,expectedNomeN4DS);

    }


}
