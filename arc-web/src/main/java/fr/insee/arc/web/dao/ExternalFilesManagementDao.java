package fr.insee.arc.web.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import fr.insee.arc.core.model.DbConstant;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.web.action.GererNormeAction;
import fr.insee.arc.web.util.VObject;
import fr.insee.arc.utils.textUtils.ICharacterConstant;


/**
 * Will own all the utilitary methode used in the {@link GererNormeAction}
 * 
 * @author Manuel Soulier
 *
 */
public class ExternalFilesManagementDao {

    private static final Logger LOGGER = Logger.getLogger(ExternalFilesManagementDao.class);

    private static final String NMCL_ = "nmcl_";
    private static final String NOM_TABLE = "nom_table";
    private static final String TYPE_COLONNE = "type_colonne";
    private static final String NOM_COLONNE = "nom_colonne";
    
    private ExternalFilesManagementDao() {
	throw new IllegalStateException("Utility class");
    }

    public static void initializeViewListNomenclatures(VObject viewListNomenclatures, String table) {
        System.out.println("/* initializeListeNomenclatures */");
        
        HashMap<String, String> defaultInputFields = new HashMap<String, String>();
        StringBuilder requete = new StringBuilder();
        requete.append("\n SELECT " + NOM_TABLE + ", description FROM "+table+" ");

        viewListNomenclatures.initialize(requete.toString(), table, defaultInputFields);

    }

    
    public static void initializeViewNomenclature(VObject viewNomenclature, VObject viewListNomenclatures) {
        System.out.println("/* initializeNomenclature */");

        Map<String, ArrayList<String>> selection = viewListNomenclatures.mapContentSelected();

        if (!selection.isEmpty() && UtilitaireDao.get(DbConstant.POOL_NAME).isTableExiste(null, "arc." + selection.get(NOM_TABLE).get(0))) {
            StringBuilder requete = new StringBuilder();
            requete.append("select * from arc." + selection.get(NOM_TABLE).get(0) + " ");

            HashMap<String, String> defaultInputFields = new HashMap<String, String>();
            defaultInputFields.put(NOM_TABLE, selection.get(NOM_TABLE).get(0));

            viewNomenclature.initialize(requete.toString(), "arc." + selection.get(NOM_TABLE).get(0), defaultInputFields);

            System.out.println(viewNomenclature.mapContent());

        } else {
            viewNomenclature.destroy();
        }

    }
    
    public static void intializeViewSchemaNmcl(VObject viewSchemaNmcl, VObject viewListNomenclatures) {
        System.out.println("/* initializeSchemaNmcl */");
        Map<String, ArrayList<String>> selection = viewListNomenclatures.mapContentSelected();

        if (!selection.isEmpty()) {
            StringBuilder requete = new StringBuilder();
            requete.append("\n SELECT type_nmcl, nom_colonne, type_colonne FROM arc.ihm_schema_nmcl ");
            requete.append("\n WHERE type_nmcl = '" + typeNomenclature(selection.get(NOM_TABLE).get(0)) + "'");
            HashMap<String, String> defaultInputFields = new HashMap<String, String>();

            defaultInputFields.put("type_nmcl", typeNomenclature(selection.get(NOM_TABLE).get(0)));
            viewSchemaNmcl.initialize(requete.toString(), "arc.ihm_schema_nmcl", defaultInputFields);
            
        } else {
            viewSchemaNmcl.destroy();
        }
    }

    private static String typeNomenclature(String nomTable) {
        String[] tokens = nomTable.split(fr.insee.arc.utils.textUtils.ICharacterConstant.UNDERSCORE);
        StringBuilder typeNomenclature = new StringBuilder();
        for (int i = 0; i < tokens.length - 1; i++) {
            typeNomenclature.append((i > 0 ? fr.insee.arc.utils.textUtils.ICharacterConstant.UNDERSCORE : "") + tokens[i]);
        }
        return typeNomenclature.toString();
    }
    
}
