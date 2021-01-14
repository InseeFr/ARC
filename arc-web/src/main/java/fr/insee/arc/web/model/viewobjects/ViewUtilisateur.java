package fr.insee.arc.web.model.viewobjects;

import java.util.HashMap;
import java.util.Map;

import fr.insee.arc.web.util.ConstantVObject;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;
import fr.insee.arc.web.util.VObject;

public class ViewUtilisateur extends VObject {
	
	public ViewUtilisateur() {
		setSessionName("viewUserList");
		setPaginationSize(15);
		setTitle("view.userlist");
		
		Map<String, ColumnRendering> fields = new HashMap<>();
        fields.put("idep", new ColumnRendering(true, "label.user.id", "30%", "text", null, true));
        fields.put("nom_prenom", new ColumnRendering(true, "label.user.name", "70%", "text", null, true));
        fields.put("groupe", new ColumnRendering(false, "label.usergroup", "0%", "text", null, true));
        fields.put("lib_groupe", new ColumnRendering(false, "label.usergroup.description", "0%", "text", null, true));
        setConstantVObject(new ConstantVObject(fields));
	}

}
