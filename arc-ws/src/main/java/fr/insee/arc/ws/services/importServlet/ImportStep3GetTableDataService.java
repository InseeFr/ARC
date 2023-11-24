package fr.insee.arc.ws.services.importServlet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.utils.Sleep;
import fr.insee.arc.ws.services.importServlet.actions.SendResponse;
import fr.insee.arc.ws.services.importServlet.bo.ArcClientIdentifier;
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

		this.arcClientIdentifier = new ArcClientIdentifier(dsnRequest);

		clientDao = new ClientDao(arcClientIdentifier);

	}

	public void execute(SendResponse resp) throws ArcException {

		// binary transfer
		ServiceDao.execQueryExportDataToResponse(resp.getWr(),
				ViewEnum.normalizeTableName(arcClientIdentifier.getClient()), this.arcClientIdentifier.getFormat().equals(ExportFormat.CSV_GZIP.getFormat()));

		if (this.clientDao.isWebServiceNotPending()) {
			this.clientDao.dropTable(arcClientIdentifier.getClient());
		} else {
			Sleep.sleep(WAIT_DELAY_ON_PENDING_TABLES_CREATION_IN_MS);
		}

		resp.endSending();	
	}

}
