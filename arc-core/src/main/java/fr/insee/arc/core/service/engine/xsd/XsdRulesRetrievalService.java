package fr.insee.arc.core.service.engine.xsd;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
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
import fr.insee.arc.core.service.engine.xsd.groups.XsdChoice;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.dataobjects.TypeEnum;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.utils.ManipString;

public class XsdRulesRetrievalService {
	
	public XsdControlDescription fetchRulesFromBase(Connection connection, JeuDeRegle jdr) throws ArcException {
		return fetchRulesFromBase(connection, jdr, null);
	}

	private XsdControlDescription fetchRulesFromBase(Connection connection, JeuDeRegle jdr, String filter) throws ArcException {
		ArcPreparedStatementBuilder request= new ArcPreparedStatementBuilder(
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
		request.append(" and id_norme" + request.sqlEqual(jdr.getIdNorme(), TypeEnum.TEXT.getTypeName()));
        request.append(" and periodicite" + request.sqlEqual(jdr.getPeriodicite(), TypeEnum.TEXT.getTypeName()));
        request.append(" and validite_inf" + request.sqlEqual(jdr.getValiditeInfString(), TypeEnum.DATE.getTypeName()));
        request.append(" and validite_sup" + request.sqlEqual(jdr.getValiditeSupString(), TypeEnum.DATE.getTypeName()));
        request.append(" and version" + request.sqlEqual(jdr.getVersion(), TypeEnum.TEXT.getTypeName()));

		ArrayList<ArrayList<String>> executeRequest = UtilitaireDao.get(0).executeRequest(connection,request);
		return parseSqlResults(executeRequest, connection);
	}

	/** Parses the control rules described in the SQL results
	 *  and returns them as a XsdControlDescription object.*/
	private XsdControlDescription parseSqlResults(ArrayList<ArrayList<String>> results, Connection connection)
			throws ArcException {
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
			Integer position = ManipString.parseInteger(line.get(columns.get("xsd_ordre")));
			Integer borneInf = ManipString.parseInteger(line.get(columns.get("borne_inf")));
			Integer borneSup = ManipString.parseInteger(line.get(columns.get("borne_sup")));
			switch (idClasse) {
			case "CARDINALITE":
				if (position!=null) {
					if (ManipString.compareStringWithNull(XsdChoice.XSD_CHOICE_IDENTIFIER, role)) {
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
				GenericBean enumAsSqlResults = new GenericBean(UtilitaireDao.get(0).executeRequest(connection, new ArcPreparedStatementBuilder(condition)));
				String columNameInResult = enumAsSqlResults.getHeaders().get(0);
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