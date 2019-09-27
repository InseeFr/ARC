package fr.insee.arc.web.action;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.interceptor.SessionAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import fr.insee.arc.core.model.BddTable;
import fr.insee.arc.core.model.DbConstant;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.format.Format;
import fr.insee.arc.utils.textUtils.ICharacterConstant;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.web.dao.ExternalFilesManagementDao;
import fr.insee.arc.web.util.VObject;

@Component
@Results({ @Result(name = "success", location = "/jsp/gererNomenclature.jsp"), @Result(name = "index", location = "/jsp/index.jsp") })
public class GererNomenclatureAction extends ArcAction {

    private static final String NMCL_ = "nmcl_";
    private static final String NOM_TABLE = "nom_table";
    private static final String TYPE_COLONNE = "type_colonne";
    private static final String NOM_COLONNE = "nom_colonne";
    private static final String REGEX_FILE_NMCL = "^nmcl_+[0-9a-z]+_{1}[0-9a-z]+$";
    private static final Logger LOGGER = Logger.getLogger(GererNormeAction.class);
    @Autowired
    @Qualifier("viewListNomenclatures")
    VObject viewListNomenclatures;

    @Autowired
    @Qualifier("viewNomenclature")
    VObject viewNomenclature;

    @Autowired
    @Qualifier("viewSchemaNmcl")
    VObject viewSchemaNmcl;

    
    @Override
    public void putAllVObjects() {
	putVObject(viewListNomenclatures, t -> ExternalFilesManagementDao.initializeViewListNomenclatures(t,
"arc.ihm_nmcl"));
	//
	putVObject(viewNomenclature, t -> ExternalFilesManagementDao.initializeViewNomenclature(t,
			viewListNomenclatures));
	//
	putVObject(viewSchemaNmcl, t -> ExternalFilesManagementDao.intializeViewSchemaNmcl(t, viewListNomenclatures));
	//
    }
    
    private File fileUpload;
    private String fileUploadContentType;
    private String fileUploadFileName;
    private String commentaire;

    private ArrayList<String> nomenclaturesList;
    private ArrayList<ArrayList<String>> nomenclatureTable;

    private String scope;

//    @Override
//    public void setSession(Map<String, Object> session) {
//        this.viewListNomenclatures.setMessage("");
//        this.viewNomenclature.setMessage("");
//        this.viewSchemaNmcl.setMessage("");
//
//    }

 
    @Action(value = "/selectListNomenclatures")
    public String selectListNomenclatures() {
        return basicAction();
    }

    @Action(value = "/addListNomenclatures")
    public String addListNomenclatures() {
        String nomTable = this.viewListNomenclatures.mapInputFields().get(NOM_TABLE).get(0);
        if (validationNomTable(nomTable)) {
            this.viewListNomenclatures.insert();
        }
        return basicAction();
    }

    @Action(value = "/updateListNomenclatures")
    public String updateListNomenclatures() {
        // vérification que tous les noms de tables updatés soient conformes
        boolean zeroErreur = true;
        if (this.viewListNomenclatures.mapSameContentFromPreviousVObject().size() > 0) {
            for (String nomTable : this.viewListNomenclatures.mapSameContentFromPreviousVObject().get(NOM_TABLE)) {
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
            this.viewListNomenclatures.update();
        }

        return basicAction();
    }

    @Action(value = "/sortListNomenclatures")
    public String sortListNomenclatures() {
        this.viewListNomenclatures.sort();
        return basicAction();
    }

    @Action(value = "/deleteListNomenclatures")
    public String deleteListNomenclatures() {
        try {
            // Suppression de la table nom table
            String nomTable = this.viewListNomenclatures.mapContentSelected().get(NOM_TABLE).get(0);
            System.out.println("/* Delete nomenclature : " + nomTable + " */");
            UtilitaireDao.get(DbConstant.POOL_NAME).executeImmediate(null, FormatSQL.dropUniqueTable(nomTable));
            StringBuilder requete = new StringBuilder();
            requete.append("\n SELECT nom_table FROM arc.ihm_nmcl ");
            requete.append("\n WHERE nom_table like '" + typeNomenclature(nomTable) + "%'");
            requete.append("\n AND nom_table <> '" + nomTable + "'");
            List<String> listeTables = UtilitaireDao.get(DbConstant.POOL_NAME).getList(null, requete.toString(), new ArrayList<String>());
            System.out.println("# Liste tables : " + Format.untokenize(listeTables, ", "));
            if (listeTables.isEmpty()) {
                requete = new StringBuilder();
                requete.append("\n DELETE FROM arc.ihm_schema_nmcl");
                requete.append("\n WHERE type_nmcl = '" + typeNomenclature(nomTable) + "'");
                UtilitaireDao.get(DbConstant.POOL_NAME).executeImmediate(null, requete.toString());
            }

            this.viewListNomenclatures.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return basicAction();
    }

    private String typeNomenclature(String nomTable) {
        String[] tokens = nomTable.split(UNDERSCORE);
        StringBuilder typeNomenclature = new StringBuilder();
        for (int i = 0; i < tokens.length - 1; i++) {
            typeNomenclature.append((i > 0 ? UNDERSCORE : "") + tokens[i]);
        }
        return typeNomenclature.toString();
    }

    @Action(value = "/importListNomenclatures")
    public String importListNomenclatures() {
        LoggerHelper.debugDebutMethodeAsComment(getClass(), "importListNomenclatures()", LOGGER);
        try {
            importNomenclatureDansBase();
        } catch (Exception ex) {
            ex.printStackTrace();
            this.viewListNomenclatures.setMessage(ex.toString());
            LoggerHelper.errorGenTextAsComment(getClass(), "importListNomenclatures()", LOGGER, ex);
        }

        return basicAction();
    }

    private boolean validationNomTable(String nomTable) {

        if (nomTable == null) {
            this.viewListNomenclatures.setMessage("Erreur - le nom de table n'est pas renseigné");
            return false;
        }

        // Vérification du bon format du nom de la table : nom_millesime
        // doit être de la forme nom_millesime

        // if (!nomTable.matches(REGEX_FILE_NMCL)) {
        // this.viewListNomenclatures.setMessage("Erreur - le nom de table n'est pas de la forme nmcl_nom_millesime, le tout en minuscule!");
        // return false;
        // }

        if (!nomTable.startsWith(NMCL_)) {
            this.viewListNomenclatures.setMessage("Erreur - le nom doit commencer par nmcl_");
            return false;
        }

        if (nomTable.split(UNDERSCORE).length < 3) {
            this.viewListNomenclatures.setMessage("Erreur - le nom doit être de la forme nmcl_type_millesime (au moins deux underscore)");
            return false;
        }

        // primary key sur la table arc.ihm_nmcl donc pas besoin de tester que le nom existe déjà

        return true;
    }


    @Action(value = "/selectSchemaNmcl")
    public String selectSchemaNmcl() {
        return basicAction();
    }

    @Action(value = "/addSchemaNmcl")
    public String addSchemaNmcl() {

        if (isColonneValide(this.viewSchemaNmcl.mapInputFields().get(NOM_COLONNE).get(0))
                && isTypeValide(this.viewSchemaNmcl.mapInputFields().get(TYPE_COLONNE).get(0))) {

            this.viewSchemaNmcl.insert();
        }
        return basicAction();
    }

    @Action(value = "/updateSchemaNmcl")
    public String updateSchemaNmcl() {
        System.out.println("/* updateSchemaNmcl */");
        HashMap<String, ArrayList<String>> selection = this.viewSchemaNmcl.mapSameContentFromPreviousVObject();
        System.out.println("taille selection : " + selection.size());
        System.out.println("Colonne : " + Format.untokenize(selection.keySet(), ", "));
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
                this.viewSchemaNmcl.update();
            }
        }
        return basicAction();
    }

    @Action(value = "/sortSchemaNmcl")
    public String sortSchemaNmcl() {
        this.viewSchemaNmcl.sort();
        return basicAction();
    }

    @Action(value = "/deleteSchemaNmcl")
    public String deleteSchemaNmcl() {
        this.viewSchemaNmcl.delete();
        return basicAction();
    }

    private void importNomenclatureDansBase() throws Exception {

        if (this.viewListNomenclatures.mapContentSelected().isEmpty()) {
            this.viewListNomenclatures.setMessage("Vous devez selectionner une nomenclature pour l'importation.");
            return;
        }

        String nouvelleNomenclature = this.viewListNomenclatures.mapContentSelected().get(NOM_TABLE).get(0);
        String typeNomenclature = typeNomenclature(nouvelleNomenclature);
        System.out.println("/* Import de la nomenclature : " + nouvelleNomenclature + "*/");

        if (StringUtils.isEmpty(nouvelleNomenclature)) {
            this.viewListNomenclatures.setMessage("Vous devez selectionner une nomenclature pour l'importation.");
            return;
        }

        // Ouverture du fichier
        if (this.fileUpload == null) {
            this.viewListNomenclatures.setMessage("Vous devez choisir un fichier pour l'importation.");
            return;
        }
        FileInputStream in = new FileInputStream(this.fileUpload);
        try(BufferedReader rd = new BufferedReader(new InputStreamReader(in))){
            
            // Verification des colonnes
            String[] colonnes = rd.readLine().split(";");
            String[] types = rd.readLine().split(";");
            verificationColonnes(colonnes, types);
            
            // Verification du nombre de colonnes
            try {
        	// Création de la table temporaire
        	creationTableDeNomenclatureTemporaire(colonnes, types);
        	
        	// Remplissage de la table
        	remplissageTableTemporaire(rd);
        	
        	// Création de la table définitive
        	creationTableDefinitif();
            
            } finally {
        	/*
        	 * Pour que les champs de saisie ne soit pas prérempli des anciennes valeurs
        	 */
        	this.commentaire = "";
            }
        }
        

    }

    private void creationTableDefinitif() throws SQLException {
        String newNomenclatureName = this.viewListNomenclatures.mapContentSelected().get(NOM_TABLE).get(0);
        StringBuilder creationTableDef = new StringBuilder();
        creationTableDef.append("\n CREATE TABLE arc." + newNomenclatureName);
        creationTableDef.append("\n AS SELECT * FROM arc.temp_" + newNomenclatureName + ";");
        creationTableDef.append("\n DROP TABLE arc.temp_" + newNomenclatureName + ";");
        // creationTableDef.append("\n COMMENT ON TABLE " + newNomenclatureName + " IS " + "'" + this.commentaire + "'" + ";");
        // Pas besoin de supprimer la temporary table
        UtilitaireDao.get(DbConstant.POOL_NAME).executeBlock(null, creationTableDef);
    }

    private void remplissageTableTemporaire(BufferedReader rd) throws Exception {
        String newNomenclatureName = this.viewListNomenclatures.mapContentSelected().get(NOM_TABLE).get(0);
    	UtilitaireDao.get(DbConstant.POOL_NAME).importing(null, "arc.temp_" + newNomenclatureName, rd, true, false, ";");
    }

    private void creationTableDeNomenclatureTemporaire(String[] colonnes, String[] types) throws SQLException {
        String newNomenclatureName = this.viewListNomenclatures.mapContentSelected().get(NOM_TABLE).get(0);
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

        UtilitaireDao.get(DbConstant.POOL_NAME).executeRequest(null, createTableRequest);
    }

    /**
     * @param colonnesFichier
     * @throws Exception
     */
    private void verificationColonnes(String[] colonnesFichier, String[] typesFichier) throws Exception {

        String newNomenclatureName = this.viewListNomenclatures.mapContentSelected().get(NOM_TABLE).get(0);
        String typeNomenclature = typeNomenclature(newNomenclatureName);

        List<String> colonnesDansFichier = convertListToLowerTrim(colonnesFichier);
        List<String> typesDansFichier = convertListToLowerTrim(typesFichier);
  
        // Verification des noms de colonnes et des types
        String selectNomColonne = "SELECT nom_colonne FROM arc.ihm_schema_nmcl WHERE type_nmcl = '" + typeNomenclature + "' ORDER BY nom_colonne";
        List<String> colonnesDansTableIhmSchemaNmcl = new ArrayList<String>();
        UtilitaireDao.get(DbConstant.POOL_NAME).getList(null, selectNomColonne, colonnesDansTableIhmSchemaNmcl);
        isListesIdentiques(colonnesDansFichier, colonnesDansTableIhmSchemaNmcl);

        // Verification des types
        String selectTypeColonne = "SELECT type_colonne FROM arc.ihm_schema_nmcl WHERE type_nmcl = '" + typeNomenclature + "' ORDER BY nom_colonne";
        List<String> typesDansTableIhmSchemaNmcl = new ArrayList<String>();
        UtilitaireDao.get(DbConstant.POOL_NAME).getList(null, selectTypeColonne, typesDansTableIhmSchemaNmcl);
        isListesIdentiques(typesDansFichier, typesDansTableIhmSchemaNmcl);

    }

    private List<String> convertListToLowerTrim(String[] tab) {
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < tab.length; i++) {
            list.add(tab[i].toLowerCase().trim());
        }
        return list;
    }

    private void isListesIdentiques(List<String> listeFichier, List<String> listIhmSchemaNmcl) {
        for (String e : listeFichier) {
            if (!listIhmSchemaNmcl.contains(e)) {
                String message = "L'element du fichier de nomenclature '" + e + "' n'est pas dans la table arc.ihm_schema_nmcl.";
                this.viewSchemaNmcl.setMessage(message);
                throw new IllegalStateException(message);
            }
        }

        // Et réciproquement si toutes les colonnes sont présentes dans le fichier
        for (String e : listIhmSchemaNmcl) {
            if (!listeFichier.contains(e)) {
                String message = "L'element de la table arc.ihm_schema_nmcl '" + e + "' n'est pas dans le fichier de nomenclature";
                this.viewSchemaNmcl.setMessage(message);
                throw new IllegalStateException(message);
            }
        }
    }

    /**
     * 
     * Vérifie si un nom de colonnes est valide. <br/>
     * Si ce n'est pas le cas une exception est jetée.
     * 
     * @param nomColonne
     * @throws SQLException
     */
    private boolean isColonneValide(String nomColonne) {
        System.out.println("Validation de : " + nomColonne);
        try {
            UtilitaireDao.get(DbConstant.POOL_NAME).executeImmediate(null, "SELECT null as " + nomColonne);
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
     * @throws SQLException
     */
    private boolean isTypeValide(String typeColonne) {
        System.out.println("Validation de : " + typeColonne);
        try {
            UtilitaireDao.get(DbConstant.POOL_NAME).executeImmediate(null, "SELECT null::" + typeColonne);
        } catch (Exception e) {
            String message = typeColonne + " n'est pas un type valide.";
            this.viewSchemaNmcl.setMessage(message);
            return false;
        }
        return true;
    }

    @Action(value = "/selectNomenclature")
    public String selectNomenclature() {
        return basicAction();
    }

    @Action(value = "/sortNomenclature")
    public String sortNomenclature() {
        this.viewNomenclature.sort();
        return basicAction();
    }

  
    /**
     * Setters et getters
     */

    public File getFileUpload() {
        return fileUpload;
    }

    public void setFileUpload(File fileUpload) {
        this.fileUpload = fileUpload;
    }

    public String getFileUploadContentType() {
        return fileUploadContentType;
    }

    public void setFileUploadContentType(String fileUploadContentType) {
        this.fileUploadContentType = fileUploadContentType;
    }

    public String getFileUploadFileName() {
        return fileUploadFileName;
    }

    public void setFileUploadFileName(String fileUploadFileName) {
        this.fileUploadFileName = fileUploadFileName;
    }

    public ArrayList<String> getNomenclaturesList() {
        return nomenclaturesList;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
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

	@Override
	public void instanciateAllDAOs() {	
	}

	@Override
	public void setProfilsAutorises() {
	}

	@Override
	protected void specificTraitementsPostDAO() {		
	}

	@Override
	public String getActionName() {
		return null;
	}


}
