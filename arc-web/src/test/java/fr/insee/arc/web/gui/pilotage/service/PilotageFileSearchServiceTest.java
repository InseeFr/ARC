package fr.insee.arc.web.gui.pilotage.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import fr.insee.arc.web.gui.all.util.Session;
import fr.insee.arc.web.gui.all.util.VObjectService;
import fr.insee.arc.web.gui.pilotage.dao.HttpSessionTemplate;
import fr.insee.arc.web.gui.pilotage.model.ModelPilotage;
import fr.insee.arc.web.gui.pilotage.model.PilotageFileSearch;

/** Exercise transitions without initializing the application or invoking the engine. */
class PilotageFileSearchServiceTest {

	private ModelPilotage views;
	private PilotageService pilotage;
	private RapportService rapport;

	@BeforeEach
	void prepareServices() {
		views = new ModelPilotage();
		Session session = new Session();
		session.setHttpSession(new HttpSessionTemplate());
		VObjectService service = new VObjectService(session);
		pilotage = new PilotageService(views, service);
		rapport = new RapportService(views, service);
	}

	@Test
	void searchClearsGridSelectionsAndReplacesPreviousSearch() {
		views.getViewPilotageBAS().setSelectedLines(new ArrayList<>(List.of(true)));
		views.getViewPilotageBAS().setSelectedColumns(new ArrayList<>(List.of(true)));
		views.getViewRapportBAS().setSelectedLines(new ArrayList<>(List.of(true)));
		pilotage.searchPilotageBAS(new ExtendedModelMap(), "MAPPING", "FIP6");
		assertTrue(views.getViewPilotageBAS().getSelectedLines().isEmpty());
		assertTrue(views.getViewPilotageBAS().getSelectedColumns().isEmpty());
		assertTrue(views.getViewRapportBAS().getSelectedLines().isEmpty());
		assertTrue(PilotageFileSearch.isActive(views.getViewFichierBAS()));
		pilotage.searchPilotageBAS(new ExtendedModelMap(), "CHARGEMENT", "FIP7");
		assertEquals("CHARGEMENT", PilotageFileSearch.getPhase(views.getViewFichierBAS()).toString());
		assertEquals("FIP7", PilotageFileSearch.getPattern(views.getViewFichierBAS()));
	}

	@Test
	void selectingACellOrReportLeavesSearchMode() {
		pilotage.searchPilotageBAS(new ExtendedModelMap(), "MAPPING", "FIP6");
		pilotage.selectPilotageBAS(new ExtendedModelMap());
		assertFalse(PilotageFileSearch.isActive(views.getViewFichierBAS()));
		pilotage.searchPilotageBAS(new ExtendedModelMap(), "MAPPING", "FIP6");
		rapport.selectRapportBAS(new ExtendedModelMap());
		assertFalse(PilotageFileSearch.isActive(views.getViewFichierBAS()));
	}

	@Test
	void filteringTheGridsKeepsTheSearchDisplayedInTheDetail() {
		pilotage.searchPilotageBAS(new ExtendedModelMap(), "MAPPING", "FIP6");
		pilotage.setScope("viewPilotageBAS;viewRapportBAS;");
		pilotage.selectPilotageBAS(new ExtendedModelMap());
		assertTrue(PilotageFileSearch.isActive(views.getViewFichierBAS()));
		rapport.setScope("viewRapportBAS;");
		rapport.selectRapportBAS(new ExtendedModelMap());
		assertTrue(PilotageFileSearch.isActive(views.getViewFichierBAS()));
		rapport.setScope("viewPilotageBAS;viewRapportBAS;viewFichierBAS;");
		rapport.selectRapportBAS(new ExtendedModelMap());
		assertFalse(PilotageFileSearch.isActive(views.getViewFichierBAS()));
	}

	@Test
	void clearingOrSubmittingAnInvalidSearchRemovesThePreviousResultContext() {
		pilotage.searchPilotageBAS(new ExtendedModelMap(), "MAPPING", "FIP6");
		pilotage.clearPilotageSearchBAS(new ExtendedModelMap());
		assertFalse(PilotageFileSearch.isActive(views.getViewFichierBAS()));
		pilotage.searchPilotageBAS(new ExtendedModelMap(), "MAPPING", "FIP6");
		pilotage.searchPilotageBAS(new ExtendedModelMap(), "EXPORT", "%");
		assertFalse(PilotageFileSearch.isActive(views.getViewFichierBAS()));
		assertEquals("managementSandbox.search.invalid", views.getViewPilotageBAS().getMessage());
		assertFalse(views.getViewFichierBAS().getIsInitialized());
	}

	private static class PilotageService extends ServiceViewPilotageBAS {
		PilotageService(ModelPilotage model, VObjectService service) {
			views = model;
			vObjectService = service;
			setBacASable("file_search_test");
		}

		@Override
		public String generateDisplay(Model model, String uri) {
			return uri;
		}
	}

	private static class RapportService extends ServiceViewRapportBAS {
		RapportService(ModelPilotage model, VObjectService service) {
			views = model;
			vObjectService = service;
		}

		@Override
		public String generateDisplay(Model model, String uri) {
			return uri;
		}
	}
}
