package fr.insee.arc.ws.services.importServlet.dao;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import fr.insee.arc.core.dataobjects.ArcDatabase;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.ws.services.importServlet.bo.TableToRetrieve;

public class ServiceDao {
	
	public static void execQueryExportDataToResponse(OutputStream os, TableToRetrieve table, boolean csvExportFormat) throws ArcException {
		
		if (csvExportFormat)
		{
			try(GZIPOutputStream goz=new GZIPOutputStream(os);)
			{
				if (table.getNod().equals(ArcDatabase.EXECUTOR))
				{
					int numberOfExecutorNods = ArcDatabase.numberOfExecutorNods();
					for (int executorConnectionId = ArcDatabase.EXECUTOR.getIndex(); executorConnectionId < ArcDatabase.EXECUTOR
							.getIndex() + numberOfExecutorNods; executorConnectionId++) {
						UtilitaireDao.get(executorConnectionId).exporting(null, table.getTableName(), goz, csvExportFormat);
					}
				}
				else
				{
					UtilitaireDao.get(0).exporting(null, table.getTableName(), goz, csvExportFormat);
				}
			} catch (IOException e) {
				throw new ArcException(ArcExceptionMessage.STREAM_WRITE_FAILED);
			}
		}
		else
		{
			if (table.getNod().equals(ArcDatabase.EXECUTOR))
			{
				throw new ArcException(ArcExceptionMessage.WS_RETRIEVE_DATA_SCALABLE_TABLE_MUST_BE_EXPORT_IN_CSV);
			}
			UtilitaireDao.get(0).exporting(null, table.getTableName(), os, csvExportFormat);
		}
	}

}
