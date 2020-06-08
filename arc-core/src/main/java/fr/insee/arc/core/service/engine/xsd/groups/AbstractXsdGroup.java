package fr.insee.arc.core.service.engine.xsd.groups;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import fr.insee.arc.core.service.engine.xsd.XsdElement;

public abstract class AbstractXsdGroup implements XsdGroup {

	protected abstract XsdGroup[] getSequences();
	protected abstract XsdIterator getCurrentIterator();
	
	@Override
	public Iterator<XsdElement> iterator() {
		return null;
	}

	@Override
	public XsdElement first() {
		if (getSequences().length == 0) {
			throw new NoSuchElementException();
		}
		return getSequences()[0].first();
	}

	@Override
	public XsdElement last() {
		if (getSequences().length == 0) {
			throw new NoSuchElementException();
		}
		return getSequences()[getSequences().length - 1].last();
	}

	@Override
	public boolean isEmpty() {
		if (getSequences().length == 0) {
			return true;
		}
		return Arrays.stream(getSequences()).allMatch(XsdGroup::isEmpty);
	}

	@Override
	public Stream<XsdElement> stream() {
		return Arrays.stream(getSequences()).flatMap(XsdGroup::stream);
	}

	@Override
	public int size() {
		return Arrays.stream(getSequences()).mapToInt(XsdGroup::size).sum();
	}

	@Override
	public List<String> getTagBeforeCurrentElement() {
		return getCurrentIterator().getTagBefore();
	}

	@Override
	public List<String> getTagAfterCurrentElement() {
		return getCurrentIterator().getTagAfter();
	}
	
	@Override
	public int compareTo(XsdGroup o) {
		return getPosition() - o.getPosition();
	}

}