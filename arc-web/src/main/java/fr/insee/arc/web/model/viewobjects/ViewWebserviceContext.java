package fr.insee.arc.web.model.viewobjects;

import java.util.HashMap;

import fr.insee.arc.utils.dao.PreparedStatementBuilder;
import fr.insee.arc.web.util.ConstantVObject;
import fr.insee.arc.web.util.VObject;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;

public class ViewWebserviceContext extends VObject {
    public ViewWebserviceContext() {
        super();
        
		this.setTitle("view.webserviceContext");
		this.setSessionName("viewWebserviceContext");
		this.setPaginationSize(15);
        
        this.setConstantVObject(new ConstantVObject(new HashMap<String, ColumnRendering>() {
            /**
			 * 
			 */
            private static final long serialVersionUID = 7742353075723133064L;

			{
                put("service_name", new ColumnRendering(true, "Nom du service", "20%", "text", null, true));
                put("call_id", new ColumnRendering(true, "Id de service", "10%", "text", null, true));
                put("service_type", new ColumnRendering(true, "Type de service", "20%", "select", new PreparedStatementBuilder("select id, val from arc.ext_webservice_type order by id"), true));
                put("environment", new ColumnRendering(true, "Environnement", "20%", "select", new PreparedStatementBuilder("select id, val from arc.ext_etat_jeuderegle order by id"), true));
                put("target_phase", new ColumnRendering(true, "n° de phase", "10%", "text", null, true));
                put("norme", new ColumnRendering(true, "Norme", "15%", "text", null, true));
                put("validite", new ColumnRendering(true, "Validite", "15%", "text", null, true));
                put("periodicite", new ColumnRendering(true, "Periodicite", "15%", "text", null, true));
            }

        }
        ));
    }
}