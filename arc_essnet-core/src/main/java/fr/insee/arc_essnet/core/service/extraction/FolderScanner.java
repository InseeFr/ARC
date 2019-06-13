package fr.insee.arc_essnet.core.service.extraction;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import fr.insee.arc_essnet.core.model.extraction.ScanException;
import fr.insee.config.InseeConfig;

public class FolderScanner {
	
	
    private static final Logger LOGGER = Logger.getLogger(FolderScanner.class);
	
	  private final Path cheminFichiersSource = recupererCheminFichiersSource();
	  
	  private final String extensionDesFichiers = InseeConfig.getConfig().getString("fr.insee.arc.repertoire.extension");

	  
	  //Pour stocker les fichiers trouvés dans répertoire
	  private Set<File> fichiersDuRepertoire = new HashSet<File>();  
	  
		
		/***********************************************************************************************/
	  	/************************************** GETTERS ET SETTERS *************************************/
	  	/***********************************************************************************************/
	  
	  	public Path getCheminFichiersSource() {
			return cheminFichiersSource;
		}


		public String getFilter() {
			return extensionDesFichiers;
		}


	
		public Set<File> getFichiersDuRepertoire() {
			return fichiersDuRepertoire;
		}

		public void setFichiersDuRepertoire(Set<File> fichiersDuRepertoire) {
			this.fichiersDuRepertoire = fichiersDuRepertoire;
		}
	  
	  
	  
	  
	  
		/***********************************************************************************************/
	  	/*************************************** METHODES **********************************************/
	  	/***********************************************************************************************/
	  
	  /**
	  * Méthode qui se charge de scanner le répertoire
	  * @throws ScanException
	  */
	  public Set<File> scanner() throws ScanException{
	    
		  Long debut = System.currentTimeMillis();
				  
		//Si le chemin n'est pas valide, on lève une exception
	    if(cheminFichiersSource == null || cheminFichiersSource.equals(""))
	      throw new ScanException("Chemin a scanner non valide (vide ou null) !");
			
	      LOGGER.info("Scan du dossier : " + cheminFichiersSource + "a la recherche des fichiers ayant cette extension " + this.extensionDesFichiers);
	    	     
	      //Maintenant, on filtre le contenu de ce même dossier sur le filtre défini
	      try(DirectoryStream<Path> listing = Files.newDirectoryStream(cheminFichiersSource, this.extensionDesFichiers)){
	    	 
	    	 for(Path nom : listing){
	    		 selectionnerFichier(nom);
	    	 }
	    	  
	    	 
	      } catch (IOException e) {	
	    	  e.printStackTrace(); 
	    	 }
		
	    Long fin = System.currentTimeMillis();  
	    LOGGER.info("Duree du scan: "+(fin-debut) );
		
	    
	    return fichiersDuRepertoire;
	  }

	  
	  public Set<File> recupererListeFichierATraiter(Date dateCreationMinFichier, Date dateCreationMaxFichier) {
			
	
			Set<File> listeDeFichiers = new HashSet<>();
			
			try {
				listeDeFichiers = scannerFichiersSelonDateCreation(dateCreationMinFichier,dateCreationMaxFichier);
			} catch (ScanException e1) {
				e1.printStackTrace();
			}
			
			return listeDeFichiers;
		}
	  
	  
	  
	
	
	

	private Set<File> scannerFichiersSelonDateCreation(	Date dateCreationMinFichier, Date dateCreationMaxFichier) throws ScanException{
		 Long debut = System.currentTimeMillis();
		  
			//Si le chemin n'est pas valide, on lève une exception
		    if(cheminFichiersSource == null || cheminFichiersSource.equals(""))
		      throw new ScanException("Chemin à scanner non valide (vide ou null) !");
				
		      LOGGER.info("Scan du dossier : " + cheminFichiersSource + " à la recherche des fichiers portant l'extension " + this.extensionDesFichiers);
		    	     
		      //Maintenant, on filtre le contenu de ce même dossier sur le filtre défini
		      try(DirectoryStream<Path> listing = Files.newDirectoryStream(cheminFichiersSource, this.extensionDesFichiers)){
		    	 
		    	 for(Path nom : listing){
		    		 selectionnerFichierSelonDateCreation(nom,dateCreationMinFichier,dateCreationMaxFichier);
		    	 }
		    	  
		    	 
		      } catch (IOException e) {	
		    	  e.printStackTrace(); 
		    	 }
			
		    Long fin = System.currentTimeMillis();  
		    LOGGER.info("Duree du scan: "+(fin-debut) );
			
		    LOGGER.info(fichiersDuRepertoire.size() + " fichier(s) portent l'extension " + extensionDesFichiers+" avec date de creation entre le: "
		    			+ dateCreationMinFichier + " et le : " +dateCreationMaxFichier);  
		    
		    return fichiersDuRepertoire;
	}


	private boolean selectionnerFichier(Path nom) {
		
		
		try {
			
				File file = nom.toFile();
				//on garde si superieur à la date min de sélection
				fichiersDuRepertoire.add(file);
				return true;

		} catch (Exception e) {
				e.printStackTrace();
				return false;
		}

			
	}

	/**
	 * Sélectionner le fichier si sa date de création se situe entre les dates Min et Max passées en paramètres
	 * @param nom
	 * @param dateCreationMinFichier
	 * @param dateCreationMaxFichier
	 */
	private boolean selectionnerFichierSelonDateCreation(Path nom, Date dateCreationMinFichier, Date dateCreationMaxFichier) {
		
		boolean isSelected = false;
		String dateFromFileName = nom.getFileName().toString().substring(0,8);
		Date dateCreation = DateUtils.newDateAAAAMMJJ(dateFromFileName);
		
				
		//Cas 1 : dateMin renseignée mais pas dateMax
		if (dateCreationMinFichier!=null && dateCreationMaxFichier==null){
			if(dateCreation.after(dateCreationMinFichier) || dateCreation.equals(dateCreationMinFichier)){
				isSelected = selectionnerFichier(nom);
			}
		}else if (dateCreationMinFichier!=null && dateCreationMaxFichier!=null){
			//Cas 2 : dateMin et DateMax renseignée
			if ((dateCreation.before(dateCreationMaxFichier)&& (dateCreation.after(dateCreationMinFichier))) || dateCreation.equals(dateCreationMinFichier) || dateCreation.equals(dateCreationMaxFichier)){
				isSelected = selectionnerFichier(nom);
			}
		}
		
		return isSelected;
			
	}
	
	
	private Path recupererCheminFichiersSource(){
		
		Path chemin = null;
		String cheminVersFichiersSource = InseeConfig.getConfig().getString("fr.insee.arc.batch.parametre.repertoire");
		
		String nomRepertoire ="ARC_PROD/EXPORT";
		String cheminComplet = cheminVersFichiersSource+nomRepertoire;
		chemin = Paths.get(cheminComplet);
		return chemin;
		
	}

}
