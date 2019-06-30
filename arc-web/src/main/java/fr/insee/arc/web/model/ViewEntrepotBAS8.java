package fr.insee.arc.web.model;

import java.util.HashMap;

import fr.insee.arc.web.util.ConstantVObject;
import fr.insee.arc.web.util.VObject;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;

public class ViewEntrepotBAS8 extends VObject {
    public ViewEntrepotBAS8() {
        super();
        this.constantVObject = new ConstantVObject(new HashMap<String, ColumnRendering>() {

            /**
             *
             */
            private static final long serialVersionUID = -7135841266887101563L;

            {

                put("id_entrepot", new ColumnRendering(true, "Entrepot", "60px", "text", null, true));
            }
        }

        );
    }
}