package fr.insee.arc.web.gui.index.model;

import java.util.HashMap;

import fr.insee.arc.web.gui.all.util.ConstantVObject;
import fr.insee.arc.web.gui.all.util.VObject;
import fr.insee.arc.web.gui.all.util.ConstantVObject.ColumnRendering;

public class ViewIndex extends VObject {
	
	public ViewIndex() {
		super();
		
		this.setTitle("view.index");
		this.setDefaultPaginationSize(0);
		this.setSessionName("viewIndex");
		
		this.setConstantVObject(new ConstantVObject(new HashMap<String, ColumnRendering>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 8105655458290481902L;

			{
				put("id", new ColumnRendering(false, "label.id", "", "text", null, false));
				put("val", new ColumnRendering(true, "label.sandbox", "20%", "text", null, false));
				put("env_description", new ColumnRendering(true, "label.sandbox.user", "80%", "text", null, false));
			}
		}
		));
	}

}
