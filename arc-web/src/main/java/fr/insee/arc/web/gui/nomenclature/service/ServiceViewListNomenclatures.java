package fr.insee.arc.web.gui.nomenclature.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.format.Format;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.ManipString;

@Service
public class ServiceViewListNomenclatures extends InteractorNomenclature {

    private static final Logger LOGGER = LogManager.getLogger(ServiceViewListNomenclatures.class);

    private static final String NMCL_PREFIX = "nmcl_";
    private static final String NOM_TABLE = "nom_table";

    public String selectListNomenclatures(Model model) {
        return basicAction(model, RESULT_SUCCESS);
    }

    public String addListNomenclatures(Model model) {
		String nomTable = views.getViewListNomenclatures().mapInputFields().get(NOM_TABLE).get(0);
        if (validationNomTable(nomTable)) {
            this.vObjectService.insert(views.getViewListNomenclatures());
        }
        return generateDisplay(model, RESULT_SUCCESS);
    }

    public String updateListNomenclatures(Model model) {
        // vérification que tous les noms de tables updatés soient conformes
        boolean zeroErreur = true;
        if (this.views.getViewListNomenclatures().mapContentAfterUpdate().size() > 0) {
			for (String nomTable : this.views.getViewListNomenclatures().mapContentAfterUpdate().get(NOM_TABLE)) {
                if (!validationNomTable(nomTable)) {
                    this.views.getViewListNomenclatures().setMessage(nomTable + "n'est pas un nom de table valide.");
                    zeroErreur = false;
                    break;
                }
            }
        } else {
            this.views.getViewListNomenclatures().setMessage("Pas de nomenclature renseignée.");
        }
        if (zeroErreur) {
            this.vObjectService.update(this.views.getViewListNomenclatures());
        }

        return generateDisplay(model, RESULT_SUCCESS);
    }

    public String sortListNomenclatures(Model model) {
        this.vObjectService.sort(views.getViewListNomenclatures());
        return basicAction(model, RESULT_SUCCESS);
    }

    public String deleteListNomenclatures(Model model) {
        try {
            // Suppression de la table nom table
			String nomTable = views.getViewListNomenclatures().mapContentSelected().get(NOM_TABLE).get(0);
			loggerDispatcher.debug("/* Delete nomenclature : " + nomTable + " */", LOGGER);
			
			
            UtilitaireDao.get(poolName).executeImmediate(null, FormatSQL.dropTableCascade(nomTable));
            
                        
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

            this.vObjectService.delete(views.getViewListNomenclatures());
        } catch (Exception e) {
        	StaticLoggerDispatcher.error("Error in GeerFamilleNormeAction.executeRequeteMiseAjourTableMetier", LOGGER);
        }
        return basicAction(model, RESULT_SUCCESS);
    }

    public String downloadListNomenclatures(Model model, HttpServletResponse response) {
    	
    	Map<String, ArrayList<String>> selection = views.getViewListNomenclatures().mapContentSelected();
    	
		if (!selection.isEmpty()) {
			
			String selectedNomenclature=selection.get(NOM_TABLE).get(0);
			
			ArcPreparedStatementBuilder requeteNomenclature = new ArcPreparedStatementBuilder();
			requeteNomenclature.append(SQL.SELECT).append("a.*").append(SQL.FROM);
			requeteNomenclature.append(dataObjectService.getFullTableNameInMetadata(selectedNomenclature));
			requeteNomenclature.append(" a");
			
			this.vObjectService.download(views.getViewListNomenclatures(), response, Arrays.asList(selectedNomenclature)
					, Arrays.asList(requeteNomenclature)
					);
			return "none";
		} else {
			this.views.getViewListNomenclatures().setMessage("You didn't select anything");
			return generateDisplay(model, RESULT_SUCCESS);
		}

    }

    private static String typeNomenclature(String nomTable) {
        String[] tokens = nomTable.split(underscore);
        StringBuilder typeNomenclature = new StringBuilder();
        for (int i = 0; i < tokens.length - 1; i++) {
            typeNomenclature.append((i > 0 ? underscore : "") + tokens[i]);
        }
        return typeNomenclature.toString();
    }

    public String importListNomenclatures(Model model, MultipartFile fileUpload) {    	
    	loggerDispatcher.debug("importListNomenclatures",LOGGER);
    	try {
            importNomenclatureDansBase(fileUpload);
        } catch (ArcException ex) {
           	this.views.getViewListNomenclatures().setMessage(ex.toString());
        }

        return generateDisplay(model, RESULT_SUCCESS);
    }

    private boolean validationNomTable(String nomTable) {
        if (nomTable == null) {
            this.views.getViewListNomenclatures().setMessage("Erreur - le nom de table n'est pas renseigné");
            return false;
        }

        // Vérification du bon format du nom de la table : nom_millesime
        // doit être de la forme nom_millesime

        if (!nomTable.startsWith(NMCL_PREFIX)) {
            this.views.getViewListNomenclatures().setMessage("Erreur - le nom doit commencer par nmcl_");
            return false;
        }

        if (nomTable.split(underscore).length < 3) {
            this.views.getViewListNomenclatures().setMessage("Erreur - le nom doit être de la forme nmcl_type_millesime (au moins deux underscore)");
            return false;
        }

        // primary key sur la table arc.ihm_nmcl donc pas besoin de tester que le nom existe déjà

        return true;
    }
    
    
    private void importNomenclatureDansBase(MultipartFile fileUpload) throws ArcException {
		if (views.getViewListNomenclatures().mapContentSelected().isEmpty()) {
            this.views.getViewListNomenclatures().setMessage("Vous devez selectionner une nomenclature pour l'importation.");
            return;
        }

        String nouvelleNomenclature = views.getViewListNomenclatures().mapContentSelected().get(NOM_TABLE).get(0);
        LoggerHelper.debug(LOGGER,"/* Import de la nomenclature : " + nouvelleNomenclature + "*/");

        if (StringUtils.isEmpty(nouvelleNomenclature)) {
            this.views.getViewListNomenclatures().setMessage("Vous devez selectionner une nomenclature pour l'importation.");
            return;
        }

        // Ouverture du fichier
        if (fileUpload == null || fileUpload.isEmpty()) {
            this.views.getViewListNomenclatures().setMessage("Vous devez selectionner un fichier pour l'importation.");
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
        	throw new ArcException(e, ArcExceptionMessage.IHM_NMCL_IMPORT_FAILED);
		}

    }

    /**
     * @param colonnesFichier
     * @throws ArcException
     */
    private void verificationColonnes(String[] colonnesFichier, String[] typesFichier) throws ArcException {
		String newNomenclatureName = views.getViewListNomenclatures().mapContentSelected().get(NOM_TABLE).get(0);
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
    

    private void areListsEquals(List<String> listeFichier, List<String> listIhmSchemaNmcl, String elementDescription) throws ArcException {
        for (String e : listeFichier) {
            if (!listIhmSchemaNmcl.contains(e)) {
                throw new ArcException(ArcExceptionMessage.IHM_NMCL_COLUMN_IN_FILE_BUT_NOT_IN_SCHEMA, e);
            }
        }

        // Et réciproquement si toutes les colonnes sont présentes dans le fichier
        for (String e : listIhmSchemaNmcl) {
            if (!listeFichier.contains(e)) {
                throw new ArcException(ArcExceptionMessage.IHM_NMCL_COLUMN_IN_SCHEMA_BUT_NOT_IN_FILE, e);
            }
        }
    }

    
    private List<String> convertListToLowerTrim(String[] tab) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < tab.length; i++) {
            list.add(tab[i].toLowerCase().trim());
        }
        return list;
    }
    


    private void creationTableDefinitif() throws ArcException {
		String newNomenclatureName = views.getViewListNomenclatures().mapContentSelected().get(NOM_TABLE).get(0);
        StringBuilder creationTableDef = new StringBuilder();

        creationTableDef.append("\n DROP TABLE IF EXISTS arc." + newNomenclatureName+";");

        creationTableDef.append("\n CREATE TABLE arc." + newNomenclatureName);
        creationTableDef.append("\n AS SELECT * FROM arc.temp_" + newNomenclatureName + ";");
        creationTableDef.append("\n DROP TABLE arc.temp_" + newNomenclatureName + ";");
        UtilitaireDao.get(poolName).executeBlock(null, creationTableDef);
    }

    private void remplissageTableTemporaire(BufferedReader rd) throws ArcException {
		String newNomenclatureName = views.getViewListNomenclatures().mapContentSelected().get(NOM_TABLE).get(0);
    	UtilitaireDao.get(poolName).importingWithReader(null, "arc.temp_" + newNomenclatureName, rd, false, ";");
    }

    private void creationTableDeNomenclatureTemporaire(String[] colonnes, String[] types) throws ArcException {
		String newNomenclatureName = views.getViewListNomenclatures().mapContentSelected().get(NOM_TABLE).get(0);
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
    
}
