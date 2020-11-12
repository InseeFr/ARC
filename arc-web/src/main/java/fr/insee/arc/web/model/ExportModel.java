package fr.insee.arc.web.model;

import fr.insee.arc.web.model.viewobjects.ViewExport;
import fr.insee.arc.web.model.viewobjects.ViewFileExport;
import fr.insee.arc.web.util.VObject;

public class ExportModel implements ArcModel {

    private VObject viewExport;

    private VObject viewFileExport;

    public ExportModel() {
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
