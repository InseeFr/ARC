package fr.insee.arc.web.model;

import java.util.HashMap;

import fr.insee.arc.web.util.ConstantVObject;
import fr.insee.arc.web.util.VObject;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;

public class ViewQuery extends VObject {
    public ViewQuery() {
        super();
        this.setTitle("view.query");

        this.constantVObject = new ConstantVObject(

        new HashMap<String, ColumnRendering>() {
            /**
			 * 
			 */
            private static final long serialVersionUID = 832055657265749083L;

			{
                put("aaa", new ColumnRendering(true, "aaa", "80px", "text", null, false));
             }
        });
    }
}