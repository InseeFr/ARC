package fr.insee.arc.web.model;

import java.util.HashMap;

import fr.insee.arc.web.util.ConstantVObject;
import fr.insee.arc.web.util.VObject;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;

public class ViewFiltrage extends VObject {
    public ViewFiltrage() {
        super();
        this.constantVObject = new ConstantVObject(new HashMap<String, ColumnRendering>() {
            private static final long serialVersionUID = -6829848051813971382L;
            {
                put("id_regle", new ColumnRendering(true, "Id", "5%", "text", null, false));
                put("id_norme", new ColumnRendering(false, "Norme", "0", "text", null, false));
                put("validite_inf", new ColumnRendering(false, "Debut Validite", "0", "text", null, false));
                put("validite_sup", new ColumnRendering(false, "Fin Validite", "0", "text", null, false));
                put("version", new ColumnRendering(false, "Version", "0", "text", null, false));
                put("periodicite", new ColumnRendering(false, "Periodicite", "0", "text", null, false));
                put("expr_regle_filtre", new ColumnRendering(true, "Expression règle", "75%", "text", null, true));
                put("commentaire", new ColumnRendering(true, "Commentaire", "20%", "text", null, true));
            }
        }

        );
    }
}