package fr.insee.arc.web.webusecases.export.models;

import fr.insee.arc.web.model.ArcModel;
import fr.insee.arc.web.util.VObject;

public class ModelExport implements ArcModel {

    private VObject viewExport;

    private VObject viewFileExport;

    public ModelExport() {
		this.viewExport = new ViewExport();
		this.viewFileExport = new ViewFileExport();
	}

	public VObject getViewExport() {
		return viewExport;
	}

	public void setViewExport(VObject viewExport) {
		this.viewExport = viewExport;
	}

	public VObject getViewFileExport() {
		return viewFileExport;
	}

	public void setViewFileExport(VObject viewFileExport) {
		this.viewFileExport = viewFileExport;
	}
    
    
	
}
