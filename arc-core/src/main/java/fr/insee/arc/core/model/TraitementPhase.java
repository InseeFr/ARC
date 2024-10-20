package fr.insee.arc.core.model;

import java.util.ArrayList;
import java.util.List;

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

    /**
     * Renvoie la phase en fonction de l'ordre
     * 
     * @param ordre
     *            , numéro d'ordre dans l'énumération (premier=1)
     * @return
     */
    public static TraitementPhase getPhase(int ordre) {
        for (TraitementPhase phase : TraitementPhase.values()) {
            if (phase.getOrdre() == ordre) {
                return phase;
            }
        }
        return null;
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
        List<TraitementPhase> listPhase = new ArrayList<>();
        for (TraitementPhase phase : TraitementPhase.values()) {
            if (phase.getOrdre() > ordre) {
                listPhase.add(phase);
            }
        }
        return listPhase;
    }

    /**
     * Renvoie la phase précédente ou null
     * 
     * @return
     */
    public TraitementPhase previousPhase() {
        TraitementPhase phase = null;
        int i = this.getOrdre();
        phase = getPhase(i - 1);
        return phase;
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
		return listPhasesBetween(TraitementPhase.CHARGEMENT, TraitementPhase.MAPPING);
	}
	
	public String tableRegleOfPhaseInSandbox() {
		return this.toString().toLowerCase()+"_regle";
	}

	public ConditionExecution getConditionExecution() {
		return conditionExecution;
	}
	
	
}
