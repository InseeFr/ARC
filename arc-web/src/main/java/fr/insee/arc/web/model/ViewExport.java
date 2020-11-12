package fr.insee.arc.web.model;

import java.util.HashMap;

import fr.insee.arc.web.util.ConstantVObject;
import fr.insee.arc.web.util.VObject;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;


public class ViewExport extends VObject {
    public ViewExport() {
        super();
        
        this.setTitle("view.export");
    	this.setPaginationSize(0);
        
        this.constantVObject = new ConstantVObject(

        new HashMap<String, ColumnRendering>() {
            /**
             * 
             */
            private static final long serialVersionUID = -3124381932840827423L;

            {
                put("file_name", new ColumnRendering(true, "Nom du fichier à créer", "150px", "text", null, true));
                put("zip", new ColumnRendering(true, "Zip ?", "50px", "text", null, true));
                put("headers", new ColumnRendering(true, "Créer la ligne d'entete ?", "150px", "text", null, true));
                put("nulls", new ColumnRendering(true, "Mettre null pour les valeurs à null ?", "150px", "text", null, true));
                put("table_to_export", new ColumnRendering(true, "Table à exporter", "150px", "text", null, true));
                put("nomenclature_export", new ColumnRendering(true, "Nomenclature de définition de l'export", "150px", "text", null, true));
                put("filter_table", new ColumnRendering(true, "Filtre", "150px", "text", null, true));
                put("order_table", new ColumnRendering(true, "Tri", "150px", "text", null, true));
                put("columns_array_header", new ColumnRendering(true, "Colonne(s) tableau contenant des entetes", "150px", "text", null, true));
                put("columns_array_value", new ColumnRendering(true, "Colonne(s) tableau contenant des valeurs", "150px", "text", null, true));
                put("etat", new ColumnRendering(true, "Etat du dernier export", "150px", "text", null, false));

                
             }
        });
    }
}