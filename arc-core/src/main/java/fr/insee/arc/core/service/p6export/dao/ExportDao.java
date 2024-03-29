package fr.insee.arc.core.service.p6export.dao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.core.service.p6export.parquet.ParquetDao;
import fr.insee.arc.core.service.p6export.provider.DirectoryPathExport;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.database.ArcDatabase;
import fr.insee.arc.utils.database.TableToRetrieve;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.utils.structure.GenericBean;

public class ExportDao {

	private Sandbox coordinatorSandbox;

	public ExportDao(Sandbox coordinatorSandbox) {
		this.coordinatorSandbox = coordinatorSandbox;
	}

	/**
	 * Compute a timestamp that identifies the export This timestamp will be used as
	 * directory name where the files will be exported
	 * 
	 * @return
	 * @throws ArcException
	 */
	public String dateExport() throws ArcException {
		ArcPreparedStatementBuilder query;
		query = new ArcPreparedStatementBuilder();
		query.build(SQL.SELECT, "to_char(localtimestamp,'YYYY_MM_DD_HH24_MI_SS_MS')");
		return UtilitaireDao.get(0).getString(this.coordinatorSandbox.getConnection(), query);
	}

	/**
	 * select mapping tables found on the executor by listing table in
	 * IHM_MOD_TABLE_METIER
	 * 
	 * @return
	 * @throws ArcException
	 */
	public Set<String> selectBusinessTableToExport() throws ArcException {
		Set<String> mappingTablesName = new HashSet<>();

		int numberOfNods = ArcDatabase.numberOfNods();

		// if executor nods, get tables to retrieve from them else get from coordinator
		if (ArcDatabase.isScaled()) {
			queryBusinessTablesFromNods(mappingTablesName, ArcDatabase.EXECUTOR.getIndex(), numberOfNods);
		} else {
			queryBusinessTablesFromNods(mappingTablesName, ArcDatabase.COORDINATOR.getIndex(), numberOfNods);
		}

		return mappingTablesName;
	}

	/**
	 * Query table name to export from mod_table_metier add them to set of tables to
	 * export We use hashset as we want unique table name
	 * 
	 * @param mappingTablesName
	 * @param startNodConnectionIndex
	 * @param endNodConnectionIndex
	 * @throws ArcException
	 */
	protected void queryBusinessTablesFromNods(Set<String> mappingTablesName, int startNodConnectionIndex,
			int endNodConnectionIndex) throws ArcException {

		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.SELECT, ColumnEnum.NOM_TABLE_METIER, SQL.FROM,
				ViewEnum.MOD_TABLE_METIER.getFullName(coordinatorSandbox.getSchema()));

		for (int connectionIndex = startNodConnectionIndex; connectionIndex < endNodConnectionIndex; connectionIndex++) {
			mappingTablesName.addAll(new GenericBean(UtilitaireDao.get(connectionIndex).executeRequest(null, query))
					.getColumnValues(ColumnEnum.NOM_TABLE_METIER.getColumnName()));
		}
	}

	public List<TableToRetrieve> fetchBusinessTableToNod(Set<String> mappingTablesName) {
		List<TableToRetrieve> tablesToExport = new ArrayList<>();
		// business mapping table tagged to executor nod if connection is scaled
		mappingTablesName.stream()
				.forEach(t -> tablesToExport.add(
						new TableToRetrieve(
								ArcDatabase.isScaled() ? ArcDatabase.EXECUTOR : ArcDatabase.COORDINATOR, //
								ViewEnum.getFullName(this.coordinatorSandbox.getSchema(), t) //
								)));
		return tablesToExport;
	}

	/**
	 * export the list of business table to parquet in the directory /bas/export/timestamp
	 * @param dateExport
	 * @param tablesToExport
	 * @throws ArcException
	 */
	public void exportTablesToParquet(String dateExport, List<TableToRetrieve> tablesToExport) throws ArcException {
		PropertiesHandler properties = PropertiesHandler.getInstance();

		new ParquetDao().exportToParquet(tablesToExport, DirectoryPathExport
				.directoryExport(properties.getBatchParametersDirectory(), this.coordinatorSandbox.getSchema(), dateExport), null);
	}

}
