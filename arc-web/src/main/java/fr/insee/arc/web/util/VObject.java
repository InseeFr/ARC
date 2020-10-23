package fr.insee.arc.web.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class VObject {

	/** Titre de la fenêtre */
	private String title;

	/** Nom dans la session */
	private String sessionName;

	/** Nombre de lignes par page */
	private int paginationSize;

	/** Requête de génération du tableau */
	private String mainQuery;

	public String beforeSelectQuery;

	public String afterUpdateQuery;

	public String afterInsertQuery;

	/** Table utilisée pour les update/insert/delete */
	public String table;

	/** Tableau du contenu de la requete (ligne, colonne) */
	private TableObject content;

	/** Rendering for this */
	private ConstantVObject constantVObject;

	/** Indicateur d'initialisation */
	private boolean isInitialized;
	private boolean isScoped;
	private boolean isActive;

	// nom des colonnes, type en base (D=Database), label dans la vue (V=VUE) ,
	// taille sur la vue
	/** Noms des colonnes en base.*/
	private ArrayList<String> headersDLabel;
	/** Types des colonnes en base.*/
	private ArrayList<String> headersDType;
	/** Noms des colonnes dans la vue.*/
	private ArrayList<String> headersVLabel;
	/** Tailles des colonnes dans la vue.*/
	private ArrayList<String> headersVSize;
	/** Types des colonnes dans la vue.*/
	private ArrayList<String> headersVType;
	private ArrayList<LinkedHashMap<String, String>> headersVSelect;
	private ArrayList<Boolean> headersVisible;
	private ArrayList<Boolean> headersUpdatable;
	private ArrayList<Boolean> headersRequired;
	/** Tableau des lignes selectionnées */
	private ArrayList<Boolean> selectedLines;
	/** Tableau des colonnes selectionnées */
	private ArrayList<Boolean> selectedColumns;
	// champs de saisie
	private HashMap<String, String> defaultInputFields;
	private ArrayList<String> inputFields;    
	// Pagination
	private Integer nbPages;
	private String idPage;

	// Gestion du tri
	/** Liste des colonnes utilisées pour trier la table */
	private ArrayList<String> headerSortDLabels;
	/** Liste des directions de tri (des colonnes nommées dans headerSortDLabels).
	 *  true = ascending, false = descending. */
	private ArrayList<Boolean> headerSortDOrders;
	/** La colonne cliquée par l'utilisateur pour le tri.*/
	private String headerSortDLabel;

	private ArrayList<String> filterFields;

	private String message;

	private HashMap<String, String> customValues = new HashMap<>();
	private HashMap<String, HashMap<String, String>> mapCustomValues = new HashMap<>();

	// upload
	private ArrayList<File> fileUpload;
	private ArrayList<String> fileUploadFileName;

	
	public boolean getIsInitialized() {
		return isInitialized;
	}

	public void setIsInitialized(boolean isInitialized) {
		this.isInitialized = isInitialized;
	}


	public int getPaginationSize() {
		return paginationSize;
	}

	public void setPaginationSize(int paginationSize) {
		this.paginationSize = paginationSize;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSessionName() {
		return sessionName;
	}

	public void setSessionName(String sessionName) {
		this.sessionName = sessionName;
	}

	public String getMainQuery() {
		return mainQuery;
	}

	public void setMainQuery(String mainQuery) {
		this.mainQuery = mainQuery;
	}

	public String getBeforeSelectQuery() {
		return beforeSelectQuery;
	}

	public void setBeforeSelectQuery(String beforeSelectQuery) {
		this.beforeSelectQuery = beforeSelectQuery;
	}

	public String getAfterUpdateQuery() {
		return afterUpdateQuery;
	}

	public void setAfterUpdateQuery(String afterUpdateQuery) {
		this.afterUpdateQuery = afterUpdateQuery;
	}

	public String getAfterInsertQuery() {
		return afterInsertQuery;
	}

	public void setAfterInsertQuery(String afterInsertQuery) {
		this.afterInsertQuery = afterInsertQuery;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public TableObject getContent() {
		return content;
	}

	public void setContent(TableObject content) {
		this.content = content;
	}

	public ConstantVObject getConstantVObject() {
		return constantVObject;
	}

	public void setConstantVObject(ConstantVObject constantVObject) {
		this.constantVObject = constantVObject;
	}

	public ArrayList<String> getHeadersDLabel() {
		return headersDLabel;
	}

	public void setHeadersDLabel(ArrayList<String> headersDLabel) {
		this.headersDLabel = headersDLabel;
	}

	public ArrayList<String> getHeadersDType() {
		return headersDType;
	}

	public void setHeadersDType(ArrayList<String> headersDType) {
		this.headersDType = headersDType;
	}

	public ArrayList<String> getHeadersVLabel() {
		return headersVLabel;
	}

	public void setHeadersVLabel(ArrayList<String> headersVLabel) {
		this.headersVLabel = headersVLabel;
	}

	public ArrayList<String> getHeadersVSize() {
		return headersVSize;
	}

	public void setHeadersVSize(ArrayList<String> headersVSize) {
		this.headersVSize = headersVSize;
	}

	public ArrayList<String> getHeadersVType() {
		return headersVType;
	}

	public void setHeadersVType(ArrayList<String> headersVType) {
		this.headersVType = headersVType;
	}

	public ArrayList<LinkedHashMap<String, String>> getHeadersVSelect() {
		return headersVSelect;
	}

	public void setHeadersVSelect(ArrayList<LinkedHashMap<String, String>> headersVSelect) {
		this.headersVSelect = headersVSelect;
	}

	public ArrayList<Boolean> getHeadersVisible() {
		return headersVisible;
	}

	public void setHeadersVisible(ArrayList<Boolean> headersVisible) {
		this.headersVisible = headersVisible;
	}

	public ArrayList<Boolean> getHeadersUpdatable() {
		return headersUpdatable;
	}

	public void setHeadersUpdatable(ArrayList<Boolean> headersUpdatable) {
		this.headersUpdatable = headersUpdatable;
	}

	public ArrayList<Boolean> getHeadersRequired() {
		return headersRequired;
	}

	public void setHeadersRequired(ArrayList<Boolean> headersRequired) {
		this.headersRequired = headersRequired;
	}

	public ArrayList<Boolean> getSelectedLines() {
		return selectedLines;
	}

	public void setSelectedLines(ArrayList<Boolean> selectedLines) {
		this.selectedLines = selectedLines;
	}

	public ArrayList<Boolean> getSelectedColumns() {
		return selectedColumns;
	}

	public void setSelectedColumns(ArrayList<Boolean> selectedColumns) {
		this.selectedColumns = selectedColumns;
	}

	public HashMap<String, String> getDefaultInputFields() {
		return defaultInputFields;
	}

	public void setDefaultInputFields(HashMap<String, String> defaultInputFields) {
		this.defaultInputFields = defaultInputFields;
	}

	public ArrayList<String> getInputFields() {
		return inputFields;
	}

	public void setInputFields(ArrayList<String> inputFields) {
		this.inputFields = inputFields;
	}

	public Integer getNbPages() {
		return nbPages;
	}

	public void setNbPages(Integer nbPages) {
		this.nbPages = nbPages;
	}

	public String getIdPage() {
		return idPage;
	}

	public void setIdPage(String idPage) {
		this.idPage = idPage;
	}

	public ArrayList<String> getFilterFields() {
		return filterFields;
	}

	public void setFilterFields(ArrayList<String> filterFields) {
		this.filterFields = filterFields;
	}

	public ArrayList<String> getHeaderSortDLabels() {
		return headerSortDLabels;
	}

	public void setHeaderSortDLabels(ArrayList<String> headerSortDLabels) {
		this.headerSortDLabels = headerSortDLabels;
	}

	public ArrayList<Boolean> getHeaderSortDOrders() {
		return headerSortDOrders;
	}

	public void setHeaderSortDOrders(ArrayList<Boolean> headerSortDOrders) {
		this.headerSortDOrders = headerSortDOrders;
	}

	public String getHeaderSortDLabel() {
		return headerSortDLabel;
	}

	public void setHeaderSortDLabel(String headerSortDLabel) {
		this.headerSortDLabel = headerSortDLabel;
	}

	@SuppressWarnings("unchecked")
	public VObject copy() {
		VObject v0 = new VObject();
		v0.setTitle(this.getTitle());
		v0.setSessionName(this.getSessionName());
		v0.setPaginationSize(this.getPaginationSize());
		v0.setIsInitialized(this.getIsInitialized());
		v0.setMainQuery(this.getMainQuery());
		v0.setBeforeSelectQuery(this.getBeforeSelectQuery());
		v0.setAfterUpdateQuery(this.getAfterUpdateQuery());
		v0.setAfterInsertQuery(this.getAfterInsertQuery());
		v0.setTable(this.getTable());
		v0.setMessage(this.getMessage());
		if (this.getContent() != null) {
			v0.setContent(this.getContent().clone());
		} else {
			v0.setContent(null);
		}
		if (this.getHeadersDLabel() != null) {
			v0.setHeadersDLabel((ArrayList<String>) this.getHeadersDLabel().clone());
		} else {
			v0.setHeadersDLabel(null);
		}
		if (this.getHeadersDType() != null) {
			v0.setHeadersDType((ArrayList<String>) this.getHeadersDType().clone());
		} else {
			v0.setHeadersDType(null);
		}
		if (this.getHeadersVLabel() != null) {
			v0.setHeadersVLabel((ArrayList<String>) this.getHeadersVLabel().clone());
		} else {
			v0.setHeadersVLabel(null);
		}
		if (this.getHeadersVSize() != null) {
			v0.setHeadersVSize((ArrayList<String>) this.getHeadersVSize().clone());
		} else {
			v0.setHeadersVSize(null);
		}
		if (this.getHeadersVType() != null) {
			v0.setHeadersVType((ArrayList<String>) this.getHeadersVType().clone());
		} else {
			v0.setHeadersVType(null);
		}
		if (this.getHeadersVSelect() != null) {
			v0.setHeadersVSelect((ArrayList<LinkedHashMap<String, String>>) this.getHeadersVSelect().clone());
		} else {
			v0.setHeadersVSelect(null);
		}
		if (this.getHeadersVisible() != null) {
			v0.setHeadersVisible((ArrayList<Boolean>) this.getHeadersVisible().clone());
		} else {
			v0.setHeadersVisible(null);
		}
		if (this.getHeadersUpdatable() != null) {
			v0.setHeadersUpdatable((ArrayList<Boolean>) this.getHeadersUpdatable().clone());
		} else {
			v0.setHeadersUpdatable(null);
		}
		if (this.getHeadersRequired() != null) {
			v0.setHeadersRequired((ArrayList<Boolean>) this.getHeadersRequired().clone());
		} else {
			v0.setHeadersRequired(null);
		}
		if (this.getHeaderSortDLabels() != null) {
			v0.setHeaderSortDLabels((ArrayList<String>) this.getHeaderSortDLabels().clone());
		} else {
			v0.setHeaderSortDLabels(null);
		}
		if (this.getHeaderSortDOrders() != null) {
			v0.setHeaderSortDOrders((ArrayList<Boolean>) this.getHeaderSortDOrders().clone());
		} else {
			v0.setHeaderSortDOrders(null);
		}
		v0.setHeaderSortDLabel(this.getHeaderSortDLabel());
		if (this.getSelectedLines() != null) {
			v0.setSelectedLines((ArrayList<Boolean>) this.getSelectedLines().clone());
		} else {
			v0.setSelectedLines(null);
		}
		if (this.getSelectedColumns() != null) {
			v0.setSelectedColumns((ArrayList<Boolean>) this.getSelectedColumns().clone());
		} else {
			v0.setSelectedColumns(null);
		}
		v0.setHeaderSortDLabel(this.getHeaderSortDLabel());
		if (this.getDefaultInputFields() != null) {
			v0.setDefaultInputFields((HashMap<String, String>) this.getDefaultInputFields().clone());
		} else {
			v0.setDefaultInputFields(null);
		}
		if (this.getInputFields() != null) {
			v0.setInputFields((ArrayList<String>) this.getInputFields().clone());
		} else {
			v0.setInputFields(null);
		}
		v0.setNbPages(this.getNbPages());
		v0.setIdPage(this.getIdPage());
		if (this.getFilterFields() != null) {
			v0.setFilterFields((ArrayList<String>) this.getFilterFields().clone());
		} else {
			v0.setFilterFields(null);
		}
		if (this.getConstantVObject() != null) {
			v0.setConstantVObject(this.getConstantVObject());
		}
		if (this.getCustomValues() != null) {
			v0.setCustomValues((HashMap<String, String>) this.getCustomValues().clone());
		}
		if (this.getMapCustomValues() != null) {
			v0.setMapCustomValues((HashMap<String, HashMap<String, String>>) this.getMapCustomValues().clone());
		}
		return v0;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Boolean getIsScoped() {
		return isScoped;
	}

	public void setIsScoped(boolean isScoped) {
		this.isScoped = isScoped;
	}

	public void setInitialized(boolean isInitialized) {
		this.isInitialized = isInitialized;
	}


	public ArrayList<String> getV(int j, TableObject content) {
		ArrayList<String> h = new ArrayList<String>();
		for (int i = 0; i < content.t.size(); i++) {
			h.add(content.t.get(i).d.get(j));
		}
		return h;
	}

	public HashMap<String, String> getCustomValues() {
		return customValues;
	}

	public void setCustomValues(HashMap<String, String> customValues) {
		this.customValues = customValues;
	}

	public HashMap<String, HashMap<String, String>> getMapCustomValues() {
		return mapCustomValues;
	}

	public void setMapCustomValues(HashMap<String, HashMap<String, String>> mapCustomValues) {
		this.mapCustomValues = mapCustomValues;
	}

	public void setScoped(boolean isScoped) {
		this.isScoped = isScoped;
	}

	public boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(boolean isActive) {
		this.isActive = isActive;
	}

	/**
	 *
	 *
	 * @return le nombre de lignes de ceci
	 */
	public int getNombreLigne() {
		return this.getContent().size();
	}


	/**
	 *
	 *
	 * @return {@code true} si aucune ligne dans {@code this}
	 */
	public boolean isEmpty() {
		return this.getNombreLigne() <= 0;
	}

	public ArrayList<File> getFileUpload() {
		return fileUpload;
	}

	public void setFileUpload(ArrayList<File> fileUpload) {
		this.fileUpload = fileUpload;
	}

	public ArrayList<String> getFileUploadFileName() {
		return fileUploadFileName;
	}

	public void setFileUploadFileName(ArrayList<String> fileUploadFileName) {
		this.fileUploadFileName = fileUploadFileName;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public void clear() {
		getCustomValues().clear();
        getMapCustomValues().clear();
        setIsInitialized(false);		
	}


}