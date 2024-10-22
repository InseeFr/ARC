package fr.insee.arc.web.gui.all.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Ligne du tableau */
public class LineObject implements Cloneable, Iterable<String> {

    public LineObject() {
        super();
        this.d = new ArrayList<>();
    }

    public static LineObject as(List<String> someData) {
        return new LineObject(someData);
    }

    private LineObject(List<String> aData) {
        super();
        this.d = aData;
    }

    /** Données de la ligne (par colonnes). */
    public List<String> d;

    public List<String> getD() {
        return this.d;
    }

    public void setD(List<String> aData) {
        this.d = aData;
    }

    @Override
    public LineObject clone() {
    	return new LineObject(new ArrayList<>(this.d));
    }

    @Override
    public Iterator<String> iterator() {
        return this.d.iterator();
    }
    
    /**
     * returns the value in cell at position pos
     * If position is out of range, return null
     * @param pos
     * @return
     */
    public String getCell(int pos)
    {
    	return (pos<d.size())?d.get(pos):null;
    }
}