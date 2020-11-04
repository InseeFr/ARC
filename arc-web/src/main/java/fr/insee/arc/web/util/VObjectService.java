package fr.insee.arc.web.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
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
import fr.insee.arc.utils.dao.UtilitaireDao;
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

    private boolean noOrder = false;
    private boolean noCount = false;

    // filtering    
    private int filterPattern = 0;
    private String filterFunction = "upper";

    
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
			if (currentData.getPaginationSize() == 0) {
				currentData.setPaginationSize(v0.getPaginationSize());
			}
			if (currentData.getInputFields() != null && v0.getDefaultInputFields() != null) {
				for (int i = 0; i < v0.getHeadersDLabel().size(); i++) {
			        if (v0.getDefaultInputFields().get(v0.getHeadersDLabel().get(i)) != null) {
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
	public void initialize(VObject data, String mainQuery, String table, HashMap<String, String> defaultInputFields) {
		initialize(data, mainQuery, table, defaultInputFields, (content) -> content);
	}

	/**
	 *
	 * @param VObject data
	 * @param mainQuery ne doit pas se terminer par des {@code ;}
	 * @param table
	 * @param defaultInputFields
	 * @param reworkContent function to rewrite the fetched content
	 */
	public void initialize(VObject data, String mainQuery, String table, HashMap<String, String> defaultInputFields, 
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
	        StringBuilder requete = new StringBuilder();
	        requete.append("select alias_de_table.* from (" + mainQuery + ") alias_de_table " + buildFilter(data.getFilterFields(), data.getHeadersDLabel()));
	
	        if (this.noOrder == false) {
	            requete.append(buildOrderBy(data.getHeaderSortDLabels(), data.getHeaderSortDOrders()));
	        }
	
	        if (data.getPaginationSize() > 0) {
	            requete.append(" limit " + data.getPaginationSize() + " offset " + ((indexPage - 1) * data.getPaginationSize()));
	        }
	

	        ArrayList<ArrayList<String>> aContent = new ArrayList<>();
	        try {
	            aContent = reworkContent.apply(UtilitaireDao.get(this.pool).executeRequest(null, requete,  ModeRequete.IHM_INDEXED));
	        } catch (SQLException ex) {
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
	        data.setHeadersVLabel(buildHeadersVLabel(data, headersDLabel));
	        data.setHeadersVSize(buildHeadersVSize(data, headersDLabel));
	        data.setHeadersVType(buildHeadersVType(data, headersDLabel));
	        data.setHeadersVSelect(buildHeadersVSelect(data, headersDLabel));
	        data.setHeadersVisible(buildHeadersVisible(data, headersDLabel));
	        data.setHeadersUpdatable(buildHeadersUpdatable(data, headersDLabel));
	        data.setHeadersRequired(buildHeadersRequired(data, headersDLabel));
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

	private Integer pageManagement(String mainQuery, VObject currentData) {
		ArrayList<ArrayList<String>> aContent = new ArrayList<>();
		if (currentData.getIdPage() == null) {
		    currentData.setIdPage("1");
		}
		if (!this.noCount) {
		    if (currentData.getPaginationSize() > 0 && this.noOrder == false) {
		        try {
		            aContent = UtilitaireDao.get(this.pool).executeRequest(
		                    null,
		                    "select ceil(count(1)::float/" + currentData.getPaginationSize() + ") from (" + mainQuery + ") alias_de_table "
		                            + buildFilter(currentData.getFilterFields(), currentData.getHeadersDLabel()), ModeRequete.IHM_INDEXED);
		        } catch (SQLException ex) {
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
     *
     * @param aRendering
     */
    public void initialiserColumnRendering(VObject data, HashMap<String, ColumnRendering> aRendering) {
    	data.getConstantVObject().setColumnRender(aRendering);
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
    public ArrayList<String> buildHeadersVLabel(VObject data, ArrayList<String> headers) {
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
    public ArrayList<String> buildHeadersVSize(VObject data, ArrayList<String> headers) {
        ArrayList<String> headersVSize = new ArrayList<>();
        for (int i = 0; i < headers.size(); i++) {
            if (data.getConstantVObject().getColumnRender().get(headers.get(i)) != null) {
                headersVSize.add(data.getConstantVObject().getColumnRender().get(headers.get(i)).size);
            } else {
                headersVSize.add("/**/");
            }
        }
        return headersVSize;
    }

    /**
     * Génére le type de colonnes. Si une déclaration est faite dans ConstantVObject, on met le type déclaré.
     * Sinon on met text.
     */
    public ArrayList<String> buildHeadersVType(VObject data, ArrayList<String> headers) {
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
    public ArrayList<Boolean> buildHeadersVisible(VObject data, ArrayList<String> headers) {
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
    public ArrayList<Boolean> buildHeadersUpdatable(VObject data, ArrayList<String> headers) {
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
    public ArrayList<Boolean> buildHeadersRequired(VObject data, ArrayList<String> headers) {
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


    public ArrayList<LinkedHashMap<String, String>> buildHeadersVSelect(VObject data, ArrayList<String> headers) {
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
                } catch (SQLException ex) {
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
    public ArrayList<String> eraseInputFields(ArrayList<String> headersDLabel,
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
            StringBuilder reqInsert = new StringBuilder();
            StringBuilder reqValues = new StringBuilder();
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
                            
                            // attributeValues[j].getFirst().equalsIgnoreCase(v0.headersDLabel.get(i))
                            ) {
                        insertValue =
                                map.get(currentData.getHeadersDLabel().get(i).toLowerCase())
                                
                                //attributeValues[j].getSecond()
                                ;
                        //j++;
                    } else if (currentData.getInputFields().get(i) != null && currentData.getInputFields().get(i).length() > 0) {
                        allNull = false;
                        insertValue = "'" + currentData.getInputFields().get(i).replace("'", "''") + "'::" + currentData.getHeadersDType().get(i);
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
            if (currentData.getAfterInsertQuery() != null) {
                requete.append("\n" + currentData.getAfterInsertQuery() + "\n");
            }
            requete.append("END;");
            try {
                if (!allNull) {
                    UtilitaireDao.get(this.pool).executeRequest(null, requete.toString());
                }
            } catch (SQLException e) {
                currentData.setMessage(e.getMessage());
                return false;
            }
        } catch (Exception ex) {
            LoggerHelper.errorGenTextAsComment(getClass(), "insert()", LOGGER, ex);
            currentData.setMessage(ex.getMessage());
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

        ArrayList<String> listeColonneNative = (ArrayList<String>) UtilitaireDao.get(this.pool).getColumns(null, new ArrayList<>(), currentData.getTable());

        StringBuilder reqDelete = new StringBuilder();
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
                            reqDelete.append("='" + v0.getContent().get(i).d.get(j).replace("'", "''") + "'::" + v0.getHeadersDType().get(j));
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
        	currentData.setMessage(e.getMessage());
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
        StringBuilder reqDelete = new StringBuilder();
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
                        reqDelete.append("='" + v0.getContent().get(i).d.get(j).replace("'", "''") + "'::" + v0.getHeadersDType().get(j));
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
        
        ArrayList<String> nativeFieldsList = (ArrayList<String>) UtilitaireDao.get(this.pool).getColumns(null, new ArrayList<>(), currentData.getTable());

        // SQL update query
        StringBuilder reqUpdate = new StringBuilder();
        reqUpdate.append("BEGIN; ");

        for (int i = 0; i < toBeUpdated.size(); i++) {
            reqUpdate.append("\nUPDATE " + v0.getTable() + " SET ");
            boolean comma = false;

            int lineToBeUpdated = toBeUpdated.get(i);
			for (int j = 0; j < v0.getHeadersDLabel().size(); j++) {
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
                        reqUpdate.append(label + "='" + newValue.replace("'", "''") + "'::" + type);
                    }
                }
            }
            reqUpdate.append(" WHERE ");

            comma = false;
            for (int j = 0; j < v0.getHeadersDLabel().size(); j++) {
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
                        reqUpdate.append(label + "='" + oldValue.replace("'", "''") + "'::" + type);
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
            reqUpdate.append("\n" + v0.getAfterUpdateQuery() + "\n");
        }
        reqUpdate.append("END;");
        try {
            if (!toBeUpdated.isEmpty()) {
                UtilitaireDao.get(this.pool).executeRequest(null, "" + reqUpdate);
            }
            session.put(currentData.getSessionName(), v0);
        } catch (SQLException e) {
            currentData.setMessage(e.getMessage());
        }
    }

    public StringBuilder queryView(VObject currentData) {
    	VObject v0 = fetchVObjectData(currentData.getSessionName());
        if (currentData.getFilterFields() == null) {
            currentData.setFilterFields(v0.getFilterFields());
        }
        StringBuilder requete = new StringBuilder();
        requete.append("select alias_de_table.* from (" + v0.getMainQuery() + ") alias_de_table ");
        requete.append(buildFilter(currentData.getFilterFields(), v0.getHeadersDLabel()));
        // requete.append(buildOrderBy(v0.headerSortDLabels,
        // v0.headerSortDOrders));
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
        } catch (SQLException ex) {
            LoggerHelper.errorGenTextAsComment(getClass(), "mapView()", LOGGER, ex);
        }
        return result;
    }

    public void destroy(VObject data) {
        data.clear();
        session.remove(data.getSessionName());
    }

    public String buildFilter(ArrayList<String> filterFields, ArrayList<String> headersDLabel) {

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
                    // On a au moins une fonction mathématique 2 cas, soit numérique, soit date. On sait que l'on a une date si
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

                                condtionOR = condtionOR.trim().substring(0, 1) + "'" + condtionOR.trim().substring(1) + "'";
                                s.append(" to_date(" + headersDLabel.get(i) + ", '" + morceauReq[0] + "')" + condtionOR);

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

                                    String colonne = condtionOR.substring(0,1)
                                    		+ "array_position("+headersDLabel.get(i-1)+",'"
                                    		+ condtionOR.substring(1,condtionOR.indexOf("]"))
                                    		+ "')"
                                    		+ condtionOR.substring(condtionOR.indexOf("]"),condtionOR.indexOf("]")+1); // on prend le [X]

                                    condtionOR = condtionOR.substring(condtionOR.indexOf("]")+1);

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
     * @param headerSortLabels noms des colonnes
     * @param headerSortDOrders ordres du tri des colonnes
     * @return
     */
    public String buildOrderBy(ArrayList<String> headerSortLabels, ArrayList<Boolean> headerSortDOrders) {
        StringBuilder s = new StringBuilder();
        if (headerSortLabels == null) {
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
        String requete = "select alias_de_table.* from (" + v0.getMainQuery() + ") alias_de_table " + buildFilter(currentData.getFilterFields(), v0.getHeadersDLabel())
                + buildOrderBy(v0.getHeaderSortDLabels(), v0.getHeaderSortDOrders());
        ArrayList<String> fileNames = new ArrayList<>();
        fileNames.add("Vue");
        this.download(currentData, response, fileNames, requete);
    }

    public void download(VObject currentData, HttpServletResponse response, List<String> fileNames, List<String> requetes) {
        download(currentData, response, fileNames, requetes.toArray(new String[0]));

    }

    /**
     * Téléchargement dans un zip de N fichiers csv, les données étant extraites de la base de données
     *
     * @param fileNames
     *            , liste des noms de fichiers obtenus
     * @param requetes
     *            , liste des requetes SQL
     */
    public void download(VObject currentData, HttpServletResponse response, List<String> fileNames, String... requetes) {
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
    public void downloadXML(VObject currentData, HttpServletResponse response, String requete, String repertoire, String anEnvExcecution, String phase, String etatOk, String etatKo) {
    	VObject v0 = fetchVObjectData(currentData.getSessionName());
        if (currentData.getFilterFields() == null) {
            currentData.setFilterFields(v0.getFilterFields());
        }
        Date dNow = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("yyyyMMddHHmmss");
        response.reset();
        response.setHeader("Content-Disposition", "attachment; filename=" + v0.getSessionName() + "_" + ft.format(dNow) + ".tar.gz");
        // Rattachement du zip à la réponse de Struts2
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

    // 4MB buffer
    private static final byte[] BUFFER = new byte[4096 * 1024];

    /**
     * copy input to output stream - available in several StreamUtils or Streams classes
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
     *            la selection de fichier avec la clé pertinente qui doit d'appeler nom_fichier
     * @param repertoire
     *            chemin jusqu'à l'avant dernier dossier
     * @param listRepertoire
     *            noms du dernier dossier (chaque fichier pouvant être dans l'un de la liste)
     */
    public void downloadEnveloppe(VObject currentData, HttpServletResponse response, String requete, String repertoire, ArrayList<String> listRepertoire) {
        VObject v0 = fetchVObjectData(currentData.getSessionName());

        if (currentData.getFilterFields() == null) {
            currentData.setFilterFields(v0.getFilterFields());
        }

        Date dNow = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("yyyyMMddHHmmss");
        response.reset();
        response.setHeader("Content-Disposition", "attachment; filename=" + v0.getSessionName() + "_" + ft.format(dNow) + ".tar");

        try (TarArchiveOutputStream taos = new TarArchiveOutputStream(response.getOutputStream());){ 
            UtilitaireDao.get(this.pool).copieFichiers(null, requete, taos, repertoire, listRepertoire);
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

    public ArrayList<String> upload(VObject data, String repertoireCible, 
    		ArrayList<String> fileUploadFileName, ArrayList<MultipartFile> fileUpload) {
        if (fileUploadFileName != null) {
            for (int i = 0; i < fileUpload.size(); i++) {
                Path location = Paths.get(repertoireCible, fileUploadFileName.get(i));
                loggerDispatcher.info( "Upload >> " + location, LOGGER);
                File newFile = location.toFile();
                try {
	                if (newFile.exists()) {
	                	Files.delete(newFile.toPath());
	                }
	                fileUpload.get(i).transferTo(newFile);
                } catch (IOException ex) {
                    LoggerHelper.errorGenTextAsComment(getClass(), "upload()", LOGGER, ex);
                }
            }
        }
        data.setMessage("Upload terminé.");
        return fileUploadFileName;
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

    public int getFilterPattern() {
        return this.filterPattern;
    }

    public void initializeByList(VObject data, ArrayList<ArrayList<String>> liste, HashMap<String, String> defaultInputFields) {
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
                requete.append("'" + liste.get(i).get(j).replace("'", "''") + "'::" + type.get(j) + " as " + header.get(j));
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
        loggerDispatcher.debug(" initializeByList requete : " + requete.toString(), LOGGER);
        // on ne gere pas les autres cas: ca doit planter
        this.initialize(data, requete.toString(), data.getTable(), defaultInputFields);
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

    public Boolean getNoCount() {
		return noCount;
	}

	public void setNoCount(Boolean noCount) {
		this.noCount = noCount;
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
