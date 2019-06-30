package fr.insee.arc.web.model;

import java.util.HashMap;

import fr.insee.arc.web.util.ConstantVObject;
import fr.insee.arc.web.util.VObject;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;

public class ViewNorme extends VObject {
    public ViewNorme() {
	super();
	this.constantVObject = new ConstantVObject(

		new HashMap<String, ColumnRendering>() {
		    /**
		    	 * 
		    	 */
		    private static final long serialVersionUID = 4705381559117478720L;

		    {
			put("id", new ColumnRendering(false, "id", "0%", "text", null, false));
			put("id_norme", new ColumnRendering(true, "Norme", "12%", "text", null, true));
			put("periodicite", new ColumnRendering(true, "Periodicite", "12%", "select",
				"select id, val from arc.ext_mod_periodicite order by id desc", true));
			put("def_norme",
				new ColumnRendering(true, "Définition de la Norme", "27%", "text", null, true));
			put("def_validite",
				new ColumnRendering(true, "Calcul de la Validite", "27%", "text", null, true));
			put("etat", new ColumnRendering(true, "Etat", "12%", "select",
				"select id, val from arc.ext_etat order by id desc", true));
			put("id_famille", new ColumnRendering(true, "Famille", "10%", "select",
				"select id_famille, id_famille from arc.ihm_famille order by id_famille desc", true));
		    }
		});
    }
}