package fr.insee.arc.core.service.engine.xsd.controls;

public class ConditionForXsd implements ControlForXsd {
	
	private final String condition;
	private final String preaction;

	public ConditionForXsd(String condition, String preaction) {
		this.condition = condition;
		this.preaction = preaction;
	}

	@Override
	public boolean defineType() {
		return false;
	}

	@Override
	public String writeControlAsXsd(String indentation) {
		StringBuilder sb = new StringBuilder("<!-- preaction=[");
		sb.append(preaction);
		sb.append("], condition=[");
		sb.append(condition);
		sb.append("] -->");
		return  sb.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ConditionForXsd) {
			ControlForXsd o = (ControlForXsd)obj;
			return this.compareTo(o) == 0;
		}
		return false;
	}
}