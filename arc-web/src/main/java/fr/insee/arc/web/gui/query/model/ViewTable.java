package fr.insee.arc.web.gui.query.model;

import java.util.HashMap;

import fr.insee.arc.web.gui.all.util.ConstantVObject;
import fr.insee.arc.web.gui.all.util.VObject;
import fr.insee.arc.web.gui.all.util.ConstantVObject.ColumnRendering;

public class ViewTable extends VObject {
    public ViewTable() {
        super();
        this.setTitle("view.schematable");
        
        this.setSessionName("viewTable");

        this.setDefaultPaginationSize(25);
        
        this.setConstantVObject(new ConstantVObject(

        new HashMap<String, ColumnRendering>() {
            /**
			 * 
			 */
            private static final long serialVersionUID = -3124381932840827423L;

			{
                put("aaa", new ColumnRendering(true, "aaa", "100%", "text", null, false));
             }
        }));
    }
}