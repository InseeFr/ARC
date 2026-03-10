package fr.insee.arc.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fr.insee.arc.core.service.p4controle.bo.ControleTypeCode;

public enum TraitementPhase {
    
	DUMMY(-1, 1, ConditionExecution.AUCUN_PREREQUIS) //
    
    , INITIALISATION(0, 1000000, ConditionExecution.AUCUN_PREREQUIS) //
    , RECEPTION(1, 1000000, ConditionExecution.AUCUN_PREREQUIS) //
    
    , CHARGEMENT(2, 1000000, ConditionExecution.PHASE_PRECEDENTE_TERMINE_PIPELINE_NON_TERMINE) //
    , NORMAGE(3, 1000000, ConditionExecution.PHASE_PRECEDENTE_TERMINE_PIPELINE_NON_TERMINE) //
    , CONTROLE(4, 1000000, ConditionExecution.PHASE_PRECEDENTE_TERMINE_PIPELINE_NON_TERMINE) //
    , MAPPING(5, 1000000, ConditionExecution.PHASE_PRECEDENTE_TERMINE_PIPELINE_NON_TERMINE) //
    
    , EXPORT(6,1, ConditionExecution.PIPELINE_TERMINE_DONNEES_NON_EXPORTEES);


    private int ordre;
    private int nbLigneATraiter;
	private ConditionExecution conditionExecution;
	
	private TraitementPhase(int anOrdre, int aNbLigneATraiter, ConditionExecution conditionExecution) {
        this.ordre = anOrdre;
        this.nbLigneATraiter = aNbLigneATraiter;
        this.conditionExecution = conditionExecution;
    }

    
    public enum ConditionExecution {
    	
    	AUCUN_PREREQUIS("null"),
    	PHASE_PRECEDENTE_TERMINE_PIPELINE_NON_TERMINE("etape=1"),
    	PIPELINE_TERMINE_DONNEES_NON_EXPORTEES("etape=2 AND client is null AND etat_traitement='{OK}'"),
    	PIPELINE_TERMINE_DONNEES_KO("etape=2 AND etat_traitement='{KO}'"),
    	;
    	
    	private String sqlFilter;
    	
    	private ConditionExecution(String sqlFilter)
    	{
    		this.sqlFilter = sqlFilter;
    	}

		public String getSqlFilter() {
			return sqlFilter;
		}

    }

    public int getOrdre() {
        return this.ordre;
    }

    public int getNbLigneATraiter() {
        return this.nbLigneATraiter;
    }  

    
	private final static Map<Integer, TraitementPhase> traitementPhaseMapByOrdre = Stream.of(TraitementPhase.values())
			.collect(Collectors.toMap(TraitementPhase::getOrdre, t -> t)); 

    
    /**
     * Renvoie la phase en fonction de l'ordre
     * 
     * @param ordre
     *            , numéro d'ordre dans l'énumération (premier=1)
     * @return
     */
    public static TraitementPhase getPhase(int ordre) {
    	
    	return traitementPhaseMapByOrdre.get(ordre);
    }
    
    
	
	public static TraitementPhase getPhase(String s)
	{
		try {
			int p=Integer.parseInt(s);
			return getPhase(p);
		}
		catch(Exception e)
		{
			return TraitementPhase.valueOf(s);
		}
	}
	

    /**
     * Renvoie une liste des phases qui sont postérieures à une phase donnée
     * 
     * @return
     */
    public List<TraitementPhase> nextPhases() {
    	return Stream.of(TraitementPhase.values()).filter(phase -> { return (phase.getOrdre() > this.ordre); } ).toList();
    }

    /**
     * Renvoie la phase précédente ou null
     * 
     * @return
     */
    public TraitementPhase previousPhase() {
    	return getPhase(this.getOrdre() - 1);
    }
    
    public TraitementPhase nextPhase() {
    	return getPhase(this.getOrdre() + 1);
    }
    
	public static List<TraitementPhase> listPhasesBetween(TraitementPhase phase, TraitementPhase phaseEnd) {
		List<TraitementPhase> listePhaseC = new ArrayList<>();
		for (TraitementPhase t : values()) {
			if (t.getOrdre()>=phase.getOrdre() && t.getOrdre()<=phaseEnd.getOrdre()) {
				listePhaseC.add(t);
			}
		}
		return listePhaseC;
	}
	
	public static List<TraitementPhase> getListPhaseExecutableInBas() {
		return listPhasesBetween(TraitementPhase.INITIALISATION, TraitementPhase.MAPPING);
	}

	public static List<TraitementPhase> getListPhaseReportedInBas() {
		return listPhasesBetween(TraitementPhase.RECEPTION, TraitementPhase.MAPPING);
	}

	public static List<TraitementPhase> getListPhaseBatchToLoopOver() {
		return listPhasesBetween(TraitementPhase.CHARGEMENT, TraitementPhase.CHARGEMENT);
	}
	
	public String tableRegleOfPhaseInSandbox() {
		return this.toString().toLowerCase()+"_regle";
	}

	public ConditionExecution getConditionExecution() {
		return conditionExecution;
	}
	
	
}
