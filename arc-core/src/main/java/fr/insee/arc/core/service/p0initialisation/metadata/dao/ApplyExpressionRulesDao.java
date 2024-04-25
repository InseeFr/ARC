package fr.insee.arc.core.service.p0initialisation.metadata.dao;

import java.sql.Connection;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.service.global.bo.JeuDeRegle;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.security.SqlInjectionChecked;
import fr.insee.arc.utils.structure.GenericBean;

public class ApplyExpressionRulesDao {
	

	/** Fetch the expressions in order so that if expression A includes expression B, then A comes before B.
	 * It is highly recommended to check for loops beforehand.*/
	public GenericBean execQueryFetchOrderedExpressions(Connection connexion, String environment,
			JeuDeRegle ruleSet) throws ArcException {
		ArcPreparedStatementBuilder request = new ArcPreparedStatementBuilder();
		request.append("WITH recursive exprs AS (select expr_nom, expr_valeur from ");
		request.append(environment);
		request.append(".expression WHERE ");
		request.append(ruleSet.getSqlEquals());
		request.append("), \n tree (expr_nom, expr_valeur, level, path)\n");
		request.append("AS (SELECT m.expr_nom, m.expr_valeur, 0, m.expr_nom \n");
		request.append(" FROM exprs m \n");
		request.append("WHERE 1 not in (select 1 from exprs \n");
		request.append(" where expr_valeur like '%{@'||m.expr_nom||'@}%') \n");
		request.append(" UNION ALL \n");
		request.append(" SELECT sub.expr_nom, sub.expr_valeur, t.level + 1, t.path||'>'||sub.EXPR_NOM \n");
		request.append("    FROM exprs sub \n");
		request.append("    INNER JOIN tree t \n");
		request.append("    ON t.expr_valeur like '%{@'||sub.expr_nom||'@}%' )\n");
		request.append("select expr_nom, expr_valeur, level from tree\n");
		return new GenericBean(
				UtilitaireDao.get(0).executeRequest(connexion, request)
				);
	}
	
	
	/**
	 * return the expressions rules
	 * @param connexion
	 * @param environnement
	 * @param ruleSet
	 * @return
	 * @throws ArcException
	 */
	public GenericBean execQueryFetchExpressions(Connection connexion, String environnement, JeuDeRegle ruleSet) 
			throws ArcException {
		ArcPreparedStatementBuilder request = new ArcPreparedStatementBuilder();
		request.append("select expr_nom, expr_valeur from ");
		request.append(environnement);
		request.append(".expression where ");
		request.append(ruleSet.getSqlEquals());
		return new GenericBean(
				UtilitaireDao.get(0).executeRequest(connexion, request)
				);
	}
	
	/**
	 * Test if an expression rule is find in a rule table
	 * @param connexion
	 * @param table
	 * @param field
	 * @param ruleSet
	 * @return
	 * @throws ArcException
	 */
	public boolean execQueryIsExpressionSyntaxPresent(Connection connexion, String table, String field, JeuDeRegle ruleSet) throws ArcException {
		ArcPreparedStatementBuilder request = new ArcPreparedStatementBuilder();
		request.append("select 1 from ");
		request.append(table);
		request.append(" where ");
		request.append(ruleSet.getSqlEquals());
		request.append(" and " + field + " ~ '(?<=\\{@)(.+?)(?=@\\})'");
		return 	UtilitaireDao.get(0).hasResults(connexion, request);
	}

	
	/**
	 * Build the query to replace all expressions found in a field
	 * @param ruleSet : the selection on rules where to expressions will be replaced
	 * @param expressions : the expression list
	 * @param table : the target rule table
	 * @param field : the rule where replacement will be made
	 * @return
	 */
	@SqlInjectionChecked(requiredAsSafe = {"table", "field"})
	public ArcPreparedStatementBuilder applyExpressionsTo(JeuDeRegle ruleSet, GenericBean expressions, String table, String field) {
		ArcPreparedStatementBuilder request = new ArcPreparedStatementBuilder();
		for (int i = 0; i < expressions.size(); i++) {
			request.append("\n UPDATE "+ table + " ");
			request.append("\n set " + field + "=replace(" + field + ", ");
			request.append(request.quoteText("{@"+expressions.mapContent().get(ColumnEnum.EXPR_NOM.getColumnName()).get(i)+"@}"));
			request.append(",");
			request.append(request.quoteText(expressions.mapContent().get(ColumnEnum.EXPR_VALEUR.getColumnName()).get(i)));
			request.append(") ");
			request.append("\n WHERE " + field + " like '%{@%@}%' ");
			request.append("\n AND ");
			request.append(ruleSet.getSqlEquals());
			request.append(";");
		}
		return request;
	}
	
}
