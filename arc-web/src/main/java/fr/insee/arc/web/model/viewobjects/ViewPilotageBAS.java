package fr.insee.arc.web.model.viewobjects;

import java.util.HashMap;

import fr.insee.arc.web.util.ConstantVObject;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;
import fr.insee.arc.web.util.VObject;

public class ViewPilotageBAS extends VObject {
    /**
     * 
     */

    public ViewPilotageBAS() {
        super();
        
    	this.setTitle("view.envManagement");
    	this.setSessionName("viewPilotageBAS");
    	
    	this.setPaginationSize(5);
    	
        
        this.setConstantVObject(new ConstantVObject(new HashMap<String, ColumnRendering>() {

            /**
             *
             */
            private static final long serialVersionUID = 3294598955186326004L;

            {
                put("date_entree", new ColumnRendering(true, "label.date.entry", "10%", "text", null, true));
            }
        }

        ));
    }

}