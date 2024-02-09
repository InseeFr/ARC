package fr.insee.arc.web.gui.all.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;

import fr.insee.arc.core.dataobjects.ArcDatabase;
import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.service.global.dao.FileSystemManagement;
import fr.insee.arc.core.util.LoggerDispatcher;
import fr.insee.arc.utils.dao.ModeRequeteImpl;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.files.CompressedUtils;
import fr.insee.arc.utils.files.CompressionExtension;
import fr.insee.arc.utils.files.FileUtilsArc;
import fr.insee.arc.utils.structure.AttributeValue;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.ManipString;
import fr.insee.arc.web.gui.all.util.ConstantVObject.ColumnRendering;

/**
 * A service to easily manipulate and display in the app any database object (or
 * more generaly any table data) stored in a VObject instance. Provides general
 * helper methods, can also interact with the database (CRUD functionalities) or
 * extract the data in a file.
 *
 */
@Service
@Qualifier("defaultVObjectService")
@Scope(scopeName = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class VObjectService {

	private static final Logger LOGGER = LogManager.getLogger(VObjectService.class);

	// filter constants
	// 0 is default
	public static final int FILTER_LIKE_CONTAINS = 0;
	public static final Integer DEFAULT_FILTER_PATTERN = FILTER_LIKE_CONTAINS;

	public static final int FILTER_LIKE_ENDSWITH = 1;
	public static final int FILTER_REGEXP_SIMILARTO = 2;
	public static final String FILTER_OR = "OR";
	public static final String FILTER_AND = "AND";

	// pagination and order default constant
	public static final boolean DEFAULT_NO_ORDER = false;
	public static final boolean DEFAULT_NO_COUNT = false;
	public static final boolean DEFAULT_NO_LIMIT = false;

	public static final String DEFAULT_FILTER_FUNCTION = "upper";

	@Autowired
	private Session session;

	@Autowired
	private LoggerDispatcher loggerDispatcher;

	// default database target for query is META_DATA
	private Integer connectionIndex = ArcDatabase.COORDINATOR.getIndex();

	private Connection connection = null;

	/**
	 * Try to set some informations based on the data saved in session.
	 * 
	 * @returns the updated data
	 */
	public VObject preInitialize(VObject currentData) {
		VObject v0 = fetchVObjectData(currentData.getSessionName());
		if (v0 != null) {
			currentData.setTitle(v0.getTitle());
			currentData.setConstantVObject(v0.getConstantVObject());
			currentData.setIsActive(v0.getIsActive());
			currentData.setSavedContent(v0.getContent());

			if (currentData.getTable() == null) {
				currentData.setTable(v0.getTable());
			}
			if (currentData.getHeaderSortDLabel() == null) {
				currentData.setHeaderSortDLabel(v0.getHeaderSortDLabel());
			}
			if (currentData.getHeaderSortDLabels() == null) {
				currentData.setHeaderSortDLabels(v0.getHeaderSortDLabels());
			}
			if (currentData.getHeaderSortDOrders() == null) {
				currentData.setHeaderSortDOrders(v0.getHeaderSortDOrders());
			}
			if (currentData.getIdPage() == null) {
				currentData.setIdPage(v0.getIdPage());
			}
			if (currentData.getFilterFields() == null) {
				currentData.setFilterFields(v0.getFilterFields());
			}
			if (currentData.getHeadersDType() == null) {
				currentData.setHeadersDType(v0.getHeadersDType());
			}
			if (currentData.getHeadersDLabel() == null) {
				currentData.setHeadersDLabel(v0.getHeadersDLabel());
			}
			if (currentData.getSelectedColumns() == null) {
				currentData.setSelectedColumns(v0.getSelectedColumns());
			}
			if (currentData.getSelectedLines() == null) {
				currentData.setSelectedLines(v0.getSelectedLines());
			}
			if (currentData.getPaginationSize() == null) {
				currentData.setPaginationSize(v0.getPaginationSize());
			}
			if (currentData.getInputFields() != null && v0.getDefaultInputFields() != null) {
				for (int i = 0; i < v0.getHeadersDLabel().size(); i++) {
					if (v0.getDefaultInputFields().get(v0.getHeadersDLabel().get(i)) != null) {
						// complete arraylist so that "i" will be in bound for the set command
						for (int k = currentData.getInputFields().size(); k <= i; k++)
							currentData.getInputFields().add(null);
						currentData.getInputFields().set(i,
								v0.getDefaultInputFields().get(v0.getHeadersDLabel().get(i)));
					}
				}
			}
			if (currentData.getCustomValues() == null) {
				currentData.setCustomValues(v0.getCustomValues());
			}
			if (currentData.getAfterInsertQuery() == null) {
				currentData.setAfterInsertQuery(v0.getAfterInsertQuery());
			}
			if (currentData.getAfterUpdateQuery() == null) {
				currentData.setAfterUpdateQuery(v0.getAfterUpdateQuery());
			}
			if (currentData.getMainQuery() == null) {
				currentData.setMainQuery(v0.getMainQuery());
			}
		}
		return currentData;
	}

	/**
	 *
	 * @param VObject            data
	 * @param mainQuery          ne doit pas se terminer par des {@code ;}
	 * @param table
	 * @param defaultInputFields
	 */
	public void initialize(VObject data, ArcPreparedStatementBuilder mainQuery, String table,
			Map<String, String> defaultInputFields) {
		initialize(data, mainQuery, table, defaultInputFields, ((content) -> content));
	}

	/**
	 *
	 * @param VObject            data
	 * @param mainQuery          ne doit pas se terminer par des {@code ;}
	 * @param table
	 * @param defaultInputFields
	 * @param reworkContent      function to rewrite the fetched content
	 */
	private void initialize(VObject data, ArcPreparedStatementBuilder mainQuery, String table,
			Map<String, String> defaultInputFields, Function<List<List<String>>, List<List<String>>> reworkContent) {
		try {
			LoggerHelper.debugAsComment(LOGGER, "initialize", data.getSessionName());

			if (data.getBeforeSelectQuery() != null) {
				UtilitaireDao.get(this.connectionIndex).executeRequest(this.connection, data.getBeforeSelectQuery());
			}

			// on sauvegarde le contenu des lignes selectionnées avant la nouvelle
			// execution de la requete
			Map<String, List<String>> selectedContent = data.mapContentSelected();
			List<String> headersDLabel = new ArrayList<>();
			List<String> headersDType = new ArrayList<>();
			// on sauvegarde les headers des colonnes selectionnées avant la
			// nouvelle execution de la requete
			List<String> selectedHeaders = data.listHeadersSelected();

			// gestion du nombre de pages
			Integer indexPage = pageManagement(mainQuery, data);

			// lancement de la requete principale et recupération du tableau
			ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
			requete.append("select alias_de_table.* from (");
			requete.append(mainQuery);
			requete.append(") alias_de_table ");

			requete.append(buildFilter(data.getFilterFields(), data.getHeadersDLabel(), data.getFilterPattern(),
					data.getFilterFunction()));

			if (!data.isNoOrder()) {
				requete.append(buildOrderBy(data.getHeaderSortDLabels(), data.getHeaderSortDOrders()));
				requete.append(", alias_de_table ");
			}

			if (!data.isNoLimit() && data.getPaginationSize() != null && data.getPaginationSize() > 0) {
				requete.append(buildLimit(data, indexPage));
			}

			List<List<String>> aContent = new ArrayList<>();
			try {
				aContent = reworkContent.apply(UtilitaireDao.get(this.connectionIndex).executeRequest(this.connection,
						requete, ModeRequeteImpl.arcModeRequeteIHM()));
			} catch (ArcException ex) {
				data.setMessage(ex.getMessage());
				LoggerHelper.errorGenTextAsComment(getClass(), "initialize()", LOGGER, ex);
			}
			if (aContent != null && !aContent.isEmpty()) {
				headersDLabel = aContent.remove(0);
				headersDType = aContent.remove(0);
			} else {
				headersDLabel = new ArrayList<>();
				headersDType = new ArrayList<>();
			}

			// on set l'objet
			if (data.getConstantVObject() == null) {
				data.setConstantVObject(new ConstantVObject());
			}
			data.setIsInitialized(true);
			data.setMainQuery(mainQuery);
			data.setTable(table);
			data.setHeadersDLabel(headersDLabel);
			data.setHeadersDType(headersDType);

			// apply the rendering
			applyColumnRendering(data, headersDLabel);

			data.setContent(TableObject.as(aContent));
			data.setDefaultInputFields(defaultInputFields);
			data.setInputFields(eraseInputFields(headersDLabel, defaultInputFields));
			// (Re-)determine selectedLines based on content
			List<Boolean> selectedLines = new ArrayList<>();
			if (!selectedContent.isEmpty()) {
				for (int i = 0; i < data.getContent().size(); i++) {
					int k = 0;
					boolean equals = false;
					while (k < selectedContent.get(data.getHeadersDLabel().get(0)).size() && !equals) {
						equals = true;
						int j = 0;
						while (j < data.getContent().get(i).d.size() && equals) {
							// test si la colonne existe dans le contenu précédent;
							// sinon on l'ignore
							if (selectedContent.get(data.getHeadersDLabel().get(j)) != null) {
								equals = equals && ManipString.compareStringWithNull(data.getContent().get(i).d.get(j),
										selectedContent.get(data.getHeadersDLabel().get(j)).get(k));
							}
							j++;
						}
						k++;
					}
					selectedLines.add(equals);
				}
			}
			data.setSelectedLines(selectedLines);
			// (Re-)determine selectedColums from selectedHeaders
			List<Boolean> selectedColumns = new ArrayList<>();
			for (int i = 0; i < data.getHeadersDLabel().size(); i++) {
				if (selectedHeaders.contains(data.getHeadersDLabel().get(i))) {
					selectedColumns.add(true);
				} else {
					selectedColumns.add(false);
				}
			}
			data.setSelectedColumns(selectedColumns);
			data.setSavedContent(data.getContent());

			// The data is saved in the session
			session.put(data.getSessionName(), data.copy());

		} catch (Exception ex) {
			LoggerHelper.errorGenTextAsComment(getClass(), "initialize()", LOGGER, ex);
		}

	}

	// set the default pagination size if the parameter is null
	public void setPaginationSizeIfNull(VObject currentData) {

		if (currentData.getPaginationSize() == null) {
			currentData.setPaginationSize(currentData.getDefaultPaginationSize());
		}
	}

	/**
	 * Calculate the number of pages that will be display according to the
	 * pagination size required If the parameter "noCount" is set to true, it won't
	 * calculate the number of pages
	 * 
	 * @param mainQuery
	 * @param currentData
	 * @return
	 */
	public Integer pageManagement(ArcPreparedStatementBuilder mainQuery, VObject currentData) {

		List<List<String>> aContent = new ArrayList<>();
		if (currentData.getIdPage() == null) {
			currentData.setIdPage("1");
		}

		setPaginationSizeIfNull(currentData);

		if (!currentData.isNoCount()) {
			if (currentData.getPaginationSize() > 0 && !currentData.isNoOrder()) {
				try {

					ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
					requete.append("select ceil(count(1)::float/" + currentData.getPaginationSize() + ") from (");
					requete.append(mainQuery);
					requete.append(") alias_de_table ");
					requete.append(buildFilter(currentData.getFilterFields(), currentData.getHeadersDLabel()));

					aContent = UtilitaireDao.get(this.connectionIndex).executeRequest(this.connection, requete,
							ModeRequeteImpl.arcModeRequeteIHM());
				} catch (ArcException ex) {
					currentData.setMessage(ex.getMessage());
					LoggerHelper.errorGenTextAsComment(getClass(), "initialize()", LOGGER, ex);
				}
				aContent.remove(0);
				aContent.remove(0);
				currentData.setNbPages(Integer.parseInt(aContent.get(0).get(0)));
			} else {
				currentData.setNbPages(1);
			}
		} else {
			currentData.setNbPages(9999);
		}

		try {
			Integer.parseInt(currentData.getIdPage());
		} catch (NumberFormatException e) {
			currentData.setIdPage("1");
		}

		Integer indexPage = Integer.parseInt(currentData.getIdPage());
		if (currentData.getNbPages() == 0) {
			currentData.setNbPages(1);
		}
		if (indexPage > currentData.getNbPages()) {
			currentData.setIdPage(currentData.getNbPages().toString());
		}
		if (indexPage < 1) {
			currentData.setIdPage("1");
		}
		indexPage = Integer.parseInt(currentData.getIdPage());
		return indexPage;
	}

	/**
	 * Apply a rendering, even after an initialization query So the result of the
	 * query can be interpreted to generate a particular rendering Use method
	 * "initialiserColumnRendering" to set the column rendering
	 * 
	 * @param data
	 * @param headersDLabel
	 */
	public void applyColumnRendering(VObject data, List<String> headersDLabel) {
		data.setHeadersVLabel(buildHeadersVLabel(data, headersDLabel));
		data.setHeadersVSize(buildHeadersVSize(data, headersDLabel));
		data.setHeadersVType(buildHeadersVType(data, headersDLabel));
		data.setHeadersVSelect(buildHeadersVSelect(data, headersDLabel));
		data.setHeadersVisible(buildHeadersVisible(data, headersDLabel));
		data.setHeadersUpdatable(buildHeadersUpdatable(data, headersDLabel));
		data.setHeadersRequired(buildHeadersRequired(data, headersDLabel));
	}

	/**
	 * Génére les labels de colonnes. Si une déclaration est faite dans
	 * ConstantVObject, on met le label déclaré. Sinon on garde le nom de colonne de
	 * la base de données
	 */
	private List<String> buildHeadersVLabel(VObject data, List<String> headers) {
		List<String> headersVLabel = new ArrayList<>();
		for (int i = 0; i < headers.size(); i++) {
			if (data.getConstantVObject()//
					.getColumnRender()//
					.get(headers.get(i)) != null) {
				headersVLabel.add(data.getConstantVObject().getColumnRender().get(headers.get(i)).getLabel());
			} else {
				headersVLabel.add(ManipString.translateAscii(headers.get(i)));
			}
		}
		return headersVLabel;
	}

	/**
	 * Génére la taille de colonnes. Si une déclaration est faite dans
	 * ConstantVObject, on met la taille déclaré. Sinon on ne met rien.
	 */
	private List<String> buildHeadersVSize(VObject data, List<String> headers) {
		List<String> headersVSize = new ArrayList<>();
		for (int i = 0; i < headers.size(); i++) {
			if (data.getConstantVObject().getColumnRender().get(headers.get(i)) != null) {
				headersVSize.add(data.getConstantVObject().getColumnRender().get(headers.get(i)).getSize());
			} else {
				headersVSize.add("auto");
			}
		}
		return headersVSize;
	}

	/**
	 * Génére le type de colonnes. Si une déclaration est faite dans
	 * ConstantVObject, on met le type déclaré. Sinon on met text.
	 */
	private List<String> buildHeadersVType(VObject data, List<String> headers) {
		List<String> headersVType = new ArrayList<>();
		for (int i = 0; i < headers.size(); i++) {
			if (data.getConstantVObject().getColumnRender().get(headers.get(i)) != null) {
				headersVType.add(data.getConstantVObject().getColumnRender().get(headers.get(i)).getType());
			} else {
				headersVType.add("text");
			}
		}
		return headersVType;
	}

	/**
	 * Génére la visibilité des colonnes. Si une déclaration est faite dans
	 * ConstantVObject, on met la visibilité déclarée. Sinon on met visible par
	 * défaut.
	 */
	private List<Boolean> buildHeadersVisible(VObject data, List<String> headers) {
		List<Boolean> headersVisible = new ArrayList<>();
		for (int i = 0; i < headers.size(); i++) {
			if (data.getConstantVObject().getColumnRender().get(headers.get(i)) != null) {
				headersVisible.add(data.getConstantVObject().getColumnRender().get(headers.get(i)).getVisible());
			} else {
				headersVisible.add(true);
			}
		}
		return headersVisible;
	}

	/**
	 * Génére le caractère modifiable des colonnes. Si une déclaration est faite
	 * dans ConstantVObject, on met la valeur déclarée. Sinon on met modifiable par
	 * défaut.
	 */
	private List<Boolean> buildHeadersUpdatable(VObject data, List<String> headers) {
		List<Boolean> headersUpdatable = new ArrayList<>();
		for (int i = 0; i < headers.size(); i++) {
			if (data.getConstantVObject().getColumnRender().get(headers.get(i)) != null) {
				headersUpdatable.add(data.getConstantVObject().getColumnRender().get(headers.get(i)).getIsUpdatable());
			} else {
				headersUpdatable.add(true);
			}
		}
		return headersUpdatable;
	}

	/**
	 * Génére le caractère obligatoire des colonnes. Si une déclaration est faite
	 * dans ConstantVObject, on met la valeur déclarée. Sinon on met obligatoire par
	 * défaut.
	 */
	private List<Boolean> buildHeadersRequired(VObject data, List<String> headers) {
		List<Boolean> headersRequired = new ArrayList<>();
		for (int i = 0; i < headers.size(); i++) {
			if (data.getConstantVObject().getColumnRender().get(headers.get(i)) != null) {
				headersRequired.add(data.getConstantVObject().getColumnRender().get(headers.get(i)).getIsRequired());
			} else {
				headersRequired.add(true);
			}
		}
		return headersRequired;
	}

	private List<Map<String, String>> buildHeadersVSelect(VObject data, List<String> headers) {
		List<List<String>> arrayVSelect = new ArrayList<>();
		List<Map<String, String>> headerVSelect = new ArrayList<>();

		for (int i = 0; i < headers.size(); i++) {
			if (data.getConstantVObject().getColumnRender().get(headers.get(i)) != null
					&& data.getConstantVObject().getColumnRender().get(headers.get(i)).getQuery() != null) {
				try {
					arrayVSelect = UtilitaireDao.get(this.connectionIndex).executeRequest(this.connection,
							data.getConstantVObject().getColumnRender().get(headers.get(i)).getQuery());
					arrayVSelect.remove(0);
					arrayVSelect.remove(0);
					Map<String, String> m = new LinkedHashMap<>();
					for (int j = 0; j < arrayVSelect.size(); j++) {
						m.put(arrayVSelect.get(j).get(0), arrayVSelect.get(j).get(1));
					}
					headerVSelect.add(m);
				} catch (ArcException ex) {
					data.setMessage(ex.getMessage());
					LoggerHelper.errorGenTextAsComment(getClass(), "buildHeadersVSelect()", LOGGER, ex);
				}
			} else {
				Map<String, String> m = new LinkedHashMap<>();
				headerVSelect.add(m);
			}
		}
		return headerVSelect;
	}

	/**
	 * Absolument indispensable dans le cas ou des tables de vues sont générées
	 * dynamiquement Set the rendering for columns
	 * 
	 * @param aRendering
	 */
	public void initialiserColumnRendering(VObject data, Map<String, ColumnRendering> aRendering) {
		data.getConstantVObject().setColumnRender(aRendering);
	}

	/**
	 * Remise à zéro des champs d'entrée avec les valeurs par défault
	 */
	private List<String> eraseInputFields(List<String> headersDLabel, Map<String, String> defaultInputFields) {
		List<String> inputFields = new ArrayList<>();
		for (int i = 0; i < headersDLabel.size(); i++) {
			if (defaultInputFields.get(headersDLabel.get(i)) != null) {
				inputFields.add(defaultInputFields.get(headersDLabel.get(i)));
			} else {
				inputFields.add(null);
			}
		}
		return inputFields;
	}

	/**
	 * On peut avoir envie d'insérer une valeur calculable de façon déterministe
	 * mais non renseignée dans les valeurs insérées. {@link AttributeValue} définit
	 * des paires de {@link String} à cet effet.
	 *
	 * @param attributeValues
	 */
	public boolean insert(VObject currentData, AttributeValue... attributeValues) {
		try {
			LoggerHelper.traceAsComment(LOGGER, "insert()", currentData.getSessionName());
			Map<String, String> map = new HashMap<>();
			Arrays.asList(attributeValues).forEach((t) -> map.put(t.getFirst().toLowerCase(), t.getSecond()));

			// Récupération des colonnes de la table cible
			List<String> nativeFieldList = (ArrayList<String>) UtilitaireDao.get(this.connectionIndex)
					.getColumns(this.connection, new ArrayList<>(), currentData.getTable());

			Boolean allNull = true;
			ArcPreparedStatementBuilder reqInsert = new ArcPreparedStatementBuilder();
			ArcPreparedStatementBuilder reqValues = new ArcPreparedStatementBuilder();
			reqInsert.append("INSERT INTO " + currentData.getTable() + " (");
			reqValues.append("VALUES (");
			int j = 0;
			boolean comma = false;
			for (int i = 0; i < currentData.getInputFields().size(); i++) {
				if (nativeFieldList.contains(currentData.getHeadersDLabel().get(i))) {
					if (comma) {
						reqInsert.append(",");
						reqValues.append(",");
					}
					comma = true;
					reqInsert.append(currentData.getHeadersDLabel().get(i));
					String insertValue;
					if (attributeValues != null && attributeValues.length > j
							&& map.containsKey(currentData.getHeadersDLabel().get(i).toLowerCase())) {
						insertValue = map.get(currentData.getHeadersDLabel().get(i).toLowerCase());
					} else if (currentData.getInputFields().get(i) != null
							&& currentData.getInputFields().get(i).length() > 0) {
						allNull = false;
						insertValue = reqValues.quoteText(currentData.getInputFields().get(i)) + "::"
								+ currentData.getHeadersDType().get(i);
					} else {
						insertValue = "null";
					}
					reqValues.append(insertValue);
				}
			}
			reqInsert.append(") ");
			reqValues.append("); ");
			ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
			requete.append("BEGIN;");
			requete.append(reqInsert);
			requete.append(reqValues);
			if (currentData.getAfterInsertQuery() != null) {
				requete.append("\n");
				requete.append(currentData.getAfterInsertQuery());
				requete.append("\n");
			}
			requete.append("END;");

			if (!allNull) {
				UtilitaireDao.get(this.connectionIndex).executeRequest(this.connection, requete);
			}

		} catch (Exception ex) {
			LoggerHelper.error(LOGGER, ex);
			currentData.setMessage("vObject.insert.error");
			currentData.setMessageArgs(ex.getCause());
			return false;
		}
		return true;
	}

	/*
	 * delete the selected item in database
	 */
	public void delete(VObject currentData, String... tables) {
		LoggerHelper.traceAsComment(LOGGER, "delete()", currentData.getSessionName());
		try {
			UtilitaireDao.get(this.connectionIndex).executeRequest(this.connection,
					deleteQuery(currentData, tables).asTransaction());
		} catch (ArcException ex) {
			LoggerHelper.error(LOGGER, ex);
			currentData.setMessage("vObject.delete.error");
			currentData.setMessageArgs(ex.getCause());
		}
	}

	/**
	 * Compute the query for deletion
	 * 
	 * @param currentData
	 * @param tables
	 * @return
	 * @throws ArcException
	 */
	public ArcPreparedStatementBuilder deleteQuery(VObject currentData, String... tables) throws ArcException {

		VObject v0 = fetchVObjectData(currentData.getSessionName());

		List<String> listeColonneNative = (ArrayList<String>) UtilitaireDao.get(this.connectionIndex)
				.getColumns(this.connection, new ArrayList<>(), currentData.getTable());
		ArcPreparedStatementBuilder reqDelete = new ArcPreparedStatementBuilder();
		for (int i = 0; i < currentData.getSelectedLines().size(); i++) {
			if (currentData.getSelectedLines().get(i) != null && currentData.getSelectedLines().get(i)) {
				if (tables.length == 0) {
					reqDelete.append("DELETE FROM " + v0.getTable() + " WHERE ");
				} else {
					reqDelete.append("DELETE FROM " + tables[0] + " WHERE ");
				}

				boolean comma = false;
				for (int j = 0; j < v0.getHeadersDLabel().size(); j++) {
					if (listeColonneNative.contains(v0.getHeadersDLabel().get(j))) {
						if (comma) {
							reqDelete.append(" AND ");
						}
						comma = true;

						reqDelete.append(v0.getHeadersDLabel().get(j));

						if (v0.getContent().get(i).d.get(j) != null && v0.getContent().get(i).d.get(j).length() > 0) {
							reqDelete.append("=" + reqDelete.quoteText(v0.getContent().get(i).d.get(j)) + "::"
									+ v0.getHeadersDType().get(j));
						} else {
							reqDelete.append(" is null");
						}
					}
				}
				reqDelete.append("; ");
			}
		}
		return reqDelete;
	}

	public void update(VObject currentData) {
		LoggerHelper.traceAsComment(LOGGER, "update()", currentData.getSessionName());
		VObject v0 = fetchVObjectData(currentData.getSessionName());
		// Compares new and old values line by line
		// Stocks the modified line number in toBeUpdated
		List<Integer> toBeUpdated = new ArrayList<>();
		for (int i = 0; i < currentData.getContent().size(); i++) {
			LineObject line = currentData.getContent().get(i);
			if (line != null) {
				for (int j = 0; j < line.d.size(); j++) {
					if (line.d.get(j) != null
							&& !ManipString.compareStringWithNull(v0.getContent().get(i).d.get(j), line.d.get(j))) {
						toBeUpdated.add(i);
						break;
					}
				}
			}
		}

		try {
			List<String> nativeFieldsList = (ArrayList<String>) UtilitaireDao.get(this.connectionIndex)
					.getColumns(this.connection, new ArrayList<>(), currentData.getTable());

			// SQL update query
			ArcPreparedStatementBuilder reqUpdate = new ArcPreparedStatementBuilder();
			reqUpdate.append("BEGIN; ");

			for (int i = 0; i < toBeUpdated.size(); i++) {
				reqUpdate.append("\nUPDATE " + v0.getTable() + " SET ");
				boolean comma = false;
				int lineToBeUpdated = toBeUpdated.get(i);
				for (int j = 0; j < currentData.getContent().get(lineToBeUpdated).d.size(); j++) {
					// If the field exists in the bdd and has any value
					String label = v0.getHeadersDLabel().get(j);
					String newValue = currentData.getContent().get(lineToBeUpdated).d.get(j);
					if (nativeFieldsList.contains(label) && newValue != null) {
						if (comma) {
							reqUpdate.append(" ,");
						}
						comma = true;

						if (ManipString.isStringNull(newValue)) {
							reqUpdate.append(label + "=NULL");
						} else {
							// Serial type is set as int4
							String type = v0.getHeadersDType().get(j).equals("serial") ? "int4"
									: v0.getHeadersDType().get(j);
							reqUpdate.append(label + "=" + reqUpdate.quoteText(newValue) + "::" + type);
						}
					}
				}
				reqUpdate.append(" WHERE ");

				comma = false;
				for (int j = 0; j < currentData.getContent().get(lineToBeUpdated).d.size(); j++) {
					String label = v0.getHeadersDLabel().get(j);
					if (nativeFieldsList.contains(label)) {
						if (comma) {
							reqUpdate.append(" AND ");
						}
						comma = true;

						String oldValue = v0.getContent().get(lineToBeUpdated).d.get(j);
						if (ManipString.isStringNull(oldValue)) {
							reqUpdate.append(label + " IS NULL");
						} else {
							String type = v0.getHeadersDType().get(j).equals("serial") ? "int4"
									: v0.getHeadersDType().get(j);
							reqUpdate.append(label + "=" + reqUpdate.quoteText(oldValue) + "::" + type);
						}

						// Updates value in v0
						String newValue = currentData.getContent().get(lineToBeUpdated).d.get(j);
						if (newValue != null) {
							v0.getContent().get(lineToBeUpdated).d.set(j, newValue);
						}
					}
				}
				reqUpdate.append("; ");
			}
			if (v0.getAfterUpdateQuery() != null) {
				reqUpdate.append("\n");
				reqUpdate.append(v0.getAfterUpdateQuery());
				reqUpdate.append("\n");
			}
			reqUpdate.append("END;");
			if (!toBeUpdated.isEmpty()) {
				UtilitaireDao.get(this.connectionIndex).executeRequest(this.connection, reqUpdate);
			}
			session.put(currentData.getSessionName(), v0);
		} catch (ArcException ex) {
			LoggerHelper.error(LOGGER, ex);
			currentData.setMessage("vObject.update.error");
			currentData.setMessageArgs(ex.getCause());
		}

	}

	public ArcPreparedStatementBuilder queryView(VObject currentData) {
		VObject v0 = fetchVObjectData(currentData.getSessionName());
		if (currentData.getFilterFields() == null) {
			currentData.setFilterFields(v0.getFilterFields());
		}
		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
		requete.append("select alias_de_table.* from (");
		requete.append(v0.getMainQuery());
		requete.append(") alias_de_table ");
		requete.append(buildFilter(currentData.getFilterFields(), v0.getHeadersDLabel()));
		return requete;
	}

	public void destroy(VObject data) {
		data.clear();
		session.remove(data.getSessionName());
	}

	public ArcPreparedStatementBuilder buildFilter(List<String> filterFields, List<String> headersDLabel) {
		return buildFilter(filterFields, headersDLabel, DEFAULT_FILTER_PATTERN, DEFAULT_FILTER_FUNCTION);
	}

	/**
	 * Build the filter expression
	 * 
	 * @param filterFields
	 * @param headersDLabel
	 * @return
	 */

	private ArcPreparedStatementBuilder buildFilter(List<String> filterFields, List<String> headersDLabel,
			Integer filterPattern, String filterFunction) {

		ArcPreparedStatementBuilder s = new ArcPreparedStatementBuilder(" WHERE true ");

		if (headersDLabel == null || filterFields == null) {
			return s;
		}

		// symbole mathématiques que l'on peut avoir dans le filtre
		Pattern patternMath = Pattern.compile("[<>=]");
		String expressionAND = " AND";
		String expressionOR = " OR";

		for (int headerIndex = 0; headerIndex < filterFields.size(); headerIndex++) {
			if (filterFields.get(headerIndex) == null || filterFields.get(headerIndex).isBlank()) {
				continue;
			}

			if ((filterPattern == FILTER_LIKE_CONTAINS || filterPattern == FILTER_LIKE_ENDSWITH
					|| filterPattern == FILTER_REGEXP_SIMILARTO)) {
				s.append(" AND (");
			}

			/*
			 * Si on a un symbole mathématique
			 */
			Matcher matcher = patternMath.matcher(filterFields.get(headerIndex));
			boolean isFilterMathematicExpression = matcher.find();
			boolean isFilterDate = filterFields.get(headerIndex).contains("§");

			if (isFilterMathematicExpression && isFilterDate) {
				buildFilterDate(headerIndex, s, filterFields, headersDLabel, expressionAND, expressionOR);
			}

			if (isFilterMathematicExpression && !isFilterDate) {
				buildFilterNumeric(headerIndex, s, filterFields, headersDLabel, expressionAND, expressionOR);
			}

			if (!isFilterMathematicExpression) {
				buildFilterString(headerIndex, s, filterFields, headersDLabel, filterPattern, filterFunction);
			}

			s.append(") ");
		}
		s.append(" ");
		return s;
	}

	private void buildFilterString(int headerIndex, ArcPreparedStatementBuilder s, List<String> filterFields,
			List<String> headersDLabel, Integer filterPattern, String filterFunction) {

		String toSearch = "";

		if (filterPattern == FILTER_LIKE_CONTAINS || filterPattern == FILTER_LIKE_ENDSWITH) {
			s.append(" " + filterFunction + "(" + headersDLabel.get(headerIndex) + "::text) LIKE ");
		}

		if (filterPattern == FILTER_REGEXP_SIMILARTO) {
			s.append(" ' '||" + filterFunction + "(" + headersDLabel.get(headerIndex) + "::text) SIMILAR TO ");
		}

		// Si on a déjà un % dans le filtre on n'en rajoute pas
		if ((filterPattern == FILTER_LIKE_CONTAINS || filterPattern == FILTER_REGEXP_SIMILARTO)
				&& !filterFields.get(headerIndex).contains("%")) {
			toSearch += "%";
		}

		if (filterPattern == FILTER_LIKE_CONTAINS || filterPattern == FILTER_LIKE_ENDSWITH) {
			toSearch += filterFields.get(headerIndex).toUpperCase();
		}

		if (filterPattern == FILTER_REGEXP_SIMILARTO) {
			String aChercher = patternMather(filterFields.get(headerIndex).toUpperCase().trim());
			toSearch += "( " + aChercher.replace(" ", "| ") + ")%";
		}

		if ((filterPattern == FILTER_LIKE_CONTAINS || filterPattern == FILTER_LIKE_ENDSWITH)
				&& !filterFields.get(headerIndex).contains("%")) {
			toSearch += "%";
		}

		s.append(s.quoteText(toSearch));
	}

	private void buildFilterNumeric(int headerIndex, ArcPreparedStatementBuilder s, List<String> filterFields,
			List<String> headersDLabel, String expressionAND, String expressionOR) {
		String[] listeAND = filterFields.get(headerIndex).split(FILTER_AND);

		for (String conditionAND : listeAND) {
			// on découpe suivant les OU
			String[] listeOR = conditionAND.split(FILTER_OR);
			for (String condtionOR : listeOR) {
				if (condtionOR.contains("[")) { // cas ou on va chercher dans un vecteur

					condtionOR = condtionOR.trim();

					s.append(" (" + headersDLabel.get(headerIndex));

					s.append(condtionOR.substring(0, 1) + "array_position(" + headersDLabel.get(headerIndex - 1) + ","
							+ s.quoteText(condtionOR.substring(1, condtionOR.indexOf("]"))) + ")"
							+ condtionOR.substring(condtionOR.indexOf("]"), condtionOR.indexOf("]") + 1));

					s.append(condtionOR.substring(condtionOR.indexOf("]") + 1));

				} else {
					s.append(" (" + headersDLabel.get(headerIndex) + ")" + condtionOR);

				}
				s.append(expressionOR);
			}
			// on retire les dernier OR
			s.setLength(s.length() - expressionOR.length());
			s.append(expressionAND);
		}

		// on retire le dernier AND
		s.setLength(s.length() - expressionAND.length());

	}

	private void buildFilterDate(int headerIndex, ArcPreparedStatementBuilder s, List<String> filterFields,
			List<String> headersDLabel, String expressionAND, String expressionOR) {
		String filtre = filterFields.get(headerIndex);
		String[] morceauReq = filtre.split("§");

		// on découpe suivant les ET
		String[] listeAND = morceauReq[1].split(FILTER_AND);

		for (String conditionAND : listeAND) {
			// on découpe suivant les OU
			String[] listeOR = conditionAND.split(FILTER_OR);
			for (String condtionOR : listeOR) {
				s.append(" to_date(" + headersDLabel.get(headerIndex) + "::text, " + s.quoteText(morceauReq[0]) + ")");
				// cast database column to the searched date format
				s.append(condtionOR.trim().substring(0, 1)); // operator
				s.append(" to_date(" + s.quoteText(condtionOR.trim().substring(1)) + "," + s.quoteText(morceauReq[0])
						+ ") ");
				// cast condition expression to the searched date format
				s.append(expressionOR);
			}
			// on retire les dernier OR
			s.setLength(s.length() - expressionOR.length());
			s.append(expressionAND);
		}
		// on retire le dernier AND
		s.setLength(s.length() - expressionAND.length());
	}

	private String patternMather(String aChercher) {
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
	 * @param headerSortLabels  noms des colonnes
	 * @param headerSortDOrders ordres du tri des colonnes
	 * @return
	 */
	public ArcPreparedStatementBuilder buildOrderBy(List<String> headerSortLabels, List<Boolean> headerSortDOrders) {
		if (headerSortLabels == null) {
			return new ArcPreparedStatementBuilder("order by alias_de_table ");
		}

		ArcPreparedStatementBuilder s = new ArcPreparedStatementBuilder();

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
		return s;
	}

	public ArcPreparedStatementBuilder buildLimit(VObject data, Integer indexPage) {

		setPaginationSizeIfNull(data);

		return new ArcPreparedStatementBuilder(
				" limit " + data.getPaginationSize() + " offset " + ((indexPage - 1) * data.getPaginationSize()));

	}

	/**
	 * Trier suivant une colonne
	 */
	public void sort(VObject currentData) {
		LoggerHelper.debugAsComment(LOGGER, "sort()");
		VObject v0 = fetchVObjectData(currentData.getSessionName());
		if (v0.getHeadersDLabel().indexOf(currentData.getHeaderSortDLabel()) != -1) {
			this.setHeaderSortDLabels(currentData, v0.getHeaderSortDLabels());
			currentData.getHeaderSortDOrders();
			// on initialize si la liste n'existe pas
			if (currentData.getHeaderSortDLabels() == null) {
				currentData.setHeaderSortDLabels(new ArrayList<>());
				currentData.setHeaderSortDOrders(new ArrayList<>());
			}
			int pos = currentData.getHeaderSortDLabels().indexOf(currentData.getHeaderSortDLabel());
			// si le champ a sort est en premiere position, on va inverser le
			// sens de l'order by
			if (pos == 0) {
				currentData.getHeaderSortDOrders().set(0, !currentData.getHeaderSortDOrders().get(0));
			}
			// si le champ est inconnu, on le met en premiere position avec un
			// sens asc
			else if (pos == -1) {
				currentData.getHeaderSortDLabels().add(0, currentData.getHeaderSortDLabel());
				currentData.getHeaderSortDOrders().add(0, true);
			}
			// sinon on l'enleve de la liste existante et on le remet en
			// premiere position avec un sens inverse a celui d'avant
			else {
				currentData.getHeaderSortDLabels().remove(pos);
				currentData.getHeaderSortDOrders().remove(pos);
				currentData.getHeaderSortDLabels().add(0, currentData.getHeaderSortDLabel());
				currentData.getHeaderSortDOrders().add(0, true);
			}
		}
	}

	/**
	 * Téléchargement dans un zip de N fichiers csv, les données étant extraites de
	 * la base de données
	 *
	 * @param fileNames , liste des noms de fichiers obtenus
	 * @param requetes  , liste des requetes SQL
	 */
	public void download(VObject currentData, HttpServletResponse response, List<String> fileNames,
			List<ArcPreparedStatementBuilder> requetes) {
		VObject v0 = fetchVObjectData(currentData.getSessionName());
		if (currentData.getFilterFields() == null) {
			currentData.setFilterFields(v0.getFilterFields());
		}
		Date dNow = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("yyyyMMddHHmmss");
		response.reset();
		response.setHeader("Content-Disposition",
				"attachment; filename=" + v0.getSessionName() + "_" + ft.format(dNow) + ".csv.zip");
		try {
			// Rattachement du zip à la réponse de Struts2
			ZipOutputStream zos = new ZipOutputStream(response.getOutputStream());
			try {
				for (int i = 0; i < requetes.size(); i++) {
					// Le nom des fichiers à l'interieur du zip seront simple :
					// fichier1.csv, fichier2.csv etc.
					// Ajout d'un nouveau fichier
					ZipEntry entry = new ZipEntry(fileNames.get(i) + ".csv");
					zos.putNextEntry(entry);
					// Ecriture dans le fichier
					UtilitaireDao.get(this.connectionIndex).outStreamRequeteSelect(this.connection, requetes.get(i),
							zos);
					zos.closeEntry();
				}
			} finally {
				zos.close();
			}
		} catch (IOException | ArcException ex) {
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
	public void downloadXML(VObject currentData, HttpServletResponse response, ArcPreparedStatementBuilder requete,
			String repertoire, String anEnvExcecution, String phase) {
		VObject v0 = fetchVObjectData(currentData.getSessionName());
		if (currentData.getFilterFields() == null) {
			currentData.setFilterFields(v0.getFilterFields());
		}
		Date dNow = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("yyyyMMddHHmmss");
		response.reset();
		response.setHeader("Content-Disposition", "attachment; filename=" + v0.getSessionName() + "_" + ft.format(dNow)
				+ CompressionExtension.TAR_GZ.getFileExtension());

		TarArchiveOutputStream taos = null;
		try {
			taos = new TarArchiveOutputStream(new GZIPOutputStream(response.getOutputStream()));
			zipOutStreamRequeteSelect(this.connection, requete, taos, repertoire, anEnvExcecution, phase, "ARCHIVE");
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

	/**
	 * Ecrit le résultat de la requête {@code requete} dans le fichier compressé
	 * {@code zos} !Important! la requete doit être ordonnée sur le container <br/>
	 *
	 *
	 * @param connexion
	 * @param requete
	 * @param taos
	 * @param nomPhase
	 *
	 * @param dirSuffix
	 */
	public void zipOutStreamRequeteSelect(Connection connexion, ArcPreparedStatementBuilder requete,
			TarArchiveOutputStream taos, String repertoireIn, String anEnvExcecution, String nomPhase,
			String dirSuffix) {
		int k = 0;
		int fetchSize = 5000;
		GenericBean g;
		List<String> listIdSource;
		List<String> listIdSourceEtat;
		List<String> listContainer;

		String repertoire = FileSystemManagement.directoryEnvRoot(repertoireIn, anEnvExcecution) + File.separator;

		String currentContainer;
		while (true) {
			// Réécriture de la requete pour avoir le i ème paquet
			ArcPreparedStatementBuilder requeteLimit = new ArcPreparedStatementBuilder();
			requeteLimit.append(requete);
			requeteLimit.append(" offset " + (k * fetchSize) + " limit " + fetchSize + " ");
			// Récupération de la liste d'id_source par paquet de fetchSize
			try {
				g = new GenericBean(
						UtilitaireDao.get(this.connectionIndex).executeRequest(this.connection, requeteLimit));
				Map<String, List<String>> m = g.mapContent();
				listIdSource = m.get(ColumnEnum.ID_SOURCE.getColumnName());
				listContainer = m.get("container");
				listIdSourceEtat = m.get("etat_traitement");
			} catch (ArcException ex) {
				LoggerHelper.errorGenTextAsComment(getClass(), "zipOutStreamRequeteSelect()", LOGGER, ex);
				break;
			}
			if (listIdSource == null) {
				LoggerHelper.traceAsComment(LOGGER, "listIdSource est null, sortie");
				break;
			}

			LoggerHelper.traceAsComment(LOGGER, " listIdSource.size() =", listIdSource.size());

			List<String> listIdSourceContainer = new ArrayList<>();
			List<String> listIdSourceEtatContainer = new ArrayList<>();

			// Ajout des fichiers à l'archive
			int i = 0;
			while (i < listIdSource.size()) {
				String receptionDirectoryRoot = Paths.get(repertoire,
						nomPhase + "_" + ManipString.substringBeforeFirst(listIdSource.get(i), "_") + "_" + dirSuffix)
						.toString();
				// fichier non archivé
				if (CompressedUtils.isNotArchive(listContainer.get(i))) {
					CompressedUtils.generateEntryFromFile(receptionDirectoryRoot,
							ManipString.substringAfterFirst(listIdSource.get(i), "_"), taos);
					i++;
				} else {
					// on sauvegarde la valeur du container courant
					// on va extraire de la listIdSource tous les fichiers du
					// même container
					currentContainer = ManipString.substringAfterFirst(listContainer.get(i), "_");
					listIdSourceContainer.clear();
					listIdSourceEtatContainer.clear();
					int j = i;
					while (j < listContainer.size()
							&& ManipString.substringAfterFirst(listContainer.get(j), "_").equals(currentContainer)) {
						listIdSourceContainer.add(ManipString.substringAfterFirst(listIdSource.get(j), "_"));
						listIdSourceEtatContainer.add(listIdSourceEtat.get(j));
						j++;
					}
					// archive .tar.gz
					if (currentContainer.endsWith(CompressionExtension.TAR_GZ.getFileExtension())
							|| currentContainer.endsWith(CompressionExtension.TGZ.getFileExtension())) {
						CompressedUtils.generateEntryFromTarGz(receptionDirectoryRoot, currentContainer,
								listIdSourceContainer, taos);
						i = i + listIdSourceContainer.size();
					} else if (currentContainer.endsWith(CompressionExtension.ZIP.getFileExtension())) {
						CompressedUtils.generateEntryFromZip(receptionDirectoryRoot, currentContainer,
								listIdSourceContainer, taos);
						i = i + listIdSourceContainer.size();
					}
					// archive .gz
					else if (listContainer.get(i).endsWith(CompressionExtension.GZ.getFileExtension())) {
						CompressedUtils.generateEntryFromGz(receptionDirectoryRoot, currentContainer,
								listIdSourceContainer, taos);
						i = i + listIdSourceContainer.size();
					}
				}
			}
			k++;
		}
	}

	/**
	 * Télécharger en tar gzip une liste de fichier
	 *
	 * @param requete        la selection de fichier avec la clé pertinente qui doit
	 *                       d'appeler nom_fichier
	 * @param repertoire     chemin jusqu'à l'avant dernier dossier
	 * @param listRepertoire noms du dernier dossier (chaque fichier pouvant être
	 *                       dans l'un de la liste)
	 */
	public void downloadEnveloppe(VObject currentData, HttpServletResponse response,
			ArcPreparedStatementBuilder requete, String repertoire, List<String> listRepertoire) {
		VObject v0 = fetchVObjectData(currentData.getSessionName());

		if (currentData.getFilterFields() == null) {
			currentData.setFilterFields(v0.getFilterFields());
		}

		Date dNow = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("yyyyMMddHHmmss");
		response.reset();
		response.setHeader("Content-Disposition",
				"attachment; filename=" + v0.getSessionName() + "_" + ft.format(dNow) + ".tar");

		try (TarArchiveOutputStream taos = new TarArchiveOutputStream(response.getOutputStream());) {
			UtilitaireDao.get(this.connectionIndex).getFilesDataStreamFromListOfInputDirectories(this.connection,
					requete, taos, repertoire, listRepertoire);
		} catch (IOException ex) {
			LoggerHelper.errorGenTextAsComment(getClass(), "downloadEnveloppe()", LOGGER, ex);
		} finally {
			try {
				response.getOutputStream().flush();
				response.getOutputStream().close();
			} catch (IOException ex) {
				LoggerHelper.errorGenTextAsComment(getClass(), "downloadEnveloppe()", LOGGER, ex);
			}
		}
	}

	/**
	 * Upload file to directory The method uses temporary folder because writing
	 * directly file to a mounted path may be not allowed
	 * 
	 * @param data
	 * @param repertoireCible
	 * @throws ArcException
	 */
	public void upload(VObject data, String repertoireCible) throws ArcException {
		if (data.getFileUpload() != null) {
			for (MultipartFile uploadedFile : data.getFileUpload()) {
				String fileName = uploadedFile.getOriginalFilename();
				// The file can be effectively empty (no name and no content)
				if (fileName != null && !fileName.isEmpty()) {

					File temporaryFolder = null;

					try {
						temporaryFolder = Files.createTempDirectory("tmp_" + System.currentTimeMillis()).toFile();

						Path locationTmp = Paths.get(temporaryFolder.getAbsolutePath(), fileName);
						loggerDispatcher.info("Upload >> " + locationTmp, LOGGER);

						File newFileTmp = locationTmp.toFile();

						// security fix : transferTo not allowed on non temporary directory
						uploadedFile.transferTo(newFileTmp);

						Path location = Paths.get(repertoireCible, fileName);
						loggerDispatcher.info("Transfering uploaded file to  >> " + location, LOGGER);
						Files.copy(locationTmp, location, StandardCopyOption.REPLACE_EXISTING);

					} catch (IOException ex) {
						throw new ArcException(ex, ArcExceptionMessage.FILE_COPY_FAILED, fileName, repertoireCible);
					} finally {
						if (temporaryFolder != null) {
							FileUtilsArc.deleteDirectory(temporaryFolder);
						}
					}
				}
			}
		}
	}

	public List<String> getHeaderSortDLabels(VObject currentData) {
		return currentData.getHeaderSortDLabels();
	}

	public void setHeaderSortDLabels(VObject currentData, List<String> headerSortLabels) {
		currentData.setHeaderSortDLabels(headerSortLabels);
	}

	/**
	 * Calcule si la vue est active ou pas :
	 * <ul>
	 * <li>si on ne retrouve pas le nom de la vue dans le scope, on ne fait rien
	 * </li>
	 * <li>si le nom de la vue est retrouvée dans le scope avec un moins devant, on
	 * desactive</li>
	 * <li>si le nom de la vue est retrouvée dans le scope sans avoir un moins
	 * devant, on active</li>
	 * </ul>
	 *
	 * @param scope
	 */
	public void setActivation(VObject data, String scope) {

		data.setIsScoped(false);

		if (scope != null) {
			if (scope.contains("-" + data.getSessionName() + ";")) {
				data.setIsActive(false);
			} else if (scope.contains(data.getSessionName() + ";")) {
				data.setIsActive(true);
			}
		}

		if (data.getIsActive() && (scope == null || scope.contains(data.getSessionName() + ";"))) {
			data.setIsScoped(true);
		}

	}

	private VObject fetchVObjectData(String sessionName) {
		return (VObject) session.get(sessionName);
	}

	public final void setColumnRendering(VObject data, Map<String, ColumnRendering> columnRender) {
		data.setConstantVObject(new ConstantVObject(columnRender));
	}

	public void initializeByList(VObject data, List<List<String>> liste, Map<String, String> defaultInputFields) {

		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
		List<String> header = liste.get(0);
		List<String> type = liste.get(1);

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
						"" + requete.quoteText(liste.get(i).get(j)) + "::" + type.get(j) + " as " + header.get(j));
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
		// on ne gere pas les autres cas: ca doit planter
		this.initialize(data, requete, data.getTable(), defaultInputFields);
	}

	// give values to be added to a result row
	public static void addRowToVObjectList(List<List<String>> result, String... elements) {
		result.add(new ArrayList<>(Arrays.asList(Arrays.copyOf(elements, elements.length))));
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public void setConnectionIndex(Integer connectionIndex) {
		this.connectionIndex = connectionIndex;
	}
	
	public void resetConnectionIndex() {
		this.connectionIndex = ArcDatabase.COORDINATOR.getIndex();
	}

}
