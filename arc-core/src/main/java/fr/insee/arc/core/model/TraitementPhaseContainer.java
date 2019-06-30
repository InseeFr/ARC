package fr.insee.arc.core.model;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import fr.insee.arc.core.dao.ProcessPhaseDAO;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.Pair;

public class TraitementPhaseContainer {

    private static final Logger LOGGER = Logger.getLogger(TraitementPhaseContainer.class);
    
    private List<TraitementPhaseEntity> traitementPhaseEntities;

    public TraitementPhaseContainer(List<TraitementPhaseEntity> traitementPhaseEntities) {
	super();
	this.traitementPhaseEntities = traitementPhaseEntities;
    }

    /**
     * In the database a two lines can represent the same phase for two differents
     * norme ({@link ProcessPhaseDAO}}). It's not user friendly to show to phases in
     * this cases. This methode scan all the phases to find phases with the same
     * name and type to group them.
     * 
     * @return a list with different phase
     */
    public List<TraitementPhaseEntity> getListDifferentPhase() {

	//Transform in a map. Each entry is link to a list with the same name et type
	Map<Pair<String, TypeTraitementPhase>, List<TraitementPhaseEntity>> map = this.traitementPhaseEntities.stream()//
		.collect(Collectors.groupingBy(
			phase -> new Pair<String, TypeTraitementPhase>(phase.getNomPhase(), phase.getTypePhase())));

	//only take the first on each list.
	List<TraitementPhaseEntity> returned = map.values().stream().map(l -> l.get(0)).collect(Collectors.toList());
	
	LoggerHelper.error(LOGGER, String.format("There is %s phase with same name/type", returned.size()));
	
	//Sorting by order
	return map.values().stream()//
			   .map(l -> (l.stream().sorted(Comparator.comparing(TraitementPhaseEntity::getOrdre)).collect(Collectors.toList())).get(0))//
			   .sorted(Comparator.comparing(TraitementPhaseEntity::getOrdre))//
			   .collect(Collectors.toList());
    }
    

    public List<TraitementPhaseEntity> getTraitementPhaseEntities() {
	return traitementPhaseEntities;
    }

    public void setTraitementPhaseEntities(List<TraitementPhaseEntity> traitementPhaseEntities) {
	this.traitementPhaseEntities = traitementPhaseEntities;
    }

}
