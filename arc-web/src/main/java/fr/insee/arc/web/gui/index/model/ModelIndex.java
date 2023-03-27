package fr.insee.arc.web.gui.index.model;

import org.springframework.stereotype.Component;

import fr.insee.arc.web.gui.all.model.ArcModel;
import fr.insee.arc.web.util.VObject;

@Component
public class ModelIndex implements ArcModel {
	
	private VObject viewIndex;
	
	public ModelIndex() {
		this.viewIndex = new ViewIndex();
	}

	public VObject getViewIndex() {
		return viewIndex;
	}

	public void setViewIndex(VObject viewIndex) {
		this.viewIndex = viewIndex;
	}

}
