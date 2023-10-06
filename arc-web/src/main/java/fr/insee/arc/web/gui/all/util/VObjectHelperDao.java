package fr.insee.arc.web.gui.all.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.DataObjectService;
import fr.insee.arc.web.gui.all.dao.IDao;

@Component
public class VObjectHelperDao implements IDao  {
	
	protected VObjectService vObjectService;
	protected DataObjectService dataObjectService;

	@Override
	public void initialize(VObjectService vObjectService, DataObjectService dataObjectService) {
		this.vObjectService = vObjectService;
		this.dataObjectService = dataObjectService;
	}
	

	protected Map<String, List<String>> selectedRecords;

	/**
	 * build the default value taken from selection when adding a record according to what the user clicked on parent view
	 * 
	 * @param selection
	 * @param selectedColumns
	 * @return
	 */
	public Map<String, String> buildDefaultInputFieldsWithFirstSelectedRecord(ColumnEnum... selectedColumns) {
		Map<String, String> defaultInputFields = new HashMap<>();
		for (ColumnEnum selectedColumn : selectedColumns) {
			defaultInputFields.put(selectedColumn.getColumnName(),
					selectedRecords.get(selectedColumn.getColumnName()).get(0));
		}
		return defaultInputFields;
	}

	/**
	 * Build the query to match a column with the corresponding value of the column in the selectedRecord
	 * This method is very common in ARC as when the the user clicks on a parent view, a new correlated child view is often shown
	 * corresponding to the records of what user clicked on the parent view
	 * @param columnToMatchWithSelectionValue
	 * @return
	 */
	public ArcPreparedStatementBuilder sqlEqualWithFirstSelectedRecord(ColumnEnum columnToMatchWithSelectionValue) {
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.append(columnToMatchWithSelectionValue.getColumnName());
		query.append(query.sqlEqual(selectedRecords.get(columnToMatchWithSelectionValue.getColumnName()).get(0),
				columnToMatchWithSelectionValue.getColumnType().getTypeName()));
		return query;
	}

	public Map<String, List<String>> getSelectedRecords() {
		return selectedRecords;
	}

	public void setSelectedRecords(Map<String, List<String>> selectedRecords) {
		this.selectedRecords = selectedRecords;
	}

}
