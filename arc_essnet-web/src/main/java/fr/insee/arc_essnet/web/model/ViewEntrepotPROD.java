package fr.insee.arc_essnet.web.model;

import java.util.HashMap;

import fr.insee.arc_essnet.web.util.ConstantVObject;
import fr.insee.arc_essnet.web.util.ConstantVObject.ColumnRendering;
import fr.insee.arc_essnet.web.util.VObject;

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