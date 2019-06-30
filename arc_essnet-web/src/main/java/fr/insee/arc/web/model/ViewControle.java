package fr.insee.arc.web.model;

import java.util.HashMap;

import fr.insee.arc.web.util.ConstantVObject;
import fr.insee.arc.web.util.VObject;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;

public class ViewControle extends VObject {
	public ViewControle() {
        super();
        this.constantVObject = new ConstantVObject(new HashMap<String, ColumnRendering>() {
            /**
			 * 
			 */
            private static final long serialVersionUID = 284660039760553986L;

			{
                put("id_norme", new ColumnRendering(false, "Norme", "0", "text", null, true));
                put("periodicite", new ColumnRendering(false, "Periodicite", "0", "text", null, true));
                put("validite_inf", new ColumnRendering(false, "Debut Validite", "0", "text", null, true));
                put("validite_sup", new ColumnRendering(false, "Fin Validite", "0", "text", null, true));
                put("version", new ColumnRendering(false, "Version", "0", "text", null, true));
                put("id_regle", new ColumnRendering(true, "Id", "5%", "text", null, true));
                put("id_classe", new ColumnRendering(true, "Type de contrôle", "13%", "select",
                        "select id, id from arc.ext_type_controle order by ordre", true));
                put("rubrique_pere", new ColumnRendering(true, "Rubrique mère", "10%", "text", null, true));
                put("rubrique_fils", new ColumnRendering(true, "Rubrique fille", "10%", "text", null, true));
                put("borne_inf", new ColumnRendering(true, "Min", "5%", "text", null, true));
                put("borne_sup", new ColumnRendering(true, "Max", "5%", "text", null, true));
                put("condition", new ColumnRendering(true, "Condition SQL", "21%", "text", null, true));
                put("pre_action", new ColumnRendering(true, "Pré-Action SQL", "21%", "text", null, true));
                put("commentaire", new ColumnRendering(true, "Commentaire", "15%", "text", null, true));
                put("todo", new ColumnRendering(false, "todo", "0%", "text", null, true));

            }
        }

        );
    }
}