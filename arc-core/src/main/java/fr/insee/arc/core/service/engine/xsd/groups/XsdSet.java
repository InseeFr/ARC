package fr.insee.arc.core.service.engine.xsd.groups;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.stream.Stream;

import fr.insee.arc.core.service.engine.xsd.XsdElement;

/** Simple sequence of elements. */
public class XsdSet implements XsdGroup {
	
	private final SortedSet<XsdElement> elements;
	private final int position;

	public XsdSet(SortedSet<XsdElement> elements) {
		this.elements = elements;
		if (elements.isEmpty()) {
			this.position = -1;
		} else {
			this.position = elements.first().getPosition();
		}
	}

	@Override
	public Iterator<XsdElement> iterator() {
		return elements.iterator();
	}

	@Override
	public XsdElement first() {
		return elements.first();
	}

	@Override
	public XsdElement last() {
		return elements.last();
	}

	@Override
	public String getType() {
		return "xs:sequence";
	}

	@Override
	public boolean isEmpty() {
		return elements.isEmpty();
	}
	
	@Override
	public Stream<XsdElement> stream() {
		return elements.stream();
	}

	@Override
	public int size() {
		return elements.size();
	}

	@Override
	public int compareTo(XsdGroup o) {
		return position - o.getPosition();
	}

	@Override
	public int getPosition() {
		return position;
	}

}