package fr.insee.arc.core.service.engine.chargeur;

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

import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.service.ApiService;
import fr.insee.arc.core.service.thread.ThreadChargementService;
import fr.insee.arc.core.util.ArbreFormat;
import fr.insee.arc.core.util.Norme;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.utils.LoggerDispatcher;
import fr.insee.arc.utils.utils.ManipString;

/**
 * Classe pour charger les fichier CSV. On lit les headers du fichier qu'on va sauvegarder. Puis ligne par ligne on va parser le fichier et
 * le transformer en xml. Attention on va diviser le fichier en entrée en plusieurs fichier en sortie si necessaire.
 * 
 * @author S4LWO8
 *
 */

public class ChargeurCSV implements IChargeur {
    private static final Logger LOGGER = Logger.getLogger(ChargeurCSV.class);

    private String separateur = ";";
    private String encoding=null;
    private String quote =null;
    private String userDefinedHeaders=null;
    private String[] headers;
    private String fileName;
    private Connection connexion;
    private String tableChargementPilTemp;
    private String currentPhase;
    private Norme norme;
    private String tableTempA;
    private String tableChargementBrutal;
    private static final String TABLE_TEMP_T = "T";
    private String jointure;
    private InputStream streamHeader;
    private InputStream streamContent;
    private String env;
    private String validite;

    public ChargeurCSV(ThreadChargementService threadChargementService, String fileName) {
        this.fileName = fileName;
        this.connexion = threadChargementService.getConnexion();
        this.tableChargementBrutal = threadChargementService.getTableChargementBrutal();
        this.tableTempA = threadChargementService.getTableTempA();
        this.tableChargementPilTemp = threadChargementService.getTableChargementPilTemp();
        this.currentPhase = threadChargementService.getCurrentPhase();
        this.streamContent =  threadChargementService.filesInputStreamLoad.getTmpInxCSV();
        this.streamHeader = threadChargementService.filesInputStreamLoad.getTmpInxChargement();
        this.env=threadChargementService.getEnvExecution();
        this.norme = threadChargementService.normeOk;
        this.validite = threadChargementService.validite;
    }

    public ChargeurCSV() {
    }

    /**
     * Ajout du 26/09/2017
     * 
     * Charger le csv directement en base avec COPY, on s'occupera des i et v plus tard
     * 
     * @throws Exception
     */

    public void csvtoBase() throws Exception {
        LoggerDispatcher.info("** CSVtoBase **", LOGGER);

        java.util.Date beginDate = new java.util.Date();

        
        LoggerDispatcher.debug(String.format("contenu delimiter %s",norme.getRegleChargement().getDelimiter() ), LOGGER);
        LoggerDispatcher.debug(String.format("contenu format %s",norme.getRegleChargement().getFormat() ), LOGGER);
        
        
        this.separateur = norme.getRegleChargement().getDelimiter().trim();
        
        if (norme.getRegleChargement().getFormat()!=null && norme.getRegleChargement().getFormat().contains("<encoding>"))
        {
        	this.encoding=ManipString.substringBeforeFirst(ManipString.substringAfterFirst(norme.getRegleChargement().getFormat(),"<encoding>"),"</encoding>");
        }
        if (norme.getRegleChargement().getFormat()!=null && norme.getRegleChargement().getFormat().contains("<headers>"))
        {
        	this.userDefinedHeaders=ManipString.substringBeforeFirst(ManipString.substringAfterFirst(norme.getRegleChargement().getFormat(),"<headers>"),"</headers>");
        }
        if (norme.getRegleChargement().getFormat()!=null && norme.getRegleChargement().getFormat().contains("<quote>"))
        {
        	this.quote=ManipString.substringBeforeFirst(ManipString.substringAfterFirst(norme.getRegleChargement().getFormat(),"<quote>"),"</quote>");
        	
            if (this.quote.length()>1)
            {
                StringBuilder req = new StringBuilder();
                req.append("select "+this.quote+" ");
                this.quote = UtilitaireDao.get("arc").executeRequest(this.connexion, req).get(2).get(0);
            }
        }
        
        // si le séparateur est une expression complexe, l'interpreter par postgres
        if (this.separateur.length()>1)
        {
            StringBuilder req = new StringBuilder();
            req.append("select "+this.separateur+" ");
            this.separateur = UtilitaireDao.get("arc").executeRequest(this.connexion, req).get(2).get(0);
        }

        // si le headers n'est pas spécifié, alors on le cherche dans le fichier en premier ligne
        if (this.userDefinedHeaders==null) {
        	try (
	            InputStreamReader inputStreamReader = new InputStreamReader(streamHeader);
	            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
		        CSVReader readerCSV = new CSVReader(bufferedReader, separateur.charAt(0));
        			)
        	{
        		this.headers=getHeader(readerCSV);
        	}
        	finally {
        		streamHeader.close();
        	}
        }
        else
        {
        	this.headers=userDefinedHeaders.replaceAll(" ","").split(",");
        }

        // On crée la table dans laquelle on va COPYer le tout
        initialiserTable();

        
        
        copyerFile();
        
        // Application des règles de format
//        applyFormat();
        

        java.util.Date endDate = new java.util.Date();

        LoggerDispatcher.info("** CSVtoBase temps**" + (endDate.getTime() - beginDate.getTime()) + " ms", LOGGER);

    }

    /**
     * @param quote
     * @throws Exception
     * @throws IOException
     */
    public void copyerFile() throws Exception {
        try {
    	StringBuilder columnForCopy = new StringBuilder();
        columnForCopy.append("(");
        for (String nomCol : this.headers) {
            columnForCopy.append("" + nomCol + " ,");
        }
        columnForCopy.setLength(columnForCopy.length() - 1);
        columnForCopy.append(")");
//        System.out.println("* Separateur * :"+ this.separateur+"");
//        System.out.println("* Encodage * :"+ this.encoding+"");
//        System.out.println("* Quote * :"+ this.quote+"");

        UtilitaireDao.get("arc").importing(connexion, TABLE_TEMP_T, columnForCopy.toString(), streamContent, true, this.userDefinedHeaders==null, this.separateur, this.quote, this.encoding);
        }
        catch (Exception e)
        {
        	e.printStackTrace();
        	throw e;
        }
        finally {
        	streamContent.close();
        }
    }

    
    /**
     * restructure a flat file
     * @throws SQLException 
     * @throws Exception
     * @throws IOException
     */
    public void applyFormat() throws SQLException {
    	String format=norme.getRegleChargement().getFormat();
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
							.executeRequest(this.connexion, "select "+joinSelect.get(i)+" from " + joinTable.get(i) + " limit 0").get(0);

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
				UtilitaireDao.get("arc").executeImmediate(connexion, req);
			}
    		
            /*
			 * recalcule de colonnes
			 * si une colonne existe déjà, elle est écrasée
			 * sinon la nouvelle colonne est créée
			 */
    		if (!cols.isEmpty())
    		{
	            List<String> colsIn = new ArrayList <String>();
	            colsIn=UtilitaireDao.get("arc").executeRequest(this.connexion, "select * from "+this.tableTempA+" limit 0").get(0);
	    		
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
	            UtilitaireDao.get("arc").executeImmediate(connexion, req);

	    		
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
	    			nbPartition=UtilitaireDao.get("arc").getInt(connexion, "select ((count(*)*10000)/"+partition_size+")+1 from "+this.tableTempA+" tablesample system(0.01)");
	    			
	    			req=new StringBuilder();
		    		req.append("\n CREATE INDEX idx_a on "+this.tableTempA+" ((abs(hashtext("+partitionExpression.get(0)+"::text)) % "+nbPartition+"));");
		            UtilitaireDao.get("arc").executeImmediate(connexion, req);
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
		            UtilitaireDao.get("arc").executeImmediate(connexion, req);
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
				
	            UtilitaireDao.get("arc").executeImmediate(connexion, req);
    		}
    	}
    }
    
    
    /**
     * @throws SQLException
     */
    public void initialiserTable() throws SQLException {
        StringBuilder req = new StringBuilder();
        req.append("DROP TABLE IF EXISTS " + TABLE_TEMP_T + " ;");
        req.append(" \nCREATE TEMPORARY TABLE " + TABLE_TEMP_T + " (");

        for (String nomCol : this.headers) {
            req.append("\n\t " + nomCol + " text,");
        }

        req.append("\n\t id SERIAL");
        req.append(");");

        UtilitaireDao.get("arc").executeImmediate(connexion, req);
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
    public String[] getHeader(CSVReader readerCSV) throws IOException {
        
        return readerCSV.readNext();
    }

    /**
     * Contrôle pour vérifier que les feuilles du format = les colonnes du csv
     * 
     * @param aFormat
     * @return
     */
    public List<String> controleFormat(ArbreFormat aFormat) {

        LoggerDispatcher.info("** ControleFormat **", LOGGER);

        ArrayList<String> colonneErreur = new ArrayList<String>();
        int i = 0;

        while (i < headers.length) {
            if (!aFormat.getArbreFormat().keySet().contains(headers[i])) {
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
        req.append("\n\t ,'" + this.norme.getIdNorme() + "'::text collate \"C\" as id_norme ");
        req.append("\n\t ,'" + this.norme.getPeriodicite() + "'::text collate \"C\" as periodicite ");
        req.append("\n\t ,'" + this.validite + "'::text collate \"C\" as validite ");
        req.append("\n\t ,0::integer as nombre_colonne");
        

        req.append("\n\t , ");

        for (int i = 0; i < this.headers.length; i++) {
            

            // tcp
        	req.append("id as i_" + headers[i]+", " + headers[i] + " as v_"+headers[i]+",");
        }

        req.setLength(req.length() - 1);


        req.append("\n FROM " + TABLE_TEMP_T + ");");
        req.append("DROP TABLE IF EXISTS " + TABLE_TEMP_T + ";");

        UtilitaireDao.get("arc").executeImmediate(this.connexion, req);

        applyFormat();

        StringBuilder requeteBilan = new StringBuilder();
        requeteBilan.append(ApiService.pilotageMarkIdsource(this.tableChargementPilTemp, fileName, this.currentPhase, TraitementEtat.OK.toString(),
                null));

        UtilitaireDao.get("arc").executeBlock(this.connexion, requeteBilan);

        
        java.util.Date endDate = new java.util.Date();
        LoggerDispatcher.info("** FlatBaseToIdedFlatBase temps**" + (endDate.getTime() - beginDate.getTime()) + " ms", LOGGER);

    }

 
    @Override
    public void initialisation() {

    }

    @Override
    public void finalisation() {

    }

    @Override
    public void excecution() throws Exception {
	LoggerDispatcher.info("excecution", LOGGER);

	String rapport = "";
	StringBuilder requeteBilan = new StringBuilder();

	// On met le fichier en base

	csvtoBase();

	// 2 cas, soit on a une hierachie et on l'utilise, soit on en a pas et on met
	// des i_= au numéro de ligne

	// if (normeCourante.getRegleChargement().getFormat() != null &&
	// !normeCourante.getRegleChargement().getFormat().trim().equals("")) {
	//
	// // On récupère le format du fichier
	//
	// ArbreFormat arbreFormat = new ArbreFormat(normeCourante);
	// ArrayList<String> colonneErreur = ControleFormat(arbreFormat);
	// if (colonneErreur.size() != 0) {
	// StringBuilder mes = new StringBuilder("Incohérence entre les colonnes du
	// fichier et les colonnes du format");
	// mes.append("\n les colonnes ");
	// for (String col : colonneErreur) {
	// mes.append(col + ", ");
	// }
	// mes.setLength(mes.length() - 2);
	// mes.append("sont en erreur");
	//
	// throw new Exception(mes.toString());
	// }
	//
	// FlatBaseToHierarchicalBase(arbreFormat);
	// } else {
	flatBaseToIdedFlatBase();
	// }

    }

    @Override
    public void charger() throws Exception {
        initialisation();
        excecution();
        finalisation();

    }

}
