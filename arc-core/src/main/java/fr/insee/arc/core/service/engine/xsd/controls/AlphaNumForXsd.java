package fr.insee.arc.core.service.engine.xsd.controls;

public class AlphaNumForXsd implements ControlForXsd {
	
	private final Integer lowerBound;
	private final Integer upperBound;

	public AlphaNumForXsd(Integer lowerBound, Integer upperBound) {
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}
	
	@Override
	public boolean defineType() {
		return true;
	}
	
	@Override
	public String getType() {
		return "xs:string";
	}

	@Override
	public String writeControlAsXsd(String indentation) {
		StringBuilder sb = new StringBuilder();
		if (lowerBound != null && upperBound != null) {
			if (lowerBound.equals(upperBound)) {
				sb.append(indentation + "<xs:length value=\"" + upperBound.toString() + "\"/>");
			} else {
				sb.append(indentation + minLength() + "\n");
				sb.append(indentation + maxLength());
			}
		} else {
			if (lowerBound != null) {
				sb.append(indentation + minLength());
			}
			if (upperBound != null) {
				sb.append(indentation + maxLength());
			}
		}
		return sb.toString();
	}

	private String minLength() {
		return "<xs:minLength value=\"" + lowerBound.toString() + "\"/>";
	}

	private String maxLength() {
		return "<xs:maxLength value=\"" + upperBound.toString() + "\"/>";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AlphaNumForXsd) {
			ControlForXsd o = (ControlForXsd)obj;
			return this.compareTo(o) == 0;
		}
		return false;
	}

}