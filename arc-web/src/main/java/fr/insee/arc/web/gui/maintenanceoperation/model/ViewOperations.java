package fr.insee.arc.web.gui.maintenanceoperation.model;

import java.util.HashMap;
import java.util.Map;

import fr.insee.arc.web.gui.all.util.ConstantVObject;
import fr.insee.arc.web.gui.all.util.ConstantVObject.ColumnRendering;
import fr.insee.arc.web.gui.all.util.VObject;

public class ViewOperations extends VObject {

	private static final Map<String, ColumnRendering> columnMap = new HashMap<>();

	static {
		columnMap.put("i", new ColumnRendering(false, "i", "0%", "text", null, false));
	}

	public ViewOperations() {
		super();
		this.setTitle("view.operations");
		this.setSessionName("viewOperations");
		this.setDefaultPaginationSize(20);
		this.setConstantVObject(new ConstantVObject(columnMap));
	}
}