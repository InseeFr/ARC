package fr.insee.arc_essnet.batch;

import fr.insee.arc_essnet.core.factory.ApiServiceFactory;
import fr.insee.arc_essnet.core.model.TypeTraitementPhase;
import fr.insee.config.InseeConfig;


public class LanceurUnitaire {

	/**
	 * 
	 * @param args
	 *            {@code args[0]} : service à invoquer<br/>
	 */
	
	public static void main(String[] args) {
		
		String nb="";
		if (
				args[0].equals(TypeTraitementPhase.INITIALIZE.toString())
			||	args[0].equals(TypeTraitementPhase.REGISTER.toString())
			)
		{
			nb = InseeConfig.getConfig().getString("fr.insee.arc.batch.parametre.nbFic");
		}
		else
		{
			nb = InseeConfig.getConfig().getString("fr.insee.arc.batch.parametre.nbEnr");
		}
		
		
		ApiServiceFactory.getService(args[0]
				, InseeConfig.getConfig().getString("fr.insee.arc.batch.parametre.env")
				, InseeConfig.getConfig().getString("fr.insee.arc.batch.parametre.envExecution")
				, InseeConfig.getConfig().getString("fr.insee.arc.batch.parametre.repertoire")
				, nb).invokeApi();
	}

}
