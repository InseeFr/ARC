package fr.insee.arc.web.gui.query.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.web.gui.query.dao.QueryDao;

@Service
public class ServiceViewTable extends InteractorQuery {
	
	public String seeTable(Model model) {
		Map<String, List<String>> mapContentSelected = views.getViewTable().mapContentSelected();
		if (!mapContentSelected.isEmpty()) {
			this.myQuery = QueryDao.queryTableSelected(mySchema, mapContentSelected.get(ColumnEnum.TABLENAME.getColumnName()).get(0));
			model.addAttribute("myQuery", myQuery);
		}
		return basicAction(model, RESULT_SUCCESS);
	}

	public String sortTable(Model model) {
		return sortVobject(model, RESULT_SUCCESS, views.getViewTable());
	}

}
