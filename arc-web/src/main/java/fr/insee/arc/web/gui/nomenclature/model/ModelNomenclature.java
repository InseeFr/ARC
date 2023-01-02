package fr.insee.arc.web.gui.nomenclature.model;

import org.springframework.stereotype.Component;

import fr.insee.arc.web.gui.all.model.ArcModel;
import fr.insee.arc.web.util.VObject;

@Component
public class ModelNomenclature implements ArcModel {

	private VObject viewListNomenclatures;
	private VObject viewNomenclature;
	private VObject viewSchemaNmcl;

	public ModelNomenclature() {
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
