package fr.insee.arc.core.service.p6export.dao;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.model.TraitementPhase.ConditionExecution;
import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.core.service.p6export.parquet.ParquetDao;
import fr.insee.arc.core.service.p6export.parquet.ParquetEncryptionKey;
import fr.insee.arc.core.service.p6export.parquet.ParquetEncryptionKey.EncryptionType;
import fr.insee.arc.core.service.p6export.provider.DirectoryPathExport;
import fr.insee.arc.core.service.s3.ArcS3;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.database.ArcDatabase;
import fr.insee.arc.utils.database.TableToRetrieve;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.utils.structure.GenericBean;

public class ExportDao {

	private Sandbox coordinatorSandbox;
	private String exportTimeStamp;

	private static final String EXPORT_CLIENT_NAME = "EXPORT";

	private String directoryOut;
	private String s3Out;

	public ExportDao(Sandbox coordinatorSandbox) {
		this.coordinatorSandbox = coordinatorSandbox;
	}

	/**
	 * Compute a timestamp that identifies the export This timestamp will be used to
	 * mark data as retrieved (column date_client in pilotage_fichier table) It is
	 * saved in exportTimeStamp variable. This timestamp will also be converted as a
	 * directory name where the files will be exported
	 * 
	 * @return
	 * @throws ArcException
	 */
	public String dateExport() throws ArcException {
		ArcPreparedStatementBuilder query;
		query = new ArcPreparedStatementBuilder();
		query.build(SQL.SELECT,
				"localtimestamp as ts, to_char(localtimestamp,'YYYY_MM_DD_HH24_MI_SS_MS') as ts_as_directory");
		Map<String, List<String>> gb = new GenericBean(
				UtilitaireDao.get(0).executeRequest(this.coordinatorSandbox.getConnection(), query)).mapContent();
		this.exportTimeStamp = gb.get("ts").get(0);
		return gb.get("ts_as_directory").get(0);
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
						new TableToRetrieve(ArcDatabase.isScaled() ? ArcDatabase.EXECUTOR : ArcDatabase.COORDINATOR, //
								ViewEnum.getFullName(this.coordinatorSandbox.getSchema(), t) //
						)));
		return tablesToExport;
	}

	/**
	 * export the list of business table to parquet in the directory
	 * /bas/export/timestamp
	 * 
	 * @param dateExport
	 * @param tablesToExport
	 * @throws ArcException
	 */
	public void exportTablesToParquet(String dateExport, List<TableToRetrieve> tablesToExport) throws ArcException {
		PropertiesHandler properties = PropertiesHandler.getInstance();
		this.directoryOut = DirectoryPathExport.directoryExport(properties.getBatchParametersDirectory(),
				this.coordinatorSandbox.getSchema(), dateExport);
		this.s3Out = DirectoryPathExport.s3Export(this.coordinatorSandbox.getSchema(), dateExport);

		ParquetEncryptionKey parquetEncryptionKey = properties.getS3OutputParquetKey().isEmpty() ? null
				: new ParquetEncryptionKey(EncryptionType.KEY256, properties.getS3OutputParquetKey());

		new ParquetDao().exportToParquet(tablesToExport, directoryOut, parquetEncryptionKey);
	}

	/**
	 * mark exported data
	 * 
	 * @throws ArcException
	 */
	public void markExportedData() throws ArcException {

		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.UPDATE, ViewEnum.PILOTAGE_FICHIER.getFullName(this.coordinatorSandbox.getSchema()));
		query.build(SQL.SET, ColumnEnum.DATE_CLIENT, "=",
				"array_append( date_client, " + query.quoteText(exportTimeStamp) + "::timestamp )");
		query.build(",", ColumnEnum.CLIENT, "=",
				"array_append( client, " + query.quoteText(EXPORT_CLIENT_NAME) + "::text)");
		query.build(SQL.WHERE, ConditionExecution.PIPELINE_TERMINE_DONNEES_NON_EXPORTEES.getSqlFilter());

		UtilitaireDao.get(0).executeRequest(this.coordinatorSandbox.getConnection(), query);
	}

	public void copyToS3Out() throws ArcException {
		ArcS3.OUTPUT_BUCKET.createDirectory(this.s3Out);

		for (File f : new File(this.directoryOut).listFiles()) {
			ArcS3.OUTPUT_BUCKET.upload(f, this.s3Out + File.separator + f.getName());
		}

	}

}
