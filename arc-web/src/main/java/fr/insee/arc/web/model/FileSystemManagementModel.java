package fr.insee.arc.web.model;

import fr.insee.arc.web.model.viewobjects.ViewDirIn;
import fr.insee.arc.web.model.viewobjects.ViewDirOut;
import fr.insee.arc.web.util.VObject;

public class FileSystemManagementModel implements ArcModel {

	private String dirIn;
    private String dirOut;
    
    private VObject viewDirIn;

    private VObject viewDirOut;
    
    public FileSystemManagementModel() {
		this.viewDirIn = new ViewDirIn();
		this.viewDirOut = new ViewDirOut();
	}

	public String getDirIn() {
		return dirIn;
	}

	public void setDirIn(String dirIn) {
		this.dirIn = dirIn;
	}

	public String getDirOut() {
		return dirOut;
	}

	public void setDirOut(String dirOut) {
		this.dirOut = dirOut;
	}

	public VObject getViewDirIn() {
		return viewDirIn;
	}

	public void setViewDirIn(VObject viewDirIn) {
		this.viewDirIn = viewDirIn;
	}

	public VObject getViewDirOut() {
		return viewDirOut;
	}

	public void setViewDirOut(VObject viewDirOut) {
		this.viewDirOut = viewDirOut;
	}

}
