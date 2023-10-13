package fr.insee.arc.web.gui.nomenclature.dao;

import static org.junit.Assert.assertEquals;

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
import fr.insee.arc.web.gui.all.util.Session;
import fr.insee.arc.web.gui.all.util.VObject;
import fr.insee.arc.web.gui.all.util.VObjectService;
import fr.insee.arc.web.gui.nomenclature.model.ViewListNomenclatures;
import fr.insee.arc.web.gui.nomenclature.model.ViewNomenclature;
import fr.insee.arc.web.gui.nomenclature.model.ViewSchemaNmcl;

public class GererNomenclatureDaoTest extends InitializeQueryTest {

	private static VObjectService vObjectService;
	private static DataObjectService dao;
	private static GererNomenclatureDao pdao;

	@BeforeClass
	public static void setup() throws ArcException {
		BddPatcherTest.createDatabase();
		BddPatcherTest.insertTestDataLight();
		vObjectService = new VObjectService();
		vObjectService.setConnection(c);
		vObjectService.setSession(new Session());
		dao = new DataObjectService();
		dao.setSandboxSchema(BddPatcherTest.testSandbox1);
		pdao = new GererNomenclatureDao();
		pdao.initialize(vObjectService, dao);
	}

	@Test
	public void initializeViewListNomenclatures() {
		
		VObject viewListNomenclatures = new ViewListNomenclatures();

		// execute query
		pdao.initializeViewListNomenclatures(viewListNomenclatures);

		// test the content of the view
		List<String> viewColumns = ColumnEnum
				.listColumnEnumByName(ViewEnum.IHM_NMCL.getColumns().keySet());
		viewColumns.removeAll(viewListNomenclatures.getHeadersDLabel());

		assertEquals(0, viewColumns.size());
	}

	@Test
	public void initializeViewNomenclature() {
		
		VObject viewNomenclature = new ViewNomenclature();

		// execute query
		pdao.initializeViewNomenclature(viewNomenclature, "nom_table", "nmcl_evenements_v001"); // tableSelected

		// test the content of the view
		assertEquals(2, viewNomenclature.getHeadersDLabel().size()); // key-value : 2 columns
	}
	
	@Test
	public void initializeViewSchemaNmcl() throws ArcException {
		
		VObject viewSchemaNmcl = new ViewSchemaNmcl();
		
		// select the first record of viewNorm and set it as the selected record
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.SELECT, "*", SQL.FROM, pdao.getDataObjectService().getView(ViewEnum.IHM_NMCL), SQL.WHERE, "nom_table='nmcl_evenements_v001'");
		Map<String, List<String>> viewNomenclatureSelectedRecords = new GenericBean(UtilitaireDao.get(0).executeRequest(c, query)).mapContent();
		pdao.setSelectedRecords(viewNomenclatureSelectedRecords);

		// execute query
		pdao.initializeViewSchemaNmcl(viewSchemaNmcl);

		// test the content of the view
		List<String> viewColumns = ColumnEnum
				.listColumnEnumByName(ViewEnum.IHM_SCHEMA_NMCL.getColumns().keySet());
		viewColumns.removeAll(viewSchemaNmcl.getHeadersDLabel());

		assertEquals(0, viewColumns.size());
		
		// in test data, must return a schema of two columns
		assertEquals(2, viewSchemaNmcl.getContent().t.size());
	}

}
