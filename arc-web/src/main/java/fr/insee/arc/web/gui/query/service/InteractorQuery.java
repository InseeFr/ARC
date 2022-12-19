package fr.insee.arc.web.gui.query.service;

import java.util.HashMap;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.context.WebApplicationContext;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.textUtils.IConstanteCaractere;
import fr.insee.arc.web.gui.ArcWebGenericService;
import fr.insee.arc.web.gui.query.model.ModelQuery;

@Service
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class InteractorQuery extends ArcWebGenericService<ModelQuery> implements  IConstanteCaractere {

	protected static final String RESULT_SUCCESS = "/jsp/gererQuery.jsp";

	private static final String DEFAULT_SCHEMA = "arc";

	protected String myQuery;

	protected String mySchema;
	
	protected ModelQuery views;

	@Override
	protected void putAllVObjects(ModelQuery model) {
		views.setViewQuery(vObjectService.preInitialize(model.getViewQuery()));
		views.setViewTable(vObjectService.preInitialize(model.getViewTable()));

		if (model.getMySchema() != null && !model.getMySchema().trim().isEmpty()) {
			this.mySchema = model.getMySchema().trim().toLowerCase();
		} else {
			this.mySchema = DEFAULT_SCHEMA;
		}
		this.myQuery = model.getMyQuery();

		putVObject(views.getViewQuery(), t -> initializeQuery());
		putVObject(views.getViewTable(), t -> initializeTable());
	}
	
	@Override
	public void extraModelAttributes(Model model) {
		model.addAttribute("myQuery", myQuery);
		model.addAttribute("mySchema", mySchema);
	}

	@Override
	protected String getActionName() {
		return "databaseManagement";
	}

	public void initializeQuery() {
		HashMap<String, String> defaultInputFields = new HashMap<>();

		if (this.myQuery!=null){
			String m=this.myQuery.trim();
			if (m.endsWith(";"))
			{
				m=m.substring(0, m.length()-1);
			}

			ArcPreparedStatementBuilder requete=new ArcPreparedStatementBuilder(m);
			
			if (Boolean.TRUE.equals(UtilitaireDao.get("arc").testResultRequest(null, requete)))
			{
				this.vObjectService.initialize(views.getViewQuery(), requete, "arc.ihm_Query", defaultInputFields);
			}
			else
			{
				try {
					UtilitaireDao.get("arc").executeImmediate(null, this.myQuery);
					this.vObjectService.destroy(views.getViewQuery());
					this.views.getViewQuery().setMessage("Requete terminée !");
				} catch (Exception e) {
					this.vObjectService.destroy(views.getViewQuery());
					this.views.getViewQuery().setMessage(e.getMessage());
				}

			}
		}

	}


	// Table list
	public void initializeTable() {
		HashMap<String, String> defaultInputFields = new HashMap<>();
		this.vObjectService.initialize(views.getViewTable(), new ArcPreparedStatementBuilder("select tablename from pg_tables where schemaname='" + this.mySchema+"'"), "arc.ihm_Table", defaultInputFields);

	}

}