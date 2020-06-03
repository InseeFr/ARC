package fr.insee.arc_composite.web.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.interceptor.SessionAware;
import org.springframework.stereotype.Component;

import com.opensymphony.xwork2.ActionSupport;

import fr.insee.arc_composite.core.model.extraction.DemandeExtraction;
import fr.insee.arc_composite.core.model.extraction.Rubrique;
import fr.insee.arc_composite.core.model.extraction.RubriqueEnum;
import fr.insee.arc_composite.core.service.engine.extraction.DateUtils;
import fr.insee.arc_composite.core.service.engine.extraction.ExtractionService;
import fr.insee.arc_composite.web.model.ArcWebModalites;

@Component
@Results({ @Result(name = "success", location = "/jsp/extraction.jsp"), @Result(name = "index", location = "/jsp/index.jsp") })
public class ExtractionAction extends ActionSupport implements SessionAware {

	

	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(ExtractionAction.class);

	/** Service permettant de récupérer les modalités des variables du modèle */
	protected static final ArcWebModalites am = new ArcWebModalites();

	
	//Liste des modalité utiles pour le chargement de la page
	private static Set<String> listValiditeDisponibles =  new TreeSet<>(); 
	
	private static Map<String,Set<String>> rubriquesParBloc = new HashMap<String, Set<String>>();
	
	//champ du formulaire
	private String debutValidite;
	
	private String finValidite;
	
	private Date debutChargement;
	
	private Date finChargement;
	
	private String bacASable;
	
	private String listeRubriquesSelectionnees;
	
	
	
	public ExtractionAction() {
		super();
	}
	
	
	public Set<String> getListValiditeDisponibles() {

		if(listValiditeDisponibles.isEmpty()){
			listValiditeDisponibles = am.obtenirListValidites();
		}
		
		return listValiditeDisponibles;
	}


	public Map<String,Set<String>> getRubriquesParBloc() {
		if(rubriquesParBloc.isEmpty()){
			rubriquesParBloc = am.obtenirRubriquesParBloc();
		}
		return rubriquesParBloc;
	}
	
	
	
    public static ArcWebModalites getAm() {
		return am;
	}

	@Override
    public void setSession(Map<String, Object> session) {

    }

	public String execute() {
		return SUCCESS;
	}

    
	/**
	 * Appel de la page JSP
	 *
	 * @return "success"
	 */

	@Action(value = "/selectExtractionService")
	public String selectExtractionJSP()
	{
		
		return SUCCESS;
	}

	@Action(value = "/traitementDemandeExtraction")
	public String traiterDemandeExtraction(){
		
		logger.info("Création de la demande!!!!");
		
		DemandeExtraction maDemande = fabriquerUneDemandeExtraction();
		
		
		logger.info("Exécuter la demande");
		
		ExtractionService extractionService = new ExtractionService(); 
		    
		extractionService.executerLaDemande(maDemande);
		
		logger.info("Fin du traitement");
		
		return SUCCESS;
	}
	
	/************************************************************************************************************************************/
	/************************************************* METHODES PRIVEES *****************************************************************/
	/************************************************************************************************************************************/

	//Construire une demande d'extraction à partir des
	//champs saisis par l'utilsateur
	private DemandeExtraction fabriquerUneDemandeExtraction() {
		
		DemandeExtraction demandeExtraction = new DemandeExtraction();
		//Initialisation de la log

		//Répertoire de travail selectionné
		demandeExtraction.renseignerRepertoireDeTravail(bacASable);
		demandeExtraction.initLog4JExtractionAppender();
		
		//Début & fin chargement
		demandeExtraction.setDateCreationMinimaleFichiers(debutChargement);
		demandeExtraction.setDateCreationMaximaleFichiers(finChargement);
		
		//Début & fin de validité
		demandeExtraction.setPeriodeValidites(DateUtils.obtenirPeriode(debutValidite, finValidite));
		
		//Les rubriques avec leurs filtres éventuels
		List<Rubrique> rubriquesSelectionnees = recupererRubriquesSelectionneesAvecFiltres();
		demandeExtraction.ajouterRubriques(rubriquesSelectionnees);
		
		return demandeExtraction;
	}

	/**
	 * 
	 * @return la liste des rubriques selectionnées 
	 */
	private List<Rubrique> recupererRubriquesSelectionneesAvecFiltres() {
		
		List<Rubrique> rubriquesSelectionnees = new ArrayList<Rubrique>();
		
		//on enleve les guillemets en debut et fin de la chaine de caratères listeRubriquesSelectionnees 
		listeRubriquesSelectionnees = listeRubriquesSelectionnees.substring(1);
		listeRubriquesSelectionnees = listeRubriquesSelectionnees.substring(0,listeRubriquesSelectionnees.length()-1);
		
		//on met les rubriques avec Filtres dans un tableau
		String rubriquesAvecFiltres[] = listeRubriquesSelectionnees.split("\";\"");
		
		
		for (String valeur : rubriquesAvecFiltres) {
			
			String rubriquePlusFiltres[] = valeur.split(":");
			
			//1er élément correspond toujours à la rubrique
			RubriqueEnum rubriqueEnum = RubriqueEnum.obtenirRubriqueEnumAPartirLibelle(rubriquePlusFiltres[0]);
			Rubrique rubriqueSelectionnee = new Rubrique(rubriqueEnum);

			//on récupère les filtres si il y en a
			if(rubriquePlusFiltres.length>1){
				
				//deuxième élément correspond toujours à la liste des filtres
				String filtres = rubriquePlusFiltres[1];
				
				//filtre sur présence obligatoire d'une valeur? 
				if("valeur obligatoire".equals(filtres)){
					rubriqueSelectionnee.setValeurObligatoire(true);
				}else{
					//filtre sur des valeurs saisie par l'utilisateur
					String tableaufiltres[] = filtres.split(";");
					List<String> listfiltres = Arrays.asList(tableaufiltres);
					rubriqueSelectionnee.setListeValeursRetenues(listfiltres);

				}
			}
			
			rubriquesSelectionnees.add(rubriqueSelectionnee);

			
		}
		
		return rubriquesSelectionnees;
	}
	
	
	
	
	
	
	
	
	
	public String getDebutValidite() {
		return debutValidite;
	}

	public void setDebutValidite(String debutValidite) {
		this.debutValidite = debutValidite;
	}


	public String getFinValidite() {
		return finValidite;
	}

	public void setFinValidite(String finValidite) {
		this.finValidite = finValidite;
	}


	public String getBacASable() {
		return bacASable;
	}


	public void setBacASable(String bacASable) {
		this.bacASable = bacASable;
	}


	public Date getDebutChargement() {
		return debutChargement;
	}


	public void setDebutChargement(Date debutChargement) {
		this.debutChargement = debutChargement;
	}


	public Date getFinChargement() {
		return finChargement;
	}


	public void setFinChargement(Date finChargement) {
		this.finChargement = finChargement;
	}


	public String getListeRubriquesSelectionnees() {
		return listeRubriquesSelectionnees;
	}


	public void setListeRubriquesSelectionnees(
			String listeRubriquesSelectionnees) {
		this.listeRubriquesSelectionnees = listeRubriquesSelectionnees;
	}



} 