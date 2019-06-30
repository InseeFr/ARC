package fr.insee.arc.web.action;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.interceptor.SessionAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import fr.insee.arc.core.factory.ApiServiceFactory;
import fr.insee.arc.core.model.TraitementState;
import fr.insee.arc.core.model.TypeTraitementPhase;
import fr.insee.arc.core.service.AbstractPhaseService;
import fr.insee.arc.core.service.ApiInitialisationService;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.format.Format;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerDispatcher;
import fr.insee.arc.utils.utils.ManipString;
import fr.insee.arc.utils.utils.SQLExecutor;
import fr.insee.arc.web.model.ViewArchivePROD;
import fr.insee.arc.web.model.ViewEntrepotPROD;
import fr.insee.arc.web.model.ViewFichierPROD;
import fr.insee.arc.web.model.ViewPilotagePROD;
import fr.insee.arc.web.model.ViewRapportPROD;
import fr.insee.arc.web.util.VObject;

@Component
@Results({ @Result(name = "success", location = "/jsp/gererPilotagePROD.jsp"), @Result(name = "index", location = "/jsp/index.jsp") })
public class PilotagePRODAction implements SessionAware {

	private String envExecution="arc_PROD";
	
    @Override
    public void setSession(Map<String, Object> session) {
        this.viewEntrepotPROD.setMessage("");
        this.viewPilotagePROD.setMessage("");
        this.viewRapportPROD.setMessage("");
        this.viewFichierPROD.setMessage("");
        this.viewArchivePROD.setMessage("");
    }

    private static final Logger logger = Logger.getLogger(PilotagePRODAction.class);

    @Autowired
    @Qualifier("viewPilotagePROD")
    ViewPilotagePROD viewPilotagePROD;

    @Autowired
    @Qualifier("viewRapportPROD")
    ViewRapportPROD viewRapportPROD;

    @Autowired
    @Qualifier("viewFichierPROD")
    ViewFichierPROD viewFichierPROD;

    @Autowired
    @Qualifier("viewEntrepotPROD")
    ViewEntrepotPROD viewEntrepotPROD;

    @Autowired
    @Qualifier("viewArchivePROD")
    ViewArchivePROD viewArchivePROD;

    private String scope;

    private String repertoire ;

    public String sessionSyncronize() {

        this.viewPilotagePROD.setActivation(this.scope);
        this.viewRapportPROD.setActivation(this.scope);
        this.viewFichierPROD.setActivation(this.scope);
        this.viewEntrepotPROD.setActivation(this.scope);
        this.viewArchivePROD.setActivation(this.scope);

        Boolean defaultWhenNoScope = true;

        if (this.viewPilotagePROD.getIsScoped()) {
            initializePilotagePROD();
            defaultWhenNoScope = false;
        }

        if (this.viewRapportPROD.getIsScoped()) {
            initializeRapportPROD();
            defaultWhenNoScope = false;
        }

        if (this.viewFichierPROD.getIsScoped()) {
            initializeFichierPROD();
            defaultWhenNoScope = false;
        }

        if (this.viewEntrepotPROD.getIsScoped()) {
            initializeEntrepotPROD();
            defaultWhenNoScope = false;
        }
        if (this.viewArchivePROD.getIsScoped()) {
            initializeArchivePROD();
            defaultWhenNoScope = false;
        }

        if (defaultWhenNoScope) {
            System.out.println("default");
            this.viewPilotagePROD.setIsActive(true);
            this.viewPilotagePROD.setIsScoped(true);

            this.viewRapportPROD.setIsActive(true);
            this.viewRapportPROD.setIsScoped(true);

            this.viewEntrepotPROD.setIsActive(true);
            this.viewEntrepotPROD.setIsScoped(true);

            initializePilotagePROD();
            initializeRapportPROD();
            initializeEntrepotPROD();
        }

        return "success";

    }

    public void initializeEntrepotPROD() {
        System.out.println("/* initializeEntrepotPROD */");
        HashMap<String, String> defaultInputFields = new HashMap<String, String>();
        StringBuilder requete = new StringBuilder();

        try {
	        if (UtilitaireDao.get("arc").hasResults(null, FormatSQL.tableExists("arc.ihm_entrepot"))) {
	            requete.append("select id_entrepot from arc.ihm_entrepot");
	        } else {
	            requete.append("select ''::text as id_entrepot");
	        }
        } catch (Exception e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }

        // this.viewEntrepotPROD.setColumnRendering(ArcConstantVObjectGetter.columnRender.get(this.viewEntrepotPROD.getSessionName()));
        this.viewEntrepotPROD.initialize(requete.toString(), null, defaultInputFields);
    }

    // private SessionMap session;

    // visual des Pilotages du bac à sable
    public void initializePilotagePROD() {
        System.out.println("/* initializePilotagePROD */");
        HashMap<String, String> defaultInputFields = new HashMap<String, String>();
        StringBuilder requete = new StringBuilder();
        requete.append("select * from "+AbstractPhaseService.dbEnv(this.envExecution)+"pilotage_fichier_t order by date_entree desc");

        // this.viewPilotagePROD.setColumnRendering(ArcConstantVObjectGetter.columnRender.get(this.viewPilotagePROD.getSessionName()));
        this.viewPilotagePROD.initialize(requete.toString(), null, defaultInputFields);
    }

    @Action(value = "/selectPilotagePROD")
    public String selectPilotagePROD() {
        return sessionSyncronize();
    }

    @Action(value = "/sortPilotagePROD")
    public String sortPilotagePROD() {
        this.viewPilotagePROD.sort();
        return sessionSyncronize();

    }

    // visual des Pilotages du bac à sable
    public void initializeRapportPROD() {
        System.out.println("/* initializeRapportPROD */");
        HashMap<String, String> defaultInputFields = new HashMap<String, String>();
        StringBuilder requete = new StringBuilder();
        requete.append("select date_entree, phase_traitement, array_to_string(etat_traitement,'$') as etat_traitement, rapport, count(1) as nb ");
        requete.append("from "+AbstractPhaseService.dbEnv(this.envExecution)+"pilotage_fichier ");
        requete.append("where rapport is not null ");
        requete.append("group by date_entree, phase_traitement, etat_traitement, rapport ");
        // this.viewRapportPROD.setColumnRendering(ArcConstantVObjectGetter.columnRender.get(this.viewRapportPROD.getSessionName()));
        this.viewRapportPROD.initialize(requete.toString(), null, defaultInputFields);
    }

    @Action(value = "/selectRapportPROD")
    public String selectRapportPROD() {
        return sessionSyncronize();
    }

    @Action(value = "/sortRapportPROD")
    public String sortRapportPROD() {
        this.viewRapportPROD.sort();
        return sessionSyncronize();

    }

    // Actions du bac à sable

    @Action(value = "/filesUploadPROD")
    public String filesUploadPROD() {

        System.out.println("/* filesUploadPROD : " + this.viewEntrepotPROD.getCustomValues() + " */");

        if (this.viewEntrepotPROD.getCustomValues() != null && !this.viewEntrepotPROD.getCustomValues().get("entrepotEcriture").equals("")
                && this.viewPilotagePROD.getFileUploadFileName() != null) {
            String repertoireUpload = this.repertoire + "ARC_PROD" + File.separator + TypeTraitementPhase.REGISTER + "_"
                    + this.viewEntrepotPROD.getCustomValues().get("entrepotEcriture");

            this.viewPilotagePROD.upload(repertoireUpload);
        } else {
            String msg = "";
            if (this.viewPilotagePROD.getFileUploadFileName() == null) {
                msg = "Erreur : aucun fichier selectionné\n";
                this.viewPilotagePROD.setMessage("Erreur : aucun fichier selectionné.");
            }

            if (this.viewEntrepotPROD.getCustomValues() == null || this.viewEntrepotPROD.getCustomValues().get("entrepotEcriture").equals("")) {
                msg += "Erreur : aucun entrepot selectionné\n";
            }

            this.viewPilotagePROD.setMessage(msg);
        }
        this.viewEntrepotPROD.getCustomValues().put("entrepotEcriture", null);
        return sessionSyncronize();

    }

    /**
     * Initialisation de la vue sur la table contenant la liste des fichiers du répertoire d'archive
     */
    public void initializeArchivePROD() {
        System.out.println("initializeArchivePROD");
        if (this.viewEntrepotPROD.getCustomValues().containsKey("entrepotLecture")
                && !this.viewEntrepotPROD.getCustomValues().get("entrepotLecture").equals("")) {
            HashMap<String, String> defaultInputFields = new HashMap<String, String>();
            StringBuilder requete = new StringBuilder();

            requete.append("select * from "+AbstractPhaseService.dbEnv(this.envExecution)+"pilotage_archive where entrepot='"
                    + this.viewEntrepotPROD.getCustomValues().get("entrepotLecture") + "'");
            // this.viewArchivePROD.setColumnRendering(ArcConstantVObjectGetter.columnRender.get(this.viewArchivePROD.getSessionName()));
            this.viewArchivePROD.initialize(requete.toString(), null, defaultInputFields);
        } else {

            this.viewArchivePROD.destroy();
        }
    }

    /**
     * Fabrication d'une table temporaire avec comme contenu le nom des archives d'un entrepot donné puis Ouverture d'un VObject sur cette
     * table
     *
     * @return
     */
    @Action(value = "/visualiserEntrepotPROD")
    public String visualiserEntrepotPROD() {
        return sessionSyncronize();

    }

    /**
     * Téléchargement d'enveloppe contenu dans le dossier d'archive
     *
     * @return
     */
    @Action(value = "/downloadEnveloppeFromArchivePROD")
    @SQLExecutor
    public String downloadEnveloppeFromArchivePROD() {
        LoggerDispatcher.trace("*** Téléchargement des enveloppes à partir de l'archive ***", logger);
        // récupération de la liste des noms d'enloppe
        Map<String, ArrayList<String>> selection = this.viewArchivePROD.mapContentSelected();

        StringBuilder querySelection = new StringBuilder();
        querySelection.append("select distinct alias_de_table.nom_archive as nom_fichier from (" + this.viewArchivePROD.getMainQuery()
                + ") alias_de_table ");
        querySelection.append(this.viewArchivePROD.buildFilter(this.viewArchivePROD.getFilterFields(), this.viewArchivePROD.getDatabaseColumnsLabel()));

        if (!selection.isEmpty()) {
            querySelection.append(" AND nom_archive IN " + Format.sqlListe(selection.get("nom_archive")) + " ");
        }

        LoggerDispatcher.info("Ma requete pour récupérer la liste des enveloppes : " + querySelection.toString(), logger);

        ArrayList<String> listRepertoire = new ArrayList<>();
        GenericBean g;
        String entrepot = "";
        try {
            g = new GenericBean(UtilitaireDao.get("arc").executeRequest(null,
                    "SELECT DISTINCT entrepot FROM (" + this.viewArchivePROD.getMainQuery() + ") alias_de_table "));
            entrepot = g.mapContent().get("entrepot").get(0);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        listRepertoire.add(TypeTraitementPhase.REGISTER + "_" + entrepot + "_ARCHIVE");
        String chemin = this.repertoire + File.separator + "ARC_PROD";
        this.viewArchivePROD.downloadEnveloppe(querySelection.toString(), chemin, listRepertoire);
        return "none";
    }

    @Action(value = "/informationInitialisationPROD")
    public String informationInitialisationPROD() {
    	
    	// demande l'initialisation : met au jour -1 à 22h
    	try {
			String heure=UtilitaireDao.get("arc").getString(null, "SELECT last_init from arc.pilotage_batch;");
			String etat=UtilitaireDao.get("arc").getString(null, "SELECT case when operation='O' then 'actif' else 'inactif' end from arc.pilotage_batch;");
			
    		viewPilotagePROD.setMessage("Le batch est "+etat+".\nLe prochain batch d'initialisation est programmé aprés : "+heure);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return sessionSyncronize();
    }

    @Action(value = "/retarderBatchInitialisationPROD")
    @SQLExecutor
    public String retarderBatchInitialisationPROD() {
    	
    	// demande l'initialisation : met au jour -1 à 22h
    	try {
			UtilitaireDao.get("arc").executeRequest(null, "UPDATE arc.pilotage_batch set last_init=to_char(current_date + interval '7 days','yyyy-mm-dd')||':22';");

			String heure=UtilitaireDao.get("arc").getString(null, "SELECT last_init from arc.pilotage_batch;");
    		viewPilotagePROD.setMessage("Le prochain batch d'initialisation aura lieu ce soir après : "+heure);

			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
        return sessionSyncronize();
    }
    
    @Action(value = "/demanderBatchInitialisationPROD")
    @SQLExecutor
    public String demanderBatchInitialisationPROD() {
    	
    	// demande l'initialisation : met au jour -1 à 22h
    	try {
			UtilitaireDao.get("arc").executeRequest(null, "UPDATE arc.pilotage_batch set last_init=to_char(current_date-interval '1 days','yyyy-mm-dd')||':22';");
			
			String heure=UtilitaireDao.get("arc").getString(null, "SELECT last_init from arc.pilotage_batch;");
	    	viewPilotagePROD.setMessage("Le prochain batch d'initialisation aura lieu dans quelques minutes (après "+heure+") ");

			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
        return sessionSyncronize();
    }
    
    @Action(value = "/toggleOnPROD")
    @SQLExecutor
    public String toggleOnPROD() {
    	
    	// demande l'initialisation : met au jour -1 à 22h
    	try {
			UtilitaireDao.get("arc").executeRequest(null, "UPDATE arc.pilotage_batch set operation='O'; ");
    		viewPilotagePROD.setMessage("Production activée ");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    	
        return sessionSyncronize();
    }

    @Action(value = "/toggleOffPROD")
    @SQLExecutor
    public String toggleOffPROD() {
    	
    	// demande l'initialisation : met au jour -1 à 22h
    	try {
			UtilitaireDao.get("arc").executeRequest(null, "UPDATE arc.pilotage_batch set operation='N'; ");
    		viewPilotagePROD.setMessage("Production arretée ");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    	
        return sessionSyncronize();
    }
    
    
    @Action(value = "/startInitialisationPROD")
    public String startInitialisationPROD() {
        // String repertoire=ServletActionContext.getServletContext().getRealPath("/");
        
    	ApiServiceFactory.getService(TypeTraitementPhase.INITIALIZE.toString(), "arc.ihm", this.envExecution, this.repertoire,
                String.valueOf(TypeTraitementPhase.INITIALIZE.getNbLinesToProcess()),new SimpleDateFormat("yyyyMMddHH").format(new Date())).invokeApi();
        return sessionSyncronize();
    }

    @Action(value = "/startReceptionPROD")
    public String startReceptionPROD() {
        LoggerDispatcher.trace("startChargementPROD", logger);
        ApiInitialisationService.copyTablesToExecution(null, "arc.ihm", this.envExecution);

        // String repertoire=ServletActionContext.getServletContext().getRealPath("/");
        ApiServiceFactory.getService(TypeTraitementPhase.REGISTER.toString(), "arc.ihm", this.envExecution, this.repertoire,
                "100",new SimpleDateFormat("yyyyMMddHH").format(new Date())).invokeApi();
        return sessionSyncronize();
    }
    
    @Action(value = "/startChargementPROD")
    public String startChargementPROD() {
        LoggerDispatcher.trace("startChargementPROD", logger);
        ApiInitialisationService.copyTablesToExecution(null, "arc.ihm", this.envExecution);

        // String repertoire=ServletActionContext.getServletContext().getRealPath("/");
        ApiServiceFactory.getService(TypeTraitementPhase.LOAD.toString(), "arc.ihm", this.envExecution, this.repertoire,
                String.valueOf(TypeTraitementPhase.LOAD.getNbLinesToProcess()),new SimpleDateFormat("yyyyMMddHH").format(new Date())).invokeApi();
        return sessionSyncronize();
    }
    
    @Action(value = "/startIdentificationPROD")
    public String startIdentificationPROD() {
        LoggerDispatcher.trace("startChargementBAS1", logger);
        ApiInitialisationService.synchroniserSchemaExecution(null, "arc.ihm", this.envExecution);

        // String repertoire=ServletActionContext.getServletContext().getRealPath("/");
        ApiServiceFactory.getService(TypeTraitementPhase.IDENTIFY.toString(), "arc.ihm", this.envExecution, this.repertoire,
                String.valueOf(TypeTraitementPhase.IDENTIFY.getNbLinesToProcess()),new SimpleDateFormat("yyyyMMddHH").format(new Date())).invokeApi();
        return sessionSyncronize();
    }

    @Action(value = "/startNormagePROD")
    public String startNormagePROD() {
        ApiInitialisationService.copyTablesToExecution(null, "arc.ihm", this.envExecution);

        // String repertoire=ServletActionContext.getServletContext().getRealPath("/");
        ApiServiceFactory.getService(TypeTraitementPhase.STRUCTURIZE_XML.toString(), "arc.ihm", this.envExecution, this.repertoire,
                String.valueOf(TypeTraitementPhase.STRUCTURIZE_XML.getNbLinesToProcess()),new SimpleDateFormat("yyyyMMddHH").format(new Date())).invokeApi();
        return sessionSyncronize();
    }

    @Action(value = "/startControlePROD")
    public String startControlePROD() {
        ApiInitialisationService.copyTablesToExecution(null, "arc.ihm", this.envExecution);

        // String repertoire=ServletActionContext.getServletContext().getRealPath("/");
        ApiServiceFactory.getService(TypeTraitementPhase.CONTROL.toString(), "arc.ihm", this.envExecution, this.repertoire,
                String.valueOf(TypeTraitementPhase.CONTROL.getNbLinesToProcess()),new SimpleDateFormat("yyyyMMddHH").format(new Date())).invokeApi();
        return sessionSyncronize();
    }

    @Action(value = "/startFiltragePROD")
    public String startFiltragePROD() {
        ApiInitialisationService.copyTablesToExecution(null, "arc.ihm", this.envExecution);

        // String repertoire=ServletActionContext.getServletContext().getRealPath("/");
        ApiServiceFactory.getService(TypeTraitementPhase.FILTER.toString(), "arc.ihm", this.envExecution, this.repertoire,
                String.valueOf(TypeTraitementPhase.FILTER.getNbLinesToProcess()),new SimpleDateFormat("yyyyMMddHH").format(new Date())).invokeApi();
        return sessionSyncronize();
    }

    @Action(value = "/startMappingPROD")
    public String startMappingPROD() {
        ApiInitialisationService.copyTablesToExecution(null, "arc.ihm", this.envExecution);

        // String repertoire=ServletActionContext.getServletContext().getRealPath("/");
        ApiServiceFactory.getService(TypeTraitementPhase.FORMAT_TO_MODEL.toString(), "arc.ihm", this.envExecution, this.repertoire,
                String.valueOf(TypeTraitementPhase.FORMAT_TO_MODEL.getNbLinesToProcess()),new SimpleDateFormat("yyyyMMddHH").format(new Date())).invokeApi();
        return sessionSyncronize();
    }

    // // Bouton undo
    // @Action(value = "/undoChargementPROD")
    // public String undoChargementPROD() {
    // // String repertoire=ServletActionContext.getServletContext().getRealPath("/");
    // ApiInitialisationService serv = new ApiInitialisationService(TraitementPhase.INITIALISATION.toString(), "arc.ihm", this.envExecution,
    // this.repertoire, 100000);
    // serv.retourPhasePrecedente(TraitementPhase.CHARGEMENT, null,
    // new ArrayList<TraitementEtat>(Arrays.asList(TraitementEtat.OK, TraitementEtat.KO)));
    // return sessionSyncronize();
    // }
    //
    // @Action(value = "/undoNormagePROD")
    // public String undoNormagePROD() {
    // // String repertoire=ServletActionContext.getServletContext().getRealPath("/");
    // ApiInitialisationService serv = new ApiInitialisationService(TraitementPhase.INITIALISATION.toString(), "arc.ihm", this.envExecution,
    // this.repertoire, 100000);
    // serv.retourPhasePrecedente(TraitementPhase.NORMAGE, null, new ArrayList<TraitementEtat>(Arrays.asList(TraitementEtat.OK,
    // TraitementEtat.KO)));
    // return sessionSyncronize();
    // }
    //
    // @Action(value = "/undoControlePROD")
    // public String undoControlePROD() {
    // // String repertoire=ServletActionContext.getServletContext().getRealPath("/");
    // ApiInitialisationService serv = new ApiInitialisationService(TraitementPhase.INITIALISATION.toString(), "arc.ihm", this.envExecution,
    // this.repertoire, 100000);
    // serv.retourPhasePrecedente(TraitementPhase.CONTROLE, null, new ArrayList<TraitementEtat>(Arrays.asList(TraitementEtat.OK,
    // TraitementEtat.KO)));
    // return sessionSyncronize();
    // }
    //
    // @Action(value = "/undoFiltragePROD")
    // public String undoFiltragePROD() {
    // // String repertoire=ServletActionContext.getServletContext().getRealPath("/");
    // ApiInitialisationService serv = new ApiInitialisationService(TraitementPhase.INITIALISATION.toString(), "arc.ihm", this.envExecution,
    // this.repertoire, 100000);
    // serv.retourPhasePrecedente(TraitementPhase.FILTRAGE, null, new ArrayList<TraitementEtat>(Arrays.asList(TraitementEtat.OK,
    // TraitementEtat.KO)));
    // return sessionSyncronize();
    // }
    //
    // @Action(value = "/undoMappingPROD")
    // public String undoMappingPROD() {
    // // String repertoire=ServletActionContext.getServletContext().getRealPath("/");
    // ApiInitialisationService serv = new ApiInitialisationService(TraitementPhase.INITIALISATION.toString(), "arc.ihm", this.envExecution,
    // this.repertoire, 100000);
    // serv.retourPhasePrecedente(TraitementPhase.MAPPING, null, new ArrayList<TraitementEtat>(Arrays.asList(TraitementEtat.OK,
    // TraitementEtat.KO)));
    // return sessionSyncronize();
    // }

    // @Action(value = "/resetPROD")
    // public String resetPROD() {
    // ApiInitialisationService.clearPilotageAndDirectories(this.repertoire, this.envExecution);
    // return sessionSyncronize();
    // }

//    @Action(value = "/resetPROD")
//    public String resetPROD() {
//        try {
//            ApiInitialisationService.clearPilotageAndDirectories(this.repertoire, this.envExecution);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            viewPilotagePROD.setMessage("Problème : " + e.getMessage());
//        }
//        ApiInitialisationService service = new ApiInitialisationService(TraitementPhase.INITIALISATION.toString(), "arc.ihm", this.envExecution,
//                this.repertoire, TraitementPhase.INITIALISATION.getNbLigneATraiter());
//        try {
//            service.resetEnvironnement();
//        } finally {
//            service.finaliser();
//        }
//        return sessionSyncronize();
//    }

    // visual des Fichiers
    public void initializeFichierPROD() {
        Map<String, ArrayList<String>> selectionLigne = this.viewPilotagePROD.mapContentSelected();
        ArrayList<String> selectionColonne = this.viewPilotagePROD.listHeadersSelected();

        Map<String, ArrayList<String>> selectionLigneRapport = this.viewRapportPROD.mapContentSelected();

        if (!selectionLigne.isEmpty() && !selectionColonne.isEmpty()) {
            System.out.println("/* initializeFichierPROD */");

            HashMap<String, String> defaultInputFields = new HashMap<>();

            String phase = selectionColonne.get(0).split("_")[0].toUpperCase();
            String etat = selectionColonne.get(0).split("_")[1].toUpperCase();

            StringBuilder requete = new StringBuilder();
            requete.append("select container, id_source,id_norme,validite,periodicite,phase_traitement,array_to_string(etat_traitement,'_') as etat_traitement ,date_traitement, rapport, round(taux_ko*100,2) as taux_ko, nb_enr, to_delete, jointure from "+AbstractPhaseService.dbEnv(this.envExecution)+"pilotage_fichier ");
            requete.append("where date_entree" + ManipString.sqlEqual(selectionLigne.get("date_entree").get(0), "text"));
            requete.append(" and array_to_string(etat_traitement,'$')" + ManipString.sqlEqual(etat, "text"));
            requete.append(" and phase_traitement" + ManipString.sqlEqual(phase, "text"));

            // this.viewFichierPROD.setColumnRendering(ArcConstantVObjectGetter.columnRender.get(this.viewFichierPROD.getSessionName()));
            this.viewFichierPROD.initialize(requete.toString(), null, defaultInputFields);
        } else if (!selectionLigneRapport.isEmpty()) {
            System.out.println("/* initializeFichierPROD */");

            HashMap<String, String> type = this.viewRapportPROD.mapHeadersType();
            HashMap<String, String> defaultInputFields = new HashMap<String, String>();

            StringBuilder requete = new StringBuilder();
            requete.append("select container, id_source,id_norme,validite,periodicite,phase_traitement,array_to_string(etat_traitement,'_') as etat_traitement ,date_traitement, rapport, round(taux_ko*100,2) as taux_ko, nb_enr, to_delete, jointure from "+AbstractPhaseService.dbEnv(this.envExecution)+"pilotage_fichier ");
            requete.append("where date_entree" + ManipString.sqlEqual(selectionLigneRapport.get("date_entree").get(0), "text"));
            requete.append(" and array_to_string(etat_traitement,'$')"
                    + ManipString.sqlEqual(selectionLigneRapport.get("etat_traitement").get(0), type.get("etat_traitement")));
            requete.append(" and phase_traitement"
                    + ManipString.sqlEqual(selectionLigneRapport.get("phase_traitement").get(0), type.get("phase_traitement")));
            requete.append(" and rapport" + ManipString.sqlEqual(selectionLigneRapport.get("rapport").get(0), type.get("rapport")));

            // this.viewFichierPROD.setColumnRendering(ArcConstantVObjectGetter.columnRender.get(this.viewFichierPROD.getSessionName()));
            this.viewFichierPROD.initialize(requete.toString(), null, defaultInputFields);
        } else {
            this.viewFichierPROD.destroy();
        }
    }

    @Action(value = "/selectFichierPROD")
    public String selectFichierPROD() {
        return sessionSyncronize();
    }

    @Action(value = "/sortFichierPROD")
    public String sortFichierPROD() {
        this.viewFichierPROD.sort();
        return sessionSyncronize();

    }

    @Action(value = "/downloadFichierPROD")
    public String downloadFichierPROD() {
        LoggerDispatcher.trace("*** Téléchargement des fichiers ***", logger);
        // récupération de la liste des id_source

        Map<String, ArrayList<String>> selection = this.viewFichierPROD.mapContentSelected();
        StringBuilder querySelection = this.viewFichierPROD.queryView();
        // si la selection de fichiers n'est pas vide, on se restreint aux fichiers sélectionner
        //
        if (!selection.isEmpty()) {
            querySelection.append(" AND id_source IN " + Format.sqlListe(selection.get("id_source")) + " ");
        }

        // optimisation pour avoir des bloc successifs sur la même archive
        querySelection.append(" order by container ");

        // String repertoire= ServletActionContext.getServletContext().getRealPath("/");
        this.viewFichierPROD.downloadXML(querySelection.toString(), this.repertoire, this.envExecution, TypeTraitementPhase.REGISTER.toString(),
                TraitementState.OK.toString(), TraitementState.KO.toString());

        LoggerDispatcher.trace("*** Fin du téléchargement des fichiers XML ***", logger);
        sessionSyncronize();
        return "none";
    }

    @Action(value = "/downloadBdPROD")
    public String downloadBdPROD() {

        Map<String, ArrayList<String>> selectionLigne = this.viewPilotagePROD.mapContentSelected();
        ArrayList<String> selectionColonne = this.viewPilotagePROD.listHeadersSelected();

        String phase = selectionColonne.get(0).split("_")[0].toUpperCase();
        String etat = selectionColonne.get(0).split("_")[1].toUpperCase();
        String date = selectionLigne.get("date_entree").get(0);

        String[] etatList = etat.split("\\$");
        String etatBdd = "{" + etat.replace("$", ",") + "}";

        // Sélection des table métiers en fonction de la phase sélectionner (5 pour mapping 1 sinon)
        ApiInitialisationService serv = new ApiInitialisationService();
        ArrayList<String> tableDownload = new ArrayList<>();
        try {
            GenericBean g = new GenericBean(UtilitaireDao.get("arc").executeRequest(null, serv.requeteListAllTablesEnv(this.envExecution)));
            if (!g.mapContent().isEmpty()) {
                ArrayList<String> envTables = g.mapContent().get("table_name");
                System.out.println("Le contenu de ma envTables : " + envTables);
                for (String table : envTables) {
                    // selection des tables qui contiennent la phase dans leur nom
                    if (table.toUpperCase().contains(phase.toUpperCase())) {
                        // ajout uniquement si pas déjà présent (pour éviter les doublons dû à table OK et KO
                        if (!tableDownload.contains(ManipString.substringBeforeLast(table, "_"))) {
                            tableDownload.add(ManipString.substringBeforeLast(table, "_"));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("Le contenu de ma tableDownload : " + tableDownload + "pour la phase : " + phase);

        String tableauRequete[] = new String[tableDownload.size() + 3];
        for (int k = 0; k < tableDownload.size(); k++) {
            // Début de la requete sur les données de la phase
            StringBuilder requete = new StringBuilder();
            requete.append("with prep as ( ");
            requete.append("select id_source, date_entree from "+AbstractPhaseService.dbEnv(this.envExecution)+"pilotage_fichier ");
            requete.append("where phase_traitement='" + phase + "' ");
            requete.append("AND etat_traitement='" + etatBdd + "' ");
            requete.append("AND date_entree='" + date + "' ");

            // Si des fichiers ont été selectionnés, on ajoute a la requete la liste des fichiers
            if (!this.viewFichierPROD.mapContentSelected().isEmpty()) {
                ArrayList<String> filesSelected = this.viewFichierPROD.mapContentSelected().get("id_source");
                requete.append("AND id_source IN (");
                for (int i = 0; i < filesSelected.size(); i++) {
                    if (i > 0) {
                        requete.append(",");
                    }
                    requete.append("'" + filesSelected.get(i) + "'");
                }
                requete.append(")");
            }
            requete.append(" ) ");
            requete.append("select * from ( ");

            for (int i = 0; i < etatList.length; i++) {
                if (i > 0) {
                    requete.append("UNION ALL ");
                }
                requete.append("select * from " + tableDownload.get(k) + "_" + etatList[i]
                        + " a where exists (select 1 from prep b where a.id_source=b.id_source) ");
            }
            requete.append(") u ");
            System.out.println(requete);
            tableauRequete[k] = requete.toString();
            // fin de la requete pour les données d'une des tables
        }

        // Récupération des règles appliquées sur ces fichiers à télécharger
        StringBuilder requeteRegleC = new StringBuilder();
        requeteRegleC.append(recupRegle("CONTROLE", AbstractPhaseService.dbEnv(this.envExecution)+"controle_regle"));
        tableauRequete[tableauRequete.length - 3] = requeteRegleC.toString();
        StringBuilder requeteRegleM = new StringBuilder();
        requeteRegleM.append(recupRegle("MAPPING", AbstractPhaseService.dbEnv(this.envExecution)+"mapping_regle"));
        tableauRequete[tableauRequete.length - 2] = requeteRegleM.toString();
        StringBuilder requeteRegleF = new StringBuilder();
        requeteRegleF.append(recupRegle("FILTRAGE", AbstractPhaseService.dbEnv(this.envExecution)+"filtrage_regle"));
        tableauRequete[tableauRequete.length - 1] = requeteRegleF.toString();
        // Pour donner des noms aux fichiers csv
        ArrayList<String> fileNames = new ArrayList<>();
        for (int k = 0; k < tableDownload.size(); k++) {
            fileNames.add("Data_" + ManipString.substringAfterFirst(tableDownload.get(k), "_") + "_" + date);
        }
        fileNames.add("Regles_controle");
        fileNames.add("Regles_mapping");
        fileNames.add("Regles_filtrage");
        this.viewFichierPROD.download(fileNames, tableauRequete);

        return "none";

    }

    @Action(value = "/downloadEnveloppePROD")
    public String downloadEnveloppePROD() {
        LoggerDispatcher.trace("*** Téléchargement des enveloppes ***", logger);
        // récupération de la liste des noms d'enloppe
        Map<String, ArrayList<String>> selection = this.viewFichierPROD.mapContentSelected();

        StringBuilder querySelection = new StringBuilder();
        querySelection.append("select distinct alias_de_table.container as nom_fichier from (" + this.viewFichierPROD.getMainQuery()
                + ") alias_de_table ");
        querySelection.append(this.viewFichierPROD.buildFilter(this.viewFichierPROD.getFilterFields(), this.viewFichierPROD.getDatabaseColumnsLabel()));

        if (!selection.isEmpty()) {
            querySelection.append(" AND container IN " + Format.sqlListe(selection.get("container")) + " ");
        }

        LoggerDispatcher.info("Ma requete pour récupérer la liste des enveloppes : " + querySelection.toString(), logger);

        ArrayList<String> listRepertoire = new ArrayList<>();
        listRepertoire.add(TypeTraitementPhase.REGISTER + "_" + TraitementState.OK);
        listRepertoire.add(TypeTraitementPhase.REGISTER + "_" + TraitementState.KO);
        String chemin = this.repertoire + File.separator + "ARC_PROD";
        this.viewFichierPROD.downloadEnveloppe(querySelection.toString(), chemin, listRepertoire);
        LoggerDispatcher.trace("*** Fin du téléchargement des enveloppes ***", logger);

        return "none";
    }

    /**
     * Marquage de fichier pour suppression lors de la prochaine initialisation
     *
     * @return
     */
    @Action(value = "/toDeletePROD")
    public String toDeletePROD() {
        LoggerDispatcher.trace("*** Marquage de fichier à supprimer ***", logger);
        Map<String, ArrayList<String>> selection = this.viewFichierPROD.mapContentSelected();
        // System.out.println(selection);

        // Récupération de la sélection de l'utilisateur
        StringBuilder querySelection = new StringBuilder();
        querySelection.append("select distinct container, id_source from (" + this.viewFichierPROD.getMainQuery() + ") alias_de_table ");
        querySelection.append(this.viewFichierPROD.buildFilter(this.viewFichierPROD.getFilterFields(), this.viewFichierPROD.getDatabaseColumnsLabel()));
        // si la selection de fichiers n'est pas vide, on se restreint aux fichiers sélectionné
        if (!selection.isEmpty()) {
            // concaténation des informations
            ArrayList<String> infoConcatenee = new ArrayList<>();
            ArrayList<String> listContainer = selection.get("container");
            ArrayList<String> listIdSource = selection.get("id_source");

            for (int i = 0; i < selection.get("id_source").size(); i++) {
                infoConcatenee.add(listContainer.get(i) + "+" + listIdSource.get(i));
            }
            querySelection.append(" AND container||'+'||id_source IN " + Format.sqlListe(infoConcatenee) + " ");
        }
        // LoggerDispatcher.info("Ma requete de selection : " + querySelection, logger);

        StringBuilder updateToDelete = requeteUpdateToDelete(querySelection, "'1'");
        String message;
        try {

            UtilitaireDao.get("arc").executeImmediate(null, updateToDelete);
			AbstractPhaseService.startProductionInitialization();
            message = "Fichier(s) supprimé(s)";
        } catch (SQLException e) {
            LoggerDispatcher
                    .info("Problème lors de la mise à jour de to_delete dans la table pilotage_fichier, requete :  " + updateToDelete, logger);
            e.printStackTrace();
            message = "Problème lors de la suppression des fichiers";
        }
        this.viewPilotagePROD.setMessage(message);

        return sessionSyncronize();
    }

    /**
     * Suppression du marquage de fichier pour suppression lors de la prochaine initialisation
     *
     * @return
     */
    @Action(value = "/undoActionPROD")
    public String undoActionPROD() {
        LoggerDispatcher.trace("*** Suppression du marquage de fichier à supprimer ***", logger);
        Map<String, ArrayList<String>> selection = this.viewFichierPROD.mapContentSelected();
        // Récupération de la sélection de l'utilisateur
        StringBuilder querySelection = new StringBuilder();
        querySelection.append("select distinct container, id_source from (" + this.viewFichierPROD.getMainQuery() + ") alias_de_table ");
        querySelection.append(this.viewFichierPROD.buildFilter(this.viewFichierPROD.getFilterFields(), this.viewFichierPROD.getDatabaseColumnsLabel()));
        // si la selection de fichiers n'est pas vide, on se restreint aux fichiers sélectionné
        if (!selection.isEmpty()) {
            // concaténation des informations
            ArrayList<String> infoConcatenee = new ArrayList<>();
            ArrayList<String> listContainer = selection.get("container");
            ArrayList<String> listIdSource = selection.get("id_source");

            for (int i = 0; i < selection.get("id_source").size(); i++) {
                infoConcatenee.add(listContainer.get(i) + "+" + listIdSource.get(i));
            }
            querySelection.append(" AND container||'+'||id_source IN " + Format.sqlListe(infoConcatenee) + " ");
        }
        // LoggerDispatcher.info("Ma requete de selection : " + querySelection, logger);

        StringBuilder updateToDelete = requeteUpdateToDelete(querySelection, "null");
        try {

            UtilitaireDao.get("arc").executeImmediate(null, updateToDelete);
        } catch (SQLException e) {
            LoggerDispatcher
                    .info("Problème lors de la mise à jour de to_delete dans la table pilotage_fichier, requete :  " + updateToDelete, logger);
            e.printStackTrace();
        }
        return sessionSyncronize();
    }

   
    /**
     * Marquage de fichier pour suppression lors de la prochaine initialisation
     *
     * @return
     */
    @Action(value = "/toRestorePROD")
    public String toRestorePROD() {
        LoggerDispatcher.trace("*** Marquage de fichier à remplacer ***", logger);
        Map<String, ArrayList<String>> selection = this.viewFichierPROD.mapContentSelected();
        // System.out.println(selection);

        // Récupération de la sélection de l'utilisateur
        StringBuilder querySelection = new StringBuilder();
        querySelection.append("select distinct container, id_source from (" + this.viewFichierPROD.getMainQuery() + ") alias_de_table ");
        querySelection.append(this.viewFichierPROD.buildFilter(this.viewFichierPROD.getFilterFields(), this.viewFichierPROD.getDatabaseColumnsLabel()));
        // si la selection de fichiers n'est pas vide, on se restreint aux fichiers sélectionnés
        if (!selection.isEmpty()) {
            // concaténation des informations
            ArrayList<String> infoConcatenee = new ArrayList<>();
            ArrayList<String> listContainer = selection.get("container");
            ArrayList<String> listIdSource = selection.get("id_source");

            for (int i = 0; i < selection.get("id_source").size(); i++) {
                infoConcatenee.add(listContainer.get(i) + "+" + listIdSource.get(i));
            }
            querySelection.append(" AND container||'+'||id_source IN " + Format.sqlListe(infoConcatenee) + " ");
        }
        // LoggerDispatcher.info("Ma requete de selection : " + querySelection, logger);

        StringBuilder updateToDelete = requeteUpdateToDelete(querySelection, "'R'");
        String message;
        try {

            UtilitaireDao.get("arc").executeImmediate(null, updateToDelete);
			AbstractPhaseService.startProductionInitialization();
            message = "Fichier(s) à rejouer";
        } catch (SQLException e) {
            LoggerDispatcher
                    .info("Problème lors de la mise à jour de to_delete dans la table pilotage_fichier, requete :  " + updateToDelete, logger);
            e.printStackTrace();
            message = "Problème lors de la restauration des fichiers";
        }

        this.viewPilotagePROD.setMessage(message);

        return sessionSyncronize();
    }
    
    /**
     * Marquage des archives à rejouer lors de la prochaine initialisation
     *
     * @return
     */
    @Action(value = "/toRestoreArchivePROD")
    public String toRestoreArchivePROD() {
        LoggerDispatcher.trace("*** Marquage de fichier à rejouer ***", logger);
        Map<String, ArrayList<String>> selection = this.viewFichierPROD.mapContentSelected();
        // System.out.println(selection);

        // Récupération de la sélection de l'utilisateur
        StringBuilder querySelection = new StringBuilder();
        querySelection.append("select distinct container, id_source from (" + this.viewFichierPROD.getMainQuery() + ") alias_de_table ");
        querySelection.append(this.viewFichierPROD.buildFilter(this.viewFichierPROD.getFilterFields(), this.viewFichierPROD.getDatabaseColumnsLabel()));
        // si la selection de fichiers n'est pas vide, on se restreint aux fichiers sélectionnés
        if (!selection.isEmpty()) {
            // concaténation des informations
            ArrayList<String> infoConcatenee = new ArrayList<>();
            ArrayList<String> listContainer = selection.get("container");
            ArrayList<String> listIdSource = selection.get("id_source");

            for (int i = 0; i < selection.get("id_source").size(); i++) {
                infoConcatenee.add(listContainer.get(i) + "+" + listIdSource.get(i));
            }
            querySelection.append(" AND container||'+'||id_source IN " + Format.sqlListe(infoConcatenee) + " ");
        }
        // LoggerDispatcher.info("Ma requete de selection : " + querySelection, logger);

        StringBuilder updateToDelete = requeteUpdateToDelete(querySelection, "'RA'");
        String message;
        try {

            UtilitaireDao.get("arc").executeImmediate(null, updateToDelete);
			AbstractPhaseService.startProductionInitialization();
            message = "Archives(s) à rejoués(s)";
        } catch (SQLException e) {
            LoggerDispatcher
                    .info("Problème lors de la mise à jour de to_delete dans la table pilotage_fichier, requete :  " + updateToDelete, logger);
            e.printStackTrace();
            message = "Problème lors de la restauration des fichiers";
        }
        
        this.viewPilotagePROD.setMessage(message);

        return sessionSyncronize();
    }
    
    

    private StringBuilder requeteUpdateToDelete(StringBuilder querySelection, String valeur) {
        StringBuilder updateToDelete = new StringBuilder();
        updateToDelete.append("WITH ");
        updateToDelete.append("prep AS ( ");
        updateToDelete.append(querySelection);
        updateToDelete.append("         ) ");
        updateToDelete.append("UPDATE "+AbstractPhaseService.dbEnv(this.envExecution)+"pilotage_fichier a ");
        updateToDelete.append("SET to_delete=" + valeur + " ");
        updateToDelete.append("WHERE EXISTS (SELECT 1 FROM prep WHERE a.container=prep.container AND a.id_source=prep.id_source); ");
        return updateToDelete;
    }

    /**
     * Méthode qui écrit la requete SQL permettant de récuperer les règles (controle, mapping ou filtrage) appliquées à un fichier
     *
     * @param phase
     *            , CONTROLE ou MAPPING (la phase de filtrage n'existe pas elle est dans MAPPING)
     * @param table
     * @return
     */
    private String recupRegle(String phase, String table) {
        StringBuilder requete = new StringBuilder();
        requete.append("WITH ");
        requete.append("prep as (SELECT DISTINCT id_norme, periodicite, validite_inf, validite_sup, version ");
        requete.append("    FROM "+AbstractPhaseService.dbEnv(this.envExecution)+"pilotage_fichier ");
        requete.append("    WHERE phase_traitement in('" + phase + "') AND rapport is null ");
        if (!this.viewFichierPROD.mapContentSelected().isEmpty()) {
            ArrayList<String> filesSelected = this.viewFichierPROD.mapContentSelected().get("id_source");
            requete.append("AND id_source IN (");
            for (int i = 0; i < filesSelected.size(); i++) {
                if (i > 0) {
                    requete.append(",");
                }
                requete.append("'" + filesSelected.get(i) + "'");
            }
            requete.append(")");
        }
        requete.append("        ) ");// fin du WITH prep
        requete.append("SELECT a.* ");
        requete.append("FROM " + table + " a ");
        requete.append("    INNER JOIN prep  ");
        requete.append("        ON a.id_norme=prep.id_norme ");
        requete.append("        AND a.periodicite=prep.periodicite ");
        requete.append("        AND a.validite_inf=prep.validite_inf ");
        requete.append("        AND a.validite_sup=prep.validite_sup ");
        requete.append("        AND a.version=prep.version ");
        LoggerDispatcher.trace("La requete portant sur : " + table + ", " + requete.toString(), logger);
        return requete.toString();
    }

    /**
     * retour arriere d'une phase
     *
     * @return
     */
    @Action(value = "/resetPhasePROD")
    public String resetPhasePROD() {
        Map<String, ArrayList<String>> selection = this.viewFichierPROD.mapContentSelected();
        StringBuilder querySelection = this.viewFichierPROD.queryView();

        // si la selection de fichiers n'est pas vide, on se restreint aux fichiers choisis pour le retour arriere
        //
        if (!selection.isEmpty()) {
            querySelection.append(" AND id_source IN " + Format.sqlListe(selection.get("id_source")) + " ");
        }

        // On recupere la phase
        String phase = this.viewFichierPROD.mapContent().get("phase_traitement").get(0);

        // Lancement du retour arrière
        // String repertoire=ServletActionContext.getServletContext().getRealPath("/");
        ApiInitialisationService serv = new ApiInitialisationService(TypeTraitementPhase.INITIALIZE.toString(), "arc.ihm", this.envExecution,
                this.repertoire, 100000);
        try {
            serv.backToPreviousPhase(TypeTraitementPhase.valueOf(phase), querySelection.toString(), null);
        } finally {
            serv.finalizePhase();
        }
        return sessionSyncronize();
    }

    public VObject getViewPilotagePROD() {
        return this.viewPilotagePROD;
    }

    public void setViewPilotagePROD(ViewPilotagePROD viewPilotagePROD) {
        this.viewPilotagePROD = viewPilotagePROD;
    }

    public VObject getViewRapportPROD() {
        return this.viewRapportPROD;
    }

    public void setViewRapportPROD(ViewRapportPROD viewRapportPROD) {
        this.viewRapportPROD = viewRapportPROD;
    }

    public VObject getViewFichierPROD() {
        return this.viewFichierPROD;
    }

    public void setViewFichierPROD(ViewFichierPROD viewFichierPROD) {
        this.viewFichierPROD = viewFichierPROD;
    }

    public VObject getViewEntrepotPROD() {
        return this.viewEntrepotPROD;
    }

    public void setViewEntrepotPROD(ViewEntrepotPROD viewEntrepotPROD) {
        this.viewEntrepotPROD = viewEntrepotPROD;
    }

    public VObject getViewArchivePROD() {
        return this.viewArchivePROD;
    }

    public void setViewArchivePROD(ViewArchivePROD viewArchivePROD) {
        this.viewArchivePROD = viewArchivePROD;
    }

    /**
     * Mise à jour de la console dans l'objet struts2
     */
    @Action(value = "/resetConsolePROD")
    public String resetConsolePROD() {
        HttpSession session = ServletActionContext.getRequest().getSession(false);
        session.setAttribute("console", "");
        return sessionSyncronize();
    }

    @Action(value = "/updateConsolePROD")
    public String updateConsolePROD() {
        HttpSession session = ServletActionContext.getRequest().getSession(false);

        if (session.getAttribute("console") == null) {
            session.setAttribute("console", "");
        }

        HttpServletResponse response = ServletActionContext.getResponse();
        response.setCharacterEncoding("UTF-8");
        PrintWriter out;
        try {
            out = response.getWriter();
            out.write((String) session.getAttribute("console"));
            out.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        session.setAttribute("console", "");

        return "none";

    }

    public String getScope() {
        return this.scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

}
