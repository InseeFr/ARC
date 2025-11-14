package fr.insee.arc.web.gui.all.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Tableau du contenu de la requete (ligne, colonne) */
public class TableObject implements Iterable<LineObject> {

	/** Lignes du tableau.*/
    public List<LineObject> t = new ArrayList<>();

    public static TableObject as(List<List<String>> someContent) {
        TableObject returned = new TableObject(new ArrayList<>());
        for (int i = 0; i < someContent.size(); i++) {
            returned.add(LineObject.as(someContent.get(i)));
        }
        return returned;
    }

    public void add(LineObject aLineObject) {
        this.t.add(aLineObject);
    }

    public TableObject() {
        super();
    }

    private TableObject(List<LineObject> someContent) {
        this.t = someContent;
    }

    public List<LineObject> getT() {
        return this.t;
    }

    public void setT(List<LineObject> t) {
        this.t = t;
    }

    public int size() {
        return this.getT().size();
    }

    public LineObject get(int index) {
        return this.t.get(index);
    }

	public TableObject duplicate() {
        List<LineObject> clonedContent = new ArrayList<>();
        for (int i = 0; i < this.t.size(); i++) {
            clonedContent.add(this.t.get(i).duplicate());
        }
        return new TableObject(clonedContent);
    }

    @Override
    public Iterator<LineObject> iterator() {
        return this.t.iterator();
    }

}