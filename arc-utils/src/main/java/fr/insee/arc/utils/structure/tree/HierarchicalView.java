package fr.insee.arc.utils.structure.tree;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Transform a relation into a hierarchy (tree or forest), enable its manipulation through hierarchical methods. <br/>
 *
 * Relation columns are known, we can access to elements of a specific column. <br/>
 *
 * In order to have a working hierarchy the columns must be given in the hierarchy order. For example, considering the
 * gramineae taxonomy, the hierarchy must be : | Family | Genus | Species | Strain
 *
 * @author QV47IK
 */
public class HierarchicalView implements ITree<String, String> {

    private static final int LEVEL_ZERO = 0;
    public static final String ROOT_LEVEL = "root";
    private static final String DUMMY_LEVEL = "dummy";
    private String name;
    private HierarchicalView parent;
    private HierarchicalView absoluteRoot;
    private String levelName;
    private int levelNumber;

    /**
     * Column view of the relation.
     *
     * The relation is viewed as a forest with potential doubled roots.
     */
    private Map<String, List<HierarchicalView>> levelView;

    /**
     * Column order
     */
    private List<String> levelOrder;

    /**
     * Hierarchical view of the relation (name -> hierarchy)
     */
    private Map<String, HierarchicalView> mapView;


    private HierarchicalView()
    {
    	super();
    }
    
    /**
     * The constructor for a non-root element.
     *
     * @param aName
     * @param aParent
     * @param anAbsoluteRoot
     * @param aLevelName
     * @param aLevelNumber
     * @param aLevelView
     * @param aLevelOrder
     * @param aMapView
     */
    private HierarchicalView(String aName, HierarchicalView aParent, HierarchicalView anAbsoluteRoot, String aLevelName, int aLevelNumber) {
        this.name = aName;
        this.parent = aParent;
        this.absoluteRoot = anAbsoluteRoot == null ? this : anAbsoluteRoot;
        this.levelName = aLevelName;
        this.levelNumber = aLevelNumber;
        this.levelView = new HashMap<>();
        this.levelOrder = new ArrayList<>();
        this.mapView = new HashMap<>();
    }

    /**
     * The constructor for a root element.
     *
     * @param aName
     * @param aLevelName
     * @param aLevelNumber
     * @param aLevelView
     * @param aLevelOrder
     * @param aMapView
     */
    private HierarchicalView(String aName, String aLevelName, int aLevelNumber) {
        this.name = aName;
        this.parent = null;
        this.absoluteRoot = this;
        this.levelName = aLevelName;
        this.levelNumber = aLevelNumber;
        this.levelView = new HashMap<>();
        this.levelOrder = new ArrayList<>();
        this.mapView = new HashMap<>();
    }


	/**
	 * set the node identifier to the root level
	 * @param aName
	 * @return
	 */
    public static final HierarchicalView asRoot(String aName) {
        return new HierarchicalView(aName, ROOT_LEVEL, LEVEL_ZERO);
    }

	/**
	 * set node as child of a parent tree
	 * @param aName
	 * @param aParent
	 * @return
	 */
    public static final HierarchicalView asChild(String aName, HierarchicalView aParent) {
        HierarchicalView returned = new HierarchicalView(aName, aParent, aParent.absoluteRoot, aParent.levelOrder.get(LEVEL_ZERO),
                aParent.levelNumber + 1);

        // Grabbing the following levels
        for (int i = 1; i < aParent.levelOrder.size(); i++) {
            returned.levelOrder.add(aParent.levelOrder.get(i));
            returned.levelView.put(aParent.levelOrder.get(i), new ArrayList<>());
        }
        // This element is its daddy son
        if (!aParent.hasChild(aName)) {
            aParent.setChild(returned);
            // This element must be in its ancestors tree
            HierarchicalView view = returned.parent;
            while (view != null) {
                view.getLevel(returned.levelName).add(returned);
                view = view.parent;
            }
        }
        return returned;
    }

    /**
     * Load columns of a {@code aRelationalView} into a hierarchichal view, constrained by {@code aColumnList}.
     *
     * The nth of a {@code aColumnList} is taken as the name of the nth column of {@code aRelationalView}.
     *
     * @param aRootName
     * @param aColumnList
     * @param aRelationalView
     * @return
     */
    public static final HierarchicalView asRelationalToHierarchical(String aRootName, List<String> aColumnList, List<List<String>> aRelationalView) {
        HierarchicalView returned = asRoot(aRootName);
        // Adding every column
        for (int i = 0; i < aColumnList.size(); i++) {
            returned.levelOrder.add(i, aColumnList.get(i));
            returned.levelView.put(aColumnList.get(i), new ArrayList<>());
        }
        HierarchicalView local;
        for (int ligne = 0; ligne < aRelationalView.size(); ligne++) {
            local = returned;
            for (int colonne = 0; colonne < aColumnList.size(); colonne++) {
                asChild(aRelationalView.get(ligne).get(colonne), local);
                local = local.get(aRelationalView.get(ligne).get(colonne));
            }
        }
        return returned;
    }

    private void setChild(HierarchicalView child) {
        this.mapView.put(child.name, child);
    }

    /**
     * Adding a child if no other child has the same name.
     */
    @Override
    public void put(String childName) {
        if (!this.hasChild(childName)) {
            asChild(childName, this);
        }
    }

    public HierarchicalView get(HierarchicalView node) {
        return this.get(node.getLocalRoot());
    }

    @Override
    public HierarchicalView get(String key) {
        return this.mapView.get(key);
    }

    @Override
    public HierarchicalView getParent() {
        return this.parent;
    }

    @Override
    public String getLocalRoot() {
        return this.name;
    }

    public void setLocalRoot(String aName) {
        this.name = aName;
    }

    public boolean isLeaf() {
        return this.mapView.isEmpty();
    }

    public boolean hasChild(String aChild) {
        return this.mapView.containsKey(aChild);
    }

    public List<HierarchicalView> getLevel(String aName) {
        return this.levelView.get(aName);
    }

    public Collection<HierarchicalView> children() {
        return this.mapView.values();
    }

    /**
     * Add a level to every children.
     *
     * @param aName
     */
    private void addLevelDescending(String aName) {
        // The parent level has only one level, aName.
        if (this.parent != null && this.parent.levelOrder.size() == 1) {
            this.levelName = aName;
        }
        if (!this.levelView.containsKey(aName)) {
            this.levelOrder.add(aName);
            this.levelView.put(aName, new ArrayList<>());
        }
        for (HierarchicalView view : this.children()) {
            view.addLevelDescending(aName);
        }
    }

    /**
     * Adding a level ; we suppose here that we only add to a balanced tree.
     *
     * @param aName
     */
    public void addLevel(String aName) {
        this.absoluteRoot.addLevelDescending(aName);
    }

    public void print(PrintStream out) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.levelNumber; i++) {
            sb.append("  ");
        }
        if (this.isLeaf()) {
            out.println(sb + this.name + " ::= " + this.levelName);
        } else {
            out.print(sb + this.name + " ::= " + this.levelName);
            out.println(" {");
            for (HierarchicalView child : this.mapView.values()) {
                child.print(out);
            }
            out.println(sb + "}");
        }
    }

    /**
     * @param generation
     * @return the parent of the nth {@code generation}
     */
    public HierarchicalView getAncestor(int generation) {
    	if (generation < 0)
    		return null;
    	
    	if (generation == 0)
    		return this;

    	if (generation == 1)
    		return this.getParent();
    	
    	return this.getParent().getAncestor(generation - 1);
    	
    }

    /**
     * Grab the first child of this hierarchy.
     *
     * @return
     */
    public HierarchicalView getUniqueChild() {
        for (HierarchicalView returned : this.children()) {
            return returned;
        }
        return null;
    }

    public boolean hasPath(HierarchicalView... orderedChildren) {
        HierarchicalView local = this;
        for (HierarchicalView child : orderedChildren) {
            if (local.hasChild(child)) {
                local = local.get(child);
            } else {
                return false;
            }
        }
        return true;
    }

    public boolean hasChild(HierarchicalView aChild) {
        return this.hasChild(aChild.getLocalRoot());
    }

    /**
     * Return true only if the path {@code orderedChildren[0] -> ... -> orderedChildren[n-1]} exists in this hierarchy.
     *
     * @param orderedChildren
     * @return
     */
    public boolean hasPath(String... orderedChildren) {
        HierarchicalView local = this;
        for (String child : orderedChildren) {
            if (local.hasChild(child)) {
                local = local.get(child);
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * @return the levelName
     */
    public String getLevelName() {
        return this.levelName;
    }

    public void printListeLevel(PrintStream out) {
        for (int i = 0; i < this.levelOrder.size(); i++) {
            out.println(this.name + " a pour " + i + "-ième niveau -> " + this.levelOrder.get(i));
        }
    }

    public static HierarchicalView dummy() {
        return HierarchicalView.asRoot(DUMMY_LEVEL);
    }
    
}
