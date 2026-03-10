package fr.insee.arc.web.gui.file.model;

import java.util.HashMap;

import fr.insee.arc.web.gui.all.util.ConstantVObject;
import fr.insee.arc.web.gui.all.util.VObject;
import fr.insee.arc.web.gui.all.util.ConstantVObject.ColumnRendering;

public class ViewDirOut extends VObject {
    public ViewDirOut() {
        super();
        
        this.setTitle("view.dirOut");
        this.setSessionName("viewDirOut");
		this.setDefaultPaginationSize(20);

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