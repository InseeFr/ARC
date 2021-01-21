package fr.insee.arc.core.service.engine.mapping;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.insee.arc.core.model.IDbConstant;
import fr.insee.arc.core.model.JeuDeRegle;
import fr.insee.arc.utils.dao.PreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.structure.GenericBean;

public class ExpressionService implements IDbConstant {


	public static final String EXPR_NAME = "expr_nom";
	public static final String EXPR_VALUE = "expr_valeur";
	
	/** Checks whether the name is a valid expression name.*/
	public boolean validateExpressionName(String expressionValue) {
		return !expressionValue.contains("@");
	}

	public Optional<String> loopInExpressionSet(GenericBean expressions){
		return loopInExpressionSet(
				expressions.mapContent().get(EXPR_NAME), 
				expressions.mapContent().get(EXPR_VALUE));
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
			throws SQLException {
		PreparedStatementBuilder request = new PreparedStatementBuilder();
		request.append("select expr_nom, expr_valeur from ");
		request.append(environnement);
		request.append(".expression where ");
		request.append(ruleSet.getSqlEquals());
		return new GenericBean(
				UtilitaireDao.get(poolName).executeRequest(connexion, request)
				);
		
	}

	/** Fetch the expressions in order so that if expression A includes expression B, then A comes before B.
	 * It is highly recommended to check for loops beforehand.*/
	public GenericBean fetchOrderedExpressions(Connection connexion, String environment,
			JeuDeRegle ruleSet) throws SQLException {
		PreparedStatementBuilder request = new PreparedStatementBuilder();
		request.append("WITH recursive exprs (select expr_nom, expr_valeur from ");
		request.append(environment);
		request.append(".expression WHERE ");
		request.append(ruleSet.getSqlEquals());
		request.append("), \n tree (expr_nom, expr_valeur, level, path)\n");
		request.append("AS (SELECT m.expr_nom, m.expr_valeur, 0, m.expr_nom \n");
		request.append(" FROM exprs m \n");
		request.append("WHERE 1 not in (select 1 from exprs \\n");
		request.append(".expression where expr_valeur like '%{@'||m.expr_nom||'@}%') \n");
		request.append(" UNION ALL \n");
		request.append(" SELECT sub.expr_nom, sub.expr_valeur, t.level + 1, t.path||'>'||sub.EXPR_NOM \n");
		request.append("    FROM exprs sub \n");
		request.append("    INNER JOIN tree t \n");
		request.append("    ON t.expr_valeur like '%@'||sub.expr_nom||'@%' )\n");
		request.append("select expr_nom, expr_valeur, level from tree\n");
		return new GenericBean(
				UtilitaireDao.get(poolName).executeRequest(connexion, request)
				);
	}

	public boolean isExpressionSyntaxPresent(Connection connexion, String environment, JeuDeRegle ruleSet) throws Exception {
		PreparedStatementBuilder request = new PreparedStatementBuilder();
		request.append("select 1 from ");
		request.append(environment);
		request.append(".mapping where ");
		request.append(ruleSet.getSqlEquals());
		request.append(" and expr_regle_col ~ '(?<=\\{@)(.+?)(?=@\\})'");
		return 	UtilitaireDao.get(poolName).hasResults(connexion, request);
	}

}