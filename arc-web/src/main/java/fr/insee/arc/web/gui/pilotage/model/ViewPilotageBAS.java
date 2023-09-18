package fr.insee.arc.web.gui.pilotage.model;

import java.util.HashMap;

import fr.insee.arc.web.gui.all.util.ConstantVObject;
import fr.insee.arc.web.gui.all.util.VObject;
import fr.insee.arc.web.gui.all.util.ConstantVObject.ColumnRendering;

public class ViewPilotageBAS extends VObject {
    /**
     * 
     */

    public ViewPilotageBAS() {
        super();
        
    	this.setTitle("view.envManagement");
    	this.setSessionName("viewPilotageBAS");
    	
    	this.setDefaultPaginationSize(5);
    	
        
        this.setConstantVObject(new ConstantVObject(new HashMap<String, ColumnRendering>() {

            /**
             *
             */
            private static final long serialVersionUID = 3294598955186326004L;

            {
                put("date_entree", new ColumnRendering(true, "label.date.entry", "17%", "text", null, true));
            }
        }

        ));
    }

}