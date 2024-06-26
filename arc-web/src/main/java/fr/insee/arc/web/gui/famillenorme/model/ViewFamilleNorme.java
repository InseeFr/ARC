package fr.insee.arc.web.gui.famillenorme.model;

import java.util.HashMap;

import fr.insee.arc.web.gui.all.util.ConstantVObject;
import fr.insee.arc.web.gui.all.util.VObject;
import fr.insee.arc.web.gui.all.util.ConstantVObject.ColumnRendering;

public class ViewFamilleNorme extends VObject {
    
    public ViewFamilleNorme() {
        super();
        
        this.setTitle("view.family");
    	this.setDefaultPaginationSize(10);
    	this.setSessionName("viewFamilleNorme");
    	
        this.setConstantVObject(new ConstantVObject(new HashMap<String, ColumnRendering>() {
            /**
             * 
             */
            private static final long serialVersionUID = -8367423233501279261L;

            {
                put("id_famille", new ColumnRendering(true, "label.normFamily", "100%", "text", null, true));
            }
        }

        ));
    }

}
