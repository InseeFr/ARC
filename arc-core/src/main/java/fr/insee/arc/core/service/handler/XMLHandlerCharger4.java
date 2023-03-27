package fr.insee.arc.core.service.handler;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.util.EDateFormat;
import fr.insee.arc.core.util.Norme;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.format.Format;
import fr.insee.arc.utils.textUtils.FastList;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerHelper;


/**
 * Classe utilisée pour gérer les événement émis par SAX lors du traitement du
 * fichier XML
 */
public class XMLHandlerCharger4 extends org.xml.sax.helpers.DefaultHandler {
	private static final Logger LOGGER = LogManager.getLogger(XMLHandlerCharger4.class);

	public XMLHandlerCharger4() {
		super();
	}

	private HashMap<String, Integer> col = new HashMap<>();
	private HashMap<String, Integer> colData = new HashMap<>();
	private HashMap<Integer, Integer> tree = new HashMap<>();
	private HashMap<Integer, Boolean> treeNode = new HashMap<>();

	private HashMap<Integer, Integer> colDist = new HashMap<>();
	private HashMap<Integer, String> keepLast = new HashMap<>();


	private int idLigne=0;

	private int distance = 0;

	public Connection connexion;

	public String fileName;
	public String jointure="";

	private String currentTag;
	private String closedTag;

	private String father = "*";
	private StringBuilder currentData = new StringBuilder();

	/*
	 * pour les rubriques recursives (au cas ou...)
	 */
	private boolean leafPossible = false;
	private boolean leafStatus = false;

	private List<String> treeStack = new ArrayList<>();
	private List<String> treeStackFather = new ArrayList<>();
	private List<String> treeStackFatherLag = new ArrayList<>();

	private FastList<String> allCols= new FastList<>();
	private List<Integer> lineCols = new ArrayList<>();
	private List<Integer> lineCols11 = new ArrayList<>();
	private List<Integer> lineIds = new ArrayList<>();
	private List<String> lineValues = new ArrayList<>();

	// parametrage des types de la base de données
	private String textBdType = "text";
	private String numBdType = "int";

	// indique que la balise courante a des données
	private boolean hasData=false;
	
	private ParallelInsert pi;

	public Norme normeCourante;
    public String validite;
    
    // column of the load table A
	public String tempTableA;
    public FastList<String> tempTableAColumnsLongName;
    public FastList<String> tempTableAColumnsShortName;
    
    private static final String ALTER ="ALTER"; 
    
    private HashMap<String,StringBuilder> requetes=new HashMap<>();
    private int requetesLength=0;
    
    
    // initialize the integration date with current
	private final String integrationDate = FormatSQL.toDate(
			FormatSQL.quoteText(new SimpleDateFormat(EDateFormat.DATE_DASH.getApplicationFormat()).format(new Date()))
			,FormatSQL.quoteText(EDateFormat.DATE_DASH.getDatastoreFormat())
			);
    
    
	/**
	 * Actions à réaliser sur les données
	 */
	@Override
	public void characters(char[] caracteres, int debut, int longueur) {
		String donnees = new String(caracteres, debut, longueur).replace("'", "''");

		// on concatène les données lues dans currentData
		// currentData est effacée quand on trouve un nouvel élement
		this.currentData.append(donnees);
		this.hasData=true;

	}

	/**
	 * Actions à réaliser lors de la fin du document XML.
	 *
	 * @throws SAXParseException
	 */
	@Override
	public void endDocument() throws SAXParseException {
		insertQueryBuilder(this.tempTableA, this.fileName, this.lineCols, this.lineIds, this.lineValues);
		
		StringBuilder requete=new StringBuilder(computeFinalQuery());
		renameColumns(requete);

		try {
			pi.join();
		} catch (InterruptedException e) {
			pi.interrupt();
			throw new SAXParseException("Error sending insert query to database ", "", "", 0, 0);
		}				
		
		try {
			UtilitaireDao.get("arc").executeImmediate(this.connexion, requete);
		} catch (ArcException ex) {
			LoggerHelper.errorGenTextAsComment(getClass(), "startElement()", LOGGER, ex);
			 throw new SAXParseException("Fichier XML : erreur de requete insertion  : "+ex.getMessage() , "", "", 0, 0);
		}
		requetes=new HashMap<>();
				
		// construction de la requete de jointure
		requeteJointureXML();
		
	}

	/**
	 * Actions à réaliser lors de la détection de la fin d'un élément.
	 *
	 * @throws SAXParseException
	 */
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXParseException {
		this.closedTag = Format.toBdRaw(qName);
		// la condition de lecture est assez spéciale
		// on doit arriver à la fin du stream de l'element et concaténer toutes
		// les données trouvées jusqu'a ce qu'on en trouve plus
		if (this.closedTag.equals(this.currentTag) && this.hasData) {
			if (this.colData.get(this.currentTag) == null) {
				this.colData.put(this.currentTag, 1);
				addQuery(ALTER, "alter table " + this.tempTableA + " add v" + this.allCols.indexOf(this.currentTag) + " " + this.textBdType + ";");
			}

			this.lineValues.remove(this.lineValues.size() - 1);
			this.lineValues.add(this.currentData.toString().trim());

		}



		this.distance--;

		this.leafStatus = false;

		this.treeStackFatherLag = new ArrayList<>(this.treeStackFather);

		this.treeStack.remove(this.treeStack.size() - 1);
		this.father = this.treeStackFather.get(this.treeStackFather.size() - 1);
		this.treeStackFather.remove(this.treeStackFather.size() - 1);

		if (this.closedTag.equals(this.currentTag) && this.leafPossible) {

			// vérifier qu'une feuille n'existe pas déjà
			 if (this.lineCols.indexOf(this.allCols.indexOf(this.closedTag)) < this.lineCols.size() - 1) {

				 insertQueryBuilder(this.tempTableA, this.fileName, this.lineCols.subList(0,this.lineCols.size()-1), this.lineIds.subList(0,this.lineCols.size()-1), this.lineValues.subList(0,this.lineCols.size()-1));

					
				 int i = this.lineCols.indexOf(this.allCols.indexOf(this.closedTag));

				 Integer g= this.lineCols.get(this.lineCols.size()-1);
				 Format.removeToIndexInt(this.lineCols, i);
				 this.lineCols.add(g);

				 g= this.lineIds.get(this.lineIds.size()-1);
				 Format.removeToIndexInt(this.lineIds, i);
				 this.lineIds.add(g);

				 g= this.lineCols11.get(this.lineCols11.size()-1);
				 Format.removeToIndexInt(this.lineCols11, i);
				 this.lineCols11.add(g);

				 String h= this.lineValues.get(this.lineValues.size()-1);
				 Format.removeToIndex(this.lineValues, i);
				 this.lineValues.add(h);

			 }

			// pour les feuilles, lineCols11 courant = lineCols11 du précédent
			 this.leafPossible = false;
			 this.leafStatus = true;

			 this.lineCols11.remove(this.lineCols11.size() - 1);
			 this.lineCols11.add(this.lineCols11.get(this.lineCols11.size() - 1));

		}

	}

	/**
	 * Actions à réaliser au début du document.
	 */
	@Override
	public void startDocument() {
		// intialisation de la connexion
		// creation de la table
		this.pi= new ParallelInsert(this.connexion, null);
		
		try {
			this.colDist.put(-1, 0);

		} catch (Exception ex) {
			LoggerHelper.errorGenTextAsComment(getClass(), "instartDocumentdex()", LOGGER, ex);
		}

	}

	/**
	 * Actions à réaliser lors de la détection d'un nouvel élément.
	 *
	 * @throws SAXParseException
	 */
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXParseException {
		this.currentTag = Format.toBdRaw(qName);
		this.currentData.setLength(0);
		this.hasData=false;

		// on ajoute les colonnes si besoin
		// on met à jour le numéro d'index
		Integer o = this.col.get(this.currentTag);


		// créer et enregistrer la colonne si elle n'existe pas
		if (o == null) {
			this.col.put(this.currentTag, 1);
			this.allCols.add(this.currentTag);
			addQuery(ALTER, "alter table " + this.tempTableA + " add i" + this.allCols.indexOf(this.currentTag) + " " + this.numBdType + ";");
			
		} else {
			// ajouter 1 a son index si la colonne existe dejà
			this.col.put(this.currentTag, o + 1);
		}

		this.distance++;
		this.tree.put(this.allCols.indexOf(this.currentTag), this.allCols.indexOf(this.father));
		this.treeNode.put(this.allCols.indexOf(this.father), true);

		if (this.colDist.get(this.allCols.indexOf(this.currentTag))== null)
		{
			this.colDist.put(this.allCols.indexOf(this.currentTag), this.distance);
		}
		else
		{
			if (!this.colDist.get(this.allCols.indexOf(this.currentTag)).equals(this.distance))
			{
				 throw new SAXParseException("Fichier XML non pris en charge : distance à la racine de la rubrique variante  : " + this.currentTag, "", "", 0, 0);
			}
		}

		/*
		 * quand le pere de la rubrique fermée est retrouvée et que ce n'est pas
		 * une feuille qui vient d'etre fermée on va réaliser l'insertion de la
		 * ligne
		 */

		if (this.treeStackFatherLag.indexOf(this.father) >= 0 && this.leafStatus == false
		) {

			insertQueryBuilder(this.tempTableA, this.fileName, this.lineCols, this.lineIds, this.lineValues);

			
			if (this.requetesLength > FormatSQL.TAILLE_MAXIMAL_BLOC_SQL) {
				
				try {
					pi.join();
				} catch (InterruptedException e) {
					pi.interrupt();
					throw new SAXParseException("Error sending insert query to database ", "", "", 0, 0);
				}

				pi =  new ParallelInsert(this.connexion, computeFinalQuery()); 
				pi.start();
				
				
				this.requetes=new HashMap<>();
				this.requetesLength=0;
			}

			// On va alors dépiler ligneCols, lineIds, lineValues jusqu'au père
			// de la rubrique
			int fatherIndex = this.lineCols11.lastIndexOf(this.allCols.indexOf(this.father)) + 1;


			Format.removeToIndexInt(this.lineCols11, fatherIndex);
			Format.removeToIndexInt(this.lineCols, fatherIndex);
			Format.removeToIndexInt(this.lineIds, fatherIndex);
			Format.removeToIndex(this.lineValues, fatherIndex);

		}


		this.treeStackFather.add(this.father);

		this.treeStack.add(this.currentTag);
		this.father = this.currentTag;

		this.leafPossible = true;
		this.lineCols11.add(this.allCols.indexOf(this.currentTag));
		this.lineCols.add(this.allCols.indexOf(this.currentTag));
		this.lineIds.add(this.col.get(this.currentTag));
		this.lineValues.add(null);

	}



	/**
	 * Requete d'insertion des données parsées
	 * C'est assez diffcile : on souhaite profiter au mieux de la commande insert multi value :
	 * INSERT into (cols) values (data1), (data2) ... ;
	 * faut bien reperer ou on met les nouvelles données et si c'est possible
	 *
	 *
	 * @param aRequete
	 * @param tempTableI
	 * @param fileName
	 * @param lineCols
	 * @param lineIds
	 * @param lineValues
	 * @throws SAXParseException 
	 */
	private void insertQueryBuilder(String tempTableI, String fileName, List<Integer> lineCols, List<Integer> lineIds,
			List<String> lineValues) {


		HashMap<Integer, String> keep = new HashMap<>();

		
		// enregistre la liste des colonnes/valeurs relatives à un noeud de l'arbre xml
		StringBuilder s=new StringBuilder();
		for (int i = 0; i < lineCols.size(); i++) {
			s.setLength(0);
			s.append("{").append(lineIds.get(i)).append("}");
			if (lineValues.get(i) != null) {
				s.append("[").append(lineValues.get(i)).append("]");
			}

			if (keep.get(this.tree.get(lineCols.get(i)))==null)
			{
				keep.put(this.tree.get(lineCols.get(i)), s.toString());
			}
			else
			{
				s.append(keep.get(this.tree.get(lineCols.get(i))));
				keep.put(this.tree.get(lineCols.get(i)), s.toString());
			}

		}


		HashMap<Integer, Boolean> doNotinsert = new HashMap<>();
		for (Map.Entry<Integer, String> entry : this.keepLast.entrySet()) {


			if (entry.getValue().equals(keep.get(entry.getKey())))
			{
				doNotinsert.put(entry.getKey(), true);

			}
		}

		this.keepLast=keep;


		StringBuilder req = new StringBuilder();
		StringBuilder req2 = new StringBuilder();
		this.idLigne++;
		
		req.append("insert into ").append(tempTableI)
				.append("(").append(tempTableAColumnsShortName.get(tempTableAColumnsLongName.indexOf(ColumnEnum.ID_SOURCE.getColumnName())))
				.append(",").append(tempTableAColumnsShortName.get(tempTableAColumnsLongName.indexOf("id")))
				.append(",").append(tempTableAColumnsShortName.get(tempTableAColumnsLongName.indexOf("date_integration")))
				.append(",").append(tempTableAColumnsShortName.get(tempTableAColumnsLongName.indexOf("id_norme")))
				.append(",").append(tempTableAColumnsShortName.get(tempTableAColumnsLongName.indexOf("periodicite")))
				.append(",").append(tempTableAColumnsShortName.get(tempTableAColumnsLongName.indexOf("validite")));
		
		req2.append("('").append(fileName).append("',").append(this.idLigne).append(",").append(integrationDate).append(",'").append(normeCourante.getIdNorme()).append("','").append(normeCourante.getPeriodicite()).append("','").append(validite).append("'");
		
		for (int i = 0; i < lineCols.size(); i++) {

			// si le bloc est a reinséré (le pere n'est pas retrouvé dans doNotInsert
			// ou si la colonne est un noeud, on procède à l'insertion

			if (doNotinsert.get(this.tree.get(lineCols.get(i)))==null || this.treeNode.get(lineCols.get(i))!=null)
			{
				
				req.append(",i").append(this.allCols.indexOf(this.allCols.get(lineCols.get(i))));
			    req2.append(",").append(lineIds.get(i));

			if (lineValues.get(i) != null) {
				req.append(",v").append(this.allCols.indexOf(this.allCols.get(lineCols.get(i))));
			    req2.append(",'").append(lineValues.get(i)).append("'");
			}
			}
		}

		 req.append(")values");
		 req2.append(")");

		 // attention; on doit insérer au bon endroit;
//		int z=aRequete.indexOf(req.toString(),this.start);
//
//		if (z>-1)
//		{
//		    req2.append(",");
//		    aRequete.insert(z+req.length(), req2);
//		}
//		else
//		{
//		    req2.append(";");
//		    aRequete.append(req);
//		    aRequete.append(req2);
//		}

		    String reqString=req.toString();
		    addQuery(reqString,req2);		    
	}

	
	private void addQuery(String key, String value)
	{
		addQuery(key, new StringBuilder(value));
	}
	
	private void addQuery(String key, StringBuilder value)
	{
	    if (requetes.get(key)!=null)
		{
			requetes.get(key).append(key.equals(ALTER)?"":",").append(value);
		}
	    else
	    {
	    	requetes.put(key,value);
	    }
	    requetesLength=requetesLength+value.length();
	}
	
	private String computeFinalQuery()
	{
		
		StringBuilder result=new StringBuilder();
		result.append((requetes.get(ALTER)!=null)?requetes.get(ALTER):"");
		
		for (String s:requetes.keySet())
		{
			if (!s.equals(ALTER))
			{
				result.append(s).append(requetes.get(s)).append(";");
			}
		}
				
		return result.toString();
		
	}
	
	
    /**
     * Permet de générer la requête SQL de normage. C'est une méthode assez compliqué car on doit décomposer le fichier
     * en bloc pour ensuite faire les jointure.
     */
    private void requeteJointureXML() {

    		// construction de la requete de jointure
            StringBuilder req= new StringBuilder();

            int[][] arr = TreeFunctions.getTreeArrayByDistance(this.tree, this.colDist);
            StringBuilder reqCreate = new StringBuilder(" \n");

            StringBuilder reqInsert = new StringBuilder();
            reqInsert.append(" INSERT INTO {table_destination} (id,"+ColumnEnum.ID_SOURCE.getColumnName()+",date_integration,id_norme,validite,periodicite");

            StringBuilder reqSelect = new StringBuilder();
            reqSelect.append("\n SELECT row_number() over (), ww.* FROM (");
            reqSelect.append("\n SELECT '{nom_fichier}',"+integrationDate+",'{id_norme}','{validite}','{periodicite}'");

            StringBuilder reqFrom = new StringBuilder();

            int d=0;

            for (int i = 0; i < arr.length; i++) {

                // pour chaque noeud

                if (arr[i][2] == 1) {

                    String leaf = TreeFunctions.getLeafs(arr[i][1], arr, this.colData, this.allCols);

                    // créer les vues
                    String leafMax = TreeFunctions.getLeafsMax(arr[i][1], arr, this.colData, this.allCols);
                    reqCreate.append("CREATE TEMPORARY TABLE t_" + this.allCols.get(arr[i][1]) + " as (select i_" + this.allCols.get(arr[i][1]) + " as m_" + this.allCols.get(arr[i][1]) + " ");
                    if (arr[i][0] >= 0) {
                        reqCreate.append(", i_" + this.allCols.get(arr[i][0]) + " as i_" + this.allCols.get(arr[i][0]) +" ");
                    }
                    reqCreate.append(TreeFunctions.getLeafsSpace(arr[i][1], arr, this.colData, this.allCols));
                    reqCreate.append(" FROM (SELECT i_" + this.allCols.get(arr[i][1])+" ");
                    reqCreate.append(leafMax);
                    reqCreate.append(" FROM {table_source} where i_" + this.allCols.get(arr[i][1]) + " is not null group by i_" + this.allCols.get(arr[i][1]) + ") a ");
                    if (arr[i][0] >= 0) {
                        reqCreate.append(" , (SELECT DISTINCT i_" + this.allCols.get(arr[i][1]) + " as pivot, i_" + this.allCols.get(arr[i][0]) + " FROM {table_source} where i_" + this.allCols.get(arr[i][1]) + " is not null) b ");
                        reqCreate.append(" where a.i_" + this.allCols.get(arr[i][1]) + " = b.pivot ");
                    }
                    reqCreate.append("); \n");


                    // la table vide faite a partir de la table du bloc; ca permet de faire simplement des jointures externe avec vide dedans
                    reqCreate.append("CREATE TEMPORARY TABLE t_" + this.allCols.get(arr[i][1]) + "_null as (select * from t_" + this.allCols.get(arr[i][1]) + " where false); \n");

                    // générer la clause select
                    reqInsert.append(",i_" + this.allCols.get(arr[i][1]) + leaf);
                    reqSelect.append(",m_" + this.allCols.get(arr[i][1]) + leaf);


                    // générer la clause from

                    if (arr[i][0]==-1) {
                        reqFrom.append("t_"+this.allCols.get(arr[i][1]));
                    }
                    else
                    {

                        if (d!=arr[i][3] && d>0)
                        {
                            reqFrom.append("\n ) ");
                            reqFrom.insert(0,"\n (");
                        }

                        reqFrom.append("\n left join t_"+this.allCols.get(arr[i][1])+" on m_"+this.allCols.get(arr[i][0])+"=t_"+this.allCols.get(arr[i][1])+".i_"+this.allCols.get(arr[i][0]));
                    }


                    d=arr[i][3];
                }
            }

            reqInsert.append("\n )");

            reqFrom.insert(0, "\n FROM ");
            reqFrom.append("\n WHERE true ) ww ");

            // on ne met pas la parenthèse fermante exprées
            req.append (reqCreate);
            req.append (reqInsert);
            req.append (reqSelect);
            req.append (reqFrom);

            this.jointure=req.toString().replace("'", "''");

    }
    
    /**
     * For optimization purpose, the columns of the load table had been shortened
     * Build the query to retrieve the long columns name of the load table A
     * @param aRequete
     */
    private void renameColumns(StringBuilder aRequete)
    {
    	for (int i=0;i<this.tempTableAColumnsShortName.size();i++)
    	{
    		aRequete.append("\n ALTER TABLE "+this.tempTableA+" RENAME "+this.tempTableAColumnsShortName.get(i)+" TO "+this.tempTableAColumnsLongName.get(i)+";");
    	}
    	
    	for (int i=0;i<this.allCols.size();i++)
    	{
    			aRequete.append("\n ALTER TABLE "+this.tempTableA+" RENAME i"+i+" TO i_" + this.allCols.get(i)+";");
    		if (colData.get(this.allCols.get(i))!=null)
    		{
        		aRequete.append("\n ALTER TABLE "+this.tempTableA+" RENAME v"+i+" TO v_" + this.allCols.get(i)+";");
    		}
    	}
    		
    }

}
