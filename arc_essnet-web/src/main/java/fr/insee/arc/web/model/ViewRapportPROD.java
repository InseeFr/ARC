package fr.insee.arc.web.model;

import java.util.HashMap;

import fr.insee.arc.web.util.ConstantVObject;
import fr.insee.arc.web.util.VObject;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;

public class ViewRapportPROD  extends VObject {
    public ViewRapportPROD() {
        super();
        this.constantVObject = new ConstantVObject(new HashMap<String, ColumnRendering>() {
            /**
			 * 
			 */
            private static final long serialVersionUID = -7999606005412359980L;

			{
                put("date_entree", new ColumnRendering(true, "Date d'entrée", "80px", "text", null, true));
                put("phase_traitement", new ColumnRendering(true, "Phase", "90px", "text", null, true));
                put("etat_traitement", new ColumnRendering(true, "Etat", "60px", "text", null, true));
                put("rapport", new ColumnRendering(true, "Rapport d'anomalie", "200px", "text", null, true));
                put("nb", new ColumnRendering(true, "Nombre de fichier", "55px", "text", null, true));
            
            }
        }

        );
    }
}