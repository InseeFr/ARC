package fr.insee.arc.core.service.engine.xsd;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.OptionalInt;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import fr.insee.arc.core.service.engine.xsd.controls.ControlForXsd;
import fr.insee.arc.core.service.engine.xsd.groups.XsdChoice;
import fr.insee.arc.core.service.engine.xsd.groups.XsdGroup;
import fr.insee.arc.core.service.engine.xsd.groups.XsdSequence;
import fr.insee.arc.core.service.engine.xsd.groups.XsdSet;
import fr.insee.arc.utils.utils.Pair;

/** Description of a sets of a control rules re-organized for easier manipulations
 * during the XSD-format export a set of control rules.*/
public class XsdControlDescription {

	/** Representation of the tree through parent -> children relations.
	 * Root element(s) are children of null.*/
	private final Map<String, XsdGroup> tree;

	/** Map of the elements and their control ruleset. Elements without rules are absents (no empty set).*/
	private final Map<String, SortedSet<ControlForXsd>> rulesByElement;
	/** Map of the elements and associated comments. Elements without comments are absents (no empty set)*/
	private final Map<String, SortedSet<ControlForXsd>> commentsByElement;

	private Map<String, String> aliasMap;

	private XsdControlDescription(Map<String, XsdGroup> reverseTree,
			Map<String, SortedSet<ControlForXsd>> rulesByElement,
			Map<String, SortedSet<ControlForXsd>> commentsByElement, Map<String, String> aliasMap) {
		this.tree = reverseTree;
		this.rulesByElement = rulesByElement;
		this.commentsByElement = commentsByElement;
		this.aliasMap = aliasMap;
	}

	/** Returns the alias for that element if it has one, or just the element.*/
	public String labelFor(String element) {
		return aliasMap.getOrDefault(element, element);
	}
	
	/** Returns the direct children of the element.*/
	public XsdGroup getChildrenOf(XsdElement element) {
		return tree.getOrDefault(element.getName(), XsdSequence.fromElements(new TreeSet<>()));
	}

	/** Returns true if the element has direct children.*/
	public boolean hasChildren(XsdElement element) {
		return !getChildrenOf(element).isEmpty();
	}

	/** Returns the root(s !) of the element tree.*/
	public XsdGroup getRoots() {
		return tree.getOrDefault(null, XsdSequence.fromElements(new TreeSet<>()));
	}

	/** Returns the control rules associated with an element.*/
	public SortedSet<ControlForXsd> getRulesFor(XsdElement element) {
		return rulesByElement.getOrDefault(element.getName(), new TreeSet<>());
	}

	/** Returns true if there are rules associated with this element.*/
	public boolean hasRules(XsdElement element) {
		return !getRulesFor(element).isEmpty();
	}

	/** Returns the comments associated with an element.*/
	public SortedSet<ControlForXsd> getCommentsFor(XsdElement element) {
		return commentsByElement.getOrDefault(element.getName(), new TreeSet<>());
	}

	/** Returns true if there are comments associated with this element.*/
	public boolean hasComments(XsdElement element) {
		return !getCommentsFor(element).isEmpty();
	}

	/** Builder class for XsdControlDescription.*/
	public static class XsdControlDescriptionBuilder {

		private static final String NO_COMPLEMENTS = stringifyComplements(new String[] {});
		/** Representation of the tree as a map :
		 * the keys are the parent, the value is another map.
		 * The keys in the inner map are the group identifier and the values are the children in that group.
		 * The group identifier is the stringify arrays of the elements that are not in this group.*/
		private final Map<String, Map<String, Set<XsdElement>>> groupMap = new HashMap<>();

		private final  Map<String, SortedSet<ControlForXsd>> rulesByElement = new HashMap<>();
		private final  Map<String, SortedSet<ControlForXsd>> commentsByElement = new HashMap<>();

		/** Map of the original names (key) and their new name for the XSD export (value).*/
		private final  Map<String, String> aliasMap = new HashMap<>();

		/** Registers the parent-child relationship between the two elements.
		 * @param parentElement
		 * @param childElement
		 * @param minOccurs
		 * @param maxOccurs can be null (=unspecified)
		 * @param int position
		 * @throws InvalidStateForXsdException if the relation already exists*/
		public XsdControlDescriptionBuilder addRelation(String parentElement, String childElement,
				int minOccurs, Integer maxOccurs, int position)
				throws InvalidStateForXsdException {
			addChoiceRelation(parentElement, childElement, minOccurs, maxOccurs, new String[] {}, position);
			return this;
		}

		/** Registers the potential parent-child relationship between the two elements.
		 * @param parentElement
		 * @param childElement
		 * @param minOccurs
		 * @param maxOccurs can be null (=unspecified)
		 * @param complement columns that must be absent for this relation to exists
		 * @param int position
		 * @throws InvalidStateForXsdException if the relation already exists*/
		public XsdControlDescriptionBuilder addChoiceRelation(String parentElement, String childElement, int minOccurs, Integer maxOccurs,
				String[] complement, int position) throws InvalidStateForXsdException {
			String complementString = stringifyComplements(complement);
			XsdElement xsdElement = new XsdElement(childElement, minOccurs, maxOccurs, position);
			if (relationExists(parentElement, childElement)) {
				throw new InvalidStateForXsdException("La relation entre " + childElement + " et "
						+ parentElement + " est déjà décrite sous forme de séquence.");
			}

			groupMap.putIfAbsent(parentElement, new HashMap<>());
			groupMap.get(parentElement).putIfAbsent(complementString, new HashSet<>());
			groupMap.get(parentElement).get(complementString).add(xsdElement);

			return this;
		}

		/** Turns a string of arrays in a string. Two arrays containing the same values,
		 * even in a different order, will always result in the same string. */
		private static String stringifyComplements(String[] strings) {
			Arrays.sort(strings);
			return Arrays.toString(strings);
		}

		/** Associates a control rule with an element.*/
		public XsdControlDescriptionBuilder addRuleTo(String element, ControlForXsd rule) {
			this.rulesByElement.computeIfAbsent(element, k -> new TreeSet<>()).add(rule);
			return this;
		}

		/** Associates a comment with an element.*/
		public XsdControlDescriptionBuilder addCommentTo(String element, ControlForXsd comment) {
			this.commentsByElement.computeIfAbsent(element, k -> new TreeSet<>()).add(comment);
			return this;
		}

		/** Sets a unique alias for a column name.
		 * @throws InvalidStateForXsdException if a different alias was already set*/
		public XsdControlDescriptionBuilder defineAliasFor(String columnName, String alias) throws InvalidStateForXsdException {
			if (aliasMap.containsKey(columnName)) {
				if (aliasMap.get(columnName).equals(alias)) {
					return this;
				}
				throw new InvalidStateForXsdException("Un alias " + alias
						+ "est déjà défini pour " + columnName + ".");
			}
			aliasMap.put(columnName, alias);
			return this;
		}
		
		/** Builds the XsdControlDescription. All elements that are associated with a child, a rule
		 *  or a comment but have no defined parent(s) are defined as root elements with an
		 *  occurence of 1.
		 * @throws InvalidStateForXsdException if the description is determined to be invalid*/
		public XsdControlDescription build() throws InvalidStateForXsdException {
			// Adds also any "floating" elements (no parent defined) to roots
			Set<String> roots = getRoots();
			for (String root : roots) {
				putIfAbsentAsRoot(root, groupMap);
			}
			for (String root : rulesByElement.keySet()) {
				putIfPertinentAsRoot(groupMap, root);
			}
			for (String root : commentsByElement.keySet()) {
				putIfPertinentAsRoot(groupMap, root);
			}
			
			// Converts Set to SortedSet - if positions were not correctly defined,
			// all elements with the same position will be overriden by one of them
			Map<String, XsdGroup> builtTree = new HashMap<>();
			for (Entry<String, Map<String, Set<XsdElement>>> tempEntry : groupMap.entrySet()) {
				Map<String, Set<XsdElement>> mapOfChildren = tempEntry.getValue();
				if (mapOfChildren.containsKey(NO_COMPLEMENTS) && mapOfChildren.size() == 1) {
					Set<XsdElement> unsortedSet = mapOfChildren.get(NO_COMPLEMENTS);
					String elementName = tempEntry.getKey();
					SortedSet<XsdElement> sortedSet = sortedSetFrom(elementName, unsortedSet);
					builtTree.put(elementName, new XsdSet(sortedSet));
				} else {
					String elementName = tempEntry.getKey();
					XsdGroup result = buildComplexSequence(elementName, mapOfChildren);
					builtTree.put(elementName, result);
				}
			}

			if (!builtTree.keySet().isEmpty()) {
				checkLoop(builtTree);
			}
			
			return new XsdControlDescription(builtTree, rulesByElement, commentsByElement, aliasMap);
		}

		/** Deduces a complex organisation of elements (anything involving at least one choice) from the map.
		 * @return the sequence/choice*/
		private XsdGroup buildComplexSequence(String elementName, Map<String, Set<XsdElement>> mapOfChildren)
				throws InvalidStateForXsdException {
			// Sorts the map to allow going through it in order
			SortedMap<Integer, Pair<String, SortedSet<XsdElement>>> sortedEntries = new TreeMap<>();
			for (Entry<String, Set<XsdElement>> entry : mapOfChildren.entrySet()) {
				Set<XsdElement> unsortedSet = entry.getValue();
				SortedSet<XsdElement> sortedSet = sortedSetFrom(elementName, unsortedSet);
				sortedEntries.put(sortedSet.first().getPosition(), new Pair<>(entry.getKey(), sortedSet));
			}

			// Distinguishes between potential choices subgroups
			SortedSet<XsdGroup> allSubGroups = new TreeSet<>();
			String previousComplement = "";
			SortedSet<XsdGroup> currentChoiceSubGroup = new TreeSet<>();
			XsdGroup sequence = null;
			for (Pair<String, SortedSet<XsdElement>> t : sortedEntries.values()) {
				String complement = t.getFirst();
				SortedSet<XsdElement> set = t.getSecond();
				if (complement.equals(NO_COMPLEMENTS)) {
					sequence = new XsdSet(set);
				} else {
					if (!previousComplement.contains(set.first().getName())) {
						if (!currentChoiceSubGroup.isEmpty()) {
							allSubGroups.add(new XsdChoice(currentChoiceSubGroup));
						}
						currentChoiceSubGroup = new TreeSet<>();
					}
					currentChoiceSubGroup.add(XsdSequence.fromElements(set));
					previousComplement = complement;
				}
			}
			if (!currentChoiceSubGroup.isEmpty()) {
				allSubGroups.add(new XsdChoice(currentChoiceSubGroup));
			}

			// Splits the sequence according to its element positions if needed
			XsdGroup result;
			if (sequence != null) {
				SortedSet<XsdGroup> allSubGroupsPlusSequences = splitSequenceBetweenGroups(allSubGroups, sequence);
				result = XsdSequence.fromGroups(allSubGroupsPlusSequences);
			} else {
				result =  XsdSequence.fromGroups(allSubGroups);
			}
			return result;
		}

		/** Given a sequence of elements and one or more choice, distributes the sequence elements in subsequences
		 *  according to their expected position.*/
		private SortedSet<XsdGroup> splitSequenceBetweenGroups(SortedSet<XsdGroup> allSubGroups, XsdGroup sequence) {
			SortedSet<XsdGroup> allSubGroupsPlusSequences = new TreeSet<>();
			allSubGroupsPlusSequences.addAll(allSubGroups);
			SortedSet<XsdElement> subSequence = new TreeSet<>();
			Iterator<XsdGroup> choicesIterator = allSubGroups.iterator();
			int currentUpperBoundary = choicesIterator.next().getPosition();
			boolean reachedChoicesEnd = false;
			for (XsdElement element : sequence) {
				if (element.getPosition() > currentUpperBoundary && !reachedChoicesEnd) {
					if (!subSequence.isEmpty()) {
						allSubGroupsPlusSequences.add(new XsdSet(subSequence));
					}
					subSequence = new TreeSet<>();
					if (choicesIterator.hasNext()) {
						currentUpperBoundary = choicesIterator.next().getPosition();
					} else {
						reachedChoicesEnd = true;
					}
				}
				subSequence.add(element);
			}

			if (!subSequence.isEmpty()) {
				allSubGroupsPlusSequences.add(new XsdSet(subSequence));
			}
			return allSubGroupsPlusSequences;
		}

		private SortedSet<XsdElement> sortedSetFrom(String elementName, Set<XsdElement> unsortedSet)
				throws InvalidStateForXsdException {
			SortedSet<XsdElement> sortedSet = new TreeSet<>(unsortedSet);
			if (sortedSet.size() != unsortedSet.size()) {
				throw new InvalidStateForXsdException("Au moins un élément parmi les enfants de "
			+ elementName + " a été écrasé parce que les positions sont mal définies.");
			}
			return sortedSet;
		}

		private void checkLoop(Map<String, XsdGroup> builtTree) throws InvalidStateForXsdException {
			LinkedList<String> path = new LinkedList<>();
			for (XsdElement el : builtTree.get(null)) {
				checkLoop(el, builtTree, path);
			}
		}

		private void checkLoop(XsdElement el, Map<String, XsdGroup> builtTree, LinkedList<String> path) throws InvalidStateForXsdException {
			if (path.contains(el.getName())) {
				throw new InvalidStateForXsdException(el.getName() + " appartient à une relation cyclique : " + path.stream().collect(Collectors.joining(" > ")));
			}
			path.addLast(el.getName());
			for (XsdElement el2 : builtTree.getOrDefault(el.getName(), XsdSequence.fromElements(new TreeSet<>()))) {
				checkLoop(el2, builtTree, path);
			}
			path.removeLast();
		}

		/** Defines the element as a root element (if it is not one already).*/
		private void putIfAbsentAsRoot(String root, Map<String, Map<String, Set<XsdElement>>> buildTree) {
			buildTree.putIfAbsent(null, new HashMap<>());
			buildTree.get(null).putIfAbsent(NO_COMPLEMENTS, new HashSet<>());

			// Finds a valid position for this element = max existing position + 1
			OptionalInt max = buildTree.get(null).get(NO_COMPLEMENTS)
						.stream()
						.mapToInt(XsdElement::getPosition)
						.max();
			int position = max.orElseGet(() -> 0) + 1;

			XsdElement element = new XsdElement(root, position);
			buildTree.get(null).get(NO_COMPLEMENTS).add(element);
		}

		/** If the element is not the child of any element, defines it as a root element.*/
		private void putIfPertinentAsRoot(Map<String, Map<String, Set<XsdElement>>> tempBuildTree, String root) {
			boolean hasAParent = false;
			for (String parent : tempBuildTree.keySet()) {
				if (parent == null) {
					hasAParent = true;
					break;
				}
				for (Set<XsdElement> element : tempBuildTree.get(parent).values()) {
					if (element.stream().anyMatch((x) -> x.getName().equals(root))) {
						hasAParent = true;
					}
				}
			}
			if (!hasAParent) {
				putIfAbsentAsRoot(root, tempBuildTree);
			}
		}

		/** Returns the root(s !) of the element tree.*/
		private Set<String> getRoots() {
			Set<String> roots = new TreeSet<>();
			for (String parent : groupMap.keySet()) {
				if (parent != null && getParentsOf(parent).isEmpty()) {
					roots.add(parent);
				}
			}
			return roots;
		}

		/** Returns the direct parent(s) of the element.*/
		private Set<String> getParentsOf(String element) {
			Set<String> parents = new TreeSet<>();
			for (Entry<String, Map<String, Set<XsdElement>>> parentsAndItsChildren : groupMap.entrySet()) {
				if (containsElement(parentsAndItsChildren.getValue(), element)) {
					parents.add(parentsAndItsChildren.getKey());
				}
					
			}
			return parents;
		}

		private boolean relationExists(String parent, String child) {
			if (!groupMap.containsKey(parent)) {
				return false;
			}
			return containsElement(groupMap.get(parent), child);
		}
		
		private boolean containsElement(Map<String, Set<XsdElement>> children, String element) {
			return children.values().stream()
			.anyMatch(s -> s.stream().anyMatch(e -> e.getName().equals(element)));
		}

	}

}