package fr.insee.arc.web.action;

import java.util.HashMap;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.textUtils.IConstanteCaractere;
import fr.insee.arc.web.model.DatabaseManagementModel;
import fr.insee.arc.web.util.VObject;

@Controller
public class GererQueryAction extends ArcAction<DatabaseManagementModel> implements  IConstanteCaractere {
	
	private static final String RESULT_SUCCESS = "/jsp/gererQuery.jsp";

    private String defaultSchema = "arc";
    
    private VObject viewQuery;

    private VObject viewTable;
    
    private String myQuery;

    private String mySchema;

    @Override
    protected void putAllVObjects(DatabaseManagementModel model) {
    	this.setViewQuery(vObjectService.preInitialize(model.getViewQuery()));
    	this.setViewTable(vObjectService.preInitialize(model.getViewTable()));
    	this.myQuery = model.getMyQuery();
    	this.mySchema = model.getMySchema();
    	
    	putVObject(getViewQuery(), t -> initializeQuery());
    	putVObject(getViewTable(), t -> initializeTable());
    }

    @Override
    protected String getActionName() {
    	return "databaseManagement";
    }
    
    public void initializeQuery() {
        System.out.println("/* initializeQuery */");
        HashMap<String, String> defaultInputFields = new HashMap<String, String>();

        	if (this.myQuery!=null && !this.myQuery.trim().equals(""))
        	{
        		String m=this.myQuery.trim();
        		if (m.endsWith(";"))
        		{
        			m=m.substring(0, m.length()-1);
        		}

                if (UtilitaireDao.get("arc").testResultRequest(null, m))
		        {
		            this.vObjectService.initialize(m, "arc.ihm_Query", defaultInputFields, viewQuery);
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
    public String selectQuery() {
        return generateDisplay(RESULT_SUCCESS);
    }

    @RequestMapping("/updateQuery")
    public String updateQuery() {
        this.vObjectService.update(viewQuery);
        return generateDisplay(RESULT_SUCCESS);
    }

    @RequestMapping("/sortQuery")
    public String sortQuery() {
        this.vObjectService.sort(viewQuery);
        return generateDisplay(RESULT_SUCCESS);
    }


    // private SessionMap session;
    // visual des Tables
    public void initializeTable() {
        System.out.println("/* initializeTable */");
        HashMap<String, String> defaultInputFields = new HashMap<String, String>();

        	if (this.mySchema!=null && !this.mySchema.trim().equals(""))
        	{
        		this.mySchema=this.mySchema.trim().toLowerCase();
        	}
        	else
        	{
        		this.mySchema=this.defaultSchema;
        	}

    		this.vObjectService.initialize("select tablename from pg_tables where schemaname='"+this.mySchema+"'", "arc.ihm_Table", defaultInputFields, viewTable);

    }

    @RequestMapping("/selectTable")
    public String selectTable() {
        this.myQuery="select * from "+this.mySchema+"."+this.vObjectService.mapContentSelected(viewTable).get("tablename").get(0)+" limit 10 ";
        return generateDisplay(RESULT_SUCCESS);
    }

    @RequestMapping("/seeTable")
    public String seeTable() {
        this.myQuery="select * from "+this.mySchema+"."+this.vObjectService.mapContentSelected(viewTable).get("tablename").get(0)+" limit 10 ";
        return generateDisplay(RESULT_SUCCESS);
    }


    @RequestMapping("/updateTable")
    public String updateTable() {
        this.vObjectService.update(viewTable);
        return generateDisplay(RESULT_SUCCESS);
    }

    @RequestMapping("/sortTable")
    public String sortTable() {
        this.vObjectService.sort(viewTable);
        return generateDisplay(RESULT_SUCCESS);
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