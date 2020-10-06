package fr.insee.arc.web.model;

import java.util.HashMap;

import fr.insee.arc.web.util.ConstantVObject;
import fr.insee.arc.web.util.VObject;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;

public class ViewDirOut extends VObject {
    public ViewDirOut() {
        super();
        
        this.setTitle("view.dirOut");
        
        this.constantVObject = new ConstantVObject(

        new HashMap<String, ColumnRendering>() {
            /**
             * 
             */
            private static final long serialVersionUID = -3124381932840827423L;

            {
                put("aaa", new ColumnRendering(true, "aaa", "80px", "text", null, false));
             }
        });
    }
}