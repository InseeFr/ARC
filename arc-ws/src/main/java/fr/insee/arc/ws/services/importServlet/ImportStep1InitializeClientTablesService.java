package fr.insee.arc.ws.services.importServlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.dataobjects.SchemaEnum;
import fr.insee.arc.core.util.BDParameters;
import fr.insee.arc.utils.database.ArcDatabase;
import fr.insee.arc.utils.database.TableToRetrieve;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.ws.services.importServlet.actions.SendResponse;
import fr.insee.arc.ws.services.importServlet.bo.ArcClientIdentifier;
import fr.insee.arc.ws.services.importServlet.bo.ExportSource;
import fr.insee.arc.ws.services.importServlet.dao.ClientDao;

public class ImportStep1InitializeClientTablesService {

	protected static final Logger LOGGER = LogManager.getLogger(ImportStep1InitializeClientTablesService.class);

	static interface Executable {
		void execute() throws ArcException;
	}

	private ClientDao clientDao;
	private ArcClientIdentifier arcClientIdentifier;
	private List<String> tablesMetierNames;

	public ImportStep1InitializeClientTablesService(ArcClientIdentifier arcClientIdentifier) {
		super();

		this.arcClientIdentifier = arcClientIdentifier;

		this.clientDao = new ClientDao(this.arcClientIdentifier);

	}

	private void executeIfNotMetadataSchema(Executable exe) throws ArcException {
		executeIf(!arcClientIdentifier.getEnvironnement().equalsIgnoreCase(SchemaEnum.ARC_METADATA.getSchemaName()), exe);
	}
	
	
	private void executeIfSourceDeclared(ExportSource source, Executable exe) throws ArcException {
		executeIf(arcClientIdentifier.getSource().contains(source), exe);
	}
	
	private void executeIfParquetDeclared(Executable exe) throws ArcException {
		executeIf(arcClientIdentifier.getFormat().isParquet(), exe);
	}
	

	private void executeIf(Boolean condition, Executable exe) throws ArcException {
		if (!condition) {
			return;
		}
		exe.execute();
	}

	public void execute(SendResponse resp) throws ArcException {

		LoggerHelper.info(LOGGER, "Data retrieval webservice invoked for client " + this.arcClientIdentifier.getClientIdentifier() + " on "+ this.arcClientIdentifier.getEnvironnement() + " with timestamp "+ this.arcClientIdentifier.getTimestamp());
		
		long formerCallTimestamp = clientDao.extractWsTrackingTimestamp();
		if (formerCallTimestamp > 0L)
		{
			LoggerHelper.info(LOGGER, "CONCURRENT CLIENT CALL WITH TIMESTAMP " + formerCallTimestamp );
		}

		// drop tables from the client that had been requested from a former call
		this.clientDao.dropPendingClientObjects();
		
		// create the table that will track the data table which has been built and retrieved
		createTrackTable();
		
		// create the wsinfo and the wspending table
		// wspending table will be delete when all 
		createWsTables();

		// create tables to retrieve family data table
		executeIfNotMetadataSchema(() -> createFamilyMappingTables());

		// create data table in an asynchronous parallel thread
		startTableCreationInParallel();

		// on renvoie l'id du client avec son timestamp
		resp.send(arcClientIdentifier.getSessionId());

		resp.endSending();
	}

	/**
	 * 1. check if the client has the right to retrieve the family. If so :
	 * 2. build the table of id_source to be retrieved in the family data table
	 * 3. return the list of family data table to retrieve
	 * @throws ArcException
	 */
	private void createFamilyMappingTables() throws ArcException {

		// check if client is allowed to retrieve family data
		if (!clientDao.verificationClientFamille()) {
			throw new ArcException(ArcExceptionMessage.WS_RETRIEVE_DATA_FAMILY_FORBIDDEN);
		}
		
		// create the table that contains the list of files to be retrieved
		clientDao.createTableOfIdSource();
		
		// get the family mapping tables name to be retrieved
		tablesMetierNames = clientDao.selectBusinessDataTables();
		
		// filter the mapping tables if any filter declared
		if (!arcClientIdentifier.getMappingTablesFilter().isEmpty())
		{
			tablesMetierNames.retainAll(arcClientIdentifier.getMappingTablesFilter());
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
		
		List<TableToRetrieve> tablesToExport = new ArrayList<>();
		tablesToExport.addAll(this.clientDao.createTableWsInfo());
		executeIfParquetDeclared(() -> exportToParquet(tablesToExport));
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
					
					List<TableToRetrieve> tablesToExport = new ArrayList<>();
					
					if (tablesMetierNames != null) {

						executeIfSourceDeclared(ExportSource.MAPPING, () -> tablesToExport.addAll(createImages(tablesMetierNames)));
						executeIfSourceDeclared(ExportSource.METADATA, () -> tablesToExport.addAll(clientDao.createTableMetier()));
						executeIfSourceDeclared(ExportSource.METADATA, () -> tablesToExport.addAll(clientDao.createTableVarMetier()));
					}
					
					executeIfSourceDeclared(ExportSource.NOMENCLATURE, () -> tablesToExport.addAll(clientDao.createTableNmcl()));
					executeIfSourceDeclared(ExportSource.METADATA, () -> tablesToExport.addAll(clientDao.createTableFamille()));
					executeIfSourceDeclared(ExportSource.METADATA, () -> tablesToExport.addAll(clientDao.createTablePeriodicite()));

					executeIfParquetDeclared(() -> exportToParquet(tablesToExport));
					
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
	 * create image tables on executor nods if connection is scaled, on coordinator
	 * nod if not
	 * 
	 * @param tablesMetierNames
	 * @throws ArcException
	 */
	private List<TableToRetrieve> createImages(List<String> tablesMetierNames) throws ArcException {
		int numberOfExecutorNods = ArcDatabase.numberOfExecutorNods();
		
		List<TableToRetrieve> tablesToRetrieve = new ArrayList<>();

		if (numberOfExecutorNods == 0) {
			clientDao.createImages(tablesMetierNames, ArcDatabase.COORDINATOR.getIndex())
			.forEach(t -> tablesToRetrieve.add(new TableToRetrieve(ArcDatabase.COORDINATOR, t)));
			
			
		} else {
			for (int executorConnectionId = ArcDatabase.EXECUTOR.getIndex(); executorConnectionId < ArcDatabase.EXECUTOR
					.getIndex() + numberOfExecutorNods; executorConnectionId++) {
				
				// copy the table containing id_source to be retrieved on executor nods
				clientDao.copyTableOfIdSourceToExecutorNod(executorConnectionId);
				
				// create the business table containing data of id_source found in table tableOfIdSource
				clientDao.createImages(tablesMetierNames, executorConnectionId)
				.forEach(t -> tablesToRetrieve.add(new TableToRetrieve(ArcDatabase.EXECUTOR, t)))
				;
			}
		}

		return tablesToRetrieve;
	}
	
	private void exportToParquet(List<TableToRetrieve> tablesToExport) throws ArcException
	{
		clientDao.exportToParquet(tablesToExport);
	}

}
