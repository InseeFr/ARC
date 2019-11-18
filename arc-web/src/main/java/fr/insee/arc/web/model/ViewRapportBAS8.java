package fr.insee.arc.web.model;

import java.util.HashMap;

import fr.insee.arc.web.util.ConstantVObject;
import fr.insee.arc.web.util.VObject;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;

public class ViewRapportBAS8 extends VObject {
    public ViewRapportBAS8() {
        super();
        
        
		this.setTitle("view.envManagement.report");
		this.setPaginationSize(10);
        
        this.constantVObject = new ConstantVObject(new HashMap<String, ColumnRendering>() {

            /**
             * 
             */
            private static final long serialVersionUID = -4191692347748388055L;

            {
                put("date_entree", new ColumnRendering(true, "label.date.entry", "80px", "text", null, true));
                put("phase_traitement", new ColumnRendering(true, "Phase", "90px", "text", null, true));
                put("etat_traitement", new ColumnRendering(true, "Etat", "60px", "text", null, true));
                put("rapport", new ColumnRendering(true, "Rapport d'anomalie", "200px", "text", null, true));
                put("nb", new ColumnRendering(true, "Nombre de fichier", "55px", "text", null, true));

            }
        }

        );
    }
}