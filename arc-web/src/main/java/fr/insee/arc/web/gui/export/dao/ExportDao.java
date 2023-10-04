package fr.insee.arc.web.gui.export.dao;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.DataObjectService;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.service.global.bo.ArcDateFormat;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.web.gui.all.util.VObject;
import fr.insee.arc.web.gui.all.util.VObjectHelperDao;
import fr.insee.arc.web.gui.all.util.VObjectService;

@Component
public class ExportDao extends VObjectHelperDao {

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
		query.build(SQL.UPDATE, nameOfViewExport);
		query.build(SQL.SET, ColumnEnum.ETAT, "=", isExported ? "to_char(current_timestamp,'"+ArcDateFormat.TIMESTAMP_FORMAT_VIEW.getDatastoreFormat()+"')" : query.quoteText(TraitementEtat.ENCOURS.toString()));
		query.build(SQL.WHERE, ColumnEnum.FILE_NAME, "=", query.quoteText(fileName.get(fileIndex)));
		UtilitaireDao.get(0).executeRequest(vObjectService.getConnection(), query);
	}

	/**
	 * retrieve rules
	 * @param n
	 * @param howToExport
	 * @param tablesToExport
	 * @param bacASable
	 * @return
	 * @throws ArcException
	 */
	protected HashMap<String, ArrayList<String>> exportFileRetrieveRules(int n, List<String> howToExport,
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
		
		query.build(SQL.SELECT, "*", SQL.FROM, ViewEnum.getFullName(bacASable, tablesToExport.get(n)));
		query.build(SQL.WHERE, (StringUtils.isEmpty(filterTable.get(n)) ? SQL.TRUE : filterTable.get(n)), " ");
		query.build((StringUtils.isEmpty(orderTable.get(n)) ? "" : SQL.ORDER_BY + orderTable.get(n)), " ");
		
		return stmt.executeQuery(query.getQuery().toString());
	}

	
	/**
	 * parse rule and export file
	 * @param h
	 * @param n
	 * @param bw
	 * @param fw
	 * @throws ArcException
	 * @throws IOException
	 * @throws SQLException
	 */
	public void exportFile(HashMap<String, ArrayList<String>> h, int n, BufferedWriter bw, FileOutputStream fw)
			throws ArcException, IOException, SQLException {
		ArrayList<String> tablesToExport = h.get("table_to_export");
		ArrayList<String> headers = h.get("headers");
		ArrayList<String> nulls = h.get("nulls");
		ArrayList<String> filterTable = h.get("filter_table");
		ArrayList<String> orderTable = h.get("order_table");
		ArrayList<String> howToExport = h.get("nomenclature_export");
		ArrayList<String> headersToScan = h.get("columns_array_header");
		ArrayList<String> valuesToScan = h.get("columns_array_value");

		HashMap<String, Integer> pos = new HashMap<>();
		ArrayList<String> headerLine = new ArrayList<>();

		h = exportFileRetrieveRules(n, howToExport, tablesToExport, this.dataObjectService.getSandboxSchema());

		for (int i = 0; i < h.get("varbdd").size(); i++) {
			pos.put(h.get("varbdd").get(i), Integer.parseInt(h.get("pos").get(i)));
			headerLine.add(h.get("varbdd").get(i));
		}

		// write header line if required
		if (!StringUtils.isEmpty(headers.get(n))) {
			for (String o : headerLine) {
				bw.write(o + ";");
			}
			bw.write("\n");
		}

		int maxPos = Integer.parseInt(h.get("maxp").get(0));

		Connection c = UtilitaireDao.get(0).getDriverConnexion();
		c.setAutoCommit(false);

		Statement stmt = c.createStatement();
		stmt.setFetchSize(5000);

		try (ResultSet res = exportFileFilteredOrdered(stmt, n, tablesToExport, filterTable, orderTable, this.dataObjectService.getSandboxSchema())) {
			ResultSetMetaData rsmd = res.getMetaData();

			ArrayList<String> output;
			String[] tabH;
			String[] tabV;
			String colName;
			while (res.next()) {
				// reinitialiser l'arraylist de sortie
				output = new ArrayList<String>();
				for (int k = 0; k < maxPos; k++) {
					output.add("");
				}

				boolean todo = false;
				tabH = null;
				tabV = null;
				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					colName = rsmd.getColumnLabel(i).toLowerCase();

					todo = true;
					// cas ou on est dans un tableau
					if (todo && colName.equals(headersToScan.get(n))) {
						todo = false;
						tabH = (String[]) res.getArray(i).getArray();
					}
					if (todo && colName.equals(valuesToScan.get(n))) {
						todo = false;
						tabV = (String[]) res.getArray(i).getArray();
					}
					if (todo) {
						todo = false;
						if (pos.get(colName) != null) {
							// if nulls value musn't be quoted as "null" and element is null then don't write
							if (!(StringUtils.isEmpty(nulls.get(n)) && StringUtils.isEmpty(res.getString(i)))) {
								output.set(pos.get(colName), res.getString(i));
							}
						}
					}
				}

				// traitement des variables tableaux
				if (tabH != null && tabV != null) {
					for (int k = 0; k < tabH.length; k++) {
						if (pos.get(tabH[k].toLowerCase()) != null) {
							// if nulls value musn't be quoted as "null" and element is null then don't write
							if (!(StringUtils.isEmpty(nulls.get(n)) && StringUtils.isEmpty(tabV[k]))) {
								output.set(pos.get(tabH[k].toLowerCase()), tabV[k]);
							}
						}
					}
				}

				for (String o : output) {
					bw.write(o + ";");
				}
				bw.write("\n");
			}
		}
		c.close();
		bw.flush();
		fw.flush();

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
