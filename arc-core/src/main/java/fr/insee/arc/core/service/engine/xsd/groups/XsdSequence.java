package fr.insee.arc.core.service.engine.xsd.groups;

import java.util.Iterator;
import java.util.SortedSet;

import fr.insee.arc.core.service.engine.xsd.XsdElement;

/** Complex sequence of elements. */
public class XsdSequence extends AbstractXsdGroup {
	
	private static final String AND = "AND";
	private final XsdGroup[] sequences;
	private final int position;
	private XsdIterator iterator;

	public static XsdSequence fromElements(SortedSet<XsdElement> sortedSet) {
		return new XsdSequence(new XsdSet(sortedSet));
	}

	public static XsdSequence fromGroups(SortedSet<XsdGroup> sortedSet) {
		return new XsdSequence(sortedSet.stream().toArray(XsdGroup[]::new));
	}
	
	@SafeVarargs
	public XsdSequence(XsdGroup... elements) {
		this.sequences = elements;
		if (isEmpty()) {
			this.position = 0;
		} else {
			this.position = new XsdIterator(getSequences(), AND).next().getPosition();
		}
		this.iterator = new XsdIterator(getSequences(), AND);
	}

	@Override
	public Iterator<XsdElement> iterator() {
		this.iterator = new XsdIterator(getSequences(), AND);
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
		return "xs:sequence";
	}
	
	@Override
	public int getPosition() {
		return position;
	}

}