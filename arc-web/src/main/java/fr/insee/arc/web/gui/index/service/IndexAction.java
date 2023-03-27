package fr.insee.arc.web.gui.index.service;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.web.gui.all.service.ArcWebGenericService;
import fr.insee.arc.web.gui.index.model.ModelIndex;

@Service
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class IndexAction extends ArcWebGenericService<ModelIndex> {

	public static final String ACTION_NAME = "index";
	protected static final String RESULT_SUCCESS = "jsp/index.jsp";
	
	private static final Logger LOGGER = LogManager.getLogger(IndexAction.class);

	@Autowired
	protected ModelIndex views;
	
	@Override
	public void putAllVObjects(ModelIndex arcModel) {
		loggerDispatcher.debug("putAllVObjects()", LOGGER);

		views.setViewIndex(vObjectService.preInitialize(arcModel.getViewIndex()));
		
		putVObject(views.getViewIndex(), t -> initializeIndex());

		loggerDispatcher.debug("putAllVObjects() end", LOGGER);
	}
	
	@Override
	public String getActionName() {
		return ACTION_NAME;
	}

	private void initializeIndex() {
		HashMap<String, String> defaultInputFields = new HashMap<>();
		this.vObjectService.initialize(views.getViewIndex(),
				new ArcPreparedStatementBuilder(
						"select id, val, env_description from arc.ext_etat_jeuderegle order by id"),
				"arc.ext_etat_jeuderegle", defaultInputFields);
	}

}