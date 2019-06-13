package fr.insee.arc_essnet.core.model.extraction;

import java.util.ArrayList;
import java.util.List;


public class Rubrique {

	//Nom de la rubrique
	private RubriqueEnum name;

	//condition sur la présence obligatoire d'une valeur pour cette rubrique
	private boolean isValeurObligatoire;
	
	//liste des valeurs à retenir lors de l'extraction 
	private List<String> listeValeursRetenues = new ArrayList<>();

	
	
	/******************************************************************************************************************************************************/
	/******************************************************* GETTERS ET SETTERS ***************************************************************************/
	/******************************************************************************************************************************************************/
	public Rubrique(RubriqueEnum name) {
		super();
		this.name = name;
	}
	
	public RubriqueEnum getName() {
		return name;
	}

	public void setName(RubriqueEnum name) {
		this.name = name;
	}
	
	
	public boolean isValeurObligatoire() {
		return isValeurObligatoire;
	}

	public void setValeurObligatoire(boolean isValeurObligatoire) {
		this.isValeurObligatoire = isValeurObligatoire;
	}

	public List<String> getListeValeursRetenues() {
		return listeValeursRetenues;
	}

	public void setListeValeursRetenues(List<String> listeValeursRetenues) {
		this.listeValeursRetenues = listeValeursRetenues;
	}

	/******************************************************************************************************************************************************/
	/******************************************************* METHODES   ***************************************************************************/
	/******************************************************************************************************************************************************/
	
	
//	public static Set<Rubrique> obtenirRubriquesSelectionnees(){
//		
//		Set<Rubrique> rubriquesSelectionnes = new HashSet<>();
//		
//		for (RubriqueEnum name : RubriqueEnum.TOUS.obtenirTouteSaDescendanceElleYC()) {
//			
//			String estSelectionnee = PREFIX_NOM_PROPERTIES_SELECT+name.toString().toLowerCase();
//			
//			boolean isSelectionnee = config.getBoolean(estSelectionnee);
//			
//			if (isSelectionnee){
//				Rubrique r = new Rubrique(name);
//				
//				rubriquesSelectionnes.add(r);
//				
//			}
//		}
//		
//		return rubriquesSelectionnes;
//		
//	}

	public boolean isValueAllowed(String input){
		
		boolean isValueAllowed = true;
		
		//est-ce qu'on impose la présence obligatoire d'une valeur pour cette rubrique?
		if (isValeurObligatoire){
			//null est representé par la valeur §§ dans les fichiers export
			isValueAllowed = (!(("§§").equals(input)));
		}
		//sinon y a t il une restriction sur certaines valeurs ?
		else if(!listeValeursRetenues.isEmpty()){
			String inputSansGuillemet = input.replace("\"", "");
			isValueAllowed = listeValeursRetenues.contains(inputSansGuillemet);
		}
		
		return isValueAllowed;
	}
	
	
	
	
	@Override
	public String toString(){
		return name.toString();
	}
	
	
}
