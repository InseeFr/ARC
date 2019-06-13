package fr.insee.arc_essnet.core.service.extraction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import fr.insee.arc_essnet.core.model.extraction.Rubrique;
import fr.insee.arc_essnet.utils.utils.LoggerDispatcher;

public class WriteCSV {

    private static final Logger LOGGER = Logger.getLogger(WriteCSV.class);

    // Delimiter used in CSV file
    private static final String COMMA_DELIMITER = ";";
    private static final String NEW_LINE_SEPARATOR = "\n";

    // Validite
    private String validite;

    // Fichier en écriture
    FileWriter fichierEnEcriture;

    public FileWriter getFichierEnEcriture() {
	return fichierEnEcriture;
    }

    public void setFichierEnEcriture(FileWriter fichierEnEcriture) {
	this.fichierEnEcriture = fichierEnEcriture;
    }

    public WriteCSV(FileWriter fichierEnEcriture) {
	super();
	this.fichierEnEcriture = fichierEnEcriture;
    }

    public WriteCSV(String validite, FileWriter fichierEnEcriture) {
	super();
	this.validite = validite;
	this.fichierEnEcriture = fichierEnEcriture;
    }

    /*****************************************************************************************************/
    /**************************************
     * Méthodes
     ******************************************************/
    /*****************************************************************************************************/

    public static Writer creerUnFichierCSVAvecColonne(Writer fileWriter, String fichierResultat,
	    List<Rubrique> rubriques) {

	// Write the CSV file header
	// 1ere colonne, toujours égale au nom du fichier source
	try {
	    fileWriter.append("Fichier_Source");
	    fileWriter.append(COMMA_DELIMITER);
	    
	    boolean ajouterVirgule = false;
	    for (Rubrique rubrique : rubriques) {
		// A la premiere boucle, je n'ajoute pas de virgule,
		// sinon j'ajoute une virgule et je met à jour le boolean
		if (ajouterVirgule) {
		    fileWriter.append(COMMA_DELIMITER);
		} else {
		    ajouterVirgule = true;
		}
		
		fileWriter.append(rubrique.toString());
	    }
	    
	    // Add a new line separator after the header
	    fileWriter.append(NEW_LINE_SEPARATOR);
	} catch (IOException e) {
	    LoggerDispatcher.error("Error in creation CSV file",e, LOGGER);
	    
	}

	return fileWriter;
    }

    public static void supprimerFichier(String fileName) {
	File fichierToDelete = new File(fileName);
	fichierToDelete.delete();
    }

    public static boolean copier(Path source, Path destination) {
	try {
	    Files.copy(source, destination);

	} catch (IOException e) {
	    e.printStackTrace();
	    return false;
	}
	return true;
    }

    public void writeCsvFile(Set<String> lignesAEcrire) {

	try {
	    for (String ligne : lignesAEcrire) {
		fichierEnEcriture.write(ligne);
	    }
	} catch (Exception e) {
	    System.out.println("Une exception est levée");
	    System.out.println("Error: " + e.getMessage());
	    cleanUp();
	} finally {
	    try {
		fichierEnEcriture.flush();
	    } catch (IOException e) {
		System.out.println("Error while flushing/closing fileWriter !!!");
		e.printStackTrace();

	    }

	}

    }

    public void writeCsvFileAvecBufferisation(Set<String> lignesAEcrire) throws IOException {

	// on encapsule le fichier en ecriture dans un buffer
	Writer bufferedWriter = new BufferedWriter(fichierEnEcriture);

	// écriture dans le fichier
	for (String ligne : lignesAEcrire) {
	    bufferedWriter.write(ligne);
	}

    }

    public void cleanUp() {
	try {
	    fichierEnEcriture.close();
	} catch (IOException e) {

	    e.printStackTrace();
	}
    }

    public String getValidite() {
	return validite;
    }

    public void setValidite(String validite) {
	this.validite = validite;
    }

}
