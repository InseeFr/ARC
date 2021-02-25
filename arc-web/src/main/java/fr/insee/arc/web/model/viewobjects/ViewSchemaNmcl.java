package fr.insee.arc.web.model.viewobjects;

import java.util.HashMap;

import fr.insee.arc.web.util.ConstantVObject;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;
import fr.insee.arc.web.util.VObject;

public class ViewSchemaNmcl extends VObject{

    
    public ViewSchemaNmcl() {
        super();
        
		this.setTitle("view.nomenclatureSchema");
		this.setDefaultPaginationSize(15);
        this.setSessionName("viewSchemaNmcl");
        this.setConstantVObject(new ConstantVObject(new HashMap<String, ColumnRendering>() {
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

        ));
    }
    
    
    
}
