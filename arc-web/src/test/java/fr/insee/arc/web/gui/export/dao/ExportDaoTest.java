package fr.insee.arc.web.gui.export.dao;

import static org.junit.Assert.assertEquals;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.DataObjectService;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.service.engine.initialisation.BddPatcherTest;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.query.InitializeQueryTest;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.web.gui.all.util.Session;
import fr.insee.arc.web.gui.all.util.VObject;
import fr.insee.arc.web.gui.all.util.VObjectService;
import fr.insee.arc.web.gui.export.model.ViewExport;

public class ExportDaoTest extends InitializeQueryTest {

	private static VObjectService vObjectService;
	private static DataObjectService dao;
	private static ExportDao pdao;

	@BeforeClass
	public static void setup() throws ArcException {
		BddPatcherTest.createDatabase();
		BddPatcherTest.insertTestDataExport();
		vObjectService = new VObjectService();
		vObjectService.setConnection(c);
		vObjectService.setSession(new Session());
		dao = new DataObjectService();
		dao.setSandboxSchema(BddPatcherTest.testSandbox1);
		pdao = new ExportDao();
		pdao.initialize(vObjectService, dao);
	}

	@Test
	public void initializeViewExport() {
		
		VObject viewExport = new ViewExport();

		// execute query
		pdao.initializeViewExport(viewExport);

		// test the content of the view
		List<String> viewColumns = ColumnEnum
				.listColumnEnumByName(ViewEnum.EXPORT.getColumns().keySet());
		viewColumns.removeAll(viewExport.getHeadersDLabel());

		assertEquals(0, viewColumns.size());
		
		// in test data, must return two exports
		assertEquals(2, viewExport.getContent().t.size());
	}

	@Test
	public void startExportRetrieve() throws ArcException {
		
		// select the first record of viewNorm and set it as the selected record
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.SELECT, "*", SQL.FROM, pdao.getDataObjectService().getView(ViewEnum.EXPORT), SQL.WHERE, "file_name='test1'");
		Map<String, List<String>> viewExportSelectedRecords = new GenericBean(UtilitaireDao.get(0).executeRequest(c, query)).mapContent();
		pdao.setSelectedRecords(viewExportSelectedRecords);

		// execute query
		Map<String, List<String>> retrievedData = pdao.startExportRetrieve();

		// test the content of the view
		List<String> viewColumns = ColumnEnum
				.listColumnEnumByName(ViewEnum.EXPORT.getColumns().keySet());
		viewColumns.removeAll(retrievedData.keySet());

		assertEquals(0, viewColumns.size());
	}

	@Test
	public void startExportUpdateStateEncours() throws ArcException {
		
		// add filename updated
		List<String> fileName = new ArrayList<String>();
		fileName.add("test1");

		// execute query
		pdao.startExportUpdateState(fileName, 0, false);

		// test the content of the view
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.SELECT, ColumnEnum.ETAT, SQL.FROM, pdao.getDataObjectService().getView(ViewEnum.EXPORT), SQL.WHERE, "file_name='test1'");
		Map<String, List<String>> viewExportUpdatedRecords = new GenericBean(UtilitaireDao.get(0).executeRequest(c, query)).mapContent();
		assertEquals(TraitementEtat.ENCOURS.toString(), viewExportUpdatedRecords.get(ColumnEnum.ETAT.toString()).get(0));
		
	}

	@Test
	public void exportFileRetrieve() throws ArcException {
		
		// add howToExport and filesToExport
		List<String> howToExport = new ArrayList<String>();
		howToExport.add(null);
		List<String> tablesToExport = new ArrayList<String>();
		tablesToExport.add("export");

		// execute query
		Map<String, List<String>> retrievedData = pdao.exportFileRetrieveRules(0, howToExport, tablesToExport, BddPatcherTest.testSandbox1);

		// test the content of the view
		assertEquals(3, retrievedData.keySet().size()); // 3 columns selected
		assertEquals(11, retrievedData.get("varbdd").size()); // 11 columns in export table
	}
	
	@Test
	public void exportFileFilteredOrdered() throws ArcException, SQLException {
		
		// create statement
		c.setAutoCommit(false);

		Statement stmt = c.createStatement();
		stmt.setFetchSize(5000);
		
		// add tablesToExport, filterTable, orderTable
		List<String> tablesToExport = new ArrayList<String>();
		tablesToExport.add("export");
		List<String> filterTable = new ArrayList<String>();
		filterTable.add(null);
		List<String> orderTable = new ArrayList<String>();
		orderTable.add(null);

		// execute query
		ResultSet res = pdao.exportFileFilteredOrdered(stmt, 0, tablesToExport, filterTable, orderTable, BddPatcherTest.testSandbox1);

		// test the content of the view
		ResultSetMetaData rsmd = res.getMetaData();
		List<String> viewColumns = ColumnEnum
				.listColumnEnumByName(ViewEnum.EXPORT.getColumns().keySet());
		for (int i = 1; i <= rsmd.getColumnCount(); i++) {
			viewColumns.remove(rsmd.getColumnLabel(i));
		}
		assertEquals(0, viewColumns.size());
		
	}


}
