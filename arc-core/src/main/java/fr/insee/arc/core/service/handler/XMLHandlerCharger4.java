package fr.insee.arc.core.service.handler;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

import fr.insee.arc.core.util.Norme;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.format.Format;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerHelper;


/**
 * Classe utilisée pour gérer les événement émis par SAX lors du traitement du
 * fichier XML
 */
public class XMLHandlerCharger4 extends org.xml.sax.helpers.DefaultHandler {
	private static final Logger LOGGER = Logger.getLogger(XMLHandlerCharger4.class);

	public XMLHandlerCharger4() {
		super();
	}

	public HashMap<String, Integer> col;
	public HashMap<String, Integer> colData;
	public HashMap<Integer, Integer> tree = new HashMap<>();
	public HashMap<Integer, Boolean> treeNode = new HashMap<>();

	public HashMap<Integer, Integer> colDist = new HashMap<>();
	public HashMap<Integer, String> keepLast = new HashMap<>();

	public int start;
	public int idLigne=0;

	public int distance = 0;

	public Connection connexion;

	public String fileName;
	public String jointure="";

	public String currentTag;
	public String closedTag;

	public String father = "*";
	public StringBuilder currentData = new StringBuilder();

	/*
	 * pour les rubriques recursives (au cas ou...)
	 */
	public boolean leafPossible = false;
	public boolean leafStatus = false;

	public List<String> treeStack = new ArrayList<String>();
	public List<String> treeStackFather = new ArrayList<String>();
	public List<String> treeStackFatherLag = new ArrayList<String>();

	public List<String> allCols;
	public List<Integer> lineCols = new ArrayList<Integer>();
	public List<Integer> lineCols11 = new ArrayList<Integer>();
	public List<Integer> lineIds = new ArrayList<Integer>();
	public List<String> lineValues = new ArrayList<String>();

	// parametrage des types de la base de données
	public String textBdType = "text";
	public String numBdType = "int";

	public StringBuilder requete;

	// indique que la balise courante a des données
	public boolean hasData=false;
	
	public int sizeLimit;

	public Norme normeCourante;
    public String validite;
    
    // column of the load table A
	public String tempTableA;
    public ArrayList<String> tempTableAColumnsLongName;
    public ArrayList<String> tempTableAColumnsShortName;

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
		
		renameColumns(this.requete);
		
		try {
			UtilitaireDao.get("arc").executeImmediate(this.connexion, this.requete);
		} catch (SQLException ex) {
			LoggerHelper.errorGenTextAsComment(getClass(), "startElement()", LOGGER, ex);
			//e.printStackTrace();
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
		this.closedTag = Format.toBdRaw(qName);
		// la condition de lecture est assez spéciale
		// on doit arriver à la fin du stream de l'element et concaténer toutes
		// les données trouvées jusqu'a ce qu'on en trouve plus
		if (this.closedTag.equals(this.currentTag) && this.hasData) {
			// System.out.println(currentTag+" = " + donnees + " ; ");

			if (this.colData.get(this.currentTag) == null) {
				this.colData.put(this.currentTag, 1);
				// try {
				// if (pst!=null){
				//
				//
				// requete=Format.executeBlock(st, requete);
				//
				// pst.executeBatch();
				// pst=null;
				// }

				this.requete.append("alter table " + this.tempTableA + " add v" + this.allCols.indexOf(this.currentTag) + " " + this.textBdType + ";");

//				this.requete.append("alter table " + this.tempTableA + " add v_" + this.currentTag + " " + this.textBdType + ";");

				// } catch (SQLException e) {
				// 666 LoggerHelper.errorGenTextAsComment(getClass(), "index()", LOGGER, ex);
				// e.printStackTrace();
				// }
			}

			this.lineValues.remove(this.lineValues.size() - 1);
			this.lineValues.add(this.currentData.toString().trim());

		}



		this.distance--;
		// System.out.println(Arrays.toString(treeStack.toArray()));
		// System.out.println(Arrays.toString(treeStackFather.toArray()));

		//
		// System.out.println(Arrays.toString(treeStackFatherLag.toArray()));
		//
		// System.out.println(Arrays.toString(treeStackFather.toArray()));

		// modif : updated
		// updateNeeded.remove(closedTag);

		this.leafStatus = false;

		// treeStackFatherLag= new ArrayList<String>(treeStackFather);
		this.treeStackFatherLag = new ArrayList<String>(this.treeStackFather);

		this.treeStack.remove(this.treeStack.size() - 1);
		this.father = this.treeStackFather.get(this.treeStackFather.size() - 1);
		this.treeStackFather.remove(this.treeStackFather.size() - 1);

		if (this.closedTag.equals(this.currentTag) && this.leafPossible) {

			// vérifier qu'une feuille n'existe pas déjà
			 if (this.lineCols.indexOf(this.allCols.indexOf(this.closedTag)) < this.lineCols.size() - 1) {

//				 System.out.println();
//				 System.out.println();
//				 System.out.println("allCols : "+ allCols);
//				 System.out.println("lineCols : "+ lineCols);
//				 System.out.println("lineIds : "+ lineIds);
//				 System.out.println("lineValues : "+ lineValues);
//				 System.out.println("lineCols11 : "+ lineCols11);
//				 System.out.println("treeStack : "+ treeStack);
//				 System.out.println("treeFatherStack : "+ treeStackFather);
//				 System.out.println("treeStackFatherLag : "+ treeStackFatherLag);
//
//
//				 System.out.println();
//				 System.out.println();
//
				 insertQueryBuilder(this.requete,this.tempTableA, this.fileName, this.lineCols.subList(0,this.lineCols.size()-1), this.lineIds.subList(0,this.lineCols.size()-1), this.lineValues.subList(0,this.lineCols.size()-1));

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


//				 System.out.println();
//				 System.out.println();
//				 System.out.println("allCols : "+ allCols);
//				 System.out.println("lineCols : "+ lineCols);
//				 System.out.println("lineIds : "+ lineIds);
//				 System.out.println("lineValues : "+ lineValues);
//				 System.out.println("lineCols11 : "+ lineCols11);
//				 System.out.println();
//				 System.out.println();
//



	//			 throw new SAXParseException("Fichier XML non pris en charge : feuille en doublon de rubrique  : " + closedTag, "", "", 0, 0);

//					insertQueryBuilder(requete,tempTableI, fileName, lineCols, lineIds, lineValues);
//					int fatherIndex = lineCols11.lastIndexOf(allCols.indexOf(father)) + 1;
//
//					//
//					// System.out.println(father);
//					// System.out.println(fatherIndex);
//					// System.out.println(Arrays.toString(lineCols.toArray()));
//
//					Format.removeToIndexInt(lineCols11, fatherIndex);
//					Format.removeToIndexInt(lineCols, fatherIndex);
//					Format.removeToIndexInt(lineIds, fatherIndex);
//					Format.removeToIndex(lineValues, fatherIndex);

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
//			this.requete.append("DROP TABLE IF EXISTS " + this.tempTableI + " cascade;");
//			this.requete.append("CREATE ");
//			if (!this.tempTableI.contains(".")) {
//				this.requete.append("TEMPORARY ");
//			}
//			this.requete.append(" TABLE " + this.tempTableI + " "+FormatSQL.WITH_NO_VACUUM+" as select * from " + this.tempTableA + " where 1=0;");

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
//			this.requete.append("alter table " + this.tempTableA + " add i_" + this.currentTag + " " + this.numBdType + ";");
			
			// } catch (SQLException e) {
			// 666 LoggerHelper.errorGenTextAsComment(getClass(), "index()", LOGGER, ex);
			// e.printStackTrace();
			// }

		} else {
			// ajouter 1 a son index si la colonne existe dejà

			this.col.put(this.currentTag, (Integer) (o) + 1);

			// if (rootDistance.get(currentTag)!=distance)
			// {
			// throw new
			// SAXParseException("Fichier XML non pris en charge : rubrique placée diferemment dans l'arbre xml  : "+currentTag,"","",0,0);
			// }

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

		// si on retrouve la colonne parmi les colonnes de la ligne en court, on
		// procède à son insertion

		// System.out.println("***");
		// System.out.println(currentTag);
		// System.out.println(father);
		// System.out.println(leafStatus);

		// if (treeStackFatherLag.indexOf(father)>=0)
		// {
		// System.out.println(leafStatus);
		// System.out.println("***");
		// }

		// if (lineCols.indexOf(currentTag)>=0)

		/*
		 * quand le pere de la rubrique fermée est retrouvée et que ce n'est pas
		 * une feuille qui vient d'etre fermée on va réaliser l'insertion de la
		 * ligne
		 */

		if (this.treeStackFatherLag.indexOf(this.father) >= 0 && this.leafStatus == false
		// modif : updated
		// && updated==false
		) {

			// System.out.println(currentTag+" -- Insertion");
			// try {
			// nbInsert++;

			// if (pst==null)
			// {
			// pst=connexion.prepareStatement(Format.prepareStatementBuilder(colData,allCols));
			// }
			// Format.prepareStatementFiller(pst, fileName, colData, allCols,
			// lineCols, lineIds, lineValues);
			//
			insertQueryBuilder(this.requete,this.tempTableA, this.fileName, this.lineCols, this.lineIds, this.lineValues);

			// statement.addBatch(Format.insertQueryBuilder(tableName, nbInsert,
			// lineCols, lineIds, lineValues));

			if (this.requete.length() > FormatSQL.TAILLE_MAXIMAL_BLOC_SQL) {
				//LoggerDispatcher.info("Insertion :" + requete.length() + " caractères ", logger);

				try {
					UtilitaireDao.get("arc").executeImmediate(this.connexion, this.requete);
				} catch (SQLException ex) {
					LoggerHelper.errorGenTextAsComment(getClass(), "startElement()", LOGGER, ex);
					//e.printStackTrace();
					 throw new SAXParseException("Fichier XML : erreur de requete insertion  : "+ex.getMessage() , "", "", 0, 0);
				}
				this.requete.setLength(0);
				this.start=0;
			}

			// if (nbInsert%2==0){
			// // System.out.println("commit");
			// // statement.executeBatch();
			// Statement st = connexion.createStatement();
			// System.out.println(requete);
			// requete=Format.executeBlock(st, requete);
			//
			// //pst.executeBatch();
			// }
			//
			// } catch (SQLException e) {
			// 666 LoggerHelper.errorGenTextAsComment(getClass(), "index()", LOGGER, ex);
			// e.printStackTrace();
			// }

			// on ajoute à la hashmap updateNeeded le pere : si j'amais on
			// retrouve une feuille directe relative à ce pere, faudra faire un
			// update des lignes préalablement insérées

			// modif : updated
			// updateNeeded.put(father, col.get(father));

			// On va alors dépiler ligneCols, lineIds, lineValues jusqu'au père
			// de la rubrique
			int fatherIndex = this.lineCols11.lastIndexOf(this.allCols.indexOf(this.father)) + 1;

			//
			// System.out.println(father);
			// System.out.println(fatherIndex);
			// System.out.println(Arrays.toString(lineCols.toArray()));

			Format.removeToIndexInt(this.lineCols11, fatherIndex);
			Format.removeToIndexInt(this.lineCols, fatherIndex);
			Format.removeToIndexInt(this.lineIds, fatherIndex);
			Format.removeToIndex(this.lineValues, fatherIndex);

			// lineCols11=lineCols11.subList(0, fatherIndex);
			// lineCols=lineCols.subList(0, fatherIndex);
			// lineIds=lineIds.subList(0, fatherIndex);
			// lineValues=lineValues.subList(0, fatherIndex);
		}

		// updated=false;

		this.treeStackFather.add(this.father);

		this.treeStack.add(this.currentTag);
		this.father = this.currentTag;

		this.leafPossible = true;
		this.lineCols11.add(this.allCols.indexOf(this.currentTag));
		this.lineCols.add(this.allCols.indexOf(this.currentTag));
		this.lineIds.add(this.col.get(this.currentTag));
		this.lineValues.add(null);

		// System.out.println("Parent :"+ligne.size());
		// System.out.println("colonne : "+ currentTag + " / n°"+
		// ((Integer)col.get(currentTag)));

		//
		// ligne.add(name);

		// for (int i = 0; i < atts.getLength(); i++) {
		// //
		// System.out.println("attribut "+i+" - "+atts.getName(i)+" : "+atts.getValue(i));
		// }
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
	public void insertQueryBuilder(StringBuilder aRequete, String tempTableI, String fileName, List<Integer> lineCols, List<Integer> lineIds,
			List<String> lineValues) throws SAXParseException {

//		System.out.println("*****");

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
//			System.out.println("****");
//
//			System.out.println(entry.getValue());
//			System.out.println(keep.get(entry.getKey()));


			if (entry.getValue().equals(keep.get(entry.getKey())))
			{
				doNotinsert.put(entry.getKey(), true);
				//System.out.println(true);

			}
		}
//		System.out.println(tree);
//		System.out.println(doNotinsert);
//		System.out.println();


		this.keepLast=keep;


		StringBuilder req = new StringBuilder();
		StringBuilder req2 = new StringBuilder();
		this.idLigne++;
		//req.append("insert into " + tempTableI + "(id_source, id, date_integration, id_norme, periodicite, validite");
		
		req.append("insert into " + tempTableI + 
				"(" + tempTableAColumnsShortName.get(tempTableAColumnsLongName.indexOf("id_source")) +
				"," + tempTableAColumnsShortName.get(tempTableAColumnsLongName.indexOf("id")) +
				"," + tempTableAColumnsShortName.get(tempTableAColumnsLongName.indexOf("date_integration")) +
				"," + tempTableAColumnsShortName.get(tempTableAColumnsLongName.indexOf("id_norme")) +
				"," + tempTableAColumnsShortName.get(tempTableAColumnsLongName.indexOf("periodicite")) +
				"," + tempTableAColumnsShortName.get(tempTableAColumnsLongName.indexOf("validite")));
		
		req2.append("('" + fileName + "',"+this.idLigne+",current_date,'"+normeCourante.getIdNorme()+"','"+normeCourante.getPeriodicite()+"','"+validite+"'");
		
		int z=0;

		for (int i = 0; i < lineCols.size(); i++) {

			// si le bloc est a reinséré (le pere n'est pas retrouvé dans doNotInsert
			// ou si la colonne est un noeud, on procède à l'insertion
//			System.out.println("*** "+lineCols.get(i));
//			System.out.println(doNotinsert.get(tree.get(lineCols.get(i)))==null || tree.containsValue(lineCols.get(i)));

			if (doNotinsert.get(this.tree.get(lineCols.get(i)))==null || this.treeNode.get(lineCols.get(i))!=null)
			{
				
				req.append(",i" + this.allCols.indexOf(this.allCols.get(lineCols.get(i))));
			    req2.append(","+ lineIds.get(i));

			if (lineValues.get(i) != null) {
				req.append(",v" + this.allCols.indexOf(this.allCols.get(lineCols.get(i))));
			    req2.append(",'" + lineValues.get(i) + "'");
			}
			}

//			else
//			{
//			    System.out.println("No insert !");
//
//			}


		}

		 req.append(")values");
		 req2.append(")");

		 // attention; on doit insérer au bon endroit;
		z=aRequete.indexOf(req.toString(),this.start);

		if (z>-1)
		{
		  //  System.out.println("trouvé : "+start+" at "+z);

		    req2.append(",");
		    aRequete.insert(z+req.length(), req2);
		}
		else
		{
		    req2.append(";");
		    aRequete.append(req);
		    aRequete.append(req2);
		}
		//System.out.println(start);

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
//          StringBuilder reqCreate = new StringBuilder("WITH noview as (select false as n) \n");
            StringBuilder reqCreate = new StringBuilder(" \n");

            StringBuilder reqInsert = new StringBuilder();
            reqInsert.append(" INSERT INTO {table_destination} (id,id_source,date_integration,id_norme,validite,periodicite");

            StringBuilder reqSelect = new StringBuilder();
            reqSelect.append("\n SELECT row_number() over (), ww.* FROM (");
            reqSelect.append("\n SELECT '{nom_fichier}',current_date,'{id_norme}','{validite}','{periodicite}'");

            StringBuilder reqFrom = new StringBuilder();

            int d=0;

            for (int i = 0; i < arr.length; i++) {

                // pour chaque noeud
//              select distinct i2 m2 ,i0
//              ,case when i3 is null then max(i3) over (partition by i2) else i3 end as i3
//              ,case when i3 is null then max(v3) over (partition by i2) else v3 end as v3
//              ,case when i4 is null then max(i4) over (partition by i2) else i4 end as i4
//              ,case when i4 is null then max(v4) over (partition by i2) else v4 end as v4
//              ,case when i5 is null then max(i5) over (partition by i2) else i5 end as i5
//              ,case when i5 is null then max(v5) over (partition by i2) else v5 end as v5
//               FROM arc.I where i2 is not null

                if (arr[i][2] == 1) {

                    String leaf = Format.getLeafs(arr[i][1], arr, this.colData, this.allCols);

                    // créer les vues

                    // Version avec fonctions d'aggregation
//                    String leafMaxWhenNull = Format.getLeafsMaxWhenNull2(arr[i][1], arr, this.colData, this.allCols);
//                  reqCreate.append("CREATE TEMPORARY TABLE t_" + allCols.get(arr[i][1]) + " as (select distinct i_" + allCols.get(arr[i][1]) + " m_" + allCols.get(arr[i][1]) + " ");
//                  if (arr[i][0] >= 0) {
//                      reqCreate.append(", i_" + allCols.get(arr[i][0]) + " ");
//                  }
//                  reqCreate.append(leafMaxWhenNull);
//                  reqCreate.append(" FROM {table_source} where i_" + allCols.get(arr[i][1]) + " is not null); \n");



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

//            try {
//              this.jointure=StringCompress.compress(req.toString());
//            } catch (IOException e) {
//              666 LoggerHelper.errorGenTextAsComment(getClass(), "index()", LOGGER, ex);
//              e.printStackTrace();
//            }
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
