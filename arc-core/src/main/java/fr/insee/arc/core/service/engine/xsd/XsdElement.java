package fr.insee.arc.core.service.engine.xsd;

/** Describes an element in the context of an XSD export.
 * Used in {@link XsdControlDescription}.*/
public class XsdElement implements Comparable<XsdElement> {

	private final int minOccurs;
	private final Integer maxOccurs;
	private int position;
	private final String name;

	/** Describes an element. */
	public XsdElement(String name) {
		this(name, 0);
	}
	
	/** Describes an element. */
	public XsdElement(String name, int position) {
		this(name, 1, 1, position);
	}

	public XsdElement(String name, int minOccurs, Integer maxOccurs, int position) {
		this.name = name;
		this.minOccurs = minOccurs;
		this.maxOccurs = maxOccurs;
		this.position= position;
	}

	/** Returns the occurrence attribute(s) (minOccurs/maxOccurs) for the child in this relation.
	 * Can be empty.*/
	public String getOccurenceAttribute() {
		if (maxOccurs == null) {
			return minOccurs() + " maxOccurs=\"unbounded\"";
		}
		if (maxOccurs == 1 && minOccurs == 1) {
			return "";
		}
		return minOccurs() + " maxOccurs=\"" + maxOccurs + "\"";
	}

	private String minOccurs() {
		return "minOccurs=\"" + minOccurs + "\"";
	}

	/** Returns a litteral description of the occurence attributes.
	 * Can be empty.*/
	public String getOccurenceDescription() {
		if (maxOccurs == null) {
			return "(" + minOccurs + "..n)";
		}
		if (maxOccurs == 1 && minOccurs == 1) {
			return "";
		}
		return "(" + minOccurs + ".." + maxOccurs + ")";
	}

	/** Name for the XSD export.*/
	public String getName() {
		return name;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public int getPosition() {
		return position;
	}

	@Override
	public int compareTo(XsdElement o) {
		return position - o.position;
	}

}