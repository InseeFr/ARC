package fr.insee.arc.core.service.p5mapping.operation;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.service.global.bo.FileIdCard;
import fr.insee.arc.core.service.global.dao.RulesOperations;
import fr.insee.arc.core.service.p5mapping.bo.IdCardMapping;
import fr.insee.arc.core.service.p5mapping.bo.RegleMapping;
import fr.insee.arc.utils.exception.ArcException;

public class MappingRulesOperation {
	
	private MappingRulesOperation() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Méthode pour savoir retrouver quels sont les mappings relatifs à la norme
	 * fourni dans fileIdCard
	 * 
	 * @param norme
	 * @return l'objet Norme avec les règles de mapping renseignées
	 * @throws ArcException
	 * @throws ArcException si aucune règle n'est trouvée
	 */
	public static void fillMappingRules(Connection connection, String envExecution, FileIdCard fileIdCard)
			throws ArcException {
		Map<String, List<String>> regle = RulesOperations.getBean(connection,
				RulesOperations.getRegles(ViewEnum.MAPPING_REGLE.getFullName(envExecution), fileIdCard));

		List<RegleMapping> listRegles = new ArrayList<>();
		for(int i = 0; i < regle.get(ColumnEnum.VARIABLE_SORTIE.getColumnName()).size(); i++) {
			listRegles.add(new RegleMapping(
					regle.get(ColumnEnum.VARIABLE_SORTIE.getColumnName()).get(i),
					regle.get(ColumnEnum.EXPR_REGLE_COL.getColumnName()).get(i)
					));
		}
		fileIdCard.setIdCardMapping(
				new IdCardMapping(listRegles));
	}

}
