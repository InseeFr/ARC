package fr.insee.arc.web.gui.famillenorme.model;

import java.util.HashMap;

import fr.insee.arc.web.util.ConstantVObject;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;
import fr.insee.arc.web.util.VObject;

public class ViewClient extends VObject {
	public ViewClient() {
        super();
        
        this.setTitle("view.client.softwares");
		this.setDefaultPaginationSize(5);
		this.setSessionName("viewClient");
		
		
        this.setConstantVObject(new ConstantVObject(new HashMap<String, ColumnRendering>() {
            /**
             *
             */
            private static final long serialVersionUID = 8263314437188358833L;

            {
                put("id_famille", new ColumnRendering(false, "label.normFamily", "0%", "text", null, true));
                put("id_application", new ColumnRendering(true, "label.client.software", "100%", "text", null, true));

            }
        }

        ));
    }

}
