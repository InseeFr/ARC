package fr.insee.arc.web.gui.query.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.context.WebApplicationContext;

import fr.insee.arc.core.dataobjects.ArcDatabase;
import fr.insee.arc.utils.textUtils.IConstanteCaractere;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.web.gui.all.service.ArcWebGenericService;
import fr.insee.arc.web.gui.all.util.VObject;
import fr.insee.arc.web.gui.query.dao.QueryDao;
import fr.insee.arc.web.gui.query.model.ModelQuery;

@Service
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class InteractorQuery extends ArcWebGenericService<ModelQuery, QueryDao> implements  IConstanteCaractere {

	protected static final String RESULT_SUCCESS = "jsp/gererQuery.jsp";

	private static final String DEFAULT_SCHEMA = "arc";
	private static final Logger LOGGER = LogManager.getLogger(InteractorQuery.class);

	protected Integer myDbConnection;
	
	protected String myQuery;

	protected String mySchema;
	
	@Autowired
	protected ModelQuery views;

	@Override
	protected void putAllVObjects(ModelQuery model) {

		views.setViewQuery(vObjectService.preInitialize(model.getViewQuery()));
		views.setViewTable(vObjectService.preInitialize(model.getViewTable()));

		if (this.myDbConnection==null)
		{
			this.myDbConnection=ArcDatabase.COORDINATOR.getIndex();
		}
		
		if (model.getMySchema() != null && !model.getMySchema().trim().isEmpty()) {
			this.mySchema = model.getMySchema().trim().toLowerCase();
		} else {
			this.mySchema = DEFAULT_SCHEMA;
		}
		this.myQuery = model.getMyQuery();

		putVObject(views.getViewQuery(), t -> initializeQuery(t));
		putVObject(views.getViewTable(), t -> initializeTable(t));
	}
	
	@Override
	public void extraModelAttributes(Model model) {
		model.addAttribute("myQuery", myQuery);
		model.addAttribute("mySchema", mySchema);
		model.addAttribute("myDbConnection", myDbConnection);

	}

	@Override
	protected String getActionName() {
		return "databaseManagement";
	}

	public void initializeQuery(VObject viewQuery) {
		LoggerHelper.debug(LOGGER, "/* initializeQuery */");
		dao.initializeQuery(viewQuery, myDbConnection, this.myQuery);
	}


	// Table list
	public void initializeTable(VObject viewTable) {
		LoggerHelper.debug(LOGGER, "/* initializeTable */");
		dao.initializeTable(viewTable, myDbConnection, this.mySchema);
	}

}