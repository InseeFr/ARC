package fr.insee.arc_essnet.web.model;

import java.util.HashMap;

import fr.insee.arc_essnet.web.util.ConstantVObject;
import fr.insee.arc_essnet.web.util.ConstantVObject.ColumnRendering;
import fr.insee.arc_essnet.web.util.VObject;

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