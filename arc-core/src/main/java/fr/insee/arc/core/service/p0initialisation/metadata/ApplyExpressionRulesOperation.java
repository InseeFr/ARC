package fr.insee.arc.core.service.p0initialisation.metadata;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.service.global.bo.JeuDeRegle;
import fr.insee.arc.core.service.p0initialisation.metadata.dao.ApplyExpressionRulesDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.structure.GenericBean;

public class ApplyExpressionRulesOperation {
	
	public ApplyExpressionRulesOperation() {
		this.dao = new ApplyExpressionRulesDao();
	}

	private ApplyExpressionRulesDao dao;
	
	public Optional<String> loopInExpressionSet(GenericBean expressions){
		return loopInExpressionSet(
				expressions.mapContent().get(ColumnEnum.EXPR_NOM.getColumnName()), 
				expressions.mapContent().get(ColumnEnum.EXPR_VALEUR.getColumnName()));
	}
	
	/** Checks whether the name is a valid expression name.
	 * @param the list of expression names
	 * @param the list of expression values, matching the other list order
	 *   @return an empty Optional if there is no loop, 
	 *   or the loop description if there is one*/
	public Optional<String> loopInExpressionSet(List<String> names, List<String> values) {
		String loop = null;
		for (int i = 0; i < names.size(); i++) {
			String name = names.get(i);
			StringBuilder potentialLoop = new StringBuilder();
			potentialLoop.append("@" + name + "@->");
			if (loopInExpression(potentialLoop, values.get(i), names, values)) {
				return Optional.of(potentialLoop.toString());
			}
		}
		return Optional.ofNullable(loop);
	}
	
	private boolean loopInExpression(StringBuilder potentialLoop, String value, List<String> names, List<String> values) {
		for (int i = 0; i < names.size(); i++) {
			String name = names.get(i);
			if (value.contains("@" + name.toLowerCase() + "@")) {
				String arobasedName = "@" + name + "@";
				if(potentialLoop.toString().contains(arobasedName)) {
					potentialLoop.append(arobasedName);
					return true;
				}
				potentialLoop.append(arobasedName + "->");
				if (loopInExpression(potentialLoop, values.get(i), names, values)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public GenericBean fetchExpressions(Connection connexion, String environnement, JeuDeRegle ruleSet) 
			throws ArcException {
		return dao.execQueryFetchExpressions(connexion, environnement, ruleSet);
	}
	
	public GenericBean fetchOrderedExpressions(Connection connexion, String environment,
			JeuDeRegle ruleSet) throws ArcException {
		return dao.execQueryFetchOrderedExpressions(connexion, environment, ruleSet);
	}
	


	public boolean isExpressionSyntaxPresentInControl(Connection connexion, String environment,
			JeuDeRegle ruleSet) throws ArcException {
		return isExpressionSyntaxPresent(connexion, ViewEnum.CONTROLE_REGLE.getFullName(environment), "condition", ruleSet)
				|| isExpressionSyntaxPresent(connexion, ViewEnum.CONTROLE_REGLE.getFullName(environment), "pre_action", ruleSet);
	}


	public boolean isExpressionSyntaxPresentInMapping(Connection connexion, String environment, JeuDeRegle ruleSet) throws ArcException {
		return isExpressionSyntaxPresent(connexion, ViewEnum.MAPPING_REGLE.getFullName(environment), "expr_regle_col", ruleSet);
	}
	


	private boolean isExpressionSyntaxPresent(Connection connexion, String table, String field, JeuDeRegle ruleSet) throws ArcException {
		return dao.execQueryIsExpressionSyntaxPresent(connexion, table, field, ruleSet);
	}

	/** Returns a request applying the given expressions to the control rules of the given ruleset.*/
	public ArcPreparedStatementBuilder applyExpressionsToControl(JeuDeRegle ruleSet, GenericBean expressions, String environment) {
		return (ArcPreparedStatementBuilder) applyExpressionsTo(ruleSet, expressions, ViewEnum.CONTROLE_REGLE.getFullName(environment), "condition")
				.append(applyExpressionsTo(ruleSet, expressions, ViewEnum.CONTROLE_REGLE.getFullName(environment), "pre_action"));
	}


	/** Returns a request applying the given expressions to the mapping rules of the given ruleset.*/
	public ArcPreparedStatementBuilder applyExpressionsToMapping(JeuDeRegle ruleSet, GenericBean expressions, String environment) {
		return applyExpressionsTo(ruleSet, expressions, ViewEnum.MAPPING_REGLE.getFullName(environment), "expr_regle_col");
	}


	private ArcPreparedStatementBuilder applyExpressionsTo(JeuDeRegle ruleSet, GenericBean expressions, String table, String field) {
		return dao.applyExpressionsTo(ruleSet, expressions, table, field);
	}

}