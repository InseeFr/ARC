package fr.insee.arc.web.model.viewobjects;

import java.util.HashMap;

import fr.insee.arc.web.util.ConstantVObject;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;
import fr.insee.arc.web.util.VObject;

public class ViewFiltrage extends VObject {
    public ViewFiltrage() {
        super();
        
        this.setTitle("view.filter");
        this.setPaginationSize(15);
        
        this.setSessionName("viewFiltrage");
        
        this.setConstantVObject(new ConstantVObject(new HashMap<String, ColumnRendering>() {
            private static final long serialVersionUID = -6829848051813971382L;
            {
                put("id_regle", new ColumnRendering(true, "label.id", "5%", "text", null, false));
                put("id_norme", new ColumnRendering(false, "label.norm", "0", "text", null, false));
                put("validite_inf", new ColumnRendering(false, "label.validity.min", "0", "text", null, false));
                put("validite_sup", new ColumnRendering(false, "label.validity.max", "0", "text", null, false));
                put("version", new ColumnRendering(false, "label.version", "0", "text", null, false));
                put("periodicite", new ColumnRendering(false, "label.periodicity", "0", "text", null, false));
                put("expr_regle_filtre", new ColumnRendering(true, "label.sql.expression", "75%", "text", null, true));
                put("commentaire", new ColumnRendering(true, "label.comment", "20%", "text", null, true));
            }
        }

        ));
    }
}