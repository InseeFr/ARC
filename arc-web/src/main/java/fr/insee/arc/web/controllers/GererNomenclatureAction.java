package fr.insee.arc.web.controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.model.IDbConstant;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.format.Format;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.ManipString;
import fr.insee.arc.web.dao.ExternalFilesManagementDao;
import fr.insee.arc.web.gui.ArcWebGenericService;
import fr.insee.arc.web.model.ExternalFilesModel;
import fr.insee.arc.web.util.VObject;

@Controller
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class GererNomenclatureAction extends ArcWebGenericService<ExternalFilesModel> implements IDbConstant{

	private static final String RESULT_SUCCESS = "/jsp/gererNomenclature.jsp";
	
    private static final String NMCL_ = "nmcl_";
    private static final String NOM_TABLE = "nom_table";
    private static final String TYPE_COLONNE = "type_colonne";
    private static final String NOM_COLONNE = "nom_colonne";
    private static final Logger LOGGER = LogManager.getLogger(GererNomenclatureAction.class);

    @Autowired
    private ExternalFilesManagementDao externalFilesManagementDao;

    private VObject viewListNomenclatures;
	private VObject viewNomenclature;
	private VObject viewSchemaNmcl;

    private ArrayList<String> nomenclaturesList;
    private ArrayList<ArrayList<String>> nomenclatureTable;

    
    @Override
    public void putAllVObjects(ExternalFilesModel model) {
    	loggerDispatcher.debug("putAllVObjects()", LOGGER);
    	setViewListNomenclatures(vObjectService.preInitialize(model.getViewListNomenclatures()));
    	setViewNomenclature(vObjectService.preInitialize(model.getViewNomenclature()));
    	setViewSchemaNmcl(vObjectService.preInitialize(model.getViewSchemaNmcl()));
  	
    	putVObject(getViewListNomenclatures(), t -> externalFilesManagementDao.initializeViewListNomenclatures(t,
    			dataObjectService.getView(ViewEnum.IHM_NMCL)));

    	putVObject(getViewNomenclature(), t -> externalFilesManagementDao.initializeViewNomenclature(t,
    			viewListNomenclatures));

    	putVObject(getViewSchemaNmcl(), t -> externalFilesManagementDao.intializeViewSchemaNmcl(t, viewListNomenclatures));

    	loggerDispatcher.debug("putAllVObjects() end", LOGGER);
    }
    
    @Override
	public String getActionName() {
		return "externalFileManagement";
	}

 
    @RequestMapping("/selectListNomenclatures")
    public String selectListNomenclatures(Model model) {
        return basicAction(model, RESULT_SUCCESS);
    }

    @RequestMapping("/addListNomenclatures")
    public String addListNomenclatures(Model model) {
		String nomTable = viewListNomenclatures.mapInputFields().get(NOM_TABLE).get(0);
        if (validationNomTable(nomTable)) {
            this.vObjectService.insert(viewListNomenclatures);
        }
        return generateDisplay(model, RESULT_SUCCESS);
    }

    @RequestMapping("/updateListNomenclatures")
    public String updateListNomenclatures(Model model) {
        // vérification que tous les noms de tables updatés soient conformes
        boolean zeroErreur = true;
        if (viewListNomenclatures.mapContentAfterUpdate().size() > 0) {
			for (String nomTable : viewListNomenclatures.mapContentAfterUpdate().get(NOM_TABLE)) {
                if (!validationNomTable(nomTable)) {
                    this.viewListNomenclatures.setMessage(nomTable + "n'est pas un nom de table valide.");
                    zeroErreur = false;
                    break;
                }
            }
        } else {
            this.viewListNomenclatures.setMessage("Pas de nomenclature renseignée.");
        }
        if (zeroErreur) {
            this.vObjectService.update(viewListNomenclatures);
        }

        return generateDisplay(model, RESULT_SUCCESS);
    }

    @RequestMapping("/sortListNomenclatures")
    public String sortListNomenclatures(Model model) {
        this.vObjectService.sort(viewListNomenclatures);
        return basicAction(model, RESULT_SUCCESS);
    }

    @RequestMapping("/deleteListNomenclatures")
    public String deleteListNomenclatures(Model model) {
        try {
            // Suppression de la table nom table
			String nomTable = viewListNomenclatures.mapContentSelected().get(NOM_TABLE).get(0);
			loggerDispatcher.debug("/* Delete nomenclature : " + nomTable + " */", LOGGER);
			
			
            UtilitaireDao.get(poolName).executeImmediate(null, FormatSQL.dropTable(nomTable));
            
                        
            StringBuilder requete = new StringBuilder();
            requete.append("\n SELECT nom_table FROM arc.ihm_nmcl ");
            requete.append("\n WHERE nom_table like '" + typeNomenclature(nomTable) + "%'");
            requete.append("\n AND nom_table <> '" + nomTable + "'");
            
            List<String> listeTables = UtilitaireDao.get(poolName).getList(null, requete.toString(), new ArrayList<>());
            loggerDispatcher.debug("# Liste tables : " + Format.untokenize(listeTables, ", "), LOGGER);
            
            if (listeTables.isEmpty()) {
                requete = new StringBuilder();
                requete.append("\n DELETE FROM arc.ihm_schema_nmcl");
                requete.append("\n WHERE type_nmcl = '" + typeNomenclature(nomTable) + "'");
                UtilitaireDao.get(poolName).executeImmediate(null, requete.toString());
            }

            this.vObjectService.delete(viewListNomenclatures);
        } catch (Exception e) {
        	StaticLoggerDispatcher.error("Error in GeerFamilleNormeAction.executeRequeteMiseAjourTableMetier", LOGGER);
        }
        return basicAction(model, RESULT_SUCCESS);
    }

    @RequestMapping("/downloadListNomenclatures")
    public String downloadListNomenclatures(Model model, HttpServletResponse response) {
    	
    	Map<String, ArrayList<String>> selection = viewListNomenclatures.mapContentSelected();
    	
		if (!selection.isEmpty()) {
			
			String selectedNomenclature=selection.get(NOM_TABLE).get(0);
			
			ArcPreparedStatementBuilder requeteNomenclature = new ArcPreparedStatementBuilder();
			requeteNomenclature.append(SQL.SELECT).append("a.*").append(SQL.FROM);
			requeteNomenclature.append(dataObjectService.getFullTableNameInMetadata(selectedNomenclature));
			requeteNomenclature.append(" a");
			
			this.vObjectService.download(viewListNomenclatures, response, Arrays.asList(selectedNomenclature)
					, Arrays.asList(requeteNomenclature)
					);
			return "none";
		} else {
			this.viewListNomenclatures.setMessage("You didn't select anything");
			return generateDisplay(model, RESULT_SUCCESS);
		}

    }

    private String typeNomenclature(String nomTable) {
        String[] tokens = nomTable.split(underscore);
        StringBuilder typeNomenclature = new StringBuilder();
        for (int i = 0; i < tokens.length - 1; i++) {
            typeNomenclature.append((i > 0 ? underscore : "") + tokens[i]);
        }
        return typeNomenclature.toString();
    }

    @RequestMapping("/importListNomenclatures")
    public String importListNomenclatures(Model model, MultipartFile fileUpload) {    	
    	loggerDispatcher.debug("importListNomenclatures",LOGGER);
    	try {
            importNomenclatureDansBase(fileUpload);
        } catch (Exception ex) {
            if (ManipString.isStringNull(this.viewListNomenclatures.getMessage())) {
            	this.viewListNomenclatures.setMessage(ex.toString());
            }
            loggerDispatcher.error("Error in GererNomenclatureAction.importListNomenclatures",LOGGER);
        }

        return generateDisplay(model, RESULT_SUCCESS);
    }

    private boolean validationNomTable(String nomTable) {
        if (nomTable == null) {
            this.viewListNomenclatures.setMessage("Erreur - le nom de table n'est pas renseigné");
            return false;
        }

        // Vérification du bon format du nom de la table : nom_millesime
        // doit être de la forme nom_millesime

        if (!nomTable.startsWith(NMCL_)) {
            this.viewListNomenclatures.setMessage("Erreur - le nom doit commencer par nmcl_");
            return false;
        }

        if (nomTable.split(underscore).length < 3) {
            this.viewListNomenclatures.setMessage("Erreur - le nom doit être de la forme nmcl_type_millesime (au moins deux underscore)");
            return false;
        }

        // primary key sur la table arc.ihm_nmcl donc pas besoin de tester que le nom existe déjà

        return true;
    }


    @RequestMapping("/selectSchemaNmcl")
    public String selectSchemaNmcl(Model model) {
        return basicAction(model, RESULT_SUCCESS);
    }

    @RequestMapping("/addSchemaNmcl")
    public String addSchemaNmcl(Model model) {
		if (isColonneValide(viewSchemaNmcl.mapInputFields().get(NOM_COLONNE).get(0))
                && isTypeValide(viewSchemaNmcl.mapInputFields().get(TYPE_COLONNE).get(0))) {

            this.vObjectService.insert(viewSchemaNmcl);
        }
        return generateDisplay(model, RESULT_SUCCESS);
    }

    @RequestMapping("/updateSchemaNmcl")
    public String updateSchemaNmcl(Model model) {
        System.out.println("/* updateSchemaNmcl */");
        
        HashMap<String, ArrayList<String>> selection = viewSchemaNmcl.mapContentAfterUpdate();
        if (!selection.isEmpty()) {
            boolean zeroErreur = true;
            String nomColonne = "";
            String typeColonne = "";
            System.out.println("nombre de valeurs : " + selection.get(NOM_COLONNE).size());
            for (int i = 0; i < selection.get(NOM_COLONNE).size(); i++) {
                nomColonne = selection.get(NOM_COLONNE).get(i);
                typeColonne = selection.get(TYPE_COLONNE).get(i);
                System.out.println("test colonne : " + nomColonne + " - " + typeColonne);
                if (!isColonneValide(nomColonne) && isTypeValide(typeColonne)) {
                    zeroErreur = false;
                    break;
                }
            }
            if (zeroErreur) {
                this.vObjectService.update(viewSchemaNmcl);
            }
        }
        return generateDisplay(model, RESULT_SUCCESS);
    }

    @RequestMapping("/sortSchemaNmcl")
    public String sortSchemaNmcl(Model model) {
        this.vObjectService.sort(viewSchemaNmcl);
        return basicAction(model, RESULT_SUCCESS);
    }

    @RequestMapping("/deleteSchemaNmcl")
    public String deleteSchemaNmcl(Model model) {
        this.vObjectService.delete(viewSchemaNmcl);
        return basicAction(model, RESULT_SUCCESS);
    }

    private void importNomenclatureDansBase(MultipartFile fileUpload) throws ArcException {
		if (viewListNomenclatures.mapContentSelected().isEmpty()) {
            this.viewListNomenclatures.setMessage("Vous devez selectionner une nomenclature pour l'importation.");
            return;
        }

        String nouvelleNomenclature = viewListNomenclatures.mapContentSelected().get(NOM_TABLE).get(0);
        System.out.println("/* Import de la nomenclature : " + nouvelleNomenclature + "*/");

        if (StringUtils.isEmpty(nouvelleNomenclature)) {
            this.viewListNomenclatures.setMessage("Vous devez selectionner une nomenclature pour l'importation.");
            return;
        }

        // Ouverture du fichier
        if (fileUpload == null || fileUpload.isEmpty()) {
            this.viewListNomenclatures.setMessage("You must select a file for import.");
            return;
        }
        try (BufferedReader rd = new BufferedReader(new InputStreamReader(fileUpload.getInputStream()))){
            
            // Verification des colonnes
            String[] colonnes = rd.readLine().split(";");
            String[] types = rd.readLine().split(";");
            verificationColonnes(colonnes, types);
            
            // Verification du nombre de colonnes
        	// Création de la table temporaire
        	creationTableDeNomenclatureTemporaire(colonnes, types);
        	
        	// Remplissage de la table
        	remplissageTableTemporaire(rd);
        	
        	// Création de la table définitive
        	creationTableDefinitif();
        } catch (IOException e) {
			LoggerHelper.error(LOGGER, e, "Error during import");
			this.viewListNomenclatures.setMessage("An error occurred while reading the file.");
		}

    }

    private void creationTableDefinitif() throws ArcException {
		String newNomenclatureName = viewListNomenclatures.mapContentSelected().get(NOM_TABLE).get(0);
        StringBuilder creationTableDef = new StringBuilder();

        creationTableDef.append("\n DROP TABLE IF EXISTS arc." + newNomenclatureName+";");

        creationTableDef.append("\n CREATE TABLE arc." + newNomenclatureName);
        creationTableDef.append("\n AS SELECT * FROM arc.temp_" + newNomenclatureName + ";");
        creationTableDef.append("\n DROP TABLE arc.temp_" + newNomenclatureName + ";");
        UtilitaireDao.get(poolName).executeBlock(null, creationTableDef);
    }

    private void remplissageTableTemporaire(BufferedReader rd) throws ArcException {
		String newNomenclatureName = viewListNomenclatures.mapContentSelected().get(NOM_TABLE).get(0);
    	UtilitaireDao.get(poolName).importing(null, "arc.temp_" + newNomenclatureName, rd, true, false, ";");
    }

    private void creationTableDeNomenclatureTemporaire(String[] colonnes, String[] types) throws ArcException {
		String newNomenclatureName = viewListNomenclatures.mapContentSelected().get(NOM_TABLE).get(0);
        StringBuilder createTableRequest = new StringBuilder();
        createTableRequest.append("\n DROP TABLE IF EXISTS arc.temp_" + newNomenclatureName + ";");
        createTableRequest.append("\n CREATE TABLE arc.temp_" + newNomenclatureName + " (");
        for (int i = 0; i < colonnes.length; i++) {
            if (i > 0) {
                createTableRequest.append(", ");
            }
            createTableRequest.append(colonnes[i] + " " + types[i]);
        }
        createTableRequest.append(");");

        UtilitaireDao.get(poolName).executeImmediate(null, createTableRequest);
    }

    /**
     * @param colonnesFichier
     * @throws ArcException
     */
    private void verificationColonnes(String[] colonnesFichier, String[] typesFichier) throws ArcException {
		String newNomenclatureName = viewListNomenclatures.mapContentSelected().get(NOM_TABLE).get(0);
        String typeNomenclature = typeNomenclature(newNomenclatureName);

        List<String> colonnesDansFichier = convertListToLowerTrim(colonnesFichier);
        List<String> typesDansFichier = convertListToLowerTrim(typesFichier);
  
        // Verification des noms de colonnes et des types
        String selectNomColonne = "SELECT nom_colonne FROM arc.ihm_schema_nmcl WHERE type_nmcl = '" + typeNomenclature + "' ORDER BY nom_colonne";
        List<String> colonnesDansTableIhmSchemaNmcl = new ArrayList<String>();
        UtilitaireDao.get(poolName).getList(null, selectNomColonne, colonnesDansTableIhmSchemaNmcl);
        areListsEquals(colonnesDansFichier, colonnesDansTableIhmSchemaNmcl, "field");

        // Verification des types
        String selectTypeColonne = "SELECT type_colonne FROM arc.ihm_schema_nmcl WHERE type_nmcl = '" + typeNomenclature + "' ORDER BY nom_colonne";
        List<String> typesDansTableIhmSchemaNmcl = new ArrayList<String>();
        UtilitaireDao.get(poolName).getList(null, selectTypeColonne, typesDansTableIhmSchemaNmcl);
        areListsEquals(typesDansFichier, typesDansTableIhmSchemaNmcl, "type");

    }

    private List<String> convertListToLowerTrim(String[] tab) {
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < tab.length; i++) {
            list.add(tab[i].toLowerCase().trim());
        }
        return list;
    }

    private void areListsEquals(List<String> listeFichier, List<String> listIhmSchemaNmcl, String elementDescription) throws ArcException {
        for (String e : listeFichier) {
            if (!listIhmSchemaNmcl.contains(e)) {
                String message = "externalFilesManagement.import.error.extraImport";
                this.viewListNomenclatures.setMessage(message);
                this.viewListNomenclatures.setMessageArgs(elementDescription, e);
                throw new ArcException(message);
            }
        }

        // Et réciproquement si toutes les colonnes sont présentes dans le fichier
        for (String e : listIhmSchemaNmcl) {
            if (!listeFichier.contains(e)) {
                String message = "L'element de la table arc.ihm_schema_nmcl '" + e + "' n'est pas dans le fichier de nomenclature";
                this.viewSchemaNmcl.setMessage(message);
                throw new ArcException(message);
            }
        }
    }

    /**
     * 
     * Vérifie si un nom de colonnes est valide. <br/>
     * Si ce n'est pas le cas une exception est jetée.
     * 
     * @param nomColonne
     * @throws ArcException
     */
    private boolean isColonneValide(String nomColonne) {
        System.out.println("Validation de : " + nomColonne);
        try {
            UtilitaireDao.get(poolName).executeImmediate(null, "SELECT null as " + nomColonne);
        } catch (Exception e) {
            String message = nomColonne + " n'est pas un nom de colonne valide.";
            this.viewSchemaNmcl.setMessage(message);
            return false;
        }
        return true;
    }

    /**
     * 
     * Vérifie si un nom de colonnes est valide. <br/>
     * Si ce n'est pas le cas une exception est jetée.
     * 
     * @param nomColonne
     * @throws ArcException
     */
    private boolean isTypeValide(String typeColonne) {
        System.out.println("Validation de : " + typeColonne);
        try {
            UtilitaireDao.get(poolName).executeImmediate(null, "SELECT null::" + typeColonne);
        } catch (Exception e) {
            String message = typeColonne + " n'est pas un type valide.";
            this.viewSchemaNmcl.setMessage(message);
            return false;
        }
        return true;
    }

    @RequestMapping("/selectNomenclature")
    public String selectNomenclature(Model model) {
        return basicAction(model, RESULT_SUCCESS);
    }

    @RequestMapping("/sortNomenclature")
    public String sortNomenclature(Model model) {
        this.vObjectService.sort(viewNomenclature);
        return basicAction(model, RESULT_SUCCESS);
    }

  
    /**
     * Setters et getters
     */


    public ArrayList<String> getNomenclaturesList() {
        return nomenclaturesList;
    }

    public void setNomenclaturesList(ArrayList<String> nomenclaturesList) {
        this.nomenclaturesList = nomenclaturesList;
    }

    public ArrayList<ArrayList<String>> getNomenclatureTable() {
        return nomenclatureTable;
    }

    public void setNomenclatureTable(ArrayList<ArrayList<String>> nomenclatureTable) {
        this.nomenclatureTable = nomenclatureTable;
    }

    public VObject getViewListNomenclatures() {
        return viewListNomenclatures;
    }

    public void setViewListNomenclatures(VObject viewListNomenclatures) {
        this.viewListNomenclatures = viewListNomenclatures;
    }

    public VObject getViewNomenclature() {
        return viewNomenclature;
    }

    public void setViewNomenclature(VObject viewNomenclature) {
        this.viewNomenclature = viewNomenclature;
    }

    public VObject getViewSchemaNmcl() {
        return viewSchemaNmcl;
    }

    public void setViewSchemaNmcl(VObject viewSchemaNmcl) {
        this.viewSchemaNmcl = viewSchemaNmcl;
    }

}