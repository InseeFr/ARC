package fr.insee.arc.ws.services.importServlet;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.utils.database.TableToRetrieve;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.ws.services.importServlet.actions.SendResponse;
import fr.insee.arc.ws.services.importServlet.bo.ArcClientIdentifier;
import fr.insee.arc.ws.services.importServlet.bo.ExportTrackingType;
import fr.insee.arc.ws.services.importServlet.dao.ClientDao;
import fr.insee.arc.ws.services.importServlet.dao.NameDao;

public class ImportStep2GetTableNameService {

	protected static final Logger LOGGER = LogManager.getLogger(ImportStep2GetTableNameService.class);
	
	private static final String NO_MORE_TABLE_TO_BE_RETRIEVED = " ";

	private ClientDao clientDao;

	private boolean reprise;

	public ImportStep2GetTableNameService(ArcClientIdentifier arcClientIdentifier) {
		super();

		reprise = arcClientIdentifier.getReprise();

		clientDao = new ClientDao(arcClientIdentifier);

	}

	public void execute(SendResponse resp) throws ArcException {

		// check if a KO
		if (this.clientDao.getAClientTableByType(ExportTrackingType.KO).getTableName() != null) {
			throw new ArcException(ArcExceptionMessage.WS_RETRIEVE_DATA_FAMILY_CREATION_FAILED);
		}
		
		// try to get a data table
		TableToRetrieve table = this.clientDao.getAClientTableByType(ExportTrackingType.DATA);

		if (table.getTableName() != null) {
			
			LoggerHelper.info(LOGGER, "Table " + table.getTableName() + " had been required by the client.");

			StringBuilder type = new StringBuilder();

			// récupération du type
			List<List<String>> metadataOnlyTable = NameDao.execQuerySelectMetadata(table);

			for (int j = 0; j < metadataOnlyTable.get(0).size(); j++) {
				if (j > 0) {
					type.append(",");
				}

				for (int i = 0; i < metadataOnlyTable.size(); i++) {
					type.append(" " + metadataOnlyTable.get(i).get(j));
				}
			}
			
			LoggerHelper.info(LOGGER, "Table metadata is " + type + ".");

			
			String output = table.getTableName() + " " + type;
			
			// renvoie un nom de table du client si il en reste une
			resp.send(output);
			resp.endSending();
			
			return;
		}

		// if no data table found, get source table to register
		table = this.clientDao.getAClientTableByType(ExportTrackingType.ID_SOURCE);
		
		if (table.getTableName() != null) {

			LoggerHelper.info(LOGGER, "Table ID_SOURCE : " + table.getTableName() + " had been required by the client.");

			if (!reprise) {
				this.clientDao.updatePilotage(table.getTableName());
			}
			
			this.clientDao.dropTable(table);
			
		}
		
		table = this.clientDao.getAClientTableByType(ExportTrackingType.TRACK);
		this.clientDao.dropTable(table);
		
		resp.send(NO_MORE_TABLE_TO_BE_RETRIEVED);
		resp.endSending();

	}

}
