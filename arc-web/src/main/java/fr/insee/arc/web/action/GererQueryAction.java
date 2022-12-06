package fr.insee.arc.web.action;

import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.textUtils.IConstanteCaractere;
import fr.insee.arc.web.model.DatabaseManagementModel;
import fr.insee.arc.web.service.ArcWebGenericService;
import fr.insee.arc.web.util.VObject;

@Controller
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class GererQueryAction extends ArcWebGenericService<DatabaseManagementModel> implements  IConstanteCaractere {

	private static final String RESULT_SUCCESS = "/jsp/gererQuery.jsp";

	private static final String DEFAULT_SCHEMA = "arc";

	private VObject viewQuery;

	private VObject viewTable;

	private String myQuery;

	private String mySchema;

	@Override
	protected void putAllVObjects(DatabaseManagementModel model) {
		this.setViewQuery(vObjectService.preInitialize(model.getViewQuery()));
		this.setViewTable(vObjectService.preInitialize(model.getViewTable()));

		if (model.getMySchema() != null && !model.getMySchema().trim().isEmpty()) {
			this.mySchema = model.getMySchema().trim().toLowerCase();
		} else {
			this.mySchema = DEFAULT_SCHEMA;
		}
		this.myQuery = model.getMyQuery();

		putVObject(getViewQuery(), t -> initializeQuery());
		putVObject(getViewTable(), t -> initializeTable());
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
		System.out.println("/* initializeQuery */");
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
				this.vObjectService.initialize(viewQuery, requete, "arc.ihm_Query", defaultInputFields);
			}
			else
			{
				try {
					UtilitaireDao.get("arc").executeImmediate(null, this.myQuery);
					this.vObjectService.destroy(viewQuery);
					this.viewQuery.setMessage("Requete terminée !");
				} catch (Exception e) {
					this.vObjectService.destroy(viewQuery);
					this.viewQuery.setMessage(e.getMessage());
				}

			}
		}

	}

	@RequestMapping("/selectQuery")
	public String selectQuery(Model model) {
		return basicAction(model, RESULT_SUCCESS);
	}

	@RequestMapping("/sortQuery")
	public String sortQuery(Model model) {
		return sortVobject(model, RESULT_SUCCESS, viewQuery);
	}


	// Table list
	public void initializeTable() {
		System.out.println("/* initializeTable */");
		HashMap<String, String> defaultInputFields = new HashMap<>();
		this.vObjectService.initialize(viewTable, new ArcPreparedStatementBuilder("select tablename from pg_tables where schemaname='" + this.mySchema+"'"), "arc.ihm_Table", defaultInputFields);

	}

	@RequestMapping({"/selectTable", "/seeTable"})
	public String seeTable(Model model) {
		HashMap<String, ArrayList<String>> mapContentSelected = viewTable.mapContentSelected();
		if (!mapContentSelected.isEmpty()) {
			this.myQuery = "select * from " + this.mySchema+"." + mapContentSelected.get("tablename").get(0) + " limit 10 ";
			model.addAttribute("myQuery", myQuery);
		}
		return basicAction(model, RESULT_SUCCESS);
	}

	@RequestMapping("/sortTable")
	public String sortTable(Model model) {
		return sortVobject(model, RESULT_SUCCESS, viewTable);
	}


	public VObject getViewQuery() {
		return this.viewQuery;
	}

	public void setViewQuery(VObject viewQuery) {
		this.viewQuery = viewQuery;
	}

	public VObject getViewTable() {
		return this.viewTable;
	}

	public void setViewTable(VObject viewTable) {
		this.viewTable = viewTable;
	}

}