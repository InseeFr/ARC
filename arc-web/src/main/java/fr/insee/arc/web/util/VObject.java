package fr.insee.arc.web.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.FileUtils;
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
public class VObject {

    private static final Logger LOGGER = Logger.getLogger(VObject.class);

    private String pool;
    /** Titre de la fenêtre */
    private String title;
    /** Nom dans la session */
    private String sessionName;
    /** Nombre de lignes par page */
    private Integer paginationSize;
    /** Indicateur d'initialisation */
    private Boolean isInitialized;
    private Boolean isActive;
    private Boolean isScoped;
    private Boolean noOrder = false;
    private Boolean noCount = false;

    /** Requête de generation du tableau */
    private String mainQuery;

    private String beforeSelectQuery;
    private String afterUpdateQuery;
    private String afterInsertQuery;
    /** Table utilisée pour les update/insert/delete */
    private String table;
    private ArrayList<String> listeColonneNative;
    /** Rendering for this */
    protected ConstantVObject constantVObject;

    /** Ligne du tableau */
    public static class LineObject implements Cloneable, Iterable<String> {

        public LineObject() {
            super();
        }

        public static LineObject as(ArrayList<String> someData) {
            return new LineObject(someData);
        }

        private LineObject(ArrayList<String> aData) {
            super();
            this.d = aData;
        }

        /** Données de la ligne (par colonnes). */
        public ArrayList<String> d;

        public ArrayList<String> getD() {
            return this.d;
        }

        public void setD(ArrayList<String> aData) {
            this.d = aData;
        }

        @Override
		@SuppressWarnings("unchecked")
        public LineObject clone() {
            return new LineObject((ArrayList<String>) this.d.clone());
        }

        @Override
        public Iterator<String> iterator() {
            return this.d.iterator();
        }
    }

    /** Tableau du contenu de la requete (ligne, colonne) */
    public static class TableObject implements Cloneable, Iterable<LineObject> {

    	/** Lignes du tableau.*/
        public ArrayList<LineObject> t;

        public static TableObject as(ArrayList<ArrayList<String>> someContent) {
            TableObject returned = new TableObject(new ArrayList<LineObject>());
            for (int i = 0; i < someContent.size(); i++) {
                returned.add(LineObject.as(someContent.get(i)));
            }
            return returned;
        }

        public void add(LineObject aLineObject) {
            this.t.add(aLineObject);
        }

        public TableObject() {
            super();
        }

        private TableObject(ArrayList<LineObject> someContent) {
            this.t = someContent;
        }

        public ArrayList<LineObject> getT() {
            return this.t;
        }

        public void setT(ArrayList<LineObject> t) {
            this.t = t;
        }

        public int size() {
            return this.getT().size();
        }

        public LineObject get(int index) {
            return this.t.get(index);
        }

        @Override
		public TableObject clone() {
            ArrayList<LineObject> clonedContent = new ArrayList<LineObject>();
            for (int i = 0; i < this.t.size(); i++) {
                clonedContent.add(this.t.get(i).clone());
            }
            return new TableObject(clonedContent);
        }

        @Override
        public Iterator<LineObject> iterator() {
            return this.t.iterator();
        }

    }

    private TableObject content;
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


    // Gestion du tri
    /** Liste des colonnes utilisées pour trier la table */
    private ArrayList<String> headerSortDLabels;
    /** Liste des directions de tri (des colonnes nommées dans headerSortDLabels).
     *  true = ascending, false = descending. */
    private ArrayList<Boolean> headerSortDOrders;
    /** La colonne cliquée par l'utilisateur pour le tri.*/
    private String headerSortDLabel;

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
    // filtrage
    private ArrayList<String> filterFields;
    private int filterPattern = 0;
    private String filterFunction = "upper";

    // upload
    private ArrayList<File> fileUpload;
    private ArrayList<String> fileUploadFileName;
    private String message;

    public HashMap<String, String> customValues;
    public HashMap<String, HashMap<String, String>> mapCustomValues;

    /** État sauvegardé du VObject.*/
    public static class VObjectOld {
        /** Titre de la fenetre */
        public String title;
        /** Nom de session */
        public String sessionName;
        /** Nombre de lignes par page */
        public Integer paginationSize;
        /** Indicateur d'initialisation */
        public Boolean isInitialized;
        /** Requête de génération du tableau */
        public String mainQuery;

        public String beforeSelectQuery;
        public String afterUpdateQuery;
        public String afterInsertQuery;
        /** Table utilisée pour les update/insert/delete */
        public String table;
        /** Tableau du contenu de la requete (ligne, colonne) */
        public TableObject content;
        // nom des colonnes, type en base (D=Database), label dans la vue
        // (V=VUE) ,
        // taille sur la vue
        public ArrayList<String> headersDLabel;
        public ArrayList<String> headersDType;
        public ArrayList<String> headersVLabel;
        public ArrayList<String> headersVSize;
        public ArrayList<String> headersVType;
        public ArrayList<LinkedHashMap<String, String>> headersVSelect;
        public ArrayList<Boolean> headersVisible;
        // gestion du sort
        public ArrayList<String> headerSortDLabels;
        public ArrayList<Boolean> headerSortDOrders;
        public String headerSortDLabel;
        // tableau des lignes et des colonnes selectionnées
        public ArrayList<Boolean> selectedLines;
        public ArrayList<Boolean> selectedColumns;
        // champs de saisie
        public HashMap<String, String> defaultInputFields;
        public ArrayList<String> inputFields;
        // Pagination
        public Integer nbPages;
        public String idPage;
        // filtrage
        public ArrayList<String> filterFields;
        public ArrayList<Boolean> headersUpdatable;
        public ArrayList<Boolean> headersRequired;
    }

    public VObject() {
        super();
        this.customValues = new HashMap<String, String>();
        this.mapCustomValues = new HashMap<String, HashMap<String, String>>();
        this.isInitialized = false;
    }

    /**
     * Absolument indispensable dans le cas ou des tables de vues sont générées dynamiquement
     *
     * @param aRendering
     */
    public void initialiserColumnRendering(HashMap<String, ColumnRendering> aRendering) {
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

    /**
     * Génére les labels de colonnes. Si une déclaration est faite dans ConstantVObject, on met le label déclaré.
     * Sinon on garde le nom de colonne de la base de données
     */
    public ArrayList<String> buildHeadersVLabel(ArrayList<String> headers) {
        ArrayList<String> headersVLabel = new ArrayList<String>();
        for (int i = 0; i < headers.size(); i++) {
            if (this.getConstantVObject()//
                    .getColumnRender()//
                    .get(headers.get(i)) != null) {
                headersVLabel.add(this.constantVObject.getColumnRender().get(headers.get(i)).label);
            } else {
                headersVLabel.add(ManipString.translateAscii(headers.get(i)));
            }
        }
        return headersVLabel;
    }

    public ConstantVObject getConstantVObject() {
        return this.constantVObject;
    }

    /**
     * Génére la taille de colonnes. Si une déclaration est faite dans ConstantVObject, on met la taille déclaré.
     * Sinon on ne met rien.
     */
    public ArrayList<String> buildHeadersVSize(ArrayList<String> headers) {
        ArrayList<String> headersVSize = new ArrayList<String>();
        for (int i = 0; i < headers.size(); i++) {
            if (this.constantVObject.getColumnRender().get(headers.get(i)) != null) {
                headersVSize.add(this.constantVObject.getColumnRender().get(headers.get(i)).size);
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
    public ArrayList<String> buildHeadersVType(ArrayList<String> headers) {
        ArrayList<String> headersVType = new ArrayList<String>();
        for (int i = 0; i < headers.size(); i++) {
            if (this.constantVObject.getColumnRender().get(headers.get(i)) != null) {
                headersVType.add(this.constantVObject.getColumnRender().get(headers.get(i)).type);
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
    public ArrayList<Boolean> buildHeadersVisible(ArrayList<String> headers) {
        ArrayList<Boolean> headersVisible = new ArrayList<Boolean>();
        for (int i = 0; i < headers.size(); i++) {
            if (this.constantVObject.getColumnRender().get(headers.get(i)) != null) {
                headersVisible.add(this.constantVObject.getColumnRender().get(headers.get(i)).visible);
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
    public ArrayList<Boolean> buildHeadersUpdatable(ArrayList<String> headers) {
        ArrayList<Boolean> headersUpdatable = new ArrayList<Boolean>();
        for (int i = 0; i < headers.size(); i++) {
            if (this.constantVObject.getColumnRender().get(headers.get(i)) != null) {
                headersUpdatable.add(this.constantVObject.getColumnRender().get(headers.get(i)).isUpdatable);
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
    public ArrayList<Boolean> buildHeadersRequired(ArrayList<String> headers) {
        ArrayList<Boolean> headersRequired = new ArrayList<Boolean>();
        for (int i = 0; i < headers.size(); i++) {
            if (this.constantVObject.getColumnRender().get(headers.get(i)) != null) {
                headersRequired.add(this.constantVObject.getColumnRender().get(headers.get(i)).isRequired);
            } else {
                headersRequired.add(true);
            }
        }
        return headersRequired;
    }


    public ArrayList<LinkedHashMap<String, String>> buildHeadersVSelect(ArrayList<String> headers) {
        ArrayList<ArrayList<String>> arrayVSelect = new ArrayList<ArrayList<String>>();
        ArrayList<LinkedHashMap<String, String>> headerVSelect = new ArrayList<LinkedHashMap<String, String>>();
        for (int i = 0; i < headers.size(); i++) {
            if (this.constantVObject.getColumnRender().get(headers.get(i)) != null
                    && this.constantVObject.getColumnRender().get(headers.get(i)).query != null) {
                try {
                    arrayVSelect = UtilitaireDao.get(this.pool)
                            .executeRequest(null, this.constantVObject.getColumnRender().get(headers.get(i)).query);
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
     * Remise à zéro des champs d'entrée avec les valeurs par défault
     */
    public ArrayList<String> eraseInputFields(ArrayList<String> headersDLabel,
            HashMap<String, String> defaultInputFields)
    {
        ArrayList<String> inputFields = new ArrayList<String>();
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

    public static <T> ArrayList<T> copyList(ArrayList<T> source) {
        ArrayList<T> dest = new ArrayList<T>();
        for (T item : source) {
            dest.add(item);
        }
        return dest;
    }

    public ArrayList<ArrayList<String>> reworkContent(ArrayList<ArrayList<String>> z) {
        return z;
    }

    /**
     *
     * @param mainQuery ne doit pas se terminer par des {@code ;}
     * @param table
     * @param defaultInputFields
     */
    @SuppressWarnings("unchecked")
    public void initialize(String mainQuery, String table, HashMap<String, String> defaultInputFields) {

        try {
            LoggerHelper.debugAsComment(LOGGER, "initialize", this.sessionName);

            if (this.beforeSelectQuery != null) {
                UtilitaireDao.get(this.pool).executeRequest(null, this.beforeSelectQuery);
            }
            HttpSession session = ServletActionContext.getRequest().getSession(false);
            VObjectOld v0 = (VObjectOld) session.getAttribute(this.sessionName);
            if (v0 != null) {
                if (this.headerSortDLabel == null) {
                    setHeaderSortDLabel(v0.headerSortDLabel);
                }
                if (this.headerSortDLabels == null) {
                    setHeaderSortDLabels(v0.headerSortDLabels);
                }
                if (this.headerSortDOrders == null) {
                    setHeaderSortDOrders(v0.headerSortDOrders);
                }
                if (this.idPage == null) {
                    setIdPage(v0.idPage);
                }
                if (this.filterFields == null) {
                    setFilterFields(v0.filterFields);
                }
                if (this.headersDLabel == null) {
                    setHeadersDLabel(v0.headersDLabel);
                }
                if (this.selectedColumns == null) {
                    setSelectedColumns(v0.selectedColumns);
                }
            }

            if (this.idPage == null) {
                setIdPage("1");
            }

            // on sauvegarde le contenu des lignes selectionnées avant la nouvelle
            // execution de la requete
            HashMap<String, ArrayList<String>> selectedContent = mapContentSelected();
            ArrayList<ArrayList<String>> aContent = new ArrayList<ArrayList<String>>();
            ArrayList<String> headersDLabel = new ArrayList<String>();
            ArrayList<String> headersDType = new ArrayList<String>();
            // on sauvegarde les headers des colonnes selectionnées avant la
            // nouvelle
            // execution de la requete
            ArrayList<String> selectedHeaders = listHeadersSelected();

            // gestion du nombre de pages
            if (!this.noCount) {
	            if (this.paginationSize > 0 && this.noOrder == false) {
	                try {
	                    aContent = UtilitaireDao.get(this.pool).executeRequest(
	                            null,
	                            "select ceil(count(1)::float/" + this.paginationSize + ") from (" + mainQuery + ") alias_de_table "
	                                    + buildFilter(this.filterFields, this.headersDLabel), ModeRequete.IHM_INDEXED);
	                } catch (SQLException ex) {
	                    this.message = ex.getMessage();
	                    LoggerHelper.errorGenTextAsComment(getClass(), "initialize()", LOGGER, ex);
	                }
	                aContent.remove(0);
	                aContent.remove(0);
	                setNbPages(Integer.parseInt(aContent.get(0).get(0)));
	            } else {
	            	setNbPages(1);
	            }
            }

            try {
                Integer.parseInt(this.idPage);
            } catch (NumberFormatException e) {
                setIdPage(v0 == null ? "1" : v0.idPage);
            }

            Integer indexPage = Integer.parseInt(this.idPage);
            if (this.nbPages == 0) {
                this.nbPages = 1;
            }
            if (indexPage > this.nbPages) {
                setIdPage(this.nbPages.toString());
            }
            if (indexPage < 1) {
                setIdPage("1");
            }
            indexPage = Integer.parseInt(this.idPage);

            // lancement de la requete principale et recupération du tableau
            StringBuilder requete = new StringBuilder();
            requete.append("select alias_de_table.* from (" + mainQuery + ") alias_de_table " + buildFilter(this.filterFields, this.headersDLabel));

            if (this.noOrder == false) {
                requete.append(buildOrderBy(this.headerSortDLabels, this.headerSortDOrders));
            }

            if (this.paginationSize > 0) {
                requete.append(" limit " + this.paginationSize + " offset " + ((indexPage - 1) * this.paginationSize));
            }

            try {
                aContent = reworkContent(UtilitaireDao.get(this.pool).executeRequest(null, requete,  ModeRequete.IHM_INDEXED));
            } catch (SQLException ex) {
                this.message = ex.getMessage();
                LoggerHelper.errorGenTextAsComment(getClass(), "initialize()", LOGGER, ex);
            }
            if (aContent != null && aContent.size() > 0) {
                headersDLabel = aContent.remove(0);
                headersDType = aContent.remove(0);
            } else {
                headersDLabel = new ArrayList<String>();
                headersDType = new ArrayList<String>();
            }

            // on set l'objet
            setIsInitialized(true);
            setMainQuery(mainQuery);
            setTable(table);
            setHeadersDLabel(headersDLabel);
            setHeadersDType(headersDType);
            setHeadersVLabel(buildHeadersVLabel(headersDLabel));
            setHeadersVSize(buildHeadersVSize(headersDLabel));
            setHeadersVType(buildHeadersVType(headersDLabel));
            setHeadersVSelect(buildHeadersVSelect(headersDLabel));
            setHeadersVisible(buildHeadersVisible(headersDLabel));
            setHeadersUpdatable(buildHeadersUpdatable(headersDLabel));
            setHeadersRequired(buildHeadersRequired(headersDLabel));
            setContent(TableObject.as(aContent));
            setDefaultInputFields(defaultInputFields);
            setInputFields(eraseInputFields(headersDLabel, defaultInputFields));
            // on remet les attributs de telechargement de fichiers à null
            setFileUpload(null);
            setFileUploadFileName(null);
            // recalcule de la selection des lignes par rapport au contenu
            ArrayList<Boolean> selectedLines = new ArrayList<Boolean>();
            if (!selectedContent.isEmpty()) {
                for (int i = 0; i < this.content.size(); i++) {
                    int k = 0;
                    boolean equals = false;
                    while (k < selectedContent.get(this.headersDLabel.get(0)).size() && !equals) {
                        equals = true;
                        int j = 0;
                        while (j < this.content.get(i).d.size() && equals) {
                            // test si la colonne existe dans le contenu précédent;
                            // sinon on l'ignore
                            if (selectedContent.get(this.headersDLabel.get(j)) != null) {
                                equals = equals
                                        && ManipString.compareStringWithNull(this.content.get(i).d.get(j),
                                                selectedContent.get(this.headersDLabel.get(j)).get(k));
                            }
                            j++;
                        }
                        k++;
                    }
                    selectedLines.add(equals);
                }
            }
            setSelectedLines(selectedLines);
            // recalcule de la selection des colonnes
            ArrayList<Boolean> selectedColumns = new ArrayList<Boolean>();
            for (int i = 0; i < this.headersDLabel.size(); i++) {
                if (selectedHeaders.contains(this.headersDLabel.get(i))) {
                    selectedColumns.add(true);
                } else {
                    selectedColumns.add(false);
                }
            }
            setSelectedColumns(selectedColumns);

            // on cale l'objet en session
            if (v0 == null) {
                v0 = new VObjectOld();
            }
            // C'est laid mais je vois pas comment faire autrement. Java de merde :
            // tout cela pour une pauvre passage par valeur
            v0.title = this.title;
            v0.sessionName = this.sessionName;
            v0.paginationSize = this.paginationSize;
            v0.isInitialized = this.isInitialized;
            v0.mainQuery = this.mainQuery;
            v0.beforeSelectQuery = this.beforeSelectQuery;
            v0.afterUpdateQuery = this.afterUpdateQuery;
            v0.afterInsertQuery = this.afterInsertQuery;
            v0.table = this.table;
            if (this.content != null) {
                v0.content = this.content.clone();
            } else {
                v0.content = null;
            }
            if (this.headersDLabel != null) {
                v0.headersDLabel = (ArrayList<String>) this.headersDLabel.clone();
            } else {
                v0.headersDLabel = null;
            }
            if (this.headersDType != null) {
                v0.headersDType = (ArrayList<String>) this.headersDType.clone();
            } else {
                v0.headersDType = null;
            }
            if (this.headersVLabel != null) {
                v0.headersVLabel = (ArrayList<String>) this.headersVLabel.clone();
            } else {
                v0.headersVLabel = null;
            }
            if (this.headersVSize != null) {
                v0.headersVSize = (ArrayList<String>) this.headersVSize.clone();
            } else {
                v0.headersVSize = null;
            }
            if (this.headersVType != null) {
                v0.headersVType = (ArrayList<String>) this.headersVType.clone();
            } else {
                v0.headersVType = null;
            }
            if (this.headersVSelect != null) {
                v0.headersVSelect = (ArrayList<LinkedHashMap<String, String>>) this.headersVSelect.clone();
            } else {
                v0.headersVSelect = null;
            }
            if (this.headersVisible != null) {
                v0.headersVisible = (ArrayList<Boolean>) this.headersVisible.clone();
            } else {
                v0.headersVisible = null;
            }
            if (this.headersUpdatable != null) {
                v0.headersUpdatable = (ArrayList<Boolean>) this.headersUpdatable.clone();
            } else {
                v0.headersUpdatable = null;
            }
            if (this.headersRequired != null) {
                v0.headersRequired = (ArrayList<Boolean>) this.headersRequired.clone();
            } else {
                v0.headersRequired = null;
            }
            if (this.headerSortDLabels != null) {
                v0.headerSortDLabels = (ArrayList<String>) this.headerSortDLabels.clone();
            } else {
                v0.headerSortDLabels = null;
            }
            if (this.headerSortDOrders != null) {
                v0.headerSortDOrders = (ArrayList<Boolean>) this.headerSortDOrders.clone();
            } else {
                v0.headerSortDOrders = null;
            }
            v0.headerSortDLabel = this.headerSortDLabel;
            if (this.selectedLines != null) {
                v0.selectedLines = (ArrayList<Boolean>) this.selectedLines.clone();
            } else {
                v0.selectedLines = null;
            }
            if (this.selectedColumns != null) {
                v0.selectedColumns = (ArrayList<Boolean>) this.selectedColumns.clone();
            } else {
                v0.selectedColumns = null;
            }
            v0.headerSortDLabel = this.headerSortDLabel;
            if (this.defaultInputFields != null) {
                v0.defaultInputFields = (HashMap<String, String>) this.defaultInputFields.clone();
            } else {
                v0.defaultInputFields = null;
            }
            if (this.inputFields != null) {
                v0.inputFields = (ArrayList<String>) this.inputFields.clone();
            } else {
                v0.inputFields = null;
            }
            v0.nbPages = this.nbPages;
            v0.idPage = this.idPage;
            if (this.filterFields != null) {
                v0.filterFields = (ArrayList<String>) this.filterFields.clone();
            } else {
                v0.filterFields = null;
            }
            session.setAttribute(this.sessionName, v0);

        } catch (Exception ex) {
            LoggerHelper.errorGenTextAsComment(getClass(), "initialize()", LOGGER, ex);
        }

    }

    /**
     * On peut avoir envie d'insérer une valeur calculable de façon déterministe mais non renseignée dans les valeurs insérées.
     * {@link AttributeValue} définit des paires de {@link String} à cet effet.
     *
     * @param attributeValues
     */
    public boolean insert(AttributeValue... attributeValues) {
        try {
            LoggerHelper.traceAsComment(LOGGER, "insert()", this.sessionName);
            Map<String, String> map = new HashMap<>();
            Arrays.asList(attributeValues).forEach((t)->map.put(t.getFirst().toLowerCase(),t.getSecond()));
            HttpSession session = ServletActionContext.getRequest().getSession(false);
            VObjectOld v0 = (VObjectOld) session.getAttribute(this.sessionName);
            // on remet dans inputFields venant du client les valeurs par default
            // des input field
            for (int i = 0; i < v0.headersDLabel.size(); i++) {
                if (v0.defaultInputFields.get(v0.headersDLabel.get(i)) != null) {
                    this.inputFields.set(i, v0.defaultInputFields.get(v0.headersDLabel.get(i)));
                }
            }

            // Récupération des colonnes de la table cible
            this.listeColonneNative = (ArrayList<String>) UtilitaireDao.get(this.pool).getColumns(null, new ArrayList<String>(), this.table);

            Boolean allNull = true;
            StringBuilder reqInsert = new StringBuilder();
            StringBuilder reqValues = new StringBuilder();
            reqInsert.append("INSERT INTO " + v0.table + " (");
            reqValues.append("VALUES (");
            int j = 0;
            boolean comma = false;
            for (int i = 0; i < this.inputFields.size(); i++) {
                if (this.listeColonneNative.contains(v0.headersDLabel.get(i))) {
                    if (comma) {
                        reqInsert.append(",");
                        reqValues.append(",");
                    }
                    comma = true;
                    reqInsert.append(v0.headersDLabel.get(i));
                    String insertValue;
                    if (attributeValues != null && attributeValues.length > j
                            &&
                            map.containsKey(  v0.headersDLabel.get(i).toLowerCase() )
                            
                            // attributeValues[j].getFirst().equalsIgnoreCase(v0.headersDLabel.get(i))
                            ) {
                        insertValue =
                                map.get(v0.headersDLabel.get(i).toLowerCase())
                                
                                //attributeValues[j].getSecond()
                                ;
                        //j++;
                    } else if (this.inputFields.get(i) != null && this.inputFields.get(i).length() > 0) {
                        allNull = false;
                        insertValue = "'" + this.inputFields.get(i).replace("'", "''") + "'::" + v0.headersDType.get(i);
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
            if (v0.afterInsertQuery != null) {
                requete.append("\n" + v0.afterInsertQuery + "\n");
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

    /*
     *
     */
    public void delete(String... tables) {
        LoggerHelper.traceAsComment(LOGGER, "delete()", this.sessionName);
        HttpSession session = ServletActionContext.getRequest().getSession(false);
        VObjectOld v0 = (VObjectOld) session.getAttribute(this.sessionName);

        ArrayList<String> listeColonneNative = (ArrayList<String>) UtilitaireDao.get(this.pool).getColumns(null, new ArrayList<String>(), this.table);

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
                for (int j = 0; j < v0.headersDLabel.size(); j++) {
                    if (listeColonneNative.contains(v0.headersDLabel.get(j))) {
                        if (comma) {
                            reqDelete.append(" AND ");
                        }
                        comma = true;

                        reqDelete.append(v0.headersDLabel.get(j));

                        if (v0.content.get(i).d.get(j) != null && v0.content.get(i).d.get(j).length() > 0) {
                            reqDelete.append("='" + v0.content.get(i).d.get(j).replace("'", "''") + "'::" + v0.headersDType.get(j));
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
    public void deleteForUpdate(String... tables) {
        LoggerHelper.debugAsComment(LOGGER, "deleteBeforeUpdate()", this.sessionName);
        HttpSession session = ServletActionContext.getRequest().getSession(false);
        VObjectOld v0 = (VObjectOld) session.getAttribute(this.sessionName);
        // comparaison des lignes dans la table avant et aprés
        // toBeUpdated contient l'identifiant des lignes à update
        ArrayList<Integer> toBeUpdated = new ArrayList<Integer>();
        for (int i = 0; i < this.content.size(); i++) {
            int j = 0;
            boolean equals = true;
            while (j < this.content.get(i).d.size() && equals) {
                equals = ManipString.compareStringWithNull(v0.content.get(i).d.get(j), this.content.get(i).d.get(j));
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
                for (int j = 0; j < v0.headersDLabel.size(); j++) {
                    if (j > 0) {
                        reqDelete.append(" AND ");
                    }
                    reqDelete.append(v0.headersDLabel.get(j));
                    if (v0.content.get(i).d.get(j) != null && v0.content.get(i).d.get(j).length() > 0) {
                        reqDelete.append("='" + v0.content.get(i).d.get(j).replace("'", "''") + "'::" + v0.headersDType.get(j));
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

    public ArrayList<ArrayList<String>> listContentBeforeUpdate() {
        HttpSession session = ServletActionContext.getRequest().getSession(false);
        VObjectOld v0 = (VObjectOld) session.getAttribute(this.sessionName);
        if (v0 == null) {
            return new ArrayList<ArrayList<String>>();
        }
        ArrayList<ArrayList<String>> r = new ArrayList<ArrayList<String>>();
        // comparaison des lignes dans la table avant et aprés
        // toBeUpdated contient l'identifiant des lignes à update
        for (int i = 0; i < this.content.size(); i++) {
            int j = 0;
            boolean equals = true;
            while (j < this.content.get(i).d.size() && equals) {
                equals = ManipString.compareStringWithNull(v0.content.get(i).d.get(j), this.content.get(i).d.get(j));
                j++;
            }
            if (!equals) {
                r.add(v0.content.get(i).d);
            }
        }
        return r;
    }

    public ArrayList<ArrayList<String>> listContentAfterUpdate() {
        HttpSession session = ServletActionContext.getRequest().getSession(false);
        VObjectOld v0 = (VObjectOld) session.getAttribute(this.sessionName);
        if (v0 == null) {
            return new ArrayList<ArrayList<String>>();
        }
        ArrayList<ArrayList<String>> r = new ArrayList<ArrayList<String>>();
        // comparaison des lignes dans la table avant et aprés
        // toBeUpdated contient l'identifiant des lignes à update
        for (int i = 0; i < this.content.size(); i++) {
            int j = 0;
            boolean equals = true;
            while (j < this.content.get(i).d.size() && equals) {
                equals = ManipString.compareStringWithNull(v0.content.get(i).d.get(j), this.content.get(i).d.get(j));
                j++;
            }
            if (!equals) {
                r.add(this.content.get(i).d);
            }
        }
        return r;
    }

    public void update() {
        LoggerHelper.traceAsComment(LOGGER, "update()", this.sessionName);
        HttpSession session = ServletActionContext.getRequest().getSession(false);
        VObjectOld v0 = (VObjectOld) session.getAttribute(this.sessionName);
        // comparaison des lignes dans la table avant et aprés
        // toBeUpdated contient l'identifiant des lignes à update
        ArrayList<Integer> toBeUpdated = new ArrayList<Integer>();

        for (int i = 0; i < this.content.size(); i++) {
            int j = 0;
            boolean equals = true;
            while (j < this.content.get(i).d.size() && equals) {
                equals = ManipString.compareStringWithNull(v0.content.get(i).d.get(j), this.content.get(i).d.get(j));
                j++;
            }
            if (!equals) {
                toBeUpdated.add(i);
            }
        }
        
        ArrayList<String> listeColonneNative = (ArrayList<String>) UtilitaireDao.get(this.pool).getColumns(null, new ArrayList<String>(), this.table);

        // Construction de l'update
        StringBuilder reqUpdate = new StringBuilder();
        reqUpdate.append("BEGIN; ");

        for (int i = 0; i < toBeUpdated.size(); i++) {
            reqUpdate.append("\nUPDATE " + v0.table + " SET ");
            boolean comma = false;

            for (int j = 0; j < v0.headersDLabel.size(); j++) {
                /*
                 * Vérifions en premier lieu que la variable est bien une colonne de la table et non une variable de vue.
                 */
                if (listeColonneNative.contains(v0.headersDLabel.get(j))) {
                    if (comma) {
                        reqUpdate.append(" ,");
                    }
                    comma = true;

                    if (ManipString.isStringNull(this.content.get(toBeUpdated.get(i)).d.get(j))) {
                        reqUpdate.append(v0.headersDLabel.get(j) + "=NULL");
                    }

                    // serial
                    else if(v0.headersDType.get(j).equals("serial")){
                        //Si on a un serial on lui met un type int pour l'insertion
                        reqUpdate.append(v0.headersDLabel.get(j) + "='" + this.content.get(toBeUpdated.get(i)).d.get(j).replace("'", "''") + "'::int4"
                                );
                        
                    }else {
                        reqUpdate.append(v0.headersDLabel.get(j) + "='" + this.content.get(toBeUpdated.get(i)).d.get(j).replace("'", "''") + "'::"
                                + v0.headersDType.get(j));
                    }
                }
            }
            reqUpdate.append(" WHERE ");

            comma = false;
            for (int j = 0; j < v0.headersDLabel.size(); j++) {
                if (listeColonneNative.contains(v0.headersDLabel.get(j))) {
                    if (comma) {
                        reqUpdate.append(" AND ");
                    }
                    comma = true;

                    if (ManipString.isStringNull(v0.content.get(toBeUpdated.get(i)).d.get(j))) {
                        reqUpdate.append(v0.headersDLabel.get(j) + " IS NULL");
                    }
                    /*
                     * je me permet de toucher au VObject car je n'arrive pas à gérer cobnvenablement les objets de type SERIAL
                     */
                    else if(v0.headersDType.get(j).equals("serial")){
                        //Si on a un serial on lui met un type int pour l'insertion
                        reqUpdate.append(v0.headersDLabel.get(j) + "='" + v0.content.get(toBeUpdated.get(i)).d.get(j).replace("'", "''") + "'::int4"
                                );
                        
                    }else {
                        reqUpdate.append(v0.headersDLabel.get(j) + "='" + v0.content.get(toBeUpdated.get(i)).d.get(j).replace("'", "''") + "'::"
                                + v0.headersDType.get(j));
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
        VObjectOld v0 = (VObjectOld) session.getAttribute(this.sessionName);
        if (v0 == null) {
            return new HashMap<String, String>();
        }
        return new GenericBean(v0.headersDLabel, v0.headersDType, null).mapTypes();
    }

    public ArrayList<ArrayList<String>> listInputFields() {
        ArrayList<ArrayList<String>> r = new ArrayList<ArrayList<String>>();
        r.add(this.inputFields);
        return r;
    }

    public HashMap<String, ArrayList<String>> mapInputFields() {
        HttpSession session = ServletActionContext.getRequest().getSession(false);
        VObjectOld v0 = (VObjectOld) session.getAttribute(this.sessionName);
        return new GenericBean(v0.headersDLabel, v0.headersDType, listInputFields()).mapContent();
    }

    /** Modifie la valeur d'input pour la colonne demandée.
     * @param headerDLabel nom de la colonne en base
     * @param value valeur à attribuer
     * @throw IllegalArgumentException si le nom de colonne est invalide */
    public void setInputFieldFor(String headerDLabel, String value) {
    	int index = headersDLabel.indexOf(headerDLabel);
    	if (index == -1) {
    		throw new IllegalArgumentException("La colonne " + headerDLabel + " n'est pas trouvée.");
    	}
    	inputFields.set(index, value);
    }

    /** Retourne la valeur d'input pour la colonne demandée.
     * Forme abrégée de mapInputFields().get("ma_colonne").get(0).
     * @param headerDLabel nom de la colonne en base
     * @throw IllegalArgumentException si le nom de colonne est invalide */
    public String getInputFieldFor(String headerDLabel) {
    	int index = headersDLabel.indexOf(headerDLabel);
    	if (index == -1) {
    		throw new IllegalArgumentException("La colonne " + headerDLabel + " n'est pas trouvée.");
    	}
    	return mapInputFields().get(headerDLabel).get(0);
    }

    public ArrayList<ArrayList<String>> listLineContent(int i) {
        ArrayList<ArrayList<String>> r = new ArrayList<ArrayList<String>>();
        r.add(this.content.get(i).d);
        return r;
    }

    public HashMap<String, ArrayList<String>> mapLineContent(int i) {
        HttpSession session = ServletActionContext.getRequest().getSession(false);
        VObjectOld v0 = (VObjectOld) session.getAttribute(this.sessionName);
        return new GenericBean(v0.headersDLabel, v0.headersDType, listLineContent(i)).mapContent();
    }

    public HashMap<String, ArrayList<String>> mapContentBeforeUpdate(int i) {
        HttpSession session = ServletActionContext.getRequest().getSession(false);
        VObjectOld v0 = (VObjectOld) session.getAttribute(this.sessionName);
        ArrayList<ArrayList<String>> r = new ArrayList<ArrayList<String>>();
        r.add(listContentBeforeUpdate().get(i));
        return new GenericBean(v0.headersDLabel, v0.headersDType, r).mapContent();
    }

    public HashMap<String, ArrayList<String>> mapContentAfterUpdate(int i) {
        HttpSession session = ServletActionContext.getRequest().getSession(false);
        VObjectOld v0 = (VObjectOld) session.getAttribute(this.sessionName);
        ArrayList<ArrayList<String>> r = new ArrayList<ArrayList<String>>();
        r.add(listContentAfterUpdate().get(i));
        return new GenericBean(v0.headersDLabel, v0.headersDType, r).mapContent();
    }

    public ArrayList<ArrayList<String>> listContentSelected() {
        HttpSession session = ServletActionContext.getRequest().getSession(false);
        VObjectOld v0 = (VObjectOld) session.getAttribute(this.sessionName);
        if (v0 == null) {
            return new ArrayList<ArrayList<String>>();
        }
        ArrayList<ArrayList<String>> r = new ArrayList<ArrayList<String>>();
        // on récupère les lignes selectionnées
        // soit de this.selectedLines (du formulaire), soit de v0.selectedLines
        if (this.selectedLines == null) {
            this.selectedLines = v0.selectedLines;
        }
        // si rien dans la liste, return null
        if (this.selectedLines == null || this.selectedLines.size() == 0) {
            return r;
        }
        for (int j = 0; j < this.selectedLines.size(); j++) {
            if (this.selectedLines.get(j) != null && this.selectedLines.get(j)) {
                r.add(v0.content.get(j).d);
            }
        }
        if (r.size() == 0) {
            return null;
        }
        return r;
    }

    /**
     * Retourne une hash map qui pour chaque entete de colonne (clé), donne la liste de toutes les valeurs selectionnées
     */
    public HashMap<String, ArrayList<String>> mapContentSelected() {
        HttpSession session = ServletActionContext.getRequest().getSession(false);
        VObjectOld v0 = (VObjectOld) session.getAttribute(this.sessionName);
        if (v0 == null) {
            return new HashMap<String, ArrayList<String>>();
        }
        // on récupère les lignes selectionnées
        // soit de this.selectedLines (du formulaire), soit de v0.selectedLines
        if (this.selectedLines == null) {
            this.selectedLines = v0.selectedLines;
        }
        // si rien dans la liste, return null
        // if (this.selectedLines == null || this.selectedLines.size() == 0) {
        // return null;
        // }
        return new GenericBean(v0.headersDLabel, v0.headersDType, listContentSelected()).mapContent();
    }

    public ArrayList<ArrayList<String>> listContent() {
        HttpSession session = ServletActionContext.getRequest().getSession(false);
        VObjectOld v0 = (VObjectOld) session.getAttribute(this.sessionName);
        if (v0 == null) {
            return new ArrayList<ArrayList<String>>();
        }
        ArrayList<ArrayList<String>> c = new ArrayList<ArrayList<String>>();
        for (int i = 0; i < v0.content.size(); i++) {
            ArrayList<String> l = new ArrayList<String>();
            for (int j = 0; j < v0.content.get(i).d.size(); j++) {
                l.add(v0.content.get(i).d.get(j));
            }
            c.add(l);
        }
        return c;
    }

    /**
     * Retourne une hash map qui pour chaque entete de colonne (clé), donne la liste de toutes les valeurs
     */
    public HashMap<String, ArrayList<String>> mapContent() {
        HttpSession session = ServletActionContext.getRequest().getSession(false);
        VObjectOld v0 = (VObjectOld) session.getAttribute(this.sessionName);
        if (v0 == null) {
            return new HashMap<String, ArrayList<String>>();
        }
        return new GenericBean(v0.headersDLabel, v0.headersDType, listContent()).mapContent();
    }

    public HashMap<String, ArrayList<String>> mapFilterFields() {
        if (getFilterFields()==null)
        {
        	return new HashMap<String, ArrayList<String>>();
        }

    	ArrayList<ArrayList<String>> r = new ArrayList<ArrayList<String>>();
        r.add(getFilterFields());
        return new GenericBean(this.headersDLabel, this.headersDType, r).mapContent();
    }



    /**
     * Retourne la liste des entetes base de donnée selectionnés
     */
    public ArrayList<String> listHeadersSelected() {
        HttpSession session = ServletActionContext.getRequest().getSession(false);
        VObjectOld v0 = (VObjectOld) session.getAttribute(this.sessionName);
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
                r.add(v0.headersDLabel.get(i));
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
        VObjectOld v0 = (VObjectOld) session.getAttribute(this.sessionName);
        if (this.filterFields == null) {
            this.filterFields = v0.filterFields;
        }
        StringBuilder requete = new StringBuilder();
        requete.append("select alias_de_table.* from (" + v0.mainQuery + ") alias_de_table ");
        requete.append(buildFilter(this.filterFields, v0.headersDLabel));
        // requete.append(buildOrderBy(v0.headerSortDLabels,
        // v0.headerSortDOrders));
        return requete;
    }

    /**
     * Renvoie le contenu d'une vue sous la forme d'une HashMap(nom de colonne, liste de valeur)
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
    public void sort() {
        LoggerHelper.debugAsComment(LOGGER, "sort()");
        HttpSession session = ServletActionContext.getRequest().getSession(false);
        VObjectOld v0 = (VObjectOld) session.getAttribute(this.sessionName);
        if (v0.headersDLabel.indexOf(this.headerSortDLabel) != -1) {
            this.headerSortDLabels = v0.headerSortDLabels;
            this.headerSortDOrders = v0.headerSortDOrders;
            // on initialize si la liste n'existe pas
            if (this.headerSortDLabels == null) {
                this.headerSortDLabels = new ArrayList<String>();
                this.headerSortDOrders = new ArrayList<Boolean>();
            }
            int pos = this.headerSortDLabels.indexOf(this.headerSortDLabel);
            // si le champ a sort est en premiere position, on va inverser le
            // sens de l'order by
            if (pos == 0) {
                this.headerSortDOrders.set(0, !this.headerSortDOrders.get(0));
            }
            // si le champ est inconnu, on le met en premiere position avec un
            // sens asc
            else if (pos == -1) {
                this.headerSortDLabels.add(0, this.headerSortDLabel);
                this.headerSortDOrders.add(0, true);
            }
            // sinon on l'enleve de la liste existante et on le remet en
            // premiere position avec un sens inverse a celui d'avant
            else {
                this.headerSortDLabels.remove(pos);
                this.headerSortDOrders.remove(pos);
                this.headerSortDLabels.add(0, this.headerSortDLabel);
                this.headerSortDOrders.add(0, true);
            }
        }
    }

    public void download() {
        HttpSession session = ServletActionContext.getRequest().getSession(false);
        VObjectOld v0 = (VObjectOld) session.getAttribute(this.sessionName);
        if (this.filterFields == null) {
            this.filterFields = v0.filterFields;
        }
        String requete = "select alias_de_table.* from (" + v0.mainQuery + ") alias_de_table " + buildFilter(this.filterFields, v0.headersDLabel)
                + buildOrderBy(v0.headerSortDLabels, v0.headerSortDOrders);
        ArrayList<String> fileNames = new ArrayList<String>();
        fileNames.add("Vue");
        this.download(fileNames, requete);
    }

    public void download(List<String> fileNames, List<String> requetes) {
        download(fileNames, requetes.toArray(new String[0]));

    }

    /**
     * Téléchargement dans un zip de N fichiers csv, les données étant extraites de la base de données
     *
     * @param fileNames
     *            , liste des noms de fichiers obtenus
     * @param requetes
     *            , liste des requetes SQL
     */
    public void download(List<String> fileNames, String... requetes) {
        HttpSession session = ServletActionContext.getRequest().getSession(false);
        VObjectOld v0 = (VObjectOld) session.getAttribute(this.sessionName);
        if (this.filterFields == null) {
            this.filterFields = v0.filterFields;
        }
        Date dNow = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("yyyyMMddHHmmss");
        HttpServletResponse response = ServletActionContext.getResponse();
        response.reset();
        response.setHeader("Content-Disposition", "attachment; filename=" + v0.sessionName + "_" + ft.format(dNow) + ".csv.zip");
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
    
    public void downloadValues(List<String> fileNames, String... values) {
        HttpSession session = ServletActionContext.getRequest().getSession(false);
        VObjectOld v0 = (VObjectOld) session.getAttribute(this.sessionName);
        if (this.filterFields == null) {
            this.filterFields = v0.filterFields;
        }
        Date dNow = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("yyyyMMddHHmmss");
        HttpServletResponse response = ServletActionContext.getResponse();
        response.reset();
        response.setHeader("Content-Disposition", "attachment; filename=" + v0.sessionName + "_" + ft.format(dNow) + ".csv.zip");
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
    public void downloadXML(String requete, String repertoire, String anEnvExcecution, String phase, String etatOk, String etatKo) {
        HttpSession session = ServletActionContext.getRequest().getSession(false);
        VObjectOld v0 = (VObjectOld) session.getAttribute(this.sessionName);
        if (this.filterFields == null) {
            this.filterFields = v0.filterFields;
        }
        Date dNow = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("yyyyMMddHHmmss");
        HttpServletResponse response = ServletActionContext.getResponse();
        response.reset();
        response.setHeader("Content-Disposition", "attachment; filename=" + v0.sessionName + "_" + ft.format(dNow) + ".tar.gz");
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
    public void downloadEnveloppe(String requete, String repertoire, ArrayList<String> listRepertoire) {
        HttpSession session = ServletActionContext.getRequest().getSession(false);
        VObjectOld v0 = (VObjectOld) session.getAttribute(this.sessionName);

        if (this.filterFields == null) {
            this.filterFields = v0.filterFields;
        }

        Date dNow = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("yyyyMMddHHmmss");
        HttpServletResponse response = ServletActionContext.getResponse();
        response.reset();
        response.setHeader("Content-Disposition", "attachment; filename=" + v0.sessionName + "_" + ft.format(dNow) + ".tar");

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

    // Getters and setters
    public String getSessionName() {
        return this.sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public String getMainQuery() {
        return this.mainQuery;
    }

    public void setMainQuery(String mainQuery) {
        this.mainQuery = mainQuery;
    }

    public String getBeforeSelectQuery() {
        return this.beforeSelectQuery;
    }

    public void setBeforeSelectQuery(String beforeSelectQuery) {
        this.beforeSelectQuery = beforeSelectQuery;
    }

    public String getAfterUpdateQuery() {
        return this.afterUpdateQuery;
    }

    public void setAfterUpdateQuery(String afterUpdateQuery) {
        this.afterUpdateQuery = afterUpdateQuery;
    }

    public String getAfterInsertQuery() {
        return this.afterInsertQuery;
    }

    public void setAfterInsertQuery(String afterInsertQuery) {
        this.afterInsertQuery = afterInsertQuery;
    }

    public String getTable() {
        return this.table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public ArrayList<Boolean> getSelectedLines() {
        return this.selectedLines;
    }

    public void setSelectedLines(ArrayList<Boolean> selectedLines) {
        this.selectedLines = selectedLines;
    }

    public ArrayList<String> getInputFields() {
        return this.inputFields;
    }

    public void setInputFields(ArrayList<String> inputFields) {
        this.inputFields = inputFields;
    }

    public ArrayList<String> getHeadersDType() {
        return this.headersDType;
    }

    public void setHeadersDType(ArrayList<String> headersDType) {
        this.headersDType = headersDType;
    }

    public ArrayList<String> getHeadersVLabel() {
        return this.headersVLabel;
    }

    public void setHeadersVLabel(ArrayList<String> headersVLabel) {
        this.headersVLabel = headersVLabel;
    }

    public ArrayList<String> getHeadersVSize() {
        return this.headersVSize;
    }

    public void setHeadersVSize(ArrayList<String> headersVSize) {
        this.headersVSize = headersVSize;
    }

    public ArrayList<String> getHeadersDLabel() {
        return this.headersDLabel;
    }

    public void setHeadersDLabel(ArrayList<String> headersDLabel) {
        this.headersDLabel = headersDLabel;
    }

    public ArrayList<String> getHeadersVType() {
        return this.headersVType;
    }

    public void setHeadersVType(ArrayList<String> headersVType) {
        this.headersVType = headersVType;
    }

    public ArrayList<LinkedHashMap<String, String>> getHeadersVSelect() {
        return this.headersVSelect;
    }

    public void setHeadersVSelect(ArrayList<LinkedHashMap<String, String>> headersVSelect) {
        this.headersVSelect = headersVSelect;
    }

    public boolean getIsInitialized() {
        return this.isInitialized;
    }

    public void setIsInitialized(Boolean isInitialized) {
        this.isInitialized = isInitialized;
    }

    public ArrayList<Boolean> getHeadersVisible() {
        return this.headersVisible;
    }

    public void setHeadersVisible(ArrayList<Boolean> headersVisible) {
        this.headersVisible = headersVisible;
    }

    public HashMap<String, String> getDefaultInputFields() {
        return this.defaultInputFields;
    }

    public void setDefaultInputFields(HashMap<String, String> defaultInputFields) {
        this.defaultInputFields = defaultInputFields;
    }

    public ArrayList<String> getHeaderSortDLabels() {
        return this.headerSortDLabels;
    }

    public void setHeaderSortDLabels(ArrayList<String> headerSortLabels) {
        this.headerSortDLabels = headerSortLabels;
    }

    public ArrayList<Boolean> getHeaderSortDOrders() {
        return this.headerSortDOrders;
    }

    public void setHeaderSortDOrders(ArrayList<Boolean> headerSortDOrders) {
        this.headerSortDOrders = headerSortDOrders;
    }

    public String getHeaderSortDLabel() {
        return this.headerSortDLabel;
    }

    public void setHeaderSortDLabel(String headerSortDLabel) {
        this.headerSortDLabel = headerSortDLabel;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getNbPages() {
        return this.nbPages;
    }

    public void setNbPages(Integer nbPages) {
        this.nbPages = nbPages;
    }

    public String getIdPage() {
        return this.idPage;
    }

    public void setIdPage(String idPage) {
        this.idPage = idPage;
    }

    public ArrayList<String> getFilterFields() {
        return this.filterFields;
    }

    public void setFilterFields(ArrayList<String> filterFields) {
        this.filterFields = filterFields;
    }

    public ArrayList<Boolean> getSelectedColumns() {
        return this.selectedColumns;
    }

    public void setSelectedColumns(ArrayList<Boolean> selectedColumns) {
        this.selectedColumns = selectedColumns;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
    public void addMessage(String message) {
    	if(this.message!=null) {
    		this.message += "\n"+ message;
    	}else {
    		setMessage(message);
    	}
    }

    public Integer getPaginationSize() {
        return this.paginationSize;
    }

    public void setPaginationSize(Integer paginationSize) {
        this.paginationSize = paginationSize;
    }

    public ArrayList<File> getFileUpload() {
        return this.fileUpload;
    }

    public void setFileUpload(ArrayList<File> fileUpload) {
        this.fileUpload = fileUpload;
    }

    public ArrayList<String> getFileUploadFileName() {
        return this.fileUploadFileName;
    }

    public void setFileUploadFileName(ArrayList<String> fileUploadFileName) {
        this.fileUploadFileName = fileUploadFileName;
    }

    /**
     * @return the headersUpdatable
     */
    public ArrayList<Boolean> getHeadersUpdatable() {
        return this.headersUpdatable;
    }

    /**
     * @param headersUpdatable
     *            the headersUpdatable to set
     */
    public void setHeadersUpdatable(ArrayList<Boolean> headersUpdatable) {
        this.headersUpdatable = headersUpdatable;
    }

    public ArrayList<Boolean> getHeadersRequired() {
        return this.headersRequired;
    }

    public void setHeadersRequired(ArrayList<Boolean> headersRequired) {
        this.headersRequired = headersRequired;
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
        ArrayList<String> h = new ArrayList<String>();
        for (int i = 0; i < content.t.size(); i++) {
            h.add(content.t.get(i).d.get(j));
        }
        return h;
    }

    public TableObject getContent() {
        return this.content;
    }

    public void setContent(TableObject content) {
        this.content = content;
    }

    public Boolean getIsActive() {
        return this.isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getIsScoped() {
        return this.isScoped;
    }

    public void setIsScoped(Boolean isScoped) {
        this.isScoped = isScoped;
    }

    /**
     * Calcule si la vue est active ou pas :
     * <ul>
     * <li>si le parametre d'activation est a null, on le met à faux </li>
     * <li>si le parametre scope est null, on ne fait rien </li>
     * <li>si on ne retrouve pas le nom de la vue dans le scope, on ne fait rien </li>
     * <li>si le nom de la vue est retrouvée dans le scope avec un moins devant, on desactive </li>
     * <li>si le nom de la vue est retrouvée dans le scope sans avoir un moins devant, on active </li>
     * </ul>
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

    public HashMap<String, String> getCustomValues() {
        return this.customValues;
    }

    public void setCustomValues(HashMap<String, String> customValues) {
        this.customValues = customValues;
    }

    public HashMap<String, ArrayList<String>> mapContentAfterUpdate() {
        HttpSession session = ServletActionContext.getRequest().getSession(false);
        VObjectOld v0 = (VObjectOld) session.getAttribute(this.sessionName);
        return new GenericBean(v0.headersDLabel, v0.headersDType, listContentAfterUpdate()).mapContent();
    }

    public HashMap<String, ArrayList<String>> mapContentBeforeUpdate() {
        HttpSession session = ServletActionContext.getRequest().getSession(false);
        VObjectOld v0 = (VObjectOld) session.getAttribute(this.sessionName);
        return new GenericBean(v0.headersDLabel, v0.headersDType, listContentBeforeUpdate()).mapContent();
    }

	/**
     * @return the listeColonneNative
     */
    public final ArrayList<String> getListeColonneNative() {
        return this.listeColonneNative;
    }

    /**
     * @param listeColonneNative
     *            the listeColonneNative to set
     */
    public final void setListeColonneNative(ArrayList<String> listeColonneNative) {
        this.listeColonneNative = listeColonneNative;
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

    /**
     * @param constantVObject
     *            the constantVObject to set
     */
    public final void setConstantVObject(ConstantVObject constantVObject) {
        this.constantVObject = constantVObject;
    }

    public final void setColumnRendering(HashMap<String, ColumnRendering> columnRender) {
        this.setConstantVObject(new ConstantVObject(columnRender));
    }

    public HashMap<String, HashMap<String, String>> getMapCustomValues() {
        return this.mapCustomValues;
    }

    public void setMapCustomValues(HashMap<String, HashMap<String, String>> mapCustomValues) {
        this.mapCustomValues = mapCustomValues;
    }

    public int getFilterPattern() {
        return this.filterPattern;
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
     * @param aNomCol
     * @return
     */
    public int getNumeroDLabel(String aNomCol) {
        LoggerHelper.debug(LOGGER, "aNomCol", aNomCol);
        for(int i =0; i< this.headersDLabel.size();i++){
            LoggerHelper.debug(LOGGER, "this.headersDLabel.get(i)", this.headersDLabel.get(i));
            if(this.headersDLabel.get(i).equals(aNomCol)){
                return i;
            }
        }
        return -1;
    }
    
   
}
