package fr.insee.arc_essnet.web;

import java.util.ArrayList;
import java.util.List;

import fr.insee.arc_essnet.web.util.LineObject;

public class Main {

    public static void main(String[] args) {
	List<String> test= new ArrayList<>();
	
	test.add("a");
	
	LineObject lio =LineObject.as(test);
	
	System.out.println(lio);
	
	test.add("b");
	System.out.println(lio);
	
	LineObject lio2 = new LineObject(lio);
	
	System.out.println(lio);
	System.out.println(lio2);
	test.add("b");
	
	System.out.println(lio);
	System.out.println(lio2);


    }

}
