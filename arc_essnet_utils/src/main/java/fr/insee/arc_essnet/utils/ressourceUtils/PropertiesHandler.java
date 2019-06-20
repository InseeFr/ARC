package fr.insee.arc_essnet.utils.ressourceUtils;

import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;


@Component("properties")
@Getter
@Setter
public class PropertiesHandler {
    
    private String databasePoolName ;
    private String databaseArcUrl ;
    private String databaseArcUsername ;
    private String databaseArcPassword ;
    private String databaseArcDriverClassName ;
    private String databaseArcSchema ;
    private String annuaireArcUri ; 
    private String annuaireArcIdent ;
    private String annuaireArcPassword ;
    private String logChemin  ;
    private String logNiveau ;
    private String logConfiguration ;
    private String batchParametreRepertoire ;
    private int threadsChargement ;
    private int threadsNormage ;
    private int threadsControle ;
    private int threadsFiltrage ;
    private int threadsMapping ;
    private int threadsRegle ;
    private int  threadNombre;
    private String version;
    private String schemaReference;
    private Boolean isProd;
    private String application;
    private String tn;
    private String repertoireRoot;
    private String repertoireReception;
    private String repertoireChargement;
    private String repertoireStockage;
    
    public static PropertiesHandler getInstance() {
	GenericXmlApplicationContext ctx = new GenericXmlApplicationContext("applicationContext.xml");

	PropertiesHandler propertitiesHandler = (PropertiesHandler) ctx.getBean("properties");
	ctx.close();
	return propertitiesHandler;
    }



}
