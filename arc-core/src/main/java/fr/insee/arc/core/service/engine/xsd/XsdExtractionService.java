package fr.insee.arc.core.service.engine.xsd;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import fr.insee.arc.core.model.JeuDeRegle;
import fr.insee.arc.core.service.engine.xsd.controls.ControlForXsd;
import fr.insee.arc.core.service.engine.xsd.groups.XsdGroup;

/** Produces an XSD-format export of a set of control rules.*/
public class XsdExtractionService {

	private static final String END_LINE = "\n";
	private static final String INDENT = "\t";

	public String get(Connection connection, JeuDeRegle jdr) throws SQLException, InvalidStateForXsdException {
		XsdRulesRetrievalService retrievalService = new XsdRulesRetrievalService();
		XsdControlDescription data = retrievalService.fetchRulesFromBase(connection, jdr);
		return generateXsdSchemaFrom(data);
	}

	/** Generates a valid XSD schema from a XsdControlDescription object.
	 * @throws InvalidStateForXsdException if the XsdControlDescription
	 * 		would result in an invalid XSD schema.*/
	public String generateXsdSchemaFrom(XsdControlDescription data)
			throws InvalidStateForXsdException {
		XsdGroup roots = data.getRoots();
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" + END_LINE);
		sb.append("<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">" + END_LINE);
		for (XsdElement root : roots) {
			sb.append(generateNodeAndChildrenAsXsdElements(data, root, INDENT));
			sb.append(END_LINE);
		}
		sb.append(END_LINE + "</xs:schema>");
		return sb.toString();
	}

	/** Generates the XSD description for the child element and all its descendants.
	 * @param data the information to use
	 * @param element the element to describe
	 * @param indentation the indentation level to start the description with
	 * @throws InvalidStateForXsdException if the XsdControlDescription
	 * 		would result in an invalid XSD schema.*/
	public String generateNodeAndChildrenAsXsdElements(XsdControlDescription data, XsdElement element, String indentation)
			throws InvalidStateForXsdException {
		StringBuilder sb = new StringBuilder();
		if (data.hasComments(element)) {
			sb.append(getComments(element, data, indentation));
		}
		sb.append(indentation + getStartOfOpeningTag(data, element));
		// Simpler case : single-line element
		if (!data.hasChildren(element) && !data.hasRules(element)) {
			return sb.append("/>").toString();
		}
		// Complex case : simpleType (has restrictions) or complexType (has children)
		sb.append(">" + END_LINE);
		sb.append(getTypeDescription(element, data, indentation + INDENT));
		return sb.append(indentation + "</xs:element>").toString();
	}

	/** Returns '&lt;xs:element name="elementName"' and minOccurs/maxOccurs for the child,
	 *  <b>without the final "&gt;"</b>.
	 * @param data */
	private String getStartOfOpeningTag(XsdControlDescription data, XsdElement element) {
		StringBuilder sb = new StringBuilder("<xs:element name=\"");
		sb.append(data.labelFor(element.getName()));
		sb.append("\"");
		String occur = element.getOccurenceAttribute();
		if (!occur.isEmpty()) {
			sb.append(" ");
			sb.append(occur);
		}
		
		return sb.toString();
	}

	/** Returns the complete XSD type description for that element (with its restrictions or its children).
	 * @param element the element to describe
	 * @param data the information to use
	 * @param indentation the indentation level to start the description with */
	private String getTypeDescription(XsdElement element, XsdControlDescription data, String indentation)
			throws InvalidStateForXsdException {
		String type = data.hasChildren(element) ? "xs:complexType" : "xs:simpleType";
		StringBuilder sb = new StringBuilder();
		sb.append(indentation + "<" + type + ">" + END_LINE);
		sb.append(getRestrictions(element, data, indentation + INDENT));
		sb.append(getChildren(element, data, indentation + INDENT));
		sb.append(indentation + "</" + type + ">" + END_LINE);
		return sb.toString();
	}

	/** Returns the XSD description of the children of an element, within an &lt;xs:sequence&gt; tag.
	 * Returns an empty String when the element has no children.
	 * @param element the element to describe
	 * @param data the information to use
	 * @param indentation the indentation level to start the description with */
	private String getChildren(XsdElement element, XsdControlDescription data, String indentation)
			throws InvalidStateForXsdException {
		XsdGroup children = data.getChildrenOf(element);
		if (children.isEmpty()) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		sb.append(indentation);
		sb.append("<" + children.getType() + ">" + END_LINE);
		String childIndentation =  indentation + INDENT;
		for (XsdElement child : children) {
			List<String> before = children.getTagBeforeCurrentElement();
			String previousBTag = "";
			for (String tagBefore : before) {
				if (!previousBTag.equals(tagBefore)) {
					sb.append(childIndentation);
					sb.append(tagBefore);
					sb.append(END_LINE);
					childIndentation += INDENT;
					previousBTag = tagBefore;
				}
			}
			sb.append(generateNodeAndChildrenAsXsdElements(data, child, childIndentation));
			List<String> after = children.getTagAfterCurrentElement();
			String previousATag = "";
			for (String tagAfter : after){
				if (!previousATag.equals(tagAfter)) {
					childIndentation = childIndentation.substring(0, childIndentation.lastIndexOf(INDENT));
					sb.append(END_LINE);
					sb.append(childIndentation);
					sb.append(tagAfter);
					previousATag = tagAfter;
				}
			}
			sb.append(END_LINE);
		}
		sb.append(indentation);
		sb.append("</" + children.getType() + ">" + END_LINE);
		return sb.toString();
	}

	/** Returns the XSD description of the controls for the value of an element, within an &lt;xs:restriction&gt; tag.
	 * Returns an empty String when the element is not associated with any controls for its value.
	 * @param element the element to describe
	 * @param data the information to use
	 * @param indentation the indentation level to start the description with */
	private String getRestrictions(XsdElement element, XsdControlDescription data, String indentation)
			throws InvalidStateForXsdException {
		Set<ControlForXsd> rules = data.getRulesFor(element);
		if (rules.isEmpty()) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		sb.append(indentation + "<xs:restriction base=\"" + findType(rules) + "\">" + END_LINE);
		for (ControlForXsd rule : rules) {
			if (rule.hasContent()) {
				sb.append(rule.writeControlAsXsd(indentation + INDENT));
				sb.append(END_LINE);
			}
		}
		sb.append(indentation + "</xs:restriction>" + END_LINE);
		return sb.toString();
	}

	/** Returns XML-format comments associated with that element.
	 * @param element the element to describe
	 * @param data the information to use
	 * @param indentation the indentation level to start the description with */
	private Object getComments(XsdElement element, XsdControlDescription data, String indentation) {
		Set<ControlForXsd> comments = data.getCommentsFor(element);
		StringBuilder sb = new StringBuilder();
		for (ControlForXsd comment : comments) {
			sb.append(comment.writeControlAsXsd(indentation));
			sb.append(END_LINE);
		}
		return sb.toString();
	}

	/** Given a set of controls rules for an element, returns its xsd type.
	 * Defaults to "xs:string".
	 * @param rules the set of rules for the element
	 * @throws InvalidStateForXsdException if more than one possible type is found*/
	public String findType(Set<ControlForXsd> rules) throws InvalidStateForXsdException {
		TreeSet<String> typesFound = new TreeSet<>();
		for (ControlForXsd rule : rules) {
			if (rule.defineType()) {
				typesFound.add(rule.getType());
			}
		}
		if (typesFound.size() > 1) {
			throw new InvalidStateForXsdException(
					"Plus d'un type est possible : le type XSD n'est pas déterminable.");
		}
		String typeFound = typesFound.pollFirst();
		return typeFound != null ? typeFound : "xs:string";
	}
}