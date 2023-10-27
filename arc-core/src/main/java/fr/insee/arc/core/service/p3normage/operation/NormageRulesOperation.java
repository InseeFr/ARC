package fr.insee.arc.core.service.p3normage.operation;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.service.global.bo.FileIdCard;
import fr.insee.arc.core.service.global.dao.RulesOperations;
import fr.insee.arc.core.service.p3normage.bo.IdCardNormage;
import fr.insee.arc.core.service.p3normage.bo.RegleNormage;
import fr.insee.arc.core.service.p3normage.bo.TypeNormage;
import fr.insee.arc.utils.exception.ArcException;

public class NormageRulesOperation {

	private NormageRulesOperation() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Méthode pour savoir retrouver quels sont les normages relatifs à la norme
	 * fourni dans fileIdCard
	 * 
	 * @param norme
	 * @return l'objet Norme avec les règles de normage renseignées
	 * @throws ArcException
	 * @throws ArcException si aucune règle n'est trouvée
	 */
	public static void fillNormageRules(Connection connection, String envExecution, FileIdCard fileIdCard)
			throws ArcException {
		
		Map<String, List<String>> regle = RulesOperations.getBean(connection,
				RulesOperations.getRegles(ViewEnum.NORMAGE_REGLE.getFullName(envExecution), fileIdCard));

		List<RegleNormage> listRegles = new ArrayList<>();
		for(int i = 0; i < regle.get(ColumnEnum.ID_CLASSE.getColumnName()).size(); i++) {
			listRegles.add(new RegleNormage(
					TypeNormage.getEnum(regle.get(ColumnEnum.ID_CLASSE.getColumnName()).get(0)),
					regle.get(ColumnEnum.RUBRIQUE.getColumnName()).get(0),
					regle.get(ColumnEnum.RUBRIQUE_NMCL.getColumnName()).get(0)
					));
		}
	
		
		fileIdCard.setIdCardNormage(
				new IdCardNormage(listRegles));
	}
	
}
