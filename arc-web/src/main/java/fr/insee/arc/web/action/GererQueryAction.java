package fr.insee.arc.web.action;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.interceptor.SessionAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.textUtils.IConstanteCaractere;
import fr.insee.arc.web.util.VObject;

@Component
@Results({ @Result(name = "success", location = "/jsp/gererQuery.jsp"), @Result(name = "index", location = "/jsp/index.jsp") })
public class GererQueryAction implements SessionAware, IConstanteCaractere {
    @Override
    public void setSession(Map<String, Object> session) {
    	this.viewTable.setMessage("");
        this.viewQuery.setMessage("");
    }

    public String myQuery;
    public String mySchema;
    public String defaultSchema="arc";

    @SuppressWarnings("unused")
	private static final Logger LOGGER = LogManager.getLogger(GererQueryAction.class);
    @Autowired
    @Qualifier("viewQuery")
    VObject viewQuery;

    @Autowired
    @Qualifier("viewTable")
    VObject viewTable;

    // pour charger un fichier CSV
        private String scope;

    public String sessionSyncronize() {
        this.viewQuery.setActivation(this.scope);
        this.viewTable.setActivation(this.scope);
        Boolean defaultWhenNoScope = true;

        if (this.viewQuery.getIsScoped()) {
            initializeQuery();
            defaultWhenNoScope = false;
        }

        if (this.viewTable.getIsScoped()) {
	        initializeTable();
	        defaultWhenNoScope = false;
	    }


        if (defaultWhenNoScope) {
            System.out.println("default");

            initializeQuery();
            this.viewQuery.setIsActive(true);
            this.viewQuery.setIsScoped(true);

            initializeTable();
            this.viewTable.setIsActive(true);
            this.viewTable.setIsScoped(true);
        }
        return "success";

    }

    // private SessionMap session;
    // visual des Querys
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
		            this.viewQuery.initialize(m, "arc.ihm_Query", defaultInputFields);
		        }
		        else
		        {
		        	try {
		                UtilitaireDao.get("arc").executeImmediate(null, this.myQuery);
		                this.viewQuery.destroy();
		                this.viewQuery.setMessage("Requete terminée !");
	                } catch (Exception e) {
	                	this.viewQuery.destroy();
		                this.viewQuery.setMessage(e.getMessage());
	                }

		        }
        	}

    }

    @Action(value = "/selectQuery")
    public String selectQuery() {
        System.out.println("selectQuery " + this.scope);
        return sessionSyncronize();
    }

    @Action(value = "/updateQuery")
    public String updateQuery() {
        this.viewQuery.update();
        return sessionSyncronize();
    }

    @Action(value = "/sortQuery")
    public String sortQuery() {
        this.viewQuery.sort();
        return sessionSyncronize();
    }


    public VObject getViewQuery() {
        return this.viewQuery;
    }

    public void setViewQuery(VObject viewQuery) {
        this.viewQuery = viewQuery;
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

    		this.viewTable.initialize("select tablename from pg_tables where schemaname='"+this.mySchema+"'", "arc.ihm_Table", defaultInputFields);

    }

    @Action(value = "/selectTable")
    public String selectTable() {
        System.out.println("selectTable " + this.scope);
        return sessionSyncronize();
    }

    @Action(value = "/seeTable")
    public String seeTable() {
        System.out.println("seeTable " + this.scope);
        this.myQuery="select * from "+this.mySchema+"."+this.viewTable.mapContentSelected().get("tablename").get(0)+" limit 10 ";
        return sessionSyncronize();
    }


    @Action(value = "/updateTable")
    public String updateTable() {
        this.viewTable.update();
        return sessionSyncronize();
    }

    @Action(value = "/sortTable")
    public String sortTable() {
        this.viewTable.sort();
        return sessionSyncronize();
    }


    public VObject getViewTable() {
        return this.viewTable;
    }

    public void setViewTable(VObject viewTable) {
        this.viewTable = viewTable;
    }















    public String getMySchema() {
		return this.mySchema;
	}

	public void setMySchema(String mySchema) {
		this.mySchema = mySchema;
	}

	public String getMyQuery() {
		return this.myQuery;
	}

	public void setMyQuery(String myQuery) {
		this.myQuery = myQuery;
	}

	/**
     * @return the scope
     */
    public final String getScope() {
        return this.scope;
    }

    /**
     * @param scope
     *            the scope to set
     */
    public final void setScope(String scope) {
        this.scope = scope;
    }
}
