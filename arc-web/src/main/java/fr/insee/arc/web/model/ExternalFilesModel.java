package fr.insee.arc.web.model;

import fr.insee.arc.web.model.viewobjects.ViewListNomenclatures;
import fr.insee.arc.web.model.viewobjects.ViewNomenclature;
import fr.insee.arc.web.model.viewobjects.ViewSchemaNmcl;
import fr.insee.arc.web.util.VObject;

public class ExternalFilesModel implements ArcModel {

	private VObject viewListNomenclatures;
	private VObject viewNomenclature;
	private VObject viewSchemaNmcl;

	public ExternalFilesModel() {
		this.viewListNomenclatures = new ViewListNomenclatures();
		this.viewNomenclature = new ViewNomenclature();
		this.viewSchemaNmcl = new ViewSchemaNmcl();
	}

	public VObject getViewListNomenclatures() {
		return viewListNomenclatures;
	}
	public void setViewListNomenclatures(VObject viewListNomenclatures) {
		this.viewListNomenclatures = viewListNomenclatures;
	}
	public VObject getViewNomenclature() {
		return viewNomenclature;
	}
	public void setViewNomenclature(VObject viewNomenclature) {
		this.viewNomenclature = viewNomenclature;
	}
	public VObject getViewSchemaNmcl() {
		return viewSchemaNmcl;
	}
	public void setViewSchemaNmcl(VObject viewSchemaNmcl) {
		this.viewSchemaNmcl = viewSchemaNmcl;
	}


}
