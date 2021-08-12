package fr.insee.arc.web.model.viewobjects;

import java.util.HashMap;

import fr.insee.arc.web.util.ConstantVObject;
import fr.insee.arc.web.util.VObject;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;


public class ViewParameters extends VObject {

	private static final HashMap<String, ColumnRendering> columnMap= new HashMap<>();

	static {
		columnMap.put("description", new ColumnRendering(true, "parameter.header.description", "70%", "text", null, true));
		columnMap.put("key", new ColumnRendering(true, "key", "20%", "text", null, true));
		columnMap.put("val", new ColumnRendering(true, "val", "10%", "text", null, true));

	}
	
	public ViewParameters() {
        super();
        this.setTitle("view.parameters");
        this.setSessionName("viewParameters");
    	this.setDefaultPaginationSize(0);
        this.setConstantVObject(new ConstantVObject(columnMap));
    }
}