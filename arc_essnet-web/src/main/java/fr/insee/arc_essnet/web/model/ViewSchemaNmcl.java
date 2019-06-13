package fr.insee.arc_essnet.web.model;

import java.util.HashMap;

import fr.insee.arc_essnet.web.util.ConstantVObject;
import fr.insee.arc_essnet.web.util.ConstantVObject.ColumnRendering;
import fr.insee.arc_essnet.web.util.VObject;

public class ViewSchemaNmcl extends VObject{

    
    public ViewSchemaNmcl() {
        super();
        this.constantVObject = new ConstantVObject(new HashMap<String, ColumnRendering>() {
            /**
             * 
             */
            private static final long serialVersionUID = -6107441894921031807L;

            {
                put("type_nmcl", new ColumnRendering(false, "Type de la nomenclature", "200px", "text", null, true));
                put("nom_colonne", new ColumnRendering(true, "Variable", "100px", "text", null, true));
                put("type_colonne", new ColumnRendering(true, "Type", "100px", "text", null, true));

            }
        }

        );
    }
    
    
    
}
