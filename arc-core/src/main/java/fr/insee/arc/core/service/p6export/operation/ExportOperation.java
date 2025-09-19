package fr.insee.arc.core.service.p6export.operation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.core.service.p6export.dao.ExportDao;
import fr.insee.arc.core.service.p6export.dao.ExportMasterNodDao;
import fr.insee.arc.core.service.p6export.dao.ExportParquetDao;
import fr.insee.arc.utils.database.ArcDatabase;
import fr.insee.arc.utils.database.TableToRetrieve;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.utils.LoggerHelper;

public class ExportOperation {

	private String paramBatch;
	private String dateExport;
	
	private ExportDao exportDao;
	private ExportParquetDao exportParquetDao;
	private ExportMasterNodDao exportMasterNodDao;
	
	List<TableToRetrieve> mappingTablesNameExported = new ArrayList<>();

	private static final Logger LOGGER = LogManager.getLogger(ExportOperation.class);

	
	public ExportOperation(Sandbox coordinatorSandbox, String paramBatch) {
		this.exportDao = new ExportDao(coordinatorSandbox);
		this.exportParquetDao = new ExportParquetDao(coordinatorSandbox);
		this.exportMasterNodDao = new ExportMasterNodDao(coordinatorSandbox);
		this.paramBatch = paramBatch;
	}

	public void initializeExport() throws ArcException {
		this.dateExport = exportDao.dateExport();
	}
	
	public void exportParquet() throws ArcException {
		
		// select business table to be exported
		Set<String> mappingTablesName = exportParquetDao.selectBusinessTableToExport();
		// assign business table to the right nod
		List<TableToRetrieve> tablesToExport = exportParquetDao.fetchBusinessTableToNod(mappingTablesName);
		
		mappingTablesNameExported.addAll(tablesToExport);
		
		// export to parquet
		exportParquetDao.exportTablesToParquet(dateExport, tablesToExport);
		
		// copy exported directory to s3
		exportParquetDao.copyToS3Out();

	}

	/**
	 * Export the data to master nod if required
	 * Only active in scalability mode .e.e ARC with executor nods
	 * @throws ArcException
	 */
	public void exportToMasterNod() throws ArcException {
		
		if (!ArcDatabase.isScaled()) {
			return;
		}
		
		Set<String> mappingTablesName = exportMasterNodDao.selectBusinessTableToExport();
		mappingTablesName.stream().forEach( t -> mappingTablesNameExported.add(new TableToRetrieve(ArcDatabase.EXECUTOR,t)));
		
		LoggerHelper.warn(LOGGER, "Tables to copy in the master database : ");
		LoggerHelper.warn(LOGGER, mappingTablesName);
		
		exportMasterNodDao.copyMappingTablesToMasterNod(mappingTablesName);
		
	}
	
	
	public void markExportedInPilotage() throws ArcException {
		// mark exported data in pilotage table in batch mode
		exportDao.markExportedData();
	}
	
	/**
	 * Mark the data that had been exported
	 * @throws ArcException
	 */
	public void commitExport() throws ArcException {
		
		exportParquetDao.commitExportParquet();
		
		exportDao.commit();

		exportDao.truncateExportedMappingTables(mappingTablesNameExported);

	}

	
	/**
	 * reset filesystem and s3
	 * @throws ArcException 
	 */
	public void rollBack() throws ArcException {
		exportParquetDao.rollback();
	}
	
}
