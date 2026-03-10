package fr.insee.arc.web.gui.entrepot.dao;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.web.gui.all.util.VObject;
import fr.insee.arc.web.gui.all.util.VObjectHelperDao;

@Component
public class EntrepotDao extends VObjectHelperDao {
	
	/**
	 * dao call to build entrepot object
	 * 
	 * @param viewEntrepot
	 */
	public void initializeViewEntrepot(VObject viewEntrepot) {
		ViewEnum dataModelEntrepot = ViewEnum.IHM_ENTREPOT;
		String nameOfViewEntrepot = dataObjectService.getView(dataModelEntrepot);
		// view query
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		StringBuilder columns = query.sqlListeOfColumnsFromModel(dataModelEntrepot);
		query.build(SQL.SELECT, "row_number() over (order by ordre_priorite,id_entrepot,id_loader)", SQL.AS, ColumnEnum.I, SQL.COMMA, columns);
		query.build(SQL.FROM, nameOfViewEntrepot);
		// default value
		Map<String, String> defaultInputFields = new HashMap<>();
		// initialize vobject
		vObjectService.initialize(viewEntrepot, query, nameOfViewEntrepot, defaultInputFields);
	}

}
