package fr.insee.arc_essnet.web.model;

import java.util.HashMap;

import fr.insee.arc_essnet.web.util.ConstantVObject;
import fr.insee.arc_essnet.web.util.ConstantVObject.ColumnRendering;
import fr.insee.arc_essnet.web.util.VObject;

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