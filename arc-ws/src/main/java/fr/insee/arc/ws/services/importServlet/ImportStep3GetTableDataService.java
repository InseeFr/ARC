package fr.insee.arc.ws.services.importServlet;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.utils.database.TableToRetrieve;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.Sleep;
import fr.insee.arc.ws.services.importServlet.actions.SendResponse;
import fr.insee.arc.ws.services.importServlet.bo.ArcClientIdentifier;
import fr.insee.arc.ws.services.importServlet.dao.ClientDao;
import fr.insee.arc.ws.services.importServlet.dao.ServiceDao;

public class ImportStep3GetTableDataService {

	protected static final Logger LOGGER = LogManager.getLogger(ImportStep3GetTableDataService.class);

	// delay to wait before requiring a new table when webservice is still creating tables to retrieve
	private static final int WAIT_DELAY_ON_PENDING_TABLES_CREATION_IN_MS = 10000;

	private ClientDao clientDao;

	private ArcClientIdentifier arcClientIdentifier;

	public ImportStep3GetTableDataService(ArcClientIdentifier arcClientIdentifier) {
		super();

		this.arcClientIdentifier = arcClientIdentifier;

		clientDao = new ClientDao(arcClientIdentifier);

	}

	public void execute(SendResponse resp) throws ArcException {

		TableToRetrieve table = clientDao.getAClientTableByName(arcClientIdentifier.getClientInputParameter());

		this.clientDao.deleteFromTrackTable(table.getTableName());

		// transfer data to http response
		ServiceDao.execQueryExportDataToResponse(resp.getWr(), table, this.arcClientIdentifier.getFormat(), clientDao);

		if (this.clientDao.isWebServiceNotPending()) {
			this.clientDao.dropTable(table);
			
			if (arcClientIdentifier.getFormat().isParquet())
				this.clientDao.deleteParquet(table);
			
			LoggerHelper.info(LOGGER, "Table " + table.getTableName() + " had been retrieved and dropped.");
			
		} else {
			Sleep.sleep(WAIT_DELAY_ON_PENDING_TABLES_CREATION_IN_MS);
		}

		resp.endSending();

	}

}
