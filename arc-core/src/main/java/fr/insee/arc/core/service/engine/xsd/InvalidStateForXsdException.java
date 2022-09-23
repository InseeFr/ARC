package fr.insee.arc.core.service.engine.xsd;

/** Exception class for problems that might occur during an XSD-format export
 * when the control rules are not fit for exports
 * (multiples types or multiples parents for an element, for instance).*/
public class InvalidStateForXsdException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3824253187638543400L;

	/** Exception during an XSD-format export
	 * when the control rules are not fit for the export
	 * (multiples types or multiples parents for an element, for instance).*/
	public InvalidStateForXsdException(String string) {
		super(string);
	}

}