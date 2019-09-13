package fr.insee.arc.web.model;

import java.util.HashMap;

import fr.insee.arc.web.util.ConstantVObject;
import fr.insee.arc.web.util.VObject;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;

public class ViewControle extends VObject {
	public ViewControle() {
        super();
        
        
        this.setTitle("view.control");
        this.setPaginationSize(15);
        
        this.constantVObject = new ConstantVObject(new HashMap<String, ColumnRendering>() {
            /**
			 * 
			 */
            private static final long serialVersionUID = 284660039760553986L;

			{
                put("id_norme", new ColumnRendering(false, "label.norm", "0", "text", null, true));
                put("periodicite", new ColumnRendering(false, "label.periodicity", "0", "text", null, true));
                put("validite_inf", new ColumnRendering(false, "label.validity.min", "0", "text", null, true));
                put("validite_sup", new ColumnRendering(false, "label.validity.max", "0", "text", null, true));
                put("version", new ColumnRendering(false, "label.version", "0", "text", null, true));
                put("id_regle", new ColumnRendering(true, "label.id", "5%", "text", null, true));
                put("id_classe", new ColumnRendering(true, "label.control.type", "13%", "select",
                        "select id, id from arc.ext_type_controle order by ordre", true));
                put("rubrique_pere", new ColumnRendering(true, "label.element.main", "10%", "text", null, true));
                put("rubrique_fils", new ColumnRendering(true, "label.element.child", "10%", "text", null, true));
                put("borne_inf", new ColumnRendering(true, "label.min", "5%", "text", null, true));
                put("borne_sup", new ColumnRendering(true, "label.max", "5%", "text", null, true));
                put("condition", new ColumnRendering(true, "label.sql.predicate", "21%", "text", null, true));
                put("pre_action", new ColumnRendering(true, "label.sql.pretreatment", "21%", "text", null, true));
                put("commentaire", new ColumnRendering(true, "label.comment", "15%", "text", null, true));
                put("todo", new ColumnRendering(false, "label.todo", "0%", "text", null, true));

            }
        }

        );
    }
}