package fr.insee.arc.core.service.engine.xsd.groups;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import fr.insee.arc.core.service.engine.xsd.XsdElement;

/** Any combination of multiple elements in a sequence, a choice or a mix between thoses.*/
public interface XsdGroup extends Iterable<XsdElement>, Comparable<XsdGroup> {

	String getType();

	/** During an iteration, returns a list of the tags that are being opened right before the current element.
	 * Uses as reference the latest iterator created by iterator().*/
	default List<String> getTagBeforeCurrentElement() {
		return Collections.emptyList();
	}

	/** During an iteration, returns a list of the tags that are being closed right after the current element.
	 * Uses as reference the latest iterator created by iterator().*/
	default List<String> getTagAfterCurrentElement() {
		return Collections.emptyList();
	}

	/** See {@link java.util.SortedSet#first first}*/
	XsdElement first();
	/** See {@link java.util.SortedSet#last last}*/
	XsdElement last();
	/** See {@link java.util.Collection#isEmpty isEmpty}*/
	boolean isEmpty();
	/** See {@link java.util.Collection#size size}*/
	int size();
	/** See {@link java.util.Collection#stream stream}*/
	Stream<XsdElement> stream();

	/** Returns the position of the first (sorted) element in this group.*/
	int getPosition();

}