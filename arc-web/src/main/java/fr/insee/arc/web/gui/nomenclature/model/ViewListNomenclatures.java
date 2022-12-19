package fr.insee.arc.web.gui.nomenclature.model;

import java.util.HashMap;

import fr.insee.arc.web.util.ConstantVObject;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;
import fr.insee.arc.web.util.VObject;

public class ViewListNomenclatures extends VObject {
	public ViewListNomenclatures() {
		super();
		
		this.setTitle("view.nomenclatureList");
		this.setDefaultPaginationSize(12);
		this.setSessionName("viewListNomenclatures");
        this.setConstantVObject(new ConstantVObject(new HashMap<String, ColumnRendering>() {
            /**
			 * 
			 */
            private static final long serialVersionUID = 9180943017049417575L;

			{
                put("nom_table", new ColumnRendering(true, "label.tablename", "100%", "text", null, true));
                put("description", new ColumnRendering(true, "label.comment", "75%", "text", null, true));
            }
        }

        ));
	}
}
