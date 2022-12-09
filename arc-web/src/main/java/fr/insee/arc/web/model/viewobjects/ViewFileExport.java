package fr.insee.arc.web.model.viewobjects;

import java.util.HashMap;

import fr.insee.arc.web.util.ConstantVObject;
import fr.insee.arc.web.util.VObject;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;


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
                put("filename", new ColumnRendering(true, "Nom du fichier", "100%", "text", null, true));
                put("isdirectory", new ColumnRendering(false, "Répertoire", "47%", "text", null, false));
             }
        }));
    }
}