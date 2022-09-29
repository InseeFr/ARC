package fr.insee.arc.web.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import fr.insee.arc.core.model.IDbConstant;
import fr.insee.arc.utils.dao.PreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.web.util.VObjectService;
import fr.insee.arc.web.util.VObject;


/**
 * Will own all the utilitary methode used in the {@link GererNormeAction}
 * 
 * @author Manuel Soulier
 *
 */
@Component
public class ExternalFilesManagementDao implements IDbConstant {

	@Autowired
	@Qualifier("defaultVObjectService")
	private VObjectService vObject;
	
    @SuppressWarnings("unused")
	private final Logger LOGGER = LogManager.getLogger(ExternalFilesManagementDao.class);

    private static final String NOM_TABLE = "nom_table";
    

    public void initializeViewListNomenclatures(VObject viewListNomenclatures, String table) {
        System.out.println("/* initializeListeNomenclatures */");
        
        HashMap<String, String> defaultInputFields = new HashMap<String, String>();
        PreparedStatementBuilder requete = new PreparedStatementBuilder();
        requete.append("\n SELECT " + NOM_TABLE + ", description FROM "+table+" ");

        vObject.initialize(viewListNomenclatures, requete, table, defaultInputFields);
    }

    
    public void initializeViewNomenclature(VObject viewNomenclature, VObject viewListNomenclatures) {
        System.out.println("/* initializeNomenclature */");

        Map<String, ArrayList<String>> selection = viewListNomenclatures.mapContentSelected();

        if (!selection.isEmpty() && UtilitaireDao.get(poolName).isTableExiste(null, "arc." + selection.get(NOM_TABLE).get(0))) {
        	PreparedStatementBuilder requete = new PreparedStatementBuilder();
            requete.append("select * from arc." + selection.get(NOM_TABLE).get(0) + " ");

            HashMap<String, String> defaultInputFields = new HashMap<String, String>();
            defaultInputFields.put(NOM_TABLE, selection.get(NOM_TABLE).get(0));

            vObject.initialize(viewNomenclature, requete, "arc." + selection.get(NOM_TABLE).get(0), defaultInputFields);
        } else {
            vObject.destroy(viewNomenclature);
        }

    }
    
    public void intializeViewSchemaNmcl(VObject viewSchemaNmcl, VObject viewListNomenclatures) {
        System.out.println("/* initializeSchemaNmcl */");
        Map<String, ArrayList<String>> selection = viewListNomenclatures.mapContentSelected();

        if (!selection.isEmpty()) {
        	PreparedStatementBuilder requete = new PreparedStatementBuilder();
            requete.append("\n SELECT type_nmcl, nom_colonne, type_colonne FROM arc.ihm_schema_nmcl ");
            requete.append("\n WHERE type_nmcl = " + requete.quoteText(typeNomenclature(selection.get(NOM_TABLE).get(0))) + " ");
            HashMap<String, String> defaultInputFields = new HashMap<String, String>();

            defaultInputFields.put("type_nmcl", typeNomenclature(selection.get(NOM_TABLE).get(0)));
            vObject.initialize(viewSchemaNmcl, requete, "arc.ihm_schema_nmcl", defaultInputFields);
            
        } else {
            vObject.destroy(viewSchemaNmcl);
        }
    }

    private String typeNomenclature(String nomTable) {
        String[] tokens = nomTable.split(fr.insee.arc.utils.textUtils.IConstanteCaractere.UNDERSCORE);
        StringBuilder typeNomenclature = new StringBuilder();
        for (int i = 0; i < tokens.length - 1; i++) {
            typeNomenclature.append((i > 0 ? fr.insee.arc.utils.textUtils.IConstanteCaractere.UNDERSCORE : "") + tokens[i]);
        }
        return typeNomenclature.toString();
    }
    
}
