package fr.insee.arc.web.model.viewobjects;

import java.util.HashMap;

import fr.insee.arc.web.util.ConstantVObject;
import fr.insee.arc.web.util.VObject;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;


public class ViewParameters extends VObject {

	private static final HashMap<String, ColumnRendering> columnMap= new HashMap<>();

	static {
		columnMap.put("description", new ColumnRendering(true, "label.description", "70%", "text", null, false));
		columnMap.put("key", new ColumnRendering(true, "label.key", "20%", "text", null, true));
		columnMap.put("val", new ColumnRendering(true, "label.val", "10%", "text", null, true));

	}
	
	public ViewParameters() {
        super();
        this.setTitle("view.parameters");
        this.setSessionName("viewParameters");
    	this.setDefaultPaginationSize(0);
        this.setConstantVObject(new ConstantVObject(columnMap));
    }
}