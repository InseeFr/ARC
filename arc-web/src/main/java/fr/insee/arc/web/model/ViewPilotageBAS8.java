package fr.insee.arc.web.model;

import java.util.ArrayList;
import java.util.HashMap;

import fr.insee.arc.web.util.ConstantVObject;
import fr.insee.arc.web.util.VObject;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;

public class ViewPilotageBAS8 extends VObject {
    /**
     * 
     */
    private static final long serialVersionUID = -8577430079827164584L;

    /**
	 *
	 */

    public ViewPilotageBAS8() {
        super();
        
    	this.setTitle("view.envManagement");
    	
    	this.setPaginationSize(5);
    	
        
        this.constantVObject = new ConstantVObject(new HashMap<String, ColumnRendering>() {

            /**
             *
             */
            private static final long serialVersionUID = 3294598955186326004L;

            {
                put("date_entree", new ColumnRendering(true, "label.date.entry", "4%", "text", null, true));
            }
        }

        );
    }

    @Override
    public ArrayList<ArrayList<String>> reworkContent(ArrayList<ArrayList<String>> content) {
        return ViewPilotage.reworkContentPilotage(content);

    }

}