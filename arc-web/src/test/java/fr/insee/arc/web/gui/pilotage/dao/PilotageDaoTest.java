package fr.insee.arc.web.gui.pilotage.dao;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.DataObjectService;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.service.engine.initialisation.BddPatcherTest;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.query.InitializeQueryTest;
import fr.insee.arc.web.gui.all.util.Session;
import fr.insee.arc.web.gui.all.util.VObject;
import fr.insee.arc.web.gui.all.util.VObjectService;
import fr.insee.arc.web.gui.pilotage.model.ViewPilotageBAS;

public class PilotageDaoTest extends InitializeQueryTest {

	private static VObjectService vObjectService;
	private static DataObjectService dao;
	private static PilotageDao pdao;

	@BeforeClass
	public static void setup() throws ArcException {
		BddPatcherTest.createDatabase();
		vObjectService = new VObjectService();
		vObjectService.setConnection(c);
		vObjectService.setSession(new Session());
		dao = new DataObjectService();
		dao.setSandboxSchema(BddPatcherTest.testSandbox1);
		pdao = new PilotageDao();
		pdao.initialize(vObjectService, dao);
	}

	@Test
	public void initializePilotageBAS() {

		VObject viewPilotageBAS = new ViewPilotageBAS();
		
		// execute query
		pdao.initializePilotageBAS(viewPilotageBAS);

		System.out.println(viewPilotageBAS.getHeadersDLabel());
		
		// test the content of the view
		List<String> viewColumns = ColumnEnum
				.listColumnEnumByName(ViewEnum.VIEW_PILOTAGE_FICHIER.getColumns().keySet());

		assertTrue(viewPilotageBAS.getHeadersDLabel().containsAll(viewColumns));

	}

}
