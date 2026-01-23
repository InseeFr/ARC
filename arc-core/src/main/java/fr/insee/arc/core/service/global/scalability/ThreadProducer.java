package fr.insee.arc.core.service.global.scalability;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;

import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;

public class ThreadProducer extends ThreadWithException {

	private final OutputStream tos;
	private int connectionIndex;
	private String inputTable;

	public ThreadProducer(OutputStream tos, int connectionIndex, String inputTable) {
		super();
		this.tos = tos;
		this.connectionIndex = connectionIndex;
		this.inputTable = inputTable;
	}

	public void run() {
		try (Connection connectionIn = UtilitaireDao.get(connectionIndex).getDriverConnexion()) {

			try (tos)
			{
			CopyManager copyManagerIn = new CopyManager((BaseConnection) connectionIn);
			copyManagerIn.copyOut("COPY " + inputTable + " TO STDOUT (FORMAT BINARY)", tos);
			}
			
		} catch (SQLException | IOException | ArcException e) {
			this.setErrorInThread(true);
		}

	}

}
