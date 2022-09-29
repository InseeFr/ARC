package fr.insee.arc.web.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

import javax.servlet.http.HttpServletResponse;

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

import fr.insee.arc.core.util.LoggerDispatcher;
import fr.insee.arc.utils.dao.ModeRequete;
import fr.insee.arc.utils.dao.PreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.structure.AttributeValue;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.ManipString;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;

/**
 * A service to easily manipulate and display in the app any database object (or
 * more generaly any table data) stored in a VObject instance.
 * Provides general helper methods, can also interact with the database (CRUD functionalities) 
 * or extract the data in a file.
 * 
 * @author Soulier Manuel
 *
 */
@Service
@Qualifier("defaultVObjectService")
@Scope(scopeName = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class VObjectService {

    private static final Logger LOGGER = LogManager.getLogger(VObjectService.class);

    @Autowired
    private Session session;

    @Autowired
    private LoggerDispatcher loggerDispatcher;
    
    private String pool = "arc";
    
    // filter constants
    // 0 is default
    public static final int FILTER_LIKE_CONTAINS=0;
    public static final Integer DEFAULT_FILTER_PATTERN=FILTER_LIKE_CONTAINS;
    
    public static final int FILTER_LIKE_ENDSWITH=1;
    public static final int FILTER_REGEXP_SIMILARTO=2;
    public static final String FILTER_OR="OR";
    public static final String FILTER_AND="AND";

    // pagination and order default constant
	public static final boolean DEFAULT_NO_ORDER=false;
	public static final boolean DEFAULT_NO_COUNT=false;
	public static final boolean DEFAULT_NO_LIMIT=false;

	public static final String DEFAULT_FILTER_FUNCTION="upper";
	
	
    

    
    /** Try to set some informations based on the data saved in session.
	 * @returns the updated data*/
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
			        	for (int k=currentData.getInputFields().size(); k<=i;k++)
			        			currentData.getInputFields().add(null);
			            currentData.getInputFields().set(i, v0.getDefaultInputFields().get(v0.getHeadersDLabel().get(i)));
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
	 * @param VObject data
	 * @param mainQuery ne doit pas se terminer par des {@code ;}
	 * @param table
	 * @param defaultInputFields
	 */
	public void initialize(VObject data, PreparedStatementBuilder mainQuery, String table, HashMap<String, String> defaultInputFields) {
		initialize(data, mainQuery, table, defaultInputFields, ((content) -> content));
	}


	/**
	 *
	 * @param VObject data
	 * @param mainQuery ne doit pas se terminer par des {@code ;}
	 * @param table
	 * @param defaultInputFields
	 * @param reworkContent function to rewrite the fetched content
	 */
	private void initialize(VObject data, PreparedStatementBuilder mainQuery, String table, HashMap<String, String> defaultInputFields, 
			Function<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>> reworkContent) {
	    try {
	        LoggerHelper.debugAsComment(LOGGER, "initialize", data.getSessionName());
	
	        if (data.getBeforeSelectQuery() != null) {
	            UtilitaireDao.get(this.pool).executeRequest(null, data.getBeforeSelectQuery());
	        }
	
	        // on sauvegarde le contenu des lignes selectionnées avant la nouvelle
	        // execution de la requete
	        HashMap<String, ArrayList<String>> selectedContent = data.mapContentSelected();
	        ArrayList<String> headersDLabel = new ArrayList<>();
	        ArrayList<String> headersDType = new ArrayList<>();
	        // on sauvegarde les headers des colonnes selectionnées avant la
	        // nouvelle execution de la requete
	        ArrayList<String> selectedHeaders = data.listHeadersSelected();
	
	        // gestion du nombre de pages
	        Integer indexPage = pageManagement(mainQuery, data);
	
	        // lancement de la requete principale et recupération du tableau
	        PreparedStatementBuilder requete = new PreparedStatementBuilder();
	        requete.append("select alias_de_table.* from (");
	        requete.append(mainQuery);
	        requete.append(") alias_de_table ");
	        
	        requete.append(buildFilter(data.getFilterFields(), data.getHeadersDLabel(), data.getFilterPattern(), data.getFilterFunction()));
 
	        if (!data.isNoOrder()) {
	            requete.append(buildOrderBy(data.getHeaderSortDLabels(), data.getHeaderSortDOrders()));
	            requete.append(", alias_de_table ");
	        }
	
	        if (!data.isNoLimit() && data.getPaginationSize() != null && data.getPaginationSize() > 0) {
	            requete.append(buildLimit(data, indexPage));
	        }
	

	        ArrayList<ArrayList<String>> aContent = new ArrayList<>();
	        try {
	            aContent = reworkContent.apply(UtilitaireDao.get(this.pool).executeRequest(null, requete,  ModeRequete.IHM_INDEXED));
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
	        ArrayList<Boolean> selectedLines = new ArrayList<>();
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
	                            equals = equals
	                                    && ManipString.compareStringWithNull(data.getContent().get(i).d.get(j),
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
	        ArrayList<Boolean> selectedColumns = new ArrayList<>();
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
	public void setPaginationSizeIfNull(VObject currentData)
	{
		
		if (currentData.getPaginationSize() == null) {
			currentData.setPaginationSize(currentData.getDefaultPaginationSize());
		}
	}
	
	
	/**
	 * Calculate the number of pages that will be display according to the pagination size required
	 * If the parameter "noCount" is set to true, it won't calculate the number of pages 
	 * @param mainQuery
	 * @param currentData
	 * @return
	 */
	public Integer pageManagement(PreparedStatementBuilder mainQuery, VObject currentData) {
				
		ArrayList<ArrayList<String>> aContent = new ArrayList<>();
		if (currentData.getIdPage() == null) {
		    currentData.setIdPage("1");
		}
		
		setPaginationSizeIfNull(currentData);
		
		if (!currentData.isNoCount()) {
		    if (currentData.getPaginationSize() > 0 
		    		&& !currentData.isNoOrder()) {
		        try {
		        	
		        	PreparedStatementBuilder requete=new PreparedStatementBuilder();
		        	requete.append("select ceil(count(1)::float/" + currentData.getPaginationSize() + ") from (");
		        	requete.append(mainQuery);
		        	requete.append(") alias_de_table ");
		        	requete.append(buildFilter(currentData.getFilterFields(), currentData.getHeadersDLabel()));

		            aContent = UtilitaireDao.get(this.pool).executeRequest(null, requete, ModeRequete.IHM_INDEXED);
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
		}
		else
		{
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
     * Absolument indispensable dans le cas ou des tables de vues sont générées dynamiquement
     * Set the rendering for columns
     * @param aRendering
     */
    public void initialiserColumnRendering(VObject data, Map<String, ColumnRendering> aRendering) {
    	data.getConstantVObject().setColumnRender(aRendering);
    }

    /**
     * Apply a rendering, even after an initialization query
     * So the result of the query can be interpreted to generate a particular rendering
     * Use method "initialiserColumnRendering" to set the column rendering
     * @param data
     * @param headersDLabel
     */
    public void applyColumnRendering(VObject data, ArrayList<String> headersDLabel )
    {
    	data.setHeadersVLabel(buildHeadersVLabel(data, headersDLabel));
        data.setHeadersVSize(buildHeadersVSize(data, headersDLabel));
        data.setHeadersVType(buildHeadersVType(data, headersDLabel));
        data.setHeadersVSelect(buildHeadersVSelect(data, headersDLabel));
        data.setHeadersVisible(buildHeadersVisible(data, headersDLabel));
        data.setHeadersUpdatable(buildHeadersUpdatable(data, headersDLabel));
        data.setHeadersRequired(buildHeadersRequired(data, headersDLabel));
    }
    
    /**
     * A vouaire
     */
    public void lock(VObject data) {
        for (String key : data.getConstantVObject().getColumnRender().keySet()) {
            data.getConstantVObject().getColumnRender().get(key).isUpdatable = false;
        }
    }

    /**
     * Génére les labels de colonnes. Si une déclaration est faite dans ConstantVObject, on met le label déclaré.
     * Sinon on garde le nom de colonne de la base de données
     */
    private ArrayList<String> buildHeadersVLabel(VObject data, ArrayList<String> headers) {
        ArrayList<String> headersVLabel = new ArrayList<>();
        for (int i = 0; i < headers.size(); i++) {
            if (data.getConstantVObject()//
                    .getColumnRender()//
                    .get(headers.get(i)) != null) {
                headersVLabel.add(data.getConstantVObject().getColumnRender().get(headers.get(i)).label);
            } else {
                headersVLabel.add(ManipString.translateAscii(headers.get(i)));
            }
        }
        return headersVLabel;
    }

    /**
     * Génére la taille de colonnes. Si une déclaration est faite dans ConstantVObject, on met la taille déclaré.
     * Sinon on ne met rien.
     */
    private ArrayList<String> buildHeadersVSize(VObject data, ArrayList<String> headers) {
        ArrayList<String> headersVSize = new ArrayList<>();
        for (int i = 0; i < headers.size(); i++) {
            if (data.getConstantVObject().getColumnRender().get(headers.get(i)) != null) {
                headersVSize.add(data.getConstantVObject().getColumnRender().get(headers.get(i)).size);
            } else {
                headersVSize.add("auto");
            }
        }
        return headersVSize;
    }

    /**
     * Génére le type de colonnes. Si une déclaration est faite dans ConstantVObject, on met le type déclaré.
     * Sinon on met text.
     */
    private ArrayList<String> buildHeadersVType(VObject data, ArrayList<String> headers) {
        ArrayList<String> headersVType = new ArrayList<>();
        for (int i = 0; i < headers.size(); i++) {
            if (data.getConstantVObject().getColumnRender().get(headers.get(i)) != null) {
                headersVType.add(data.getConstantVObject().getColumnRender().get(headers.get(i)).type);
            } else {
                headersVType.add("text");
            }
        }
        return headersVType;
    }

    /**
     * Génére la visibilité des colonnes. Si une déclaration est faite dans ConstantVObject, on met la visibilité déclarée.
     * Sinon on met visible par défaut.
     */
    private ArrayList<Boolean> buildHeadersVisible(VObject data, ArrayList<String> headers) {
        ArrayList<Boolean> headersVisible = new ArrayList<>();
        for (int i = 0; i < headers.size(); i++) {
            if (data.getConstantVObject().getColumnRender().get(headers.get(i)) != null) {
                headersVisible.add(data.getConstantVObject().getColumnRender().get(headers.get(i)).visible);
            } else {
                headersVisible.add(true);
            }
        }
        return headersVisible;
    }

    /**
     * Génére le caractère modifiable des colonnes. Si une déclaration est faite dans ConstantVObject,
     *  on met la valeur déclarée.
     * Sinon on met modifiable par défaut.
     */
    private ArrayList<Boolean> buildHeadersUpdatable(VObject data, ArrayList<String> headers) {
        ArrayList<Boolean> headersUpdatable = new ArrayList<>();
        for (int i = 0; i < headers.size(); i++) {
            if (data.getConstantVObject().getColumnRender().get(headers.get(i)) != null) {
                headersUpdatable.add(data.getConstantVObject().getColumnRender().get(headers.get(i)).isUpdatable);
            } else {
                headersUpdatable.add(true);
            }
        }
        return headersUpdatable;
    }

    /**
     * Génére le caractère obligatoire des colonnes. Si une déclaration est faite dans ConstantVObject,
     *  on met la valeur déclarée.
     * Sinon on met obligatoire par défaut.
     */
    private ArrayList<Boolean> buildHeadersRequired(VObject data, ArrayList<String> headers) {
        ArrayList<Boolean> headersRequired = new ArrayList<>();
        for (int i = 0; i < headers.size(); i++) {
            if (data.getConstantVObject().getColumnRender().get(headers.get(i)) != null) {
                headersRequired.add(data.getConstantVObject().getColumnRender().get(headers.get(i)).isRequired);
            } else {
                headersRequired.add(true);
            }
        }
        return headersRequired;
    }


    private ArrayList<LinkedHashMap<String, String>> buildHeadersVSelect(VObject data, ArrayList<String> headers) {
        ArrayList<ArrayList<String>> arrayVSelect = new ArrayList<>();
        ArrayList<LinkedHashMap<String, String>> headerVSelect = new ArrayList<>();
        for (int i = 0; i < headers.size(); i++) {
            if (data.getConstantVObject().getColumnRender().get(headers.get(i)) != null
                    && data.getConstantVObject().getColumnRender().get(headers.get(i)).query != null) {
                try {
                    arrayVSelect = UtilitaireDao.get(this.pool)
                            .executeRequest(null, data.getConstantVObject().getColumnRender().get(headers.get(i)).query);
                    arrayVSelect.remove(0);
                    arrayVSelect.remove(0);
                    LinkedHashMap<String, String> m = new LinkedHashMap<>();
                    for (int j = 0; j < arrayVSelect.size(); j++) {
                        m.put(arrayVSelect.get(j).get(0), arrayVSelect.get(j).get(1));
                    }
                    headerVSelect.add(m);
                } catch (ArcException ex) {
                    data.setMessage(ex.getMessage());
                    LoggerHelper.errorGenTextAsComment(getClass(), "buildHeadersVSelect()", LOGGER, ex);
                }
            } else {
                LinkedHashMap<String, String> m = new LinkedHashMap<>();
                headerVSelect.add(m);
            }
        }
        return headerVSelect;
    }

    /**
     * Remise à zéro des champs d'entrée avec les valeurs par défault
     */
    private ArrayList<String> eraseInputFields(ArrayList<String> headersDLabel,
            HashMap<String, String> defaultInputFields)
    {
        ArrayList<String> inputFields = new ArrayList<>();
        for (int i = 0; i < headersDLabel.size(); i++)
        {
            if (defaultInputFields.get(headersDLabel.get(i)) != null)
            {
                inputFields.add(defaultInputFields.get(headersDLabel.get(i)));
            }
            else
            {
                inputFields.add(null);
            }
        }
        return inputFields;
    }


    /**
     * On peut avoir envie d'insérer une valeur calculable de façon déterministe mais non renseignée dans les valeurs insérées.
     * {@link AttributeValue} définit des paires de {@link String} à cet effet.
     *
     * @param attributeValues
     */
    public boolean insert(VObject currentData, AttributeValue... attributeValues) {
        try {
            LoggerHelper.traceAsComment(LOGGER, "insert()", currentData.getSessionName());
            Map<String, String> map = new HashMap<>();
            Arrays.asList(attributeValues).forEach((t)->map.put(t.getFirst().toLowerCase(),t.getSecond()));            

            // Récupération des colonnes de la table cible
            List<String> nativeFieldList = (ArrayList<String>) UtilitaireDao.get(this.pool).getColumns(null, new ArrayList<>(), currentData.getTable());
            
            Boolean allNull = true;
            PreparedStatementBuilder reqInsert = new PreparedStatementBuilder();
            PreparedStatementBuilder reqValues = new PreparedStatementBuilder();
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
                            &&
                            map.containsKey(currentData.getHeadersDLabel().get(i).toLowerCase() )
                            ) {
                        insertValue =
                                map.get(currentData.getHeadersDLabel().get(i).toLowerCase());
                    } else if (currentData.getInputFields().get(i) != null && currentData.getInputFields().get(i).length() > 0) {
                        allNull = false;
                        insertValue = reqValues.quoteText(currentData.getInputFields().get(i))+ "::" + currentData.getHeadersDType().get(i);
                    } else {
                        insertValue = "null";
                    }
                    reqValues.append(insertValue);
                }
            }
            reqInsert.append(") ");
            reqValues.append("); ");
            PreparedStatementBuilder requete = new PreparedStatementBuilder();
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
                UtilitaireDao.get(this.pool).executeRequest(null, requete);
            }

        } catch (Exception ex) {
            LoggerHelper.error(LOGGER, ex);
            currentData.setMessage("Error in VObjectService.insert");
            return false;
        }
        return true;
    }
    
  

    /*
     *
     */
    public void delete(VObject currentData, String... tables) {
        LoggerHelper.traceAsComment(LOGGER, "delete()", currentData.getSessionName());
        VObject v0 = fetchVObjectData(currentData.getSessionName());

        try {
        ArrayList<String> listeColonneNative = (ArrayList<String>) UtilitaireDao.get(this.pool).getColumns(null, new ArrayList<>(), currentData.getTable());

        PreparedStatementBuilder reqDelete = new PreparedStatementBuilder();
        reqDelete.append("BEGIN; ");
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
								reqDelete.append("="+ reqDelete.quoteText(v0.getContent().get(i).d.get(j))+ "::" + v0.getHeadersDType().get(j));
                        } else {
                            reqDelete.append(" is null");
                        }
                    }
                }
                reqDelete.append("; ");
            }
        }
        reqDelete.append("END; ");
        UtilitaireDao.get(this.pool).executeRequest(null, reqDelete);
        } catch (ArcException ex) {
            LoggerHelper.error(LOGGER, ex);
        	currentData.setMessage("Error in VObjectService.delete");
        }
    }

    /**
     * Méthode de suppression spécifique lors de l'action de UPDATE du controle
     */
    public void deleteForUpdate(VObject currentData, String... tables) {
        LoggerHelper.debugAsComment(LOGGER, "deleteBeforeUpdate()", currentData.getSessionName());
        VObject v0 = fetchVObjectData(currentData.getSessionName());
        // comparaison des lignes dans la table avant et aprés
        // toBeUpdated contient l'identifiant des lignes à update
        ArrayList<Integer> toBeUpdated = new ArrayList<>();
        for (int i = 0; i < currentData.getContent().size(); i++) {
            int j = 0;
            boolean equals = true;
            while (j < currentData.getContent().get(i).d.size() && equals) {
                equals = ManipString.compareStringWithNull(v0.getContent().get(i).d.get(j), currentData.getContent().get(i).d.get(j));
                j++;
            }
            if (!equals) {
                toBeUpdated.add(i);
            }
        }
        LoggerHelper.traceAsComment(LOGGER, "toBeUpdated : ", toBeUpdated);
                
        PreparedStatementBuilder reqDelete = new PreparedStatementBuilder();
        reqDelete.append("BEGIN; ");
        for (int i = 0; i < v0.getContent().size(); i++) {
            if (toBeUpdated.contains(i)) {
                if (tables.length == 0) {
                    reqDelete.append("DELETE FROM " + v0.getTable() + " WHERE ");
                } else {
                    reqDelete.append("DELETE FROM " + tables[0] + " WHERE ");
                }
                for (int j = 0; j < v0.getHeadersDLabel().size(); j++) {
                    if (j > 0) {
                        reqDelete.append(" AND ");
                    }
                    reqDelete.append(v0.getHeadersDLabel().get(j));
                    if (v0.getContent().get(i).d.get(j) != null && v0.getContent().get(i).d.get(j).length() > 0) {
							reqDelete.append("=" + reqDelete.quoteText(v0.getContent().get(i).d.get(j)) + "::" + v0.getHeadersDType().get(j));
                    } else {
                        reqDelete.append(" is null");
                    }
                }
                reqDelete.append("; ");
            }
        }
        reqDelete.append("END; ");
        try {
            UtilitaireDao.get(this.pool).executeRequest(null, reqDelete);
        } catch (ArcException e) {
        	currentData.setMessage(e.getMessage());
        }
    }

    public void update(VObject currentData) {
        LoggerHelper.traceAsComment(LOGGER, "update()", currentData.getSessionName());
        VObject v0 = fetchVObjectData(currentData.getSessionName());
        // Compares new and old values line by line
        // Stocks the modified line number in toBeUpdated
        ArrayList<Integer> toBeUpdated = new ArrayList<>();
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
        ArrayList<String> nativeFieldsList = (ArrayList<String>) UtilitaireDao.get(this.pool).getColumns(null, new ArrayList<>(), currentData.getTable());

        // SQL update query
        PreparedStatementBuilder reqUpdate = new PreparedStatementBuilder();
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
                        //Serial type is set as int4
                    	String type = v0.getHeadersDType().get(j).equals("serial") ? "int4" : v0.getHeadersDType().get(j);
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
                    } else{
                    	String type = v0.getHeadersDType().get(j).equals("serial") ? "int4" : v0.getHeadersDType().get(j);
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
	                UtilitaireDao.get(this.pool).executeRequest(null, reqUpdate);
	            }
	            session.put(currentData.getSessionName(), v0);
        } catch (ArcException ex) {
            LoggerHelper.error(LOGGER, ex);
        	currentData.setMessage("Error in VObjectService.update");
        }
        
    }

    public PreparedStatementBuilder queryView(VObject currentData) {
    	VObject v0 = fetchVObjectData(currentData.getSessionName());
        if (currentData.getFilterFields() == null) {
            currentData.setFilterFields(v0.getFilterFields());
        }
        PreparedStatementBuilder requete = new PreparedStatementBuilder();
        requete.append("select alias_de_table.* from (");
        requete.append(v0.getMainQuery());
        requete.append(") alias_de_table ");
        requete.append(buildFilter(currentData.getFilterFields(), v0.getHeadersDLabel()));
        return requete;
    }

    /**
     * Renvoie le contenu d'une vue sous la forme d'une HashMap(nom de colonne, liste de valeur)
     *
     * @return
     */
    public HashMap<String, ArrayList<String>> mapView(VObject currentData) {
        HashMap<String, ArrayList<String>> result = new HashMap<>();
        try {
            GenericBean g = new GenericBean(UtilitaireDao.get(this.pool).executeRequest(null, queryView(currentData)));
            result = g.mapContent();
        } catch (ArcException ex) {
            LoggerHelper.errorGenTextAsComment(getClass(), "mapView()", LOGGER, ex);
        }
        return result;
    }

    public void destroy(VObject data) {
        data.clear();
        session.remove(data.getSessionName());
    }

    
    
    public PreparedStatementBuilder buildFilter(ArrayList<String> filterFields, ArrayList<String> headersDLabel) {
    return buildFilter(filterFields, headersDLabel, DEFAULT_FILTER_PATTERN, DEFAULT_FILTER_FUNCTION);
    }


    /**
     * Build the filter expression
     * @param filterFields
     * @param headersDLabel
     * @return
     */
    
    private PreparedStatementBuilder buildFilter(ArrayList<String> filterFields, ArrayList<String> headersDLabel, Integer filterPattern, String filterFunction) {

        Pattern patternMath = Pattern.compile("[<>=]");

        PreparedStatementBuilder s = new PreparedStatementBuilder(" WHERE true ");
        if (headersDLabel == null || filterFields == null) {
            return s;
        }

        for (int i = 0; i < filterFields.size(); i++) {
            if (filterFields.get(i) != null && !filterFields.get(i).equals("")) {

                if ((filterPattern == FILTER_LIKE_CONTAINS || filterPattern == FILTER_LIKE_ENDSWITH || filterPattern == FILTER_REGEXP_SIMILARTO)) {
                    s.append(" AND (");
                }
                
                /*
                 * Si on a un symbole mathématique
                 */
                Matcher matcher = patternMath.matcher(filterFields.get(i));
                if (matcher.find()) {
                    // On a au moins une fonction mathématique 2 cas, soit numérique, soit date. On sait que l'on a une date si
                    // le filtre contient §

                    if (filterFields.get(i).contains("§")) { // on a une date donc filtre du type format§condition
                        String filtre = filterFields.get(i);
                        String[] morceauReq = filtre.split("§");

                        // on découpe suivant les ET
                        String[] listeAND = morceauReq[1].split(FILTER_AND);

                        for (String conditionAND : listeAND) {
                            // on découpe suivant les OU
                            String[] listeOR = conditionAND.split(FILTER_OR);
                            for (String condtionOR : listeOR) {
                                s.append(" to_date(" + headersDLabel.get(i) + "::text, " + s.quoteText(morceauReq[0]) + ")"); // cast database column to the searched date format
                                s.append(condtionOR.trim().substring(0, 1)); // operator
                                s.append(" to_date(" + s.quoteText(condtionOR.trim().substring(1)) + "," + s.quoteText(morceauReq[0]) + ") "); // cast condition expression to the searched date format
                                s.append(" OR");
                            }
                            // on retire les dernier OR
                            s.setLength(s.length() - 3);
                            s.append(" AND");
                        }

                    } else { // on a des nombres
                        // on découpe suivant les ET
                        String[] listeAND = filterFields.get(i).split(FILTER_AND);

                        for (String conditionAND : listeAND) {
                            // on découpe suivant les OU
                            String[] listeOR = conditionAND.split(FILTER_OR);
                            for (String condtionOR : listeOR) {
                                if (condtionOR.contains("[")) { // cas ou on va chercher dans un vecteur

                                    condtionOR = condtionOR.trim();

                                    s.append(" (" + headersDLabel.get(i));
                                    
                                    s.append(condtionOR.substring(0,1)
                                    		+ "array_position("+headersDLabel.get(i-1)+","
                                    		+ s.quoteText(condtionOR.substring(1,condtionOR.indexOf("]")))
                                    		+ ")"
                                    		+ condtionOR.substring(condtionOR.indexOf("]"),condtionOR.indexOf("]")+1));
                                    
                                    s.append(condtionOR.substring(condtionOR.indexOf("]")+1));


                                } else {
                                    s.append(" (" + headersDLabel.get(i) + ")" + condtionOR);

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

                	
                	String toSearch="";
                	
                    if (filterPattern == FILTER_LIKE_CONTAINS || filterPattern == FILTER_LIKE_ENDSWITH) {
                        s.append(" " + filterFunction + "(" + headersDLabel.get(i) + "::text) LIKE ");
                    }

                    if (filterPattern == FILTER_REGEXP_SIMILARTO) {
                        s.append(" ' '||" + filterFunction + "(" + headersDLabel.get(i) + "::text) SIMILAR TO ");
                    }

                    // Si on a déjà un % dans le filtre on n'en rajoute pas
                    if ((filterPattern == FILTER_LIKE_CONTAINS || filterPattern == FILTER_REGEXP_SIMILARTO) && !filterFields.get(i).contains("%")) {
                    	toSearch+="%";
                    }

                    if (filterPattern == FILTER_LIKE_CONTAINS || filterPattern == FILTER_LIKE_ENDSWITH) {
                    	toSearch+=filterFields.get(i).toUpperCase();
                    }

                    if (filterPattern == FILTER_REGEXP_SIMILARTO) {
                        String aChercher = patternMather(filterFields.get(i).toUpperCase().trim());
                        toSearch+="( " + aChercher.replace(" ", "| ") + ")%";
                    }

                    if ((filterPattern == FILTER_LIKE_CONTAINS || filterPattern == FILTER_LIKE_ENDSWITH) && !filterFields.get(i).contains("%")) {
                    	toSearch+="%";
                    }
                    
                    s.append(s.quoteText(toSearch));
                    
                }
                s.append(") ");
            }
        }
        s.append(" ");
        return s;
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
     * @param headerSortLabels noms des colonnes
     * @param headerSortDOrders ordres du tri des colonnes
     * @return
     */
    public PreparedStatementBuilder buildOrderBy(ArrayList<String> headerSortLabels, ArrayList<Boolean> headerSortDOrders) {
        if (headerSortLabels == null) {
            return new PreparedStatementBuilder("order by alias_de_table ");
        }

        PreparedStatementBuilder s = new PreparedStatementBuilder();

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
    
    public PreparedStatementBuilder buildLimit(VObject data, Integer indexPage)
    {
    
	setPaginationSizeIfNull(data);

    return new PreparedStatementBuilder(" limit " + data.getPaginationSize() + " offset " + ((indexPage - 1) * data.getPaginationSize()));
    
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

    public void download(VObject currentData, HttpServletResponse response) {
    	VObject v0 = fetchVObjectData(currentData.getSessionName());
        if (currentData.getFilterFields() == null) {
            currentData.setFilterFields(v0.getFilterFields());
        }
        PreparedStatementBuilder requete = new PreparedStatementBuilder();
        requete
        	.append("select alias_de_table.* from (" )
        	.append(v0.getMainQuery())
        	.append(") alias_de_table ")
        	.append(buildFilter(currentData.getFilterFields(), v0.getHeadersDLabel()))
        	.append(buildOrderBy(v0.getHeaderSortDLabels(), v0.getHeaderSortDOrders()))
        	.append(", alias_de_table ")
        	;
        ArrayList<String> fileNames = new ArrayList<>();
        fileNames.add("Vue");
        this.download(currentData, response, fileNames, requete);
    }

    public void download(VObject currentData, HttpServletResponse response, List<String> fileNames, List<PreparedStatementBuilder> requetes) {
    	
    	
    	PreparedStatementBuilder[] array=new PreparedStatementBuilder[requetes.size()];
    	for (int i=0;i<requetes.size();i++)
    	{
    		array[i]=requetes.get(i);
    	}
    	
        download(currentData, response, fileNames, array);

    }

    /**
     * Téléchargement dans un zip de N fichiers csv, les données étant extraites de la base de données
     *
     * @param fileNames
     *            , liste des noms de fichiers obtenus
     * @param requetes
     *            , liste des requetes SQL
     */
    private void download(VObject currentData, HttpServletResponse response, List<String> fileNames, PreparedStatementBuilder... requetes) {
    	VObject v0 = fetchVObjectData(currentData.getSessionName());
        if (currentData.getFilterFields() == null) {
            currentData.setFilterFields(v0.getFilterFields());
        }
        Date dNow = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("yyyyMMddHHmmss");
        response.reset();
        response.setHeader("Content-Disposition", "attachment; filename=" + v0.getSessionName() + "_" + ft.format(dNow) + ".csv.zip");
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
    
    public void downloadValues(VObject currentData, HttpServletResponse response, List<String> fileNames, String... values) {
    	VObject v0 = fetchVObjectData(currentData.getSessionName());
        if (currentData.getFilterFields() == null) {
            currentData.setFilterFields(v0.getFilterFields());
        }
        Date dNow = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("yyyyMMddHHmmss");
        response.reset();
        response.setHeader("Content-Disposition", "attachment; filename=" + v0.getSessionName() + "_" + ft.format(dNow) + ".csv.zip");
        try {
            // Rattachement du zip à la réponse de Struts2
            ZipOutputStream zos = new ZipOutputStream(response.getOutputStream());
            try {
                for (int i = 0; i < fileNames.size(); i++) {
                    // Le nom des fichiers à l'interieur du zip seront simple :
                    // fichier1.csv, fichier2.csv etc.
                    // Ajout d'un nouveau fichier
                    ZipEntry entry = new ZipEntry(fileNames.get(i));
                    zos.putNextEntry(entry);
                    // Ecriture dans le fichier
                    zos.write(values[i].getBytes("UTF8"));
                    zos.closeEntry();
                }
            } finally {
                zos.close();
            }
        } catch (Exception ex) {
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
     * Téléchargement d'une liste de fichier qui sont stockés dans le dossier RECEPTION_OK ou RECEPTION_KO
     *
     * @param phase
     *
     * @param etatOk
     *
     * @param etatKo
     *
     * @param listIdSource
     */
    public void downloadXML(VObject currentData, HttpServletResponse response, PreparedStatementBuilder requete, String repertoire, String anEnvExcecution, String phase, String etatOk, String etatKo) {
    	VObject v0 = fetchVObjectData(currentData.getSessionName());
        if (currentData.getFilterFields() == null) {
            currentData.setFilterFields(v0.getFilterFields());
        }
        Date dNow = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("yyyyMMddHHmmss");
        response.reset();
        response.setHeader("Content-Disposition", "attachment; filename=" + v0.getSessionName() + "_" + ft.format(dNow) + ".tar.gz");

        TarArchiveOutputStream taos = null;
        try {
            taos = new TarArchiveOutputStream(new GZIPOutputStream(response.getOutputStream()));
            UtilitaireDao.get(this.pool).zipOutStreamRequeteSelect(null, requete, taos, repertoire, anEnvExcecution, phase, "ARCHIVE");
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
     * Télécharger en tar gzip une liste de fichier
     *
     * @param requete
     *            la selection de fichier avec la clé pertinente qui doit d'appeler nom_fichier
     * @param repertoire
     *            chemin jusqu'à l'avant dernier dossier
     * @param listRepertoire
     *            noms du dernier dossier (chaque fichier pouvant être dans l'un de la liste)
     */
    public void downloadEnveloppe(VObject currentData, HttpServletResponse response, PreparedStatementBuilder requete, String repertoire, ArrayList<String> listRepertoire) {
        VObject v0 = fetchVObjectData(currentData.getSessionName());

        if (currentData.getFilterFields() == null) {
            currentData.setFilterFields(v0.getFilterFields());
        }

        Date dNow = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("yyyyMMddHHmmss");
        response.reset();
        response.setHeader("Content-Disposition", "attachment; filename=" + v0.getSessionName() + "_" + ft.format(dNow) + ".tar");

        try (TarArchiveOutputStream taos = new TarArchiveOutputStream(response.getOutputStream());){ 
            UtilitaireDao.get(this.pool).getFilesDataStreamFromListOfInputDirectories(null, requete, taos, repertoire, listRepertoire);
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

    public void upload(VObject data, String repertoireCible) {
    	int nbUploaded = 0;
    	if (data.getFileUpload() != null) {
    		for (MultipartFile uploadedFile : data.getFileUpload()) {
    			String fileName = uploadedFile.getOriginalFilename();
    			// The file can be effectively empty (no name and no content)
				if (fileName != null && !fileName.isEmpty()) {
    				Path location = Paths.get(repertoireCible, fileName);
    				loggerDispatcher.info( "Upload >> " + location, LOGGER);
    				File newFile = location.toFile();
    				try {
    					if (newFile.exists()) {
    						Files.delete(newFile.toPath());
    					}
    					uploadedFile.transferTo(newFile);
    				} catch (IOException ex) {
    					LoggerHelper.errorGenTextAsComment(getClass(), "upload()", LOGGER, ex);
    				}
    			}
				nbUploaded++;
    		}
    	}
        data.setMessage("managementSandbox.load.success");
        data.setMessageArgs(nbUploaded);
    }

    public ArrayList<String> getHeaderSortDLabels(VObject currentData) {
        return currentData.getHeaderSortDLabels();
    }

    public void setHeaderSortDLabels(VObject currentData, ArrayList<String> headerSortLabels) {
        currentData.setHeaderSortDLabels(headerSortLabels);
    }
    
    public void addMessage(VObject data, String message) {
    	if(data.getMessage()!=null) {
    		data.setMessage(data.getMessage() + "\n"+ message);
    	}else {
    		data.setMessage(message);
    	}
    }

    /**
     * Calcule si la vue est active ou pas :
     * <ul>
     * <li>si on ne retrouve pas le nom de la vue dans le scope, on ne fait rien </li>
     * <li>si le nom de la vue est retrouvée dans le scope avec un moins devant, on desactive </li>
     * <li>si le nom de la vue est retrouvée dans le scope sans avoir un moins devant, on active </li>
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

    /**
     * @return the pool
     */
    public final String getPool() {
        return this.pool;
    }

    /**
     * @param pool
     *            the pool to set
     */
    public final void setPool(String pool) {
        this.pool = pool;
    }

    public final void setColumnRendering(VObject data, HashMap<String, ColumnRendering> columnRender) {
        data.setConstantVObject(new ConstantVObject(columnRender));
    }

    public void initializeByList(VObject data, ArrayList<ArrayList<String>> liste, HashMap<String, String> defaultInputFields) {

    	
    	PreparedStatementBuilder requete = new PreparedStatementBuilder();
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
					requete.append("" + requete.quoteText(liste.get(i).get(j)) + "::" + type.get(j) + " as " + header.get(j));
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
	public static void addRowToVObjectList(ArrayList<ArrayList<String>> result,String...elements)
	{
		result.add(new ArrayList<>(Arrays.asList(Arrays.copyOf(elements, elements.length))));
	}

	/**
     * Determine si un filtre existe
     *
     * @return
     */
    public Boolean filterExists(VObject data) {
        boolean filterExist = false;
        if (data.getFilterFields() != null && !data.getFilterFields().isEmpty()) {
            for (int i = 0; i < data.getFilterFields().size(); i++) {
                if (data.getFilterFields().get(i) != null && !data.getFilterFields().get(i).equals("")) {
                    filterExist = true;
                    break;
                }

            }
        }
        return filterExist;
    }

}
