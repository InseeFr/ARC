package fr.insee.arc.core;

import org.springframework.context.support.GenericXmlApplicationContext;

import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;

public class Main {

    public static void main(String[] args) {
	GenericXmlApplicationContext ctx = new GenericXmlApplicationContext("applicationContext.xml");

	PropertiesHandler propertitiesHandler = (PropertiesHandler) ctx.getBean("properties");
	
	System.out.println(propertitiesHandler.getAnnuaireArcIdent());
    
    }

}
