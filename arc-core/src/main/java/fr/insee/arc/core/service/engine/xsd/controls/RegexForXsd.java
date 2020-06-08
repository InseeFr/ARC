package fr.insee.arc.core.service.engine.xsd.controls;

public class RegexForXsd implements ControlForXsd {

	private final String pattern;

	public RegexForXsd(String pattern) {
		this.pattern = convertPattern(pattern);
	}

	private String convertPattern(String patternToConvert) {
		String anythingPattern = "[\\s\\S]*";
		if (!patternToConvert.startsWith("^")) {
			patternToConvert = anythingPattern + patternToConvert;
		}
		if (!patternToConvert.endsWith("$")) {
			patternToConvert = patternToConvert + anythingPattern;
		}
		return patternToConvert;
	}

	@Override
	public boolean defineType() {
		return false;
	}

	@Override
	public String writeControlAsXsd(String indentation) {
		StringBuilder sb = new StringBuilder();
		sb.append(indentation);
		sb.append("<xs:pattern value=\"");
		sb.append(pattern);
		sb.append("\"/>");
		return sb.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RegexForXsd) {
			ControlForXsd o = (ControlForXsd)obj;
			return this.compareTo(o) == 0;
		}
		return false;
	}
}