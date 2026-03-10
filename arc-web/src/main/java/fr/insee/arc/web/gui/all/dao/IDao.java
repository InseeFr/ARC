package fr.insee.arc.web.gui.all.dao;

import fr.insee.arc.core.dataobjects.DataObjectService;
import fr.insee.arc.web.gui.all.util.VObjectService;

public interface IDao {

	public void initialize(VObjectService vObjectService, DataObjectService dataObjectService);
	
}
