package fr.insee.arc.core.service.engine.xsd.controls;

public class DateTimeForXsd implements ControlForXsd {

	@Override
	public boolean defineType() {
		return true;
	}

	@Override
	public String getType() {
		return "xs:dateTime";
	}
	
	@Override
	public String writeControlAsXsd(String indentation) {
		return "";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DateTimeForXsd) {
			ControlForXsd o = (ControlForXsd)obj;
			return this.compareTo(o) == 0;
		}
		return false;
	}
}