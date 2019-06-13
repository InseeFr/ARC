package fr.insee.arc_essnet.web.model;

import java.util.HashMap;

import fr.insee.arc_essnet.web.util.ConstantVObject;
import fr.insee.arc_essnet.web.util.ConstantVObject.ColumnRendering;
import fr.insee.arc_essnet.web.util.VObject;

public class ViewCalendrier extends VObject {
    public ViewCalendrier() {
	super();
	this.constantVObject = new ConstantVObject(new HashMap<String, ColumnRendering>() {
	    /**
	    	 * 
	    	 */
	    private static final long serialVersionUID = 1317794848630970765L;

	    {
		put("id", new ColumnRendering(false, "id", "0%", "text", null, false));
		put("id_norme", new ColumnRendering(false, "Norme", "0", "text", null, true));
		put("periodicite", new ColumnRendering(false, "Periodicite", "0", "text", null, true));
		put("validite_inf", new ColumnRendering(true, "Debut Validite", "30%", "text", null, true));
		put("validite_sup", new ColumnRendering(true, "Fin Validite", "30%", "text", null, true));
		put("etat", new ColumnRendering(true, "Etat", "30%", "select",
			"select id, val from arc.ext_etat order by id desc", true));

	    }
	}

	);
    }
}