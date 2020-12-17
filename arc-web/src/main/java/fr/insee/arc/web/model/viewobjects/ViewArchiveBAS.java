package fr.insee.arc.web.model.viewobjects;

import java.util.HashMap;

import fr.insee.arc.web.util.ConstantVObject;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;
import fr.insee.arc.web.util.VObject;

public class ViewArchiveBAS extends VObject {
    public ViewArchiveBAS() {
        super();
        
        this.setTitle("view.envManagement.archive");
        this.setSessionName("viewArchiveBAS");
        
        this.setConstantVObject(new ConstantVObject(new HashMap<String, ColumnRendering>() {

            /**
             *
             */
            private static final long serialVersionUID = -7589043509155060981L;

            {
                put("entrepot", new ColumnRendering(true, "label.filestore", "25%", "text", null, true));
                put("nom_archive", new ColumnRendering(true, "label.archive", "75%", "text", null, true));

            }
        }

        ));
    }
}