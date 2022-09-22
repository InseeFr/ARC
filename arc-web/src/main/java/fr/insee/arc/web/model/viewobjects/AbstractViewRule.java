package fr.insee.arc.web.model.viewobjects;

import java.util.HashMap;
import java.util.Map;

import fr.insee.arc.web.util.ConstantVObject;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;
import fr.insee.arc.web.util.VObject;

/** Abstract class for rules display (load, control,...)*/
abstract class AbstractViewRule extends VObject {
	AbstractViewRule(String title, String sessionName, int paginationSize) {
        super();
        
        this.setTitle(title);
        this.setDefaultPaginationSize(paginationSize);
        this.setSessionName(sessionName);
        
        Map<String, ColumnRendering> columnRenderings = new HashMap<>();
        columnRenderings.put("id_norme", new ColumnRendering(false, "label.norm", "0", "text", null, false));
        columnRenderings.put("periodicite", new ColumnRendering(false, "label.periodicity", "0", "text", null, false));
        columnRenderings.put("validite_inf", new ColumnRendering(false, "label.validity.min", "0", "text", null, false));
        columnRenderings.put("validite_sup", new ColumnRendering(false, "label.validity.max", "0", "text", null, false));
        columnRenderings.put("version", new ColumnRendering(false, "label.version", "0", "text", null, false));
        columnRenderings.put("id_regle", new ColumnRendering(true, "label.id", "5%", "text", null, false));
        columnRenderings.putAll(extraFields());

		this.setConstantVObject(new ConstantVObject(columnRenderings));
    }

	/** Adds any extra columns or overwrites predefined ones.*/
	protected abstract Map<String, ColumnRendering> extraFields();
}