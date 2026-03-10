package fr.insee.arc.web.gui.report.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.web.gui.all.service.ArcWebGenericService;
import fr.insee.arc.web.gui.all.util.VObject;
import fr.insee.arc.web.gui.report.dao.GererReportDao;
import fr.insee.arc.web.gui.report.model.ModelReport;

@Service
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class InteractorReport extends ArcWebGenericService<ModelReport, GererReportDao> {
	
	protected static final String RESULT_SUCCESS = "jsp/gererReport.jsp";

	private static final Logger LOGGER = LogManager.getLogger(InteractorReport.class);
	
	@Autowired
	protected ModelReport views;

	// The action Name
	public static final String ACTION_NAME="reportManagement";

	@Override
	public void putAllVObjects(ModelReport model) {

		views.setViewReport(vObjectService.preInitialize(model.getViewReport()));

		putVObject(views.getViewReport(), t -> initializeViewReport(t));
	}

	@Override
	public String getActionName() {
		return ACTION_NAME;
	}
	
	
	/**
	 * Initialize the {@value InteractorReport#viewNorme}. Call dao to create the view
	 */
	public void initializeViewReport(VObject viewReport) {
		LoggerHelper.debug(LOGGER, "/* initializeNorme */");
		dao.initializeViewReport(viewReport);
	}


}
