package fr.insee.arc.web.model.viewobjects;

import java.util.HashMap;
import java.util.Map;

import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;

public class ViewMapping extends AbstractViewRule {
    public ViewMapping() {
        super("view.mapping", "viewMapping", 15);
    }

	@Override
	protected Map<String, ColumnRendering> extraFields() {
		HashMap<String, ColumnRendering> columnRenderings = new HashMap<>();
        columnRenderings.put("variable_sortie", new ColumnRendering(true, "label.mapmodel.field", "10%", "text", null, false));
        columnRenderings.put("expr_regle_col", new ColumnRendering(true, "label.sql.expression", "48%", "text", null, true));
        columnRenderings.put("type_sortie", new ColumnRendering(true, "label.mapmodel.field.type", "10%", "text", null, false));
        columnRenderings.put("type_consolidation", new ColumnRendering(false, "label.mapmodel.field.aggregate", "17%", "text", null, false));
        columnRenderings.put("commentaire", new ColumnRendering(true, "label.comment", "10%", "text", null, true));
		return columnRenderings;
	}
}