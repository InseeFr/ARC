package fr.insee.arc_essnet.web.model;

import java.util.HashMap;

import fr.insee.arc_essnet.web.util.ConstantVObject;
import fr.insee.arc_essnet.web.util.ConstantVObject.ColumnRendering;
import fr.insee.arc_essnet.web.util.VObject;

public class ViewRapportBAS8 extends VObject {
    public ViewRapportBAS8() {
        super();
        this.constantVObject = new ConstantVObject(new HashMap<String, ColumnRendering>() {

            /**
             * 
             */
            private static final long serialVersionUID = -4191692347748388055L;

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