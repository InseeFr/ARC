package fr.insee.arc.web.util;

import java.util.HashMap;
import java.util.Map;

import fr.insee.arc.utils.dao.PreparedStatementBuilder;

public final class ConstantVObject {

    public Map<String, ColumnRendering> columnRender;

    public static class ColumnRendering {
        public Boolean visible;
        public String label;
        public String size;
        public String type;
        public PreparedStatementBuilder query;
        public Boolean isUpdatable;
        public Boolean isRequired;

        /**
         *
         * @param visible
         *            est-ce que ma colonne est visible ?
         * @param label
         *            quel nom est affiché pour ma colonne ?
         * @param size
         *            quelle taille en pixels ?
         * @param type
         *            quel type d'affichage ? (text = texte, select = liste déroulante)
         * @param query
         *            si type = text, quelle requête permet d'obtenir la liste ?
         * @param isUpdatable
         *            est-ce que je peux modifier la colonne ?
         */
        public ColumnRendering(Boolean visible, String label, String size, String type, PreparedStatementBuilder query, Boolean isUpdatable) {
            this.visible = visible;
            this.label = label;
            this.size = size;
            this.type = type;
            this.query = query;
            this.isUpdatable = isUpdatable;
        }
        
        /**
        *
        * @param visible
        *            est-ce que ma colonne est visible ?
        * @param label
        *            quel nom est affiché pour ma colonne ?
        * @param size
        *            quelle taille en pixels ?
        * @param type
        *            quel type d'affichage ? (text = texte, select = liste déroulante)
        * @param query
        *            si type = text, quelle requête permet d'obtenir la liste ?
        * @param isUpdatable
        *            est-ce que je peux modifier la colonne ?
        * @param isRequired
        *           est-ce un champ qui doit absolument être saisi?
        */
        public ColumnRendering(Boolean visible, String label, String size, String type, PreparedStatementBuilder query, Boolean isUpdatable, Boolean isRequired) {
            this(visible, label, size, type, query, isUpdatable);
            this.isRequired = isRequired;
        }
        
        
    }

    public ConstantVObject() {
        this.columnRender =  new HashMap<>();
    }

    public ConstantVObject(Map<String, ColumnRendering> someColumnRendering) {
        this.columnRender = someColumnRendering;
    }


    /**
     * @return the columnrender
     */
    public Map<String, ColumnRendering> getColumnRender() {
        return this.columnRender;
    }

    public void setColumnRender(Map<String, ColumnRendering> aRendering) {
        this.columnRender = aRendering;
    }

    /**
     * Attention -> false=updatable / true=non updatable
     */
    public void setIsUpdatable(String aCol, boolean isUpdatable) {
        getColumnRender().get(aCol).isUpdatable = isUpdatable;
    }

}
