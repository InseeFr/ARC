package fr.insee.arc.web.gui.export.model;

import java.util.HashMap;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.web.gui.all.util.ConstantVObject;
import fr.insee.arc.web.gui.all.util.ConstantVObject.ColumnRendering;
import fr.insee.arc.web.gui.all.util.VObject;

public class ViewExportOption extends VObject {

	public ViewExportOption() {
        super();
        
        this.setTitle("view.exportOption");
        this.setSessionName("viewExportOption");
    	this.setDefaultPaginationSize(10);
        
        this.setConstantVObject(new ConstantVObject(

        new HashMap<String, ColumnRendering>() {
        	private static final long serialVersionUID = -4260429835977421646L;
			{
            put("nom_table_metier", new ColumnRendering(true, "label.tablename", "80%", "text", null, true));
            put("export_parquet_option", new ColumnRendering(true, "label.export.parquet", "10%", 
            		"select",
					new ArcPreparedStatementBuilder("select id, val from arc.ext_etat order by id desc")
            		, true));
            put("export_coordinator_option", new ColumnRendering(true, "label.export.coordinator", "10%",
            		"select",
					new ArcPreparedStatementBuilder("select id, val from arc.ext_etat order by id desc")
            		, true));
			}
    	}
        ));
        
	}
}