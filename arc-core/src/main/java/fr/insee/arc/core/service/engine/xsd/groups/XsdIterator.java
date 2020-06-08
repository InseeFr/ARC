package fr.insee.arc.core.service.engine.xsd.groups;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import fr.insee.arc.core.service.engine.xsd.XsdElement;

public class XsdIterator implements Iterator<XsdElement> {
	
	private final 	XsdGroup[] sequences;
	private Iterator<XsdElement> currentIterator = Collections.emptyIterator();
	private int currentSequence;
	private XsdElement currentElement;
	/** String description of the relationship between two groups (AND/OR).*/
	private String pivot;

	public XsdIterator(XsdGroup[] sequences, String pivot) {
		this.sequences = sequences;
		this.pivot = pivot;
		currentSequence = -1;
		if (this.sequences.length > 0) {
			advanceSequenceIfNeeded();
		}
	}

	@Override
	public boolean hasNext() {
		advanceSequenceIfNeeded();
		return currentIterator.hasNext();
	}

	@Override
	public XsdElement next() {
		advanceSequenceIfNeeded();
		currentElement = currentIterator.next();
		return currentElement;
	}

	private void advanceSequenceIfNeeded() {
		while (!currentIterator.hasNext() &&  currentSequence < (sequences.length - 1)) {
			currentSequence++;
			currentIterator = sequences[currentSequence].iterator();
		}
	}
	
	List<String> getTagBefore() {
		List<String> tags = new ArrayList<>();
		if (currentElement.getName().equals(sequences[currentSequence].first().getName())
				&& sequences[currentSequence].size() != 1) {
			StringBuilder sb = new StringBuilder();
			sb.append("<");
			sb.append(sequences[currentSequence].getType());
			sb.append(">");
			tags.add(sb.toString());
		}
		if (currentIterator instanceof XsdIterator) {
			List<String> tagBefore = ((XsdIterator)currentIterator).getTagBefore();
			tags.addAll(tagBefore);
		}
		return tags;
	}

	List<String> getTagAfter() {
		List<String> tags = new ArrayList<>();
		if (currentIterator instanceof XsdIterator) {
			List<String> tagBefore = ((XsdIterator)currentIterator).getTagAfter();
			tags.addAll(tags.size(), tagBefore);
		}
		if (currentElement.getName().equals(sequences[currentSequence].last().getName())
				&& sequences[currentSequence].size() != 1) {
			String tag = "</" + sequences[currentSequence].getType() + ">";
			tags.add(tag);
		}
		return tags;
	}

	/** If betweeen groups, returns a description of the relationship between the two groups (AND/OR).
	 * Returns empty otherwise.*/
	public String getCurrentPivot() {
		if (currentIterator instanceof XsdIterator) {
			String innerChoice = ((XsdIterator)currentIterator).getCurrentPivot();
			if (!innerChoice.isEmpty()) {
				return innerChoice;
			}
		}
		if (currentSequence > 0 && currentElement.getName().equals(sequences[currentSequence - 1].last().getName())){
			return pivot;
		}
		return "";
	}
	

}