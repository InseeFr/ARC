package fr.insee.arc.web.gui.nomenclature.service;

import java.util.ArrayList;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.web.gui.all.service.ArcWebGenericService;
import fr.insee.arc.web.gui.nomenclature.dao.GererNomenclatureDao;
import fr.insee.arc.web.gui.nomenclature.model.ModelNomenclature;
import fr.insee.arc.web.util.VObject;

@Service
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class InteractorNomenclature extends ArcWebGenericService<ModelNomenclature> {

	protected static final String RESULT_SUCCESS = "/jsp/gererNomenclature.jsp";

	private static final Logger LOGGER = LogManager.getLogger(InteractorNomenclature.class);

	@Autowired
	protected ModelNomenclature views;

	private GererNomenclatureDao dao;

	@Override
	public void putAllVObjects(ModelNomenclature model) {
		loggerDispatcher.debug("putAllVObjects()", LOGGER);

		dao = new GererNomenclatureDao(vObjectService, dataObjectService);

		views.setViewListNomenclatures(vObjectService.preInitialize(model.getViewListNomenclatures()));
		views.setViewNomenclature(vObjectService.preInitialize(model.getViewNomenclature()));
		views.setViewSchemaNmcl(vObjectService.preInitialize(model.getViewSchemaNmcl()));

		putVObject(views.getViewListNomenclatures(), t -> initializeListNomenclatures(t));

		putVObject(views.getViewNomenclature(), t -> initializeNomenclature(t, views.getViewListNomenclatures()));

		putVObject(views.getViewSchemaNmcl(), t -> initializeSchemaNmcl(t, views.getViewListNomenclatures()));

		loggerDispatcher.debug("putAllVObjects() end", LOGGER);
	}

	@Override
	public String getActionName() {
		return "externalFileManagement";
	}

	private static final String NOM_TABLE = "nom_table";

	/**
	 * Initializes {@code ModelNomenclature#viewListNomenclatures}. Calls dao to
	 * create the view.
	 * 
	 * @param viewListNomenclatures
	 */
	public void initializeListNomenclatures(VObject viewListNomenclatures) {
		LoggerHelper.debug(LOGGER, "/* initializeListeNomenclatures */");
		dao.initializeViewListNomenclatures(viewListNomenclatures);
	}

	/**
	 * Initializes {@code ModelNomenclature#viewNomenclature}. Only gets the
	 * nomenclature linked to the selection in the nomenclature list.
	 * 
	 * @param viewNomenclature
	 * @param viewListNomenclatures
	 */
	public void initializeNomenclature(VObject viewNomenclature, VObject viewListNomenclatures) {
		LoggerHelper.debug(LOGGER, "/* initializeViewNomenclature */");
		try {
			// get the list nomenclatures selected record
			Map<String, ArrayList<String>> selectionListNomenclatures = viewListNomenclatures.mapContentSelected();
			// if nomenclature selected, trigger call to dao to construct nomenclature view
			if (!selectionListNomenclatures.isEmpty() && Boolean.TRUE.equals(UtilitaireDao.get(0)
					.isTableExiste(null, "arc." + selectionListNomenclatures.get(NOM_TABLE).get(0)))) {
				dao.setSelectedRecords(selectionListNomenclatures);
				dao.initializeViewNomenclature(viewNomenclature, NOM_TABLE,
						selectionListNomenclatures.get(NOM_TABLE).get(0));
			} else {
				vObjectService.destroy(viewNomenclature);
			}
		} catch (Exception ex) {
			StaticLoggerDispatcher.error("Error in InteractorNomenclature.initializeNomenclature", LOGGER);
		}
	}

	/**
	 * Initializes {@code ModelNomenclature#viewSchemaNmcl}. Only gets the
	 * schema linked to the selection in the nomenclature list.
	 * 
	 * @param viewSchemaNmcl
	 * @param viewListNomenclatures
	 */
	public void initializeSchemaNmcl(VObject viewSchemaNmcl, VObject viewListNomenclatures) {
		LoggerHelper.debug(LOGGER, "/* initializeSchemaNmcl */");
		try {
			// get the list nomenclatures selected record
			Map<String, ArrayList<String>> selectionListNomenclatures = viewListNomenclatures.mapContentSelected();
			// if nomenclature selected, trigger call to dao to construct schema
			if (!selectionListNomenclatures.isEmpty()) {
				dao.setSelectedRecords(selectionListNomenclatures);
				dao.initializeViewSchemaNmcl(viewSchemaNmcl);
			} else {
				vObjectService.destroy(viewSchemaNmcl);
			}
		} catch (Exception ex) {
			StaticLoggerDispatcher.error("Error in InteractorNomenclature.initializeSchemaNmcl", LOGGER);
		}
	}


}