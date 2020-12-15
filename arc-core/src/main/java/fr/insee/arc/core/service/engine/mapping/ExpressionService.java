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


	/** Checks whether the name is a valid expression name.*/
	public boolean validateExpressionName(String expressionValue) {
		return !expressionValue.contains("@");
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

	/** Apply a set of substitutable expressions to a rule.
	 * @param evaluatedString the String where the expressions will be apply
	 * @param expressions a set of expressions (two fields : "expr_nom", expr_valeur")*/
	public String applyTo(String evaluatedString, GenericBean expressions) {
		Pattern pattern = Pattern.compile("(?<=@)(.+?)@");
		Matcher matcher = pattern.matcher(evaluatedString);
		HashMap<String, ArrayList<String>> mapContent = expressions.mapContent();
		while (matcher.find()) {
			String group = matcher.group();
			String exprName = group.substring(0, group.length() - 1);
			int i = mapContent.get("expr_nom").indexOf(exprName);
			if (i != -1) {
				String exprValue = mapContent.get("expr_valeur").get(i);
				evaluatedString = new StringBuilder(evaluatedString)
							.replace(matcher.start() - 1, matcher.end(), exprValue)
							.toString();
				// Reset on change
				matcher = pattern.matcher(evaluatedString);
			}
		}
		return evaluatedString;

	}

}