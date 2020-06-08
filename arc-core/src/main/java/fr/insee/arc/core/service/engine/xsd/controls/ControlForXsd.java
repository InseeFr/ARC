package fr.insee.arc.core.service.engine.xsd.controls;

/** Description of a control rule for use in XSD-format export.*/
public interface ControlForXsd extends Comparable<ControlForXsd> {

	/** Writes the control in an XSD-valid format. The result can be empty : {@link #hasContent()}.*/
	String writeControlAsXsd(String indentation);

	/** Returns true if there is any XSD-format information to write for this control.
	 * A control might not have any contents, if for instance it only results in type definition ({@link #getType()})
	 * and adds no further restrictions. */
	default boolean hasContent() {
		return !writeControlAsXsd("").isEmpty();
	}

	/** Returns true if the rules implies or explicitely defines a type.*/
	boolean defineType();

	/** Returns the XSD type ("xs:integer" for instance) defined by this control rule.
	 * <b>Will throw an UnsupportedOperationException</b> if the control rule does not define a type.
	 * ({@link #defineType()}) should always be called first.*/
	default String getType() {
		throw new UnsupportedOperationException();
	}

	/** Comparable is implemented to allow the used of TreeSet to store RuleForXsd.
	 * So far, the rule order is :
	 * 1)a rule that define a type comes before a rule that doesn't
	 * 2)if both rules defines a different types, the alphabetical orders of the type prevails
	 * 3)otherwise, the alphabetical order of writeControlAsXsd prevails*/
	@Override
	default int compareTo(ControlForXsd o) {
		if (defineType() && o.defineType() && !getType().equals(o.getType())) {
			return getType().compareTo(o.getType());
		} else if (defineType() && !o.defineType()) {
			return -1;
		} else if (!defineType() && o.defineType()){
			return 1;
		}
		return writeControlAsXsd("").compareTo(o.writeControlAsXsd(""));
	}

}