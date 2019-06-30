package fr.insee.arc.utils.structure.tree;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.text.AbstractDocument.LeafElement;

/**
 * Convertir et manipuler une relation de la même manière qu'une hiérarchie (arbre ou forêt)<br/>
 * Les colonnes de la relation sont connues, et il est possible d'accéder aux élements d'une colonne choisie arbritrairement.<br/>
 * Pour que la vue hiérarchique soit fonctionnelle, les colonnes doivent être présentée dans l'ordre de la hiérarchie.<br/>
 * Ainsi, dans l'exemple d'une relation présentant la taxinomie des graminae, la relation doit être présentée sous la forme :<br/>
 * | famille | genre | espèce | variété |<br/>
 *
 * @author QV47IK
 */
public class HierarchicalView implements ITree<String, String> {

    private static final int levelZero = 0;
    public static final String rootLevel = "root";
    private static final String dummyLevel = "dummy";
    private String name;
    private HierarchicalView parent;
    private HierarchicalView absoluteRoot;
    private String levelName;
    private int levelNumber;
    /**
     * Si lazy est vrai, la vision en colonnes (coupe de l'arbre selon un éloignement depuis la racine) n'est disponible que pour la racine.
     */
    private boolean lazy = false;
    /**
     * Vue par colonne de la relation. La colonne est simplement vue comme une forêt dont les racines peuvent être doublonnées.
     */
    private Map<String, List<HierarchicalView>> levelView;
    /**
     * Ordre des colonnes
     */
    private List<String> levelOrder;
    /**
     * Vue hiérarchique de la relation : name -> hierarchy
     */
    private Map<String, HierarchicalView> mapView;

    /**
     * Le constructeur pour un élément non racine
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
    private HierarchicalView(String aName, HierarchicalView aParent, HierarchicalView anAbsoluteRoot, String aLevelName, int aLevelNumber,
            Map<String, List<HierarchicalView>> aLevelView, List<String> aLevelOrder, Map<String, HierarchicalView> aMapView) {
        this.name = aName;
        this.parent = aParent;
        this.absoluteRoot = anAbsoluteRoot == null ? this : anAbsoluteRoot;
        this.levelName = aLevelName;
        this.levelNumber = aLevelNumber;
        this.levelView = aLevelView;
        this.levelOrder = aLevelOrder;
        this.mapView = aMapView;
    }

    /**
     * Le constructeur pour une racine
     *
     * @param aName
     * @param aLevelName
     * @param aLevelNumber
     * @param aLevelView
     * @param aLevelOrder
     * @param aMapView
     */
    private HierarchicalView(String aName, String aLevelName, int aLevelNumber, Map<String, List<HierarchicalView>> aLevelView,
            List<String> aLevelOrder, Map<String, HierarchicalView> aMapView) {
        this.name = aName;
        this.parent = null;
        this.absoluteRoot = this;
        this.levelName = aLevelName;
        this.levelNumber = aLevelNumber;
        this.levelView = aLevelView;
        this.levelOrder = aLevelOrder;
        this.mapView = aMapView;
    }

    private static final class HierarchicalViewHelper {
        private boolean isLazy = false;

    }

    /**
     * FIXME pour le moment, c'est moche car pas optimisé.
     *
     * @param aHierarchicalView
     * @param aLevelOrder
     * @return la projection
     */
    public static HierarchicalView asCoupe(HierarchicalView aHierarchicalView, List<String> aLevelOrder) {
        HierarchicalView returned = asRoot(rootLevel);
        List<String> path = new ArrayList<String>();
        /**
         * Pour chaque niveau
         */
        for (int i = 0; i < aLevelOrder.size(); i++) {
            /**
             * J'ajoute ce niveau
             */
            returned.addLevel(aLevelOrder.get(i));
            /**
             * Pour chaque élément du niveau en question
             */
            for (HierarchicalView v : aHierarchicalView.getLevel(aLevelOrder.get(i))) {
                HierarchicalView current = v;
                /**
                 * Recherche du chemin vers l'élément depuis la racine : tant que le niveau de l'élément courant n'est pas celui en instance
                 * de parcours
                 */
                while (!current.getLevelName().equals(rootLevel)) {
                    /**
                     * Un niveau au-dessus
                     */
                    current = current.getParent();
                    if (aLevelOrder.contains(current.getLevelName())) {
                        path.add(0, current.getLocalRoot());
                    }
                }
                current = returned;
                /**
                 * Parcours de ce chemin jusqu'au parent de l'élément
                 */
                for (int j = 0; j < path.size(); j++) {
                    current = current.get(path.get(j));
                }
                path.clear();
                /**
                 * Puis insertion de cet élément dans la coupe
                 */
                current.put(v.getLocalRoot());
            }
        }
        return returned;
    }

    public static final HierarchicalView asRoot(String aName) {
        return new HierarchicalView(aName, rootLevel, levelZero, new HashMap<String, List<HierarchicalView>>(), new ArrayList<String>(),
                new HashMap<String, HierarchicalView>());
    }

    public static final HierarchicalView asChild(String aName, HierarchicalView aParent) {
        HierarchicalView returned = new HierarchicalView(aName, aParent, aParent.absoluteRoot, aParent.levelOrder.get(levelZero),
                aParent.levelNumber + 1, new HashMap<String, List<HierarchicalView>>(), new ArrayList<String>(),
                new HashMap<String, HierarchicalView>());
        // Récupération des niveaux qui suivent ce niveau
        for (int i = 1; i < aParent.levelOrder.size(); i++) {
            returned.levelOrder.add(aParent.levelOrder.get(i));
            returned.levelView.put(aParent.levelOrder.get(i), new ArrayList<HierarchicalView>());
        }
        // Cet élément est le fils à papa
        if (!aParent.hasChild(aName)) {
            aParent.setChild(returned);
            // Cet élément doit apparaître dans l'arborescence de tous ses
            // ancêtres
            HierarchicalView view = returned.parent;
            while (view != null) {
                view.getLevel(returned.levelName).add(returned);
                view = view.parent;
            }
        }
        return returned;
    }

    /**
     * Charge les colonnes de {@code aRelationalView} dans une vue hiérarchique, dans la limite des colonnes indiquées dans
     * {@code aListeColonne}. {@link LeafElement} i-ième élement de {@code aListeColonne} est considéré comme le nom de la i-ième colonne de
     * {@code aRelationalView}
     *
     * @param aRootName
     * @param aListeColonne
     * @param aRelationalView
     * @return
     */
    public static final HierarchicalView asRelationalToHierarchical(String aRootName, List<String> aListeColonne, List<List<String>> aRelationalView) {
        HierarchicalView returned = asRoot(aRootName);
        // Ajout de toutes les colonnes
        for (int i = 0; i < aListeColonne.size(); i++) {
            returned.levelOrder.add(i, aListeColonne.get(i));
            returned.levelView.put(aListeColonne.get(i), new ArrayList<HierarchicalView>());
        }
        HierarchicalView local;
        for (int ligne = 0; ligne < aRelationalView.size(); ligne++) {
            local = returned;
            for (int colonne = 0; colonne < aListeColonne.size(); colonne++) {
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
     * L'enfant n'est ajouté que si aucun enfant n'a le même nom
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
     * Ajoute un niveau à tous les fils et à ceci
     *
     * @param aName
     */
    private void addLevelDescending(String aName) {
        // Le niveau parent a un seul niveau : ce niveau est aName
        if (this.parent != null && this.parent.levelOrder.size() == 1) {
            this.levelName = aName;
        }
        if (!this.levelView.containsKey(aName)) {
            this.levelOrder.add(aName);
            this.levelView.put(aName, new ArrayList<HierarchicalView>());
        }
        for (HierarchicalView view : this.children()) {
            view.addLevelDescending(aName);
        }
    }

    /**
     * Ajouter un niveau : je suppose qu'un niveau n'est ajouté que sur un arbre équilibré
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
     * @return le parent de la {@code generation}-ième génération
     */
    public HierarchicalView getAncestor(int generation) {
        return generation < 0 ? null : //
                (generation == 0 ? this : //
                        (generation == 1 ? this.getParent() : //
                                (this.getParent().getAncestor(generation - 1))));
    }

    /**
     * Récupère le premier enfant de cette hiérarchie
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
     * Renvoie vrai si cette hiérarchie contient le chemin {@code orderedChildren[0] -> ... -> orderedChildren[n-1]}
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
        return HierarchicalView.asRoot(dummyLevel);
    }
}
