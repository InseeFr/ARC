package fr.insee.arc.ws.services.importServlet.dao;

import java.io.OutputStream;

import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;

public class ServiceDao {

	
	public static void execQueryExportDataToResponse(OutputStream os, String tableName, boolean csvExportFormat) throws ArcException {
		UtilitaireDao.get(0).exporting(null, tableName, os, csvExportFormat);
	}

}
