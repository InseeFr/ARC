package fr.insee.arc.core.service.global.scalability;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;

import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;

public class ThreadConsumer extends ThreadWithException {

	private InputStream is;
	private int connectionIndex;
	private String outputTable;

	public ThreadConsumer(InputStream is, int connectionIndex, String outputTable) {
		super();
		this.is = is;
		this.connectionIndex = connectionIndex;
		this.outputTable = outputTable;
	}

	public void run() {
		try (Connection connectionOut = UtilitaireDao.get(connectionIndex).getDriverConnexion()) {
				CopyManager copyManagerOut = new CopyManager((BaseConnection) connectionOut);
				copyManagerOut.copyIn("COPY " + outputTable + " FROM STDIN WITH (FORMAT BINARY)", is);
		} catch (SQLException  | IOException | ArcException e) {
			this.setErrorInThread(true);
		}

	}

}
