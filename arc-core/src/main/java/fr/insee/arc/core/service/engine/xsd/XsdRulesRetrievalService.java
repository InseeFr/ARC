package fr.insee.arc.core.service.engine.xsd;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import fr.insee.arc.core.model.JeuDeRegle;
import fr.insee.arc.core.service.engine.controle.ControleRegleService;
import fr.insee.arc.core.service.engine.xsd.XsdControlDescription.XsdControlDescriptionBuilder;
import fr.insee.arc.core.service.engine.xsd.controls.AlphaNumForXsd;
import fr.insee.arc.core.service.engine.xsd.controls.ConditionForXsd;
import fr.insee.arc.core.service.engine.xsd.controls.DateForXsd;
import fr.insee.arc.core.service.engine.xsd.controls.DateTimeForXsd;
import fr.insee.arc.core.service.engine.xsd.controls.EnumForXsd;
import fr.insee.arc.core.service.engine.xsd.controls.IntForXsd;
import fr.insee.arc.core.service.engine.xsd.controls.RegexForXsd;
import fr.insee.arc.core.service.engine.xsd.controls.TimeForXsd;
import fr.insee.arc.utils.dao.PreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.utils.ManipString;

public class XsdRulesRetrievalService {
	
	public XsdControlDescription fetchRulesFromBase(Connection connection, JeuDeRegle jdr) throws SQLException, InvalidStateForXsdException {
		return fetchRulesFromBase(connection, jdr, null);
	}

	public XsdControlDescription fetchRulesFromBase(Connection connection, JeuDeRegle jdr, String filter) throws SQLException, InvalidStateForXsdException {
		PreparedStatementBuilder request= new PreparedStatementBuilder(
				"select id_classe, "
				+ "rubrique_pere, rubrique_fils, "
				+ "borne_inf, borne_sup, condition, pre_action, "
				+ "xsd_role, xsd_label_fils, xsd_ordre "
				+ "from arc.ihm_controle_regle ");
		if (filter != null) {
	        request.append(filter);
		} else {
			request.append(" where true ");
		}
		request.append(" and id_norme" + request.sqlEqual(jdr.getIdNorme(), "text"));
        request.append(" and periodicite" + request.sqlEqual(jdr.getPeriodicite(), "text"));
        request.append(" and validite_inf" + request.sqlEqual(jdr.getValiditeInfString(), "date"));
        request.append(" and validite_sup" + request.sqlEqual(jdr.getValiditeSupString(), "date"));
        request.append(" and version" + request.sqlEqual(jdr.getVersion(), "text"));

		ArrayList<ArrayList<String>> executeRequest = UtilitaireDao.get("arc").executeRequest(connection,request);
		return parseSqlResults(executeRequest, connection);
	}

	/** Parses the control rules described in the SQL results
	 *  and returns them as a XsdControlDescription object.*/
	public XsdControlDescription parseSqlResults(ArrayList<ArrayList<String>> results, Connection connection)
			throws SQLException, InvalidStateForXsdException {
		XsdControlDescriptionBuilder builder = new XsdControlDescriptionBuilder();
		Map<String, Integer> columns = new GenericBean(results).mapIndex();
		for (int i = 1; i < results.size() ; i++) {
			ArrayList<String> line = results.get(i);
			String idClasse = line.get(columns.get("id_classe"));
			String unparsedName = line.get(columns.get("rubrique_fils"));
			String rubriquePere = getElementName(line.get(columns.get("rubrique_pere")));
			String rubriqueFils = getElementName(unparsedName);
			String condition = line.get(columns.get("condition"));
			String role = line.get(columns.get("xsd_role"));
			String label = line.get(columns.get("xsd_label_fils"));
			Integer position = ManipString.parseNumber(line.get(columns.get("xsd_ordre")));
			Integer borneInf = ManipString.parseNumber(line.get(columns.get("borne_inf")));
			Integer borneSup = ManipString.parseNumber(line.get(columns.get("borne_sup")));
			switch (idClasse) {
			case "CARDINALITE":
				if (rubriquePere.equals("liasse")) {
					System.out.println("");
				}
				if (position!=null) {
					if (ManipString.compareStringWithNull("xs:choice", role)) {
						String[] complements = parseComplementsFrom(condition);
						builder.addChoiceRelation(rubriquePere, rubriqueFils, borneInf, borneSup, complements, position);
					} else {
						builder.addRelation(rubriquePere, rubriqueFils, borneInf, borneSup, position);
					}
				}
				if (!ManipString.isStringNull(label)) {
					builder.defineAliasFor(rubriqueFils, label);
				}
				break;
			case "ALPHANUM":
				builder.addRuleTo(rubriquePere, new AlphaNumForXsd(borneInf, borneSup));
			break;
			case "CONDITION":
				String preaction = line.get(columns.get("pre_action"));
				builder.addCommentTo(rubriquePere, new ConditionForXsd(condition, preaction));
			break;
			case "DATE":
				if (condition.equalsIgnoreCase(ControleRegleService.XSD_DATE_NAME))
				{
					builder.addRuleTo(rubriquePere, new DateForXsd());
				} else if (condition.equalsIgnoreCase(ControleRegleService.XSD_DATETIME_NAME))
				{
					builder.addRuleTo(rubriquePere, new DateTimeForXsd());
				} else if (condition.equalsIgnoreCase(ControleRegleService.XSD_TIME_NAME))
				{
					builder.addRuleTo(rubriquePere, new TimeForXsd());
				}
			break;
			case "ENUM_BRUTE":
				List<String> enumList = ManipString.splitAndCleanList(condition, ",");
				//Remove enclosing quotes
				enumList = enumList.stream()
						.map(s -> s.replaceFirst("^'", "").replaceFirst("'$", ""))
						.collect(Collectors.toList());
				builder.addRuleTo(rubriquePere, new EnumForXsd(enumList));
			break;
			case "ENUM_TABLE":
				GenericBean enumAsSqlResults = new GenericBean(UtilitaireDao.get("arc").executeRequest(connection, new PreparedStatementBuilder(condition)));
				String columNameInResult = enumAsSqlResults.headers.get(0);
				builder.addRuleTo(rubriquePere, new EnumForXsd(enumAsSqlResults.mapContent().get(columNameInResult)));
			break;
			case "NUM":
				builder.addRuleTo(rubriquePere, new IntForXsd(borneInf, borneSup));
			break;
			case "REGEXP":
				builder.addRuleTo(rubriquePere, new RegexForXsd(condition));
			break;
			default:
				// ignore unknown/unsupported control types
				break;
			}
		}
		return builder.build();
	}

	/** Récupère un tableau des noms d'éléments depuis une chaîne de type {i_e}=-1, {i_f}=-1.*/
	private String[] parseComplementsFrom(String condition) {
		String[] complements = condition.trim().split("=-1");
		for (int i = 0 ; i < complements.length ; i++) {
			complements[i] = complements[i].replaceAll(".*\\{(.*)\\}.*", "$1");
		}
		return complements;
	}

	private String getElementName(String columnName) {
		if (ManipString.isStringNull(columnName)) {
			return null;
		}
		return columnName.replaceFirst("^(i_)|^(v_)", "");
	}

}