package fr.insee.arc_essnet.core.model.extraction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.joda.time.DateTime;

import fr.insee.arc_essnet.core.service.extraction.DateUtils;
import fr.insee.config.InseeConfig;

public class DemandeExtraction {
	
	private static final Logger LOGGER = Logger.getLogger(DemandeExtraction.class);
	
	//Rubriques à récupérer
	private List<Rubrique> rubriques = new ArrayList<Rubrique>();
    
	//Rubrique validité obligatoire
	Rubrique validite = new Rubrique(RubriqueEnum.V_S20_G00_05_005);
	
    //Nom du fichier où sera copié le résultat de la requête
  	private final String nomFichierResultat = InseeConfig.getConfig().getString("fr.insee.arc.nom.resultat");
  	
  	//Répertoire de travail
  	private String repertoireDeTravail;
  	
  	//Date de création minimiale des fichiers
  	private Date dateCreationMinimaleFichiers; 
  	
  	//Date de création maximiale des fichiers
  	private Date dateCreationMaximaleFichiers;
  	
  	
  	
  	/***********************************************************************************************/
  	/***************************************GETTERS ET SETTERS *************************************/
  	/***********************************************************************************************/

	public DemandeExtraction() {
		
		super();
		//On impose que la validité soit une rubrique obligatoire dans la demande,
		//on l'initialize ici.
		validite.setListeValeursRetenues(new ArrayList<String>());
		rubriques.add(validite);
		
	}

	
		
  	public String getNomFichierResultat() {
		return nomFichierResultat;
	}


	

	public Date getDateCreationMinimaleFichiers() {

		//on récupère la validité la plus ancienne parmi celles demandées saisie par l'utilisateur
		Date validitePlusAncienne = obtenirValiditeLaPlusAncienne();
		//la date de chargement min est fonction de la validité: si mois validité = m alors dateChargement = m-1 et j=18
		Date dateChargementMinSelonValiditePlusAncienne = obtenirDateChargementMinSelonValidite(validitePlusAncienne);
		
		if (dateCreationMinimaleFichiers==null || dateCreationMinimaleFichiers.before(dateChargementMinSelonValiditePlusAncienne)){
			return dateChargementMinSelonValiditePlusAncienne;
		}else{
			return dateCreationMinimaleFichiers;
		}
		
	}



	


	public void setDateCreationMinimaleFichiers(Date dateCreationMinimaleFichiers) {
		this.dateCreationMinimaleFichiers = dateCreationMinimaleFichiers;
	}


	public Date getDateCreationMaximaleFichiers() {
		return dateCreationMaximaleFichiers;
	}


	public void setDateCreationMaximaleFichiers(
			Date dateCreationMaximaleFichiers) {
		this.dateCreationMaximaleFichiers = dateCreationMaximaleFichiers;
	}



	public String getRepertoireDeTravail() {
		return repertoireDeTravail;
	}


	public void setRepertoireDeTravail(String repertoireDeTravail) {
		this.repertoireDeTravail = repertoireDeTravail;
	}


	public List<String> getPeriodeValidites() {
		return validite.getListeValeursRetenues();
	}


	public void setPeriodeValidites(List<String> periodeValidites) {
		this.validite.setListeValeursRetenues(periodeValidites);
		
	}


	public List<Rubrique> getRubriques() {
		return rubriques;
	}

	

	public void ajouterRubriques(List<Rubrique> rubriques) {
		this.rubriques.addAll(rubriques);
	}


	public String recupererCheminFichierResultatComplet() {
		return repertoireDeTravail+"/"+nomFichierResultat;
	}
		
	/**************************************************************************************/
	/********************************METHODES PUBLIQUES ***********************************/
	/**************************************************************************************/
	
	public void renseignerRepertoireDeTravail(String bacASable){

		
		String numeroBacASable="";
		String cheminFiles = InseeConfig.getConfig().getString("fr.insee.arc.batch.parametre.repertoire");
		
		//récuperation du numéro de bac à sable (uniquement pour n° bac à sable > 1)
		if(!bacASable.endsWith("1")){
			numeroBacASable = bacASable.substring(bacASable.length() - 1); 
		}
		
		//Construction du chemin  
		repertoireDeTravail = cheminFiles+"ARC_BAS"+numeroBacASable+"\\EXPORT";
		
		//on fait le ménage dans le répertoire
		viderRepertoire(repertoireDeTravail);
		
		
	}

	
	/**************************************************************************************/
	/********************************METHODES PRIVEES *************************************/
	/**************************************************************************************/

	
	
	/*
	 * Méthode qui paramètre le logger pour que celui-ci envoie la log dans le fichier du 
	 * bac à sable séléctionné par l'utilisateur
	 */
	public void initLog4JExtractionAppender() {
		
		LOGGER.info("méthode initLog4JHTMLAppender");
		
		//Voici le logger spécifique au service d'extraction
		Logger monLogger = Logger.getLogger("fr.insee.arc_essnet.core.service.extraction");
		
		//Voici l'appender qui va écrire dans le fichier de log du bac à sable séléctionné par l'utilisateur
		FileAppender monAppender = (FileAppender) monLogger.getAppender("ExtractionAppender");
		
		//Voici le chemin d'ecriture
		String cheminHTMLLog = repertoireDeTravail+"/bilan.txt";
		
		//Paramétrage de l'appender. 
		if(monAppender==null){
			//S'il n'est pas encore initializé, je le crée
			PatternLayout layout = new PatternLayout("%5p %d{DATE} - %X{UserID} - %c{1}:%-4L - %m%n");
			
			try {
				monAppender = new FileAppender(layout, cheminHTMLLog);
				monAppender.setName("ExtractionAppender");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else{
			//Sinon, je lui passe le bon chemin d'ecriture
			monLogger.removeAppender("ExtractionAppender");
			monAppender.setFile(cheminHTMLLog);
		}
		
		//On ajoute le fichier au logger
		monAppender.activateOptions();
		monLogger.addAppender(monAppender);
		
		LOGGER.info("La log est écrite dans le fichier .txt suivant: "+cheminHTMLLog);
		
	}	
	
	
	
	private Date obtenirValiditeLaPlusAncienne() {
		
		//on récupère la liste des validités selectionnées par l'utilisateur
		List<String> validitesSelectionnees = validite.getListeValeursRetenues();
		//on recherche la validité la plus ancienne
		Date validitePlusAncienneSelectionnee = null;
		if (!validitesSelectionnees.isEmpty()){
			validitePlusAncienneSelectionnee = DateUtils.getOlderValiditeDate(validitesSelectionnees);
		}
		
		return validitePlusAncienneSelectionnee;
	}
	
	private Date obtenirDateChargementMinSelonValidite(Date validite){
		
		//règle: on recoit à partir du 18 du mois m la validité du mois m-1
		DateTime dateTimeValidite = new DateTime(validite);

		dateTimeValidite = dateTimeValidite.minusMonths(1);
		dateTimeValidite = dateTimeValidite.withDayOfMonth(18);
		
		return dateTimeValidite.toDate();
		
	}
	
	private void viderRepertoire(String pathFile){
		
		File file = new File(pathFile);
		
		if (file.exists()){
			 
			File[] fichiersDuRepertoire = file.listFiles();
	        for (int i=0; i<fichiersDuRepertoire.length; i++) {
	          fichiersDuRepertoire[i].delete();
	        }
	        
		}else{
			file.mkdirs();
		}

	}
}
