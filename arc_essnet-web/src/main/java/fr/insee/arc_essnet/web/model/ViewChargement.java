package fr.insee.arc_essnet.web.model;

import java.util.HashMap;

import fr.insee.arc_essnet.web.util.ConstantVObject;
import fr.insee.arc_essnet.web.util.ConstantVObject.ColumnRendering;
import fr.insee.arc_essnet.web.util.VObject;

public class ViewChargement extends VObject {
    public ViewChargement() {
        super();
        this.constantVObject = new ConstantVObject(new HashMap<String, ColumnRendering>() {
            /**
             * 
             */
            private static final long serialVersionUID = 5462424402569908140L;

            {
                put("id_norme", new ColumnRendering(false, "Norme", "0", "text", null, true));
                put("periodicite", new ColumnRendering(false, "Periodicite", "0", "text", null, true));
                put("validite_inf", new ColumnRendering(false, "Debut Validite", "0", "text", null, true));
                put("validite_sup", new ColumnRendering(false, "Fin Validite", "0", "text", null, true));
                put("version", new ColumnRendering(false, "Version", "0", "text", null, true));
                put("id_regle", new ColumnRendering(true, "Id", "5%", "text", null, true));
                put("type_fichier", new ColumnRendering(true, "Type de fichier", "10%", "select",
                        "select id, id from arc.ext_type_fichier_chargement order by ordre", true));
                put("delimiter", new ColumnRendering(true, "Delimiter", "10%", "text", null, true));
                put("format", new ColumnRendering(true, "Format", "65%", "text", null, true));
                put("commentaire", new ColumnRendering(true, "Commentaire", "10%", "text", null, true));

            }
        }

        );
    }
}