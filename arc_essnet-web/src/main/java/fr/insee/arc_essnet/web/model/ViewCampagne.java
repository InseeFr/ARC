package fr.insee.arc_essnet.web.model;

import java.util.HashMap;

import fr.insee.arc_essnet.web.util.ConstantVObject;
import fr.insee.arc_essnet.web.util.ConstantVObject.ColumnRendering;
import fr.insee.arc_essnet.web.util.VObject;

public class ViewCampagne extends VObject {
	public ViewCampagne() {
        super();
        this.constantVObject = new ConstantVObject(new HashMap<String, ColumnRendering>() {
            /**
			 * 
			 */
            private static final long serialVersionUID = 4196377593469401943L;

			{
                put("id_norme", new ColumnRendering(true, "Norme", "5", "text", null, true));
                put("id_source", new ColumnRendering(true, "Source", "5", "text", null, true));

            }
        }

        );
    }
}