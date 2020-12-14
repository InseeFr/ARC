package fr.insee.arc.web.model.viewobjects;

import java.util.HashMap;

import fr.insee.arc.utils.dao.PreparedStatementBuilder;
import fr.insee.arc.web.util.ConstantVObject;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;
import fr.insee.arc.web.util.VObject;

public class ViewCalendrier extends VObject {
    public ViewCalendrier() {
	super();
	
	this.setTitle("view.calendar");
	
	this.setPaginationSize(15);
	
	this.setSessionName("viewCalendrier");
	
	this.setConstantVObject(new ConstantVObject(new HashMap<String, ColumnRendering>() {
	    /**
	    	 * 
	    	 */
	    private static final long serialVersionUID = 1317794848630970765L;

	    {
		put("id", new ColumnRendering(false, "label.id", "0%", "text", null, false));
		put("id_norme", new ColumnRendering(false, "label.norm", "0", "text", null, true));
		put("periodicite", new ColumnRendering(false, "label.periodicity", "0", "text", null, true));
		put("validite_inf", new ColumnRendering(true, "label.validity.min", "30%", "text", null, true));
		put("validite_sup", new ColumnRendering(true, "label.validity.max", "30%", "text", null, true));
		put("etat", new ColumnRendering(true, "label.state", "30%", "select",
			new PreparedStatementBuilder("select id, val from arc.ext_etat order by id desc"), true));

	    }
	}

	));
    }
}