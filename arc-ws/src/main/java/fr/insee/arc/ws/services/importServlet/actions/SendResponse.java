package fr.insee.arc.ws.services.importServlet.actions;

import java.io.IOException;
import java.io.OutputStream;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.utils.LoggerHelper;

/**
 * Cette classe permet d'envoyer la réponse au client
 */
public class SendResponse {

	private static final Logger LOGGER = LogManager.getLogger(SendResponse.class);

	private OutputStream wr;
	private HttpServletResponse response;

	public SendResponse(OutputStream os) {
		this.wr = os;
	}
	
	public SendResponse(HttpServletResponse response) {
		this.response = response;
		try {
			this.response.setBufferSize(128 * 1024);
			this.wr = this.response.getOutputStream();
		} catch (IOException e) {
			StaticLoggerDispatcher.error(LOGGER, "** Error in servlet SendResponse **");
		}
	}

	/**
	 * Ecrit la chaîne de caractères dans le flux de réponse compressé.
	 * 
	 * @param string
	 */
	public void send(String string) {
		try {
			this.wr.write(string.getBytes());// "UTF-8"
		} catch (IOException ex) {
			LoggerHelper.errorGenTextAsComment(getClass(), "send()", LOGGER, ex);
		}
	}

	public void sendError(ArcException e) {
		try {
			this.response.sendError(500, e.getMessage());
		} catch (IOException e1) {
			StaticLoggerDispatcher.error(LOGGER, "** Error in servlet SendResponse **");
		}
	}

	/**
	 * Fermeture du flux.
	 *
	 */
	public void endSending() {
		try {
			this.wr.flush();
			this.wr.close();
		} catch (IOException ex) {
			LoggerHelper.errorGenTextAsComment(getClass(), "endSending()", LOGGER, ex);
		}
	}

	public OutputStream getWr() {
		return wr;
	}

}
