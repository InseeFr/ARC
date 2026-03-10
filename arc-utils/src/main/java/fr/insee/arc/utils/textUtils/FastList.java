package fr.insee.arc.utils.textUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FastList<T>{

	/**
	 * 
	 */
	private Map<Integer,T> value;
	private Map<T,Integer> index;
	private int length=0;
	
		
	public FastList() {
		value=new HashMap<>();
		index=new HashMap<>();
		length=0;
	}

	public FastList(List<T> initList) {
		value=new HashMap<>();
		index=new HashMap<>();
		length=0;
		
		for (T element: initList)
		{
			add(element);
		}
		
	}

	public void add(T object)
	{
		value.put(length, object);
		index.put(object, length);
		length++;
	}

	public int size()
	{
		return length;
	}
	
	public int indexOf(Object object)
	{
		Integer pos= index.get(object);
		if (pos!=null && pos<length)
		{
			return pos;
		}
		return -1;
	}
	
	public T get(int position)
	{
		return (position<length)?value.get(position):null;
	}
	
	public List<T> subList(int start, int end)
	{
		List<T> z=new ArrayList<>();
		for (int pos=start; pos<end; pos++)
		{
			z.add(value.get(pos));
		}
		return z;
	}
	
	public List<T> asList()
	{
		return subList(0, length);
	}
	
}
