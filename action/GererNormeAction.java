package fr.insee.arc_composite.web.action;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.interceptor.SessionAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import fr.insee.arc_composite.core.dao.MappingRegleDao;
import fr.insee.arc_composite.core.model.IDbConstant;
import fr.insee.arc_composite.core.model.JeuDeRegle;
import fr.insee.arc_composite.core.model.RegleControleEntity;
import fr.insee.arc_composite.core.model.RegleMappingEntity;
import fr.insee.arc_composite.core.model.TraitementTableParametre;
import fr.insee.arc_composite.core.service.ApiMappingService;
import fr.insee.arc_composite.core.service.engine.controle.ControleRegleService;
import fr.insee.arc_composite.core.service.engine.mapping.RegleMappingFactory;
import fr.insee.arc_composite.core.service.engine.mapping.VariableMapping;
import fr.insee.arc_composite.core.service.engine.mapping.regles.AbstractRegleMapping;
import fr.insee.arc_composite.core.service.engine.xsd.XsdControlDescription;
import fr.insee.arc_composite.core.service.engine.xsd.XsdExtractionService;
import fr.insee.arc_composite.core.service.engine.xsd.XsdRulesRetrievalService;
import fr.insee.siera.core.dao.EntityDao;
import fr.insee.siera.core.dao.UtilitaireDao;
import fr.insee.siera.core.format.Format;
import fr.insee.siera.core.structure.AttributeValue;
import fr.insee.siera.core.util.FormatSQL;
import fr.insee.siera.core.util.LoggerHelper;
import fr.insee.siera.core.util.ManipString;
import fr.insee.siera.textutils.IConstanteCaractere;
import fr.insee.siera.webutils.LoggerDispatcher;
import fr.insee.siera.webutils.VObject;

@Component
@Results({
	@Result(name = "success", location = "/jsp/gererNorme.jsp"),
	@Result(name = "index", location = "/jsp/index.jsp")})
public class GererNormeAction implements SessionAware, IDbConstant, IConstanteCaractere {
    @Override
    public void setSession(Map<String, Object> session) {
        this.viewNorme.setMessage("");
        this.viewCalendrier.setMessage("");
        this.viewJeuxDeRegles.setMessage("");
        this.viewChargement.setMessage("");
        this.viewNormage.setMessage("");
        this.viewControle.setMessage("");
        this.viewFiltrage.setMessage("");
        this.viewMapping.setMessage("");
        this.viewJeuxDeReglesCopie.setMessage("");
    }

    public static final int indexColonneVariableTableRegleMapping = 6;

    @Autowired
    ControleRegleService service;
    private static final Logger LOGGER = Logger.getLogger(GererNormeAction.class);
    @Autowired
    @Qualifier("viewNorme")
    VObject viewNorme;
    @Autowired
    @Qualifier("viewCalendrier")
    VObject viewCalendrier;
    @Autowired
    @Qualifier("viewJeuxDeRegles")
    VObject viewJeuxDeRegles;
    @Autowired
    @Qualifier("viewChargement")
    VObject viewChargement;
    @Autowired
    @Qualifier("viewNormage")
    VObject viewNormage;
    @Autowired
    @Qualifier("viewControle")
    VObject viewControle;
    @Autowired
    @Qualifier("viewFiltrage")
    VObject viewFiltrage;
    @Autowired
    @Qualifier("viewMapping")
    VObject viewMapping;
    @Autowired
    @Qualifier("viewJeuxDeReglesCopie")
    VObject viewJeuxDeReglesCopie;
    private final static String selectedJeuDeRegle = "selectedJeuDeRegle";

    private static final String clefConsolidation = "{clef}";

    private static final String tokenNomVariable = "{tokenNomVariable}";

    private static final String messageVariableClefNull = "La variable {tokenNomVariable} est une variable clef pour la consolidation.\nVous devez vous assurer qu'elle ne soit jamais null.";

    // pour charger un fichier CSV
    private File fileUpload;
    private String fileUploadContentType;
    private String fileUploadFileName;

    private String scope;

    private XsdControlDescription controlDescription;
    private XsdControlDescription controlDescriptionFiltered;
    private String currentSelectedControlRule;

    public String sessionSyncronize() {
        this.viewNorme.setActivation(this.scope);
        this.viewCalendrier.setActivation(this.scope);
        this.viewJeuxDeRegles.setActivation(this.scope);
        this.viewControle.setActivation(this.scope);
        this.viewChargement.setActivation(this.scope);
        this.viewNormage.setActivation(this.scope);
        this.viewFiltrage.setActivation(this.scope);
        this.viewMapping.setActivation(this.scope);
        this.viewJeuxDeReglesCopie.setActivation(this.scope);

        Boolean defaultWhenNoScope = true;

        if (this.viewNorme.getIsScoped()) {
            initializeNorme();
            defaultWhenNoScope = false;
        }

        if (this.viewCalendrier.getIsScoped()) {
            initializeCalendrier();
            defaultWhenNoScope = false;
        }

        if (this.viewJeuxDeRegles.getIsScoped()) {
            initializeJeuxDeRegles();
            defaultWhenNoScope = false;
        }
        
        if (this.viewChargement.getIsScoped()) {
            initializeChargement();
            defaultWhenNoScope = false;
        }


        if (this.viewNormage.getIsScoped()) {
            initializeNormage();
            defaultWhenNoScope = false;
        }

        if (this.viewControle.getIsScoped()) {
            initializeControle();
            defaultWhenNoScope = false;
        }

        if (this.viewFiltrage.getIsScoped()) {
            initializeFiltrage();
            defaultWhenNoScope = false;
        }
        if (this.viewMapping.getIsScoped()) {
            initializeMapping();
            defaultWhenNoScope = false;
        }
        if (this.viewJeuxDeReglesCopie.getIsScoped()) {
            initializeJeuxDeReglesCopie();
            defaultWhenNoScope = false;
        }
        if (scope != null && scope.contains("normTree")) {
            generateNormTree();
            defaultWhenNoScope = false;
        }

        if (defaultWhenNoScope) {
            System.out.println("default");
            initializeNorme();
            this.viewNorme.setIsActive(true);
            this.viewNorme.setIsScoped(true);
        }

        return "success";

    }

    // private SessionMap session;
    // visual des Normes
    public void initializeNorme() {
        System.out.println("/* initializeNorme */");
        HashMap<String, String> defaultInputFields = new HashMap<String, String>();
        // System.out.println(">>>>>>>>>>>>>>>>>>>>>"+ArcConstantVObjectGetter.columnRender.get(this.viewNorme.getSessionName()));
        // this.viewNorme.setColumnRendering(ArcConstantVObjectGetter.columnRender.get(this.viewNorme.getSessionName()));
        this.viewNorme.initialize("select id_famille, id_norme, periodicite, def_norme, def_validite, etat from arc.ihm_norme order by id_norme",
                "arc.ihm_norme", defaultInputFields);
    }

    @Action(value = "/selectNorme")
    public String selectNorme() {
//        Iterator itr=InseeConfig.getConfig().getKeys();
//        while (itr.hasNext())
//        {
//        	String element = (String) itr.next();
//        	System.out.println(element+ " : "+InseeConfig.getConfig().getString(element));
//        }
        return sessionSyncronize();
    }

    @Action(value = "/addNorme")
    public String addNorme() {
        this.viewNorme.insert();
        return sessionSyncronize();
    }

    /**
     * Suppression de Norme Regle de gestion : impossible de supprimer une norme active
     *
     * @return
     */
    @Action(value = "/deleteNorme")
    public String deleteNorme() {

        HashMap<String, ArrayList<String>> selection = this.viewNorme.mapContentSelected();
        if (!selection.isEmpty()) {
            String etat = selection.get("etat").get(0);
            LoggerDispatcher.info("L'état à supprimer : " + etat, LOGGER);
            // test où on ne peut pas supprimer les normes active (code 1)
            if ("1".equals(etat)) {
                this.viewNorme.setMessage("Attention, il est interdit de supprimer une norme active");
            } else {
                this.viewNorme.delete();
            }
        } else {
            this.viewJeuxDeRegles.setMessage("Vous n'avez rien sélectionné");
        }
        return sessionSyncronize();
    }

    @Action(value = "/updateNorme")
    public String updateNorme() {
        this.viewNorme.update();
        return sessionSyncronize();
    }

    @Action(value = "/sortNorme")
    public String sortNorme() {
        this.viewNorme.sort();
        return sessionSyncronize();
    }

    // visual des Calendriers
    public void initializeCalendrier() {
        System.out.println("/* initializeCalendrier */");
        HashMap<String, ArrayList<String>> selection = this.viewNorme.mapContentSelected();
        if (!selection.isEmpty()) {
            HashMap<String, String> type = this.viewNorme.mapHeadersType();
            // requete de la vue
            StringBuilder requete = new StringBuilder();
            requete.append("select id_norme, periodicite, validite_inf, validite_sup, etat from arc.ihm_calendrier");
            requete.append(" where id_norme" + ManipString.sqlEqual(selection.get("id_norme").get(0), type.get("id_norme")));
            requete.append(" and periodicite" + ManipString.sqlEqual(selection.get("periodicite").get(0), type.get("periodicite")));
            
            // construction des valeurs par défaut pour les ajouts
            HashMap<String, String> defaultInputFields = new HashMap<String, String>();
            defaultInputFields.put("id_norme", selection.get("id_norme").get(0));
            defaultInputFields.put("periodicite", selection.get("periodicite").get(0));
            this.viewCalendrier.setAfterInsertQuery("select arc.fn_check_calendrier(); ");
            this.viewCalendrier.setAfterUpdateQuery("select arc.fn_check_calendrier(); ");
            // this.viewCalendrier.setColumnRendering(ArcConstantVObjectGetter.columnRender.get(this.viewCalendrier.getSessionName()));
            this.viewCalendrier.initialize(requete.toString(), "arc.ihm_calendrier", defaultInputFields);
        } else {
            this.viewCalendrier.destroy();
        }
    }

    @Action(value = "/selectCalendrier")
    public String selectCalendrier() {
        return sessionSyncronize();
    }

    @Action(value = "/addCalendrier")
    public String addCalendrier() {
        this.viewCalendrier.insert();
        return sessionSyncronize();
    }

    /**
     * Suppression de Calendrier Regle de gestion : impossible de supprimer un calendrier actif
     *
     * @return
     */
    @Action(value = "/deleteCalendrier")
    public String deleteCalendrier() {

        HashMap<String, ArrayList<String>> selection = this.viewCalendrier.mapContentSelected();
        if (!selection.isEmpty()) {
            String etat = selection.get("etat").get(0);
            LoggerDispatcher.info("L'état à supprimer : " + etat, LOGGER);
            // test où on ne peut pas supprimer les calendriers actif (code 1)
            if ("1".equals(etat)) {
                this.viewCalendrier.setMessage("Attention, il est interdit de supprimer un calendrier actif");
            } else {
                this.viewCalendrier.delete();
            }
        } else {
            this.viewJeuxDeRegles.setMessage("Vous n'avez rien sélectionné");
        }
        return sessionSyncronize();
    }

    @Action(value = "/updateCalendrier")
    public String updateCalendrier() {
        this.viewCalendrier.update();
        return sessionSyncronize();
    }

    @Action(value = "/sortCalendrier")
    public String sortCalendrier() {
        this.viewCalendrier.sort();
        return sessionSyncronize();
    }

    // visual des Jeux de Regles
    public void initializeJeuxDeRegles() {
        System.out.println("/* initializeJeuxDeRegles */");
        HashMap<String, ArrayList<String>> selection = this.viewCalendrier.mapContentSelected();
        if (!selection.isEmpty()) {
            HashMap<String, String> type = this.viewCalendrier.mapHeadersType();
            StringBuilder requete = new StringBuilder();
            requete.append("select id_norme, periodicite, validite_inf, validite_sup, version, etat from arc.ihm_jeuderegle ");
            requete.append(" where id_norme" + ManipString.sqlEqual(selection.get("id_norme").get(0), type.get("id_norme")));
            requete.append(" and periodicite" + ManipString.sqlEqual(selection.get("periodicite").get(0), type.get("periodicite")));
            requete.append(" and validite_inf" + ManipString.sqlEqual(selection.get("validite_inf").get(0), type.get("validite_inf")));
            requete.append(" and validite_sup" + ManipString.sqlEqual(selection.get("validite_sup").get(0), type.get("validite_sup")));
            HashMap<String, String> defaultInputFields = new HashMap<String, String>();
            defaultInputFields.put("id_norme", selection.get("id_norme").get(0));
            defaultInputFields.put("periodicite", selection.get("periodicite").get(0));
            defaultInputFields.put("validite_inf", selection.get("validite_inf").get(0));
            defaultInputFields.put("validite_sup", selection.get("validite_sup").get(0));
            this.viewJeuxDeRegles.setAfterInsertQuery("select arc.fn_check_jeuderegle(); ");
            this.viewJeuxDeRegles.setAfterUpdateQuery("select arc.fn_check_jeuderegle(); ");
            // this.viewJeuxDeRegles.setColumnRendering(ArcConstantVObjectGetter.columnRender.get(this.viewJeuxDeRegles.getSessionName()));
            this.viewJeuxDeRegles.initialize(requete.toString(), "arc.ihm_jeuderegle", defaultInputFields);
        } else {
            this.viewJeuxDeRegles.destroy();
        }
    }

    @Action(value = "/selectJeuxDeRegles")
    public String selectJeuxDeRegles() {
        return sessionSyncronize();
    }

    @Action(value = "/addJeuxDeRegles")
    public String addJeuxDeRegles() {
    	
    	HashMap<String, ArrayList<String>> selection = this.viewJeuxDeRegles.mapInputFields();
    	if (!selection.isEmpty()) {
    	String etat = selection.get("etat").get(0);
    	   if ("arc.prod".equals(etat)) {
               this.viewJeuxDeRegles.setMessage("Attention, il est interdit d'ajouter un jeu de règle dans l'état PRODUCTION");
           } else {
    		   this.viewJeuxDeRegles.insert();
           }
    	}
        
        return sessionSyncronize();
    }

    /**
     * Suppression d'un jeu de règle Règle de gestion : impossible de supprimer un jeu de règle dans l'état PRODUCTION
     *
     * @return
     */
    @Action(value = "/deleteJeuxDeRegles")
    public String deleteJeuxDeRegles() {

        HashMap<String, ArrayList<String>> selection = this.viewJeuxDeRegles.mapContentSelected();
        if (!selection.isEmpty()) {
            String etat = selection.get("etat").get(0);
            LoggerDispatcher.info("L'état à supprimer : " + etat, LOGGER);
            // test où on ne peut pas supprimer les jeux de règles en production
            if ("arc.prod".equals(etat)) {
                this.viewJeuxDeRegles.setMessage("Attention, il est interdit de supprimer un jeu de règle dans l'état PRODUCTION");
            } else {
                this.viewJeuxDeRegles.delete();
            }
        } else {
            this.viewJeuxDeRegles.setMessage("Vous n'avez rien sélectionné");
        }

        return sessionSyncronize();
    }

    @Action(value = "/updateJeuxDeRegles")
    public String updateJeuxDeRegles() {
        HashMap<String, ArrayList<String>> selection = this.viewJeuxDeRegles.mapContentAfterUpdate();
        // si on updatge vers la production, créer le fichier dummy qui va déclencher le batch d'initialisation
        // on les crée dans tous les environnements et tous les entrepots
        // (ca evite les erreurs et car ca ne spécialise aucun environnement dans un role à priori)
        if (!selection.isEmpty()) {
        	for (int i=0;i<selection.get("etat").size();i++)
        	{
                String etat = selection.get("etat").get(i);
        		if ("arc.prod".equals(etat)) {
        			miseEnProdJeuxDeRegles();
        		}
        	}
        
            this.viewJeuxDeRegles.update();
        	
        }
        
        return sessionSyncronize();
    }

    @Action(value = "/sortJeuxDeRegles")
    public String sortJeuxDeRegles() {
        this.viewJeuxDeRegles.sort();
        return sessionSyncronize();
    }

    
    public void miseEnProdJeuxDeRegles() {
        // String repertoire=ServletActionContext.getServletContext().getRealPath("/");
		 DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd:HH");
		 Date dNow = new Date();
    	
		 System.out.println("miseEnProdJeuxDeRegles : "+dateFormat.format(dNow));
		 
		try {
	        UtilitaireDao.get("arc").executeRequest(null, "update arc.pilotage_batch set last_init='"+dateFormat.format(dNow)+"', operation=case when operation='R' then 'O' else operation end;");
	        viewJeuxDeRegles.setMessage("Mise en production des règles enregistrée");

        } catch (SQLException e) {
	        // TODO Auto-generated catch block

        	viewJeuxDeRegles.setMessage("Erreur dans la demande de mise en production des règles");
        }
    }
    
    
    
    @Action(value = "/downloadJeuxDeRegles")
    public String downloadJeuxDeRegles() {
        HashMap<String, ArrayList<String>> selection = this.viewJeuxDeRegles.mapContentSelected();
        if (!selection.isEmpty()) {
            StringBuilder requeteRegleChargement = new StringBuilder();
            requeteRegleChargement.append(recupRegle("arc.ihm_" + TraitementTableParametre.CHARGEMENT_REGLE));
            StringBuilder requeteRegleNormage = new StringBuilder();
            requeteRegleNormage.append(recupRegle("arc.ihm_" + TraitementTableParametre.NORMAGE_REGLE));
            StringBuilder requeteRegleControle = new StringBuilder();
            requeteRegleControle.append(recupRegle("arc.ihm_" + TraitementTableParametre.CONTROLE_REGLE));
            StringBuilder requeteRegleMapping = new StringBuilder();
            requeteRegleMapping.append(recupRegle("arc.ihm_" + TraitementTableParametre.MAPPING_REGLE));
            StringBuilder requeteRegleFiltrage = new StringBuilder();
            requeteRegleFiltrage.append(recupRegle("arc.ihm_" + TraitementTableParametre.FILTRAGE_REGLE));
            // TODO idem pour les règle de filtrage
            ArrayList<String> fileNames = new ArrayList<>();
            fileNames.add("Regles_chargement");
            fileNames.add("Regles_normage");
            fileNames.add("Regles_controle");
            fileNames.add("Regles_mapping");
            fileNames.add("Regles_filtrage");
            this.viewJeuxDeRegles.download(fileNames//
                                	   , requeteRegleChargement.toString()//
                                	   , requeteRegleNormage.toString()//
                                	   , requeteRegleControle.toString()//
                                	   , requeteRegleMapping.toString()//
                                	   , requeteRegleFiltrage.toString());
            return "none";
        } else {
            this.viewJeuxDeRegles.setMessage("Vous n'avez rien sélectionné");
            return sessionSyncronize();
        }

    }

    /**
     * Méthode pour ecrire le SQL qui permet de récupérer les règles liées à un jeu de règle on suppose que selection n'est pas vide
     *
     * @param table
     * @return
     */
    private String recupRegle(String table) {
        StringBuilder requete = new StringBuilder();
        HashMap<String, ArrayList<String>> selection = this.viewJeuxDeRegles.mapContentSelected();
        HashMap<String, String> type = this.viewJeuxDeRegles.mapHeadersType();
        requete.append("select * from " + table + " ");
        requete.append(" where id_norme" + ManipString.sqlEqual(selection.get("id_norme").get(0), type.get("id_norme")));
        requete.append(" and periodicite" + ManipString.sqlEqual(selection.get("periodicite").get(0), type.get("periodicite")));
        requete.append(" and validite_inf" + ManipString.sqlEqual(selection.get("validite_inf").get(0), type.get("validite_inf")));
        requete.append(" and validite_sup" + ManipString.sqlEqual(selection.get("validite_sup").get(0), type.get("validite_sup")));
        requete.append(" and version" + ManipString.sqlEqual(selection.get("version").get(0), type.get("version")));
        LoggerDispatcher.info("Ma requete de téléchargement : " + requete.toString(), LOGGER);
        return requete.toString();
    }

    // private SessionMap session;
    
//     visual des Chargements
  public void initializeChargement() {
      System.out.println("/* initializeChargement */");
      HashMap<String, ArrayList<String>> selection = this.viewJeuxDeRegles.mapContentSelected();
      if (!selection.isEmpty()) {
          HashMap<String, String> type = this.viewJeuxDeRegles.mapHeadersType();
          StringBuilder requete = new StringBuilder();
          requete.append("select id_norme,periodicite,validite_inf,validite_sup,version,id_regle,type_fichier, delimiter, format, commentaire from arc.ihm_chargement_regle");
          requete.append(" where id_norme" + ManipString.sqlEqual(selection.get("id_norme").get(0), type.get("id_norme")));
          requete.append(" and periodicite" + ManipString.sqlEqual(selection.get("periodicite").get(0), type.get("periodicite")));
          requete.append(" and validite_inf" + ManipString.sqlEqual(selection.get("validite_inf").get(0), type.get("validite_inf")));
          requete.append(" and validite_sup" + ManipString.sqlEqual(selection.get("validite_sup").get(0), type.get("validite_sup")));
          requete.append(" and version" + ManipString.sqlEqual(selection.get("version").get(0), type.get("version")));
          HashMap<String, String> defaultInputFields = new HashMap<String, String>();
          defaultInputFields.put("id_norme", selection.get("id_norme").get(0));
          defaultInputFields.put("periodicite", selection.get("periodicite").get(0));
          defaultInputFields.put("validite_inf", selection.get("validite_inf").get(0));
          defaultInputFields.put("validite_sup", selection.get("validite_sup").get(0));
          defaultInputFields.put("version", selection.get("version").get(0));
          // this.viewNormage.setColumnRendering(ArcConstantVObjectGetter.columnRender.get(this.viewNormage.getSessionName()));
          this.viewChargement.initialize(requete.toString(), "arc.ihm_chargement_regle", defaultInputFields);
      } else {
          this.viewChargement.destroy();
      }
  }

  @Action(value = "/selectChargement")
  public String selectChargement() {
      System.out.println("selectChargement " + this.scope);
      return sessionSyncronize();
  }

  @Action(value = "/addChargement")
  public String addChargement() {
      this.viewChargement.insert();
      return sessionSyncronize();
  }

  /**
   * Suppression de Chargement Regle de gestion : impossible de supprimer une Chargement active
   *
   * @return
   */
  @Action(value = "/deleteChargement")
  public String deleteChargement() {
      this.viewChargement.delete();
      return sessionSyncronize();
  }

  @Action(value = "/updateChargement")
  public String updateChargement() {
      this.viewChargement.update();
      return sessionSyncronize();
  }

  @Action(value = "/sortChargement")
  public String sortChargement() {
      this.viewChargement.sort();
      return sessionSyncronize();
  }
  
  @Action(value = "/importChargement")
  public String importChargement() throws IOException {
     
	uploadFileRule(getViewChargement());
	
      return sessionSyncronize();
  }
    
    // visual des Normages
    public void initializeNormage() {
        System.out.println("/* initializeNormage */");
        HashMap<String, ArrayList<String>> selection = this.viewJeuxDeRegles.mapContentSelected();
        if (!selection.isEmpty()) {
            HashMap<String, String> type = this.viewJeuxDeRegles.mapHeadersType();
            StringBuilder requete = new StringBuilder();
            requete.append("select id_norme,periodicite,validite_inf,validite_sup,version,id_regle,id_classe,rubrique,rubrique_nmcl,commentaire from arc.ihm_normage_regle");
            requete.append(" where id_norme" + ManipString.sqlEqual(selection.get("id_norme").get(0), type.get("id_norme")));
            requete.append(" and periodicite" + ManipString.sqlEqual(selection.get("periodicite").get(0), type.get("periodicite")));
            requete.append(" and validite_inf" + ManipString.sqlEqual(selection.get("validite_inf").get(0), type.get("validite_inf")));
            requete.append(" and validite_sup" + ManipString.sqlEqual(selection.get("validite_sup").get(0), type.get("validite_sup")));
            requete.append(" and version" + ManipString.sqlEqual(selection.get("version").get(0), type.get("version")));
            HashMap<String, String> defaultInputFields = new HashMap<String, String>();
            defaultInputFields.put("id_norme", selection.get("id_norme").get(0));
            defaultInputFields.put("periodicite", selection.get("periodicite").get(0));
            defaultInputFields.put("validite_inf", selection.get("validite_inf").get(0));
            defaultInputFields.put("validite_sup", selection.get("validite_sup").get(0));
            defaultInputFields.put("version", selection.get("version").get(0));
            // this.viewNormage.setColumnRendering(ArcConstantVObjectGetter.columnRender.get(this.viewNormage.getSessionName()));
            this.viewNormage.initialize(requete.toString(), "arc.ihm_normage_regle", defaultInputFields);
        } else {
            this.viewNormage.destroy();
        }
    }
    
    @Action(value = "/importNormage")
    public String importNormage() throws IOException {
       
	uploadFileRule(getViewNormage());
	
        return sessionSyncronize();
    }

    @Action(value = "/selectNormage")
    public String selectNormage() {
        System.out.println("selectNormage " + this.scope);
        return sessionSyncronize();
    }

    @Action(value = "/addNormage")
    public String addNormage() {
        this.viewNormage.insert();
        return sessionSyncronize();
    }

    /**
     * Suppression de Normage Regle de gestion : impossible de supprimer une Normage active
     *
     * @return
     */
    @Action(value = "/deleteNormage")
    public String deleteNormage() {
        this.viewNormage.delete();
        return sessionSyncronize();
    }

    @Action(value = "/updateNormage")
    public String updateNormage() {
        this.viewNormage.update();
        return sessionSyncronize();
    }

    @Action(value = "/sortNormage")
    public String sortNormage() {
        this.viewNormage.sort();
        return sessionSyncronize();
    }

    // visual des Contrôles
    public void initializeControle() {
        System.out.println("/* initializeControle */");
        HashMap<String, ArrayList<String>> selection = this.viewJeuxDeRegles.mapContentSelected();
        if (!selection.isEmpty()) {
            HashMap<String, String> type = this.viewJeuxDeRegles.mapHeadersType();
            StringBuilder requete = new StringBuilder();
            requete.append("select id_norme,periodicite,validite_inf,validite_sup,version,id_regle,id_classe,rubrique_pere,rubrique_fils,borne_inf,borne_sup,condition,pre_action,xsd_ordre,xsd_label_fils,xsd_role,commentaire from arc.ihm_controle_regle");
            requete.append(" where id_norme" + ManipString.sqlEqual(selection.get("id_norme").get(0), type.get("id_norme")));
            requete.append(" and periodicite" + ManipString.sqlEqual(selection.get("periodicite").get(0), type.get("periodicite")));
            requete.append(" and validite_inf" + ManipString.sqlEqual(selection.get("validite_inf").get(0), type.get("validite_inf")));
            requete.append(" and validite_sup" + ManipString.sqlEqual(selection.get("validite_sup").get(0), type.get("validite_sup")));
            requete.append(" and version" + ManipString.sqlEqual(selection.get("version").get(0), type.get("version")));
            HashMap<String, String> defaultInputFields = new HashMap<String, String>();
            defaultInputFields.put("id_norme", selection.get("id_norme").get(0));
            defaultInputFields.put("periodicite", selection.get("periodicite").get(0));
            defaultInputFields.put("validite_inf", selection.get("validite_inf").get(0));
            defaultInputFields.put("validite_sup", selection.get("validite_sup").get(0));
            defaultInputFields.put("version", selection.get("version").get(0));
            // this.viewControle.setColumnRendering(ArcConstantVObjectGetter.columnRender.get(this.viewControle.getSessionName()));
            this.viewControle.initialize(requete.toString(), "arc.ihm_controle_regle", defaultInputFields);
        } else {
            this.viewControle.destroy();
        }
    }

    @Action(value = "/selectControle")
    public String selectControle() {
        return sessionSyncronize();
    }

    @Action(value = "/addControle")
    public String addControle() {
        // viewControle.insert();
        LoggerDispatcher.info("Ajout de la règle : " + this.viewControle.getInputFields().toString(), LOGGER);
        boolean isAjouter = true;
        HashMap<String, ArrayList<String>> selection = this.viewJeuxDeRegles.mapContentSelected();
        /*
         * Fabrication d'un JeuDeRegle pour conserver les informations sur norme et calendrier
         */
        JeuDeRegle jdr = new JeuDeRegle();
        jdr.setIdNorme(selection.get("id_norme").get(0));
        jdr.setPeriodicite(selection.get("periodicite").get(0));
        jdr.setValiditeInfString(selection.get("validite_inf").get(0), "yyyy-MM-dd");
        jdr.setValiditeSupString(selection.get("validite_sup").get(0), "yyyy-MM-dd");
        jdr.setVersion(selection.get("version").get(0));
        /* Fabrication de la règle à ajouter */
        ArrayList<RegleControleEntity> listRegle = new ArrayList<>();
        RegleControleEntity reg = new RegleControleEntity(this.viewControle.mapInputFields());
        listRegle.add(reg);
        try {
            // Fabrication de la table temporaire pour tester l'insertion
        	
            UtilitaireDao.get("arc").executeRequest(null, createTableTempTest(TraitementTableParametre.CONTROLE_REGLE.toString()));
            // Insertion de cette règle dans la table temporaire
            isAjouter = this.service.ajouterRegles(jdr, "arc", listRegle);
        } catch (Exception e) {
            this.viewControle.setMessage(e.toString());
            e.printStackTrace();
            isAjouter = false;
        }
        // bilan de l'import
        if (isAjouter) {
            this.viewControle.insert();
            LoggerDispatcher.info("Insertion de la nouvelle règle", LOGGER);
            this.viewControle.setMessage("Insertion de la nouvelle règle");
        }
        return sessionSyncronize();
    }

    
    /**
     * Création d'une table pour tester les modifications d'une table de règle attention aux contraintes !
     *
     * @param env
     * @param table
     * @return
     */
    private static String createTableTempTest(String tableACopier) {
    	
    	String nomTableTest="arc.test_ihm_"+tableACopier;
    	
        StringBuilder create = new StringBuilder();
        
        create.append("DROP TABLE IF EXISTS " + nomTableTest + "; ");
        create.append("CREATE ");
       
        create.append(" TABLE " + nomTableTest + " AS SELECT * FROM arc.ihm_" + tableACopier + ";");
        create.append("ALTER TABLE " +nomTableTest + " ADD PRIMARY KEY (id_norme, periodicite, validite_inf, validite_sup, version, id_regle);");

        create.append("CREATE CONSTRAINT TRIGGER doublon ");
        create.append("AFTER INSERT OR UPDATE OF rubrique_pere, rubrique_fils ");
        create.append("ON " + nomTableTest + " DEFERRABLE INITIALLY DEFERRED ");
        create.append("FOR EACH ROW ");
        create.append("EXECUTE PROCEDURE arc.verif_doublon(); ");

        create.append("CREATE TRIGGER tg_insert_controle ");
        create.append("before INSERT ON " + nomTableTest + " ");
        create.append("FOR EACH ROW ");
        create.append("EXECUTE PROCEDURE arc.insert_controle(); ");

        // LoggerDispatcher.info("Requete de création de la table de test temp : " + create.toString(), LOGGER);
        return create.toString();
    }
    
    
    /**
     * Création d'une table pour tester les modifications d'une table de règle attention aux contraintes !
     *
     * @param env
     * @param table
     * @return
     */
//    private static String createTableTempTest(String env, String table) {
//
//        StringBuilder create = new StringBuilder();
//        String environnement = ApiService.dbEnv(env);
//
//
//
//
//
//
//        create.append("DROP TABLE IF EXISTS " + ApiService.dbEnv(env) + table + "; ");
//        create.append("CREATE ");
//        if (!env.contains(".")) {
//            create.append(" TEMPORARY ");
//        }
//
//        create.append(" TABLE " + ApiService.dbEnv(env) + table + " AS SELECT * FROM arc.ihm_" + table + ";");
//        create.append("ALTER TABLE " + ApiService.dbEnv(env) + table + " ADD PRIMARY KEY (id_norme, periodicite, validite_inf, validite_sup, version, id_regle);");
//
//        create.append("CREATE CONSTRAINT TRIGGER doublon ");
//        create.append("AFTER INSERT OR UPDATE OF rubrique_pere, rubrique_fils ");
//        create.append("ON " + ApiService.dbEnv(env) + table + " DEFERRABLE INITIALLY DEFERRED ");
//        create.append("FOR EACH ROW ");
//        create.append("EXECUTE PROCEDURE arc.verif_doublon(); ");
//
//        create.append("CREATE TRIGGER tg_insert_controle ");
//        create.append("before INSERT ON " + ApiService.dbEnv(env) + table + " ");
//        create.append("FOR EACH ROW ");
//        create.append("EXECUTE PROCEDURE arc.insert_controle(); ");
//
//        // LoggerDispatcher.info("Requete de création de la table de test temp : " + create.toString(), LOGGER);
//        return create.toString();
//    }

    @Action(value = "/deleteControle")
    public String deleteControle() {
        // viewControle.delete();
        HashMap<String, ArrayList<String>> selection = this.viewJeuxDeRegles.mapContentSelected();
        /*
         * Fabrication d'un JeuDeRegle pour conserver les informations sur norme et calendrier
         */
//        JeuDeRegle jdr = new JeuDeRegle();
//        jdr.setIdNorme(selection.get("id_norme").get(0));
//        jdr.setPeriodicite(selection.get("periodicite").get(0));
//        jdr.setValiditeInfString(selection.get("validite_inf").get(0), "yyyy-MM-dd");
//        jdr.setValiditeSupString(selection.get("validite_sup").get(0), "yyyy-MM-dd");
//        jdr.setVersion(selection.get("version").get(0));
        try {
            // Fabrication de la table temporaire pour tester l'insertion
//
//            UtilitaireDao.get("arc").executeRequest(null, createTableTempTest(TraitementTableParametre.CONTROLE_REGLE.toString()));
//
//            // suppression des données de la table temporaire
//            this.viewControle.delete("arc.test_ihm_" + TraitementTableParametre.CONTROLE_REGLE.toString());
//
//            // Test que le nouveau jeu de règle est cohérent
//            this.service.executeABlanc(jdr, "arc", TraitementPhase.CONTROLE.toString());
//
            // si oui => delete effectif
            this.viewControle.delete();
            
        } catch (Exception e) {
            // si non => message d'erreur
            this.viewControle.setMessage("La suppression d'une des règles rend le jeu de règles incohérents : " + e.getMessage().toString());
            e.printStackTrace();
        }
        return sessionSyncronize();
    }

    @Action(value = "/updateControle")
    public String updateControle() {
        JeuDeRegle jdr = fetchJeuDeRegle();
        // mise en liste de règles des anciennes données et des nouvelles
        // données
        // LoggerDispatcher.info(logger,"Mes anciennes données : " + viewControle.listContentBeforeUpdate().toString());
        // ArrayList<RegleControle> listRegleAnc = new ArrayList<>();
        // for (int i = 0; i < viewControle.listContentBeforeUpdate().size(); i++) {
        // RegleControle reg = new RegleControle(viewControle.mapContentBeforeUpdate(i));
        // listRegleAnc.add(reg);
        // }
        LoggerDispatcher.info("Mes nouvelles données : " + this.viewControle.listContentAfterUpdate().toString(), LOGGER);
        ArrayList<RegleControleEntity> listRegleNouv = new ArrayList<>();
        for (int i = 0; i < this.viewControle.listContentAfterUpdate().size(); i++) {
            RegleControleEntity reg = new RegleControleEntity(this.viewControle.mapContentAfterUpdate(i));
            listRegleNouv.add(reg);
        }
        try {
            // Fabrication de la table temporaire pour tester la modifcation
            UtilitaireDao.get("arc").executeRequest(null, createTableTempTest(TraitementTableParametre.CONTROLE_REGLE.toString()));
            // suppression des lignes modifiées
            this.viewControle.deleteForUpdate("arc.test_ihm_" + TraitementTableParametre.CONTROLE_REGLE.toString());
            // test du nouveau paquet en passant par la méthode ajouterRegles()
            // afin de lancer la batterie de test (borne_inf<borne_sup etc.)
            this.service.ajouterRegles(jdr, "arc", listRegleNouv);
            this.viewControle.setMessage("Règles modifiées !");
            this.viewControle.update();
        } catch (Exception e) {
            this.viewControle.setMessage("Les modification rendent l'ensemble de règle incohérent : " + e.toString());
        }
        return sessionSyncronize();
    }

    private JeuDeRegle fetchJeuDeRegle() {
        HashMap<String, ArrayList<String>> selection = this.viewJeuxDeRegles.mapContentSelected();
        /*
         * Fabrication d'un JeuDeRegle pour conserver les informations sur norme et calendrier
         */
        JeuDeRegle jdr = new JeuDeRegle();
        jdr.setIdNorme(selection.get("id_norme").get(0));
        jdr.setPeriodicite(selection.get("periodicite").get(0));
        jdr.setValiditeInfString(selection.get("validite_inf").get(0), "yyyy-MM-dd");
        jdr.setValiditeSupString(selection.get("validite_sup").get(0), "yyyy-MM-dd");
        jdr.setVersion(selection.get("version").get(0));
        jdr.setEtat(selection.get("etat").get(0));
        return jdr;
    }

    @Action(value = "/sortControle")
    public String sortControle() {
        this.viewControle.sort();
        return sessionSyncronize();
    }

    @Action(value = "/importControle")
    public String importControle() throws IOException {
        LoggerDispatcher.info("Je tiens mon action ! ! !", LOGGER);
        System.out.println(this.fileUpload.getPath());
        String fichierRegle = ManipString.readFileAsString(this.fileUpload.getPath());
        LoggerDispatcher.info("J'ai un fichier de longueur : " + fichierRegle.length(), LOGGER);
        boolean isAjouter = true;
        HashMap<String, ArrayList<String>> selection = this.viewJeuxDeRegles.mapContentSelected();
        /*
         * Fabrication d'un JeuDeRegle pour conserver les informations sur norme et calendrier
         */
        JeuDeRegle jdr = new JeuDeRegle();
        jdr.setIdNorme(selection.get("id_norme").get(0));
        jdr.setPeriodicite(selection.get("periodicite").get(0));
        jdr.setValiditeInfString(selection.get("validite_inf").get(0), "yyyy-MM-dd");
        jdr.setValiditeSupString(selection.get("validite_sup").get(0), "yyyy-MM-dd");
        jdr.setVersion(selection.get("version").get(0));
        ArrayList<RegleControleEntity> listRegle = new ArrayList<>();
        listRegle = this.service.miseEnRegleC(ControleRegleService.nomTableRegleControle("arc.test_ihm", true), fichierRegle);
        try {
            // Fabrication de la table temporaire pour tester l'insertion
            UtilitaireDao.get("arc").executeRequest(null, createTableTempTest(TraitementTableParametre.CONTROLE_REGLE.toString()));
            // Insertion de cette règle dans la table temporaire
            isAjouter = this.service.ajouterRegles(jdr, "arc", listRegle);
        } catch (Exception e) {
            this.viewControle.setMessage(e.toString());
            e.printStackTrace();
            isAjouter = false;
        }
        // bilan de l'import
        if (isAjouter) {
            try {
                // l'ajout réel en base
                this.service.ajouterReglesValidees(jdr, "arc", listRegle);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            LoggerDispatcher.info("Insertion des nouvelles règles", LOGGER);
            this.viewControle.setMessage("Insertion des nouvelles règles");
        }
        return sessionSyncronize();
    }

    @Action(value = "/xsdControle")
    public String xsdControle() throws Exception {
        HashMap<String, ArrayList<String>> selection = this.viewJeuxDeRegles.mapContentSelected();
        if (!selection.isEmpty()) {
        	
        	XsdExtractionService xsdExtractionService = new XsdExtractionService();
            String xsd = xsdExtractionService.get(null, new JeuDeRegle(
            		selection.get("id_norme").get(0)
            		, selection.get("periodicite").get(0)
            		, selection.get("validite_inf").get(0)
            		, selection.get("validite_sup").get(0)
            		, selection.get("version").get(0)) );
            
            ArrayList<String> z=new ArrayList<String>();
            z.add(selection.get("id_norme").get(0)+"_"+selection.get("version").get(0)+".xsd");

            this.viewControle.downloadValues(z, xsd);
            
            System.out.println(xsd);
            
        	}
        return "none";

    }
    
    
    
    public String viderTableRegle(String table) {
        LoggerDispatcher.info("suppression des règles de ce jeu de règle", LOGGER);
        HashMap<String, ArrayList<String>> selection = this.viewJeuxDeRegles.mapContentSelected();
        HashMap<String, String> type = this.viewJeuxDeRegles.mapHeadersType();
        StringBuilder requete = new StringBuilder();
        requete.append("DELETE FROM arc.ihm_" + table);
        requete.append(" WHERE id_norme" + ManipString.sqlEqual(selection.get("id_norme").get(0), type.get("id_norme")));
        requete.append(" AND periodicite" + ManipString.sqlEqual(selection.get("periodicite").get(0), type.get("periodicite")));
        requete.append(" AND validite_inf" + ManipString.sqlEqual(selection.get("validite_inf").get(0), type.get("validite_inf")));
        requete.append(" AND validite_sup" + ManipString.sqlEqual(selection.get("validite_sup").get(0), type.get("validite_sup")));
        requete.append(" AND version" + ManipString.sqlEqual(selection.get("version").get(0), type.get("version")));
        requete.append(" ;");
        // LoggerDispatcher.info("La requete de suppression des règles pour ce jeu de règle particulier : \n" + requete, LOGGER);
        try {
            UtilitaireDao.get("arc").executeRequest(null, requete);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return sessionSyncronize();
    }
    
    @Action(value = "/viderChargement")
    public String viderChargement() {
        return this.viderTableRegle(TraitementTableParametre.CHARGEMENT_REGLE.toString());
    }

    @Action(value = "/viderNormage")
    public String viderNormage() {
        return this.viderTableRegle(TraitementTableParametre.NORMAGE_REGLE.toString());
    }
    
    @Action(value = "/viderControle")
    public String viderControle() {
        return this.viderTableRegle(TraitementTableParametre.CONTROLE_REGLE.toString());
    }
    
    @Action(value = "/viderFiltrage")
    public String viderFiltrage() {
        return this.viderTableRegle(TraitementTableParametre.FILTRAGE_REGLE.toString());
    }

    @Action(value = "/viderMapping")
    public String viderMapping() {
        return this.viderTableRegle(TraitementTableParametre.MAPPING_REGLE.toString());
    }

    // visual du mapping
    public void initializeMapping() {
        try {
            System.out.println("/* initializeMapping */");
            HashMap<String, ArrayList<String>> selection = this.viewJeuxDeRegles.mapContentSelected();
            if (!selection.isEmpty()) {
                HashMap<String, String> type = this.viewJeuxDeRegles.mapHeadersType();
                StringBuilder requete = new StringBuilder(
                        "SELECT mapping.id_regle, mapping.id_norme, mapping.validite_inf, mapping.validite_sup, mapping.version, mapping.periodicite, mapping.variable_sortie, mapping.expr_regle_col, mapping.commentaire, variables.type_variable_metier type_sortie, variables.nom_table_metier nom_table_metier\n");
                requete.append("  FROM arc.ihm_mapping_regle mapping INNER JOIN arc.ihm_jeuderegle jdr\n");
                requete.append("  ON mapping.id_norme     = jdr.id_norme     AND mapping.periodicite           = jdr.periodicite AND mapping.validite_inf = jdr.validite_inf AND mapping.validite_sup = jdr.validite_sup AND mapping.version = jdr.version\n");
                requete.append("  INNER JOIN arc.ihm_norme norme\n");
                requete.append("  ON norme.id_norme       = jdr.id_norme AND norme.periodicite   = jdr.periodicite\n");
                requete.append("  INNER JOIN (SELECT id_famille, nom_variable_metier, type_variable_metier, string_agg(nom_table_metier,',') as nom_table_metier FROM arc.ihm_mod_variable_metier group by id_famille, nom_variable_metier, type_variable_metier) variables\n");
                requete.append("  ON variables.id_famille = norme.id_famille AND variables.nom_variable_metier = mapping.variable_sortie\n");
                requete.append("  WHERE mapping.id_norme" + ManipString.sqlEqual(selection.get("id_norme").get(0), type.get("id_norme")));
                requete.append("  AND mapping.periodicite" + ManipString.sqlEqual(selection.get("periodicite").get(0), type.get("periodicite")));
                requete.append("  AND mapping.validite_inf" + ManipString.sqlEqual(selection.get("validite_inf").get(0), type.get("validite_inf")));
                requete.append("  AND mapping.validite_sup" + ManipString.sqlEqual(selection.get("validite_sup").get(0), type.get("validite_sup")));
                requete.append("  AND mapping.version" + ManipString.sqlEqual(selection.get("version").get(0), type.get("version")));
                HashMap<String, String> defaultInputFields = new HashMap<String, String>();
                defaultInputFields.put("id_norme", selection.get("id_norme").get(0));
                defaultInputFields.put("periodicite", selection.get("periodicite").get(0));
                defaultInputFields.put("validite_inf", selection.get("validite_inf").get(0));
                defaultInputFields.put("validite_sup", selection.get("validite_sup").get(0));
                defaultInputFields.put("version", selection.get("version").get(0));
                // this.viewMapping.setColumnRendering(ArcConstantVObjectGetter.columnRender.get(this.viewMapping.getSessionName()));
                this.viewMapping.initialize(requete.toString(), "arc.ihm_mapping_regle", defaultInputFields);
            } else {
                this.viewMapping.destroy();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // visual du filtrage
    public void initializeFiltrage() {
        try {
            System.out.println("/* initializeFiltrage */");
            HashMap<String, ArrayList<String>> selection = this.viewJeuxDeRegles.mapContentSelected();
            if (!selection.isEmpty()) {
                HashMap<String, String> type = this.viewJeuxDeRegles.mapHeadersType();
                StringBuilder requete = new StringBuilder();
                requete.append("select * from arc.ihm_filtrage_regle");
                requete.append(" where id_norme" + ManipString.sqlEqual(selection.get("id_norme").get(0), type.get("id_norme")));
                requete.append(" and periodicite" + ManipString.sqlEqual(selection.get("periodicite").get(0), type.get("periodicite")));
                requete.append(" and validite_inf" + ManipString.sqlEqual(selection.get("validite_inf").get(0), type.get("validite_inf")));
                requete.append(" and validite_sup" + ManipString.sqlEqual(selection.get("validite_sup").get(0), type.get("validite_sup")));
                requete.append(" and version" + ManipString.sqlEqual(selection.get("version").get(0), type.get("version")));
                HashMap<String, String> defaultInputFields = new HashMap<String, String>();
                defaultInputFields.put("id_norme", selection.get("id_norme").get(0));
                defaultInputFields.put("periodicite", selection.get("periodicite").get(0));
                defaultInputFields.put("validite_inf", selection.get("validite_inf").get(0));
                defaultInputFields.put("validite_sup", selection.get("validite_sup").get(0));
                defaultInputFields.put("version", selection.get("version").get(0));
                // this.viewFiltrage.setColumnRendering(ArcConstantVObjectGetter.columnRender.get(this.viewFiltrage.getSessionName()));
                this.viewFiltrage.initialize(requete.toString(), "arc.ihm_filtrage_regle", defaultInputFields);
            } else {
                this.viewFiltrage.destroy();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Action(value = "/updateFiltrage")
    public String updateFiltrage() {
        try {
            boolean isRegleOk = true;
            // HierarchicalView afterUpdate = HierarchicalView.asRelationalToHierarchical("All", Arrays.asList("id_regle",
            // "id_norme", "validite_inf", "validite_sup", "version", "periodicite", "expr_regle_filtre",
            // "commentaire"), Format.patch(this.viewFiltrage.listContentAfterUpdate()));
            // afterUpdate.print(System.out);
            // HierarchicalView normeToPeriodiciteToValiditeInfToValiditeSupToRegle = HierarchicalView.asCoupe(
            // afterUpdate,
            // Arrays.asList("id_norme", "periodicite", "validite_inf", "validite_sup", "expr_regle_filtre"));
            // ArrayList<ArrayList<String>> afterUpdate = this.viewFiltrage.listContentAfterUpdate();
            LoggerDispatcher.info("Contenu de l'update : " + this.viewFiltrage.listContentAfterUpdate(), LOGGER);
            String exprRegleFiltre = this.viewFiltrage.listContentAfterUpdate().get(0).get(6);

            StringBuilder message = new StringBuilder();
            try {
                // Fabrication de la table de test
                UtilitaireDao.get("arc").executeRequest(null,
                        createTableTest("arc.test_ihm_controle_ok", ManipString.extractRubriques(exprRegleFiltre)));

                UtilitaireDao.get("arc").executeRequest(null,
                        "SELECT * FROM arc.test_ihm_controle_ok WHERE " + ManipString.extractAllRubrique(exprRegleFiltre));
                LoggerDispatcher.info(
                        "La requete de test ? " + "SELECT * FROM arc.test_ihm_controle_ok WHERE " + ManipString.extractAllRubrique(exprRegleFiltre),
                        LOGGER);
                message.append("Règles modifiées avec succès !");
            } catch (Exception ex) {
                isRegleOk = false;
                message.append("Erreur lors de l'insertion de la règle : " + ex.getMessage());
            }

            // HierarchicalView rootToRegle = HierarchicalView.asRoot("root");
            // rootToRegle.addLevel("expr_regle_filtre");
            // rootToRegle.put(exprRegleFiltre);
            // ServiceCommunFiltrageMapping.parserRegleGlobale(null, "arc.ihm", rootToRegle, "expr_regle_filtre");
            // Set<String> emptySet = Collections.emptySet();
            // ApiFiltrageService.parserRegleCorrespondanceFonctionnelle(rootToRegle, emptySet, "expr_regle_filtre");
            // StringBuilder message = new StringBuilder();
            // for (HierarchicalView regle : rootToRegle.getLevel("expr_regle_filtre")) {
            // try {
            // UtilitaireDao.get("arc").executeRequest(null, "SELECT * FROM arc.test_ihm_controle_ok WHERE " + regle.getLocalRoot());
            // LoggerDispatcher.info("La requete de test ? "+"SELECT * FROM arc.test_ihm_controle_ok WHERE " + regle.getLocalRoot(),
            // logger);
            // message.append("Règles modifiées avec succès !");
            // } catch (Exception ex) {
            // isRegleOk = false;
            // message.append("Erreur sur la règle : " + ex.getMessage());
            // }
            // }
            this.viewFiltrage.setMessage(message.toString());
            if (isRegleOk) {
                this.viewFiltrage.update();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sessionSyncronize();
    }

    private static String createTableTest(String aNomTable, ArrayList<String> listRubrique) {
        StringBuilder sb = new StringBuilder();
        sb.append("DROP TABLE IF EXISTS " + aNomTable + ";");
        sb.append("CREATE TABLE " + aNomTable);
        sb.append("(id_norme text, periodicite text, id_source text, validite text, id integer, controle text, brokenrules text[] ");
        
        //Je m'assure que les ubriques de base ne sont pas dans la liste des rubriques du filtre (sinon requête invalide),
        //--> cas possible lorsque par exemple, le paramètre {validite} apparait dans la règle de filtrage.
        listRubrique.remove("id_norme");
        listRubrique.remove("periodicite");
        listRubrique.remove("id_source");
        listRubrique.remove("validite");
        listRubrique.remove("id");
        listRubrique.remove("controle");
        listRubrique.remove("brokenrules");

        
        for (String rub : listRubrique) {
            if (rub.toUpperCase().startsWith("I")) {
                sb.append("," + rub + " integer");
            } else {
                sb.append("," + rub + " text");
            }

        }
        sb.append(");");
        LoggerDispatcher.info("Requete de création de la table de test : " + sb, LOGGER);
        return sb.toString();
    }

    @Action(value = "/selectMapping")
    public String selectMapping() {
        return sessionSyncronize();
    }

    @Action(value = "/addMapping")
    public String addMapping() {
        this.viewMapping.insert(new AttributeValue("id_regle", "(SELECT max(id_regle)+1 FROM arc.ihm_mapping_regle)"));
        return sessionSyncronize();
    }

    @Action(value = "/deleteMapping")
    public String deleteMapping() {
        this.viewMapping.delete();
        return sessionSyncronize();
    }

    /**
     *
     * @param someRegleMapping
     * @param anEnvironnementParameter
     * @param anEnvironnementTest
     * @param anEtat
     * @param anIdNorme
     * @param message
     * @return
     */
    // public static final boolean testerListeRegleMapping(List<List<String>> someRegleMapping, String anEnvironnementParameter,
    // String anEnvironnementTest, String anEtat, String anIdNorme, StringBuilder message) {
    // boolean isRegleOk = true;
    // int indexRegle = 0;
    // try {
    // ApiInitialisationService.copyTablesToExecution(null, anEnvironnementParameter, anEnvironnementTest);
    // ApiMappingService service = (ApiMappingService) ApiServiceFactory.getService(TraitementPhase.MAPPING.toString(),
    // anEnvironnementParameter, anEnvironnementTest, null, String.valueOf(ApiService.nombreLigneMaxTraiter));
    // try {
    // Set<String> listeRubrique = ServiceCommunFiltrageMapping.calculerListeColonnes(null, service.tableTempFiltrageOk);
    // System.out.println(anEtat);
    // List<List<String>> normeTableVariable = ApiMappingService.getListeNormePeriodiciteValInfValSupTableMetierVariable(null,
    // anEnvironnementParameter, anEnvironnementTest, anEtat);
    // HierarchicalView normeToPeriodiciteToValiditeInfToValiditeSupToTableToVariableToIdRubriqueToGroupe = ApiMappingService
    // .calculerNormeToPeriodiciteToValiditeInfToValiditeSupToTableToVariableToIdRubriqueToGroupe(normeTableVariable);
    // HierarchicalView normeToPeriodiciteToValiditeInfToValiditeSupToTableMetierToIdRubrique = HierarchicalView.asCoupe(
    // normeToPeriodiciteToValiditeInfToValiditeSupToTableToVariableToIdRubriqueToGroupe,
    // Arrays.asList("id_norme", ApiMappingService.levelPeriodicite, "validite_inf", "validite_sup", "table_metier"));
    // normeToPeriodiciteToValiditeInfToValiditeSupToTableMetierToIdRubrique.addLevel("id_rubrique");
    // Set<String> listeVariable = new HashSet<String>();
    // normeToPeriodiciteToValiditeInfToValiditeSupToTableToVariableToIdRubriqueToGroupe.print(System.out);
    // for (HierarchicalView variable : normeToPeriodiciteToValiditeInfToValiditeSupToTableToVariableToIdRubriqueToGroupe.get(anIdNorme)
    // .getLevel("variable_metier")) {
    // listeVariable.add(variable.getLocalRoot());
    // }
    // List<List<String>> normeVariableTable = ApiMappingService.getListeNormeVariableTableMetier(null, anEnvironnementTest);
    // HierarchicalView normeToVariableToTableMetier = ApiMappingService.calculerNormeToVariableToTableMetier(normeVariableTable);
    // HierarchicalView normeToVariableToType = ApiMappingService.calculerNormeToVariableToType(null, anEnvironnementTest);
    // normeToVariableToTableMetier.print(System.out);
    // for (indexRegle = 0; indexRegle < someRegleMapping.size(); indexRegle++) {
    // /**
    // * Récupération des informations intéressantes
    // */
    // String idNorme = someRegleMapping.get(indexRegle).get(1);
    // String periodicite = someRegleMapping.get(indexRegle).get(5);
    // String validiteInf = someRegleMapping.get(indexRegle).get(2);
    // String validiteSup = someRegleMapping.get(indexRegle).get(3);
    // String variable = someRegleMapping.get(indexRegle).get(indexColonneVariableTableRegleMapping);
    // String regle = someRegleMapping.get(indexRegle).get(7);
    // String type = normeToVariableToType.get(idNorme).get(variable).getUniqueChild().getLocalRoot();
    // System.out.println("Recherche de " + idNorme + " et " + variable);
    // Collection<HierarchicalView> listeTable = normeToVariableToTableMetier.get(idNorme).get(variable).children();
    // listeVariable.remove(variable);
    // String regleApresParsingPourRegleGlobale = ServiceCommunFiltrageMapping.traiterRegleGlobale(null, regle, anEnvironnementTest);
    // Set<String> regleApresParsingPourRegleDeGroupe = ApiMappingService.traiterRegleGroupe(regleApresParsingPourRegleGlobale);
    // Set<String> regleApresParsingPourRegleCorrespondanceFonctionnelle = new HashSet<>();
    // for (String expr : regleApresParsingPourRegleDeGroupe) {
    // regleApresParsingPourRegleCorrespondanceFonctionnelle.add(ApiMappingService.remplacerRubriqueDansRegle(expr, listeTable,
    // listeRubrique, normeToPeriodiciteToValiditeInfToValiditeSupToTableMetierToIdRubrique, type, null, null, null));
    // }
    // for (String expr : regleApresParsingPourRegleCorrespondanceFonctionnelle) {
    // if (expr.matches(ApiMappingService.regexpRegleClefUnique)) {
    // System.out.println(expr + " -> " + expr.replaceAll("^\\{pk:", "arc_test_ihm_").replaceAll("\\}$", "_encours"));
    // /**
    // * Une règle de "clef unique" doit porter sur un nom de table valide tel que défini dans ***_mod_table_metier
    // */
    // if (!normeToPeriodiciteToValiditeInfToValiditeSupToTableToVariableToIdRubriqueToGroupe.get(idNorme).get(periodicite)
    // .get(validiteInf).get(validiteSup)
    // .hasChild(expr.replaceAll("^\\{pk:", "arc_test_ihm_").replaceAll("\\}$", "_encours"))) {
    // throw new SQLException("La table " + expr.replaceAll("^\\{pk:", "").replaceAll("\\}$", "") + "n'existe pas !");
    // }
    // } else {
    // StringBuilder select = new StringBuilder("SELECT " + expr + " AS alias").append("  FROM " + service.getTablePrevious());
    // System.out.println("EXECUTION DE " + select.toString());
    // UtilitaireDao.get("arc").executeRequest(null, select.toString());
    // }
    // }
    // }
    // } finally {
    // service.finaliser();
    // }
    // } catch (Exception e) {
    // e.printStackTrace();
    // isRegleOk = false;
    // String variable = someRegleMapping.get(indexRegle).get(indexColonneVariableTableRegleMapping);
    // String regle = someRegleMapping.get(indexRegle).get(7);
    // String idRegle = someRegleMapping.get(indexRegle).get(0);
    // message.append("Erreur sur la règle " + idRegle + " : " + variable + " = " + regle);
    // message.append("\n").append("  " + e.getLocalizedMessage());
    // }
    //
    // return isRegleOk;
    // }
    //
    @Action(value = "/updateMapping")
    public String updateMapping() {
        Map<String, ArrayList<String>> afterUpdate = this.viewMapping.mapContentAfterUpdate();
        boolean isRegleOk = testerReglesMapping(afterUpdate);
        System.out.println("------------------->" + isRegleOk);
        if (isRegleOk) {
            this.viewMapping.update();
        }
        return sessionSyncronize();
    }

    /**
     * @param afterUpdate
     * @param isRegleOk
     * @return
     */
    public boolean testerReglesMapping(Map<String, ArrayList<String>> afterUpdate) {
        boolean isRegleOk = true;
        List<String> tableADropper = new ArrayList<>();
        String zeExpression = null;
        String zeVariable = null;
        if (!afterUpdate.isEmpty() && !afterUpdate.get("expr_regle_col").isEmpty()) {

            try {
                /*
                 * Récupération du jeu de règle
                 */
                JeuDeRegle jdr = fetchJeuDeRegle();
                /*
                 * recopie des tables de l'environnement
                 */
                List<String> tables = getTableEnvironnement(jdr.getEtat());
                List<String> sources = new ArrayList<String>();
                List<String> targets = new ArrayList<String>();
                String envTarget = jdr.getEtat().replace(DOT, ".test_");
                for (int i = 0; i < tables.size(); i++) {
                    sources.add(jdr.getEtat() + tables.get(i));
                    targets.add(envTarget + tables.get(i));
                }
                tableADropper.addAll(targets);

                UtilitaireDao.get(poolName).dupliquerVers(null, sources, targets, "false");
                List<AbstractRegleMapping> listRegle = new ArrayList<>();
                RegleMappingFactory regleMappingFactory = new RegleMappingFactory(null, envTarget, new HashSet<String>(), new HashSet<String>());
                String idFamille = this.viewNorme.mapContentSelected().get("id_famille").get(0);
                regleMappingFactory.setIdFamille(idFamille);
                for (int i = 0; i < afterUpdate.get("expr_regle_col").size(); i++) {
                    String expression = afterUpdate.get("expr_regle_col").get(i);
                    String variable = afterUpdate.get("variable_sortie").get(i);
                    String type = afterUpdate.get("type_sortie").get(i);
                    VariableMapping variableMapping = new VariableMapping(regleMappingFactory, variable, type);
                    variableMapping.setExpressionRegle(regleMappingFactory.get(expression, variableMapping));
                    listRegle.add(variableMapping.getExpressionRegle());
                }
                /*
                 * on dérive pour avoir de belles expressions à tester et connaitre les noms de colonnes
                 */
                for (int i = 0; i < listRegle.size(); i++) {
                    listRegle.get(i).deriverTest();
                }
                Set<Integer> groupesUtiles = groupesUtiles(listRegle);
                /*
                 * on fait remonter les noms de colonnes dans colUtiles
                 */
                Set<String> colUtiles = new TreeSet<>();
                for (int i = 0; i < listRegle.size(); i++) {
                    for (Integer groupe : groupesUtiles) {
                        colUtiles.addAll(listRegle.get(i).getEnsembleIdentifiantsRubriques(groupe));
                        colUtiles.addAll(listRegle.get(i).getEnsembleNomsRubriques(groupe));
                        System.out.println(groupe + " -> " + colUtiles);
                    }
                    colUtiles.addAll(listRegle.get(i).getEnsembleIdentifiantsRubriques());
                    colUtiles.addAll(listRegle.get(i).getEnsembleNomsRubriques());
                }

                createTablePhasePrecedente(envTarget, colUtiles, tableADropper);
                for (int i = 0; i < listRegle.size(); i++) {
                    zeExpression = listRegle.get(i).getExpression();
                    zeVariable = listRegle.get(i).getVariableMapping().getNomVariable();
                    // Test de l'expression
                    groupesUtiles = groupesUtiles(Arrays.asList(listRegle.get(i)));
                    if (groupesUtiles.isEmpty()) {
                        if (createRequeteSelect(envTarget, listRegle.get(i))
                                && clefConsolidation.equalsIgnoreCase(afterUpdate.get("type_consolidation").get(i))) {
                            throw new IllegalArgumentException(messageVariableClefNull.replace(tokenNomVariable, listRegle.get(i)
                                    .getVariableMapping().getNomVariable()));
                        }
                    } else {
                        for (Integer groupe : groupesUtiles) {
                            if (createRequeteSelect(envTarget, listRegle.get(i), groupe)
                                    && clefConsolidation.equalsIgnoreCase(afterUpdate.get("type_consolidation").get(i))) {
                                throw new IllegalArgumentException(messageVariableClefNull.replace(tokenNomVariable, listRegle.get(i)
                                        .getVariableMapping().getNomVariable()
                                        + "(groupe " + groupe + ")"));
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                isRegleOk = false;
                this.viewMapping.setMessage((zeVariable == null ? empty : "La règle " + zeVariable + " ::= " + zeExpression + " est erronée.\n")
                        + "Exception levée : " + ex.getMessage());
                LoggerHelper.errorGenTextAsComment(getClass(), "testerReglesMapping()", LOGGER, ex);
            } finally {
                UtilitaireDao.get(poolName).dropTable(null, tableADropper.toArray(new String[0]));
            }
        }
        return isRegleOk;
    }

    /**
     * @param listRegle
     * @param returned
     * @return
     * @throws Exception
     */
    private static Set<Integer> groupesUtiles(List<AbstractRegleMapping> listRegle) throws Exception {
        Set<Integer> returned = new TreeSet<>();
        for (int i = 0; i < listRegle.size(); i++) {
            returned.addAll(listRegle.get(i).getEnsembleGroupes());
        }
        return returned;
    }

    /**
     * Exécution d'une règle sur la table <anEnvTarget>_filtrage_ok
     *
     * @param anEnvTarget
     * @param regleMapping
     * @return
     * @throws SQLException
     */
    private static Boolean createRequeteSelect(String anEnvTarget, AbstractRegleMapping regleMapping) throws Exception {
        StringBuilder requete = new StringBuilder("SELECT CASE WHEN ")//
                .append("(" + regleMapping.getExpressionSQL() + ")::" + regleMapping.getVariableMapping().getType())//
//                .append(" IS NULL THEN true ELSE false END")//
                .append(" IS NULL THEN false ELSE false END")//
                .append(" AS " + regleMapping.getVariableMapping().getNomVariable());
        requete.append("\n  FROM " + anEnvTarget + underscore + "filtrage_ok ;");
        return UtilitaireDao.get(poolName).getBoolean(null, requete);
    }

    private static Boolean createRequeteSelect(String anEnvTarget, AbstractRegleMapping regleMapping, Integer groupe) throws Exception {
        StringBuilder requete = new StringBuilder("SELECT CASE WHEN ");//
        requete.append("(" + regleMapping.getExpressionSQL(groupe) + ")::" + regleMapping.getVariableMapping().getType().replace("[]", ""))//
//                .append(" IS NULL THEN true ELSE false END")//
                .append(" IS NULL THEN false ELSE false END")//
                .append(" AS " + regleMapping.getVariableMapping().getNomVariable());
        requete.append("\n  FROM " + anEnvTarget + underscore + "filtrage_ok ;");
        return UtilitaireDao.get(poolName).getBoolean(null, requete);
    }

    /**
     * Creation d'une table vide avec les colonnes adéquates.<br/>
     * En particulier, si la règle n'utilise pas du tout de noms de colonnes, une colonne {@code col$null} est créée, qui permette un
     * requêtage.
     *
     * @param anEnvTarget
     * @param colUtiles
     * @param tableADropper
     * @throws SQLException
     */
    private static void createTablePhasePrecedente(String anEnvTarget, Set<String> colUtiles, List<String> tableADropper) throws SQLException {
        StringBuilder requete = new StringBuilder("DROP TABLE IF EXISTS " + anEnvTarget + underscore + "filtrage_ok;");
        requete.append("CREATE TABLE " + anEnvTarget + underscore + "filtrage_ok (");
        requete.append(Format.untokenize(colUtiles, " text, "));
        requete.append(colUtiles.isEmpty() ? "col$null text" : " text")//
                .append(");");

        requete.append("\nINSERT INTO " + anEnvTarget + underscore + "filtrage_ok (")//
                .append(Format.untokenize(colUtiles, ", "))//
                .append(colUtiles.isEmpty() ? "col$null" : empty)//
                .append(") VALUES (");
        boolean isFirst = true;
        for (String variable : colUtiles) {
            if (isFirst) {
                isFirst = false;
            } else {
                requete.append(", ");
            }
            if (ApiMappingService.colNeverNull.contains(variable)) {
                requete.append("'" + variable + "'");
            } else {
                requete.append("null");
            }
        }
        requete.append(colUtiles.isEmpty() ? "null" : empty)//
                .append(");");

        tableADropper.add(anEnvTarget + underscore + "filtrage_ok");
        UtilitaireDao.get(poolName).executeRequest(null, requete);
    }

    private static List<String> getTableEnvironnement(String etat) {
        StringBuilder requete = new StringBuilder();

        String zeEnv = ManipString.substringAfterFirst(etat, DOT);
        String zeSchema = ManipString.substringBeforeFirst(etat, DOT);

        requete.append("SELECT replace(lower(relname), '" + zeEnv + "', '') ")//
                .append("\n  FROM pg_class a ")//
                .append("\n  INNER JOIN pg_namespace b ON a.relnamespace=b.oid ")//
                .append("\n  WHERE lower(b.nspname)=lower('" + zeSchema + "') ")//
                .append("\n  AND lower(relname) LIKE '" + zeEnv.toLowerCase() + "\\_%'; ");
        return UtilitaireDao.get(poolName).getList(null, requete, new ArrayList<String>());
    }

    @Action(value = "/importFiltrage")
    public String importFiltrage() throws IOException {

        return sessionSyncronize();
    }

    @Action(value = "/preGenererRegleFiltrage")
    public String preGenererRegleFiltrage() {
        try {
            UtilitaireDao.get("arc").executeRequest(null, new StringBuilder("INSERT INTO " + this.viewFiltrage.getTable())//
                    .append("  " + Format.stringListe(this.viewFiltrage.getHeadersDLabel()))//
                    .append("  SELECT (SELECT coalesce(max(id_regle),1) FROM " + this.viewFiltrage.getTable() + ")+row_number() over () ,")//
                    .append("  '" + this.viewJeuxDeRegles.mapContentSelected().get("id_norme").get(0) + "', ")//
                    .append("  '" + this.viewJeuxDeRegles.mapContentSelected().get("validite_inf").get(0) + "', ")//
                    .append("  '" + this.viewJeuxDeRegles.mapContentSelected().get("validite_sup").get(0) + "', ")//
                    .append("  '" + this.viewJeuxDeRegles.mapContentSelected().get("version").get(0) + "', ")//
                    .append("  '" + this.viewJeuxDeRegles.mapContentSelected().get("periodicite").get(0) + "', ")//
                    .append("  null,")//
                    .append("  null;"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sessionSyncronize();
    }

    @Action(value = "/importMapping")
    public String importMapping() throws IOException {
        if (StringUtils.isBlank(this.fileUpload.getPath())) {
            this.viewMapping.setMessage("Vous devez d'abord choisir un fichier.");
        } else {
            boolean isRegleOk = false;
            try {
                Map<String, String> mapVariableToType = new HashMap<String, String>();
                Map<String, String> mapVariableToTypeConso = new HashMap<String, String>();
                calculerVariableToType(mapVariableToType, mapVariableToTypeConso);
                Set<String> variablesAttendues = mapVariableToType.keySet();
                String nomTable = "arc.ihm_mapping_regle";
                List<RegleMappingEntity> listeRegle = new ArrayList<>();
                EntityDao<RegleMappingEntity> dao = new MappingRegleDao();
                System.out.println(dao);
                dao.setTableName(nomTable);
                dao.setEOLSeparator(true);
                Map<String, ArrayList<String>> reglesAImporter = calculerReglesAImporter(this.fileUpload, listeRegle, dao, mapVariableToType,
                        mapVariableToTypeConso);
                System.out.println(listeRegle);
                Set<String> variablesSoumises = new HashSet<>();
                for (int i = 0; i < listeRegle.size(); i++) {
                    variablesSoumises.add(Format.toLowerCase(listeRegle.get(i).getVariableSortie()));
                }
                System.out.println(variablesSoumises);
                System.out.println(variablesAttendues);
                Set<String> variablesAttenduesTemp = new HashSet<>(variablesAttendues);
                variablesAttenduesTemp.removeAll(variablesSoumises);
                if (!variablesAttenduesTemp.isEmpty()) {
                    throw new IllegalStateException("Les variables " + variablesAttenduesTemp + " ne font pas l'objet d'une règle de mapping.");
                }
                variablesSoumises.removeAll(variablesAttendues);
                if (!variablesSoumises.isEmpty()) {
                    throw new IllegalStateException("Les variables " + variablesSoumises + " ne figurent pas dans le modèle.");
                }
                isRegleOk = testerReglesMapping(reglesAImporter);
                Map<String, String> map = new HashMap<String, String>();
                map.put("id_regle", "(SELECT max(id_regle)+1 FROM " + nomTable + ")");
                map.put("id_norme", this.viewNorme.mapContentSelected().get("id_norme").get(0));
                map.put("validite_inf", this.viewJeuxDeRegles.mapContentSelected().get("validite_inf").get(0));
                map.put("validite_sup", this.viewJeuxDeRegles.mapContentSelected().get("validite_sup").get(0));
                map.put("version", this.viewJeuxDeRegles.mapContentSelected().get("version").get(0));
                map.put("periodicite", this.viewJeuxDeRegles.mapContentSelected().get("periodicite").get(0));
                if (isRegleOk) {
                    // voir si ch variable a une règle
                    JeuDeRegle jdr = fetchJeuDeRegle();
                    StringBuilder bloc = new StringBuilder();
                    /*
                     * DELETE from
                     */
                    bloc.append("DELETE FROM " + nomTable + " WHERE " + jdr.getSqlEquals() + ";");
                    for (int i = 0; i < listeRegle.size(); i++) {
                        bloc.append(dao.getInsert(listeRegle.get(i), map));
                    }
                    UtilitaireDao.get(poolName).executeBlock(null, bloc);
                }
            } catch (Exception ex) {
                LoggerHelper.errorGenTextAsComment(getClass(), "importMapping()", LOGGER, ex);
                this.viewMapping.setMessage("Erreur lors de l'import : " + ex.toString());
            }
        }
        return sessionSyncronize();
    }

    private void calculerVariableToType(Map<String, String> mapVariableToType, Map<String, String> mapVariableToTypeConso) throws SQLException {
        ArrayList<ArrayList<String>> resultat = UtilitaireDao
                .get("arc")
                .executeRequest(
                        null,
                        new StringBuilder(
                                "SELECT DISTINCT lower(nom_variable_metier) AS nom_variable_metier, type_variable_metier, type_consolidation AS type_sortie FROM arc.ihm_mod_variable_metier WHERE id_famille='"
                                        + this.viewNorme.mapContentSelected().get("id_famille").get(0) + "'"));
        for (int i = 2; i < resultat.size(); i++) {
            mapVariableToType.put(resultat.get(i).get(0), resultat.get(i).get(1));
            mapVariableToTypeConso.put(resultat.get(i).get(0), resultat.get(i).get(2));
        }
    }

    private Map<String, ArrayList<String>> calculerReglesAImporter(File aFileUpload, List<RegleMappingEntity> listeRegle,
            EntityDao<RegleMappingEntity> dao, Map<String, String> mapVariableToType, Map<String, String> mapVariableToTypeConso) throws Exception {
        Map<String, ArrayList<String>> returned = new HashMap<>();
        returned.put("type_sortie", new ArrayList<String>());
        returned.put("type_consolidation", new ArrayList<String>());
        if (!aFileUpload.exists()) {
            throw new FileNotFoundException(aFileUpload.getAbsolutePath());
        }
        if (aFileUpload.isDirectory()) {
            throw new IOException(aFileUpload.getAbsolutePath() + " n'est pas un chemin de fichier valide.");
        }
        try {
            BufferedReader br = Files.newBufferedReader(aFileUpload.toPath(), Charset.forName("UTF-8"));
            try {
                dao.setSeparator(";");
                String line = br.readLine();
                String someNames = line;
                dao.setNames(someNames);
                line = br.readLine();
                String someTypes = line;
                dao.setTypes(someTypes);
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                    RegleMappingEntity entity = dao.get(line);
                    listeRegle.add(entity);
                    for (String colName : entity.colNames()) {
                        if (!returned.containsKey(colName)) {
                            /*
                             * La colonne n'existe pas encore ? Je l'ajoute.
                             */
                            returned.put(colName, new ArrayList<String>());
                        }
                        /*
                         * J'ajoute la valeur en fin de colonne.
                         */
                        returned.get(colName).add(entity.get(colName));
                    }
                    returned.get("type_sortie").add(mapVariableToType.get(entity.getVariableSortie()));
                    returned.get("type_consolidation").add(mapVariableToTypeConso.get(entity.getVariableSortie()));
                }
            } finally {
                br.close();
            }
        } catch (Exception ex) {
            LoggerHelper.errorGenTextAsComment(getClass(), "calculerReglesAImporter()", LOGGER, ex);
            throw ex;
        }
        return returned;
    }

    public static boolean testerConsistanceRegleMapping(List<List<String>> data, String anEnvironnement, String anIdFamille, StringBuilder aMessage) {
        Set<String> variableRegleCharge = new HashSet<String>();
        for (int i = 0; i < data.size(); i++) {
            variableRegleCharge.add(data.get(i).get(indexColonneVariableTableRegleMapping));
        }
        Set<String> variableTableModele = new HashSet<String>();
        variableTableModele.addAll(UtilitaireDao.get("arc").getList(
                null,
                new StringBuilder("SELECT DISTINCT nom_variable_metier FROM " + anEnvironnement + "_mod_variable_metier WHERE id_famille='"
                        + anIdFamille + "'"), new ArrayList<String>()));
        LoggerDispatcher.info("La requete de construction de variableTableMetier : \n" + "SELECT DISTINCT nom_variable_metier FROM "
                + anEnvironnement + "_mod_variable_metier WHERE id_famille='" + anIdFamille + "'", LOGGER);
        Set<String> variableToute = new HashSet<String>();
        variableToute.addAll(variableRegleCharge);
        variableToute.addAll(variableTableModele);
        boolean ok = true;
        LoggerDispatcher.info("Les variables du modèle : " + variableTableModele, LOGGER);
        LoggerDispatcher.info("Les variables des règles chargées : " + variableRegleCharge, LOGGER);
        for (String variable : variableToute) {
            if (!variableRegleCharge.contains(variable)) {
                ok = false;
                aMessage.append("La variable " + variable + " n'est pas présente dans les règles chargées.\n");
            }
            if (!variableTableModele.contains(variable)) {
                ok = false;
                aMessage.append(variable + " ne correspond à aucune variable existant.\n");
            }
        }
        if (!ok) {
            aMessage.append("Les règles ne seront pas chargées.");
        }
        return ok;
    }

    public static String getIdFamille(String anEnvironnement, String anIdNorme) throws SQLException {
        return UtilitaireDao.get("arc").getString(null,
                "SELECT id_famille FROM " + anEnvironnement + "_norme" + " WHERE id_norme='" + anIdNorme + "'");
    }

    /**
     *
     * @param returned
     * @param index
     *            -1 : en fin de tableau<br/>
     *            [0..size[ : le premier élément est ajouté à l'emplacement {@code index}, les suivants, juste après
     * @param args
     * @return
     */
    public static List<List<String>> ajouterInformationTableau(List<List<String>> returned, int index, String... args) {
        for (int i = 0; i < returned.size(); i++) {
            for (int j = 0; j < args.length; j++) {
                returned.get(i).add((index == -1 ? returned.get(i).size() : index + j), args[j]);
            }
        }
        return returned;
    }

    @Action(value = "/preGenererRegleMapping")
    public String preGenererRegleMapping() {
        try {
            LoggerDispatcher.info("************* preGenererRegleMapping **************", LOGGER);
            // liste des colonnes en dur dans le INSERT pour bien concorder avec l'ordre du SELECT
            StringBuilder requete = new StringBuilder("INSERT INTO " + this.viewMapping.getTable())
                    .append("  (id_regle, id_norme, validite_inf, validite_sup,  version , periodicite, variable_sortie, expr_regle_col, commentaire) ")
                    .append("  SELECT (SELECT max(id_regle) FROM " + this.viewMapping.getTable() + ")+row_number() over () ,")
                    .append("  '" + this.viewJeuxDeRegles.mapContentSelected().get("id_norme").get(0) + "', ")
                    .append("  '" + this.viewJeuxDeRegles.mapContentSelected().get("validite_inf").get(0) + "', ")
                    .append("  '" + this.viewJeuxDeRegles.mapContentSelected().get("validite_sup").get(0) + "', ")
                    .append("  '" + this.viewJeuxDeRegles.mapContentSelected().get("version").get(0) + "', ")
                    .append("  '" + this.viewJeuxDeRegles.mapContentSelected().get("periodicite").get(0) + "', ")
                    .append("  liste_colonne.nom_variable_metier,")
                    .append("  null,")
                    .append("  null")
                    .append("  FROM ("
                            + FormatSQL.listeColonneTableMetierSelonFamilleNorme("arc.ihm", this.viewNorme.mapContentSelected().get("id_famille")
                                    .get(0)) + ") liste_colonne");
            System.out.println(requete);
            UtilitaireDao.get("arc").executeRequest(null, requete);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sessionSyncronize();
    }

    @Action(value = "/sortFiltrage")
    public String sortFiltrage() {
        this.viewFiltrage.sort();
        return sessionSyncronize();
    }

    @Action(value = "/sortMapping")
    public String sortMapping() {
        this.viewMapping.sort();
        return sessionSyncronize();
    }

    /**
     * Méthode pour afficher l'ensemble des JeuDeRegle afin de choisir lequel copier
     */

    @Action(value = "selectJeuxDeReglesChargementCopie")
    public String selectJeuxDeReglesChargementCopie() {
        initializeJeuxDeReglesCopie();
        this.viewJeuxDeReglesCopie.getCustomValues().put(selectedJeuDeRegle, this.viewChargement.getTable());
        return sessionSyncronize();
    }
    
    @Action(value = "selectJeuxDeReglesNormageCopie")
    public String selectJeuxDeReglesNormageCopie() {
        initializeJeuxDeReglesCopie();
        this.viewJeuxDeReglesCopie.getCustomValues().put(selectedJeuDeRegle, this.viewNormage.getTable());
        return sessionSyncronize();
    }

    @Action(value = "selectJeuxDeReglesControleCopie")
    public String selectJeuxDeReglesControleCopie() {
        initializeJeuxDeReglesCopie();
        this.viewJeuxDeReglesCopie.getCustomValues().put(selectedJeuDeRegle, this.viewControle.getTable());
        return sessionSyncronize();
    }

    @Action(value = "selectJeuxDeReglesFiltrageCopie")
    public String selectJeuxDeReglesFiltrageCopie() {
        initializeJeuxDeReglesCopie();
        this.viewJeuxDeReglesCopie.getCustomValues().put(selectedJeuDeRegle, this.viewFiltrage.getTable());
        return sessionSyncronize();
    }

    @Action(value = "/selectJeuxDeReglesMappingCopie")
    public String selectJeuxDeReglesMappingCopie() {
        // this.selectedJeuDeRegle = this.viewMapping.getTable();
        initializeJeuxDeReglesCopie();
        this.viewJeuxDeReglesCopie.getCustomValues().put(selectedJeuDeRegle, this.viewMapping.getTable());
        return sessionSyncronize();
    }

    public void initializeJeuxDeReglesCopie() {
        System.out.println("/* initializeJeuxDeReglesCopie */");
        StringBuilder requete = new StringBuilder();
        requete.append("select id_norme, periodicite, validite_inf, validite_sup, version, etat from arc.ihm_jeuderegle ");
        HashMap<String, String> defaultInputFields = new HashMap<String, String>();
        // this.viewJeuxDeReglesCopie.setColumnRendering(ArcConstantVObjectGetter.columnRender.get(this.viewJeuxDeReglesCopie.getSessionName()));
        this.viewJeuxDeReglesCopie.initialize(requete.toString(), "arc.ihm_jeuderegle", defaultInputFields);
        System.out.println("/******************" + this.viewJeuxDeReglesCopie.getCustomValues() + " */");
    }

    @Action(value = "/selectJeuxDeReglesCopie")
    public String selectJeuxDeReglesCopie() {
        try {
            LoggerDispatcher.info("Mon action pour sélectionner les règles à copier", LOGGER);
            initializeJeuxDeReglesCopie();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sessionSyncronize();
    }

    // TODO : trop pourri ; à refaire en générique
    @Action(value = "/copieJeuxDeRegles")
    public String copieJeuxDeRegles() {
        LoggerDispatcher.info("Mon action pour copier un jeu de règles", LOGGER);
        // le jeu de regle à copier
        HashMap<String, ArrayList<String>> selectionOut = this.viewJeuxDeRegles.mapContentSelected();
        // le nouveau jeu de regle
        HashMap<String, ArrayList<String>> selectionIn = this.viewJeuxDeReglesCopie.mapContentSelected();
        HashMap<String, String> type = this.viewJeuxDeReglesCopie.mapHeadersType();
        // System.out.println("mon test " + selectionIn.isEmpty());
        if (!selectionIn.isEmpty()) {
            StringBuilder requete = new StringBuilder();
            requete.append("INSERT INTO " + this.getSelectedJeuDeRegle() + " ");
            if (this.getSelectedJeuDeRegle().equals("arc.ihm_normage_regle")) {
                viderTableRegle(TraitementTableParametre.NORMAGE_REGLE.toString());
                requete.append("(id_norme, periodicite, validite_inf, validite_sup, version,  id_classe, rubrique,  rubrique_nmcl,  commentaire) ");
                requete.append("SELECT '" + selectionOut.get("id_norme").get(0) + "', '" + selectionOut.get("periodicite").get(0) + "', '"
                        + selectionOut.get("validite_inf").get(0) + "'::date, '" + selectionOut.get("validite_sup").get(0) + "'::date, '"
                        + selectionOut.get("version").get(0) + "',  id_classe, rubrique,  rubrique_nmcl,  commentaire  ");
            } else if (this.getSelectedJeuDeRegle().equals("arc.ihm_controle_regle")) {
                viderTableRegle(TraitementTableParametre.CONTROLE_REGLE.toString());
                requete.append("(id_norme, periodicite, validite_inf, validite_sup, version, id_classe, rubrique_pere, rubrique_fils, borne_inf, borne_sup, condition, pre_action, id_regle, commentaire, xsd_ordre, xsd_label_fils, xsd_role) ");
                requete.append("SELECT '" + selectionOut.get("id_norme").get(0) + "', '" + selectionOut.get("periodicite").get(0) + "', '"
                        + selectionOut.get("validite_inf").get(0) + "'::date, '" + selectionOut.get("validite_sup").get(0) + "'::date, '"
                        + selectionOut.get("version").get(0)
                        + "', id_classe, rubrique_pere, rubrique_fils, borne_inf, borne_sup, condition, pre_action, id_regle, commentaire, xsd_ordre, xsd_label_fils, xsd_role  ");
            } else if (this.getSelectedJeuDeRegle().equals("arc.ihm_filtrage_regle")) {
                viderTableRegle(TraitementTableParametre.FILTRAGE_REGLE.toString());
                requete.append("(id_regle, id_norme, validite_inf, validite_sup, version, periodicite, expr_regle_filtre, commentaire) ");
                requete.append("SELECT " + "row_number() over () +(SELECT max(id_regle) FROM " + this.getSelectedJeuDeRegle() + "), '"
                        + selectionOut.get("id_norme").get(0) + "', '" + selectionOut.get("validite_inf").get(0) + "'::date, '"
                        + selectionOut.get("validite_sup").get(0) + "'::date, '"//
                        + selectionOut.get("version").get(0) + "', '"//
                        + selectionOut.get("periodicite").get(0) + "', expr_regle_filtre, commentaire ");
            } else if (this.getSelectedJeuDeRegle().equals("arc.ihm_mapping_regle")) {
                viderTableRegle(TraitementTableParametre.MAPPING_REGLE.toString());
                requete.append("(id_regle, id_norme, validite_inf, validite_sup, version, periodicite, variable_sortie, expr_regle_col, commentaire) ");
                requete.append("SELECT " + "row_number() over () +(SELECT max(id_regle) FROM " + this.getSelectedJeuDeRegle() + "), '" //
                        + selectionOut.get("id_norme").get(0) + "', '" //
                        + selectionOut.get("validite_inf").get(0) + "'::date, '" //
                        + selectionOut.get("validite_sup").get(0) + "'::date, '"//
                        + selectionOut.get("version").get(0) + "', '" //
                        + selectionOut.get("periodicite").get(0) + "', variable_sortie, expr_regle_col, commentaire ");
            } else if (this.getSelectedJeuDeRegle().equals("arc.ihm_chargement_regle")) {
                viderTableRegle(TraitementTableParametre.CHARGEMENT_REGLE.toString());
                requete.append("(id_regle, id_norme, validite_inf, validite_sup, version, periodicite, type_fichier, delimiter, format, commentaire) ");
                requete.append("SELECT " + "row_number() over () +(SELECT max(id_regle) FROM " + this.getSelectedJeuDeRegle() + "), '" //
                        + selectionOut.get("id_norme").get(0) + "', '" //
                        + selectionOut.get("validite_inf").get(0) + "'::date, '" //
                        + selectionOut.get("validite_sup").get(0) + "'::date, '"//
                        + selectionOut.get("version").get(0) + "', '" //
                        + selectionOut.get("periodicite").get(0) + "',  type_fichier, delimiter, format, commentaire ");
            }
            requete.append("FROM " + this.getSelectedJeuDeRegle() + "  ");
            requete.append(" WHERE id_norme" + ManipString.sqlEqual(selectionIn.get("id_norme").get(0), type.get("id_norme")));
            requete.append(" AND periodicite" + ManipString.sqlEqual(selectionIn.get("periodicite").get(0), type.get("periodicite")));
            requete.append(" AND validite_inf" + ManipString.sqlEqual(selectionIn.get("validite_inf").get(0), type.get("validite_inf")));
            requete.append(" AND validite_sup" + ManipString.sqlEqual(selectionIn.get("validite_sup").get(0), type.get("validite_sup")));
            requete.append(" AND version" + ManipString.sqlEqual(selectionIn.get("version").get(0), type.get("version")));
            requete.append(" ;");

            // LoggerDispatcher.info("La requete de copie : \n " + requete, LOGGER);
            try {
                UtilitaireDao.get("arc").executeRequest(null, requete);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            this.viewJeuxDeReglesCopie.destroy();
        } else {
            LoggerDispatcher.info("Veuillez sélectionner un jeu de règles", LOGGER);
            this.viewJeuxDeRegles.setMessage("Veuillez sélectionner un jeu de règles");
        }
        return sessionSyncronize();
    }
    
    
    
    /**
     * 
     * @param vObjectToUpdate
     *            the vObject to update with file
     * @param tableName
     *            the
     */
    private void uploadFileRule(VObject vObjectToUpdate) {
	StringBuilder requete = new StringBuilder();

	// Check if there is file
	if (this.fileUpload == null || StringUtils.isBlank(this.fileUpload.getPath())) {
	    // No file -> ko
	    vObjectToUpdate.setMessage("Veuillez sélectionner un fichier !!");
	    return;
	}

	LoggerHelper.debug(LOGGER, " filesUpload  : " + this.getFileUpload());
	String nomTableImage = FormatSQL.temporaryTableName(vObjectToUpdate.getTable() + "_img" + 0);
	BufferedReader bufferedReader = null;
	try {
	    bufferedReader = Files.newBufferedReader(this.fileUpload.toPath(), Charset.forName("UTF-8"));

	    String listeColonnesAggregees = bufferedReader.readLine();
	    List<String> listeColonnes = Arrays.asList(listeColonnesAggregees.split(IConstanteCaractere.semicolon));
	    LoggerHelper.debug(LOGGER, "liste des colonnes : ", Format.untokenize(listeColonnes, ", "));
	    /*
	     * Création d'une table temporaire (qui ne peut pas être TEMPORARY)
	     */
	    requete.append("\n DROP TABLE IF EXISTS " + nomTableImage + " cascade;");
	    requete.append("\n CREATE TABLE " + nomTableImage + " AS SELECT "//
		    + Format.untokenize(listeColonnes, ", ") //
		    + "\n\t FROM " //
		    + vObjectToUpdate.getTable() //
		    + "\n\t WHERE false");

	    UtilitaireDao.get("arc").executeRequest(null, requete.toString());

	    //On saute la ligne de type
	    bufferedReader.readLine();

	    UtilitaireDao.get("arc").importing(null, nomTableImage, bufferedReader, true,false, IConstanteCaractere.semicolon);

	} catch (Exception ex) {
	    vObjectToUpdate.setMessage("Erreur grave : " + ex.getMessage());
	    LoggerHelper.error(LOGGER, ex, "uploadOutils()", "\n");
	    return;
	} finally {
	    try {
		if(bufferedReader!= null) {
			bufferedReader.close();
		}
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
	LoggerHelper.debug(LOGGER, "insertion dans la table imh_outil");

	HashMap<String, ArrayList<String>> selection = this.viewJeuxDeRegles.mapContentSelected();

	requete.setLength(0);

	requete.append("\n UPDATE " + nomTableImage + " SET ");
	requete.append("\n id_norme='" + selection.get("id_norme").get(0) + "'");
	requete.append("\n, periodicite='" + selection.get("periodicite").get(0) + "'");
	requete.append("\n, validite_inf='" + selection.get("validite_inf").get(0) + "'");
	requete.append("\n, validite_sup='" + selection.get("validite_sup").get(0) + "'");
	requete.append("\n, version='" + selection.get("version").get(0) + "';");
	requete.append("\n DELETE FROM " + vObjectToUpdate.getTable());
	requete.append("\n WHERE ");
	requete.append("\n id_norme='" + selection.get("id_norme").get(0) + "'");
	requete.append("\n AND  periodicite='" + selection.get("periodicite").get(0) + "'");
	requete.append("\n AND  validite_inf='" + selection.get("validite_inf").get(0) + "'");
	requete.append("\n AND  validite_sup='" + selection.get("validite_sup").get(0) + "'");
	requete.append("\n AND  version='" + selection.get("version").get(0) + "';");
	requete.append("\n INSERT INTO " + vObjectToUpdate.getTable() + " ");
	requete.append("\n SELECT * FROM " + nomTableImage + " ;");
	requete.append("\n DROP TABLE IF EXISTS " + nomTableImage + " cascade;");

	try {
	    UtilitaireDao.get("arc").executeRequest(null, requete.toString());
	} catch (Exception ex) {
	    vObjectToUpdate.setMessage("Erreur grave : " + ex.getMessage());
	    LoggerHelper.error(LOGGER, ex, "uploadOutils()");
	}

    }
    
    @Action(value="getNormTree")
	public String getNormTree() {
    	generateNormTree();
		return "success";
	}

    private void generateNormTree() {
    	try {
    		// Current ruleset
    		HashMap<String, ArrayList<String>> selection = this.viewJeuxDeRegles.mapContentSelected();
    		String idNorme = selection.get("id_norme").get(0);
    		String periodicite = selection.get("periodicite").get(0);
    		String validiteInf = selection.get("validite_inf").get(0);
    		String validiteSup = selection.get("validite_sup").get(0);
    		String version = selection.get("version").get(0);

    		// Filter infos
    		ArrayList<String> headersDLabel = viewControle.getHeadersDLabel();
    		ArrayList<String> filterFields = viewControle.getFilterFields();

    		HashMap<String, ArrayList<String>> controlSelected = viewControle.mapContentSelected();
    		if (!controlSelected.isEmpty()) {
    			currentSelectedControlRule = controlSelected.get("rubrique_pere").get(0);
    		}
    		controlDescription =  new XsdRulesRetrievalService().fetchRulesFromBase(null,
    				new JeuDeRegle(idNorme, periodicite, validiteInf, validiteSup, version));
    		String filterSql = viewControle.buildFilter(filterFields, headersDLabel);
    		if (filterSql.trim().equalsIgnoreCase("where true")) {
    			controlDescriptionFiltered = null;
    		} else {
    			controlDescriptionFiltered =  new XsdRulesRetrievalService().fetchRulesFromBase(null,
        				new JeuDeRegle(idNorme, periodicite, validiteInf, validiteSup, version),
        				filterSql);
    		}
    	} catch (Exception e) {
    		String message = "Erreur lors de la génération de l'arbre des règles : "
    				+ e.getClass().getName() + "(message : " + e.getMessage() + ")";
    		viewControle.addMessage(message);
    	}
    }

	public VObject getViewNorme() {
        return this.viewNorme;
    }

    public void setViewNorme(VObject viewNorme) {
        this.viewNorme = viewNorme;
    }

    public VObject getViewCalendrier() {
        return this.viewCalendrier;
    }

    public void setViewCalendrier(VObject viewCalendrier) {
        this.viewCalendrier = viewCalendrier;
    }

    public VObject getViewJeuxDeRegles() {
        return this.viewJeuxDeRegles;
    }

    public void setViewJeuxDeRegles(VObject viewJeuxDeRegles) {
        this.viewJeuxDeRegles = viewJeuxDeRegles;
    }

    public VObject getViewChargement() {
        return viewChargement;
    }

    public void setViewChargement(VObject viewChargement) {
        this.viewChargement = viewChargement;
    }

    public VObject getViewNormage() {
        return this.viewNormage;
    }

    public void setViewNormage(VObject viewNormage) {
        this.viewNormage = viewNormage;
    }

    public VObject getViewControle() {
        return this.viewControle;
    }

    public void setViewControle(VObject viewControle) {
        this.viewControle = viewControle;
    }

    public VObject getViewMapping() {
        return this.viewMapping;
    }

    public void setViewMapping(VObject viewMapping) {
        this.viewMapping = viewMapping;
    }

    public File getFileUpload() {
        return this.fileUpload;
    }

    public void setFileUpload(File fileUpload) {
        this.fileUpload = fileUpload;
    }

    public String getFileUploadContentType() {
        return this.fileUploadContentType;
    }

    public void setFileUploadContentType(String fileUploadContentType) {
        this.fileUploadContentType = fileUploadContentType;
    }

    public String getFileUploadFileName() {
        return this.fileUploadFileName;
    }

    public void setFileUploadFileName(String fileUploadFileName) {
        this.fileUploadFileName = fileUploadFileName;
    }

    public ControleRegleService getService() {
        return this.service;
    }

    public void setService(ControleRegleService service) {
        this.service = service;
    }

    public VObject getViewJeuxDeReglesCopie() {
        return this.viewJeuxDeReglesCopie;
    }

    public void setViewJeuxDeReglesCopie(VObject viewJeuxDeReglesCopie) {
        this.viewJeuxDeReglesCopie = viewJeuxDeReglesCopie;
    }

    /**
     * @return the viewFiltrage
     */
    public VObject getViewFiltrage() {
        return this.viewFiltrage;
    }

    /**
     * @param viewFiltrage
     *            the viewFiltrage to set
     */
    public void setViewFiltrage(VObject viewFiltrage) {
        this.viewFiltrage = viewFiltrage;
    }

    /**
     * @return the selectedJeuDeRegle
     */
    public String getSelectedJeuDeRegle() {
        return this.viewJeuxDeReglesCopie.getCustomValues().get(selectedJeuDeRegle);
    }

    /**
     * @param selectedJeuDeRegle
     *            the selectedJeuDeRegle to set
     */
    public void setSelectedJeuDeRegle(String selectedJeuDeRegle) {
        // this.selectedJeuDeRegle = selectedJeuDeRegle;
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

	public XsdControlDescription getControlDescription() {
		return controlDescription;
	}
	
	public XsdControlDescription getControlDescriptionFiltered() {
		return controlDescriptionFiltered;
	}
	
	public String getCurrentSelectedControlRule() {
		return currentSelectedControlRule;
	}
}