package fr.insee.arc.web.action;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.interceptor.SessionAware;


public class Authentifier implements SessionAware
{
    @SuppressWarnings("unused")
	private static final Logger LOGGER = LogManager.getLogger(Authentifier.class);
    private Map<String, Object> session;
    private String user;


    @Override
    public void setSession(Map<String, Object> arg0)
    {
        this.session = arg0;
    }

    
    public void grantAccess(String... profils)
    {

    }

    public String getUser()
    {
        return this.user;
    }

    public void setUser(String user)
    {
        this.user = user;
    }

    /**
     * @return the session
     */
    public final Map<String, Object> getSession()
    {
        return this.session;
    }
}
