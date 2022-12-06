package fr.insee.arc.web.model.viewobjects;

import java.util.HashMap;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.web.util.ConstantVObject;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;
import fr.insee.arc.web.util.VObject;

public class ViewNorme extends VObject {
    public ViewNorme() {
	super();

	this.setTitle("view.norms");
	
	this.setDefaultPaginationSize(15);
	
	this.setSessionName("viewNorme");

	this.setConstantVObject(new ConstantVObject(
		
		new HashMap<String, ColumnRendering>() {
		    /**
		    	 * 
		    	 */
		    private static final long serialVersionUID = 4705381559117478720L;

		    {
			put("id", new ColumnRendering(false, "label.id", "0%", "text", null, false));
			put("id_norme", new ColumnRendering(true, "label.norm", "14%", "text", null, true));
			put("periodicite", new ColumnRendering(true, "label.periodicity", "12%", "select",
					new ArcPreparedStatementBuilder("select id, val from arc.ext_mod_periodicite order by id desc"), true));
			put("def_norme",
				new ColumnRendering(true, "label.norm.calculation", "27%", "text", null, true));
			put("def_validite",
				new ColumnRendering(true, "label.validity.calculation", "27%", "text", null, true));
			put("etat", new ColumnRendering(true, "label.state", "10%", "select",
					new ArcPreparedStatementBuilder("select id, val from arc.ext_etat order by id desc"), true));
			put("id_famille", new ColumnRendering(true, "label.normFamily", "10%", "select",
					new ArcPreparedStatementBuilder("select id_famille, id_famille from arc.ihm_famille order by id_famille desc"), true));
		    }
		}));
    }
}