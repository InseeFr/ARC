package fr.insee.arc.web.gui.index.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.web.gui.all.service.ArcWebGenericService;
import fr.insee.arc.web.gui.index.dao.IndexDao;
import fr.insee.arc.web.gui.index.model.ModelIndex;
import fr.insee.arc.web.util.VObject;

@Service
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class IndexAction extends ArcWebGenericService<ModelIndex> {

	public static final String ACTION_NAME = "index";
	protected static final String RESULT_SUCCESS = "jsp/index.jsp";
	
	private static final Logger LOGGER = LogManager.getLogger(IndexAction.class);

	@Autowired
	protected ModelIndex views;
	
	private IndexDao dao;
	
	@Override
	public void putAllVObjects(ModelIndex arcModel) {
		loggerDispatcher.debug("putAllVObjects()", LOGGER);
		
		dao = new IndexDao(vObjectService, dataObjectService);

		views.setViewIndex(vObjectService.preInitialize(arcModel.getViewIndex()));
		
		putVObject(views.getViewIndex(), t -> initializeIndex(t));

		loggerDispatcher.debug("putAllVObjects() end", LOGGER);
	}
	
	@Override
	public String getActionName() {
		return ACTION_NAME;
	}

	/**
	 * Initializes {@code ModelIndex#viewIndex}. Calls dao to create the view.
	 * 
	 * @param viewIndex
	 */
	private void initializeIndex(VObject viewIndex) {
		LoggerHelper.debug(LOGGER, "/* initializeIndex */");
		dao.initializeViewIndex(viewIndex);
	}

}