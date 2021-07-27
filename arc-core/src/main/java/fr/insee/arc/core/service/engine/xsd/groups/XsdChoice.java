package fr.insee.arc.core.service.engine.xsd.groups;

import java.util.Iterator;
import java.util.SortedSet;

import fr.insee.arc.core.service.engine.xsd.XsdElement;

public class XsdChoice extends AbstractXsdGroup {

	private static final String OR = "OR";
	private final XsdGroup[] sequences;
	private final int position;
	private XsdIterator iterator;
	
	public static final String xsdChoiceIdentifier="xs:choice";

	@SafeVarargs
	public XsdChoice(XsdGroup... elements) {
		this.sequences = elements;
		if (isEmpty()) {
			this.position = 0;
		} else {
			this.position = new XsdIterator(getSequences(), OR).next().getPosition();
		}
		this.iterator = new XsdIterator(getSequences(), OR);
	}

	public XsdChoice(SortedSet<XsdGroup> elements) {
		this(elements.stream().toArray(XsdGroup[]::new));
	}

	@Override
	public Iterator<XsdElement> iterator() {
		this.iterator = new XsdIterator(getSequences(), OR);
		return getCurrentIterator();
	}

	@Override
	protected XsdIterator getCurrentIterator() {
		return iterator;
	}

	@Override
	protected XsdGroup[] getSequences() {
		return sequences;
	}

	@Override
	public String getType() {
		return xsdChoiceIdentifier;
	}
	
	@Override
	public int getPosition() {
		return position;
	}
}