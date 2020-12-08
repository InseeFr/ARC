package fr.insee.arc.web.model.viewobjects;

import java.util.HashMap;

import fr.insee.arc.utils.dao.PreparedStatementBuilder;
import fr.insee.arc.web.util.ConstantVObject;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;
import fr.insee.arc.web.util.VObject;

public class ViewNormage extends VObject {
    public ViewNormage() {
        super();
        
        this.setTitle("view.structurize");
        
        this.setPaginationSize(15);
        
        this.setSessionName("viewNormage");
        
        this.setConstantVObject(new ConstantVObject(new HashMap<String, ColumnRendering>() {
            /**
			 * 
			 */
            private static final long serialVersionUID = -8371019897936660387L;

			{
                put("id_norme", new ColumnRendering(false, "label.norm", "0", "text", null, true));
                put("periodicite", new ColumnRendering(false, "label.periodicity", "0", "text", null, true));
                put("validite_inf", new ColumnRendering(false, "label.validity.min", "0", "text", null, true));
                put("validite_sup", new ColumnRendering(false, "label.validity.max", "0", "text", null, true));
                put("version", new ColumnRendering(false, "label.version", "0", "text", null, true));
                put("id_regle", new ColumnRendering(true, "label.id", "5%", "text", null, true));
                put("id_classe", new ColumnRendering(true, "label.structure.type", "23%", "select",
                        new PreparedStatementBuilder("select id, id from arc.ext_type_normage order by ordre"), true));
                put("rubrique", new ColumnRendering(true, "label.element.main", "24%", "text", null, true));
                put("rubrique_nmcl", new ColumnRendering(true, "label.element.child", "24%", "text", null, true));
                put("commentaire", new ColumnRendering(true, "label.comment", "24%", "text", null, true));
                put("todo", new ColumnRendering(false, "label.todo", "0%", "text", null, true));


            }
        }

        ));
    }
}