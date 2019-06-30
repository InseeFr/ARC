package fr.insee.arc.web.model;

import java.util.HashMap;

import fr.insee.arc.web.util.ConstantVObject;
import fr.insee.arc.web.util.VObject;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;

public class ViewTableMetier extends VObject {
    public ViewTableMetier() {
        super();
        this.constantVObject = new ConstantVObject(new HashMap<String, ColumnRendering>() {
            private static final long serialVersionUID = -6829848051813971382L;
            {
                put("id_famille", new ColumnRendering(false, "Famille de norme", "", "text", null, false));
                put("nom_table_metier", new ColumnRendering(true, "Nom de la table", "60%", "text", null, true));
                put("description_table_metier", new ColumnRendering(true, "Description", "40%", "text", null, true));
            }
        }

        );
    }
}