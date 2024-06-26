package fr.insee.arc.web.gui.pilotage.model;

import java.util.HashMap;

import fr.insee.arc.web.gui.all.util.ConstantVObject;
import fr.insee.arc.web.gui.all.util.VObject;
import fr.insee.arc.web.gui.all.util.ConstantVObject.ColumnRendering;

public class ViewEntrepotBAS extends VObject {
    public ViewEntrepotBAS() {
        super();
        this.setSessionName("viewEntrepotBAS");
        this.setTitle("view.envManagement.fileStore");
        this.setDefaultPaginationSize(15);
        
        this.setConstantVObject(new ConstantVObject(new HashMap<String, ColumnRendering>() {

            /**
             *
             */
            private static final long serialVersionUID = -7135841266887101563L;

            {

                put("id_entrepot", new ColumnRendering(true, "Entrepot", "100%q", "text", null, true));
            }
        }

        ));
    }
}