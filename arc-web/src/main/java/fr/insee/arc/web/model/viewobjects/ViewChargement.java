package fr.insee.arc.web.model.viewobjects;

import java.util.HashMap;

import fr.insee.arc.utils.dao.PreparedStatementBuilder;
import fr.insee.arc.web.util.ConstantVObject;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;
import fr.insee.arc.web.util.VObject;

public class ViewChargement extends VObject {
    public ViewChargement() {
        super();
        
        this.setTitle("view.load");
        
        this.setPaginationSize(15);
        
        this.setSessionName("viewChargement");
        
        this.setConstantVObject(new ConstantVObject(new HashMap<String, ColumnRendering>() {
            /**
             * 
             */
            private static final long serialVersionUID = 5462424402569908140L;

            {
                put("id_norme", new ColumnRendering(false, "label.norm", "0", "text", null, true));
                put("periodicite", new ColumnRendering(false, "label.periodicity", "0", "text", null, true));
                put("validite_inf", new ColumnRendering(false, "label.validity.min", "0", "text", null, true));
                put("validite_sup", new ColumnRendering(false, "label.validity.max", "0", "text", null, true));
                put("version", new ColumnRendering(false, "label.version", "0", "text", null, true));
                put("id_regle", new ColumnRendering(true, "label.id", "5%", "text", null, true));
                put("type_fichier", new ColumnRendering(true, "label.file.type", "10%", "select",
                        new PreparedStatementBuilder("select id, id from arc.ext_type_fichier_chargement order by ordre"), true));
                put("delimiter", new ColumnRendering(true, "label.file.delimiter", "10%", "text", null, true));
                put("format", new ColumnRendering(true, "label.file.format", "65%", "text", null, true));
                put("commentaire", new ColumnRendering(true, "label.comment", "10%", "text", null, true));

            }
        }

        ));
    }
}