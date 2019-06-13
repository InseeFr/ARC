package fr.insee.arc_essnet.web.model;

import java.util.HashMap;

import fr.insee.arc_essnet.web.util.ConstantVObject;
import fr.insee.arc_essnet.web.util.ConstantVObject.ColumnRendering;
import fr.insee.arc_essnet.web.util.VObject;

public class ViewListNomenclatures extends VObject {
	public ViewListNomenclatures() {
		super();
        this.constantVObject = new ConstantVObject(new HashMap<String, ColumnRendering>() {
            /**
			 * 
			 */
            private static final long serialVersionUID = 9180943017049417575L;

			{
                put("nom_table", new ColumnRendering(true, "Nomenclatures", "200px", "text", null, true));
                put("description", new ColumnRendering(true, "Description", "200px", "text", null, true));
            }
        }

        );
	}
}
