package fr.insee.arc.utils.textUtils;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

public class FastListTest {

	@Test
	public void testFastListAddElementOneByOne() {

	FastList<String> myFastList;
	myFastList = new FastList<>();
	
	myFastList.add("s");
	myFastList.add("e");
	myFastList.add("u");
	
	List<String> myArrayList = new ArrayList<>(Arrays.asList("s","e","u"));

	// list equality
	assertEquals(myArrayList, myFastList.asList());
	
	// list index of
	assertEquals(myArrayList.indexOf("e"),myFastList.indexOf("e"));
	assertEquals(myArrayList.indexOf("unknown"),myFastList.indexOf("unknown"));
	
	}
	
	
	@Test
	public void testFastListAddElementByList() {
	FastList<String> myFastList = new FastList<>(Arrays.asList("s","e","u"));
	List<String> myArrayList = new ArrayList<>(Arrays.asList("s","e","u"));

	// list equality
	assertEquals(myArrayList, myFastList.asList());
	
	// list index of
	assertEquals(myArrayList.indexOf("e"),myFastList.indexOf("e"));
	assertEquals(myArrayList.indexOf("unknown"),myFastList.indexOf("unknown"));

	}
	
	@Test
	public void testSubList() {
	FastList<String> myFastList = new FastList<>(Arrays.asList("s","e","u"));
	List<String> myArrayList = new ArrayList<>(Arrays.asList("s","e","u"));
	
	// sub-listing
	List<String> subListOfMyArrayList=myArrayList.subList(1, 2);
	List<String> subListOfMyFastList=myFastList.subList(1, 2);
	
	assertEquals(subListOfMyArrayList,subListOfMyFastList);
	
	}
	

}
