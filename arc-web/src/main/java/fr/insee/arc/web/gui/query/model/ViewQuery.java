package fr.insee.arc.web.gui.query.model;

import java.util.HashMap;

import fr.insee.arc.web.gui.all.util.ConstantVObject;
import fr.insee.arc.web.gui.all.util.VObject;
import fr.insee.arc.web.gui.all.util.ConstantVObject.ColumnRendering;

public class ViewQuery extends VObject {
    public ViewQuery() {
        super();
        this.setTitle("view.query");

        this.setSessionName("viewQuery");
        
        this.setDefaultPaginationSize(10);

        this.setConstantVObject(new ConstantVObject(

        new HashMap<String, ColumnRendering>() {
            /**
			 * 
			 */
            private static final long serialVersionUID = 832055657265749083L;

			{
                put("aaa", new ColumnRendering(true, "aaa", "100%", "text", null, false));
             }
        }));
    }
}