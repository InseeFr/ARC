package fr.insee.arc_essnet.web.action;

import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.struts2.interceptor.SessionAware;


public class Authentifier implements SessionAware
{
    private static final Logger LOGGER = Logger.getLogger(Authentifier.class);
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
