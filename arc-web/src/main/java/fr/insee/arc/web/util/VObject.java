package fr.insee.arc.web.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.springframework.stereotype.Component;

import fr.insee.arc.utils.dao.ModeRequete;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.structure.AttributeValue;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.utils.LoggerDispatcher;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.ManipString;
import fr.insee.arc.utils.utils.SQLExecutor;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;

/**
 * An custom object to easly manipulate and display in IHM database object (or
 * more generaly table data). Can interact with the database (CRUD
 * functionalities), extract the data in a file, and will keep some style
 * information fore the GUI (see {@link ConstantVObject}) (it's kinda a bad
 * practise, but it work)
 * 
 * Struts2 dependant for now
 * 
 * @author Soulier Manuel
 *
 */
@Component
public class VObject implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 6609509084694479069L;
    private static final Logger LOGGER = Logger.getLogger(VObject.class);
    /**
     *
     */
    private String pool;

    // The previous VObject put in session for persistance.
    private transient VObject inSessionVObject;

    // Title for the gui
    private String title;
    // Session name
    private String sessionName;
    // Number of reccord in a page
    private Integer paginationSize;
    // To keep in mind if the VObject has already been initialized to avoid some
    // useless recreation
    private Boolean isInitialized;
    // Is
    private Boolean isActive;
    private Boolean isScoped;
    private Boolean noOrder = false;

    // The query to get the data if a database is use
    private String mainQuery;

    private String beforeSelectQuery;
    private String afterUpdateQuery;
    private String afterInsertQuery;

    // Table used for the update/insert/delete (can be different from the one use to
    // get the data)
    private String table;

    // Rendering for this
    protected ConstantVObject constantVObject;

    // content of the VObjct (table)
    private TableObject content;

    // List of the header of the column in databse
    private ArrayList<String> databaseColumnsLabel;

    // List of type of the column in database
    private ArrayList<String> databaseColumnsType;

    // List of the header of the column in the GUI
    private ArrayList<String> guiColumnsLabel;

    // List of the size of the column in GUI
    private ArrayList<String> guiColumnsSize;

    // List of type of the column in GUI
    private ArrayList<String> guiColumnsType;

    // List of option of a select object
    private ArrayList<LinkedHashMap<String, String>> guiSelectedColumns;

    // The visible column in the GUI (this list size is the same as the header list,
    // if the nth element of this list is true, the nth header will be visible)
    private ArrayList<Boolean> visibleHeaders;

    // The updatable column column in the GUI (this list size is the same as the
    // header list,
    // if the nth element of this list is true, the nth header will be updatable)
    private ArrayList<Boolean> updatableHeaders;

    // The required column in the GUI (this list size is the same as the header
    // list,
    // if the nth element of this list is true, the nth header will be required)
    private ArrayList<Boolean> requiredHeaders;

    // Sort magement
    // The list of the column use to sort the table
    private ArrayList<String> databaseColumnsSortLabel;

    // Asc or desc order
    private ArrayList<Boolean> databaseColumnsSortOrder;

    // the column being clicked by the user to be sort
    private String databaseColumnSort;

    // The selected lines
    private ArrayList<Boolean> selectedLines;

    // The slected columns
    private ArrayList<Boolean> selectedColumns;

    // the default input field value. can be usefull for set field. The kay of the
    // map are the column name
    private HashMap<String, String> defaultInputFields;

    // the value of the input field send by the gui
    private ArrayList<String> inputFields;
    // Pagination
    // The number of page
    private Integer nbPages;
    // the current page
    private String idPage;
    // filtering
    private ArrayList<String> filterFields;
    private int filterPattern = 0;
    private String filterFunction = "upper";

    // upload
    private ArrayList<File> fileUpload;
    private ArrayList<String> fileUploadFileName;
    private String message;

    public Map<String, String> customValues;
    public Map<String, HashMap<String, String>> mapCustomValues;

    public VObject() {
	super();
	this.customValues = new HashMap<>();
	this.mapCustomValues = new HashMap<>();
	this.isInitialized = false;
    }

    /**
     * This method makes a "deep clone" of a given Vobject.
     * 
     * @author Alvin Alexander, http://alvinalexander.com
     */
    public VObject deepClone() {
	try {
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    ObjectOutputStream oos = new ObjectOutputStream(baos);
	    oos.writeObject(this);
	    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
	    ObjectInputStream ois = new ObjectInputStream(bais);
	    return (VObject) ois.readObject();
	} catch (Exception e) {
	    LoggerHelper.error(LOGGER, "Error when deep cloning", e);
	    return null;
	}
    }

    /**
     * Absolument indispensable dans le cas ou des tables de vues sont générées
     * dynamiquement
     *
     * @param aRendering
     */
    public void initialiserColumnRendering(Map<String, ColumnRendering> aRendering) {

	this.constantVObject.setColumnRender(aRendering);
    }

    /**
     * A vouaire
     */
    public void lock() {
	for (String key : this.constantVObject.getColumnRender().keySet()) {
	    this.constantVObject.getColumnRender().get(key).isUpdatable = false;
	}
    }

    /*
     * Génére les labels de colonnes. Si une déclaration est faite dans
     * ConstantVObject, on met le label déclaré Sinon on garde le nom de colonne de
     * la base de données
     */
    public List<String> buildGuiColumnsLabel() {
	List<String> headersVLabel = new ArrayList<>();
	for (int i = 0; i < this.databaseColumnsLabel.size(); i++) {
	    if (this.getConstantVObject()//
		    .getColumnRender()//
		    .get(this.databaseColumnsLabel.get(i)) != null) {
		headersVLabel.add(this.constantVObject.getColumnRender().get(this.databaseColumnsLabel.get(i)).label);
	    } else {
		headersVLabel.add(ManipString.translateAscii(this.databaseColumnsLabel.get(i)));
	    }
	}
	return headersVLabel;
    }

    public ConstantVObject getConstantVObject() {
	return this.constantVObject;
    }

    public List<String> buildGuiColumnsSize() {
	List<String> headersVSize = new ArrayList<>();
	for (int i = 0; i < this.databaseColumnsLabel.size(); i++) {
	    if (this.constantVObject.getColumnRender().get(this.databaseColumnsLabel.get(i)) != null) {
		headersVSize.add(this.constantVObject.getColumnRender().get(this.databaseColumnsLabel.get(i)).size);
	    } else {
		headersVSize.add("/**/");
	    }
	}
	return headersVSize;
    }

    public List<String> buildGuiColumnsType() {
	List<String> headersVType = new ArrayList<>();
	for (int i = 0; i < this.databaseColumnsLabel.size(); i++) {
	    if (this.constantVObject.getColumnRender().get(this.databaseColumnsLabel.get(i)) != null) {
		headersVType.add(this.constantVObject.getColumnRender().get(this.databaseColumnsLabel.get(i)).type);
	    } else {
		headersVType.add("text");
	    }
	}
	return headersVType;
    }

    public List<Boolean> buildVisibleHeaders() {
	List<Boolean> headersVisible = new ArrayList<>();
	for (int i = 0; i < this.databaseColumnsLabel.size(); i++) {
	    if (this.constantVObject.getColumnRender().get(this.databaseColumnsLabel.get(i)) != null) {
		headersVisible
			.add(this.constantVObject.getColumnRender().get(this.databaseColumnsLabel.get(i)).visible);
	    } else {
		headersVisible.add(true);
	    }
	}
	return headersVisible;
    }

    public List<Boolean> buildUpdatableHeaders() {
	List<Boolean> headersUpdatable = new ArrayList<>();
	for (int i = 0; i < this.databaseColumnsLabel.size(); i++) {
	    if (this.constantVObject.getColumnRender().get(this.databaseColumnsLabel.get(i)) != null) {
		headersUpdatable
			.add(this.constantVObject.getColumnRender().get(this.databaseColumnsLabel.get(i)).isUpdatable);
	    } else {
		headersUpdatable.add(true);
	    }
	}
	return headersUpdatable;
    }

    public List<Boolean> buildRequiredHeaders() {
	List<Boolean> headersRequired = new ArrayList<>();
	for (int i = 0; i < this.databaseColumnsLabel.size(); i++) {
	    if (this.constantVObject.getColumnRender().get(this.databaseColumnsLabel.get(i)) != null) {
		headersRequired
			.add(this.constantVObject.getColumnRender().get(this.databaseColumnsLabel.get(i)).isRequired);
	    } else {
		headersRequired.add(true);
	    }
	}
	return headersRequired;
    }
    @SQLExecutor
    public List<LinkedHashMap<String, String>> buildGuiSelectedColumns() {
	List<ArrayList<String>> arrayVSelect = new ArrayList<>();
	List<LinkedHashMap<String, String>> headerVSelect = new ArrayList<LinkedHashMap<String, String>>();
	for (int i = 0; i < databaseColumnsLabel.size(); i++) {
	    if (this.constantVObject.getColumnRender().get(databaseColumnsLabel.get(i)) != null
		    && this.constantVObject.getColumnRender().get(databaseColumnsLabel.get(i)).query != null) {
		try {
		    arrayVSelect = UtilitaireDao.get(this.pool).executeRequest(null,
			    this.constantVObject.getColumnRender().get(databaseColumnsLabel.get(i)).query);
		    arrayVSelect.remove(0);
		    arrayVSelect.remove(0);
		    LinkedHashMap<String, String> m = new LinkedHashMap<String, String>();
		    for (int j = 0; j < arrayVSelect.size(); j++) {
			m.put(arrayVSelect.get(j).get(0), arrayVSelect.get(j).get(1));
		    }
		    headerVSelect.add(m);
		} catch (SQLException ex) {
		    this.message = ex.getMessage();
		    LoggerHelper.errorGenTextAsComment(getClass(), "buildHeadersVSelect()", LOGGER, ex);
		}
	    } else {
		LinkedHashMap<String, String> m = new LinkedHashMap<String, String>();
		headerVSelect.add(m);
	    }
	}
	return headerVSelect;
    }

    /**
     * Reset the input field with the default value. Thode value can be pass throw
     * the defaultInputFields param, or are null by default
     * 
     * @param defaultInputFields
     *            : a map <columnHeader, value> containing the field with define
     *            value.
     */
    public void resetInputFieldsValues() {
	List<String> inputFieldsTemp = new ArrayList<>();
	for (int i = 0; i < this.databaseColumnsLabel.size(); i++) {
	    if (this.defaultInputFields.get(this.databaseColumnsLabel.get(i)) != null) {
		inputFieldsTemp.add(this.defaultInputFields.get(this.databaseColumnsLabel.get(i)));
	    } else {
		inputFieldsTemp.add(null);
	    }
	}
	setInputFields(inputFieldsTemp);
    }

    public static <T> List<T> copyList(List<T> source) {
	List<T> dest = new ArrayList<>();
	for (T item : source) {
	    dest.add(item);
	}
	return dest;
    }

    /**
     *
     * @param mainQuery
     *            ne doit pas se terminer par des {@code ;}
     * @param table
     * @param defaultInputFields
     */
    @SuppressWarnings("unchecked")
    @SQLExecutor
    public void initialize(String mainQuery, String table, Map<String, String> defaultInputFields) {
	LoggerHelper.debug(LOGGER, "initialize", this.sessionName);

	setMainQuery(mainQuery);
	setTable(table);

	try {

	    if (this.beforeSelectQuery != null) {
		UtilitaireDao.get(this.pool).executeRequest(null, this.beforeSelectQuery);
	    }
	    HttpSession session = ServletActionContext.getRequest().getSession(false);
	    this.inSessionVObject = (VObject) session.getAttribute(this.sessionName);

	    if (inSessionVObject != null) {
		if (StringUtils.isEmpty(this.databaseColumnSort)) {
		    setHeaderSortDLabel(inSessionVObject.databaseColumnSort);
		}
		if (CollectionUtils.isEmpty(this.databaseColumnsSortLabel )) {
		    setDatabaseColumnsSort(inSessionVObject.databaseColumnsSortLabel);
		}
		if (CollectionUtils.isEmpty(this.databaseColumnsSortOrder )) {
		    setDatabaseSortColumnsOrder(inSessionVObject.databaseColumnsSortOrder);
		}
		if (this.idPage == null) {
		    setIdPage(inSessionVObject.idPage);
		}
		if (this.filterFields == null) {
		    setFilterFields(inSessionVObject.filterFields);
		}
		if (CollectionUtils.isEmpty(this.databaseColumnsLabel )) {
		    setDatabaseColumnsLabel(inSessionVObject.databaseColumnsLabel);
		}
		if (CollectionUtils.isEmpty(this.selectedColumns)) {
		    setSelectedColumns(inSessionVObject.selectedColumns);
		}
		if (CollectionUtils.isEmpty(this.selectedLines )) {
		    setSelectedLines(inSessionVObject.selectedLines);
		}
	    }

	    if (this.idPage == null) {
		setIdPage("1");
	    }

	    // Save the content of the selected line before run an other request
	    Map<String, ArrayList<String>> selectedContent = mapContentSelected();

	    // Save the selected column before run an other request
	    ArrayList<String> selectedHeaders = listHeadersSelected();

	    // Set the max page number
	    setNbPagesFromQuery();

	    // Set the current page. If ther is something in session, use that, else set to
	    // 1
	    setCurrentPageBetweenMinMax();
	    List<ArrayList<String>> aContent = getContentFromDatabase();

	    setColumnDatabaseLabelAndType(aContent);

	    // on set l'objet
	    setIsInitialized(true);

	    setTheGuiElements();

	    setContent(TableObject.as(aContent));
	    setDefaultInputFields(defaultInputFields);
	    resetInputFieldsValues();

	    // Reset download attribute
	    setFileUpload(null);
	    setFileUploadFileName(null);

	    // compute the selected line with the new content
	    computeSelectedLineId(selectedContent);

	    // Compute the new column selection if the column changed
	    setSelectedColumns(//
		    databaseColumnsLabel.stream()//
			    .map(databaseHeader -> selectedHeaders.contains(databaseHeader))//
			    .collect(Collectors.toList()));//

	    // Put the object in session
	    session.setAttribute(this.sessionName, deepClone());

	} catch (Exception ex) {
	    LoggerHelper.error(LOGGER, "initialize()", ex);
	    ex.printStackTrace();
	}

    }

    /**
     * This methode found the new selected line id after any change in the VObject.
     * This make it possible to keep the same selected line avec a sort.
     * 
     * 
     * @param selectedContent
     */
    private void computeSelectedLineId(Map<String, ArrayList<String>> selectedContent) {
	List<Boolean> selectedLinesTemp = new ArrayList<>();
	// If there is selection find the line
	if (!selectedContent.isEmpty()) {
	    int numberOfLineSelected = selectedContent.get(this.databaseColumnsLabel.get(0)).size();

	    // Go throw the content
	    for (int i = 0; i < this.content.size(); i++) {
		int k = 0;
		boolean isSelectedLine = false;

		// While it remain selected content to check and it did not found the line
		while (k < numberOfLineSelected && !isSelectedLine) {
		    isSelectedLine = true;
		    int j = 0;

		    // While no difference is found between the content and the selected content
		    while (j < this.content.get(i).getData().size() && isSelectedLine) {

			isSelectedLine = isCurrentElementEqualsBetweenSelectedContentAndContent(
				selectedContent.get(this.databaseColumnsLabel.get(j)),
				selectedContent.get(this.databaseColumnsLabel.get(j)).get(k),
				this.content.get(i).getData().get(j), isSelectedLine);
			j++;
		    }
		    k++;
		    selectedLinesTemp.add(isSelectedLine);
		}

	    }
	    // no selection -> set all false
	} else {
	    this.content.forEach(c->selectedLinesTemp.add(false));
	}
	setSelectedLines(selectedLinesTemp);
    }

    /**
     * Check if two data are the same
     * 
     * @param currentHeaderList
     * @param currentContent
     * @param selectedContentData
     * @param isSelectedLine
     * @return
     */
    private boolean isCurrentElementEqualsBetweenSelectedContentAndContent(List<String> currentHeaderList,
	    String currentContent, String selectedContentData, boolean isSelectedLine) {
	// Check if the previous column in the content
	if (currentHeaderList != null) {
	    isSelectedLine = ManipString.compareStringWithNull(currentContent, selectedContentData);
	}
	return isSelectedLine;
    }

    /**
     * Set all the gui elements.
     * <ul>
     * <li>{@link VObject#guiColumnsLabel}</li>
     * <li>{@link VObject#guiColumnsSize}</li>
     * <li>{@link VObject#guiColumnsType}</li>
     * <li>{@link VObject#guiSelectedColumns}</li>
     * <li>{@link VObject#visibleHeaders}</li>
     * <li>{@link VObject#updatableHeaders}</li>
     * <li>{@link VObject#requiredHeaders}</li>
     * </ul>
     */
    private void setTheGuiElements() {
	setGuiColumnsLabel(buildGuiColumnsLabel());
	setGuiColumnsSize(buildGuiColumnsSize());
	setGuiColumnsType(buildGuiColumnsType());
	setGuiSelectedColumns(buildGuiSelectedColumns());
	setVisibleHeaders(buildVisibleHeaders());
	setUpdatableHeaders(buildUpdatableHeaders());
	setRequiredHeaders(buildRequiredHeaders());
    }

    /**
     * Set the {@link VObject#databaseColumnsLabel} and
     * {@link VObject#databaseColumnsType} with the passed content
     * 
     * @param aContent
     *            : a table of data with headers on the first line and the type in
     *            the second
     */
    private void setColumnDatabaseLabelAndType(List<ArrayList<String>> aContent) {
	ArrayList<String> theDatabaseColumnsLabel;
	ArrayList<String> theDatabaseColumnsType;
	if (!CollectionUtils.isEmpty(aContent)) {
	    theDatabaseColumnsLabel = aContent.remove(0);
	    theDatabaseColumnsType = aContent.remove(0);
	} else {
	    theDatabaseColumnsLabel = new ArrayList<>();
	    theDatabaseColumnsType = new ArrayList<>();
	}
	setDatabaseColumnsLabel(theDatabaseColumnsLabel);
	setDatabaseColumnsType(theDatabaseColumnsType);
    }

    /**
     * Request the database with the {@link VObject#mainQuery}. Return a
     * {@link List} of {@link ArrayList} with each {@link ArrayList} representing a
     * line of the query result. The first two lines are the headers and the type of
     * the columns.
     * 
     * @return the result of the query in the form of a {@link List} of
     *         {@link ArrayList}.
     */
    private List<ArrayList<String>> getContentFromDatabase() {
	List<ArrayList<String>> aContent = new ArrayList<>();
	Integer indexPage = Integer.parseInt(this.idPage);

	// create the query ro tun
	StringBuilder theQueryToRun = new StringBuilder();
	theQueryToRun.append("select alias_de_table.* from (" + this.mainQuery + ") alias_de_table "
		+ buildFilter(this.filterFields, this.databaseColumnsLabel));

	// do in the gui some column are selected to order the result ?
	if (!this.noOrder) {
	    theQueryToRun.append(buildOrderBy(this.databaseColumnsSortLabel, this.databaseColumnsSortOrder));
	}

	// pagination if needed
	if (this.paginationSize > 0) {
	    theQueryToRun
		    .append(" limit " + this.paginationSize + " offset " + ((indexPage - 1) * this.paginationSize));
	}

	try {
	    aContent = reworkContent(UtilitaireDao.get(this.pool).executeRequest(null, theQueryToRun, ModeRequete.IHM_INDEXED));
	} catch (SQLException ex) {
	    this.message = ex.getMessage();
	    LoggerHelper.error(LOGGER, "initialize()", ex);
	}
	return aContent;
    }

    /**
     * Set the current page number beteween 1 and the max page number
     */
    private void setCurrentPageBetweenMinMax() {

	Integer indexPage = Integer.parseInt(this.idPage);

	if (indexPage > this.nbPages) {
	    setIdPage(this.nbPages.toString());
	}
	if (indexPage < 1) {
	    setIdPage("1");
	}
    }

    /**
     * Excute an SQL query to get the total number of page of this VObject
     */
    @SQLExecutor
    private void setNbPagesFromQuery() {
	if (this.paginationSize > 0 && !this.noOrder) {
	    try {
		List<ArrayList<String>> aContent = UtilitaireDao.get(this.pool).executeRequest(null,
			"select ceil(count(1)::float/" + this.paginationSize + ") from (" + this.mainQuery
				+ ") alias_de_table " + buildFilter(this.filterFields, this.databaseColumnsLabel),
			ModeRequete.IHM_INDEXED);

		// To be sure the nb of pages si > 0
		setNbPages(Math.max(1, Integer.parseInt(aContent.get(2).get(0))));
	    } catch (SQLException ex) {
		this.message = ex.getMessage();
		LoggerHelper.error(LOGGER, "setMaxPagesFromQuery()", ex);
		setNbPages(1);
	    }
	} else {
	    setNbPages(1);
	}
    }

    /**
     * Insert values in the {@link VObject#table}. Those can be user defined or
     * computed by the application. The user defined are in
     * {@link VObject#inputFields} and the computed are passed in parameter
     * {@link AttributeValue}
     * 
     *
     * @param attributeValues
     *            : varargs made of {@link AttributeValue}, which are, basicly, and
     *            pair of {@link String}
     * @return true if the insertion is effective, false otherwise. Check the
     *         {@link VObject#message} to know what happened
     * 
     * @deprecated this method, is deprecated because it go found in session v0. But
     *             it's a bad way to do, because if the vobject is initialize this
     *             is already load
     */
    @SQLExecutor
    public boolean insert(AttributeValue... attributeValues) {
	try {
	    LoggerHelper.trace(LOGGER, "insert()", this.sessionName);
	    Map<String, String> map = new HashMap<>();
	    Arrays.asList(attributeValues).forEach(t -> map.put(t.getFirst().toLowerCase(), t.getSecond()));
	    HttpSession session = ServletActionContext.getRequest().getSession(false);
	    VObject previousVObject = (VObject) session.getAttribute(this.sessionName);
	    // on remet dans inputFields venant du client les valeurs par default
	    // des input field
	    updateInputFiledWithDefaultInputField();

	    // Récupération des colonnes de la table cible
	    ArrayList<String> listeColonneNative = (ArrayList<String>) UtilitaireDao.get(this.pool).getColumns(null,
		    new ArrayList<String>(), this.table);

	    Boolean allNull = true;
	    StringBuilder reqInsert = new StringBuilder();
	    StringBuilder reqValues = new StringBuilder();
	    reqInsert.append("INSERT INTO " + previousVObject.table + " (");
	    reqValues.append("VALUES (");
	    int j = 0;
	    boolean comma = false;
	    for (int i = 0; i < this.inputFields.size(); i++) {
		if (listeColonneNative.contains(previousVObject.databaseColumnsLabel.get(i))) {
		    if (comma) {
			reqInsert.append(",");
			reqValues.append(",");
		    }
		    comma = true;
		    reqInsert.append(previousVObject.databaseColumnsLabel.get(i));
		    String insertValue;
		    if (attributeValues != null && attributeValues.length > j
			    && map.containsKey(previousVObject.databaseColumnsLabel.get(i).toLowerCase())

		    // attributeValues[j].getFirst().equalsIgnoreCase(v0.headersDLabel.get(i))
		    ) {
			insertValue = map.get(previousVObject.databaseColumnsLabel.get(i).toLowerCase())

			// attributeValues[j].getSecond()
			;
			// j++;
		    } else if (this.inputFields.get(i) != null && this.inputFields.get(i).length() > 0) {
			allNull = false;
			insertValue = "'" + this.inputFields.get(i).replace("'", "''") + "'::"
				+ previousVObject.databaseColumnsType.get(i);
		    } else {
			insertValue = "null";
		    }
		    reqValues.append(insertValue);
		}
	    }
	    reqInsert.append(") ");
	    reqValues.append("); ");
	    StringBuilder requete = new StringBuilder();
	    requete.append("BEGIN;");
	    requete.append(reqInsert);
	    requete.append(reqValues);
	    if (StringUtils.isNotEmpty(this.afterInsertQuery)) {
		requete.append("\n" + this.afterInsertQuery + "\n");
	    }
	    requete.append("END;");
	    try {
		if (!allNull) {
		    UtilitaireDao.get(this.pool).executeRequest(null, requete.toString());
		}
	    } catch (SQLException e) {
		setMessage(e.getMessage());
		return false;
	    }
	} catch (Exception ex) {
	    LoggerHelper.errorGenTextAsComment(getClass(), "insert()", LOGGER, ex);
	    setMessage(ex.getMessage());
	    return false;
	}
	return true;
    }

    /**
     * Insert values in the {@link VObject#table}. Those can be user defined or
     * computed by the application. The user defined are in
     * {@link VObject#inputFields} and the computed are passed in parameter
     * {@link AttributeValue}
     * 
     * This method will replace {@link VObject#insert(AttributeValue...)}
     *
     * @param attributeValues
     *            : varargs made of {@link AttributeValue}, which are, basicly, and
     *            pair of {@link String}
     * @return true if the insertion is effective, false otherwise. Check the
     *         {@link VObject#message} to know what happened
     * 
     * 
     */
    @SQLExecutor
    public boolean insertWIP(AttributeValue... attributeValues) {

	// Check if there is a need to insert value
	if (!this.inputFields.stream().allMatch(StringUtils::isEmpty)) {
	    try {
		LoggerHelper.trace(LOGGER, "insert()", this.sessionName);
		Map<String, String> mapOfColumnAndValue = new HashMap<>();

		// Create a map with the
		Arrays.asList(attributeValues)
			.forEach(t -> mapOfColumnAndValue.put(t.getFirst().toLowerCase(), t.getSecond()));

		updateInputFiledWithDefaultInputField();

		// Get the column name of the table (can be different form the one in the
		// Vobject)
		ArrayList<String> listeColonneNative = (ArrayList<String>) UtilitaireDao.get(this.pool).getColumns(null,
			new ArrayList<String>(), this.table);

		StringBuilder reqInsert = new StringBuilder();
		StringBuilder reqValues = new StringBuilder();
		reqInsert.append("INSERT INTO " + this.table + " (");
		reqValues.append("VALUES (");

		appendColumnNamesAndValuesToRequest(mapOfColumnAndValue, listeColonneNative, reqInsert, reqValues);

		reqInsert.append(") ");
		reqValues.append("); ");
		StringBuilder requete = new StringBuilder();
		requete.append("BEGIN;");
		requete.append(reqInsert);
		requete.append(reqValues);
		if (StringUtils.isNotEmpty(this.afterInsertQuery)) {
		    requete.append("\n" + this.afterInsertQuery + "\n");
		}
		requete.append("END;");
		try {
		    UtilitaireDao.get(this.pool).executeRequest(null, requete.toString());
		} catch (SQLException e) {
		    setMessage(e.getMessage());
		    return false;
		}
	    } catch (Exception ex) {
		LoggerHelper.errorGenTextAsComment(getClass(), "insert()", LOGGER, ex);
		setMessage(ex.getMessage());
		return false;
	    }

	}

	return true;
    }

    /**
     * Append to the reqInsert and the reqValue the column name and the values
     * 
     * @param mapOfColumnAndValue
     * @param listeColonneNative
     * @param allNull
     * @param reqInsert
     * @param reqValues
     * @param attributeValues
     * @return
     */
    private void appendColumnNamesAndValuesToRequest(Map<String, String> mapOfColumnAndValue,
	    ArrayList<String> listeColonneNative, StringBuilder reqInsert, StringBuilder reqValues) {
	boolean firstTime = true;
	for (int i = 0; i < this.inputFields.size(); i++) {
	    if (listeColonneNative.contains(this.databaseColumnsLabel.get(i))) {
		if (!firstTime) {
		    reqInsert.append(",");
		    reqValues.append(",");
		}
		firstTime = false;
		reqInsert.append(this.databaseColumnsLabel.get(i));

		reqValues.append(computeInsertValue(mapOfColumnAndValue, i));
	    }
	}
    }

    private String computeInsertValue(Map<String, String> mapOfColumnAndValue, int i) {
	String insertValue;
	if (mapOfColumnAndValue.containsKey(this.databaseColumnsLabel.get(i).toLowerCase())) {
	    insertValue = mapOfColumnAndValue.get(this.databaseColumnsLabel.get(i).toLowerCase());

	} else if (StringUtils.isNotEmpty(this.inputFields.get(i))) {
	    insertValue = "'" + this.inputFields.get(i).replace("'", "''") + "'::" + this.databaseColumnsType.get(i);
	} else {
	    insertValue = "null";
	}
	return insertValue;
    }

    /**
     * Update the {@link VObject#inputFields} with the value ot
     * {@link VObject#defaultInputFields}
     */
    private void updateInputFiledWithDefaultInputField() {
	for (int i = 0; i < this.databaseColumnsLabel.size(); i++) {
	    if (this.defaultInputFields.get(this.databaseColumnsLabel.get(i)) != null) {
		this.inputFields.set(i, this.defaultInputFields.get(this.databaseColumnsLabel.get(i)));
	    }
	}
    }

    /*
     *
     */
    @SQLExecutor
    public void delete(String... tables) {
	LoggerHelper.traceAsComment(LOGGER, "delete()", this.sessionName);
	HttpSession session = ServletActionContext.getRequest().getSession(false);
	VObject v0 = (VObject) session.getAttribute(this.sessionName);

	ArrayList<String> listeColonneNative = (ArrayList<String>) UtilitaireDao.get(this.pool).getColumns(null,
		new ArrayList<String>(), this.table);

	StringBuilder reqDelete = new StringBuilder();
	reqDelete.append("BEGIN; ");
	for (int i = 0; i < this.selectedLines.size(); i++) {
	    if (this.selectedLines.get(i) != null && this.selectedLines.get(i)) {
		if (tables.length == 0) {
		    reqDelete.append("DELETE FROM " + v0.table + " WHERE ");
		} else {
		    reqDelete.append("DELETE FROM " + tables[0] + " WHERE ");
		}

		boolean comma = false;
		for (int j = 0; j < v0.databaseColumnsLabel.size(); j++) {
		    if (listeColonneNative.contains(v0.databaseColumnsLabel.get(j))) {
			if (comma) {
			    reqDelete.append(" AND ");
			}
			comma = true;

			reqDelete.append(v0.databaseColumnsLabel.get(j));

			if (v0.content.get(i).getData().get(j) != null
				&& v0.content.get(i).getData().get(j).length() > 0) {
			    reqDelete.append("='" + v0.content.get(i).getData().get(j).replace("'", "''") + "'::"
				    + v0.databaseColumnsType.get(j));
			} else {
			    reqDelete.append(" is null");
			}
		    }
		}
		reqDelete.append("; ");
	    }
	}
	reqDelete.append("END; ");
	try {
	    UtilitaireDao.get(this.pool).executeRequest(null, "" + reqDelete);
	} catch (SQLException e) {
	    this.message = e.getMessage();
	}
    }

    /**
     * Méthode de suppression spécifique lors de l'action de UPDATE du controle
     */
    @SQLExecutor
    public void deleteForUpdate(String... tables) {
	LoggerHelper.debugAsComment(LOGGER, "deleteBeforeUpdate()", this.sessionName);
	HttpSession session = ServletActionContext.getRequest().getSession(false);
	VObject v0 = (VObject) session.getAttribute(this.sessionName);
	// comparaison des lignes dans la table avant et aprés
	// toBeUpdated contient l'identifiant des lignes à update
	ArrayList<Integer> toBeUpdated = new ArrayList<Integer>();
	for (int i = 0; i < this.content.size(); i++) {
	    int j = 0;
	    boolean equals = true;
	    while (j < this.content.get(i).getData().size() && equals) {
		equals = ManipString.compareStringWithNull(v0.content.get(i).getData().get(j),
			this.content.get(i).getData().get(j));
		j++;
	    }
	    if (!equals) {
		toBeUpdated.add(i);
	    }
	}
	LoggerHelper.traceAsComment(LOGGER, "toBeUpdated : ", toBeUpdated);
	StringBuilder reqDelete = new StringBuilder();
	reqDelete.append("BEGIN; ");
	for (int i = 0; i < v0.content.size(); i++) {
	    if (toBeUpdated.contains(i)) {
		if (tables.length == 0) {
		    reqDelete.append("DELETE FROM " + v0.table + " WHERE ");
		} else {
		    reqDelete.append("DELETE FROM " + tables[0] + " WHERE ");
		}
		for (int j = 0; j < v0.databaseColumnsLabel.size(); j++) {
		    if (j > 0) {
			reqDelete.append(" AND ");
		    }
		    reqDelete.append(v0.databaseColumnsLabel.get(j));
		    if (v0.content.get(i).getData().get(j) != null && v0.content.get(i).getData().get(j).length() > 0) {
			reqDelete.append("='" + v0.content.get(i).getData().get(j).replace("'", "''") + "'::"
				+ v0.databaseColumnsType.get(j));
		    } else {
			reqDelete.append(" is null");
		    }
		}
		reqDelete.append("; ");
	    }
	}
	reqDelete.append("END; ");
	try {
	    UtilitaireDao.get(this.pool).executeRequest(null, "" + reqDelete);
	} catch (SQLException e) {
	    this.message = e.getMessage();
	}
    }

    public List<ArrayList<String>> listSameContentFromPreviousVObject() {
	return listSameContentFromVObject(inSessionVObject); 
    }
    
    public List<ArrayList<String>> listSameContentFromCurrentVObject() {
	return listSameContentFromVObject(this);

    }
    
    public List<ArrayList<String>> listSameContentFromVObject(VObject aVobject) {
	if (inSessionVObject == null) {
	    return new ArrayList<>();
	}
	List<ArrayList<String>> identicalsLines = new ArrayList<>();
	// comparaison des lignes dans la table avant et aprés
	// toBeUpdated contient l'identifiant des lignes à update
	for (int i = 0; i < this.content.size(); i++) {
	    int j = 0;
	    boolean equals = true;
	    while (j < this.content.get(i).getData().size() && equals) {
		equals = ManipString.compareStringWithNull(inSessionVObject.content.get(i).getData().get(j),
			this.content.get(i).getData().get(j));
		j++;
	    }
	    if (!equals) {
		identicalsLines.add(new ArrayList<String>(aVobject.content.get(i).getData()));
	    }
	}
	return identicalsLines;
    }
    
    
    @SQLExecutor
    public void update() {
	LoggerHelper.trace(LOGGER, "update()", this.sessionName);
	HttpSession session = ServletActionContext.getRequest().getSession(false);
	VObject v0 = (VObject) session.getAttribute(this.sessionName);
	// comparaison des lignes dans la table avant et aprés
	// toBeUpdated contient l'identifiant des lignes à update
	ArrayList<Integer> toBeUpdated = new ArrayList<Integer>();

	for (int i = 0; i < this.content.size(); i++) {
	    int j = 0;
	    boolean equals = true;
	    while (j < this.content.get(i).getData().size() && equals) {
		equals = ManipString.compareStringWithNull(v0.content.get(i).getData().get(j),
			this.content.get(i).getData().get(j));
		j++;
	    }
	    if (!equals) {
		toBeUpdated.add(i);
	    }
	}

	ArrayList<String> listeColonneNative = (ArrayList<String>) UtilitaireDao.get(this.pool).getColumns(null,
		new ArrayList<String>(), this.table);

	// Construction de l'update
	StringBuilder reqUpdate = new StringBuilder();
	reqUpdate.append("BEGIN; ");

	for (int i = 0; i < toBeUpdated.size(); i++) {
	    reqUpdate.append("\nUPDATE " + v0.table + " SET ");
	    boolean comma = false;

	    for (int j = 0; j < v0.databaseColumnsLabel.size(); j++) {
		/*
		 * Vérifions en premier lieu que la variable est bien une colonne de la table et
		 * non une variable de vue.
		 */
		if (listeColonneNative.contains(v0.databaseColumnsLabel.get(j))) {
		    if (comma) {
			reqUpdate.append(" ,");
		    }
		    comma = true;

		    if (ManipString.isStringNull(this.content.get(toBeUpdated.get(i)).getData().get(j))) {
			reqUpdate.append(v0.databaseColumnsLabel.get(j) + "=NULL");
		    }

		    // serial
		    else if (v0.databaseColumnsType.get(j).equals("serial")) {
			// Si on a un serial on lui met un type int pour l'insertion
			reqUpdate.append(v0.databaseColumnsLabel.get(j) + "='"
				+ this.content.get(toBeUpdated.get(i)).getData().get(j).replace("'", "''") + "'::int4");

		    } else {
			reqUpdate.append(v0.databaseColumnsLabel.get(j) + "='"
				+ this.content.get(toBeUpdated.get(i)).getData().get(j).replace("'", "''") + "'::"
				+ v0.databaseColumnsType.get(j));
		    }
		}
	    }
	    reqUpdate.append(" WHERE ");

	    comma = false;
	    for (int j = 0; j < v0.databaseColumnsLabel.size(); j++) {
		if (listeColonneNative.contains(v0.databaseColumnsLabel.get(j))) {
		    if (comma) {
			reqUpdate.append(" AND ");
		    }
		    comma = true;

		    if (ManipString.isStringNull(v0.content.get(toBeUpdated.get(i)).getData().get(j))) {
			reqUpdate.append(v0.databaseColumnsLabel.get(j) + " IS NULL");
		    }
		    /*
		     * je me permet de toucher au VObject car je n'arrive pas à gérer
		     * cobnvenablement les objets de type SERIAL
		     */
		    else if (v0.databaseColumnsType.get(j).equals("serial")) {
			// Si on a un serial on lui met un type int pour l'insertion
			reqUpdate.append(v0.databaseColumnsLabel.get(j) + "='"
				+ v0.content.get(toBeUpdated.get(i)).getData().get(j).replace("'", "''") + "'::int4");

		    } else {
			reqUpdate.append(v0.databaseColumnsLabel.get(j) + "='"
				+ v0.content.get(toBeUpdated.get(i)).getData().get(j).replace("'", "''") + "'::"
				+ v0.databaseColumnsType.get(j));
		    }
		}
	    }
	    reqUpdate.append("; ");
	}
	if (v0.afterUpdateQuery != null) {
	    reqUpdate.append("\n" + v0.afterUpdateQuery + "\n");
	}
	reqUpdate.append("END;");
	try {
	    if (toBeUpdated.size() > 0) {
		UtilitaireDao.get(this.pool).executeRequest(null, "" + reqUpdate);
	    }
	    // Si la requete s'est bien déroulée, mettre à jour le content de v0
	    // avec le this.content
	    // ca sert a mettre à jour les checkbox correctement
	    v0.content = this.content;
	    session.setAttribute(this.sessionName, v0);
	} catch (SQLException e) {
	    this.message = e.getMessage();
	}
    }

    public HashMap<String, String> mapHeadersType() {
	HttpSession session = ServletActionContext.getRequest().getSession(false);
	VObject v0 = (VObject) session.getAttribute(this.sessionName);
	if (v0 == null) {
	    return new HashMap<String, String>();
	}
	return new GenericBean(v0.databaseColumnsLabel, v0.databaseColumnsType, null).mapTypes();
    }

    public ArrayList<ArrayList<String>> listInputFields() {
	ArrayList<ArrayList<String>> r = new ArrayList<ArrayList<String>>();
	r.add(this.inputFields);
	return r;
    }

    public HashMap<String, ArrayList<String>> mapInputFields() {
	HttpSession session = ServletActionContext.getRequest().getSession(false);
	VObject v0 = (VObject) session.getAttribute(this.sessionName);
	return new GenericBean(v0.databaseColumnsLabel, v0.databaseColumnsType, listInputFields()).mapContent();
    }

    public ArrayList<ArrayList<String>> listLineContent(int i) {
	ArrayList<ArrayList<String>> r = new ArrayList<ArrayList<String>>();
	r.add(new ArrayList<String>(this.content.get(i).getData()));
	return r;
    }

    public HashMap<String, ArrayList<String>> mapLineContent(int i) {
	HttpSession session = ServletActionContext.getRequest().getSession(false);
	VObject v0 = (VObject) session.getAttribute(this.sessionName);
	return new GenericBean(v0.databaseColumnsLabel, v0.databaseColumnsType, listLineContent(i)).mapContent();
    }

    public HashMap<String, ArrayList<String>> mapContentBeforeUpdate(int i) {
	HttpSession session = ServletActionContext.getRequest().getSession(false);
	VObject v0 = (VObject) session.getAttribute(this.sessionName);
	ArrayList<ArrayList<String>> r = new ArrayList<ArrayList<String>>();
	r.add(listSameContentFromPreviousVObject().get(i));
	return new GenericBean(v0.databaseColumnsLabel, v0.databaseColumnsType, r).mapContent();
    }

    public ArrayList<ArrayList<String>> listContentSelected() {
	if (this.inSessionVObject == null) {
	    return new ArrayList<>();
	}
	ArrayList<ArrayList<String>> r = new ArrayList<>();
	// on récupère les lignes selectionnées
	// soit de this.selectedLines (du formulaire), soit de v0.selectedLines
	if (this.selectedLines == null) {
	    this.selectedLines = this.inSessionVObject.selectedLines;
	}
	// si rien dans la liste, return null
	if (CollectionUtils.isEmpty(this.selectedLines)) {
	    return r;
	}
	for (int j = 0; j < this.selectedLines.size(); j++) {
	    if (this.selectedLines.get(j) != null && this.selectedLines.get(j)) {
		r.add(new ArrayList<String>(this.inSessionVObject.content.get(j).getData()));
	    }
	}
	if (r.isEmpty()) {
	    return new ArrayList<>();
	}
	return r;
    }

    /*
     * Retourne une hash map qui pour chaque entete de colonne (clé), donne la liste
     * de toutes les valeurs selectionnées
     */
    public Map<String, ArrayList<String>> mapContentSelected() {
	// on récupère les lignes selectionnées
	// soit de this.selectedLines (du formulaire), soit de v0.selectedLines

	return new GenericBean(databaseColumnsLabel, databaseColumnsType, listContentSelected()).mapContent();
    }

    public ArrayList<ArrayList<String>> listContent() {
	HttpSession session = ServletActionContext.getRequest().getSession(false);
	VObject v0 = (VObject) session.getAttribute(this.sessionName);
	if (v0 == null) {
	    return new ArrayList<ArrayList<String>>();
	}
	ArrayList<ArrayList<String>> c = new ArrayList<ArrayList<String>>();
	for (int i = 0; i < v0.content.size(); i++) {
	    ArrayList<String> l = new ArrayList<String>();
	    for (int j = 0; j < v0.content.get(i).getData().size(); j++) {
		l.add(v0.content.get(i).getData().get(j));
	    }
	    c.add(l);
	}
	return c;
    }

    /*
     * Retourne une hash map qui pour chaque entete de colonne (clé), donne la liste
     * de toutes les valeurs selectionnées
     */
    public HashMap<String, ArrayList<String>> mapContent() {
	HttpSession session = ServletActionContext.getRequest().getSession(false);
	VObject v0 = (VObject) session.getAttribute(this.sessionName);
	if (v0 == null) {
	    return new HashMap<String, ArrayList<String>>();
	}
	return new GenericBean(v0.databaseColumnsLabel, v0.databaseColumnsType, listContent()).mapContent();
    }

    public HashMap<String, ArrayList<String>> mapFilterFields() {
	if (getFilterFields() == null) {
	    return new HashMap<String, ArrayList<String>>();
	}

	ArrayList<ArrayList<String>> r = new ArrayList<ArrayList<String>>();
	r.add(new ArrayList<>(getFilterFields()));
	return new GenericBean(this.databaseColumnsLabel, this.databaseColumnsType, r).mapContent();
    }

    /*
     * Retourne la liste des entetes base de donnée selectionnés
     */
    public ArrayList<String> listHeadersSelected() {
	HttpSession session = ServletActionContext.getRequest().getSession(false);
	VObject v0 = (VObject) session.getAttribute(this.sessionName);
	if (v0 == null) {
	    return new ArrayList<String>();
	}
	ArrayList<String> r = new ArrayList<String>();
	// on récupère les lignes selectionnées
	// soit de this.selectedLines (du formulaire), soit de v0.selectedLines
	if (this.selectedColumns == null) {
	    this.selectedColumns = v0.selectedColumns;
	}
	// si rien dans la liste, return null
	if (this.selectedColumns == null || this.selectedColumns.size() == 0) {
	    return r;
	}
	// pour chaque colonne, on va mettre le contenu selectionné dans une
	// arraylist
	// et ajouter cette arraylist dans la hash map avec pour clé le nom de
	// la colonne
	for (int i = 0; i < this.selectedColumns.size(); i++) {
	    if (this.selectedColumns.get(i) != null && this.selectedColumns.get(i)) {
		r.add(v0.databaseColumnsLabel.get(i));
	    }
	}
	if (r.size() == 0) {
	    return new ArrayList<String>();
	}
	return r;
    }

    public HashMap<String, String> mapHeadersSelected() {
	HashMap<String, String> r = new HashMap<String, String>();
	for (String s : listHeadersSelected()) {
	    r.put(s, s);
	}
	return r;
    }

    public StringBuilder queryView() {
	HttpSession session = ServletActionContext.getRequest().getSession(false);
	VObject v0 = (VObject) session.getAttribute(this.sessionName);
	if (this.filterFields == null) {
	    this.filterFields = v0.filterFields;
	}
	StringBuilder requete = new StringBuilder();
	requete.append("select alias_de_table.* from (" + v0.mainQuery + ") alias_de_table ");
	requete.append(buildFilter(this.filterFields, v0.databaseColumnsLabel));
	// requete.append(buildOrderBy(v0.headerSortDLabels,
	// v0.headerSortDOrders));
	return requete;
    }

    /**
     * Renvoie le contenu d'une vue sous la forme d'une HashMap(nom de colonne,
     * liste de valeur)
     *
     * @return
     */
    public HashMap<String, ArrayList<String>> mapView() {
	HashMap<String, ArrayList<String>> result = new HashMap<>();
	try {
	    GenericBean g = new GenericBean(UtilitaireDao.get(this.pool).executeRequest(null, queryView()));
	    result = g.mapContent();
	} catch (SQLException ex) {
	    LoggerHelper.errorGenTextAsComment(getClass(), "mapView()", LOGGER, ex);
	}
	return result;
    }

    public void destroy() {
	this.getCustomValues().clear();
	this.getMapCustomValues().clear();

	HttpSession session = ServletActionContext.getRequest().getSession(false);
	session.removeAttribute(this.sessionName);
	setIsInitialized(false);
    }

    public String buildFilter(List<String> filterFields, List<String> headersDLabel) {

	Pattern patternMath = Pattern.compile("[<>=]");

	StringBuilder s = new StringBuilder(" WHERE true ");
	if (headersDLabel == null || filterFields == null) {
	    return s.toString();
	}

	// boolean first=false;
	for (int i = 0; i < filterFields.size(); i++) {
	    if (filterFields.get(i) != null && !filterFields.get(i).equals("")) {

		if ((this.filterPattern == 0 || this.filterPattern == 1 || this.filterPattern == 2)) {
		    s.append(" AND (");
		}

		// if (!first)
		// {
		// s.append(" WHERE ");
		// }

		/*
		 * Si on a un symbole mathématique
		 */
		Matcher matcher = patternMath.matcher(filterFields.get(i));
		if (matcher.find()) {
		    // On a au moins une fonction mathématique 2 cas, soit numérique, soit date. On
		    // sait que l'on a une date si
		    // le filtre contient §

		    if (filterFields.get(i).contains("§")) { // on a une date donc filtre du type format§condition
			String filtre = filterFields.get(i);
			String[] morceauReq = filtre.split("§");

			// on découpe suivant les ET
			String[] listeAND = morceauReq[1].split("ET");

			for (String conditionAND : listeAND) {
			    // on découpe suivant les OU
			    String[] listeOR = conditionAND.split("OU");
			    for (String condtionOR : listeOR) {

				condtionOR = condtionOR.trim().substring(0, 1) + "'" + condtionOR.trim().substring(1)
					+ "'";
				s.append(
					" to_date(" + headersDLabel.get(i) + ", '" + morceauReq[0] + "')" + condtionOR);

				s.append(" OR");
			    }
			    // on retire les dernier OR
			    s.setLength(s.length() - 3);
			    s.append(" AND");
			}

		    } else { // on a des nombres
			// on découpe suivant les ET
			String[] listeAND = filterFields.get(i).split("ET");

			for (String conditionAND : listeAND) {
			    // on découpe suivant les OU
			    String[] listeOR = conditionAND.split("OU");
			    for (String condtionOR : listeOR) {
				if (condtionOR.contains("[")) { // cas ou on va chercher dans un vecteur

				    condtionOR = condtionOR.trim();

				    String colonne = condtionOR.substring(0, 1) + "array_position("
					    + headersDLabel.get(i - 1) + ",'"
					    + condtionOR.substring(1, condtionOR.indexOf("]")) + "')" + condtionOR
						    .substring(condtionOR.indexOf("]"), condtionOR.indexOf("]") + 1); // on
														      // prend
														      // le
														      // [X]

				    condtionOR = condtionOR.substring(condtionOR.indexOf("]") + 1);

				    s.append(" (" + headersDLabel.get(i) + colonne + "::bigint)" + condtionOR);

				} else {
				    s.append(" (" + headersDLabel.get(i) + "::bigint)" + condtionOR);

				}
				s.append(" OR");
			    }
			    // on retire les dernier OR
			    s.setLength(s.length() - 3);
			    s.append(" AND");
			}
		    }

		    // on retire le dernier AND
		    s.setLength(s.length() - 4);

		} else {

		    // if (first && (filterPattern == 2))
		    // {
		    // s.append(" OR ");
		    // }

		    if (this.filterPattern == 0 || this.filterPattern == 1) {
			s.append(" " + this.filterFunction + "(" + headersDLabel.get(i) + "::text) LIKE '");
		    }

		    if (this.filterPattern == 2) {
			s.append(" ' '||" + this.filterFunction + "(" + headersDLabel.get(i) + "::text) SIMILAR TO '");
		    }

		    // Si on a déjà un % dans le filtre on n'en rajoute pas
		    if ((this.filterPattern == 0 || this.filterPattern == 2) && !filterFields.get(i).contains("%")) {
			s.append("%");
		    }

		    if (this.filterPattern == 0 || this.filterPattern == 1) {
			s.append(filterFields.get(i).toUpperCase());
		    }

		    if (this.filterPattern == 2) {
			String aChercher = patternMather(filterFields.get(i).toUpperCase().trim());
			s.append("( " + aChercher.replace(" ", "| ") + ")%");
		    }

		    if ((this.filterPattern == 0 || this.filterPattern == 1) && !filterFields.get(i).contains("%")) {
			s.append("%");
		    }
		    s.append("'");
		}
		s.append(") ");
	    }
	}
	s.append(" ");
	return s.toString();
    }

    public String patternMather(String aChercher) {
	String returned = aChercher;
	returned = ManipString.translateAscii(returned).replace(" ", "  ") + " ";
	// enlever tout les mots de moins de 2 lettres
	returned = returned.replaceAll(" [^ ][^ ] ", " ");
	returned = returned.replaceAll(" [^ ] ", " ");
	returned = returned.replaceAll("[ ]+", " ");
	returned = returned.trim();
	return returned;
    }

    /**
     * Créer la clause order by
     *
     * @param headerSortLabels
     * @param headerSortDOrders
     * @return
     */
    public String buildOrderBy(ArrayList<String> headerSortLabels, ArrayList<Boolean> headerSortDOrders) {
	StringBuilder s = new StringBuilder();
	if (CollectionUtils.isEmpty(headerSortLabels)) {
	    return "order by alias_de_table ";
	}
	for (int i = 0; i < headerSortLabels.size(); i++) {
	    if (i > 0) {
		s.append(",");
	    } else {
		s.append(" ORDER BY ");
	    }
	    s.append(headerSortLabels.get(i) + " ");
	    if (!headerSortDOrders.get(i)) {
		s.append("desc ");
	    }
	}
	s.append(", alias_de_table ");
	return s.toString();
    }

    /*
     * Trier suivant une colonne
     */
    public void sort() {
	LoggerHelper.debugAsComment(LOGGER, "sort()");

	
	System.out.print("*** SORT ***");
	
	HttpSession session = ServletActionContext.getRequest().getSession(false);
	VObject v0 = (VObject) session.getAttribute(this.sessionName);
	
	System.out.print(v0.databaseColumnsLabel);
	System.out.print(this.databaseColumnSort);
	
	if (v0.databaseColumnsLabel.indexOf(this.databaseColumnSort) != -1) {
		
		System.out.print("***"+this.databaseColumnsSortLabel);
		System.out.print("***"+this.databaseColumnsSortOrder);
		
	    this.databaseColumnsSortLabel = v0.databaseColumnsSortLabel;
	    this.databaseColumnsSortOrder = v0.databaseColumnsSortOrder;
	    // on initialize si la liste n'existe pas
	    if (this.databaseColumnsSortLabel == null) {
		this.databaseColumnsSortLabel = new ArrayList<String>();
		this.databaseColumnsSortOrder = new ArrayList<Boolean>();
	    }
	    int pos = this.databaseColumnsSortLabel.indexOf(this.databaseColumnSort);
	    // si le champ a sort est en premiere position, on va inverser le
	    // sens de l'order by
	    if (pos == 0) {
		this.databaseColumnsSortOrder.set(0, !this.databaseColumnsSortOrder.get(0));
	    }
	    // si le champ est inconnu, on le met en premiere position avec un
	    // sens asc
	    else if (pos == -1) {
		this.databaseColumnsSortLabel.add(0, this.databaseColumnSort);
		this.databaseColumnsSortOrder.add(0, true);
	    }
	    // sinon on l'enleve de la liste existante et on le remet en
	    // premiere position avec un sens inverse a celui d'avant
	    else {
		this.databaseColumnsSortLabel.remove(pos);
		this.databaseColumnsSortOrder.remove(pos);
		this.databaseColumnsSortLabel.add(0, this.databaseColumnSort);
		this.databaseColumnsSortOrder.add(0, true);
	    }
	}
    }

    public void download() {
	HttpSession session = ServletActionContext.getRequest().getSession(false);
	VObject v0 = (VObject) session.getAttribute(this.sessionName);
	if (this.filterFields == null) {
	    this.filterFields = v0.filterFields;
	}
	String requete = "select alias_de_table.* from (" + v0.mainQuery + ") alias_de_table "
		+ buildFilter(this.filterFields, v0.databaseColumnsLabel)
		+ buildOrderBy(v0.databaseColumnsSortLabel, v0.databaseColumnsSortOrder);
	ArrayList<String> fileNames = new ArrayList<String>();
	fileNames.add("Vue");
	this.download(fileNames, requete);
    }

    public void download(List<String> fileNames, List<String> requetes) {
	download(fileNames, requetes.toArray(new String[0]));

    }

    /**
     * Téléchargement dans un zip de N fichiers csv, les données étant extraites de
     * la base de données
     *
     * @param fileNames
     *            , liste des noms de fichiers obtenus
     * @param requetes
     *            , liste des requetes SQL
     */
    public void download(List<String> fileNames, String... requetes) {
	HttpSession session = ServletActionContext.getRequest().getSession(false);
	VObject v0 = (VObject) session.getAttribute(this.sessionName);
	if (this.filterFields == null) {
	    this.filterFields = v0.filterFields;
	}
	Date dNow = new Date();
	SimpleDateFormat ft = new SimpleDateFormat("yyyyMMddHHmmss");
	HttpServletResponse response = ServletActionContext.getResponse();
	response.reset();
	response.setHeader("Content-Disposition",
		"attachment; filename=" + v0.sessionName + "_" + ft.format(dNow) + ".csv.zip");
	try {
	    // Rattachement du zip à la réponse de Struts2
	    ZipOutputStream zos = new ZipOutputStream(response.getOutputStream());
	    try {
		for (int i = 0; i < requetes.length; i++) {
		    // Le nom des fichiers à l'interieur du zip seront simple :
		    // fichier1.csv, fichier2.csv etc.
		    // Ajout d'un nouveau fichier
		    ZipEntry entry = new ZipEntry(fileNames.get(i) + ".csv");
		    zos.putNextEntry(entry);
		    // Ecriture dans le fichier
		    UtilitaireDao.get(this.pool).outStreamRequeteSelect(null, requetes[i], zos);
		    zos.closeEntry();
		}
	    } finally {
		zos.close();
	    }
	} catch (IOException | SQLException ex) {
	    LoggerHelper.errorGenTextAsComment(getClass(), "download()", LOGGER, ex);
	} finally {
	    try {
		response.getOutputStream().flush();
		response.getOutputStream().close();
	    } catch (IOException ex) {
		LoggerHelper.errorGenTextAsComment(getClass(), "download()", LOGGER, ex);
	    }
	}
    }

    /**
     * Téléchargement d'une liste de fichier qui sont stockés dans le dossier
     * RECEPTION_OK ou RECEPTION_KO
     *
     * @param phase
     *
     * @param etatOk
     *
     * @param etatKo
     *
     * @param listIdSource
     */
    public void downloadXML(String requete, String repertoire, String anEnvExcecution, String phase, String etatOk,
	    String etatKo) {
	HttpSession session = ServletActionContext.getRequest().getSession(false);
	VObject v0 = (VObject) session.getAttribute(this.sessionName);
	if (this.filterFields == null) {
	    this.filterFields = v0.filterFields;
	}
	Date dNow = new Date();
	SimpleDateFormat ft = new SimpleDateFormat("yyyyMMddHHmmss");
	HttpServletResponse response = ServletActionContext.getResponse();
	response.reset();
	response.setHeader("Content-Disposition",
		"attachment; filename=" + v0.sessionName + "_" + ft.format(dNow) + ".tar.gz");
	// Rattachement du zip à la réponse de Struts2
	TarArchiveOutputStream taos = null;
	try {
	    taos = new TarArchiveOutputStream(new GZIPOutputStream(response.getOutputStream()));
	    UtilitaireDao.get(this.pool).zipOutStreamRequeteSelect(null, requete, taos, repertoire, anEnvExcecution,
		    phase, "ARCHIVE");
	} catch (IOException ex) {
	    LoggerHelper.errorGenTextAsComment(getClass(), "downloadXML()", LOGGER, ex);
	} finally {
	    try {
		if (taos != null) {
		    try {
			taos.close();
		    } catch (IOException ioe) {
			// Silent catch
		    }
		}
		response.getOutputStream().flush();
		response.getOutputStream().close();
	    } catch (IOException ex) {
		LoggerHelper.errorGenTextAsComment(getClass(), "downloadXML()", LOGGER, ex);
	    }
	}
    }

    // 4MB buffer
    private static final byte[] BUFFER = new byte[4096 * 1024];

    /**
     * copy input to output stream - available in several StreamUtils or Streams
     * classes
     */
    public static void copy(InputStream input, OutputStream output) throws IOException {
	int bytesRead;
	while ((bytesRead = input.read(BUFFER)) != -1) {
	    output.write(BUFFER, 0, bytesRead);
	}
    }

    /**
     * Télécharger en tar gzip une liste de fichier
     *
     * @param requete
     *            la selection de fichier avec la clé pertinente qui doit d'appeler
     *            nom_fichier
     * @param repertoire
     *            chemin jusqu'à l'avant dernier dossier
     * @param listRepertoire
     *            noms du dernier dossier (chaque fichier pouvant être dans l'un de
     *            la liste)
     */
    public void downloadEnveloppe(String requete, String repertoire, ArrayList<String> listRepertoire) {
	HttpSession session = ServletActionContext.getRequest().getSession(false);
	VObject v0 = (VObject) session.getAttribute(this.sessionName);

	if (this.filterFields == null) {
	    this.filterFields = v0.filterFields;
	}

	Date dNow = new Date();
	SimpleDateFormat ft = new SimpleDateFormat("yyyyMMddHHmmss");
	HttpServletResponse response = ServletActionContext.getResponse();
	response.reset();
	response.setHeader("Content-Disposition",
		"attachment; filename=" + v0.sessionName + "_" + ft.format(dNow) + ".tar");

	TarArchiveOutputStream taos = null;
	try {
	    taos = new TarArchiveOutputStream(response.getOutputStream());
	    UtilitaireDao.get(this.pool).copieFichiers(null, requete, taos, repertoire, listRepertoire);

	} catch (IOException ex) {
	    LoggerHelper.errorGenTextAsComment(getClass(), "downloadEnveloppe()", LOGGER, ex);
	} finally {

	    try {
		if (taos != null) {
		    try {
			taos.close();
		    } catch (IOException ioe) {
			// Silent catch
		    }
		}
		response.getOutputStream().flush();
		response.getOutputStream().close();

	    } catch (IOException ex) {
		LoggerHelper.errorGenTextAsComment(getClass(), "downloadEnveloppe()", LOGGER, ex);
	    }
	}
    }

    public ArrayList<String> upload(String repertoireCible) {
	if (this.fileUploadFileName != null) {
	    for (int i = 0; i < this.fileUpload.size(); i++) {
		String location = repertoireCible + File.separator + this.fileUploadFileName.get(i);
		// LoggerDispatcher.info("Repertoire Cible "+repertoireCible,logger);
		// LoggerDispatcher.info("Existance ? "+newFile2.exists(),logger);
		// LoggerDispatcher.info("Droit d'écriture "+newFile2.canWrite(),logger);
		LoggerDispatcher.info("Upload >> " + location, LOGGER);
		File newFile = new File(location);
		if (newFile.exists()) {
		    newFile.delete();
		}
		try {
		    FileUtils.copyFile(this.fileUpload.get(i), newFile);
		} catch (IOException ex) {
		    LoggerHelper.errorGenTextAsComment(getClass(), "upload()", LOGGER, ex);
		}
		// LoggerDispatcher.info("Fichier copié ? "+newFile.exists(),logger);
	    }
	}
	this.message = "Upload terminé.";
	return this.fileUploadFileName;
    }

    /**
     *
     *
     * @return le nombre de lignes de ceci
     */
    public int getNombreLigne() {
	return this.content.size();
    }

    /**
     *
     *
     * @return {@code true} si aucune ligne dans {@code this}
     */
    public boolean isEmpty() {
	return this.getNombreLigne() <= 0;
    }

    public ArrayList<String> getV(int j, TableObject content) {
	ArrayList<String> h = new ArrayList<>();
	for (int i = 0; i < content.getLines().size(); i++) {
	    h.add(content.getLines().get(i).getData().get(j));
	}
	return h;
    }

    /**
     * Calcule si la vue est active ou pas si le parametre d'activation est a null,
     * on le met à faux si le parametre scope est null, on ne fait rien si on ne
     * retrouve pas le nom de la vue dans le scope, on ne fait rien si le nom de la
     * vue est retrouvée dans le scope avec un moins devant, on desactive si le nom
     * de la vue est retrouvée dans le scope sans avoir un moins devant, on active
     *
     * @param scope
     */
    public void setActivation(String scope) {
	Boolean t = true;

	if (this.getIsActive() == null) {
	    this.setIsActive(false);
	}

	if (this.getIsScoped() == null) {
	    this.setIsScoped(false);
	}

	if (t && scope != null && scope.contains("-" + this.sessionName + ";")) {
	    this.setIsActive(false);
	    t = false;
	}

	if (t && scope != null && scope.contains(this.sessionName + ";")) {
	    this.setIsActive(true);
	    t = false;
	}

	if (this.getIsActive() && (scope == null || scope.contains(this.sessionName + ";"))) {
	    this.setIsScoped(true);
	} else {
	    this.setIsScoped(false);
	}

    }

    public HashMap<String, ArrayList<String>> mapSameContentFromPreviousVObject() {
	return new GenericBean(this.databaseColumnsLabel, this.databaseColumnsType,
		new ArrayList<ArrayList<String>>(listSameContentFromPreviousVObject())).mapContent();
    }
    
    public HashMap<String, ArrayList<String>> mapSameContentFromCurrentVObject() {

	return new GenericBean(this.databaseColumnsLabel, this.databaseColumnsType,
		new ArrayList<ArrayList<String>>(listSameContentFromCurrentVObject())).mapContent();
    }
    
    public HashMap<String, ArrayList<String>> mapSameContentFromPreviousVObject(int indexLine) {
	ArrayList<ArrayList<String>> table = new ArrayList<ArrayList<String>>();
	table.add(listSameContentFromCurrentVObject().get(indexLine));
	return new GenericBean(this.databaseColumnsLabel, this.databaseColumnsType,
		table).mapContent();
    }
    
    public HashMap<String, ArrayList<String>> mapSameContentFromCurrentVObject(int indexLine) {
	ArrayList<ArrayList<String>> table = new ArrayList<ArrayList<String>>();
	table.add(listSameContentFromCurrentVObject().get(indexLine));
	return new GenericBean(this.databaseColumnsLabel, this.databaseColumnsType,
		table).mapContent();
    }

    @SuppressWarnings("unchecked")
    public void initializeByList(ArrayList<ArrayList<String>> liste, HashMap<String, String> defaultInputFields) {
	StringBuilder requete = new StringBuilder();
	ArrayList<String> header = liste.get(0);
	ArrayList<String> type = liste.get(1);

	// cas classique avec des données
	for (int i = 2; i < liste.size(); i++) {
	    if (i > 2) {
		requete.append("\n UNION ALL ");
	    }
	    requete.append("SELECT ");

	    for (int j = 0; j < liste.get(i).size(); j++) {
		if (j > 0) {
		    requete.append(",");
		}
		requete.append(
			"'" + liste.get(i).get(j).replace("'", "''") + "'::" + type.get(j) + " as " + header.get(j));
	    }
	}

	// cas sans données avec juste les headers et le type
	if (liste.size() == 2) {
	    requete.append("SELECT ");

	    for (int j = 0; j < liste.get(0).size(); j++) {
		if (j > 0) {
		    requete.append(",");
		}
		requete.append("null::" + type.get(j) + " as " + header.get(j) + " ");
	    }
	    requete.append("WHERE false ");

	}
	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug(" initializeByList requete : " + requete.toString());
	}
	// on ne gere pas les autres cas: ca doit planter
	this.initialize(requete.toString(), this.table, defaultInputFields);
    }

    public void setFilterPattern(int filterPattern) {
	this.filterPattern = filterPattern;
    }

    public String getFilterFunction() {
	return this.filterFunction;
    }

    public void setFilterFunction(String filterFunction) {
	this.filterFunction = filterFunction;
    }

    public Boolean getNoOrder() {
	return this.noOrder;
    }

    public void setNoOrder(Boolean noPage) {
	this.noOrder = noPage;
    }

    /**
     * Determine si un filtre existe
     *
     * @return
     */
    public Boolean filterExists() {
	boolean filterExist = false;
	if (this.filterFields != null && !this.filterFields.isEmpty()) {
	    for (int i = 0; i < this.filterFields.size(); i++) {
		if (this.filterFields.get(i) != null && !this.filterFields.get(i).equals("")) {
		    filterExist = true;
		    break;
		}

	    }
	}
	return filterExist;
    }

    /**
     * Renvoie le numéro d'une colonne
     * 
     * @param aNomCol
     * @return
     */
    public int getNumeroDLabel(String aNomCol) {
	LoggerHelper.debug(LOGGER, "aNomCol", aNomCol);
	for (int i = 0; i < this.databaseColumnsLabel.size(); i++) {
	    LoggerHelper.debug(LOGGER, "this.headersDLabel.get(i)", this.databaseColumnsLabel.get(i));
	    if (this.databaseColumnsLabel.get(i).equals(aNomCol)) {
		return i;
	    }
	}
	return -1;
    }


    public ArrayList<ArrayList<String>> reworkContent(ArrayList<ArrayList<String>> content) {
	return content;
    }
    
    public String getPool() {
	return pool;
    }

    public void setPool(String pool) {
	this.pool = pool;
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

    public Integer getPaginationSize() {
	return paginationSize;
    }

    public void setPaginationSize(Integer paginationSize) {
	this.paginationSize = paginationSize;
    }

    public Boolean getIsInitialized() {
	return isInitialized;
    }

    public void setIsInitialized(Boolean isInitialized) {
	this.isInitialized = isInitialized;
    }

    public Boolean getIsActive() {
	return isActive;
    }

    public void setIsActive(Boolean isActive) {
	this.isActive = isActive;
    }

    public Boolean getIsScoped() {
	return isScoped;
    }

    public void setIsScoped(Boolean isScoped) {
	this.isScoped = isScoped;
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

    public List<String> getDatabaseColumnsLabel() {
	return databaseColumnsLabel;
    }

    public void setDatabaseColumnsLabel(List<String> databaseColumnsLabel) {

	if (CollectionUtils.isEmpty(databaseColumnsLabel)) {
	    this.databaseColumnsLabel = new ArrayList<>();
	} else {
	    this.databaseColumnsLabel = new ArrayList<>(databaseColumnsLabel);
	}
    }

    public List<String> getDatabaseColumnsType() {
	return databaseColumnsType;
    }

    public void setDatabaseColumnsType(List<String> databaseColumnsType) {
	if (CollectionUtils.isEmpty(databaseColumnsType)) {
	    this.databaseColumnsType = new ArrayList<>();
	} else {
	    this.databaseColumnsType = new ArrayList<>(databaseColumnsType);
	}
    }

    public List<String> getGuiColumnsLabel() {
	return guiColumnsLabel;
    }

    public void setGuiColumnsLabel(List<String> guiColumnsLabel) {
	if (CollectionUtils.isEmpty(guiColumnsLabel)) {
	    this.guiColumnsLabel = new ArrayList<>();
	} else {
	    this.guiColumnsLabel = new ArrayList<>(guiColumnsLabel);
	}
    }

    public List<String> getGuiColumnsSize() {
	return guiColumnsSize;
    }

    public void setGuiColumnsSize(List<String> guiColumnsSize) {
	if (CollectionUtils.isEmpty(guiColumnsSize)) {
	    this.guiColumnsSize = new ArrayList<>();
	} else {
	    this.guiColumnsSize = new ArrayList<>(guiColumnsSize);
	}
    }

    public List<String> getGuiColumnsType() {
	return guiColumnsType;
    }

    public void setGuiColumnsType(List<String> guiColumnsType) {
	if (CollectionUtils.isEmpty(guiColumnsType)) {
	    this.guiColumnsType = new ArrayList<>();
	} else {
	    this.guiColumnsType = new ArrayList<>(guiColumnsType);
	}
    }

    public List<LinkedHashMap<String, String>> getGuiSelectedColumns() {
	return guiSelectedColumns;
    }

    public void setGuiSelectedColumns(List<LinkedHashMap<String, String>> guiSelectedColumns) {
	if (CollectionUtils.isEmpty(guiSelectedColumns)) {
	    this.guiSelectedColumns = new ArrayList<>();
	} else {
	    this.guiSelectedColumns = new ArrayList<>(guiSelectedColumns);
	}
    }

    public List<Boolean> getVisibleHeaders() {
	return visibleHeaders;
    }

    public void setVisibleHeaders(List<Boolean> visibleHeaders) {
	if (CollectionUtils.isEmpty(visibleHeaders)) {
	    this.visibleHeaders = new ArrayList<>();
	} else {
	    this.visibleHeaders = new ArrayList<>(visibleHeaders);
	}
    }

    public List<Boolean> getUpdatableHeaders() {
	return updatableHeaders;
    }

    public void setUpdatableHeaders(List<Boolean> updatableHeaders) {
	if (CollectionUtils.isEmpty(updatableHeaders)) {
	    this.updatableHeaders = new ArrayList<>();
	} else {
	    this.updatableHeaders = new ArrayList<>(updatableHeaders);
	}
    }

    public List<Boolean> getRequiredHeaders() {
	return requiredHeaders;
    }

    public void setRequiredHeaders(List<Boolean> requiredHeaders) {
	if (CollectionUtils.isEmpty(databaseColumnsSortLabel)) {
	    this.requiredHeaders = new ArrayList<>();
	} else {
	    this.requiredHeaders = new ArrayList<>(requiredHeaders);
	}
    }

    public List<String> getDatabaseColumnsSort() {
	return databaseColumnsSortLabel;
    }

    public void setDatabaseColumnsSort(List<String> databaseColumnsSort) {
	if (CollectionUtils.isEmpty(databaseColumnsSort)) {
	    this.databaseColumnsSortLabel = new ArrayList<>();
	} else {
	    this.databaseColumnsSortLabel = new ArrayList<>(databaseColumnsSort);
	}
    }

    public List<Boolean> getDatabaseSortColumnsOrder() {
	return databaseColumnsSortOrder;
    }

    public void setDatabaseSortColumnsOrder(List<Boolean> databaseSortColumnsOrder) {
	if (CollectionUtils.isEmpty(databaseColumnsSortLabel)) {
	    this.databaseColumnsSortOrder = new ArrayList<>();
	} else {
	    this.databaseColumnsSortOrder = new ArrayList<>(databaseSortColumnsOrder);
	}
    }

    public String getHeaderSortDLabel() {
	return databaseColumnSort;
    }

    public void setHeaderSortDLabel(String headerSortDLabel) {
	this.databaseColumnSort = headerSortDLabel;
    }

    public List<Boolean> getSelectedLines() {
	return selectedLines;
    }

    public void setSelectedLines(List<Boolean> selectedLines) {
	LoggerHelper.debug(LOGGER, "selectedLines :", selectedLines);
	if (CollectionUtils.isEmpty(selectedLines)) {
	    this.selectedLines = new ArrayList<>();
	} else {
	    this.selectedLines = new ArrayList<>(selectedLines);
	}
    }

    public List<Boolean> getSelectedColumns() {
	return selectedColumns;
    }

    public void setSelectedColumns(List<Boolean> selectedColumns) {
	if (CollectionUtils.isEmpty(selectedColumns)) {
	    this.selectedColumns = new ArrayList<>();
	} else {
	    this.selectedColumns = new ArrayList<>(selectedColumns);
	}
    }

    public Map<String, String> getDefaultInputFields() {
	return defaultInputFields;
    }

    public void setDefaultInputFields(Map<String, String> defaultInputFields) {
	if (defaultInputFields.isEmpty()) {
	    this.defaultInputFields = new HashMap<>();
	} else {
	    this.defaultInputFields = new HashMap<>(defaultInputFields);
	}
    }

    public List<String> getInputFields() {
	return inputFields;
    }

    public void setInputFields(List<String> inputFields) {
	if (CollectionUtils.isEmpty(inputFields)) {
	    this.inputFields = new ArrayList<>();
	} else {
	    this.inputFields = new ArrayList<>(inputFields);
	}
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

    public List<String> getFilterFields() {
	return filterFields;
    }

    public void setFilterFields(ArrayList<String> filterFields) {
	this.filterFields = filterFields;
    }

    public List<File> getFileUpload() {
	return fileUpload;
    }

    public void setFileUpload(ArrayList<File> fileUpload) {
	this.fileUpload = fileUpload;
    }

    public List<String> getFileUploadFileName() {
	return fileUploadFileName;
    }

    public void setFileUploadFileName(ArrayList<String> fileUploadFileName) {
	this.fileUploadFileName = fileUploadFileName;
    }

    public String getMessage() {
	return message;
    }

    public void setMessage(String message) {
	this.message = message;
    }

    public Map<String, String> getCustomValues() {
	return customValues;
    }

    public void setCustomValues(Map<String, String> customValues) {
	this.customValues = customValues;
    }

    public Map<String, HashMap<String, String>> getMapCustomValues() {
	return mapCustomValues;
    }

    public void setMapCustomValues(Map<String, HashMap<String, String>> mapCustomValues) {
	this.mapCustomValues = mapCustomValues;
    }

    public int getFilterPattern() {
	return filterPattern;
    }

    public void setConstantVObject(ConstantVObject constantVObject) {
	this.constantVObject = constantVObject;
    }

    public VObject getInSessionVObject() {
	return inSessionVObject;
    }

    public void setInSessionVObject(VObject inSessionVObject) {
	this.inSessionVObject = inSessionVObject;
    }

	public String getDatabaseColumnSort() {
		return databaseColumnSort;
	}

	public void setDatabaseColumnSort(String databaseColumnSort) {
		this.databaseColumnSort = databaseColumnSort;
	}


}
