package fr.insee.arc.web.gui.report.dao;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.DataObjectService;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.database.Delimiters;
import fr.insee.arc.web.gui.all.util.VObject;
import fr.insee.arc.web.gui.all.util.VObjectHelperDao;
import fr.insee.arc.web.gui.all.util.VObjectService;

@Component
public class GererReportDao extends VObjectHelperDao {

	/**
	 * dao call to build norm vobject
	 * 
	 * @param viewNorme
	 */
	public void initializeViewReport(VObject viewReport) {

		ViewEnum dataModelReport = ViewEnum.MAPPING_ARC_REPORT_OK;

		Map<String, String> defaultInputFields = new HashMap<>();

		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		
		query.build(SQL.SELECT, query.sqlListeOfColumnsFromModel(dataModelReport));
		query.build(SQL.FROM, dataObjectService.getView(dataModelReport), SQL.AS, ViewEnum.ALIAS_A);
		query.build(SQL.WHERE, SQL.NOT, SQL.EXISTS, "(");
		query.build(SQL.SELECT, SQL.FROM, dataObjectService.getView(ViewEnum.PILOTAGE_FICHIER), SQL.AS, ViewEnum.ALIAS_B);
		query.build(SQL.WHERE, ColumnEnum.PHASE_TRAITEMENT, "=", query.quoteText(TraitementPhase.RECEPTION.toString()));
		query.build(SQL.AND, ViewEnum.ALIAS_B, ".", ColumnEnum.O_CONTAINER, "=", ViewEnum.ALIAS_A, ".", ColumnEnum.ENTREPOT, "||", query.quoteText(Delimiters.SQL_TOKEN_DELIMITER), "||", ViewEnum.ALIAS_A, ".", ColumnEnum.ARCHIVE_FILENAME);
		query.build(")");
		query.build(SQL.ORDER_BY, ColumnEnum.ARCHIVE_TIMESTAMP, SQL.DESC);
		
		// Initialize the vobject
		vObjectService.initialize(viewReport, query, dataObjectService.getView(dataModelReport), defaultInputFields);
	}

	public VObjectService getvObjectService() {
		return vObjectService;
	}

	public void setvObjectService(VObjectService vObjectService) {
		this.vObjectService = vObjectService;
	}

	public DataObjectService getDataObjectService() {
		return dataObjectService;
	}

	public void setDataObjectService(DataObjectService dataObjectService) {
		this.dataObjectService = dataObjectService;
	}

}
