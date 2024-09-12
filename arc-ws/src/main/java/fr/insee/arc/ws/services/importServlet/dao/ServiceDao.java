package fr.insee.arc.ws.services.importServlet.dao;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.service.p6export.parquet.ParquetDao;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.database.ArcDatabase;
import fr.insee.arc.utils.database.TableToRetrieve;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.files.CompressedUtils;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.ws.services.importServlet.bo.ExportFormat;

public class ServiceDao {
	
	protected static final Logger LOGGER = LogManager.getLogger(ServiceDao.class);

	public static void execQueryExportDataToResponse(OutputStream os, TableToRetrieve table, ExportFormat format, ClientDao clientDao) throws ArcException {

		LoggerHelper.info(LOGGER, "Transfer from " + table.getTableName() + " started");		
		LoggerHelper.info(LOGGER, "Data format is " + format);
		
		switch(format)
		{
			case BINARY:exportBinary(os, table); break;
			case CSV_GZIP:exportCsvGzip(os, table); break;
			case PARQUET:exportParquet(os, table, clientDao); break;
		}

		LoggerHelper.info(LOGGER, "Transfer data from " + table.getTableName() + " ended");		
		
	}

	private static void exportBinary(OutputStream os, TableToRetrieve table) throws ArcException {
		
		int numberOfExecutorNods = ArcDatabase.numberOfExecutorNods();
		
		// binary transfer cannot be scaled
		if (numberOfExecutorNods>0)
		{
			throw new ArcException(ArcExceptionMessage.WS_RETRIEVE_DATA_SCALABLE_TABLE_MUST_BE_EXPORT_IN_CSV);
		}
		UtilitaireDao.get(0).exporting(null, table.getTableName(), os, false);		
	}

	private static void exportCsvGzip(OutputStream os, TableToRetrieve table) throws ArcException {
		
		int numberOfExecutorNods = ArcDatabase.numberOfExecutorNods();
		
		try(GZIPOutputStream goz=new GZIPOutputStream(os);)
		{

			if (table.getNod().equals(ArcDatabase.EXECUTOR) && numberOfExecutorNods>0)
			{
				for (int executorConnectionId = ArcDatabase.EXECUTOR.getIndex(); executorConnectionId < ArcDatabase.EXECUTOR
						.getIndex() + numberOfExecutorNods; executorConnectionId++) {
					UtilitaireDao.get(executorConnectionId).exporting(null, table.getTableName(), goz, true);
				}
			}
			else
			{
				UtilitaireDao.get(0).exporting(null, table.getTableName(), goz, true);
			}
		} catch (IOException e) {
			throw new ArcException(ArcExceptionMessage.STREAM_WRITE_FAILED);
		}		
	}


	/**
	 * Export table to retrieve to a parquet file located in the sandbox directory
	 */
	private static void exportParquet(OutputStream os, TableToRetrieve table, ClientDao clientDao) throws ArcException {
		
		File fileToTransfer = new File(ParquetDao.exportTablePath(table,clientDao.getParquetDirectory()));
		
		try (FileInputStream fis = new FileInputStream(fileToTransfer);
			 BufferedInputStream bis = new BufferedInputStream(fis, CompressedUtils.READ_BUFFER_SIZE);)
		{
			byte[] buffer = new byte[CompressedUtils.READ_BUFFER_SIZE];
			int len;
			while ((len = bis.read(buffer)) != -1) {
			    os.write(buffer, 0, len);
			}
		} catch (FileNotFoundException e) {
			throw new ArcException(e, ArcExceptionMessage.FILE_READ_FAILED, fileToTransfer);
		} catch (IOException e) {
			throw new ArcException(e, ArcExceptionMessage.FILE_READ_FAILED, fileToTransfer);
		}
	}

	
}
