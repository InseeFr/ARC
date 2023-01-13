package fr.insee.arc.web.gui.index.service;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

import fr.insee.arc.web.gui.all.model.NoModel;
import fr.insee.arc.web.gui.all.service.ArcWebGenericService;

@Service
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class IndexAction extends ArcWebGenericService<NoModel> {

	public static final String ACTION_NAME = "index";
	protected static final String RESULT_SUCCESS = "jsp/index.jsp";

	@Override
	public void putAllVObjects(NoModel arcModel) {
		// no vObject in this controller
	}

	@Override
	public String getActionName() {
		return ACTION_NAME;
	}

}