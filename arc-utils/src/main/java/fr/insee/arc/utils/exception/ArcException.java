package fr.insee.arc.utils.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.utils.utils.LoggerHelper;

public class ArcException extends Exception {

	private static final Logger LOGGER = LogManager.getLogger(ArcException.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = -6127318245282541168L;
	
	private ArcExceptionMessage error;
	
	/**
	 * build a custom business exception
	 * @param error
	 * @param parameters
	 */
	public ArcException(ArcExceptionMessage error, Object...parameters) {
		super(error.formatException(parameters));
		this.initCause(new Throwable(error.formatException(parameters)));
		this.error = error;
	}
	
	/**
	 * build a custom technical exception
	 * @param rootException
	 * @param error
	 * @param parameters
	 */
	public ArcException(Exception rootException, ArcExceptionMessage error, Object...parameters) {
		super(error.formatException(parameters),rootException);
	}

	public ArcException logFullException()
	{
		LoggerHelper.error(LOGGER, this.getCause(), this.getMessage());
		this.printStackTrace();
		return this;
	}
	
	public ArcException logMessageException()
	{
		LoggerHelper.error(LOGGER, this.getMessage());
		return this;
	}

	public ArcExceptionMessage getError() {
		return error;
	}
	
	

}
