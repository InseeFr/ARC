package fr.insee.arc.web.gui.norme.dao;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

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
import fr.insee.arc.web.gui.norme.model.ViewCalendrier;
import fr.insee.arc.web.gui.norme.model.ViewChargement;
import fr.insee.arc.web.gui.norme.model.ViewJeuxDeRegles;
import fr.insee.arc.web.gui.norme.model.ViewMapping;
import fr.insee.arc.web.gui.norme.model.ViewNorme;

public class GererNormeDaoTest extends InitializeQueryTest {

	private static VObjectService vObjectService;
	private static DataObjectService dao;
	private static GererNormeDao pdao;

	@BeforeClass
	public static void setup() throws ArcException, SQLException {
		
		buildPropertiesWithoutScalability(null);
		
		BddPatcherTest.createDatabase();
		BddPatcherTest.insertTestDataSiera();
		vObjectService = new VObjectService();
		vObjectService.setConnection(c);
		vObjectService.setSession(new Session());
		dao = new DataObjectService();
		dao.setSandboxSchema(BddPatcherTest.testSandbox1);
		pdao = new GererNormeDao();
		pdao.initialize(vObjectService, dao);

	}

	@Test
	public void initializeViewNorme() {

		VObject viewNorme = new ViewNorme();
		
		// execute query
		pdao.initializeViewNorme(viewNorme);

		// test the number of columns of the view
		List<String> viewColumns = ColumnEnum
				.listColumnEnumByName(ViewEnum.IHM_NORME.getColumns().keySet());
		viewColumns.removeAll(viewNorme.getHeadersDLabel());

		assertEquals(0, viewColumns.size());
		
		// in test data, must return one norm
		assertEquals(1, viewNorme.getContent().t.size());

	}

	@Test
	public void initializeViewCalendar() throws ArcException {

		VObject viewCalendar = new ViewCalendrier();
		
		// select the first record of viewNorm and set it as the selected record
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.SELECT, "*", SQL.FROM, pdao.getDataObjectService().getView(ViewEnum.IHM_NORME), SQL.WHERE, "id_norme='PHASE3V1'");
		Map<String, List<String>> viewNormSelectedRecords = new GenericBean(UtilitaireDao.get(0).executeRequest(c, query)).mapContent();
		pdao.setSelectedRecords(viewNormSelectedRecords);
		
		// execute query
		pdao.initializeViewCalendar(viewCalendar);

		// test the number of columns of the view
		List<String> viewColumns = ColumnEnum
				.listColumnEnumByName(ViewEnum.IHM_CALENDRIER.getColumns().keySet());
		viewColumns.removeAll(viewCalendar.getHeadersDLabel());

		assertEquals(0, viewColumns.size());
		
		// in test data, must return one calendar
		assertEquals(1, viewCalendar.getContent().t.size());

	}
	
	
	@Test
	public void initializeViewRulesSet() throws ArcException {

		VObject viewRulesSet = new ViewJeuxDeRegles();
		
		// select the first record of viewCalendar and set it as the selected record
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.SELECT, "*", SQL.FROM, pdao.getDataObjectService().getView(ViewEnum.IHM_CALENDRIER), SQL.WHERE, "id_norme='PHASE3V1'");
		Map<String, List<String>> viewCalendarSelectedRecords = new GenericBean(UtilitaireDao.get(0).executeRequest(c, query)).mapContent();
		pdao.setSelectedRecords(viewCalendarSelectedRecords);
		
		// execute query
		pdao.initializeViewRulesSet(viewRulesSet);

		// test the number of columns of the view
		List<String> viewColumns = ColumnEnum
				.listColumnEnumByName(ViewEnum.IHM_JEUDEREGLE.getColumns().keySet());
		viewColumns.removeAll(viewRulesSet.getHeadersDLabel());

		assertEquals(0, viewColumns.size());
		
		// in test data, must return 1 rule set
		assertEquals(1, viewRulesSet.getContent().t.size());

	}

	@Test
	public void initializeViewChargement() throws ArcException {

		VObject viewChargement = new ViewChargement();
		
		// select the first record of viewRulesSet and set it as the selected record
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.SELECT, "*", SQL.FROM, pdao.getDataObjectService().getView(ViewEnum.IHM_JEUDEREGLE));
		query.build(SQL.WHERE, "id_norme='PHASE3V1'", SQL.AND, "version='v002'");
		Map<String, List<String>> viewRulesSetSelectedRecords = new GenericBean(UtilitaireDao.get(0).executeRequest(c, query)).mapContent();
		pdao.setSelectedRecords(viewRulesSetSelectedRecords);
		
		// execute query
		pdao.initializeViewChargement(viewChargement);

		// test the number of columns of the view
		List<String> viewColumns = ColumnEnum
				.listColumnEnumByName(ViewEnum.IHM_CHARGEMENT_REGLE.getColumns().keySet());
		viewColumns.removeAll(viewChargement.getHeadersDLabel());

		assertEquals(0, viewColumns.size());
		
		// in test data, must return one loading rule set
		assertEquals(1, viewChargement.getContent().t.size());

	}
	
	@Test
	public void uploadFileMapping() throws ArcException, IOException {
		
		String path = "src/test/resources/fr/insee/testfiles/ihm_mapping_regle_siera_test.csv";
		File file = new File(path);
	    byte[] fileBytes = Files.readAllBytes(file.toPath());
	    MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "text/plain", fileBytes);

		VObject viewMapping = new ViewMapping();
		VObject viewRulesSet = new ViewJeuxDeRegles();
		VObject viewNorme = new ViewNorme();
		
		
		pdao.initializeViewRulesSet(viewRulesSet);
		pdao.initializeViewNorme(viewNorme);	
		viewMapping.setTable(pdao.getDataObjectService().getView(ViewEnum.IHM_MAPPING_REGLE));
		
		// select the first line of norme
		viewNorme.setSelectedLines(Arrays.asList(true));
		
		// select the first line of ruleset
		viewRulesSet.setSelectedLines(Arrays.asList(true));
		
		// execute query
		pdao.uploadFileMapping(viewMapping, viewRulesSet, viewNorme, multipartFile);
		
		// test the content of ihm_mapping_regle
		ArcPreparedStatementBuilder queryAssert = new ArcPreparedStatementBuilder();
		queryAssert.build(SQL.SELECT, ColumnEnum.VARIABLE_SORTIE, ", ", ColumnEnum.EXPR_REGLE_COL, SQL.FROM, pdao.getDataObjectService().getView(ViewEnum.IHM_MAPPING_REGLE));
		
		
		Map<String, List<String>> viewMappingUpload = new GenericBean(UtilitaireDao.get(0).executeRequest(c, queryAssert)).mapContent();
		assertEquals(131, viewMappingUpload.get(ColumnEnum.VARIABLE_SORTIE.toString())
				.size()); // number of variables

		
		assertEquals(3, viewMappingUpload.get(ColumnEnum.EXPR_REGLE_COL.toString())
				.stream()
				.filter(Objects::isNull)
			    .collect(Collectors.toList())
			    .size()); // number of metadata absent from the file
	}

}
