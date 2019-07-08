  package fr.insee.arc.core.service.chargeur;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.opencsv.CSVReader;

import fr.insee.arc.core.archive_loader.FilesInputStreamLoad;
import fr.insee.arc.core.model.Norme;
import fr.insee.arc.core.model.TraitementState;
import fr.insee.arc.core.service.AbstractPhaseService;
import fr.insee.arc.core.service.thread.ThreadLoadService;
import fr.insee.arc.core.util.CustomTreeFormat;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.utils.LoggerDispatcher;

/**
 * Read CSV files.
 * 
 * First read headers then line by line
 * 
 * @author S4LWO8
 *
 */

public class LoaderCSV implements ILoader {
    private static final Logger LOGGER = Logger.getLogger(LoaderCSV.class);

    private String csvSeparator = ";";
    private String[] headers;
    private String fileName;
    private Connection connection;
    private String tablePilotageLoadTemp;
    private String currentPhase;
    private Norme currentNorme;
    private String tableTempA = "A";
    private String tableHardLoad = "B";
    private static final String TABLE_TEMP_T = "T";
    private String quote ="\"";
    private String jointure;
    private String validity;
    private InputStream streamHeader;
    private InputStream streamContent;

    public LoaderCSV(ThreadLoadService threadChargementService, FilesInputStreamLoad filesInputStreamLoad) {
        this.fileName = threadChargementService.getIdSource();
        this.connection = threadChargementService.getConnection();
        this.tablePilotageLoadTemp = threadChargementService.getTablePilTempThread();
        this.currentPhase = threadChargementService.getTokenInputPhaseName();
        this.currentNorme = threadChargementService.getNormeFile();
        this.streamContent =  filesInputStreamLoad.getTmpInxCSV();
        this.streamHeader = filesInputStreamLoad.getTmpInxLoad();
        this.validity = threadChargementService.getValidite();
    }

    public LoaderCSV() {
    }

    /**
     * Hard Load with postresql COPY
     * 
     * @throws Exception
     */

    public void csvToBase() throws Exception {
        LoggerDispatcher.info("** CSVtoBase **", LOGGER);

        java.util.Date beginDate = new java.util.Date();

        determineHeaders();
        
        streamHeader.close();

        initializeTable();

        copyFile();

        java.util.Date endDate = new java.util.Date();

        LoggerDispatcher.info("** CSVtoBase time**" + (endDate.getTime() - beginDate.getTime()) + " ms", LOGGER);

    }

    public void determineHeaders() throws IOException {
	try (InputStreamReader inputStreamReader = new InputStreamReader(this.streamHeader)) {
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            LoggerDispatcher.debug(String.format("contents delimiter %s",this.currentNorme.getRegleChargement().getDelimiter() ), LOGGER);
            LoggerDispatcher.debug(String.format("contents format %s",this.currentNorme.getRegleChargement().getFormat() ), LOGGER);
               
            this.csvSeparator = this.currentNorme.getRegleChargement().getDelimiter();
            CSVReader readerCSV = null;
            if (this.currentNorme.getRegleChargement().getFormat()!= null && this.currentNorme.getRegleChargement().getFormat().trim().length() == 1) {
        	quote = this.currentNorme.getRegleChargement().getFormat().trim();
        	LoggerDispatcher.debug(String.format("Need to escape %s",quote), LOGGER);
        	readerCSV = new CSVReader(bufferedReader, csvSeparator.charAt(0), quote.charAt(0));
        	
            } else {
        	readerCSV = new CSVReader(bufferedReader, csvSeparator.charAt(0));
            }

            this.headers=readHeaders(readerCSV);
            
        }
    }

    /**
     * Use the COPY function of postgresql to copy the data in base
     * @param QUOTE
     * @throws Exception
     */
    public void copyFile() throws  Exception {
	LoggerDispatcher.info("copyFile()", LOGGER);

        StringBuilder columnForCopy = new StringBuilder();
        columnForCopy.append("(");
        for (String nomCol : this.headers) {
            columnForCopy.append("" + nomCol + " ,");
        }
        columnForCopy.setLength(columnForCopy.length() - 1);
        columnForCopy.append(")");


        UtilitaireDao.get("arc").importing(connection, TABLE_TEMP_T, columnForCopy.toString(), streamContent, true, true, this.csvSeparator, quote);

        streamContent.close();
    }

    /**
     * Create the table
     * @throws SQLException
     */
    public void initializeTable() throws SQLException {
        StringBuilder req = new StringBuilder();
        req.append("DROP TABLE IF EXISTS " + TABLE_TEMP_T + " ;");
        req.append(" \nCREATE TEMPORARY TABLE " + TABLE_TEMP_T + " (");

        for (String nomCol : this.headers) {
            req.append("\n\t " + nomCol + " text,");
        }

        req.append("\n\t id SERIAL");
        req.append(");");

        UtilitaireDao.get("arc").executeImmediate(connection, req);
    }

    /**
     * @param inputStreaReader
     * @return
     * @throws IOException
     */
    public String getQuote(InputStreamReader inputStreaReader) throws IOException {
        String quote;
        if (inputStreaReader.read() == '"') {
            quote = "\"";

        } else {
            quote = null;
        }
        return quote;
    }

    /**
     * @param bufferedReader
     * @return 
     * @return
     * @throws IOException
     */
    public String[] readHeaders(CSVReader readerCSV) throws IOException {
        
        return readerCSV.readNext();
    }

    /**
     * Contrôle pour vérifier que les feuilles du format = les colonnes du csv
     * 
     * @param aFormat
     * @return
     * @deprecated
     */
    public List<String> controleFormat(CustomTreeFormat aFormat) {

        LoggerDispatcher.info("** ControleFormat **", LOGGER);

        List<String> colonneErreur = new ArrayList<>();
        int i = 0;

        while (i < headers.length) {
            if (!aFormat.getTheTree().keySet().contains(headers[i])) {
                colonneErreur.add(headers[i]);
            }
            i++;
        }

        return colonneErreur;
    }

    /**
     * Transformation de la base "plate" (celle issue de la copy du fichier) en base hierachique. La "hierachie" ce fait grâce aux colonnes
     * i_XX qui permettent de voir les bloc identiques
     * 
     * @param aFormat
     * @throws SQLException
     * @deprecated
     */
    public void flatBaseToHierarchicalBase(CustomTreeFormat aFormat) throws SQLException {
        LoggerDispatcher.info("** FlatBaseToHierarchicalBase **", LOGGER);

        StringBuilder req = new StringBuilder();

        // on récupère la validité

        req.append("with alias_table as (");
        req.append("select * from " + this.tableHardLoad);
        req.append(")");
        req.append(this.currentNorme.getDefValidite());
        String validite = UtilitaireDao.get("arc").executeRequest(this.connection, req).get(2).get(0);

        req.setLength(0);
        req.append("DROP TABLE IF EXISTS " + tableTempA + ";");
        req.append("CREATE ");
        if (!tableTempA.contains(".")) {
            req.append("TEMPORARY ");
        } else {
            req.append(" ");
        }

        req.append(" TABLE " + this.tableTempA);
        req.append(" AS (SELECT ");
        req.append("\n\t '" + this.fileName + "'::text collate \"C\" as id_source");
        req.append("\n\t ,id::integer as id");
        req.append("\n\t ,current_date::text collate \"C\" as date_integration ");
        req.append("\n\t ,'" + this.currentNorme.getIdNorme() + "'::text collate \"C\" as id_norme ");
        req.append("\n\t ,'" + this.currentNorme.getPeriodicite() + "'::text collate \"C\" as periodicite ");
        req.append("\n\t ,'" + validite + "'::text collate \"C\" as validite ");
        req.append("\n\t ,0::integer as nombre_colonne");

        req.append("\n\t , ");

        for (String feuille : aFormat.getEndLeaves()) {

            List<String> listePere = aFormat.getAncestors(feuille);

            req.append("DENSE_RANK() OVER (ORDER BY ");
            for (int i = listePere.size() - 1; i >= 0; i--) {
                req.append(listePere.get(i));

                if (i != 0) {
                    req.append(", ");
                }
            }
            
            // tcp
            req.append(") as i_" + feuille +"," + feuille + " as v_" + feuille + ",");

            
        }

        for (String branche : aFormat.getBranches()) {
            List<String> listePere = aFormat.getAncestors(branche);
            req.append("DENSE_RANK() OVER (ORDER BY ");
            for (int i = listePere.size() - 1; i >= 0; i--) {
                req.append(listePere.get(i));

                if (i == 0) {

                } else {
                    req.append(", ");
                }
            }

            // tcp
            req.append(") as i_" + branche + "," + branche + " as v_" + branche + ",");
            
        }
        req.setLength(req.length() - 1);
                
        req.append("\n FROM " + TABLE_TEMP_T + ")");

        UtilitaireDao.get("arc").executeImmediate(this.connection, req);

        StringBuilder requeteBilan = new StringBuilder();
        requeteBilan.append(AbstractPhaseService.pilotageMarkIdsource(this.tablePilotageLoadTemp, fileName, this.currentPhase, TraitementState.OK.toString(),
                null));

        UtilitaireDao.get("arc").executeBlock(this.connection, requeteBilan);

    }

    public void flatBaseToIdedFlatBase() throws SQLException {
        LoggerDispatcher.info("** FlatBaseToIdedFlatBase **", LOGGER);
        java.util.Date beginDate = new java.util.Date();

        StringBuilder req = new StringBuilder();

        // on récupère la validité

        req.setLength(0);
        req.append("DROP TABLE IF EXISTS " + tableTempA + ";");
        req.append("CREATE ");
        if (!tableTempA.contains(".")) {
            req.append("TEMPORARY ");
        } else {
            req.append(" ");
        }

        req.append(" TABLE " + this.tableTempA);
        req.append(" AS (SELECT ");
        req.append("\n\t '" + this.fileName + "'::text collate \"C\" as id_source");
        req.append("\n\t ,id::integer as id");
        req.append("\n\t ,current_date::text collate \"C\" as date_integration ");
        req.append("\n\t ,'" + this.currentNorme.getIdNorme() + "'::text collate \"C\" as id_norme ");
        req.append("\n\t ,'" + this.currentNorme.getPeriodicite() + "'::text collate \"C\" as periodicite ");
        req.append("\n\t ,'" + this.validity + "'::text collate \"C\" as validite ");
        req.append("\n\t ,0::integer as nombre_colonne");
        

        req.append("\n\t , ");

        for (int i = 0; i < this.headers.length; i++) {
            

            // tcp
        	req.append("id as i_" + headers[i]+", " + headers[i] + " as v_"+headers[i]+",");
        }

        req.setLength(req.length() - 1);


        req.append("\n FROM " + TABLE_TEMP_T + ")");

        UtilitaireDao.get("arc").executeImmediate(this.connection, req);


        StringBuilder requeteBilan = new StringBuilder();
        requeteBilan.append(AbstractPhaseService.pilotageMarkIdsource(this.tablePilotageLoadTemp, fileName, this.currentPhase, TraitementState.OK.toString(),
                null));

        UtilitaireDao.get("arc").executeBlock(this.connection, requeteBilan);

        
        java.util.Date endDate = new java.util.Date();
        LoggerDispatcher.info("** FlatBaseToIdedFlatBase temps**" + (endDate.getTime() - beginDate.getTime()) + " ms", LOGGER);

    }

    
    /**
     * Permet de générer la reuqête de jointure pour le normage.  Comme le normage va seulement retirer des colonnes, on fait une
     * jointure minimal : la table en sortie est la même qu'en entrée.
     * @deprecated
     */
    public void generateRequeteJointure() {
        LoggerDispatcher.info("generateRequeteJointure", LOGGER);

        // construction de la requete de jointure
        StringBuilder req = new StringBuilder();
        StringBuilder reqCreate = new StringBuilder(" \n");

        StringBuilder reqInsert = new StringBuilder();
        reqInsert.append(" INSERT INTO {table_destination} (id,id_source,date_integration,id_norme,validite,periodicite");

        StringBuilder reqSelect = new StringBuilder();
        reqSelect.append("\n SELECT row_number() over (), ww.* FROM (");
        reqSelect.append("\n SELECT '{nom_fichier}',current_date,'{id_norme}','{validite}','{periodicite}'");

        StringBuilder reqFrom = new StringBuilder();

        reqCreate.append("CREATE TEMPORARY TABLE t_" + this.headers[0] + " as (select i_" + this.headers[0] + " as m_" + this.headers[0] + ", ");
        reqCreate.append(" v_" + this.headers[0] + " as v_" + this.headers[0] + " ");
        reqSelect.append(",m_" + this.headers[0]);

        for (int i = 1; i < headers.length; i++) {
            reqCreate.append(", i_" + this.headers[i] + " as i_" + this.headers[i] + " ");
            reqCreate.append(", v_" + this.headers[i] + " as v_" + this.headers[i] + " ");

            reqSelect.append(", i_" + this.headers[i] + " , v_" + this.headers[i] + " ");
            reqInsert.append(", i_" + this.headers[i] + " , v_" + this.headers[i] + " ");

        }

        reqCreate.append("(SELECT i_" + this.headers[0] + ", min(v_" + this.headers[0] + ") as v_" + this.headers[0] + " ");
        for (int i = 1; i < headers.length; i++) {
            reqCreate.append(", min(i_" + this.headers[i] + ")  as i_" + this.headers[i] + " ");
            reqCreate.append(", min(v_" + this.headers[i] + ")  as v_" + this.headers[i] + " ");

        }

        reqCreate.append("FROM {table_source} where i_" + this.headers[0] + " is not null group by i_" + this.headers[0] + ") a );\n");

        reqCreate.append("CREATE TEMPORARY TABLE t_" + this.headers[0] + "_null as (select * from t_" + this.headers[0] + " where false);\n");

        reqInsert.append("\n )");

        reqFrom.insert(0, "\n FROM ");

        reqFrom.append("t_" + this.headers[0]);

        reqFrom.append("\n WHERE true ) ww ");

        // on ne met pas la parathenthèse fermantes exprées

        req.append(reqCreate);
        req.append(reqInsert);
        req.append(reqSelect);
        req.append(reqFrom);

        this.setJointure(req.toString().replace("'", "''"));

    }

    @Override
    public void initialisation() {
	LoggerDispatcher.debug("initialisation", LOGGER);

    }

    @Override
    public void finalisation() {
	LoggerDispatcher.debug("finalisation", LOGGER);

    }

    @Override
    public void excecution() throws Exception {
	LoggerDispatcher.info("excecution", LOGGER);
	csvToBase();

	flatBaseToIdedFlatBase();


    }

    @Override
    public void charger() throws Exception {
        initialisation();
        excecution();
        finalisation();

    }

    public String getJointure() {
	return jointure;
    }

    public void setJointure(String jointure) {
	this.jointure = jointure;
    }

    public String getCsvSeparator() {
        return csvSeparator;
    }

    public void setCsvSeparator(String csvSeparator) {
        this.csvSeparator = csvSeparator;
    }

    public String[] getHeaders() {
        return headers;
    }

    public void setHeaders(String[] headers) {
        this.headers = headers;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public String getTablePilotageLoadTemp() {
        return tablePilotageLoadTemp;
    }

    public void setTablePilotageLoadTemp(String tablePilotageLoadTemp) {
        this.tablePilotageLoadTemp = tablePilotageLoadTemp;
    }

    public String getCurrentPhase() {
        return currentPhase;
    }

    public void setCurrentPhase(String currentPhase) {
        this.currentPhase = currentPhase;
    }

    public Norme getCurrentNorme() {
        return currentNorme;
    }

    public void setCurrentNorme(Norme currentNorme) {
        this.currentNorme = currentNorme;
    }

    public String getTableTempA() {
        return tableTempA;
    }

    public void setTableTempA(String tableTempA) {
        this.tableTempA = tableTempA;
    }

    public String getTableHardLoad() {
        return tableHardLoad;
    }

    public void setTableHardLoad(String tableHardLoad) {
        this.tableHardLoad = tableHardLoad;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public String getValidity() {
        return validity;
    }

    public void setValidity(String validity) {
        this.validity = validity;
    }

    public InputStream getStreamHeader() {
        return streamHeader;
    }

    public void setStreamHeader(InputStream streamHeader) {
        this.streamHeader = streamHeader;
    }

    public InputStream getStreamContent() {
        return streamContent;
    }

    public void setStreamContent(InputStream streamContent) {
        this.streamContent = streamContent;
    }

}
