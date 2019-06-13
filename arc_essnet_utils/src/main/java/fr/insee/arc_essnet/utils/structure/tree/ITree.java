package fr.insee.arc_essnet.utils.structure.tree;

public interface ITree<K, V> {

    /**
     * Ajoute un enfant à ce noeud
     * 
     * @param childName
     */
    public void put(K childName);

    /**
     * Récupère l'arbre dont {@code key} est la racine
     * 
     * @param key
     * @return
     */
    public ITree<K, V> get(K key);

    public ITree<K, V> getParent();

    public K getLocalRoot();
}
