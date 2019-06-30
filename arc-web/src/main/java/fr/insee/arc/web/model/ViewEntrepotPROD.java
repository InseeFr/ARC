package fr.insee.arc.web.model;

import java.util.HashMap;

import fr.insee.arc.web.util.ConstantVObject;
import fr.insee.arc.web.util.VObject;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;

public class ViewEntrepotPROD extends VObject {
    public ViewEntrepotPROD() {
        super();
        this.constantVObject = new ConstantVObject(new HashMap<String, ColumnRendering>() {
            /**
			 * 
			 */
            private static final long serialVersionUID = 332976263989793768L;

			{

                put("id_entrepot", new ColumnRendering(true, "Entrepot", "60px", "text", null, true));
            }
        }

        );
    }
}