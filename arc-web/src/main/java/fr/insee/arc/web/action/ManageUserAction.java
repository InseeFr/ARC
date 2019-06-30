package fr.insee.arc.web.action;

import java.util.Map;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.interceptor.SessionAware;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;


/**
 * Empty class for know. Will manage the user of the application (add, delete, update right). Was using
 * and internal library, thats why it's empty know.
 * TODO : make this class usefull again
 * @author Pépin Rémi
 *
 */
@Component
@Results({ @Result(name = "success", location = "/jsp/gererUtilisateurs.jsp"),
	@Result(name = "index", location = "/jsp/index.jsp") })
@Getter
@Setter
public class ManageUserAction implements SessionAware {

    
    
    @Override
    public void setSession(Map<String, Object> session) {
	// TODO Auto-generated method stub
	
    }

  

}
