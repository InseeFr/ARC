package fr.insee.arc.web.model;

import java.util.HashMap;

import fr.insee.arc.web.util.ConstantVObject;
import fr.insee.arc.web.util.VObject;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;

public class ViewSchemaNmcl extends VObject{

    
    public ViewSchemaNmcl() {
        super();
        this.constantVObject = new ConstantVObject(new HashMap<String, ColumnRendering>() {
            /**
             * 
             */
            private static final long serialVersionUID = -6107441894921031807L;

            {
                put("type_nmcl", new ColumnRendering(false, "Type de la nomenclature", "100%", "text", null, true));
                put("nom_colonne", new ColumnRendering(true, "Variable", "100%", "text", null, true));
                put("type_colonne", new ColumnRendering(true, "Type", "50%", "text", null, true));

            }
        }

        );
    }
    
    
    
}
