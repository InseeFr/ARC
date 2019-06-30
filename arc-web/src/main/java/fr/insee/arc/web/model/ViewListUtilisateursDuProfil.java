package fr.insee.arc.web.model;

import java.util.HashMap;

import fr.insee.arc.web.util.ConstantVObject;
import fr.insee.arc.web.util.VObject;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;

public class ViewListUtilisateursDuProfil extends VObject {
    @SuppressWarnings("serial")
	public ViewListUtilisateursDuProfil() {
        super();
        this.constantVObject = new ConstantVObject(new HashMap<String, ColumnRendering>() {
            {
                put("idep", new ColumnRendering(true, "Idep", "100px", "text", null, false));
                put("nom_prenom", new ColumnRendering(true, "Nom Prenom", "200px", "text", null, true));
                put("groupe", new ColumnRendering(false, "Groupe", "200px", "text", null, true));
                put("lib_groupe", new ColumnRendering(false, "Libelle du groupe", "200px", "text", null, true));
            }
        }

        );
    }
}
