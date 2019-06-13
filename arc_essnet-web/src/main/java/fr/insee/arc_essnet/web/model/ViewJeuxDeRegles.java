package fr.insee.arc_essnet.web.model;

import java.util.HashMap;

import fr.insee.arc_essnet.web.util.ConstantVObject;
import fr.insee.arc_essnet.web.util.ConstantVObject.ColumnRendering;
import fr.insee.arc_essnet.web.util.VObject;

public class ViewJeuxDeRegles extends VObject {
    public ViewJeuxDeRegles() {
        super();
        this.constantVObject = new ConstantVObject(new HashMap<String, ColumnRendering>() {
            /**
			 * 
			 */
            private static final long serialVersionUID = 7742353075723133064L;

			{
                put("id_norme", new ColumnRendering(false, "Norme", "0", "text", null, true));
                put("periodicite", new ColumnRendering(false, "Periodicite", "0", "text", null, true));
                put("validite_inf", new ColumnRendering(false, "Debut Validite", "0", "text", null, true));
                put("validite_sup", new ColumnRendering(false, "Fin Validite", "0", "text", null, true));
                put("version", new ColumnRendering(true, "Version", "60px", "text", null, true));
                put("etat", new ColumnRendering(true, "Statut", "120px", "select", "select id, val from arc.ext_etat_jeuderegle order by id", true));
                put("date_production", new ColumnRendering(true, "Date production", "80px", "text", null, true));
                put("date_inactif", new ColumnRendering(true, "Date inactif", "80px", "text", null, true));

            }

        }

        );
    }
}