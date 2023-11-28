package fr.insee.arc.web.gui.all.util;

import java.util.HashMap;
import java.util.Map;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;

public final class ConstantVObject {

    private Map<String, ColumnRendering> columnRender;

    public static class ColumnRendering {
        private Boolean visible;
        private String label;
        private String size;
        private String type;
        private ArcPreparedStatementBuilder query;
        private Boolean isUpdatable;
        private Boolean isRequired;

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
        public ColumnRendering(Boolean visible, String label, String size, String type, ArcPreparedStatementBuilder query, Boolean isUpdatable) {
            this.setVisible(visible);
            this.setLabel(label);
            this.setSize(size);
            this.setType(type);
            this.setQuery(query);
            this.setIsUpdatable(isUpdatable);
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
        public ColumnRendering(Boolean visible, String label, String size, String type, ArcPreparedStatementBuilder query, Boolean isUpdatable, Boolean isRequired) {
            this(visible, label, size, type, query, isUpdatable);
            this.setIsRequired(isRequired);
        }

		public Boolean getVisible() {
			return visible;
		}

		public void setVisible(Boolean visible) {
			this.visible = visible;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public String getSize() {
			return size;
		}

		public void setSize(String size) {
			this.size = size;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public Boolean getIsUpdatable() {
			return isUpdatable;
		}

		public void setIsUpdatable(Boolean isUpdatable) {
			this.isUpdatable = isUpdatable;
		}

		public Boolean getIsRequired() {
			return isRequired;
		}

		public void setIsRequired(Boolean isRequired) {
			this.isRequired = isRequired;
		}

		public ArcPreparedStatementBuilder getQuery() {
			return query;
		}

		public void setQuery(ArcPreparedStatementBuilder query) {
			this.query = query;
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
