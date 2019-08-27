package fr.insee.arc.utils.structure.tree;

// TODO add documentation
public interface ITree<K, V> {

    /**
     * Add a child to this node.
     *
     * @param childName
     */
    public void put(K childName);

    /**
     * Get the tree of wich {@code key} is the root.
     * 
     * @param key
     * @return
     */
    public ITree<K, V> get(K key);

    public ITree<K, V> getParent();

    public K getLocalRoot();
}
