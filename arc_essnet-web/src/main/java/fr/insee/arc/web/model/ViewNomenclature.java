package fr.insee.arc.web.model;

import java.util.HashMap;

import fr.insee.arc.web.util.ConstantVObject;
import fr.insee.arc.web.util.VObject;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;

public class ViewNomenclature extends VObject {
	public ViewNomenclature() {
		super();
        this.constantVObject = new ConstantVObject(new HashMap<String, ColumnRendering>() {
            /**
			 * 
			 */
            private static final long serialVersionUID = 595652512540034630L;

			{
                put("0", new ColumnRendering(true, "", "", "", null, true));
            }
        }

        );
	}
}
