package fr.insee.arc_essnet.core.service.extraction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import fr.insee.arc_essnet.core.model.extraction.DemandeExtraction;
import fr.insee.arc_essnet.core.model.extraction.Rubrique;
import fr.insee.config.InseeConfig;

public class ExtractionService {

    private static final Logger LOGGER = Logger.getLogger(ExtractionService.class);

    private final int nbThreads = InseeConfig.getConfig().getInt("fr.insee.arc.threads.nombre");

    private FileWriter fichierEnSortie;

    private BufferedWriter bufferedWriter;

    private FolderScanner fScan = new FolderScanner();

    private List<Rubrique> rubriqueARecuperer;

    // Répertoire où sont lus les fichiers sources dezzippés
    private String pathFichiersCSVEnLecture;

    /***********************************************************************************************/
    /***************************************
     * GETTERS ET SETTERS
     *************************************/
    /***********************************************************************************************/

    public ExtractionService() {
	super();
    }

    public String getPathFichiersCSVEnLecture() {
	return pathFichiersCSVEnLecture;
    }

    public void setPathFichiersCSVEnLecture(String pathFichiersCSVEnLecture) {
	this.pathFichiersCSVEnLecture = pathFichiersCSVEnLecture;
    }

    public FileWriter getFichierEnSortie() {
	return fichierEnSortie;
    }

    public void setFichierEnSortie(FileWriter fichierEnSortie) {
	this.fichierEnSortie = fichierEnSortie;
    }

    public List<Rubrique> getRubriqueARecuperer() {
	return rubriqueARecuperer;
    }

    public void setRubriqueARecuperer(List<Rubrique> rubriqueARecuperer) {
	this.rubriqueARecuperer = rubriqueARecuperer;
    }

    public FolderScanner getFolderScanner() {
	return fScan;
    }

    public void setFolderScanner(FolderScanner folderScanner) {
	this.fScan = folderScanner;
    }

    /***********************************************************************************************/
    /******************************************
     * METHODES
     *******************************************/
    /***********************************************************************************************/

    public void executerLaDemande(DemandeExtraction dExt) {

	LOGGER.info("DEBUT du traitement de la demande...");
	// Initialisation
	pathFichiersCSVEnLecture = dExt.getRepertoireDeTravail();
	rubriqueARecuperer = dExt.getRubriques();

	// Création du fichier en sortie
	String fichierResultat = dExt.recupererCheminFichierResultatComplet();

	try (FileWriter fileWriter = new FileWriter(fichierResultat)) {
	    this.fichierEnSortie = (FileWriter) WriteCSV.creerUnFichierCSVAvecColonne(fileWriter, fichierResultat,
		    rubriqueARecuperer);
	    this.bufferedWriter = new BufferedWriter(fichierEnSortie);
	    // Scanner le répertoire source pour récupérer la liste de fichiers à traiter
	    Set<File> listeDeFichiers = fScan.recupererListeFichierATraiter(dExt.getDateCreationMinimaleFichiers(),
		    dExt.getDateCreationMaximaleFichiers());

	    // Puis on lance l'extraction
	    try {
		int compteur = 0;
		ExecutorService executor = Executors.newFixedThreadPool(nbThreads);
		for (File file : listeDeFichiers) {

		    compteur++;
		    Runnable thread = new copierEnParralele(file, compteur);
		    executor.execute(thread);

		}

		executor.shutdown();
		while (!executor.isTerminated()) {
		}
	    } finally {
		// on ferme le fichier en sortie en cas de plantage
		cleanResources();
	    }

	    LOGGER.info("FIN du traitement de la demande. Le resutat est dans ce fichier: " + fichierResultat);

	} catch (IOException e1) {

	}

    }

    private void cleanResources() {
	try {
	    bufferedWriter.flush();
	    fichierEnSortie.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public class copierEnParralele implements Runnable {

	File file;

	int numeroDansPileTraitement;

	public int getNumeroDansPileTraitement() {
	    return numeroDansPileTraitement;
	}

	public void setNumeroDansPileTraitement(int numeroDansPileTraitement) {
	    this.numeroDansPileTraitement = numeroDansPileTraitement;
	}

	public File getFile() {
	    return file;
	}

	public void setFile(File file) {
	    this.file = file;
	}

	public copierEnParralele(File file, int numero) {
	    super();
	    this.file = file;
	    this.numeroDansPileTraitement = numero;
	}

	@Override
	public void run() {
	    extraireDonnees(file);
	}

	private void extraireDonnees(File file) {

	    String nomFichierGzip = file.toString();
	    String nomFichierCSV = file.getName().replace(".gz", "");
	    String pathFichierEnLecture = pathFichiersCSVEnLecture + "/" + nomFichierCSV;

	    ReadCSV readCSV = new ReadCSV(pathFichierEnLecture);
	    Set<String> lignesLues = new HashSet<>();

	    LOGGER.info("Traitement du fichier numero: " + numeroDansPileTraitement
		    + " - Extraction donnees depuis le fichier: " + nomFichierCSV);

	    try {

		OutilsZip.gunzip(nomFichierGzip, pathFichierEnLecture);
		lignesLues = readCSV.lireContenu(rubriqueARecuperer);

		if (!lignesLues.isEmpty()) {

		    // écriture dans le fichier

		    for (String ligne : lignesLues) {
			bufferedWriter.write(ligne);
		    }

		}
	    } catch (IOException e) {
		e.printStackTrace();
	    } finally {
		WriteCSV.supprimerFichier(pathFichierEnLecture);
	    }

	}

    }

}
