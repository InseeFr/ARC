package fr.insee.arc.web.gui.famillenorme.model;

import java.util.HashMap;

import fr.insee.arc.web.gui.all.util.ConstantVObject;
import fr.insee.arc.web.gui.all.util.VObject;
import fr.insee.arc.web.gui.all.util.ConstantVObject.ColumnRendering;

public class ViewHostAllowed  extends VObject {

    public ViewHostAllowed() {
        super();
        

        this.setTitle("view.mapmodel.whitelist");
        this.setDefaultPaginationSize(15);
        this.setSessionName("viewHostAllowed");
        
        this.setConstantVObject(new ConstantVObject(new HashMap<String, ColumnRendering>() {
            private static final long serialVersionUID = -6829848051813971382L;
            {
                put("id_webservice_whitelist", new ColumnRendering(false, "Id.", "", "text", null, false));
                put("id_famille", new ColumnRendering(false, "label.normFamily", "", "text", null, true));
                put("id_application", new ColumnRendering(false, "label.client.software", "", "text", null, true));
                put("host_allowed", new ColumnRendering(true, "label.webservice.host.allowed", "80%", "text", null, true));
                put("is_secured", new ColumnRendering(true, "label.webservice.host.secured", "20%", "text", null, true));
            }
        }));
    }
    
}