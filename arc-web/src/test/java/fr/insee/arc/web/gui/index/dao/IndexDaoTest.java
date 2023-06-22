package fr.insee.arc.web.gui.index.dao;

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
import fr.insee.arc.web.gui.index.model.ViewIndex;
import fr.insee.arc.web.util.Session;
import fr.insee.arc.web.util.VObject;
import fr.insee.arc.web.util.VObjectService;

public class IndexDaoTest extends InitializeQueryTest {

	private static VObjectService vObjectService;
	private static DataObjectService dao;
	private static IndexDao pdao;

	@BeforeClass
	public static void setup() throws ArcException {
		BddPatcherTest.createDatabase();
		vObjectService = new VObjectService();
		vObjectService.setConnection(c);
		vObjectService.setSession(new Session());
		dao = new DataObjectService(BddPatcherTest.testSandbox);
		pdao = new IndexDao(vObjectService, dao);
	}

	@Test
	public void initializeViewIndex() {

		VObject viewIndex = new ViewIndex();

		// execute query
		pdao.initializeViewIndex(viewIndex);

		// test the content of the view
		List<String> viewColumns = ColumnEnum.listColumnEnumByName(ViewEnum.EXT_ETAT_JEUDEREGLE.getColumns().keySet());
		viewColumns.removeAll(viewIndex.getHeadersDLabel());

		assertEquals(2, viewColumns.size());
	}

}
