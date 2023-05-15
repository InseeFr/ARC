package fr.insee.arc.core.service.engine.xsd.controls;

import java.util.List;

import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;

public class EnumForXsd implements ControlForXsd {

	private final List<String> enumeration;

	public EnumForXsd(List<String> enumeration) throws ArcException {
		if (enumeration == null || enumeration.isEmpty()) {
			throw new ArcException(ArcExceptionMessage.CONTROLE_XSD_ENUM_EMPTY);
		}
		this.enumeration = enumeration;
	}

	@Override
	public boolean defineType() {
		return false;
	}

	@Override
	public String writeControlAsXsd(String indentation) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < enumeration.size(); i++) {
			String element = enumeration.get(i);
			sb.append(indentation + "<xs:enumeration value=\"" + element + "\"/>");
			if (i < enumeration.size() -1) {
				sb.append("\n");
			}
		}
		return sb.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof EnumForXsd) {
			ControlForXsd o = (ControlForXsd)obj;
			return this.compareTo(o) == 0;
		}
		return false;
	}
}