package fr.insee.arc.web.gui.maintenanceparametre.dao;

import static org.junit.Assert.assertEquals;

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
import fr.insee.arc.web.gui.maintenanceparametre.model.ViewParameters;

public class MaintenanceParametreDaoTest extends InitializeQueryTest {

	private static VObjectService vObjectService;
	private static DataObjectService dao;
	private static MaintenanceParametreDao pdao;

	@BeforeClass
	public static void setup() throws ArcException {
		BddPatcherTest.createDatabase();
		vObjectService = new VObjectService();
		vObjectService.setConnection(c);
		vObjectService.setSession(new Session());
		dao = new DataObjectService(BddPatcherTest.testSandbox1);
		pdao = new MaintenanceParametreDao(vObjectService, dao);
	}

	@Test
	public void initializeViewParameters() {

		VObject viewParameters = new ViewParameters();
		
		// execute query
		pdao.initializeViewParameters(viewParameters);

		// test the content of the view
		List<String> viewColumns = ColumnEnum
				.listColumnEnumByName(ViewEnum.PARAMETER.getColumns().keySet());
		viewColumns.removeAll(viewParameters.getHeadersDLabel());

		assertEquals(0, viewColumns.size());

	}

}
