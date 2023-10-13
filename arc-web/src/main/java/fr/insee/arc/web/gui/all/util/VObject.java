package fr.insee.arc.web.gui.all.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.utils.ManipString;

public class VObject {

	/** Titre de la fenêtre */
	private String title;

	/** Nom dans la session */
	private String sessionName;

	/** Nombre de lignes par page par défaut. */
	private int defaultPaginationSize;

	/** Nombre de lignes par page */
	private Integer paginationSize;

	/** Requête de génération du tableau */
	private ArcPreparedStatementBuilder mainQuery;

	private ArcPreparedStatementBuilder beforeSelectQuery;

	private ArcPreparedStatementBuilder afterUpdateQuery;

	private ArcPreparedStatementBuilder afterInsertQuery;

	/** Table utilisée pour les update/insert/delete */
	private String table;

	/** Tableau du contenu de la requete (ligne, colonne) */
	private TableObject content;

	/** Previous state of the content (from session) */
	private TableObject savedContent;

	/** Rendering for this */
	private ConstantVObject constantVObject;

	/** Indicateur d'initialisation */
	private boolean isInitialized;
	private boolean isScoped;
	private boolean isActive;

	// nom des colonnes, type en base (D=Database), label dans la vue (V=VUE) ,
	// taille sur la vue
	/** Noms des colonnes en base. */
	private List<String> headersDLabel;
	/** Types des colonnes en base. */
	private List<String> headersDType;
	/** Noms des colonnes dans la vue. */
	private List<String> headersVLabel;
	/** Tailles des colonnes dans la vue. */
	private List<String> headersVSize;
	/** Types des colonnes dans la vue. */
	private List<String> headersVType;
	private List<Map<String, String>> headersVSelect;
	private List<Boolean> headersVisible;
	private List<Boolean> headersUpdatable;
	private List<Boolean> headersRequired;
	/** Tableau des lignes selectionnées */
	private List<Boolean> selectedLines;
	/** Tableau des colonnes selectionnées */
	private List<Boolean> selectedColumns;
	// champs de saisie
	private Map<String, String> defaultInputFields;
	private List<String> inputFields;
	// Pagination
	private Integer nbPages;
	private String idPage;

	// Gestion du tri
	/** Liste des colonnes utilisées pour trier la table */
	private List<String> headerSortDLabels;
	/**
	 * Liste des directions de tri (des colonnes nommées dans headerSortDLabels).
	 * true = ascending, false = descending.
	 */
	private List<Boolean> headerSortDOrders;
	/** La colonne cliquée par l'utilisateur pour le tri. */
	private String headerSortDLabel;

	private List<String> filterFields;

	private String message;

	private Object[] messageArgs;

	private Map<String, String> customValues;

	// pagination and order attribute
	private boolean noOrder = VObjectService.DEFAULT_NO_ORDER;
	private boolean noCount = VObjectService.DEFAULT_NO_COUNT;
	private boolean noLimit = VObjectService.DEFAULT_NO_LIMIT;

	// filtering
	private int filterPattern = VObjectService.DEFAULT_FILTER_PATTERN;
	private String filterFunction = VObjectService.DEFAULT_FILTER_FUNCTION;

	public List<List<String>> listContent() {
		if (getSavedContent() == null) {
			return new ArrayList<>();
		}
		List<List<String>> c = new ArrayList<>();
		for (int i = 0; i < getSavedContent().size(); i++) {
			List<String> l = new ArrayList<>();
			for (int j = 0; j < getSavedContent().get(i).d.size(); j++) {
				l.add(getSavedContent().get(i).d.get(j));
			}
			c.add(l);
		}
		return c;
	}

	/**
	 * Retourne une hash map qui pour chaque entete de colonne (clé), donne la liste
	 * de toutes les valeurs
	 */
	public Map<String, List<String>> mapContent() {
		return new GenericBean(getHeadersDLabel(), getHeadersDType(), listContent()).mapContent();
	}

	/** Returns the old values for the lines with new content. */
	public List<List<String>> listContentBeforeUpdate() {
		if (getSavedContent() == null) {
			return new ArrayList<>();
		}
		List<List<String>> r = new ArrayList<>();
		// comparaison des lignes dans la table avant et aprés
		// toBeUpdated contient l'identifiant des lignes à update
		for (int i = 0; i < getContent().size(); i++) {
			int j = 0;
			boolean equals = true;
			while (j < getContent().get(i).d.size() && equals) {
				equals = compareOldAndNew(getContent().get(i).d.get(j), getSavedContent().get(i).d.get(j));
				j++;
			}
			if (!equals) {
				r.add(getSavedContent().get(i).d);
			}
		}
		return r;
	}

	private boolean compareOldAndNew(String newContentValue, String oldContentValue) {
		return newContentValue == null || ManipString.compareStringWithNull(oldContentValue, newContentValue);
	}

	public Map<String, List<String>> mapContentBeforeUpdate() {
		return new GenericBean(getHeadersDLabel(), getHeadersDType(), listContentBeforeUpdate()).mapContent();
	}

	public Map<String, List<String>> mapContentBeforeUpdate(int i) {
		List<List<String>> r = new ArrayList<>();
		r.add(listContentBeforeUpdate().get(i));
		return new GenericBean(getHeadersDLabel(), getHeadersDType(), r).mapContent();
	}

	/** Returns the lines with the new content. */
	public List<List<String>> listContentAfterUpdate() {
		if (getSavedContent() == null) {
			return new ArrayList<>();
		}
		List<List<String>> r = new ArrayList<>();
		// comparaison des lignes dans la table avant et aprés
		// toBeUpdated contient l'identifiant des lignes à update
		for (int i = 0; i < getContent().size(); i++) {
			int j = 0;
			boolean equals = true;
			while (j < getContent().get(i).d.size() && equals) {
				equals = compareOldAndNew(getContent().get(i).d.get(j), getSavedContent().get(i).d.get(j));
				j++;
			}
			if (!equals) {
				r.add(getContent().get(i).d);
			}
		}
		return r;
	}

	public Map<String, List<String>> mapContentAfterUpdate() {
		return new GenericBean(getHeadersDLabel(), getHeadersDType(), listContentAfterUpdate()).mapContent();
	}

	public Map<String, List<String>> mapContentAfterUpdate(int i) {
		List<List<String>> r = new ArrayList<>();
		r.add(listContentAfterUpdate().get(i));
		return new GenericBean(getHeadersDLabel(), getHeadersDType(), r).mapContent();
	}

	/** Returns the content as it would be after the update. */
	public List<List<String>> listUpdatedContent() {
		if (getSavedContent() == null) {
			return new ArrayList<>();
		}
		List<List<String>> r = new ArrayList<>();
		for (int i = 0; i < getSavedContent().size(); i++) {
			r.add(new ArrayList<>());
			for (int j = 0; j < getSavedContent().get(i).d.size(); j++) {
				String oldContentValue = getSavedContent().get(i).d.get(j);
				if (i >= getContent().size() || j >= getContent().get(i).d.size()) {
					r.get(i).add(oldContentValue);
				} else {
					String newContentValue = getContent().get(i).d.get(j);
					if (compareOldAndNew(newContentValue, oldContentValue)) {
						r.get(i).add(oldContentValue);
					} else {
						r.get(i).add(newContentValue);
					}
				}

			}
		}
		return r;
	}

	public Map<String, List<String>> mapUpdatedContent() {
		return new GenericBean(getHeadersDLabel(), getHeadersDType(), listUpdatedContent()).mapContent();
	}

	/**
	 * Returns the content as it would be after the update, only on the changed
	 * lines.
	 */
	public List<List<String>> listOnlyUpdatedContent() {
		if (getSavedContent() == null) {
			return new ArrayList<>();
		}
		List<List<String>> r = new ArrayList<>();
		for (int i = 0; i < getContent().size(); i++) {
			List<String> line = new ArrayList<>();
			boolean changed = false;
			for (int j = 0; j < getContent().get(i).d.size(); j++) {
				String oldContentValue = getSavedContent().get(i).d.get(j);

				String newContentValue = getContent().get(i).d.get(j);
				if (compareOldAndNew(newContentValue, oldContentValue)) {
					line.add(oldContentValue);
				} else {
					line.add(newContentValue);
					changed = true;
				}
			}
			if (changed) {
				r.add(line);
			}
		}
		return r;
	}

	public Map<String, List<String>> mapOnlyUpdatedContent() {
		return new GenericBean(getHeadersDLabel(), getHeadersDType(), listOnlyUpdatedContent()).mapContent();
	}

	public List<List<String>> listContentSelected() {
		List<List<String>> r = new ArrayList<>();
		// si rien dans la liste, return null
		if (getSelectedLines() == null || getSelectedLines().isEmpty()) {
			return r;
		}
		for (int j = 0; j < getSelectedLines().size(); j++) {
			if (Boolean.TRUE.equals(getSelectedLines().get(j))) {
				r.add(getSavedContent().get(j).d);
			}
		}
		return r;
	}

	/**
	 * Retourne une hash map qui pour chaque entete de colonne (clé), donne la liste
	 * de toutes les valeurs selectionnées
	 */
	public Map<String, List<String>> mapContentSelected() {
		return new GenericBean(getHeadersDLabel(), getHeadersDType(), listContentSelected()).mapContent();
	}

	/** Return the index of headers selected */
	public List<Integer> indexHeadersSelected() {
		if (getSavedContent() == null) {
			return new ArrayList<>();
		}

		List<String> listHeadersSelected = listHeadersSelected();
		List<Integer> indexHeadersSelected = new ArrayList<>();
		for (Integer i = 0; i < getHeadersDLabel().size(); i++) {
			if (listHeadersSelected.contains(getHeadersDLabel().get(i))) {
				indexHeadersSelected.add(i);
			}

		}
		return indexHeadersSelected;
	}

	/**
	 * Retourne la liste des entetes base de donnée selectionnés
	 */
	public List<String> listHeadersSelected() {
		if (getSavedContent() == null) {
			return new ArrayList<>();
		}
		List<String> r = new ArrayList<>();
		if (getSelectedColumns() == null || getSelectedColumns().isEmpty()) {
			return r;
		}
		for (int i = 0; i < getSelectedColumns().size(); i++) {
			if (Boolean.TRUE.equals(getSelectedColumns().get(i))) {
				r.add(getHeadersDLabel().get(i));
			}
		}
		return r;
	}

	public Map<String, String> mapHeadersSelected() {
		Map<String, String> r = new HashMap<>();
		for (String s : listHeadersSelected()) {
			r.put(s, s);
		}
		return r;
	}

	public Map<String, String> mapHeadersType() {
		return new GenericBean(getHeadersDLabel(), getHeadersDType(), null).mapTypes();
	}

	public List<List<String>> listInputFields() {
		List<List<String>> r = new ArrayList<>();
		r.add(getInputFields());
		return r;
	}

	public Map<String, List<String>> mapInputFields() {
		return new GenericBean(getHeadersDLabel(), getHeadersDType(), listInputFields()).mapContent();
	}

	/**
	 * Modifie la valeur d'input pour la colonne demandée.
	 * 
	 * @param headerDLabel nom de la colonne en base
	 * @param value        valeur à attribuer
	 * @throw IllegalArgumentException si le nom de colonne est invalide
	 */
	public void setInputFieldFor(String headerDLabel, String value) {
		int index = getHeadersDLabel().indexOf(headerDLabel);
		if (index == -1) {
			throw new IllegalArgumentException("Field " + headerDLabel + " was not found.");
		}
		getInputFields().set(index, value);
	}

	/**
	 * Retourne la valeur d'input pour la colonne demandée. Forme abrégée de
	 * mapInputFields().get("ma_colonne").get(0).
	 * 
	 * @param headerDLabel nom de la colonne en base
	 * @throw IllegalArgumentException si le nom de colonne est invalide
	 */
	public String getInputFieldFor(String headerDLabel) {
		int index = getHeadersDLabel().indexOf(headerDLabel);
		if (index == -1) {
			throw new IllegalArgumentException("Field " + headerDLabel + " was not found.");
		}
		return mapInputFields().get(headerDLabel).get(0);
	}

	public List<List<String>> listLineContent(int i) {
		List<List<String>> r = new ArrayList<>();
		r.add(getContent().get(i).d);
		return r;
	}

	public Map<String, List<String>> mapLineContent(int i) {
		return new GenericBean(getHeadersDLabel(), getHeadersDType(), listLineContent(i)).mapContent();
	}

	public Map<String, List<String>> mapFilterFields() {
		if (getFilterFields() == null) {
			return new HashMap<>();
		}

		List<List<String>> r = new ArrayList<>();
		r.add(getFilterFields());
		return new GenericBean(getHeadersDLabel(), getHeadersDType(), r).mapContent();
	}

	VObject copy() {
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
			v0.setHeadersDLabel(new ArrayList<>(this.getHeadersDLabel()));
		} else {
			v0.setHeadersDLabel(null);
		}
		if (this.getHeadersDType() != null) {
			v0.setHeadersDType(new ArrayList<>(this.getHeadersDType()));
		} else {
			v0.setHeadersDType(null);
		}
		if (this.getHeadersVLabel() != null) {
			v0.setHeadersVLabel(new ArrayList<>(this.getHeadersVLabel()));
		} else {
			v0.setHeadersVLabel(null);
		}
		if (this.getHeadersVSize() != null) {
			v0.setHeadersVSize(new ArrayList<>(this.getHeadersVSize()));
		} else {
			v0.setHeadersVSize(null);
		}
		if (this.getHeadersVType() != null) {
			v0.setHeadersVType(new ArrayList<>(this.getHeadersVType()));
		} else {
			v0.setHeadersVType(null);
		}
		if (this.getHeadersVSelect() != null) {
			v0.setHeadersVSelect(new ArrayList<>(this.getHeadersVSelect()));
		} else {
			v0.setHeadersVSelect(null);
		}
		if (this.getHeadersVisible() != null) {
			v0.setHeadersVisible(new ArrayList<>(this.getHeadersVisible()));
		} else {
			v0.setHeadersVisible(null);
		}
		if (this.getHeadersUpdatable() != null) {
			v0.setHeadersUpdatable(new ArrayList<>(this.getHeadersUpdatable()));
		} else {
			v0.setHeadersUpdatable(null);
		}
		if (this.getHeadersRequired() != null) {
			v0.setHeadersRequired(new ArrayList<>(this.getHeadersRequired()));
		} else {
			v0.setHeadersRequired(null);
		}
		if (this.getHeaderSortDLabels() != null) {
			v0.setHeaderSortDLabels(new ArrayList<>(this.getHeaderSortDLabels()));
		} else {
			v0.setHeaderSortDLabels(null);
		}
		if (this.getHeaderSortDOrders() != null) {
			v0.setHeaderSortDOrders( new ArrayList<>(this.getHeaderSortDOrders()));
		} else {
			v0.setHeaderSortDOrders(null);
		}
		v0.setHeaderSortDLabel(this.getHeaderSortDLabel());
		if (this.getSelectedLines() != null) {
			v0.setSelectedLines(new ArrayList<>(this.getSelectedLines()));
		} else {
			v0.setSelectedLines(null);
		}
		if (this.getSelectedColumns() != null) {
			v0.setSelectedColumns(new ArrayList<>(this.getSelectedColumns()));
		} else {
			v0.setSelectedColumns(null);
		}
		v0.setHeaderSortDLabel(this.getHeaderSortDLabel());
		if (this.getDefaultInputFields() != null) {
			v0.setDefaultInputFields(new HashMap<>(this.getDefaultInputFields()));
		} else {
			v0.setDefaultInputFields(null);
		}
		if (this.getInputFields() != null) {
			v0.setInputFields(new ArrayList<>(this.getInputFields()));
		} else {
			v0.setInputFields(null);
		}
		v0.setNbPages(this.getNbPages());
		v0.setIdPage(this.getIdPage());
		if (this.getFilterFields() != null) {
			v0.setFilterFields(new ArrayList<>(this.getFilterFields()));
		} else {
			v0.setFilterFields(null);
		}
		if (this.getConstantVObject() != null) {
			v0.setConstantVObject(this.getConstantVObject());
		}
		if (this.getCustomValues() != null) {
			v0.setCustomValues(new HashMap<>(this.getCustomValues()));
		}
		return v0;
	}

	public List<String> getV(int j, TableObject content) {
		List<String> h = new ArrayList<>();
		if (content == null) {
			return h;
		}
		for (int i = 0; i < content.t.size(); i++) {
			h.add(content.t.get(i).d.get(j));
		}
		return h;
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

	public void clear() {
		setIsInitialized(false);
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

	public int getDefaultPaginationSize() {
		return defaultPaginationSize;
	}

	public void setDefaultPaginationSize(int paginationSize) {
		this.defaultPaginationSize = paginationSize;
	}

	public Integer getPaginationSize() {
		return paginationSize;
	}

	public void setPaginationSize(Integer paginationSize) {
		this.paginationSize = paginationSize;
	}

	public ArcPreparedStatementBuilder getMainQuery() {
		return mainQuery;
	}

	public void setMainQuery(ArcPreparedStatementBuilder mainQuery) {
		this.mainQuery = mainQuery;
	}

	public ArcPreparedStatementBuilder getBeforeSelectQuery() {
		return beforeSelectQuery;
	}

	public void setBeforeSelectQuery(ArcPreparedStatementBuilder beforeSelectQuery) {
		this.beforeSelectQuery = beforeSelectQuery;
	}

	public ArcPreparedStatementBuilder getAfterUpdateQuery() {
		return afterUpdateQuery;
	}

	public void setAfterUpdateQuery(ArcPreparedStatementBuilder afterUpdateQuery) {
		this.afterUpdateQuery = afterUpdateQuery;
	}

	public ArcPreparedStatementBuilder getAfterInsertQuery() {
		return afterInsertQuery;
	}

	public void setAfterInsertQuery(ArcPreparedStatementBuilder afterInsertQuery) {
		this.afterInsertQuery = afterInsertQuery;
	}

	public void setInitialized(boolean isInitialized) {
		this.isInitialized = isInitialized;
	}

	public void setScoped(boolean isScoped) {
		this.isScoped = isScoped;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
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

	public TableObject getSavedContent() {
		return savedContent;
	}

	public void setSavedContent(TableObject savedContent) {
		this.savedContent = savedContent;
	}

	public ConstantVObject getConstantVObject() {
		return constantVObject;
	}

	public void setConstantVObject(ConstantVObject constantVObject) {
		this.constantVObject = constantVObject;
	}

	public boolean getIsInitialized() {
		return isInitialized;
	}

	public void setIsInitialized(boolean isInitialized) {
		this.isInitialized = isInitialized;
	}

	public boolean getIsScoped() {
		return isScoped;
	}

	public void setIsScoped(boolean isScoped) {
		this.isScoped = isScoped;
	}

	public boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(boolean isActive) {
		this.isActive = isActive;
	}

	public List<String> getHeadersDLabel() {
		return headersDLabel;
	}

	public void setHeadersDLabel(List<String> headersDLabel) {
		this.headersDLabel = headersDLabel;
	}

	public List<String> getHeadersDType() {
		return headersDType;
	}

	public void setHeadersDType(List<String> headersDType) {
		this.headersDType = headersDType;
	}

	public List<String> getHeadersVLabel() {
		return headersVLabel;
	}

	public void setHeadersVLabel(List<String> headersVLabel) {
		this.headersVLabel = headersVLabel;
	}

	public List<String> getHeadersVSize() {
		return headersVSize;
	}

	public void setHeadersVSize(List<String> headersVSize) {
		this.headersVSize = headersVSize;
	}

	public List<String> getHeadersVType() {
		return headersVType;
	}

	public void setHeadersVType(List<String> headersVType) {
		this.headersVType = headersVType;
	}

	public List<Map<String, String>> getHeadersVSelect() {
		return headersVSelect;
	}

	public void setHeadersVSelect(List<Map<String, String>> headersVSelect) {
		this.headersVSelect = headersVSelect;
	}

	public List<Boolean> getHeadersVisible() {
		return headersVisible;
	}

	public void setHeadersVisible(List<Boolean> headersVisible) {
		this.headersVisible = headersVisible;
	}

	public List<Boolean> getHeadersUpdatable() {
		return headersUpdatable;
	}

	public void setHeadersUpdatable(List<Boolean> headersUpdatable) {
		this.headersUpdatable = headersUpdatable;
	}

	public List<Boolean> getHeadersRequired() {
		return headersRequired;
	}

	public void setHeadersRequired(List<Boolean> headersRequired) {
		this.headersRequired = headersRequired;
	}

	public List<Boolean> getSelectedLines() {
		return selectedLines;
	}

	public void setSelectedLines(List<Boolean> selectedLines) {
		this.selectedLines = selectedLines;
	}

	public List<Boolean> getSelectedColumns() {
		return selectedColumns;
	}

	public void setSelectedColumns(List<Boolean> selectedColumns) {
		this.selectedColumns = selectedColumns;
	}

	public Map<String, String> getDefaultInputFields() {
		return defaultInputFields;
	}

	public void setDefaultInputFields(Map<String, String> defaultInputFields) {
		this.defaultInputFields = defaultInputFields;
	}

	public List<String> getInputFields() {
		return inputFields;
	}

	public void setInputFields(List<String> inputFields) {
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

	public List<String> getHeaderSortDLabels() {
		return headerSortDLabels;
	}

	public void setHeaderSortDLabels(List<String> headerSortDLabels) {
		this.headerSortDLabels = headerSortDLabels;
	}

	public List<Boolean> getHeaderSortDOrders() {
		return headerSortDOrders;
	}

	public void setHeaderSortDOrders(List<Boolean> headerSortDOrders) {
		this.headerSortDOrders = headerSortDOrders;
	}

	public String getHeaderSortDLabel() {
		return headerSortDLabel;
	}

	public void setHeaderSortDLabel(String headerSortDLabel) {
		this.headerSortDLabel = headerSortDLabel;
	}

	public List<String> getFilterFields() {
		return filterFields;
	}

	public void setFilterFields(List<String> filterFields) {
		this.filterFields = filterFields;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Object[] getMessageArgs() {
		return messageArgs;
	}

	public void setMessageArgs(Object... messageArgs) {
		this.messageArgs = messageArgs;
	}

	public Map<String, String> getCustomValues() {
		return customValues;
	}

	public void setCustomValues(Map<String, String> customValues) {
		this.customValues = customValues;
	}

	public String getCustomValue(String key) {
		if (getCustomValues() == null) {
			return null;
		}
		return getCustomValues().get(key);
	}

	public void setCustomValue(String key, String value) {
		if (getCustomValues() == null) {
			setCustomValues(new HashMap<>());
		}
		getCustomValues().put(key, value);
	}

	private List<MultipartFile> fileUpload;

	public List<MultipartFile> getFileUpload() {
		return fileUpload;
	}

	public void setFileUpload(List<MultipartFile> fileUpload) {
		this.fileUpload = fileUpload;
	}

	public boolean isNoOrder() {
		return noOrder;
	}

	public void setNoOrder(boolean noOrder) {
		this.noOrder = noOrder;
	}

	public boolean isNoCount() {
		return noCount;
	}

	public void setNoCount(boolean noCount) {
		this.noCount = noCount;
	}

	public boolean isNoLimit() {
		return noLimit;
	}

	public void setNoLimit(boolean noLimit) {
		this.noLimit = noLimit;
	}

	public int getFilterPattern() {
		return filterPattern;
	}

	public void setFilterPattern(int filterPattern) {
		this.filterPattern = filterPattern;
	}

	public String getFilterFunction() {
		return filterFunction;
	}

	public void setFilterFunction(String filterFunction) {
		this.filterFunction = filterFunction;
	}

}