package fr.insee.arc.web.model.viewobjects;

import java.util.HashMap;
import java.util.List;

import fr.insee.arc.utils.dao.PreparedStatementBuilder;
import fr.insee.arc.web.util.ConstantVObject;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;
import fr.insee.arc.web.util.VObject;

public class ViewVariableMetier  extends VObject {

    public ViewVariableMetier() {
        super();
        

        this.setTitle("view.mapmodel.fields");
        this.setDefaultPaginationSize(15);
        this.setSessionName("viewVariableMetier");
        
        this.setConstantVObject(new ConstantVObject(new HashMap<String, ColumnRendering>() {
            private static final long serialVersionUID = -6829848051813971382L;
            {
                /**
                 * Deux moyens d'initialiser cette view
                 */
            }
        }));
    }
    
    /**
     * default columns
     * @param returned
     * @return
     */
    public static HashMap<String, ColumnRendering> getInitialRenderingViewVariableMetier(HashMap<String, ColumnRendering> returned) {
        returned.put("id_famille", new ColumnRendering(false, "Id.", "", "text", null, false));
        returned.put("nom_variable_metier", new ColumnRendering(true, "label.mapmodel.field", "33%", "text", null, true));
        returned.put("description_variable_metier", new ColumnRendering(true, "label.comment", "33%", "text", null, true));
        returned.put("type_variable_metier", new ColumnRendering(true, "label.mapmodel.field.type", "33%", "select",
        		new PreparedStatementBuilder("SELECT nom_type id, nom_type val FROM arc.ext_mod_type_autorise ORDER BY nom_type"), true, true));
        returned.put("type_consolidation", new ColumnRendering(false, "label.mapmodel.field.aggregate", "", "text", null, true));
        return returned;
    }
    
    /**
     * business columns
     */
    public static final HashMap<String, ColumnRendering> getInitialRendering(List<String> aVariableListe) {
        HashMap<String, ColumnRendering> returned = new HashMap<>();
        String size = "10%";
        String type = "text";
        for (int i = 0; i < aVariableListe.size(); i++) {
            returned.put(aVariableListe.get(i),
                    new ColumnRendering(true, aVariableListe.get(i).replaceAll("^mapping_[^_]*_", "").replaceAll("_ok$", "").toLowerCase(), size,
                            type, null, true));
        }
        return returned;
    }

}