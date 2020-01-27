package fr.insee.arc.web.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public  class LineObject implements Iterable<String> {
    
    private List<String> data;
    
    public LineObject() {
        super();
    }

    public static LineObject as(List<String> someData) {
        return new LineObject(someData);
    }

    private LineObject(List<String> aData) {
        super();
        this.data = aData;
    }

    /**
     * A copy constructor
     * @param lineObjectToCopy
     */
    public LineObject (LineObject lineObjectToCopy) {
	super();
        this.data = lineObjectToCopy.getData().stream()
        	  .collect(Collectors.toList());
	    }

    public List<String> getData() {
        return this.data;
    }

    public void setData(List<String> aData) {
        this.data = aData;
    }


    @Override
    public Iterator<String> iterator() {
        return this.data.iterator();
    }

    @Override
    public String toString() {
	return "LineObject [data=" + data + "]";
    }
    
    @SuppressWarnings("unchecked")
	public LineObject clone() {
        return new LineObject((List<String>) ((ArrayList<String>) (this.data)).clone());
    }
}
