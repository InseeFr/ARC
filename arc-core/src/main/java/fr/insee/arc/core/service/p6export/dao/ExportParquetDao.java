package fr.insee.arc.core.service.p6export.dao;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.model.ExportOption;
import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.core.service.p6export.bo.TableToExport;
import fr.insee.arc.core.service.p6export.parquet.ParquetDao;
import fr.insee.arc.core.service.p6export.parquet.ParquetEncryptionKey;
import fr.insee.arc.core.service.p6export.parquet.ParquetEncryptionKey.EncryptionType;
import fr.insee.arc.core.service.p6export.provider.DirectoryPathExport;
import fr.insee.arc.core.service.s3.ArcS3;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.database.ArcDatabase;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.files.FileUtilsArc;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.utils.LoggerHelper;

public class ExportParquetDao {

	private Sandbox coordinatorSandbox;
	
	private static final Logger LOGGER = LogManager.getLogger(ExportParquetDao.class);

	private String directoryOut;
	private String directoryOutTemp;

	private String s3Out;
	private String s3OutTemp;

	public ExportParquetDao(Sandbox coordinatorSandbox) {
		this.coordinatorSandbox = coordinatorSandbox;
	}
	/**
	 * select mapping tables found on the executor by listing table in
	 * IHM_MOD_TABLE_METIER
	 * 
	 * @return
	 * @throws ArcException
	 */

	/**
	 * Query table name to export from mod_table_metier add them to set of tables to
	 * export
	 * 
	 * @param mappingTablesName
	 * @param startNodConnectionIndex
	 * @param endNodConnectionIndex
	 * @return
	 * @throws ArcException
	 */
	public GenericBean selectBusinessTableToExport() throws ArcException {

		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();

		query.build(SQL.WITH, ViewEnum.T1, SQL.AS, "(");
		query.build(SQL.BR, SQL.SELECT, ColumnEnum.NOM_TABLE_METIER.alias(SQL.ALIAS_A), SQL.FROM);
		query.build(SQL.BR, ViewEnum.MOD_TABLE_METIER.getFullName(coordinatorSandbox.getSchema()), SQL.ALIAS_A);
		query.build(SQL.BR, SQL.LEFT_JOIN, ViewEnum.EXPORT_OPTION.getFullName(coordinatorSandbox.getSchema()),
				SQL.ALIAS_B);
		query.build(SQL.BR, SQL.ON, ColumnEnum.NOM_TABLE_METIER.alias(SQL.ALIAS_A), "=",
				ColumnEnum.NOM_TABLE_METIER.alias(SQL.ALIAS_B));
		query.build(SQL.BR, SQL.WHERE, "COALESCE(", ColumnEnum.EXPORT_PARQUET_OPTION.alias(SQL.ALIAS_B), ",",
				query.quoteText(ExportOption.ACTIVE.getStatus()), ")", "=",
				query.quoteText(ExportOption.ACTIVE.getStatus()));
		query.build(SQL.BR, ")");
		query.build(SQL.BR, SQL.SELECT, ColumnEnum.NOM_TABLE_METIER);
		query.build(SQL.BR, ",", ColumnEnum.TIMESTAMP_DIRECTORY);
		query.build(SQL.BR, ",", ColumnEnum.FILE_NAME);
		query.build(SQL.BR, ",", ColumnEnum.ZIP);
		query.build(SQL.BR, ",", ColumnEnum.NOMENCLATURE_EXPORT);
		query.build(SQL.BR, ",", ColumnEnum.NULLS);
		query.build(SQL.BR, ",", ColumnEnum.HEADERS);
		query.build(SQL.BR, ",", ColumnEnum.FILTER_TABLE);
		query.build(SQL.BR, ",", ColumnEnum.ORDER_TABLE);
		query.build(SQL.BR, ",", ColumnEnum.JSON_KEY_VALUE);
		query.build(SQL.BR, ",", ColumnEnum.COLUMNS_ARRAY_HEADER);
		query.build(SQL.BR, ",", ColumnEnum.COLUMNS_ARRAY_VALUE);
		query.build(SQL.BR, SQL.FROM, ViewEnum.T1, SQL.ALIAS_A);
		query.build(SQL.BR, SQL.LEFT_JOIN, ViewEnum.EXPORT.getFullName(coordinatorSandbox.getSchema()), SQL.ALIAS_B);
		query.build(SQL.BR, SQL.ON, ColumnEnum.NOM_TABLE_METIER.alias(SQL.ALIAS_A), "=",
				ColumnEnum.TABLE_TO_EXPORT.alias(SQL.ALIAS_B));

		GenericBean gb = new GenericBean(
				UtilitaireDao.get(ArcDatabase.COORDINATOR.getIndex()).executeRequest(null, query));

		return gb;
	}

	public List<TableToExport> fetchBusinessTableToNod() throws ArcException {
		
		GenericBean mappingTablesNameToExport = selectBusinessTableToExport();
		
		List<TableToExport> tablesToExport = new ArrayList<>();
		Map<String, List<String>> m = mappingTablesNameToExport.mapContent();

		IntStream.range(0, mappingTablesNameToExport.getContent().size()).boxed().forEach(rowIndex -> {

			// the business mapping tables are tagged to executor nod if connection is scaled
			TableToExport tableToExport = new TableToExport(
					ArcDatabase.isScaled() ? ArcDatabase.EXECUTOR : ArcDatabase.COORDINATOR,
					ViewEnum.getFullName(this.coordinatorSandbox.getSchema(),
							m.get(ColumnEnum.NOM_TABLE_METIER.getColumnName()).get(rowIndex)))
					.setTimestampDirectory(m.get(ColumnEnum.TIMESTAMP_DIRECTORY.getColumnName()).get(rowIndex)) //
					.setFileName(m.get(ColumnEnum.FILE_NAME.getColumnName()).get(rowIndex)) //
					.setFileFormat(m.get(ColumnEnum.ZIP.getColumnName()).get(rowIndex)) //
					.setNullAsNull(m.get(ColumnEnum.NULLS.getColumnName()).get(rowIndex)) //
					.setHeaders(m.get(ColumnEnum.HEADERS.getColumnName()).get(rowIndex)) //
					.setTableSqlFilter(m.get(ColumnEnum.FILTER_TABLE.getColumnName()).get(rowIndex))
					.setTableSqlOrder(m.get(ColumnEnum.ORDER_TABLE.getColumnName()).get(rowIndex))
					.setTableSqlJsonToColumns(m.get(ColumnEnum.JSON_KEY_VALUE.getColumnName()).get(rowIndex));

			tablesToExport.add(tableToExport);
		});

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
	public void exportTablesToParquet(String dateExport, List<TableToExport> tablesToExport) throws ArcException {

		PropertiesHandler properties = PropertiesHandler.getInstance();
		
		this.directoryOut = DirectoryPathExport.directoryExport(properties.getBatchParametersDirectory(),
				this.coordinatorSandbox.getSchema(), dateExport);

		this.directoryOutTemp = DirectoryPathExport.directoryExportTemp(properties.getBatchParametersDirectory(),
				this.coordinatorSandbox.getSchema(), dateExport);

		this.s3Out = DirectoryPathExport.s3Export(this.coordinatorSandbox.getSchema(), dateExport);

		this.s3OutTemp = DirectoryPathExport.s3ExportTemp(this.coordinatorSandbox.getSchema(), dateExport);

		ParquetEncryptionKey parquetEncryptionKey = properties.getS3OutputParquetKey().isEmpty() ? null
				: new ParquetEncryptionKey(EncryptionType.KEY256, properties.getS3OutputParquetKey());

		new ParquetDao().exportToParquet(tablesToExport, directoryOutTemp, parquetEncryptionKey);
	}


	public void copyToS3Out() throws ArcException {
		
		if (ArcS3.OUTPUT_BUCKET.isS3Off())
		{
			LoggerHelper.warn(LOGGER, "S3 OUPUT is OFF !");
			return;
		}
		
		// copy first to a temporary folder
		ArcS3.OUTPUT_BUCKET.createDirectory(this.s3OutTemp);

		for (File f : Objects.requireNonNull(new File(this.directoryOutTemp).listFiles())) {
			ArcS3.OUTPUT_BUCKET.upload(f, this.s3OutTemp + File.separator + f.getName());
		}
		
		// delete the temporary export directory
		FileUtilsArc.deleteDirectory(this.directoryOutTemp);

	}
	
	// commit
	// move files in temporary folder to final folders
	public void commitExportParquet() throws ArcException
	{
		if (ArcS3.OUTPUT_BUCKET.isS3Off())
		{
			// move files from directoryOutTemp to directoryOut if s3 is off
			
			File dirOutTemp = new File(this.directoryOutTemp);
			
			FileUtilsArc.createDirIfNotexist(new File(this.directoryOut));
			
			for (File f:dirOutTemp.listFiles())
			{
				FileUtilsArc.deplacerFichier(directoryOutTemp, this.directoryOut, f.getName(), f.getName());
			}
			
			FileUtilsArc.deleteDirectory(dirOutTemp);
			
			
			return;
		}

		// once upload to S3 complete, move the file in temporary bucket to the
		// permanent bucket
		ArcS3.OUTPUT_BUCKET.createDirectory(this.s3Out);
		ArcS3.OUTPUT_BUCKET.moveDirectory(this.s3OutTemp, this.s3Out);
	}
	
	/**
	 * Roll back file system and s3 on problem Delete the temporary directories or
	 * bucket used to export parquet files
	 * 
	 * @throws ArcException
	 */
	public void rollback() throws ArcException
	{
		// delete the temporary export directory
		FileUtilsArc.deleteDirectory(this.directoryOut);
		
		if (ArcS3.OUTPUT_BUCKET.isS3Off())
		{
			return;
		}
		
		// delete the export buckets
		ArcS3.OUTPUT_BUCKET.deleteDirectory(this.s3OutTemp);
		ArcS3.OUTPUT_BUCKET.deleteDirectory(this.s3Out);
		
	}

}
