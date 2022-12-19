package fr.insee.arc.web.gui.norme.model;

import java.util.HashMap;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.web.util.ConstantVObject;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;
import fr.insee.arc.web.util.VObject;

public class ViewJeuxDeRegles extends VObject {
    public ViewJeuxDeRegles() {
        super();
        
        this.setTitle("view.ruleset");

        this.setDefaultPaginationSize(15);
        
        this.setSessionName("viewJeuxDeRegles");

        this.setConstantVObject(new ConstantVObject(new HashMap<String, ColumnRendering>() {
            /**
			 * 
			 */
            private static final long serialVersionUID = 7742353075723133064L;

			{
                put("id_norme", new ColumnRendering(false, "label.norm", "0", "text", null, true));
                put("periodicite", new ColumnRendering(false, "label.periodicity", "0", "text", null, true));
                put("validite_inf", new ColumnRendering(false, "label.validity.min", "0", "text", null, true));
                put("validite_sup", new ColumnRendering(false, "label.validity.max", "0", "text", null, true));
                put("version", new ColumnRendering(true, "label.version", "40%", "text", null, true));
                put("etat", new ColumnRendering(true, "label.state", "60%", "select", new ArcPreparedStatementBuilder("select id, val from arc.ext_etat_jeuderegle order by id"), true));
                put("date_production", new ColumnRendering(false, "label.date.production", "33%", "text", null, true));
                put("date_inactif", new ColumnRendering(false, "label.date.disable", "24%", "text", null, true));

            }

        }

        ));
    }
}