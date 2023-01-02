package fr.insee.arc.web.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import fr.insee.arc.core.dataobjects.ColumnEnum;

public class ArcGenericDao {


	
	/**
	 * Default builder for input fields for vObject replace the default value with
	 * selection value it is used to map vobject between each others
	 * 
	 * @param selection
	 * @param selectedColumns
	 * @return
	 */
	public static HashMap<String, String> buildDefaultInputFieldsFromSelection(Map<String, ArrayList<String>> selection,
			ColumnEnum... selectedColumns) {
		HashMap<String, String> defaultInputFields = new HashMap<>();
		for (ColumnEnum selectedColumn : selectedColumns) {
			defaultInputFields.put(selectedColumn.getColumnName(),
					selection.get(selectedColumn.getColumnName()).get(0));
		}
		return defaultInputFields;
	}

}
