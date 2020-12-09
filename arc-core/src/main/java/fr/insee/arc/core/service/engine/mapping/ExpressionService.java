package fr.insee.arc.core.service.engine.mapping;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.insee.arc.core.model.IDbConstant;
import fr.insee.arc.core.model.JeuDeRegle;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.structure.GenericBean;

public class ExpressionService implements IDbConstant {

	private final String expressionTable;
	private Connection connexion;

	public ExpressionService(Connection connexion, String environnement) {
		this.connexion = connexion;
		this.expressionTable = environnement + ".expression";
	}

	public GenericBean fetchExpressions(JeuDeRegle ruleSet) throws SQLException {
		StringBuilder request = new StringBuilder();
		request.append("select expr_nom, expr_value from ");
		request.append(expressionTable);
		request.append(" where ");
		request.append(ruleSet.getSqlEquals());
		return new GenericBean(
				UtilitaireDao.get(poolName).executeRequest(this.connexion, request)
				);
		
	}

	public String applyTo(String exprCol, GenericBean expressions) {
		Pattern pattern = Pattern.compile("(?<=@)(.+?)@");
		Matcher matcher = pattern.matcher(exprCol);
		HashMap<String, ArrayList<String>> mapContent = expressions.mapContent();
		while (matcher.find()) {
			for (int i = 0; i < expressions.size(); i++) {
				String exprName = mapContent.get("expr_nom").get(i);
				if (matcher.group().equalsIgnoreCase(exprName + "@")) {
					String exprValue = mapContent.get("expr_valeur").get(i);
					exprCol = new StringBuilder(exprCol)
							.replace(matcher.start() - 1, matcher.end(), exprValue)
							.toString();
					// Reset on change
					matcher = pattern.matcher(exprCol);
					break;
				}
			}
		}
		return exprCol;

	}

}