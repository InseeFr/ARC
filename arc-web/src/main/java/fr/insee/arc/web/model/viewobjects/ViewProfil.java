package fr.insee.arc.web.model.viewobjects;

import java.util.HashMap;
import java.util.Map;

import fr.insee.arc.web.util.ConstantVObject;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;
import fr.insee.arc.web.util.VObject;

public class ViewProfil extends VObject {

	public ViewProfil() {
		setSessionName("viewUserProfiles");
		setDefaultPaginationSize(15);
		setTitle("view.userprofiles");
		Map<String, ColumnRendering> fields = new HashMap<>();
        fields.put("groupe", new ColumnRendering(true, "label.usergroup", "50%", "text", null, true));
        fields.put("lib_groupe", new ColumnRendering(true, "label.usergroup.description", "50%", "text", null, true));
        setConstantVObject(new ConstantVObject(fields));
	}
	
}
