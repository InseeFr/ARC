package fr.insee.arc.ws.services.importServlet.dao;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;

public class ServiceDao {
	
	public static void execQueryExportDataToResponse(OutputStream os, String tableName, boolean csvExportFormat) throws ArcException {
		
		if (csvExportFormat)
		{
			try(GZIPOutputStream goz=new GZIPOutputStream(os);)
			{
				UtilitaireDao.get(0).exporting(null, tableName, goz, csvExportFormat);
			} catch (IOException e) {
				throw new ArcException(ArcExceptionMessage.STREAM_WRITE_FAILED);
			}
		}
		else
		{
			UtilitaireDao.get(0).exporting(null, tableName, os, csvExportFormat);
		}
	}

}
