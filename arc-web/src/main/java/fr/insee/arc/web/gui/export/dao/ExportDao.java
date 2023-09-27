package fr.insee.arc.web.gui.export.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.DataObjectService;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.web.gui.all.util.VObject;
import fr.insee.arc.web.gui.all.util.VObjectHelperDao;
import fr.insee.arc.web.gui.all.util.VObjectService;

public class ExportDao extends VObjectHelperDao {

	private VObjectService vObjectService;
	private DataObjectService dataObjectService;

	public ExportDao(VObjectService vObjectService, DataObjectService dataObjectService) {
		super();
		this.vObjectService = vObjectService;
		this.dataObjectService = dataObjectService;
	}

	/**
	 * dao call to build export vobject
	 * 
	 * @param viewExport
	 */
	public void initializeViewExport(VObject viewExport) {
		ViewEnum dataModelExport = ViewEnum.EXPORT;
		String nameOfViewExport = dataObjectService.getView(dataModelExport);
		// view query
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		StringBuilder columns = query.sqlListeOfColumnsFromModel(dataModelExport);
		query.build(SQL.SELECT, columns, SQL.FROM, nameOfViewExport);
		// default value
		HashMap<String, String> defaultInputFields = new HashMap<>();
		// initialize vobject
		vObjectService.initialize(viewExport, query, nameOfViewExport, defaultInputFields);
	}

	/**
	 * dao call to retrieve exports to make
	 * 
	 * @param viewExport
	 * @return A map of all exports to make with their associated rules
	 * @throws ArcException
	 */
	public HashMap<String, ArrayList<String>> startExportRetrieve() throws ArcException {
		ViewEnum dataModelExport = ViewEnum.EXPORT;
		String nameOfViewExport = dataObjectService.getView(dataModelExport);
		// Récupérer les exports sélectionnés
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		StringBuilder columns = query.sqlListeOfColumnsFromModel(dataModelExport);
		StringBuilder selectedRecords = query.sqlListeOfValues(getSelectedRecords().get("file_name"));
		query.build(SQL.SELECT, columns, SQL.FROM, nameOfViewExport,
				SQL.WHERE, ColumnEnum.FILE_NAME, SQL.IN, "(", selectedRecords, ") ");
		return new GenericBean(UtilitaireDao.get(0).executeRequest(vObjectService.getConnection(), query)).mapContent();
	}

	/**
	 * dao call to update the export state
	 * 
	 * @param fileName
	 * @param fileIndex
	 * @param isExported {@code false} to set state to ENCOURS (export launched),
	 *                   {@code true} to set state to timestamp (export finished)
	 * @throws ArcException
	 */
	public void startExportUpdateState(List<String> fileName, int fileIndex, boolean isExported) throws ArcException {
		ViewEnum dataModelExport = ViewEnum.EXPORT;
		String nameOfViewExport = dataObjectService.getView(dataModelExport);
		// Actualiser l'état de l'export
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		String etatSet;
		if (isExported) {
			etatSet = "to_char(current_timestamp,'YYYY-MM-DD HH24:MI:SS') ";
		} else {
			etatSet = query.quoteText(TraitementEtat.ENCOURS.toString());
		}
		query.build(SQL.UPDATE, nameOfViewExport, SQL.SET, ColumnEnum.ETAT, "=", etatSet,
				SQL.WHERE, ColumnEnum.FILE_NAME, "=", query.quoteText(fileName.get(fileIndex)));
		UtilitaireDao.get(0).executeRequest(vObjectService.getConnection(), query);
	}

	public HashMap<String, ArrayList<String>> exportFileRetrieve(int n, List<String> howToExport,
			List<String> tablesToExport, String bacASable) throws ArcException {
		// if columns,orders table is specified, get the information from database metadata
		String howToExportReworked;
		if (howToExport.get(n) == null) {
			howToExportReworked = "(select column_name as varbdd, ordinal_position as pos from information_schema.columns where table_schema||'.'||table_name = '"
					+ bacASable.toLowerCase() + "." + tablesToExport.get(n) + "') ww ";
		} else {
			howToExportReworked = "arc." + howToExport.get(n);
		}

		// lire la table how to export pour voir comment on va s'y prendre
		// L'objectif est de créer une hashmap de correspondance entre la variable et la position
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.SELECT, "lower(", ColumnEnum.VARBDD, ")", SQL.AS, ColumnEnum.VARBDD, SQL.COMMA,
				ColumnEnum.POS, SQL.CAST_OPERATOR, "int-1", SQL.AS, ColumnEnum.POS, SQL.COMMA,
				"max(", ColumnEnum.POS, SQL.CAST_OPERATOR, "int) over()", SQL.AS, ColumnEnum.MAXP, SQL.FROM,
				howToExportReworked, SQL.ORDER_BY, ColumnEnum.POS, SQL.CAST_OPERATOR, "int");
		return new GenericBean(UtilitaireDao.get(0).executeRequest(vObjectService.getConnection(), query)).mapContent();
	}
	
	public ResultSet exportFileFilteredOrdered(Statement stmt, int n, List<String> tablesToExport, List<String> filterTable, List<String> orderTable, String bacASable) throws SQLException {
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.SELECT, "*", SQL.FROM, bacASable, SQL.DOT, tablesToExport.get(n),
				SQL.WHERE, (StringUtils.isEmpty(filterTable.get(n)) ? SQL.TRUE : filterTable.get(n)), " ",
				(StringUtils.isEmpty(orderTable.get(n)) ? "" : SQL.ORDER_BY + orderTable.get(n)), " ");
		return stmt.executeQuery(query.getQuery().toString());
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
