package fr.insee.arc.ws.services.importServlet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.ws.services.importServlet.actions.SendResponse;
import fr.insee.arc.ws.services.importServlet.bo.ArcClientIdentifier;
import fr.insee.arc.ws.services.importServlet.dao.ClientDao;
import fr.insee.arc.ws.services.importServlet.dao.ServiceDao;

public class ImportStep3GetTableDataService {
	
	protected static final Logger LOGGER = LogManager.getLogger(ImportStep3GetTableDataService.class);

	private static final boolean IS_EXPORT_CSV = false;

	private ClientDao clientDao;

	private ArcClientIdentifier arcClientIdentifier;
	
	public ImportStep3GetTableDataService(JSONObject dsnRequest) {
		super();

		this.arcClientIdentifier = new ArcClientIdentifier(dsnRequest);
		
		clientDao = new ClientDao(arcClientIdentifier);
		
	}

	public void execute(SendResponse resp) throws ArcException {

		try {
			// binary transfer
			ServiceDao.execQueryExportDataToResponse(resp.getWr(), arcClientIdentifier.getClient(), IS_EXPORT_CSV);
			this.clientDao.dropTable(arcClientIdentifier.getClient());

			resp.endSending();

			// renvoie un nom de table du client si il en reste une

		} catch (ArcException e) {
			StaticLoggerDispatcher.error(LOGGER, "** Error in servlet ImportStep3GetTableDataService **");
			resp.send("\"type\":\"jsonwsp/response\",\"error\":\"" + e.getMessage() + "\"}");
			resp.endSending();
		}
	}

}
