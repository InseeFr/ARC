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
		BddPatcherTest.insertTestDataLight();
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
		query.build(SQL.SELECT, "*", SQL.FROM, pdao.getDataObjectService().getView(ViewEnum.IHM_FAMILLE), SQL.WHERE, "id_famille='SIRENE4'");
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

}
