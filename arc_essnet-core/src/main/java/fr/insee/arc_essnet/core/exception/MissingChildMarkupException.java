package fr.insee.arc_essnet.core.exception;

public class MissingChildMarkupException extends Exception {
    
    /**
     * 
     */
    private static final long serialVersionUID = 6301161908387897576L;

    public MissingChildMarkupException(String markup) {
	super(String.format("the child markup %s does no exist in the format file", markup));
    }

}
