package fr.insee.arc.web.gui.famillenorme.model;

import java.util.HashMap;

import fr.insee.arc.web.gui.all.util.ConstantVObject;
import fr.insee.arc.web.gui.all.util.VObject;
import fr.insee.arc.web.gui.all.util.ConstantVObject.ColumnRendering;

public class ViewTableMetier extends VObject {
    public ViewTableMetier() {
    	super();
    	
    	this.setTitle("view.mapmodel.tables");
    	this.setDefaultPaginationSize(10);
    	this.setSessionName("viewTableMetier");
    	    	
        this.setConstantVObject(new ConstantVObject(new HashMap<String, ColumnRendering>() {
            private static final long serialVersionUID = -6829848051813971382L;
            {
                put("id_famille", new ColumnRendering(false, "label.normFamily", "", "text", null, false));
                put("nom_table_metier", new ColumnRendering(true, "label.tablename", "60%", "text", null, true));
                put("description_table_metier", new ColumnRendering(true, "label.comment", "40%", "text", null, true));
            }
        }

        ));
    }
}