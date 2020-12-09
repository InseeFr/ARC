package fr.insee.arc.web.model.viewobjects;

import java.util.HashMap;

import fr.insee.arc.web.util.ConstantVObject;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;
import fr.insee.arc.web.util.VObject;

public class ViewExpression extends VObject {
    public ViewExpression() {
        super();
        
		this.setTitle("view.expression");
		this.setPaginationSize(15);
		this.setSessionName("viewExpression");
        
        this.setConstantVObject(new ConstantVObject(

        new HashMap<String, ColumnRendering>() {
            private static final long serialVersionUID = -6829848051813971382L;
            {
                put("id_regle", new ColumnRendering(true, "label.id", "5%", "text", null, false));
                put("id_norme", new ColumnRendering(false, "label.norm", "0", "text", null, true));
                put("validite_inf", new ColumnRendering(false, "label.validity.min", "0", "text", null, false));
                put("validite_sup", new ColumnRendering(false, "label.validity.max", "0", "text", null, false));
                put("version", new ColumnRendering(false, "label.version", "0", "text", null, false));
                put("periodicite", new ColumnRendering(false, "label.periodicity", "0", "text", null, false));
                put("expr_nom", new ColumnRendering(true, "label.expression.name", "25%", "text", null, true));
                put("expr_valeur", new ColumnRendering(true, "label.expression.value", "60%", "text", null, true));
                put("commentaire", new ColumnRendering(true, "label.comment", "10%", "text", null, true));
            }
        }));
    }
}