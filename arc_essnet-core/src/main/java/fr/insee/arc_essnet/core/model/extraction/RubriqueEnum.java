package fr.insee.arc_essnet.core.model.extraction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


public enum RubriqueEnum {

	
	
	
	TOUS(null,"Toutes les rubriques"),
	
		ENTREPRISE(TOUS,"Entreprise S21.G00.06"),
		
			V_S21_G00_06_001(ENTREPRISE, "SIREN S21.G00.06.001"),
			V_S21_G00_06_002(ENTREPRISE, "NIC du siège S21.G00.06.002"),
			V_S21_G00_06_003(ENTREPRISE, "Code APEN S21.G00.06.003"),
			V_S21_G00_06_004(ENTREPRISE, "Numéro, extension, nature et libellé de la voie S21.G00.06.004"),
			V_S21_G00_06_005(ENTREPRISE, "Code postal S21.G00.06.005"),
			V_S21_G00_06_006(ENTREPRISE, "Localité S21.G00.06.006"),
			V_S21_G00_06_007(ENTREPRISE, "Complément de la localisation de la construction S21.G00.06.007"),
			V_S21_G00_06_008(ENTREPRISE, "Service de distribution, complément de localisation de la voie S21.G00.06.008"),
			V_S21_G00_06_009(ENTREPRISE, "Effectif moyen de l'entreprise au 31 décembre S21.G00.06.009"),
			V_S21_G00_06_010(ENTREPRISE, "Code pays S21.G00.06.010"),
			V_S21_G00_06_011(ENTREPRISE, "Code de distribution à l'étranger S21.G00.06.011"),
			V_S21_G00_06_012(ENTREPRISE, "Implantation de l'entreprise S21.G00.06.012"),
			V_S21_G00_06_013(ENTREPRISE, "Date de début de la période de référence (CVAE) S21.G00.06.013"),
			V_S21_G00_06_014(ENTREPRISE, "Date de fin de la période de référence (CVAE) S21.G00.06.014"),
			
		ETABLISSEMENT(TOUS,"Etablissement S21.G00.11"),
			
			V_S21_G00_11_001(ETABLISSEMENT, "NIC S21.G00.11.001"),
			V_S21_G00_11_002(ETABLISSEMENT, "Code APET S21.G00.11.002"),
			V_S21_G00_11_003(ETABLISSEMENT, "Numéro, extension, nature et libellé de la voie S21.G00.11.003"),
			V_S21_G00_11_004(ETABLISSEMENT, "Code postal S21.G00.11.004"),
			V_S21_G00_11_005(ETABLISSEMENT, "Localité S21.G00.11.005"),
			V_S21_G00_11_006(ETABLISSEMENT, "Complément de la localisation de la construction S21.G00.11.006"),
			V_S21_G00_11_007(ETABLISSEMENT, "Service de distribution, complément de localisation de la voie S21.G00.11.007"),
			V_S21_G00_11_008(ETABLISSEMENT, "Effectif de fin de période déclarée de l'établissement S21.G00.11.008"),
			V_S21_G00_11_009(ETABLISSEMENT, "Type de rémunération soumise à contributions d'Assurance chômage pour expatriés S21.G00.11.009"),
			V_S21_G00_11_015(ETABLISSEMENT, "Code pays S21.G00.11.015"),
			V_S21_G00_11_016(ETABLISSEMENT, "Code de distribution à l'étranger S21.G00.11.016"),
			V_S21_G00_11_017(ETABLISSEMENT, "Nature juridique de l'employeur S21.G00.11.017"),
			V_S21_G00_11_018(ETABLISSEMENT, "Date de clôture de l'exercice comptable S21.G00.11.018"),
			
		INDIVIDU(TOUS,"Individu S21.G00.30"),
		
			V_S21_G00_30_001(INDIVIDU, "Numéro d'inscription au répertoire S21.G00.30.001"),
			V_S21_G00_30_002(INDIVIDU, "Nom de famille S21.G00.30.002"),
			V_S21_G00_30_003(INDIVIDU, "Nom d'usage S21.G00.30.003"),
			V_S21_G00_30_004(INDIVIDU, "Prénoms S21.G00.30.004"),
			V_S21_G00_30_005(INDIVIDU, "Sexe S21.G00.30.005"),
			V_S21_G00_30_006(INDIVIDU, "Date de naissance S21.G00.30.006"),
			V_S21_G00_30_007(INDIVIDU, "Lieu de naissance S21.G00.30.007"),
			V_S21_G00_30_008(INDIVIDU, "Numéro, extension, nature et libellé de la voie S21.G00.30.008"),
			V_S21_G00_30_009(INDIVIDU, "Code postal S21.G00.30.009"),
			V_S21_G00_30_010(INDIVIDU, "Localité S21.G00.30.010"),
			V_S21_G00_30_011(INDIVIDU, "Code pays S21.G00.30.011"),
			V_S21_G00_30_012(INDIVIDU, "Code de distribution à l'étranger S21.G00.30.012"),
			V_S21_G00_30_013(INDIVIDU, "Codification UE S21.G00.30.013"),
			V_S21_G00_30_014(INDIVIDU, "Code département de naissance S21.G00.30.014"),
			V_S21_G00_30_015(INDIVIDU, "Code pays de naissance S21.G00.30.015"),
			V_S21_G00_30_016(INDIVIDU, "Complément de la localisation de la construction S21.G00.30.016"),
			V_S21_G00_30_017(INDIVIDU, "Service de distribution, complément de localisation de la voie S21.G00.30.017"),
			V_S21_G00_30_018(INDIVIDU, "Adresse mél S21.G00.30.018"),
			V_S21_G00_30_019(INDIVIDU, "Matricule de l'individu dans l'entreprise S21.G00.30.019"),
			V_S21_G00_30_020(INDIVIDU, "Numéro technique temporaire S21.G00.30.020"),
			V_S21_G00_30_021(INDIVIDU, "Nombre d'enfants à charge S21.G00.30.021"),
			V_S21_G00_30_022(INDIVIDU, "Statut à l'étranger au sens fiscal S21.G00.30.022"),
			V_S21_G00_30_023(INDIVIDU, "Embauche S21.G00.30.023"),
			
		CONTRAT(TOUS,"(contrat de travail, convention, mandat) S21.G00.40"),	
			
			V_S21_G00_40_001(CONTRAT, "Date de début du contrat S21.G00.40.001"),
			V_S21_G00_40_002(CONTRAT, "Statut du salarié (conventionnel) S21.G00.40.002"),
			V_S21_G00_40_003(CONTRAT, "Code statut catégoriel Retraite Complémentaire obligatoire S21.G00.40.003"),
			V_S21_G00_40_004(CONTRAT, "Code profession et catégorie socioprofessionnelle (PCS-ESE) S21.G00.40.004"),
			V_S21_G00_40_005(CONTRAT, "Code complément PCS-ESE S21.G00.40.005"),
			V_S21_G00_40_006(CONTRAT, "Libellé de l'emploi S21.G00.40.006"),
			V_S21_G00_40_007(CONTRAT, "Nature du contrat S21.G00.40.007"),
			V_S21_G00_40_008(CONTRAT, "Dispositif de politique publique et conventionnel S21.G00.40.008"),
			V_S21_G00_40_009(CONTRAT, "Numéro du contrat S21.G00.40.009"),
			V_S21_G00_40_010(CONTRAT, "Date de fin prévisionnelle du contrat S21.G00.40.010"),
			V_S21_G00_40_011(CONTRAT, "Unité de mesure de la quotité de travail S21.G00.40.011"),
			V_S21_G00_40_012(CONTRAT, "Quotité de travail de référence de l'entreprise pour la catégorie de salarié S21.G00.40.012"),
			V_S21_G00_40_013(CONTRAT, "Quotité de travail du contrat S21.G00.40.013"),
			V_S21_G00_40_014(CONTRAT, "Modalité d'exercice du temps de travail S21.G00.40.014"),
			V_S21_G00_40_016(CONTRAT, "Complément de base au régime obligatoire S21.G00.40.016"),
			V_S21_G00_40_017(CONTRAT, "Code convention collective applicable S21.G00.40.017"),
			V_S21_G00_40_018(CONTRAT, "Code régime de base risque maladie S21.G00.40.018"),
			V_S21_G00_40_019(CONTRAT, "Identifiant du lieu de travail S21.G00.40.019"),
			V_S21_G00_40_020(CONTRAT, "Code régime de base risque vieillesse S21.G00.40.020"),
			V_S21_G00_40_021(CONTRAT, "Motif de recours S21.G00.40.021"),
			V_S21_G00_40_022(CONTRAT, "Code caisse professionnelle de congés payés S21.G00.40.022"),
			V_S21_G00_40_023(CONTRAT, "Taux de déduction forfaitaire spécifique pour frais professionnels S21.G00.40.023"),
			V_S21_G00_40_024(CONTRAT, "Travailleur à l'étranger au sens du code de la Sécurité Sociale S21.G00.40.024"),
			V_S21_G00_40_025(CONTRAT, "Motif d'exclusion DSN S21.G00.40.025"),
			V_S21_G00_40_026(CONTRAT, "Statut d'emploi du salarié S21.G00.40.026"),
			V_S21_G00_40_027(CONTRAT, "Code affectation Assurance chômage S21.G00.40.027"),
			V_S21_G00_40_028(CONTRAT, "Numéro interne employeur public S21.G00.40.028"),
			V_S21_G00_40_029(CONTRAT, "Type de gestion de l’Assurance chômage S21.G00.40.029"),
			V_S21_G00_40_030(CONTRAT, "Date d'adhésion S21.G00.40.030"),
			V_S21_G00_40_031(CONTRAT, "Date de dénonciation S21.G00.40.031"),
			V_S21_G00_40_032(CONTRAT, "Date d’effet de la convention de gestion S21.G00.40.032"),
			V_S21_G00_40_033(CONTRAT, "Numéro de convention de gestion S21.G00.40.033"),
			V_S21_G00_40_035(CONTRAT, "Code délégataire du risque maladie S21.G00.40.035"),
			V_S21_G00_40_036(CONTRAT, "Code emplois multiples S21.G00.40.036"),
			V_S21_G00_40_037(CONTRAT, "Code employeurs multiples S21.G00.40.037"),
			V_S21_G00_40_038(CONTRAT, "Code métier S21.G00.40.038"),
			V_S21_G00_40_039(CONTRAT, "Code régime de base risque accident du travail S21.G00.40.039"),
			V_S21_G00_40_040(CONTRAT, "Code risque accident du travail S21.G00.40.040"),
			V_S21_G00_40_041(CONTRAT, "Positionnement dans la convention collective S21.G00.40.041"),
			V_S21_G00_40_042(CONTRAT, "Code statut catégoriel APECITA S21.G00.40.042"),
			V_S21_G00_40_043(CONTRAT, "Taux de cotisation accident du travail S21.G00.40.043"),
			V_S21_G00_40_044(CONTRAT, "Salarié à temps partiel cotisant à temps plein S21.G00.40.044"),
			V_S21_G00_40_045(CONTRAT, "Rémunération au pourboire S21.G00.40.045"),
			V_S21_G00_40_046(CONTRAT, "Identifiant de l’établissement utilisateur S21.G00.40.046"),
			V_S21_G00_40_047(CONTRAT, "Numéro de certification sociale S21.G00.40.047"),
			V_S21_G00_40_048(CONTRAT, "Numéro de label « Prestataire de services du spectacle vivant » S21.G00.40.048"),
			V_S21_G00_40_049(CONTRAT, "Numéro de licence entrepreneur spectacle S21.G00.40.049"),
			V_S21_G00_40_050(CONTRAT, "Numéro objet spectacle S21.G00.40.050"),
			V_S21_G00_40_051(CONTRAT, "Statut organisateur spectacle S21.G00.40.051"),
			
		AUTRES(TOUS,"autres"),
		
			V_S20_G00_05_005(AUTRES,"validité S.S20.G00.05.005"),
			V_S21_G00_50_002(AUTRES,"non defini S.S21.G00.50.002"), 
			V_S21_G00_50_004(AUTRES,"non defini S.S21.G00.50.004"), 
			V_S21_G00_51_001(AUTRES,"non defini S.S21.G00.51.001"),
			V_S21_G00_51_002(AUTRES,"non defini S.S21.G00.51.002"), 
			V_S21_G00_51_011(AUTRES,"non defini S.S21.G00.51.011"),
			V_S21_G00_51_013(AUTRES,"non defini S.S21.G00.51.013");
			
	// Voici les champs privés définis par le constructeur
	private RubriqueEnum pere;
	
	private String libelle;
	
	private Set<RubriqueEnum> enfants = new HashSet<RubriqueEnum>();
	
	// Voici le constructeur invoqué pour chaque valeur ci-dessus.
	private RubriqueEnum(RubriqueEnum pere, String libelle) {
		
		this.setPere(pere);
		this.setLibelle(libelle);
		
		// Mise à jour du père
		if (this.pere != null) {
			// ajout de l'item courant en tant qu'enfant 
			this.pere.enfants.add(this);
		}
		
	}

	
	
	/******************************************************************************************************************************************************/
	/******************************************************* GETTERS ET SETTERS ***************************************************************************/
	/******************************************************************************************************************************************************/
	
 
	public RubriqueEnum getPere() {
		return pere;
	}


	public void setPere(RubriqueEnum pere) {
		this.pere = pere;
	}


	public String getLibelle() {
		return libelle;
	}


	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}


	/************************************************************************************************************************/
	/******************************************* METHODES *******************************************************************/
	/************************************************************************************************************************/

	public Set<RubriqueEnum> obtenirTouteSaDescendance(){
		
		Set<RubriqueEnum> descendance = new TreeSet<>();
		
		for (RubriqueEnum rubriqueEnum : enfants) {
				descendance.addAll(enfants);
				descendance.addAll(rubriqueEnum.obtenirTouteSaDescendance());
			
		}
		
		return descendance;
		
	}
	
	
	public Set<RubriqueEnum> obtenirTouteSaDescendanceElleYC(){
		
		Set<RubriqueEnum> descendance = new TreeSet<>();
		descendance.add(this);
		descendance.addAll(obtenirTouteSaDescendance());
		
		return descendance;
	}
	    
	
	public static Map<RubriqueEnum,Set<RubriqueEnum>> obtenirRubriquesParBloc(){
		
		Map<RubriqueEnum,Set<RubriqueEnum>> rubriquesParBloc = new HashMap<RubriqueEnum, Set<RubriqueEnum>>();
		
		for (RubriqueEnum bloc : obtenirLesBlocs()) {
			rubriquesParBloc.put(bloc, bloc.enfants);
		}
		
		return rubriquesParBloc;
		
		
	}
	
	//obtenir rubriques de niveau 1 (TOUS=0)
	private static Set<RubriqueEnum> obtenirLesBlocs(){
		
		Set<RubriqueEnum> blocs = new HashSet<>();
		//les rubriques 'blocs' sont ceux dont le père est 'TOUS'
		
		for (RubriqueEnum rubriqueEnum : TOUS.obtenirTouteSaDescendance()) {
			if (rubriqueEnum.getPere().equals(TOUS)){
				blocs.add(rubriqueEnum);
			}
		}
		
		return blocs;		
	}
	
	
	public static RubriqueEnum obtenirRubriqueEnumAPartirLibelle(String libelle){
		Set<RubriqueEnum> toutesLesRubriques = TOUS.obtenirTouteSaDescendance();

		for (RubriqueEnum rubriqueEnum : toutesLesRubriques) {
			if(rubriqueEnum.libelle.equals(libelle)){
				return rubriqueEnum;
			}
		}
		return null;		
	}
	
	
	@Override
	public String toString(){
		return super.toString().toLowerCase();	
	}
	
	
}
