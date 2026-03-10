package fr.insee.arc.web.gui.export.model;

import java.util.HashMap;

import fr.insee.arc.web.gui.all.util.ConstantVObject;
import fr.insee.arc.web.gui.all.util.VObject;
import fr.insee.arc.web.gui.all.util.ConstantVObject.ColumnRendering;


public class ViewFileExport extends VObject {
    public ViewFileExport() {
        super();
        
        this.setTitle("view.exportFile");
        this.setSessionName("viewFileExport");
    	this.setDefaultPaginationSize(0);
        
        this.setConstantVObject(new ConstantVObject(

        new HashMap<String, ColumnRendering>() {
            /**
             * 
             */
            private static final long serialVersionUID = -3124381932840827423L;

            {
                put("filename", new ColumnRendering(true, "Nom du fichier", "90%", "text", null, true));
                put("isdirectory", new ColumnRendering(true, "Répertoire", "10%", "text", null, false));
             }
        }));
    }
}