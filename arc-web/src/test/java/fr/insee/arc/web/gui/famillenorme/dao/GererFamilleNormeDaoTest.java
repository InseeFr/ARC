package fr.insee.arc.web.gui.famillenorme.dao;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.DataObjectService;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.service.engine.initialisation.BddPatcherTest;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.query.InitializeQueryTest;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.web.gui.famillenorme.model.ViewClient;
import fr.insee.arc.web.gui.famillenorme.model.ViewFamilleNorme;
import fr.insee.arc.web.gui.famillenorme.model.ViewHostAllowed;
import fr.insee.arc.web.gui.famillenorme.model.ViewTableMetier;
import fr.insee.arc.web.gui.famillenorme.model.ViewVariableMetier;
import fr.insee.arc.web.util.Session;
import fr.insee.arc.web.util.VObject;
import fr.insee.arc.web.util.VObjectService;

public class GererFamilleNormeDaoTest extends InitializeQueryTest {

	private static VObjectService vObjectService;
	private static DataObjectService dao;
	private static GererFamilleNormeDao pdao;

	@BeforeClass
	public static void setup() throws ArcException {
		BddPatcherTest.createDatabase();
		BddPatcherTest.insertTestDataFamilleNorme();
		vObjectService = new VObjectService();
		vObjectService.setConnection(c);
		vObjectService.setSession(new Session());
		dao = new DataObjectService(BddPatcherTest.testSandbox);
		pdao = new GererFamilleNormeDao(vObjectService, dao);
	}

	@Test
	public void initializeViewFamilleNorme() {
		
		VObject viewFamilleNorme = new ViewFamilleNorme();

		// execute query
		pdao.initializeViewFamilleNorme(viewFamilleNorme);

		// test the content of the view
		List<String> viewColumns = ColumnEnum
				.listColumnEnumByName(ViewEnum.IHM_FAMILLE.getColumns().keySet());
		viewColumns.removeAll(viewFamilleNorme.getHeadersDLabel());

		assertEquals(0, viewColumns.size());
	}
	
	@Test
	public void initializeViewClient() throws ArcException {
		
		VObject viewClient = new ViewClient();
		
		// select the first record of viewNormFamily and set it as the selected record
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.SELECT, "*", SQL.FROM, pdao.getDataObjectService().getView(ViewEnum.IHM_FAMILLE), SQL.WHERE, "id_famille='DSN'");
		Map<String, ArrayList<String>> viewNormFamilySelectedRecords = new GenericBean(UtilitaireDao.get(0).executeRequest(c, query)).mapContent();
		pdao.setSelectedRecords(viewNormFamilySelectedRecords);

		// execute query
		pdao.initializeViewClient(viewClient);

		// test the content of the view
		List<String> viewColumns = ColumnEnum
				.listColumnEnumByName(ViewEnum.IHM_CLIENT.getColumns().keySet());
		viewColumns.removeAll(viewClient.getHeadersDLabel());

		assertEquals(0, viewColumns.size());
	}
	
	@Test
	public void initializeViewHostAllowed() throws ArcException {
		
		VObject viewHostAllowed = new ViewHostAllowed();
		
		// select the first record of viewClient and set it as the selected record
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.SELECT, "*", SQL.FROM, pdao.getDataObjectService().getView(ViewEnum.IHM_CLIENT), SQL.WHERE, "id_application='ARTEMIS'");
		Map<String, ArrayList<String>> viewClientSelectedRecords = new GenericBean(UtilitaireDao.get(0).executeRequest(c, query)).mapContent();
		pdao.setSelectedRecords(viewClientSelectedRecords);
		
		
		// execute query
		pdao.initializeViewHostAllowed(viewHostAllowed);
		
		// test the content of the view
		List<String> viewColumns = ColumnEnum
				.listColumnEnumByName(ViewEnum.IHM_WEBSERVICE_WHITELIST.getColumns().keySet());
		viewColumns.removeAll(viewHostAllowed.getHeadersDLabel());
		
		assertEquals(0, viewColumns.size());
		
		// in test data, must return one host allowed
		assertEquals(1, viewHostAllowed.getContent().t.size());
	}
	
	@Test
	public void initializeViewTableMetier() throws ArcException {
		
		VObject viewTableMetier = new ViewTableMetier();
		
		// select the first record of viewFamilleNorme and set it as the selected record
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.SELECT, "*", SQL.FROM, pdao.getDataObjectService().getView(ViewEnum.IHM_FAMILLE), SQL.WHERE, "id_famille='DSN'");
		Map<String, ArrayList<String>> viewNormFamilySelectedRecords = new GenericBean(UtilitaireDao.get(0).executeRequest(c, query)).mapContent();
		pdao.setSelectedRecords(viewNormFamilySelectedRecords);
		

		// execute query
		pdao.initializeViewTableMetier(viewTableMetier);

		// test the content of the view
		List<String> viewColumns = ColumnEnum
				.listColumnEnumByName(ViewEnum.IHM_MOD_TABLE_METIER.getColumns().keySet());
		viewColumns.removeAll(viewTableMetier.getHeadersDLabel());

		assertEquals(0, viewColumns.size());
		
		// in test data, must return 6 business tables
		assertEquals(6, viewTableMetier.getContent().t.size());
	}
	
	@Test
	public void initializeViewVariableMetier() throws ArcException {
		
		VObject viewVariableMetier = new ViewVariableMetier();
		
		// to later check count of all variables
		viewVariableMetier.setPaginationSize(1000);
		
		// select the first record of viewFamilleNorme and set it as the selected record
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.SELECT, "*", SQL.FROM, pdao.getDataObjectService().getView(ViewEnum.IHM_FAMILLE), SQL.WHERE, "id_famille='DSN'");
		Map<String, ArrayList<String>> viewNormFamilySelectedRecords = new GenericBean(UtilitaireDao.get(0).executeRequest(c, query)).mapContent();
		pdao.setSelectedRecords(viewNormFamilySelectedRecords);
		
		// select tables of dsn
		ArcPreparedStatementBuilder queryTables = new ArcPreparedStatementBuilder();
		queryTables.build(SQL.SELECT, "*", SQL.FROM, pdao.getDataObjectService().getView(ViewEnum.IHM_MOD_TABLE_METIER), SQL.WHERE, "id_famille='DSN'");
		List<String> listeTableFamille = UtilitaireDao.get(0).getList(c, query.toString(), new ArrayList<String>());
		

		// execute query
		pdao.initializeViewVariableMetier(viewVariableMetier, listeTableFamille);

		// test the content of the view
		List<String> viewColumns = ColumnEnum
				.listColumnEnumByName(ViewEnum.IHM_MOD_VARIABLE_METIER.getColumns().keySet());
		viewColumns.removeAll(viewVariableMetier.getHeadersDLabel());

		// nom_table_metier is left unselected
		assertEquals(1, viewColumns.size());
		
		// in test data, must return 131 unique business variables
		assertEquals(131, viewVariableMetier.getContent().t.size());
	}	

}
