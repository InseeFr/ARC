package fr.insee.arc.web.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 
 * @author Pépin Rémi
 *
 */
public class TableObject implements Iterable<LineObject>, Serializable{
    /**
     * 
     */
    private static final long serialVersionUID = 3497853934055535830L;
    private List<LineObject> lines;

    public TableObject(TableObject tableObjectToCopy) {
	this.lines = tableObjectToCopy.getLines().stream().map(LineObject::new).collect(Collectors.toList());

    }

    public TableObject() {
	super();
    }

    private TableObject(ArrayList<LineObject> someContent) {
	this.lines = someContent;
    }

    public static TableObject as(List<ArrayList<String>> someContent) {
	TableObject returned = new TableObject(new ArrayList<LineObject>());
	for (int i = 0; i < someContent.size(); i++) {
	    returned.add(LineObject.as(someContent.get(i)));
	}
	return returned;
    }

    public void add(LineObject aLineObject) {
	this.lines.add(aLineObject);
    }

    public List<LineObject> getLines() {
	return this.lines;
    }

    public void setLines(List<LineObject> lines) {
	this.lines = lines;
    }

    public int size() {
	return this.getLines().size();
    }

    public LineObject get(int index) {
	return this.lines.get(index);
    }

    @Override
    public Iterator<LineObject> iterator() {
	return this.lines.iterator();
    }

    @Override
    public String toString() {
	return "TableObject [table=" + lines + "]";
    }
    
    

}