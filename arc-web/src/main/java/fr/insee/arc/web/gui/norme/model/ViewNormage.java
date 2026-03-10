package fr.insee.arc.web.gui.norme.model;

import java.util.HashMap;
import java.util.Map;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.web.gui.all.util.ConstantVObject.ColumnRendering;

public class ViewNormage extends AbstractViewRule {
    public ViewNormage() {
        super("view.structurize", "viewNormage", 15);
    }

	@Override
	protected Map<String, ColumnRendering> extraFields() {
		Map<String, ColumnRendering> columnRenderings = new HashMap<>();
        columnRenderings.put("id_classe", new ColumnRendering(true, "label.structure.type", "23%", "select",
        		new ArcPreparedStatementBuilder("select id, id from arc.ext_type_normage order by ordre"), true));
        columnRenderings.put("rubrique", new ColumnRendering(true, "label.element.main", "24%", "text", null, true));
        columnRenderings.put("rubrique_nmcl", new ColumnRendering(true, "label.element.child", "24%", "text", null, true));
        columnRenderings.put("commentaire", new ColumnRendering(true, "label.comment", "24%", "text", null, true));
        columnRenderings.put("todo", new ColumnRendering(false, "label.todo", "0%", "text", null, true));
		return columnRenderings;
	}
}