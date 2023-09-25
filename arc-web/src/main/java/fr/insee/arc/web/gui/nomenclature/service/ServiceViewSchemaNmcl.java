package fr.insee.arc.web.gui.nomenclature.service;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;

@Service
public class ServiceViewSchemaNmcl extends InteractorNomenclature {

    private static final Logger LOGGER = LogManager.getLogger(ServiceViewSchemaNmcl.class);

	
    private static final String TYPE_COLONNE = "type_colonne";
    private static final String NOM_COLONNE = "nom_colonne";
	 
    public String selectSchemaNmcl(Model model) {
        return basicAction(model, RESULT_SUCCESS);
    }

    public String addSchemaNmcl(Model model) {
		if (isColonneValide(views.getViewSchemaNmcl().mapInputFields().get(NOM_COLONNE).get(0))
                && isTypeValide(views.getViewSchemaNmcl().mapInputFields().get(TYPE_COLONNE).get(0))) {

            this.vObjectService.insert(views.getViewSchemaNmcl());
        }
        return generateDisplay(model, RESULT_SUCCESS);
    }

    public String updateSchemaNmcl(Model model) {
        HashMap<String, ArrayList<String>> selection = views.getViewSchemaNmcl().mapContentAfterUpdate();
        if (!selection.isEmpty()) {
            boolean zeroErreur = true;
            String nomColonne = "";
            String typeColonne = "";
            loggerDispatcher.debug("nombre de valeurs : " + selection.get(NOM_COLONNE).size(),LOGGER);
            for (int i = 0; i < selection.get(NOM_COLONNE).size(); i++) {
                nomColonne = selection.get(NOM_COLONNE).get(i);
                typeColonne = selection.get(TYPE_COLONNE).get(i);
                loggerDispatcher.debug("test colonne : " + nomColonne + " - " + typeColonne, LOGGER);
                if (!isColonneValide(nomColonne) && isTypeValide(typeColonne)) {
                    zeroErreur = false;
                    break;
                }
            }
            if (zeroErreur) {
                this.vObjectService.update(views.getViewSchemaNmcl());
            }
        }
        return generateDisplay(model, RESULT_SUCCESS);
    }

    public String sortSchemaNmcl(Model model) {
        this.vObjectService.sort(views.getViewSchemaNmcl());
        return basicAction(model, RESULT_SUCCESS);
    }

    public String deleteSchemaNmcl(Model model) {
        this.vObjectService.delete(views.getViewSchemaNmcl());
        return basicAction(model, RESULT_SUCCESS);
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
        try {
            UtilitaireDao.get(0).executeImmediate(null, "SELECT null as " + nomColonne);
        } catch (Exception e) {
            this.views.getViewSchemaNmcl().setMessage("nmclManagement.schema.error.invalidColumnName");
            this.views.getViewSchemaNmcl().setMessageArgs(nomColonne);
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
        try {
            UtilitaireDao.get(0).executeImmediate(null, "SELECT null::" + typeColonne);
        } catch (Exception e) {
            this.views.getViewSchemaNmcl().setMessage("nmclManagement.schema.error.invalidTypeName");
            this.views.getViewSchemaNmcl().setMessageArgs(typeColonne);
            return false;
        }
        return true;
    }


	
	
}
