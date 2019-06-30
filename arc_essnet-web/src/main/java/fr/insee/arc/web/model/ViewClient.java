package fr.insee.arc.web.model;

import java.util.HashMap;

import fr.insee.arc.web.util.ConstantVObject;
import fr.insee.arc.web.util.VObject;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;

public class ViewClient extends VObject {
	public ViewClient() {
        super();
        this.constantVObject = new ConstantVObject(new HashMap<String, ColumnRendering>() {
            /**
             *
             */
            private static final long serialVersionUID = 8263314437188358833L;

            {
                put("id_famille", new ColumnRendering(false, "Famille", "20%", "text", null, true));
                put("id_application", new ColumnRendering(true, "Application cliente", "80%", "text", null, true));

            }
        }

        );
    }

}
