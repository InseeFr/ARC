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
		// view query
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.append(SQL.SELECT);
		query.append(query.sqlListeOfColumnsFromModel(dataModelExport));
		query.append(SQL.FROM);
		query.append(dataObjectService.getView(dataModelExport));
		// default value
		HashMap<String, String> defaultInputFields = new HashMap<>();
		// initialize vobject
		vObjectService.initialize(viewExport, query, dataObjectService.getView(dataModelExport), defaultInputFields);
	}

	/**
	 * dao call to retrieve exports to make
	 * 
	 * @param viewExport
	 * @return A map of all exports to make with their associated rules
	 * @throws ArcException
	 */
	public HashMap<String, ArrayList<String>> startExportRetrieve(VObject viewExport) throws ArcException {
		ViewEnum dataModelExport = ViewEnum.EXPORT;
		// Récupérer les exports sélectionnés
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.append(SQL.SELECT);
		query.append(query.sqlListeOfColumnsFromModel(dataModelExport));
		query.append(SQL.FROM);
		query.append(dataObjectService.getView(dataModelExport));
		query.append(SQL.WHERE);
		query.append(ColumnEnum.FILE_NAME);
		query.append(" IN ");
		query.append("(" + query.sqlListeOfValues(getSelectedRecords().get("file_name")) + ") ");
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
		// Actualiser l'état de l'export
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.append("UPDATE ");
		query.append(dataObjectService.getView(dataModelExport));
		query.append(" SET ");
		query.append(ColumnEnum.ETAT);
		query.append("=");
		if (isExported) {
			query.append("to_char(current_timestamp,'YYYY-MM-DD HH24:MI:SS') ");
		} else {
			query.append(query.quoteText(TraitementEtat.ENCOURS.toString()));
		}
		query.append(SQL.WHERE);
		query.append(ColumnEnum.FILE_NAME);
		query.append("=");
		query.append(query.quoteText(fileName.get(fileIndex)));
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
		return new GenericBean(UtilitaireDao.get(0).executeRequest(vObjectService.getConnection(),
				new ArcPreparedStatementBuilder(
						"SELECT lower(varbdd) as varbdd, pos::int-1 as pos, max(pos::int) over() as maxp FROM "
								+ howToExportReworked + " order by pos::int ")))
				.mapContent();
	}
	
	public ResultSet exportFileFilteredOrdered(Statement stmt, int n, List<String> tablesToExport, List<String> filterTable, List<String> orderTable, String bacASable) throws SQLException {
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.append(SQL.SELECT);
		query.append("*");
		query.append(SQL.FROM);
		query.append(bacASable + "." + tablesToExport.get(n));
		query.append(SQL.WHERE);
		query.append((StringUtils.isEmpty(filterTable.get(n)) ? "true" : filterTable.get(n)) + " ");
		query.append(StringUtils.isEmpty(orderTable.get(n)) ? "" : "ORDER BY " + orderTable.get(n) + " ");
		
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
