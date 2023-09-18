package fr.insee.arc.web.gui.all.util;

import java.util.ArrayList;
import java.util.Iterator;

/** Ligne du tableau */
public class LineObject implements Cloneable, Iterable<String> {

    public LineObject() {
        super();
        this.d = new ArrayList<>();
    }

    public static LineObject as(ArrayList<String> someData) {
        return new LineObject(someData);
    }

    private LineObject(ArrayList<String> aData) {
        super();
        this.d = aData;
    }

    /** Données de la ligne (par colonnes). */
    public ArrayList<String> d;

    public ArrayList<String> getD() {
        return this.d;
    }

    public void setD(ArrayList<String> aData) {
        this.d = aData;
    }

    @Override
	@SuppressWarnings("unchecked")
    public LineObject clone() {
        return new LineObject((ArrayList<String>) this.d.clone());
    }

    @Override
    public Iterator<String> iterator() {
        return this.d.iterator();
    }
}