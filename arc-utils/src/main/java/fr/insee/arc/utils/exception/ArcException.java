package fr.insee.arc.utils.exception;

public class ArcException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6127318245282541168L;

	public ArcException(String error) {
		super(error);
	}
	
	public ArcException(Exception ex) {
		super(ex);
	}

	public ArcException(String error, Exception ex) {
		super(ex);
	}

}
