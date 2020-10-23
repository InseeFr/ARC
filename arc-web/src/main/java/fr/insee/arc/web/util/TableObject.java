package fr.insee.arc.web.util;

import java.util.ArrayList;
import java.util.Iterator;

/** Tableau du contenu de la requete (ligne, colonne) */
public class TableObject implements Cloneable, Iterable<LineObject> {

	/** Lignes du tableau.*/
    public ArrayList<LineObject> t = new ArrayList<>();

    public static TableObject as(ArrayList<ArrayList<String>> someContent) {
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

    private TableObject(ArrayList<LineObject> someContent) {
        this.t = someContent;
    }

    public ArrayList<LineObject> getT() {
        return this.t;
    }

    public void setT(ArrayList<LineObject> t) {
        this.t = t;
    }

    public int size() {
        return this.getT().size();
    }

    public LineObject get(int index) {
        return this.t.get(index);
    }

    @Override
	public TableObject clone() {
        ArrayList<LineObject> clonedContent = new ArrayList<>();
        for (int i = 0; i < this.t.size(); i++) {
            clonedContent.add(this.t.get(i).clone());
        }
        return new TableObject(clonedContent);
    }

    @Override
    public Iterator<LineObject> iterator() {
        return this.t.iterator();
    }

}