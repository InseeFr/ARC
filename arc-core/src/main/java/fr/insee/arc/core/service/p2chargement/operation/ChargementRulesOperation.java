package fr.insee.arc.core.service.p2chargement.operation;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.service.global.bo.FileIdCard;
import fr.insee.arc.core.service.global.dao.RulesOperations;
import fr.insee.arc.core.service.p2chargement.bo.IdCardChargement;
import fr.insee.arc.core.service.p2chargement.factory.TypeChargement;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;

public class ChargementRulesOperation {

	private ChargementRulesOperation() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Méthode pour savoir retrouver quel est le type de chargeur relatif à la norme
	 * fourni dans fileIdCard
	 * 
	 * @param norme
	 * @return l'objet Norme avec la règle de chargement renseignée
	 * @throws ArcException
	 * @throws ArcException si aucune règle n'est trouvée
	 */
	public static void fillChargementRules(Connection connection, String envExecution, FileIdCard fileIdCard)
			throws ArcException {
		Map<String, List<String>> regle = RulesOperations.getBean(connection,
				RulesOperations.getRegles(ViewEnum.CHARGEMENT_REGLE.getFullName(envExecution), fileIdCard));

		if (regle.get(ColumnEnum.TYPE_FICHIER.getColumnName()).isEmpty()) {
			throw new ArcException(ArcExceptionMessage.LOAD_RULES_NOT_FOUND, fileIdCard.getIdNorme());
		}

		if (regle.get(ColumnEnum.TYPE_FICHIER.getColumnName()).size() > 1) {
			throw new ArcException(ArcExceptionMessage.LOAD_RULES_NOT_FOUND, fileIdCard.getIdNorme());
		}

		fileIdCard.setIdCardChargement(
				new IdCardChargement(TypeChargement.getEnum(regle.get(ColumnEnum.TYPE_FICHIER.getColumnName()).get(0)),
						regle.get(ColumnEnum.DELIMITER.getColumnName()).get(0),
						regle.get(ColumnEnum.FORMAT.getColumnName()).get(0)));
	}

}
