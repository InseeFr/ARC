package fr.insee.arc_essnet.core.service.extraction;

import org.apache.log4j.Logger;

import fr.insee.arc_essnet.core.model.extraction.DemandeExtraction;

public class ExtractionLanceur {

    private static final Logger LOGGER = Logger.getLogger(ExtractionLanceur.class);

	public static void main(String[] args) {

		LOGGER.info("Début du Traitement");
		
		//CREATION DE LA DEMANDE 
		DemandeExtraction demandeExtraction = new DemandeExtraction();
		
		//EXECUTION DE LA DEMANDE
	    ExtractionService extractionService = new ExtractionService(); 
	    
	    extractionService.executerLaDemande(demandeExtraction);
	    LOGGER.info("Fin du Traitement");
	    
	  }
	
}
