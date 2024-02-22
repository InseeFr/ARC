package fr.insee.arc.web.gui.entrepot.model;

import java.util.HashMap;
import java.util.Map;

import fr.insee.arc.web.gui.all.util.ConstantVObject;
import fr.insee.arc.web.gui.all.util.VObject;
import fr.insee.arc.web.gui.all.util.ConstantVObject.ColumnRendering;

public class ViewEntrepot extends VObject {
	
	private static final Map<String, ColumnRendering> columnMap= new HashMap<>();

	static {
		columnMap.put("i", new ColumnRendering(false, "i", "0%", "text", null, false));
		columnMap.put("id_entrepot", new ColumnRendering(true, "label.filestore", "50%", "text", null, true));
		columnMap.put("id_loader", new ColumnRendering(true, "label.loader", "50%", "text", null, true));

	}
	
	public ViewEntrepot() {
        super();
        this.setTitle("view.entrepot");
        this.setSessionName("viewEntrepot");
    	this.setDefaultPaginationSize(20);
        this.setConstantVObject(new ConstantVObject(columnMap));
    }

}
