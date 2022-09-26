package fr.insee.arc.core.service.handler;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

import fr.insee.arc.core.service.ApiService;
import fr.insee.arc.core.util.EDateFormat;
import fr.insee.arc.core.util.Norme;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.format.Format;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.Pair;


/**
 * Classe utilisée pour gérer les événement émis par SAX lors du traitement du
 * fichier XML
 */
public class XMLComplexeHandlerCharger extends org.xml.sax.helpers.DefaultHandler {
	private static final Logger LOGGER = LogManager.getLogger(XMLHandlerCharger4.class);

	public XMLComplexeHandlerCharger() {
		super();
	}

	public HashMap<String, Integer> col;
	public HashMap<String, Integer> colData;
	
	// @trees
	private HashMap<Integer, Integer> tree = new HashMap<>();
	private HashMap<Integer, Boolean> treeNode = new HashMap<>();

	private HashMap<Integer, Integer> colDist = new HashMap<>();
	private HashMap<Integer, String> keepLast = new HashMap<>();

	public int start;
	private int idLigne=0;

	private int distance = 0;

	public Connection connexion;

	public String fileName;
	public String jointure="";

	private String currentTag;
	
	private String closedTag;
	private String closedTag1;
	private String closedTag2;
	
	// this handler will keep the father reference to handle elements which have the same name but not the same parent
	private String root_father = "*";
	
	private String father = root_father;
	private StringBuilder currentData = new StringBuilder();

	/*
	 * pour les rubriques recursives (au cas ou...)
	 */
	private boolean leafPossible = false;
	private boolean leafStatus = false;

	/* stacks of ancestors */
	/* ancestors with raw xml name*/
	private HashMap<String,Integer> treeStackQName = new HashMap<String,Integer>();
	private Integer orderTreeStackQName=0;
	
	/* ancestors with database name*/
	private List<String> treeStack = new ArrayList<String>();
	private List<String> treeStackFather = new ArrayList<String>();
	private List<String> treeStackFatherLag = new ArrayList<String>();

	public List<String> allCols;
	private List<Integer> lineCols = new ArrayList<Integer>();
	private List<Integer> lineCols11 = new ArrayList<Integer>();
	private List<Integer> lineIds = new ArrayList<Integer>();
	private List<String> lineValues = new ArrayList<String>();

	// parametrage des types de la base de données
	private String textBdType = "text";
	private String numBdType = "int";

	public StringBuilder requete;
	private StringBuilder structure=new StringBuilder();

	// indique que la balise courante a des données
	private boolean hasData=false;
	
	public int sizeLimit;

	public Norme normeCourante;
    public String validite;
    
    // column of the load table A
	public String tempTableA;
    public ArrayList<String> tempTableAColumnsLongName;
    public ArrayList<String> tempTableAColumnsShortName;
    
    // format to rename column with format rules
	public ArrayList<Pair<String, String>> format;
    
    public static final String JOINXML_QUERY_BLOCK="\n -- query";
    public static final String JOINXML_STRUCTURE_BLOCK="\n -- structure\n";
    private static final String HEADER="$h";
    
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
		insertQueryBuilder(this.requete,this.tempTableA, this.fileName, this.lineCols, this.lineIds, this.lineValues);
		this.start=this.requete.length();
		
		renameColumns();

		multiLeafUpdate();
		
		
		try {
			UtilitaireDao.get("arc").executeImmediate(this.connexion, this.requete);
		} catch (SQLException ex) {
			LoggerHelper.errorGenTextAsComment(getClass(), "XMLComplexeHandlerCharger.startElement()", LOGGER, ex);
			 throw new SAXParseException("Fichier XML : erreur de requete insertion  : "+ex.getMessage() , "", "", 0, 0);
		}
		this.requete.setLength(0);
		this.start=0;
		
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
		this.closedTag2=this.closedTag1;
		this.closedTag1=this.closedTag;
		this.closedTag = Format.toBdRaw(renameColumn(qName));
				
		// la condition de lecture est assez spéciale
		// on doit arriver à la fin du stream de l'element et concaténer toutes
		// les données trouvées jusqu'a ce qu'on en trouve plus
		if (this.closedTag.equals(this.currentTag) && this.hasData) {

			if (this.colData.get(this.currentTag) == null) {
				this.colData.put(this.currentTag, 1);
				this.requete.append("alter table " + this.tempTableA + " add v" + this.allCols.indexOf(this.currentTag) + " " + this.textBdType + ";");
			}

			this.lineValues.remove(this.lineValues.size() - 1);
			this.lineValues.add(this.currentData.toString().trim());

		}



		this.distance--;

		this.leafStatus = false;

		
		
/** cas des feuilles muliples
 * this.closedTag1.equals(this.closedTag) : la meme balise a été fermée 2 fois de suite
 */
		boolean multipleLeaf=
				(this.closedTag1!=null && this.closedTag1.equals(this.closedTag))
				||(this.closedTag2!=null && this.closedTag2.equals(this.closedTag1))
				;
		
		

		if (multipleLeaf)
				{
			String closedTagHeader1=this.closedTag1+HEADER;
				 // we create an header tag
				 Object o = this.col.get(closedTagHeader1);
					// créer et enregistrer la colonne si elle n'existe pas
					if (o == null) {
						this.allCols.add(closedTagHeader1);
						this.requete.append("alter table " + this.tempTableA + " add i" + this.allCols.indexOf(closedTagHeader1) + " " + this.numBdType + ";");
					}
					
					String fatherOfTheBlock=this.treeStackFatherLag.get(this.treeStackFatherLag.size()-1);
					
					// mettre à jour la colonne si elle n'existe pas dans tree ou si elle ne pointe pas vers le bon pere 
					if (tree.get(this.allCols.indexOf(closedTagHeader1))==null 
							/* @trees || !tree.get(this.allCols.indexOf(closedTagHeader1)).equals(this.allCols.indexOf(fatherOfTheBlock)) */
							)
					{	

						// mettre à jour tree
						this.tree.put(this.allCols.indexOf(closedTagHeader1), this.allCols.indexOf(fatherOfTheBlock));
						this.tree.put(this.allCols.indexOf(this.closedTag1), this.allCols.indexOf(closedTagHeader1));
						
						this.treeNode.put(this.allCols.indexOf(closedTagHeader1), true);
						
						this.colDist.put(this.allCols.indexOf(closedTagHeader1), this.colDist.get(this.allCols.indexOf(this.closedTag1)));
						this.colDist.put(this.allCols.indexOf(this.closedTag1), this.colDist.get(this.allCols.indexOf(this.closedTag1))+1);
					}
					
					// on donne a la rubrique header le même identifiant que la rubrique multiple
					this.col.put(closedTagHeader1,lineIds.get(lineCols.indexOf(allCols.indexOf(closedTag1))));
					
					lineIds.add(lineCols.indexOf(allCols.indexOf(closedTag1)), this.col.get(closedTagHeader1));
					// on met null pour sa values (ce n'est qu'un id)
					lineValues.add(lineCols.indexOf(allCols.indexOf(closedTag1)), null);
					// on décale les colonnes pour l'ajouter a la liste des colonnes
					if (lineCols11.indexOf(allCols.indexOf(closedTag1))>0)
					{
					lineCols11.add(lineCols11.indexOf(allCols.indexOf(closedTag1)), allCols.indexOf(closedTagHeader1));
					}
					
					lineCols.add(lineCols.indexOf(allCols.indexOf(closedTag1)), allCols.indexOf(closedTagHeader1));
					
				}
		
		// mise à jour des listes
		treeStackQName.remove(qName);
		orderTreeStackQName--;
				
		this.treeStack.remove(this.treeStack.size() - 1);
		this.treeStackFatherLag = new ArrayList<String>(this.treeStackFather);
		this.father = this.treeStackFather.get(this.treeStackFather.size() - 1);
		this.treeStackFather.remove(this.treeStackFather.size() - 1);
		
		if (this.closedTag.equals(this.currentTag) && this.leafPossible) {

			// vérifier qu'une feuille n'existe pas déjà
			 if (this.lineCols.indexOf(this.allCols.indexOf(this.closedTag)) < this.lineCols.size() - 1) {
				 
				 // réalisation de l'insertion
				 
				 insertQueryBuilder(this.requete,this.tempTableA, this.fileName, this.lineCols.subList(0,this.lineCols.size()-1), this.lineIds.subList(0,this.lineCols.size()-1), this.lineValues.subList(0,this.lineCols.size()-1));

				 int i=0;
				 if (multipleLeaf)
					 {
					 String closedTagHeader1=this.closedTag1+HEADER;
					 i = this.lineCols.indexOf(this.allCols.indexOf(closedTagHeader1));
					 }
				 else 
					 {
					 i = this.lineCols.indexOf(this.allCols.indexOf(this.closedTag));
					 }

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

		this.treeStackQName.put(qName,orderTreeStackQName);
		orderTreeStackQName++;
		
		this.currentTag = Format.toBdRaw(renameColumn(qName));
//				+(this.father.equals(root_father)?"":
//					(father_separator+
//							ManipString.substringBeforeFirst(
//									this.father
//									,father_separator)
//					)
//					)
				;
		
		
		


		
		this.currentData.setLength(0);
		this.hasData=false;
//		this.firstData = true;
		// distance++;

		// on ajoute les colonnes si besoin
		// on met à jour le numéro d'index
		Object o = this.col.get(this.currentTag);


		// créer et enregistrer la colonne si elle n'existe pas
		if (o == null) {
			this.col.put(this.currentTag, 1);
			// rootDistance.put(currentTag,distance);
			// try {
			// if (pst!=null){
			// requete=Format.executeBlock(st, requete);
			//
			// pst.executeBatch();
			// pst=null;
			// }

			this.allCols.add(this.currentTag);

			this.requete.append("alter table " + this.tempTableA + " add i" + this.allCols.indexOf(this.currentTag) + " " + this.numBdType + ";");

		} else {
			// ajouter 1 a son index si la colonne existe dejà
			this.col.put(this.currentTag, (Integer) (o) + 1);
		}

		this.distance++;
		
		// si le lien pere fils n'est pas enregistré
		// ou que pour ce fils, on trouve un autre pere et que ce pere trouvé n'est pas grand pere
		
		if (this.tree.get(this.allCols.indexOf(this.currentTag))==null
				)
		{
			this.tree.put(this.allCols.indexOf(this.currentTag), this.allCols.indexOf(this.father));
		}
	
		// enregistrement de la structure
		structure.append(
				(
				","+(this.father.equals(root_father)?ApiService.ROOT:"i_"+this.father))
				+" "
				+(this.father.equals(root_father)?"1":this.col.get(this.father))
				+" "
				+"i_"+this.currentTag
				);
		
		
		if (
				this.tree.get(this.allCols.indexOf(this.currentTag)).equals(this.allCols.indexOf(this.father))
				// cas des bloc multiples
				|| (this.tree.get(this.allCols.indexOf(this.currentTag+HEADER))!=null && this.tree.get(this.allCols.indexOf(this.currentTag+HEADER)).equals(this.allCols.indexOf(this.father)))
				)
		{}
		else
		{
			throw new SAXParseException("Le tag "+this.currentTag+" a des pères differents", "", "", 0, 0);
		}
		
		
		this.treeNode.put(this.allCols.indexOf(this.father), true);

		if (this.colDist.get(this.allCols.indexOf(this.currentTag))== null)
		{
			this.colDist.put(this.allCols.indexOf(this.currentTag), this.distance);
		}

		/*
		 * quand le pere de la rubrique fermée est retrouvée et que ce n'est pas
		 * une feuille qui vient d'etre fermée on va réaliser l'insertion de la
		 * ligne
		 */

		if (this.treeStackFatherLag.indexOf(this.father) >= 0 && this.leafStatus == false
		) {

			insertQueryBuilder(this.requete,this.tempTableA, this.fileName, this.lineCols, this.lineIds, this.lineValues);

			if (this.requete.length() > FormatSQL.TAILLE_MAXIMAL_BLOC_SQL) {
				try {
					UtilitaireDao.get("arc").executeImmediate(this.connexion, this.requete);
				} catch (SQLException ex) {
					LoggerHelper.errorGenTextAsComment(getClass(), "startElement()", LOGGER, ex);
					 throw new SAXParseException("Fichier XML : erreur de requete insertion  : "+ex.getMessage() , "", "", 0, 0);
				}
				this.requete.setLength(0);
				this.start=0;
			}

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
	 * INSERT into (cols) values (data1) values (data2) ... ;
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
	private void insertQueryBuilder(StringBuilder aRequete, String tempTableI, String fileName, List<Integer> lineCols, List<Integer> lineIds,
			List<String> lineValues) throws SAXParseException {

		HashMap<Integer, String> keep = new HashMap<>();

		String s;
		for (int i = 0; i < lineCols.size(); i++) {
			s="{"+lineIds.get(i)+"}";
			if (lineValues.get(i) != null) {
				s=s+"["+lineValues.get(i)+"]";
			}

			if (keep.get(this.tree.get(lineCols.get(i)))==null)
			{
				keep.put(this.tree.get(lineCols.get(i)), s);
			}
			else
			{
				s=s+keep.get(this.tree.get(lineCols.get(i)));
				keep.put(this.tree.get(lineCols.get(i)), s);
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
		
		req.append("insert into " + tempTableI + 
				"(" + tempTableAColumnsShortName.get(tempTableAColumnsLongName.indexOf("id_source")) +
				"," + tempTableAColumnsShortName.get(tempTableAColumnsLongName.indexOf("id")) +
				"," + tempTableAColumnsShortName.get(tempTableAColumnsLongName.indexOf("date_integration")) +
				"," + tempTableAColumnsShortName.get(tempTableAColumnsLongName.indexOf("id_norme")) +
				"," + tempTableAColumnsShortName.get(tempTableAColumnsLongName.indexOf("periodicite")) +
				"," + tempTableAColumnsShortName.get(tempTableAColumnsLongName.indexOf("validite")));
		
		req2.append("('" + fileName + "',"+this.idLigne+","+integrationDate+",'"+normeCourante.getIdNorme()+"','"+normeCourante.getPeriodicite()+"','"+validite+"'");
		
		int z=0;

		for (int i = 0; i < lineCols.size(); i++) {

			// si le bloc est a reinséré (le pere n'est pas retrouvé dans doNotInsert
			// ou si la colonne est un noeud, on procède à l'insertion
			if (doNotinsert.get(this.tree.get(lineCols.get(i)))==null || this.treeNode.get(lineCols.get(i))!=null)
			{
				
				req.append(",i" + this.allCols.indexOf(this.allCols.get(lineCols.get(i))));
			    req2.append(","+ lineIds.get(i));

			if (lineValues.get(i) != null) {
				req.append(",v" + this.allCols.indexOf(this.allCols.get(lineCols.get(i))));
			    req2.append(",'" + lineValues.get(i) + "'");
			}
			}

		}

		 req.append(")values");
		 req2.append(")");

		// attention : on doit insérer au bon endroit
		z=aRequete.indexOf(req.toString(),this.start);

		if (z>-1)
		{
		    req2.append(",");
		    aRequete.insert(z+req.length(), req2);
		}
		else
		{
		    req2.append(";");
		    aRequete.append(req);
		    aRequete.append(req2);
		}
		// en production, on peut vouloir limiter la taille du fichier et le faire passer en KO
		if (sizeLimit>0)
		{
			 if (this.idLigne>sizeLimit)
			 {
				 throw new SAXParseException("Fichier trop volumineux","", "", sizeLimit, sizeLimit);
			 }
		}
	}
	
	

	
	
    /**
     * Permet de générer la requête SQL de normage. C'est une méthode assez compliqué car on doit décomposer le fichier
     * en bloc pour ensuite faire les jointure.
     */
    private void requeteJointureXML() {
    	
    		// construction de la requete de jointure
                StringBuilder req= new StringBuilder();
                
                int[][] arr = Format.getTreeArrayByDistance(this.tree, this.colDist);
	            StringBuilder reqCreate = new StringBuilder(" \n");
	
	            StringBuilder reqInsert = new StringBuilder();
	            reqInsert.append(" INSERT INTO {table_destination} (id,id_source,date_integration,id_norme,validite,periodicite");
	
	            StringBuilder reqSelect = new StringBuilder();
	            reqSelect.append("\n SELECT row_number() over (), ww.* FROM (");
	            reqSelect.append("\n SELECT '{nom_fichier}',"+integrationDate+",'{id_norme}','{validite}','{periodicite}'");
	
	            StringBuilder reqFrom = new StringBuilder();
	
	            int d=0;
	
	            for (int i = 0; i < arr.length; i++) {
	
	                if (arr[i][2] == 1) {
	
	                    String leaf = Format.getLeafs(arr[i][1], arr, this.colData, this.allCols);
	
	                    // Version sans fonctions d'aggregation
	                    String leafMax = Format.getLeafsMax(arr[i][1], arr, this.colData, this.allCols);
	                    reqCreate.append("CREATE TEMPORARY TABLE t_" + this.allCols.get(arr[i][1]) + " as (select i_" + this.allCols.get(arr[i][1]) + " as m_" + this.allCols.get(arr[i][1]) + " ");
	                    if (arr[i][0] >= 0) {
	                        reqCreate.append(", i_" + this.allCols.get(arr[i][0]) + " as i_" + this.allCols.get(arr[i][0]) +" ");
	                    }
	                    reqCreate.append(Format.getLeafsSpace(arr[i][1], arr, this.colData, this.allCols));
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
	
	
	            // la compression fait perdre trop de temp
	            // Mais apres, c'est sur que la table de pilotage devient un peu trop "grosse"
	            req.append(JOINXML_STRUCTURE_BLOCK);
	            req.append(structure.substring(1));
	            this.jointure=this.jointure+req.toString().replace("'", "''");


    }
    
    /**
     * For optimization purpose, the columns of the load table had been shortened
     * Build the query to retrieve the long columns name of the load table A
     * @param aRequete
     */
    private void renameColumns()
    {
    	for (int i=0;i<this.tempTableAColumnsShortName.size();i++)
    	{
    		this.requete.append("\n ALTER TABLE "+this.tempTableA+" RENAME "+this.tempTableAColumnsShortName.get(i)+" TO "+this.tempTableAColumnsLongName.get(i)+";");
    	}
    	
    	for (int i=0;i<this.allCols.size();i++)
    	{
    		this.requete.append("\n ALTER TABLE "+this.tempTableA+" RENAME i"+i+" TO i_" + this.allCols.get(i)+";");
    		if (colData.get(this.allCols.get(i))!=null)
    		{
    			this.requete.append("\n ALTER TABLE "+this.tempTableA+" RENAME v"+i+" TO v_" + this.allCols.get(i)+";");
    		}
    	}
    		
    }

    private void multiLeafUpdate()
    {
    	 // gestion des rubriques multiple
 		for (int i=0;i<this.allCols.size();i++)
     	{
 			if (this.allCols.get(i).endsWith(HEADER))
 			{
 				String headerCol=this.allCols.get(i);
 				String col=this.allCols.get(i).replace(HEADER, "");
 				
 				this.requete.append("\n UPDATE "+this.tempTableA+" SET i_"+headerCol+"=i_"+col+" WHERE i_"+headerCol+" IS NULL and i_"+col+" IS NOT NULL;");
 			}
 		}
    		
    }

  /**
   * 
   * @param qName
   * @param toRename
   * @return
   */
    private String renameColumn(String qName)
    {
    	Map<Integer, String> m= new TreeMap<Integer, String>(); 
    	
    	for (Pair<String,String> p:this.format)
			{
				if (
						treeStackQName.get(p.getSecond())!=null
						&& treeStackQName.get(p.getFirst())!=null
						&& treeStackQName.get(p.getFirst())<treeStackQName.get(p.getSecond())
						)
				{
					m.put(treeStackQName.get(p.getFirst()), p.getFirst());
				}
			}
    	
    	if (!m.isEmpty()) {
		 for(Integer key: m.keySet()){
	            qName+="_"+m.get(key);
	        }
    	}
    	
    	return qName;
    }
    
    
}
