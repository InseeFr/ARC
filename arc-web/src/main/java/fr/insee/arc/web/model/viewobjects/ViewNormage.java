package fr.insee.arc.web.model.viewobjects;

import java.util.HashMap;
import java.util.Map;

import fr.insee.arc.utils.dao.PreparedStatementBuilder;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;

public class ViewNormage extends AbstractViewRule {
    public ViewNormage() {
        super("view.structurize", "viewNormage", 15);
    }

	@Override
	protected Map<String, ColumnRendering> extraFields() {
		HashMap<String, ColumnRendering> columnRenderings = new HashMap<>();
        columnRenderings.put("id_classe", new ColumnRendering(true, "label.structure.type", "23%", "select",
        		new PreparedStatementBuilder("select id, id from arc.ext_type_normage order by ordre"), true));
        columnRenderings.put("rubrique", new ColumnRendering(true, "label.element.main", "24%", "text", null, true));
        columnRenderings.put("rubrique_nmcl", new ColumnRendering(true, "label.element.child", "24%", "text", null, true));
        columnRenderings.put("commentaire", new ColumnRendering(true, "label.comment", "24%", "text", null, true));
        columnRenderings.put("todo", new ColumnRendering(false, "label.todo", "0%", "text", null, true));
		return columnRenderings;
	}
}