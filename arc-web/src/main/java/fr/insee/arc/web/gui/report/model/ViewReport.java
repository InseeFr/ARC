package fr.insee.arc.web.gui.report.model;

import java.util.HashMap;

import fr.insee.arc.web.gui.all.util.ConstantVObject;
import fr.insee.arc.web.gui.all.util.ConstantVObject.ColumnRendering;
import fr.insee.arc.web.gui.all.util.VObject;

public class ViewReport extends VObject {
    public ViewReport() {
	super();

	this.setTitle("view.report");
	
	this.setDefaultPaginationSize(15);
	
	this.setSessionName("viewReport");

	this.setConstantVObject(new ConstantVObject(
		
		new HashMap<String, ColumnRendering>() {
		    /**
		    	 * 
		    	 */
		    private static final long serialVersionUID = 4705381559117478720L;

		    {
			put("id", new ColumnRendering(false, "label.id", "0%", "text", null, false));
		    }
		}));
    }
}