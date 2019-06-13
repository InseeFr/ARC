package fr.insee.arc_essnet.core.service.extraction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import fr.insee.arc_essnet.core.model.extraction.Rubrique;
import fr.insee.arc_essnet.utils.utils.LoggerDispatcher;

public class ReadCSV {

    private static final Logger LOGGER = Logger.getLogger(ReadCSV.class);

    private static final String NEW_LINE_SEPARATOR = "\n";

    private static final String CVS_SPLIT_BY = ";";

    private String csvFile;

    private String fileName;

    private HashMap<String, Integer> elementRankMap;

    private int nbColumnsInFile = 0;

    public ReadCSV(String csvFile) {
	super();
	this.csvFile = csvFile;
	this.fileName = new File(csvFile).getName();
    }

    public String getCsvFile() {
	return csvFile;
    }

    public void setCsvFile(String csvFile) {
	this.csvFile = csvFile;
    }

    public Set<String> lireContenu(List<Rubrique> rubriqueARecuperer) {
	String line = "";

	Set<String> lignes = new HashSet<>();

	try (FileReader fileReader = new FileReader(csvFile)) {
	    try (BufferedReader br = new BufferedReader(fileReader)) {

		// Rang des rubriques dans le fichier .csv
		getElementRank(rubriqueARecuperer, br);

		while ((line = br.readLine()) != null) {

		    StringBuilder valeurRubriques = extractElementFromLine(line, rubriqueARecuperer);

		    if (valeurRubriques != null) {
			lignes.add(valeurRubriques.toString());
		    }
		}
	    }

	} catch (IOException e) {
	    LoggerDispatcher.error("error in CSV read", e, LOGGER);
	}

	return lignes;
    }

    private void getElementRank(List<Rubrique> rubriqueARecuperer, BufferedReader br) throws IOException {

	String line;
	String[] ligne;

	// Initialize of the element map
	elementRankMap = creerMapNumeroColonneRubriques(rubriqueARecuperer);

	// read the header line
	if ((line = br.readLine()) != null) {

	    ligne = line.split(CVS_SPLIT_BY);
	    setNbColumnsInFile(ligne.length);

	    int i = 0;
	    for (String element : ligne) {

		try {

		    if (elementRankMap.containsKey(element)) {
			// Get the element rank in the file
			elementRankMap.put(element, i);
		    }

		} catch (IllegalArgumentException iea) {
		    LoggerDispatcher.error(
			    String.format("This element is not in the predifined elements in the app %s", element),
			    LOGGER);
		} finally {
		    i++;
		}

	    }
	}

	// Check for all column in the input file
	for (Map.Entry<String, Integer> entry : elementRankMap.entrySet()) {

	    if (entry.getValue() == null) {
		LOGGER.warn("Le fichier " + fileName + " ne contient pas la rubrique: " + entry.getKey() + " !!!");
	    }

	}

    }

    private StringBuilder extractElementFromLine(String line, List<Rubrique> elementToGet) {

	// Split line
	String[] ligne = line.split(CVS_SPLIT_BY);

	StringBuilder lineElementvalue = new StringBuilder();

	// on ajoute le nom du fichier au début de chaque ligne construite
	lineElementvalue.append("\"").append(fileName).append("\"");

	for (Rubrique element : elementToGet) {

	    lineElementvalue.append(CVS_SPLIT_BY);

	    Integer rang = elementRankMap.get(element.toString());

	    String curentValue = (rang == null) ? "" : ligne[rang];

	    // Check if value is refused
	    if (!element.isValueAllowed(curentValue)) {
		// we do nothing
		return null;
	    } else {
		// else add to other elements in that line
		lineElementvalue.append(curentValue);
	    }

	}

	lineElementvalue.append(NEW_LINE_SEPARATOR);

	// End reading,return found values
	return lineElementvalue;
    }

    // COnvert List<K> en Map<K,V>
    private HashMap<String, Integer> creerMapNumeroColonneRubriques(List<Rubrique> rubriqueARecuperer) {

	HashMap<String, Integer> map = new HashMap<>();

	for (Rubrique k : rubriqueARecuperer) {
	    map.put(k.toString(), null);
	}

	return map;

    }

    public String getFileName() {
	return fileName;
    }

    public void setFileName(String nomFichier) {
	this.fileName = nomFichier;
    }

    public int getNbColumnsInFile() {
	return nbColumnsInFile;
    }

    public void setNbColumnsInFile(int nbColumnsInFile) {
	this.nbColumnsInFile = nbColumnsInFile;
    }

}
