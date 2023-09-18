package fr.insee.arc.web.gui.norme.model;

import java.util.HashMap;

import fr.insee.arc.web.gui.all.util.ConstantVObject;
import fr.insee.arc.web.gui.all.util.VObject;
import fr.insee.arc.web.gui.all.util.ConstantVObject.ColumnRendering;

public class ViewModules extends VObject {
    public ViewModules() {
        super();
        
    	this.setTitle("view.modules");
    	this.setSessionName("viewModules");
    	this.setDefaultPaginationSize(0);
    	
        this.setConstantVObject(new ConstantVObject(new HashMap<String, ColumnRendering>() {

			private static final long serialVersionUID = -2650780865638129839L;

            {
                put("id", new ColumnRendering(true, "id", "100%", "text", null, false));
            }
        }

        ));
    }
}