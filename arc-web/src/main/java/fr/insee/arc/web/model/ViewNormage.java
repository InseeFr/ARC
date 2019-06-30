package fr.insee.arc.web.model;

import java.util.HashMap;

import fr.insee.arc.web.util.ConstantVObject;
import fr.insee.arc.web.util.VObject;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;

public class ViewNormage extends VObject {
    public ViewNormage() {
        super();
        this.constantVObject = new ConstantVObject(new HashMap<String, ColumnRendering>() {
            /**
			 * 
			 */
            private static final long serialVersionUID = -8371019897936660387L;

			{
                put("id_norme", new ColumnRendering(false, "Norme", "0", "text", null, true));
                put("periodicite", new ColumnRendering(false, "Periodicite", "0", "text", null, true));
                put("validite_inf", new ColumnRendering(false, "Debut Validite", "0", "text", null, true));
                put("validite_sup", new ColumnRendering(false, "Fin Validite", "0", "text", null, true));
                put("version", new ColumnRendering(false, "Version", "0", "text", null, true));
                put("id_regle", new ColumnRendering(true, "Id", "5%", "text", null, true));
                put("id_classe", new ColumnRendering(true, "Type de contrôle", "23%", "select",
                        "select id, id from arc.ext_type_normage order by ordre", true));
                put("rubrique", new ColumnRendering(true, "Rubrique", "24%", "text", null, true));
                put("rubrique_nmcl", new ColumnRendering(true, "Rubrique Nomenclature", "24%", "text", null, true));
                put("commentaire", new ColumnRendering(true, "Commentaire", "24%", "text", null, true));
                put("todo", new ColumnRendering(false, "todo", "0%", "text", null, true));


            }
        }

        );
    }
}