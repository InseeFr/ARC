package fr.insee.arc.ws.services.importServlet;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import fr.insee.arc.core.dataobjects.ArcDatabase;
import fr.insee.arc.core.dataobjects.SchemaEnum;
import fr.insee.arc.core.model.Delimiters;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.ws.services.importServlet.actions.SendResponse;
import fr.insee.arc.ws.services.importServlet.bo.ArcClientIdentifier;
import fr.insee.arc.ws.services.importServlet.bo.ExportSource;
import fr.insee.arc.ws.services.importServlet.bo.JsonKeys;
import fr.insee.arc.ws.services.importServlet.dao.ClientDao;

public class ImportStep1InitializeClientTablesService {

	protected static final Logger LOGGER = LogManager.getLogger(ImportStep1InitializeClientTablesService.class);

	static interface Executable {
		void execute() throws ArcException;
	}

	private ClientDao clientDao;
	private JSONObject dsnRequest;
	private ArcClientIdentifier arcClientIdentifier;
	private List<String> tablesMetierNames;

	public ImportStep1InitializeClientTablesService(JSONObject dsnRequest) {
		super();

		this.dsnRequest = dsnRequest;

		this.arcClientIdentifier = new ArcClientIdentifier(dsnRequest, true);

		this.sources = makeSource(dsnRequest);

		this.clientDao = new ClientDao(this.arcClientIdentifier);

	}

	private List<String> sources;

	private void executeIf(ExportSource source, Executable exe) throws ArcException {
		if (!sources.contains(source.getSource())) {
			return;
		}
		exe.execute();
	}

	private static List<String> makeSource(JSONObject dsnRequest) {
		JSONArray source = dsnRequest.getJSONArray(JsonKeys.SOURCE.getKey());
		List<String> returned = new ArrayList<>();
		for (int i = 0; i < source.length(); i++) {
			returned.add(source.getString(i));
		}
		return returned;
	}

	public void execute(SendResponse resp) throws ArcException {
		
		// drop tables from the client that had been requested from a former call
		dropPendingClientTables();

		// create the table that will track the data table which has been built and retrieved
		createTrackTable();
		
		// create the wsinfo and the wspending table
		// wspending table will be delete when all 
		createWsTables();

		// create tables to retrieve family data table
		createMetaFamilyTables();

		// create data table in an asynchronous parallel thread
		startTableCreationInParallel();

		// on renvoie l'id du client avec son timestamp
		resp.send(arcClientIdentifier.getEnvironnement() + Delimiters.SQL_SCHEMA_DELIMITER
				+ arcClientIdentifier.getClientIdentifier() + Delimiters.SQL_TOKEN_DELIMITER
				+ arcClientIdentifier.getTimestamp());

		resp.endSending();
	}

	/**
	 * 1. check if the client has the right to retrieve the family. If so :
	 * 2. build the table of id_source to be retrieved in the family data table
	 * 3. return the list of family data table to retrieve
	 * @throws ArcException
	 */
	private void createMetaFamilyTables() throws ArcException {
		if (!arcClientIdentifier.getEnvironnement().equalsIgnoreCase(SchemaEnum.ARC_METADATA.getSchemaName())) {
			
			if (!clientDao.verificationClientFamille()) {
				throw new ArcException(ArcExceptionMessage.WS_RETRIEVE_DATA_FAMILY_FORBIDDEN);
			}
			
			clientDao.createTableOfIdSource(dsnRequest);
			tablesMetierNames = clientDao.selectBusinessDataTables();
		}
	}

	/**
	 * create the table that tracks the client table which had been built
	 * when the data of a table will be retrieved by the client, the table entry will be deleted from the track table 
	 * @throws ArcException
	 */
	private void createTrackTable() throws ArcException {
		clientDao.createTableTrackRetrievedTables();
	}

	/**
	 * create the wsinfo and wspending tables
	 * wspending will be deleted when all client tables will have been retrieved
	 * wsinfo table will be looped transfered to the client until wspending table is dropped
	 * @throws ArcException
	 */
	private void createWsTables() throws ArcException {
		this.clientDao.createTableWsInfo();
	}

	/**
	 * Will send handshake to client every @HANDSHAKE_TIMER_IN_MS milliseconds Ugly
	 * but we failed at fixing that in front of a F5 controller
	 * 
	 * @param resp
	 */
	private void startTableCreationInParallel() {
		Thread maintenance = new Thread() {
			@Override
			public void run() {
				try {
					if (tablesMetierNames != null) {

						executeIf(ExportSource.MAPPING, () -> createImages(tablesMetierNames));
						executeIf(ExportSource.METADATA, () -> clientDao.createTableMetier());
						executeIf(ExportSource.METADATA, () -> clientDao.createTableVarMetier());
					}
					executeIf(ExportSource.NOMENCLATURE, () -> clientDao.createTableNmcl());
					executeIf(ExportSource.METADATA, () -> clientDao.createTableFamille());
					executeIf(ExportSource.METADATA, () -> clientDao.createTablePeriodicite());
				} catch (ArcException e) {
						e.logFullException();
						clientDao.registerWsKO();
				} finally {
					try {
						clientDao.dropTableWsPending();
					} catch (ArcException e) {
						e.logFullException();
						clientDao.registerWsKO();
					}
				}

			}
		};

		maintenance.start();
	}

	
	/**
	 * drop tables on coordinator and executors if the exists
	 * @throws ArcException
	 */
	private void dropPendingClientTables() throws ArcException {
		
		this.clientDao.dropPendingClientTables(0);
		
		int numberOfExecutorNods = ArcDatabase.numberOfExecutorNods();
		for (int executorConnectionId = ArcDatabase.EXECUTOR.getIndex(); executorConnectionId < ArcDatabase.EXECUTOR
				.getIndex() + numberOfExecutorNods; executorConnectionId++) {
			this.clientDao.dropPendingClientTables(executorConnectionId);
		}
	}

	
	
	/**
	 * create image tables on executor nods if connection is scaled, on coordinator
	 * nod if not
	 * 
	 * @param tablesMetierNames
	 * @throws ArcException
	 */
	private void createImages(List<String> tablesMetierNames) throws ArcException {
		int numberOfExecutorNods = ArcDatabase.numberOfExecutorNods();
		if (numberOfExecutorNods == 0) {
			clientDao.createImages(tablesMetierNames, ArcDatabase.COORDINATOR.getIndex());
		} else {
			for (int executorConnectionId = ArcDatabase.EXECUTOR.getIndex(); executorConnectionId < ArcDatabase.EXECUTOR
					.getIndex() + numberOfExecutorNods; executorConnectionId++) {
				
				// copy the table containing id_source to be retrieved on executor nods
				clientDao.copyTableOfIdSourceToExecutorNod(executorConnectionId);
				
				// create the business table containing data of id_source found in table tableOfIdSource
				clientDao.createImages(tablesMetierNames, executorConnectionId);
			}
		}
	}

}
