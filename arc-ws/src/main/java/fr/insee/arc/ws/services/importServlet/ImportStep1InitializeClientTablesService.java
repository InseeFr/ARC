package fr.insee.arc.ws.services.importServlet;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import fr.insee.arc.core.dataobjects.SchemaEnum;
import fr.insee.arc.core.model.Delimiters;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
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

	public ImportStep1InitializeClientTablesService(JSONObject dsnRequest) throws ArcException {
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
		try {

			this.clientDao.dropPendingClientTables();
			
			startHandShake(resp);

			if (!arcClientIdentifier.getEnvironnement().equalsIgnoreCase(SchemaEnum.ARC_METADATA.getSchemaName())) {
				this.clientDao.verificationClientFamille();
				List<String> tablesMetierNames = this.clientDao.getIdSrcTableMetier(this.dsnRequest);
				executeIf(ServletArc.MAPPING, () -> this.clientDao.createImages(tablesMetierNames));
				executeIf(ServletArc.METADATA, () -> this.clientDao.createTableMetier());
				executeIf(ServletArc.METADATA, () -> this.clientDao.createVarMetier());
			}
			executeIf(ServletArc.NOMENCLATURE, () -> this.clientDao.createNmcl());
			executeIf(ServletArc.METADATA, () -> this.clientDao.createTableFamille());
			executeIf(ServletArc.METADATA, () -> this.clientDao.createTablePeriodicite());

			stopHandShake();

			// on renvoie l'id du client avec son timestamp
			resp.send(arcClientIdentifier.getEnvironnement()+ Delimiters.SQL_SCHEMA_DELIMITER + arcClientIdentifier.getClient()
					+ Delimiters.SQL_TOKEN_DELIMITER + arcClientIdentifier.getTimestamp());

			resp.endSending();
		} catch (ArcException e) {
			StaticLoggerDispatcher.error(LOGGER, "** Error in servlet ImportStep1InitializeClientTablesService **");
			resp.send("\"type\":\"jsonwsp/response\",\"error\":\"" + e.getMessage() + "\"}");
			resp.endSending();
		}
	}

	
	Thread maintenance;

	// set to 1 minute
	private static final  int HANDSHAKE_TIMER_IN_MS = 60000;
	
	/**
	 * Will send handshake to client every @HANDSHAKE_TIMER_IN_MS milliseconds
	 * Ugly but we failed at fixing that in front of a F5 controller
	 * @param resp
	 */
	private void startHandShake(SendResponse resp) {
		maintenance = new Thread() {
			@Override
			public void run() {
				do {
					try {
						Thread.sleep(HANDSHAKE_TIMER_IN_MS);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						break;
					}
					resp.send(Delimiters.HANDSHAKE_DELIMITER);
				} while (true);
			}
		};

		maintenance.start();
	}

	/**
	 * Stop to send handshake
	 * @param resp
	 */
	private void stopHandShake() {

		maintenance.interrupt();
		try {
			maintenance.join();
		} catch (InterruptedException e) {
			new ArcException(ArcExceptionMessage.STREAM_WRITE_FAILED, e).logFullException();
			Thread.currentThread().interrupt();
		}
	}

	
	
}
