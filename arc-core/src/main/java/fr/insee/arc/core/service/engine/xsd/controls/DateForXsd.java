package fr.insee.arc.core.service.engine.xsd.controls;

public class DateForXsd implements ControlForXsd {

	@Override
	public boolean defineType() {
		return true;
	}

	@Override
	public String getType() {
		return "xs:date";
	}
	
	@Override
	public String writeControlAsXsd(String indentation) {
		return "";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DateForXsd) {
			ControlForXsd o = (ControlForXsd)obj;
			return this.compareTo(o) == 0;
		}
		return false;
	}
}