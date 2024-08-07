package fr.insee.arc.web.gui.norme.model;

import java.util.HashMap;
import java.util.Map;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.web.gui.all.util.ConstantVObject.ColumnRendering;

public class ViewControle extends AbstractViewRule {
	public ViewControle() {
        super("view.control", "viewControle", 15);
    }
	
	@Override
	protected Map<String, ColumnRendering> extraFields() {
		Map<String, ColumnRendering> columnRenderings = new HashMap<>();
        columnRenderings.put("id_classe", new ColumnRendering(true, "label.control.type", "13%", "select",
        		new ArcPreparedStatementBuilder("select id, id from arc.ext_type_controle order by ordre"), true));
        columnRenderings.put("rubrique_pere", new ColumnRendering(true, "label.element.main", "11%", "text", null, true));
        columnRenderings.put("rubrique_fils", new ColumnRendering(true, "label.element.child", "11%", "text", null, true));
        columnRenderings.put("borne_inf", new ColumnRendering(true, "label.min", "4%", "text", null, true));
        columnRenderings.put("borne_sup", new ColumnRendering(true, "label.max", "4%", "text", null, true));
        columnRenderings.put("condition", new ColumnRendering(true, "label.sql.predicate", "32%", "text", null, true));
        columnRenderings.put("pre_action", new ColumnRendering(true, "label.sql.pretreatment", "21%", "text", null, true));
        columnRenderings.put("commentaire", new ColumnRendering(true, "label.comment", "21%", "text", null, true));
        columnRenderings.put("todo", new ColumnRendering(false, "label.todo", "0%", "text", null, true));
        columnRenderings.put("xsd_ordre", new ColumnRendering(false, "label.xsd.order", "5%", "text", null, true));
        columnRenderings.put("xsd_label_fils", new ColumnRendering(false, "label.xsd.child", "15%", "text", null, true));
        columnRenderings.put("xsd_role", new ColumnRendering(false, "label.xsd.role", "6%", "text", null, true));
        columnRenderings.put("blocking_threshold", new ColumnRendering(true, "label.threshold.blocking", "6%", "text", null, true));
        columnRenderings.put("error_row_processing", new ColumnRendering(true, "label.errorRowProcessing", "11%", "select",
        		new ArcPreparedStatementBuilder("select id, label FROM arc.ext_error_row_processing order by ordre"), true));

		return columnRenderings;
	}
}

