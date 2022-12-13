package fr.insee.arc.web.webusecases.gerernorme.model;

import java.util.HashMap;
import java.util.Map;

import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;

public class ViewFiltrage extends AbstractViewRule {
    public ViewFiltrage() {
        super("view.filter", "viewFiltrage", 15);
    }

	@Override
	protected Map<String, ColumnRendering> extraFields() {
		HashMap<String, ColumnRendering> columnRenderings = new HashMap<>();
        columnRenderings.put("expr_regle_filtre", new ColumnRendering(true, "label.sql.expression", "75%", "text", null, true));
        columnRenderings.put("commentaire", new ColumnRendering(true, "label.comment", "20%", "text", null, true));
		return columnRenderings;
	}
}