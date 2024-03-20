package fr.insee.arc.ws.services.importServlet.dao;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.database.ArcDatabase;
import fr.insee.arc.utils.database.TableToRetrieve;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;

public class ServiceDao {
	
	public static void execQueryExportDataToResponse(OutputStream os, TableToRetrieve table, boolean csvExportFormat) throws ArcException {

		int numberOfExecutorNods = ArcDatabase.numberOfExecutorNods();

		if (csvExportFormat)
		{
			try(GZIPOutputStream goz=new GZIPOutputStream(os);)
			{

				if (table.getNod().equals(ArcDatabase.EXECUTOR) && numberOfExecutorNods>0)
				{
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
			// binary transfer cannot be scaled
			if (numberOfExecutorNods>0)
			{
				throw new ArcException(ArcExceptionMessage.WS_RETRIEVE_DATA_SCALABLE_TABLE_MUST_BE_EXPORT_IN_CSV);
			}
			UtilitaireDao.get(0).exporting(null, table.getTableName(), os, csvExportFormat);
		}
	}

}
