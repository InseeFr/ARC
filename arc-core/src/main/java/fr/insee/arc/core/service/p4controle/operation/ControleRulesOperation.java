package fr.insee.arc.core.service.p4controle.operation;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.service.global.bo.FileIdCard;
import fr.insee.arc.core.service.global.dao.RulesOperations;
import fr.insee.arc.core.service.p4controle.bo.ControleTypeCode;
import fr.insee.arc.core.service.p4controle.bo.IdCardControle;
import fr.insee.arc.core.service.p4controle.bo.RegleControle;
import fr.insee.arc.utils.exception.ArcException;

public class ControleRulesOperation {

	private ControleRulesOperation() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Méthode pour savoir retrouver quels sont les contrôles relatifs à la norme
	 * fourni dans fileIdCard
	 * 
	 * @param norme
	 * @return l'objet Norme avec les règles de contrôle renseignées
	 * @throws ArcException
	 * @throws ArcException si aucune règle n'est trouvée
	 */
	public static void fillControleRules(Connection connection, String envExecution, FileIdCard fileIdCard)
			throws ArcException {
		Map<String, List<String>> regle = RulesOperations.getBean(connection,
				RulesOperations.getRegles(ViewEnum.CONTROLE_REGLE.getFullName(envExecution), fileIdCard));

		List<RegleControle> listRegles = new ArrayList<>();
		for(int i = 0; i < regle.get(ColumnEnum.ID_CLASSE.getColumnName()).size(); i++) {
			listRegles.add(new RegleControle(
					ControleTypeCode.getEnum(regle.get(ColumnEnum.ID_CLASSE.getColumnName()).get(0)),
					regle.get(ColumnEnum.RUBRIQUE_PERE.getColumnName()).get(0),
					regle.get(ColumnEnum.RUBRIQUE_FILS.getColumnName()).get(0),
					regle.get(ColumnEnum.BORNE_INF.getColumnName()).get(0),
					regle.get(ColumnEnum.BORNE_SUP.getColumnName()).get(0),
					regle.get(ColumnEnum.CONDITION.getColumnName()).get(0),
					regle.get(ColumnEnum.PRE_ACTION.getColumnName()).get(0),
					Integer.parseInt(regle.get(ColumnEnum.ID_REGLE.getColumnName()).get(0)),
					Integer.parseInt(regle.get(ColumnEnum.XSD_ORDRE.getColumnName()).get(0)),
					regle.get(ColumnEnum.XSD_LABEL_FILS.getColumnName()).get(0),
					regle.get(ColumnEnum.XSD_ROLE.getColumnName()).get(0),
					regle.get(ColumnEnum.BLOCKING_THRESHOLD.getColumnName()).get(0),
					regle.get(ColumnEnum.ERROR_ROW_PROCESSING.getColumnName()).get(0)
					));
		}
		fileIdCard.setIdCardControle(
				new IdCardControle(listRegles));
	}
	
}
