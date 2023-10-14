package fr.insee.arc.ws.services.importServlet;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import fr.insee.arc.core.dataobjects.SchemaEnum;
import fr.insee.arc.core.model.Delimiters;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.ws.services.importServlet.actions.SendResponse;
import fr.insee.arc.ws.services.importServlet.bo.ArcClientIdentifier;
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

		this.arcClientIdentifier = new ArcClientIdentifier(dsnRequest);

		this.sources = makeSource(dsnRequest);

		this.clientDao = new ClientDao(this.arcClientIdentifier);

	}

	private List<String> sources;

	private void executeIf(String source, Executable exe) throws ArcException {
		if (!sources.contains(source)) {
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
		this.clientDao.dropPendingClientTables();

		this.clientDao.createTableWsStatus();

		if (!arcClientIdentifier.getEnvironnement().equalsIgnoreCase(SchemaEnum.ARC_METADATA.getSchemaName())) {
			clientDao.verificationClientFamille();
			tablesMetierNames = clientDao.getIdSrcTableMetier(dsnRequest);
		}

		startTableCreationInParallel();

		// on renvoie l'id du client avec son timestamp
		resp.send(arcClientIdentifier.getEnvironnement() + Delimiters.SQL_SCHEMA_DELIMITER
				+ arcClientIdentifier.getClient() + Delimiters.SQL_TOKEN_DELIMITER
				+ arcClientIdentifier.getTimestamp());

		resp.endSending();
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
						executeIf(ServletArc.MAPPING, () -> clientDao.createImages(tablesMetierNames));
						executeIf(ServletArc.METADATA, () -> clientDao.createTableMetier());
						executeIf(ServletArc.METADATA, () -> clientDao.createVarMetier());
					}
					executeIf(ServletArc.NOMENCLATURE, () -> clientDao.createNmcl());
					executeIf(ServletArc.METADATA, () -> clientDao.createTableFamille());
					executeIf(ServletArc.METADATA, () -> clientDao.createTablePeriodicite());
				} catch (ArcException e) {
					try {
						clientDao.createTableWsKO();
					} catch (ArcException e1) {
						new ArcException(ArcExceptionMessage.DATABASE_CONNECTION_FAILED).logFullException();
					}
				} finally {
					try {
						clientDao.dropTableWsPending();
					} catch (ArcException e) {
						try {
							clientDao.createTableWsKO();
						} catch (ArcException e1) {
							new ArcException(ArcExceptionMessage.DATABASE_CONNECTION_FAILED).logFullException();
						}
					}
				}

			}
		};

		maintenance.start();
	}

}
