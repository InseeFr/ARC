package fr.insee.arc.web.gui.webservice.model;

import java.util.HashMap;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.web.util.ConstantVObject;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;
import fr.insee.arc.web.util.VObject;

public class ViewWebserviceQuery extends VObject {
    public ViewWebserviceQuery() {
        super();
        
        this.setTitle("view.webserviceQuery");
        this.setSessionName("viewWebserviceQuery");
		this.setDefaultPaginationSize(15);

        
        this.setConstantVObject(new ConstantVObject(new HashMap<String, ColumnRendering>() {
            /**
			 * 
			 */
            private static final long serialVersionUID = 7742353075723133064L;

			{
                put("query_id", new ColumnRendering(true, "Id", "10%", "text", null, true));
                put("query_name", new ColumnRendering(true, "Nom de la requête", "20%", "text", null, true));
                put("expression", new ColumnRendering(true, "Expression SQL", "80%", "text", null, true));
                put("query_view", new ColumnRendering(true, "Rendu", "10%", "select", new ArcPreparedStatementBuilder("select id, val from arc.ext_webservice_queryview order by id"), true));
                put("service_name", new ColumnRendering(false, "Nom du service", "0", "text", null, false));
                put("call_id", new ColumnRendering(false, "Id du service", "0", "text", null, false));
            }
        }
        ));
    }
}