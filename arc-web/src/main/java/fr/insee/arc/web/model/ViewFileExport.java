package fr.insee.arc.web.model;

import java.util.HashMap;

import fr.insee.arc.web.util.ConstantVObject;
import fr.insee.arc.web.util.VObject;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;


public class ViewFileExport extends VObject {
    public ViewFileExport() {
        super();
        
        this.setTitle("view.exportFile");
    	this.setPaginationSize(0);
        
        this.constantVObject = new ConstantVObject(

        new HashMap<String, ColumnRendering>() {
            /**
             * 
             */
            private static final long serialVersionUID = -3124381932840827423L;

            {
                put("filename", new ColumnRendering(true, "Nom du fichier", "400px", "text", null, false));
                put("isdirectory", new ColumnRendering(false, "Répertoire", "350px", "text", null, false));
             }
        });
    }
}