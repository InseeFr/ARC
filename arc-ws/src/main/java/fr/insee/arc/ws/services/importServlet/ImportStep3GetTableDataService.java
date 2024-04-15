package fr.insee.arc.ws.services.importServlet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import fr.insee.arc.utils.database.TableToRetrieve;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.utils.Sleep;
import fr.insee.arc.ws.services.importServlet.actions.SendResponse;
import fr.insee.arc.ws.services.importServlet.bo.ArcClientIdentifier;
import fr.insee.arc.ws.services.importServlet.bo.ArcClientIdentifierUnsafe;
import fr.insee.arc.ws.services.importServlet.bo.ExportFormat;
import fr.insee.arc.ws.services.importServlet.dao.ClientDao;
import fr.insee.arc.ws.services.importServlet.dao.ServiceDao;

public class ImportStep3GetTableDataService {

	protected static final Logger LOGGER = LogManager.getLogger(ImportStep3GetTableDataService.class);

	// delay to wait before requiring a new table when webservice is still creating tables to retrieve
	private static final int WAIT_DELAY_ON_PENDING_TABLES_CREATION_IN_MS = 10000;

	private ClientDao clientDao;

	private ArcClientIdentifier arcClientIdentifier;

	public ImportStep3GetTableDataService(JSONObject dsnRequest) {
		super();

		this.arcClientIdentifier = new ArcClientIdentifier(new ArcClientIdentifierUnsafe(dsnRequest, false));

		clientDao = new ClientDao(arcClientIdentifier);

	}

	public void execute(SendResponse resp) throws ArcException {

		TableToRetrieve table = clientDao.getAClientTableByName(arcClientIdentifier.getClientInputParameter());
		
		// binary transfer
		ServiceDao.execQueryExportDataToResponse(resp.getWr(), table, ExportFormat.isCsv(this.arcClientIdentifier.getFormat()));

		if (this.clientDao.isWebServiceNotPending()) {
			this.clientDao.dropTable(table);
			this.clientDao.deleteFromTrackTable(table.getTableName());
		} else {
			Sleep.sleep(WAIT_DELAY_ON_PENDING_TABLES_CREATION_IN_MS);
		}

		resp.endSending();	
	}

}
