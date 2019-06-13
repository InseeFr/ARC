package fr.insee.arc_essnet.web.model;

import java.util.HashMap;

import fr.insee.arc_essnet.web.util.ConstantVObject;
import fr.insee.arc_essnet.web.util.ConstantVObject.ColumnRendering;
import fr.insee.arc_essnet.web.util.VObject;

public class ViewMapping extends VObject {
    public ViewMapping() {
        super();
        this.constantVObject = new ConstantVObject(

        new HashMap<String, ColumnRendering>() {
            private static final long serialVersionUID = -6829848051813971382L;
            {
                put("id_regle", new ColumnRendering(true, "Id", "5%", "text", null, false));
                put("id_norme", new ColumnRendering(false, "Norme", "0", "text", null, false));
                put("validite_inf", new ColumnRendering(false, "Debut Validite", "0", "text", null, false));
                put("validite_sup", new ColumnRendering(false, "Fin Validite", "0", "text", null, false));
                put("version", new ColumnRendering(false, "Version", "0", "text", null, false));
                put("periodicite", new ColumnRendering(false, "Periodicite", "0", "text", null, false));
                put("variable_sortie", new ColumnRendering(true, "Variable sortie", "10%", "text", null, false));
                put("expr_regle_col", new ColumnRendering(true, "Expression règle", "48%", "text", null, true));
                put("type_sortie", new ColumnRendering(true, "Type", "10%", "text", null, false));
                put("type_consolidation", new ColumnRendering(true, "Consolidation", "17%", "text", null, false));
                put("commentaire", new ColumnRendering(true, "Commentaire", "10%", "text", null, true));
            }
        });
    }
}