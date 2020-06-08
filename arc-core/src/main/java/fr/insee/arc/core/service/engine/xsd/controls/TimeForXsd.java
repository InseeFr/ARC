package fr.insee.arc.core.service.engine.xsd.controls;

public class TimeForXsd implements ControlForXsd {

	@Override
	public boolean defineType() {
		return true;
	}

	@Override
	public String getType() {
		return "xs:time";
	}
	
	@Override
	public String writeControlAsXsd(String indentation) {
		return "";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TimeForXsd) {
			ControlForXsd o = (ControlForXsd)obj;
			return this.compareTo(o) == 0;
		}
		return false;
	}
}