package fr.insee.arc.web.gui.gerernorme.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.web.dao.ArcGenericDao;
import fr.insee.arc.web.util.VObject;
import fr.insee.arc.web.util.VObjectService;

public class GererNormeDao extends ArcGenericDao {

	/**
	 * dao call to build norm vobject
	 * 
	 * @param viewObject
	 * @param viewNorme
	 * @param theTableName
	 */
	public static void initializeViewNorme(VObjectService viewObject, VObject viewNorme, String dataViewName) {

		HashMap<String, String> defaultInputFields = new HashMap<>();

		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.append(SQL.SELECT);
		query.append(query.sqlListeOfColumnsFromModel(ViewEnum.IHM_NORME));
		query.append(SQL.FROM);
		query.append(dataViewName);
		query.append(SQL.ORDER_BY);
		query.append(ColumnEnum.ID_NORME);

		viewObject.initialize(viewNorme, query, dataViewName, defaultInputFields);
	}

	/**
	 * dao call to build calendar vobject
	 * 
	 * @param viewObject
	 * @param viewCalendar
	 * @param theTableName
	 * @param selection
	 */
	public static void initializeViewCalendar(VObjectService viewObject, VObject viewCalendar, String theTableName,
			Map<String, ArrayList<String>> selection, Map<String, String> type) {

		// requete de la vue
		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
		requete.append("select id_norme, periodicite, validite_inf, validite_sup, etat from arc.ihm_calendrier");
		requete.append(" where id_norme" + requete.sqlEqual(selection.get("id_norme").get(0), type.get("id_norme")));
		requete.append(
				" and periodicite" + requete.sqlEqual(selection.get("periodicite").get(0), type.get("periodicite")));

		// check constraints on calendar
		viewCalendar.setAfterInsertQuery(new ArcPreparedStatementBuilder("select arc.fn_check_calendrier(); "));
		viewCalendar.setAfterUpdateQuery(new ArcPreparedStatementBuilder("select arc.fn_check_calendrier(); "));

		// Create the vobject
		viewObject.initialize(viewCalendar, requete, theTableName, buildDefaultInputFieldsFromSelection(selection, ColumnEnum.ID_NORME, ColumnEnum.PERIODICITE));

	}

}
