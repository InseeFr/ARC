package fr.insee.arc_essnet.utils.structure;

import java.util.ArrayList;
import java.util.HashMap;

public class GenericBean {

	public ArrayList<String> headers;
	public ArrayList<String> types;
	public ArrayList<ArrayList<String>> content;

	/**
	 * @param headers
	 * @param types
	 * @param content
	 */
	public GenericBean(ArrayList<String> headers, ArrayList<String> types,
			ArrayList<ArrayList<String>> content) {
		this.headers = headers;
		this.types = types;
		this.content = content;
	}

	/**
	 * @param requestResult
	 */
	public GenericBean(ArrayList<ArrayList<String>> requestResult) {
		// refactor de la méthode; faut pas utiliser la commande "remove" sinon on détruit le requestResult initial
		this.headers = requestResult.get(0);
		this.types = requestResult.get(1);
		this.content =new ArrayList<ArrayList<String>>();
		for (int i=2;i<requestResult.size();i++)
		{
			this.content.add(requestResult.get(i));
			
		}
	}

	/**
	 * @return les index des entetes sous forme de hashmap(entete,type)
	 */
	public HashMap<String, Integer> mapIndex() {
		HashMap<String, Integer> r = new HashMap<String, Integer>();
		for (int i = 0; i < headers.size(); i++) {
			r.put(this.headers.get(i), i);
		}
		return r;
	}

	/**
	 * @return le type des entetes sous forme de hashmap(entete,type)
	 */
	public HashMap<String, String> mapTypes() {
		HashMap<String, String> r = new HashMap<String, String>();

		for (int i = 0; i < headers.size(); i++) {
			r.put(this.headers.get(i), this.types.get(i));
		}
		return r;
	}

	/**
	 * @return le contenu (la liste) relatif à l'entete sous forme de
	 *         hashmap(entete,contenu)
	 */
	public HashMap<String, ArrayList<String>> mapContent() {
		if (this.content == null || this.content.size() == 0) {
			return new HashMap<String, ArrayList<String>>();
		}
		
		HashMap<String, ArrayList<String>> r = new HashMap<String, ArrayList<String>>();
		for (int i = 0; i < this.content.size(); i++) {

			for (int j = 0; j < this.content.get(i).size(); j++) {
				if (r.get(this.headers.get(j)) == null) {
					r.put(this.headers.get(j), new ArrayList<String>());
				}
				r.get(this.headers.get(j)).add(this.content.get(i).get(j));
			}
		}
		return r;
	}

    public int size() {
        return this.content.size();
    }
    
    public ArrayList<String> getHeadersUpperCase()
    {
    	ArrayList<String> l= new ArrayList<String>();
    	if (this.headers!=null)
    	{
	    	for (String s:this.headers)
	    	{
	    		l.add(s.toUpperCase());
	    	}
    	}
    	return l;
    }
}
