package fr.insee.arc.web.model;

import java.util.HashMap;

import fr.insee.arc.web.util.ConstantVObject;
import fr.insee.arc.web.util.VObject;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;

public class ViewListNomenclatures extends VObject {
	public ViewListNomenclatures() {
		super();
		
		this.setTitle("view.nomenclatureList");
		this.setPaginationSize(15);
		
        this.constantVObject = new ConstantVObject(new HashMap<String, ColumnRendering>() {
            /**
			 * 
			 */
            private static final long serialVersionUID = 9180943017049417575L;

			{
                put("nom_table", new ColumnRendering(true, "label.tablename", "100%", "text", null, true));
                put("description", new ColumnRendering(true, "label.comment", "75%", "text", null, true));
            }
        }

        );
	}
}
