package fr.insee.arc.core.service.p2chargement.xmlhandler;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

import fr.insee.arc.core.service.global.bo.FileIdCard;
import fr.insee.arc.core.service.p2chargement.dao.HandlerXMLDao;
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

	public XMLHandlerCharger4(Connection connexion, FileIdCard fileIdCard, String tempTableA) {
		super();
		this.dao = new HandlerXMLDao(connexion, fileIdCard, tempTableA, allCols, colData, tree, treeNode);
	}

	HandlerXMLDao dao;

	// output
	private String jointure = "";

	// map le nom des balises trouvé dans le fichier XML avec leur nombre
	// d'occurence en cours du traitement
	private Map<String, Integer> col = new HashMap<>();

	// on met dans cette map les balises pour lesquels le parser a trouvé de la
	// données
	// l'integer ne sert à rien -> refactor avec un set
	private Map<String, Integer> colData = new HashMap<>();

	private Map<Integer, Integer> tree = new HashMap<>();
	private Map<Integer, Boolean> treeNode = new HashMap<>();

	private Map<Integer, Integer> colDist = new HashMap<>();

	private int distance = 0;

	private String currentTag;

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

	// contient la liste de toutes les balises trouvées dans le XML
	private FastList<String> allCols = new FastList<>();
	private List<Integer> lineCols = new ArrayList<>();
	private List<Integer> lineCols11 = new ArrayList<>();
	private List<Integer> lineIds = new ArrayList<>();
	private List<String> lineValues = new ArrayList<>();

	// indique que la balise courante a des données
	private boolean hasData = false;

	/**
	 * Actions à réaliser sur les données
	 */
	@Override
	public void characters(char[] caracteres, int debut, int longueur) {
		String donnees = FormatSQL.quoteTextWithoutEnclosings(new String(caracteres, debut, longueur));

		// on concatène les données lues dans currentData
		// currentData est effacée quand on trouve un nouvel élement
		this.currentData.append(donnees);
		this.hasData = true;

	}

	/**
	 * Actions à réaliser lors de la fin du document XML.
	 *
	 * @throws SAXParseException
	 */
	@Override
	public void endDocument() throws SAXParseException {

		dao.insertQueryBuilder(this.lineCols, this.lineIds, this.lineValues);
		dao.execQueryInsertFinal(false);

		// construction de la requete de jointure
		this.jointure = dao.requeteJointureXML(this.colDist);

	}

	/**
	 * Actions à réaliser lors de la détection de la fin d'un élément.
	 *
	 * @throws SAXParseException
	 */
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXParseException {
		String closedTag = Format.convertIntoDatabaseColumnValidName(qName);
		// la condition de lecture est assez spéciale
		// on doit arriver à la fin du stream de l'element et concaténer toutes
		// les données trouvées jusqu'a ce qu'on en trouve plus
		if (closedTag.equals(this.currentTag) && this.hasData) {
			if (this.colData.get(this.currentTag) == null) {
				this.colData.put(this.currentTag, 1);

				dao.addValueColumn(this.currentTag);
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

		if (closedTag.equals(this.currentTag) && this.leafPossible) {

			// vérifier qu'une feuille n'existe pas déjà
			if (this.lineCols.indexOf(this.allCols.indexOf(closedTag)) < this.lineCols.size() - 1) {

				// réalisation de l'insertion
				dao.insertQueryBuilder(this.lineCols.subList(0, this.lineCols.size() - 1),
						this.lineIds.subList(0, this.lineCols.size() - 1),
						this.lineValues.subList(0, this.lineCols.size() - 1));

				int i = this.lineCols.indexOf(this.allCols.indexOf(closedTag));

				Integer g = this.lineCols.get(this.lineCols.size() - 1);
				Format.removeToIndexInt(this.lineCols, i);
				this.lineCols.add(g);

				g = this.lineIds.get(this.lineIds.size() - 1);
				Format.removeToIndexInt(this.lineIds, i);
				this.lineIds.add(g);

				g = this.lineCols11.get(this.lineCols11.size() - 1);
				Format.removeToIndexInt(this.lineCols11, i);
				this.lineCols11.add(g);

				String h = this.lineValues.get(this.lineValues.size() - 1);
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

		dao.initializeInsert();

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
	public void startElement(String uri, String localName, String qName, Attributes attributes)
			throws SAXParseException {
		this.currentTag = Format.convertIntoDatabaseColumnValidName(qName);
		this.currentData.setLength(0);
		this.hasData = false;

		// on ajoute les colonnes si besoin
		// on met à jour le numéro d'index
		Integer o = this.col.get(this.currentTag);

		// créer et enregistrer la colonne si elle n'existe pas
		if (o == null) {
			this.col.put(this.currentTag, 1);
			this.allCols.add(this.currentTag);

			dao.addIdColumn(this.currentTag);

		} else {
			// ajouter 1 a son index si la colonne existe dejà
			this.col.put(this.currentTag, o + 1);
		}

		this.distance++;
		this.tree.put(this.allCols.indexOf(this.currentTag), this.allCols.indexOf(this.father));
		this.treeNode.put(this.allCols.indexOf(this.father), true);

		if (this.colDist.get(this.allCols.indexOf(this.currentTag)) == null) {
			this.colDist.put(this.allCols.indexOf(this.currentTag), this.distance);
		} else {
			if (!this.colDist.get(this.allCols.indexOf(this.currentTag)).equals(this.distance)) {
				throw new SAXParseException(
						"Fichier XML non pris en charge : distance à la racine de la rubrique variante  : "
								+ this.currentTag,
						"", "", 0, 0);
			}
		}

		/*
		 * quand le pere de la rubrique fermée est retrouvée et que ce n'est pas une
		 * feuille qui vient d'etre fermée on va réaliser l'insertion de la ligne
		 */

		if (this.treeStackFatherLag.indexOf(this.father) >= 0 && this.leafStatus == false) {

			dao.insertQueryBuilder(this.lineCols, this.lineIds, this.lineValues);

			dao.execQueryInsert();

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



	public String getJointure() {
		return jointure;
	}

}
