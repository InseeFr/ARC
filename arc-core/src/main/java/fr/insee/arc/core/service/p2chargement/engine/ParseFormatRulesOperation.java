package fr.insee.arc.core.service.p2chargement.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.insee.arc.core.service.p2chargement.bo.IParseFormatRules;
import fr.insee.arc.core.service.p2chargement.bo.Norme;
import fr.insee.arc.utils.utils.ManipString;

public class ParseFormatRulesOperation<T extends IParseFormatRules> {
	
	public ParseFormatRulesOperation(Norme norme, Class<T> type) {
		super();
		this.norme = norme;
		this.parseResult = new HashMap<>();
		this.type = type;
	}

	private Norme norme;
	private Class<T> type;

	private Map<T, List<String>> parseResult;

	public List<String> getValues(T t) {
		return parseResult.get(t) == null ? new ArrayList<>() : parseResult.get(t);
	}

	public String getValue(T t) {
		return parseResult.get(t) == null ? null : parseResult.get(t).get(0);
	}
	
	public void setValue(T t, String value) {
		parseResult.put(t, Arrays.asList(value));
	}


	public void parseFormatRules() {
		String formatRules = this.norme.getRegleChargement().getFormat();
		if (formatRules == null || formatRules.isBlank()) {
			return;
		}

		formatRules = formatRules.trim();

		String[] lines = formatRules.split("\n");
		for (String line : lines) {
			parse(line);
		}
	}

	public Map<T, List<String>> getParseResult() {
		return parseResult;
	}

	/**
	 * parse the line
	 * @param inputString
	 */
	private void parse(String inputString) {

		
		// chaine vide : on break immédiatement
		if (inputString == null || inputString.isBlank())
		{
			return;
		}
		
		for (T criteria : type.getEnumConstants()) {
			String parsed = parse(criteria, inputString);
			
			if (parsed != null) {
				if (parseResult.get(criteria) == null) {
					parseResult.put(criteria, new ArrayList<>());
				}

				parseResult.get(criteria).add(parsed);
				
				if (criteria.isStop())
				{
					break;
				}
			}
		}
	}

	/**
	 * parse a string to see if it match a criteria return null if no match return
	 * parsed criteria if matched
	 * 
	 * @param criteria
	 * @param inputString
	 * @return
	 */
	private String parse(T criteria, String inputString) {
	
		if (criteria.getAfterTag() == null && criteria.getBeforeTag()==null) {
			return null;
		}

		inputString = inputString.trim();

		if (criteria.getAfterTag() == null) {
			if (inputString.contains(criteria.getBeforeTag())) {
				return ManipString.substringBeforeFirst(inputString, criteria.getBeforeTag());
			} else {
				return null;
			}
		}

		if (inputString.contains(criteria.getAfterTag())) {
			if (criteria.getBeforeTag() != null) {
				return ManipString.substringBeforeFirst(
						ManipString.substringAfterFirst(inputString, criteria.getAfterTag()), criteria.getBeforeTag());
			} else {
				return ManipString.substringAfterFirst(inputString, criteria.getAfterTag());
			}
		}

		return null;
	}

}
