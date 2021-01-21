package fr.insee.arc.web.model.viewobjects;

import java.util.HashMap;

import fr.insee.arc.web.util.ConstantVObject;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;
import fr.insee.arc.web.util.VObject;

public class ViewRapportBAS extends VObject {
    public ViewRapportBAS() {
        super();
        
        
		this.setTitle("view.envManagement.report");
		this.setSessionName("viewRapportBAS");
		this.setPaginationSize(10);
        
        this.setConstantVObject(new ConstantVObject(new HashMap<String, ColumnRendering>() {

            /**
             * 
             */
            private static final long serialVersionUID = -4191692347748388055L;

            {
                put("date_entree", new ColumnRendering(true, "label.date.entry", "17%", "text", null, true));
                put("phase_traitement", new ColumnRendering(true, "label.step.name", "19%", "text", null, true));
                put("etat_traitement", new ColumnRendering(true, "label.step.status", "12%", "text", null, true));
                put("rapport", new ColumnRendering(true, "label.step.report", "41%", "text", null, true));
                put("nb", new ColumnRendering(true, "label.step.count.file", "11%", "text", null, true));

            }
        }

        ));
    }
}