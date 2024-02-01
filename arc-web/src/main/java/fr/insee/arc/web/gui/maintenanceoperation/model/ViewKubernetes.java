package fr.insee.arc.web.gui.maintenanceoperation.model;

import java.util.HashMap;
import java.util.Map;

import fr.insee.arc.web.gui.all.util.ConstantVObject;
import fr.insee.arc.web.gui.all.util.ConstantVObject.ColumnRendering;
import fr.insee.arc.web.gui.all.util.VObject;


public class ViewKubernetes extends VObject {

	private static final Map<String, ColumnRendering> columnMap= new HashMap<>();

	static {
		columnMap.put("i", new ColumnRendering(false, "i", "0%", "text", null, false));
		columnMap.put("key", new ColumnRendering(true, "label.key", "15%", "text", null, true));
		columnMap.put("val", new ColumnRendering(true, "label.val", "15%", "text", null, true));
		columnMap.put("description", new ColumnRendering(true, "label.description", "70%", "text", null, false));

	}
	
	public ViewKubernetes() {
        super();
        this.setTitle("view.viewKubernetes");
        this.setSessionName("viewKubernetes");
    	this.setDefaultPaginationSize(20);
        this.setConstantVObject(new ConstantVObject(columnMap));
    }
}