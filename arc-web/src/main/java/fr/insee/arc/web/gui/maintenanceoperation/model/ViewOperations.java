package fr.insee.arc.web.gui.maintenanceoperation.model;

import java.util.HashMap;

import fr.insee.arc.web.util.ConstantVObject;
import fr.insee.arc.web.util.VObject;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;


public class ViewOperations extends VObject {

	private static final HashMap<String, ColumnRendering> columnMap= new HashMap<>();

	static {
		columnMap.put("i", new ColumnRendering(false, "i", "0%", "text", null, false));
		columnMap.put("key", new ColumnRendering(true, "label.key", "15%", "text", null, true));
		columnMap.put("val", new ColumnRendering(true, "label.val", "15%", "text", null, true));
		columnMap.put("description", new ColumnRendering(true, "label.description", "70%", "text", null, false));

	}
	
	public ViewOperations() {
        super();
        this.setTitle("view.operations");
        this.setSessionName("viewOperations");
    	this.setDefaultPaginationSize(20);
        this.setConstantVObject(new ConstantVObject(columnMap));
    }
}