package fr.insee.arc.web.gui.entrepot.model;

import org.springframework.stereotype.Component;

import fr.insee.arc.web.gui.all.model.ArcModel;
import fr.insee.arc.web.gui.all.util.VObject;

@Component
public class ModelEntrepot implements ArcModel {
	
	private VObject viewEntrepot;


    public ModelEntrepot() {
		this.viewEntrepot = new ViewEntrepot();
	}

	public VObject getViewEntrepot() {
		return viewEntrepot;
	}

	public void setViewEntrepot(VObject viewEntrepot) {
		this.viewEntrepot = viewEntrepot;
	}

}
