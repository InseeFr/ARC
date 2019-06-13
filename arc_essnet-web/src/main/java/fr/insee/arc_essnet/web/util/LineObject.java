package fr.insee.arc_essnet.web.util;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public  class LineObject implements Iterable<String> , Serializable {
    
    /**
     * 
     */
    private static final long serialVersionUID = -7713378009007338700L;
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
    
    
}
