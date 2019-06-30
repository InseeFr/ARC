package fr.insee.arc.web.model;

import java.util.HashMap;

import fr.insee.arc.web.util.ConstantVObject;
import fr.insee.arc.web.util.VObject;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;

public class ViewFamilleNorme extends VObject {
    
    public ViewFamilleNorme() {
        super();
        this.constantVObject = new ConstantVObject(new HashMap<String, ColumnRendering>() {
            /**
             * 
             */
            private static final long serialVersionUID = -8367423233501279261L;

            {
                put("id_famille", new ColumnRendering(true, "Famille de norme", "100%", "text", null, true));
            }
        }

        );
    }
}
