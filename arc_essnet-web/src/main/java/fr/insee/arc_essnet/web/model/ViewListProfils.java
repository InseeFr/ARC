package fr.insee.arc_essnet.web.model;

import java.util.HashMap;

import fr.insee.arc_essnet.web.util.ConstantVObject;
import fr.insee.arc_essnet.web.util.ConstantVObject.ColumnRendering;
import fr.insee.arc_essnet.web.util.VObject;

public class ViewListProfils extends VObject {
    @SuppressWarnings("serial")
	public ViewListProfils() {
        super();
        this.constantVObject = new ConstantVObject(new HashMap<String, ColumnRendering>() {
            {
                put("groupe", new ColumnRendering(true, "Groupe", "200px", "text", null, false));
                put("lib_groupe", new ColumnRendering(true, "Libelle du groupe", "200px", "text", null, false));
            }
        }

        );
    }
}
