package fr.insee.arc.core.service.engine.xsd.controls;

public class IntForXsd implements ControlForXsd {
	
	private final Integer lowerBound;
	private final Integer upperBound;

	public IntForXsd(Integer lowerBound, Integer upperBound) {
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}

	@Override
	public boolean defineType() {
		return true;
	}
	
	@Override
	public String getType() {
		return "xs:integer";
	}

	@Override
	public String writeControlAsXsd(String indentation) {
		StringBuilder sb = new StringBuilder();
		if (lowerBound != null && upperBound != null) {
			if (lowerBound.equals(upperBound)) {
				sb.append(indentation + "<xs:totalDigits value=\"" + upperBound.toString() + "\"/>");
			} else {
				sb.append(indentation + pattern("\\d{" + lowerBound + "," + upperBound + "}"));
			}
		} else if (lowerBound != null) {
				sb.append(indentation + pattern("\\d{" + lowerBound + ",}"));
		} else if (upperBound != null) {
				sb.append(indentation + pattern("\\d{0," + upperBound + "}"));
		}
		return sb.toString();
	}

	private String pattern(String pattern) {
		return "<xs:pattern value=\"" + pattern + "\"/>";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IntForXsd) {
			ControlForXsd o = (ControlForXsd)obj;
			return this.compareTo(o) == 0;
		}
		return false;
	}
}