package fr.insee.arc.web.gui.export.model;

import java.util.HashMap;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.web.gui.all.util.ConstantVObject;
import fr.insee.arc.web.gui.all.util.VObject;
import fr.insee.arc.web.gui.all.util.ConstantVObject.ColumnRendering;


public class ViewExport extends VObject {
    public ViewExport() {
        super();
        
        this.setTitle("view.export");
        this.setSessionName("viewExport");
    	this.setDefaultPaginationSize(0);
        
        this.setConstantVObject(new ConstantVObject(

        new HashMap<String, ColumnRendering>() {
            /**
             * 
             */
            private static final long serialVersionUID = -3124381932840827423L;

            {
                put(ColumnEnum.TIMESTAMP_DIRECTORY.getColumnName(), new ColumnRendering(true, "label.export.timestamp", "8%", "text", null, true));
            	put(ColumnEnum.FILE_NAME.getColumnName(), new ColumnRendering(true, "label.export.file.name", "15%", "text", null, true));
                put(ColumnEnum.ZIP.getColumnName(), new ColumnRendering(true, "label.export.file.format", "6%", "select", new ArcPreparedStatementBuilder("select id, val from arc.ext_export_format order by id"), true));
                put(ColumnEnum.HEADERS.getColumnName(), new ColumnRendering(true, "label.export.table.headers", "8%", "text", null, true));
                put(ColumnEnum.NULLS.getColumnName(), new ColumnRendering(true, "label.export.table.nulls", "8%", "text", null, true));
                put(ColumnEnum.TABLE_TO_EXPORT.getColumnName(), new ColumnRendering(true, "label.export.table.name", "15%", "text", null, true));
                put(ColumnEnum.NOMENCLATURE_EXPORT.getColumnName(), new ColumnRendering(true, "label.export.file.schema", "15%", "text", null, true));
                put(ColumnEnum.FILTER_TABLE.getColumnName(), new ColumnRendering(true, "label.export.table.filter", "10%", "text", null, true));
                put(ColumnEnum.ORDER_TABLE.getColumnName(), new ColumnRendering(true, "label.export.table.order", "10%", "text", null, true));
                put(ColumnEnum.JSON_KEY_VALUE.getColumnName(), new ColumnRendering(true, "label.export.table.json", "15%", "text", null, true));
                put(ColumnEnum.ETAT.getColumnName(), new ColumnRendering(true, "label.export.etat", "15%", "text", null, true));
             }
        }));
    }
}