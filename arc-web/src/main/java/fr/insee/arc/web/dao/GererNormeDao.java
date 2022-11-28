package fr.insee.arc.web.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import fr.insee.arc.core.databaseobjects.ColumnEnum;
import fr.insee.arc.utils.dao.PreparedStatementBuilder;
import fr.insee.arc.web.util.VObject;
import fr.insee.arc.web.util.VObjectService;

public class GererNormeDao {

	/**
	 * dao call to build norm vobject
	 * @param viewObject
	 * @param viewNorme
	 * @param theTableName
	 */
	public static void initializeViewNorme(VObjectService viewObject, VObject viewNorme, String theTableName) {
		
		HashMap<String, String> defaultInputFields = new HashMap<>();
		
		viewObject.initialize(
				viewNorme,
				new PreparedStatementBuilder("SELECT "+ColumnEnum.ID_FAMILLE.getColumnName()+", id_norme, periodicite, def_norme, def_validite, etat FROM arc.ihm_norme order by id_norme"), theTableName, defaultInputFields);
	}

	/**
	 * dao call to build calendar vobject
	 * @param viewObject
	 * @param viewCalendar
	 * @param theTableName
	 * @param selection
	 */
	public static void initializeViewCalendar(VObjectService viewObject, VObject viewCalendar, String theTableName,
			Map<String, ArrayList<String>> selection, HashMap<String, String> type) {

		// construction des valeurs par défaut pour les ajouts
		HashMap<String, String> defaultInputFields = new HashMap<>();
		
		defaultInputFields.put("id_norme", selection.get("id_norme").get(0));
		defaultInputFields.put("periodicite", selection.get("periodicite").get(0));
	
		// requete de la vue
		PreparedStatementBuilder requete = new PreparedStatementBuilder();
		requete.append("select id_norme, periodicite, validite_inf, validite_sup, etat from arc.ihm_calendrier");
		requete.append(
				" where id_norme" + requete.sqlEqual(selection.get("id_norme").get(0), type.get("id_norme")));
		requete.append(" and periodicite"
				+ requete.sqlEqual(selection.get("periodicite").get(0), type.get("periodicite")));

		// check constraints on calendar
		viewCalendar.setAfterInsertQuery(new PreparedStatementBuilder("select arc.fn_check_calendrier(); "));
		viewCalendar.setAfterUpdateQuery(new PreparedStatementBuilder("select arc.fn_check_calendrier(); "));

		// Create the vobject
		viewObject.initialize(viewCalendar, requete, theTableName, defaultInputFields);
			
	}


	
	
	
	
}
