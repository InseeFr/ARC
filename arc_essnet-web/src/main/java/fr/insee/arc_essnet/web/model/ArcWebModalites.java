package fr.insee.arc_essnet.web.model;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import fr.insee.arc_essnet.core.model.extraction.RubriqueEnum;

/**
 * Classe permettant de gerer la liste des modalités à afficher dans les écrans 
 */
public final class ArcWebModalites {


	/**
	 * Constructeur par défaut
	 */
	public ArcWebModalites() {
		super();
	}

	
	/**
	 * Renvoie les dernières validités traitées à ce jours.
	 * Important: parametrée pour ne renvoyer que les 4 dernières validités (modifiable)
	 */
	public Set<String> obtenirListValidites() {
		
		Set<String> validitesDisponibles = new LinkedHashSet<String>();
		
		Calendar aujourdhui = Calendar.getInstance();
		int jour = aujourdhui.get(Calendar.DAY_OF_MONTH);
		int annee = aujourdhui.get(Calendar.YEAR);
		int mois = aujourdhui.get(Calendar.MONTH);
		int anneePrecedente=annee-1;
		HashMap<Integer, String> calendrier = new HashMap<>();
		
		
		calendrier.put(-5, "Août "+anneePrecedente);
		calendrier.put(-4, "Septembre "+anneePrecedente);
		calendrier.put(-3, "Octobre "+anneePrecedente);
		calendrier.put(-2, "Novembre "+anneePrecedente);
		calendrier.put(-1, "Décembre "+anneePrecedente);
		calendrier.put(0, "Janvier "+annee);
		calendrier.put(1, "Février "+annee);
		calendrier.put(2, "Mars "+annee);
		calendrier.put(3, "Avril "+annee);
		calendrier.put(4, "Mai "+annee);
		calendrier.put(5, "Juin "+annee);
		calendrier.put(6, "Juillet "+annee);
		calendrier.put(7, "Août "+annee);
		calendrier.put(8, "Septembre "+annee);
		calendrier.put(9, "Octobre "+annee);
		calendrier.put(10, "Novembre "+annee);
		calendrier.put(11, "Décembre "+annee);
		
		int derniereValiditeChargee;
		//validité traitée la plus récente = mois actuel moins deux si on se situe avant le 18 du mois actuel
		//							       = mois actuel mois un si on se situe après le 18 de mois actuel (18 inclus)
		if (jour<18){
			derniereValiditeChargee = mois-2;
		}else{
			derniereValiditeChargee = mois-1;
		}
		
		
		 //on veut récupèrer jusqu'à 4 mois de validité en arrière
		int premiereValidite = derniereValiditeChargee-3; 
		 for(int i = premiereValidite; i <= derniereValiditeChargee; i++){
			 
			 validitesDisponibles.add(calendrier.get(i));			 
		 }

		return validitesDisponibles;
	}

	/**
	 * 
	 * Renvoie la liste des rubriques au sein d'une map, triées par bloc (rubrique mère)
	 */
	public Map<String,Set<String>> obtenirRubriquesParBloc(){
		
		Map<String,Set<String>> rubriquesParBloc = new HashMap<String, Set<String>>();
		
		Map<RubriqueEnum, Set<RubriqueEnum>> map = RubriqueEnum.obtenirRubriquesParBloc();
		
		for (Entry<RubriqueEnum, Set<RubriqueEnum>> entry : map.entrySet()) {
			//Initialisation pour ce bloc
			Set<String> enfantsLibelle = new HashSet<>();
			String blocLibelle = entry.getKey().getLibelle();
			Set<RubriqueEnum> enfants = entry.getValue();
			
			//Si ce bloc est AUTRES, alors on retire la validité de ses sous-rubriques (car inutile pour le formulaire).
			if (entry.getKey().equals(RubriqueEnum.AUTRES)){
				enfants.remove(RubriqueEnum.V_S20_G00_05_005);
			}
			
			for (RubriqueEnum enfant : enfants) {
				enfantsLibelle.add(enfant.getLibelle());
			}
			
			rubriquesParBloc.put(blocLibelle, enfantsLibelle);
		}
		
		return rubriquesParBloc;
		
		
	}
}