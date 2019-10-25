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
import fr.insee.arc.utils.utils.ManipString;

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
    private String env;
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
        this.env=threadChargementService.getExecutionEnv();
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
     * restructure a flat file
     * @throws SQLException 
     * @throws Exception
     * @throws IOException
     */
    public void applyFormat() throws SQLException {
    	String format=currentNorme.getRegleChargement().getFormat();
    	if (format!=null && !format.isEmpty())
    	{
    		format=format.trim();
    		String[] lines=format.split("\n");
    		
    		ArrayList<String> cols=new ArrayList<String>();
    		ArrayList<String> exprs=new ArrayList<String>();
    		ArrayList<String> wheres=new ArrayList<String>();

    		ArrayList<String> joinTable=new ArrayList<String>();
    		ArrayList<String> joinType=new ArrayList<String>();
    		ArrayList<String> joinClause=new ArrayList<String>();
    		ArrayList<String> joinSelect=new ArrayList<String>();
    		ArrayList<String> partitionExpression=new ArrayList<String>();

    		for (String line:lines)
    		{
    			if (line.startsWith("<join-table>"))
    			{
    				joinTable.add(ManipString.substringAfterFirst(line,">").trim());
    			}
    			else if (line.startsWith("<join-type>"))
    			{
    				joinType.add(ManipString.substringAfterFirst(line,">").trim());
    			}
    			else if (line.startsWith("<join-clause>"))
    			{
    				joinClause.add(ManipString.substringAfterFirst(line,">").trim());
    			}
    			else if (line.startsWith("<join-select"))
    			{
    				joinSelect.add(ManipString.substringAfterFirst(line,">").trim());
    			}
    			else if (line.startsWith("<where>"))
    			{
    				wheres.add(ManipString.substringAfterFirst(line,">").trim());
    			}
    			else if (line.startsWith("<partition-expression>"))
    			{
    				partitionExpression.add(ManipString.substringAfterFirst(line,">").trim());
    			}
    			else if (line.startsWith("<encoding>"))
    			{
    			}
    			else if (line.startsWith("<headers>"))
    			{
    			}
    			else if (line.startsWith("<quote>"))
    			{
    			}
    			else if (line.startsWith("/*"))
    			{

    			}
    			else
    			{
    				cols.add(ManipString.substringBeforeFirst(line,"=").trim());
    				exprs.add(ManipString.substringAfterFirst(line,"=").trim());
    			}
    		}
    		
    		
    		/* jointure
    		 * 
    		 */
    		
    		StringBuilder addId=new StringBuilder();
    		StringBuilder req;
    	
    		if (!joinTable.isEmpty())
    		{
				req = new StringBuilder();

				req.append("\n DROP TABLE IF EXISTS TTT; ");
				req.append("\n CREATE TEMPORARY TABLE TTT AS ");

				// On renumérote les lignes après jointure pour etre cohérent
				req.append("\n SELECT  (row_number() over ())::int as id$new$, l.* ");
				for (int i = 0; i < joinTable.size(); i++) {
					req.append("\n , v"+i+".* ");
				}
				req.append("FROM  " + this.tableTempA + " l ");
				
    			
				for (int i = 0; i < joinTable.size(); i++) {
					// if schema precised in table name, keep it, if not , add execution schema to
					// tablename

					joinTable.set(i,
							joinTable.get(i).contains(".") ? joinTable.get(i) : this.env + "." + joinTable.get(i));

					// récupération des colonnes de la table
					List<String> colsIn = new ArrayList<String>();
					colsIn = UtilitaireDao.get("arc")
							.executeRequest(this.connection, "select "+joinSelect.get(i)+" from " + joinTable.get(i) + " limit 0").get(0);

					// join type
					req.append("\n " + joinType.get(i) + " ");

					req.append("\n (SELECT ");
					// build column name to be suitable to load process aka : i_col, v_col
					boolean start = true;
					for (int j = 0; j < colsIn.size(); j++) {
						if (start) {
							start = false;
						} else {
							req.append("\n ,");
						}

						req.append(
									"null::int as i_" + colsIn.get(j) + ", " + 
										colsIn.get(j) + " as v_" + colsIn.get(j) + " ");

					}
					req.append("\n FROM " + joinTable.get(i) + " ");
					req.append("\n ) v"+i+" ");
					req.append("\n ON " + joinClause.get(i) + " ");

				}
				req.append("\n ;");
				req.append("\n ALTER TABLE TTT DROP COLUMN id; ");
				req.append("\n ALTER TABLE TTT RENAME COLUMN id$new$ TO id; ");
				req.append("\n DROP TABLE " + this.tableTempA + ";");
				req.append("\n ALTER TABLE TTT RENAME TO " + this.tableTempA + ";");
				UtilitaireDao.get("arc").executeImmediate(connection, req);
			}
    		
            /*
			 * recalcule de colonnes
			 * si une colonne existe déjà, elle est écrasée
			 * sinon la nouvelle colonne est créée
			 */
    		if (!cols.isEmpty())
    		{
	            List<String> colsIn = new ArrayList <String>();
	            colsIn=UtilitaireDao.get("arc").executeRequest(this.connection, "select * from "+this.tableTempA+" limit 0").get(0);
	    		
	    		String renameSuffix="$new$";
	    		String partitionNumberPLaceHolder="#pn#";
	    		req=new StringBuilder();
	
	    		// Creation de la table
	    		req.append("\n DROP TABLE IF EXISTS TTT; ");
	    		req.append("\n CREATE TEMPORARY TABLE TTT AS ");
	    		req.append("\n SELECT w.* FROM ");
	    		req.append("\n (SELECT v.* ");
	    		for (int i=0;i<cols.size();i++)
	    		{
	    			// si on trouve dans l'expression le suffix alors on sait qu'on a voulu préalablement calculer la valeur
	    			if (exprs.get(i).contains(renameSuffix))
	    			{
		    			req.append("\n ,");
		    			req.append(exprs.get(i).replace(partitionNumberPLaceHolder,"0::bigint"));
		    			req.append(" as ");
		    			req.append(cols.get(i)+renameSuffix+" ");
	    			}
	    		}
	    		req.append("\n FROM ");
	    		req.append("\n (SELECT u.* ");
	    		for (int i=0;i<cols.size();i++)
	    		{
	    			if (!exprs.get(i).contains(renameSuffix))
	    			{
		    			req.append("\n ,");
		    			req.append(exprs.get(i).replace(partitionNumberPLaceHolder,"0::bigint"));
		    			req.append(" as ");
		    			req.append(cols.get(i)+renameSuffix+" ");
	    			}
	    		}
	    		req.append("\n FROM "+this.tableTempA+" u ) v ) w ");
	    		req.append("\n WHERE false ");
	    		for (String s : wheres)
	    		{
	    			req.append("\n AND "+s);
	    		}
	    		req.append(";");
	            UtilitaireDao.get("arc").executeImmediate(connection, req);

	    		
	    		// Itération
	    		
	    		// si pas de partition, nbIteration=1
	    		boolean partitionedProcess=(partitionExpression.size()>0);
	    		// default value 100000
	    		int partition_size=100000;
	    		
	    		
	    		int nbPartition=1;
	    		// creation de l'index de partitionnement
	    		if (partitionedProcess)
	    		{
	    			req=new StringBuilder();

	    			// comptage rapide su échantillon à 1/10000 pour trouver le nombre de partiton
	    			nbPartition=UtilitaireDao.get("arc").getInt(connection, "select ((count(*)*10000)/"+partition_size+")+1 from "+this.tableTempA+" tablesample system(0.01)");
	    			
	    			req=new StringBuilder();
		    		req.append("\n CREATE INDEX idx_a on "+this.tableTempA+" ((abs(hashtext("+partitionExpression.get(0)+"::text)) % "+nbPartition+"));");
		            UtilitaireDao.get("arc").executeImmediate(connection, req);
	    		}
	    		
	    		int nbIteration=nbPartition;
	    		
	    		for (int part=0;part<nbIteration;part++)
	    		{	
		    		req=new StringBuilder();
		    		req.append("\n INSERT INTO TTT ");
		    		req.append("\n SELECT w.* FROM ");
		    		req.append("\n (SELECT v.* ");
		    		for (int i=0;i<cols.size();i++)
		    		{
		    			// si on trouve dans l'expression le suffix alors on sait qu'on a voulu préalablement calculer la valeur
		    			if (exprs.get(i).contains(renameSuffix))
		    			{
			    			req.append("\n ,");
			    			req.append(exprs.get(i).replace(partitionNumberPLaceHolder,part+"000000000000::bigint"));
			    			req.append(" as ");
			    			req.append(cols.get(i)+renameSuffix+" ");
		    			}
		    		}
		    		req.append("\n FROM ");
		    		req.append("\n (SELECT u.* ");
		    		for (int i=0;i<cols.size();i++)
		    		{
		    			if (!exprs.get(i).contains(renameSuffix))
		    			{
			    			req.append("\n ,");
			    			req.append(exprs.get(i).replace(partitionNumberPLaceHolder,part+"000000000000::bigint"));
			    			req.append(" as ");
			    			req.append(cols.get(i)+renameSuffix+" ");
		    			}
		    		}
		    		req.append("\n FROM "+this.tableTempA+" u ");
		    		if (partitionedProcess)
		    		{
		    			req.append("\n WHERE abs(hashtext("+partitionExpression.get(0)+"::text)) % "+nbPartition+"="+part+" ");
		    		}
		    		req.append("\n ) v ) w ");
		    		req.append("\n WHERE true ");
		    		for (String s : wheres)
		    		{
		    			req.append("\n AND "+s);
		    		}
		    		req.append(";");
		            UtilitaireDao.get("arc").executeImmediate(connection, req);
	    		}
	    		
	    		req=new StringBuilder();
	    		for (int i=0;i<cols.size();i++)
	    		{
					
	    			if (colsIn.contains(cols.get(i)))
	    			{
	    				req.append("\n ALTER TABLE TTT DROP COLUMN "+cols.get(i)+";");
	    			}
	    			req.append("\n ALTER TABLE TTT RENAME COLUMN "+cols.get(i)+renameSuffix+" TO "+cols.get(i)+";");
	    		}
	    		
				req.append("\n DROP TABLE "+this.tableTempA+";");
				req.append("\n ALTER TABLE TTT RENAME TO "+this.tableTempA+";");
				
	            UtilitaireDao.get("arc").executeImmediate(connection, req);
    		}
    	}
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
    
    public void flatBaseToIdedFlatBase() throws SQLException {
        LoggerDispatcher.info("** FlatBaseToIdedFlatBase **", LOGGER);
        java.util.Date beginDate = new java.util.Date();

        StringBuilder req = new StringBuilder();
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


        req.append("\n FROM " + TABLE_TEMP_T + ");");
        req.append("DROP TABLE IF EXISTS " + TABLE_TEMP_T + ";");

        UtilitaireDao.get("arc").executeImmediate(this.connection, req);

        applyFormat();

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
