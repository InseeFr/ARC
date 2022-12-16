package fr.insee.arc.web.gui.gerernorme.model;

import java.util.HashMap;
import java.util.Map;

import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;

public class ViewExpression extends AbstractViewRule {
    public ViewExpression() {
        super("view.expression", "viewExpression", 15);
    }

	@Override
	protected Map<String, ColumnRendering> extraFields() {
		HashMap<String, ColumnRendering> columnRenderings = new HashMap<>();
        columnRenderings.put("expr_nom", new ColumnRendering(true, "label.expression.name", "25%", "text", null, true));
        columnRenderings.put("expr_valeur", new ColumnRendering(true, "label.expression.value", "60%", "text", null, true));
        columnRenderings.put("commentaire", new ColumnRendering(true, "label.comment", "10%", "text", null, true));
		return columnRenderings;
	}
}