package fr.insee.arc.web.model;

import java.util.HashMap;
import java.util.List;

import fr.insee.arc.web.util.ConstantVObject;
import fr.insee.arc.web.util.VObject;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;

public class ViewVariableMetier  extends VObject {

    public ViewVariableMetier() {
        super();
        

        this.setTitle("view.mapmodel.fields");
        this.setPaginationSize(15);
        
        this.constantVObject = new ConstantVObject(new HashMap<String, ColumnRendering>() {
            private static final long serialVersionUID = -6829848051813971382L;
            {
                /**
                 * Deux moyens d'initialiser cette view
                 */
            }
        });
    }
    
    /**
     * default columns
     * @param returned
     * @return
     */
    public static HashMap<String, ColumnRendering> getInitialRenderingViewVariableMetier(HashMap<String, ColumnRendering> returned) {
        returned.put("id_famille", new ColumnRendering(false, "Id.", "20px", "text", null, true));
        returned.put("nom_variable_metier", new ColumnRendering(true, "label.mapmodel.field", "200px", "text", null, true));
        returned.put("description_variable_metier", new ColumnRendering(true, "label.comment", "200px", "text", null, true));
        returned.put("type_variable_metier", new ColumnRendering(true, "label.mapmodel.field.type", "100px", "select",
                "SELECT nom_type id, nom_type val FROM arc.ext_mod_type_autorise ORDER BY nom_type", true));
        returned.put("type_consolidation", new ColumnRendering(false, "label.mapmodel.field.aggregate", "200px", "text", null, true));
        return returned;
    }
    
    /**
     * business columns
     */
    public static final HashMap<String, ColumnRendering> getInitialRendering(List<String> aVariableListe) {
        HashMap<String, ColumnRendering> returned = new HashMap<String, ColumnRendering>();
        String size = "100px";
        String type = "text";
        String query = null;
        for (int i = 0; i < aVariableListe.size(); i++) {
            System.out.println(aVariableListe.get(i).replaceAll("^mapping_[^_]*_", "").replaceAll("_ok$", "").toLowerCase());
            returned.put(aVariableListe.get(i),
                    new ColumnRendering(true, aVariableListe.get(i).replaceAll("^mapping_[^_]*_", "").replaceAll("_ok$", "").toLowerCase(), size,
                            type, query, true));
        }
        return returned;
    }

}