package fr.insee.arc.utils.structure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;

import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;

/**
 * Result of a select query given by UtilitaireDao.execute request is a list of
 * list as [ [col_name#1, col_name#2, ..., col_name#p] ,[col_type#1, col_type#2,
 * ..., col_type#p] ,[col_data#1-1, col_data#1-2, ..., col_data#1-p]
 * ,[col_data#2-1, col_data#2-2, ..., col_data#2-p] , ... ,[col_data#n-1,
 * col_data#n-2, ..., col_data#n-p] ] This class split this table view into
 * headers, content, data object list and also provide function to return the
 * list of data or the type mapped by the column name
 * 
 * @author FY2QEQ
 *
 */
public class GenericBean {

	private List<String> headers;
	private List<String> types;
	public List<List<String>> content;

	/**
	 * @param headers
	 * @param types
	 * @param content
	 */
	public GenericBean(List<String> headers, List<String> types, List<List<String>> content) {
		this.headers = headers;
		this.types = types;
		this.content = content;
	}
	
	/**
	 * @param headers
	 * @param types
	 * @param content
	 */
	public GenericBean(String headers, String types, List<String> contentList) {
		this.headers = new ArrayList<>(Arrays.asList(headers));
		this.types = new ArrayList<>(Arrays.asList(types));
		this.content = reworkListAsContent(contentList);
	}
	
	
	protected static List<List<String>> reworkListAsContent(List<String> contentList) {
		
		List<List<String>> contentReworked =new ArrayList<>();
		for (int i=0; i<contentList.size(); i++)
		{
			contentReworked.add(new ArrayList<>(Arrays.asList(contentList.get(i))));
		}	
		return contentReworked;
	}
	
	/**
	 * @param requestResult
	 */
	public GenericBean(List<List<String>> requestResult) {
		// refactor de la méthode; faut pas utiliser la commande "remove" sinon on
		// détruit le requestResult initial
		this.headers = new ArrayList<>();
		this.headers.addAll(requestResult.get(0));

		this.types = new ArrayList<>();
		this.types.addAll(requestResult.get(1));

		this.content = new ArrayList<>();
		for (int i = 2; i < requestResult.size(); i++) {
			this.content.add(requestResult.get(i));

		}
	}

	/**
	 * Transform the headers ArrayList to a HashMap containing the index of headers
	 * (key: header, value: index)
	 *
	 * @return the resulting HashMap
	 */
	public Map<String, Integer> mapIndex() {
		Map<String, Integer> r = new HashMap<>();
		for (int i = 0; i < headers.size(); i++) {
			r.put(this.headers.get(i), i);
		}
		return r;
	}

	/**
	 * Produce a HashMap containing headers and their relative types (header, type)
	 *
	 * @return the HashMap
	 */
	public Map<String, String> mapTypes() {
		Map<String, String> r = new HashMap<>();

		for (int i = 0; i < headers.size(); i++) {
			r.put(this.headers.get(i), this.types.get(i));
		}
		return r;
	}

	/**
	 * Produce a HashMap containing headers and their relative content (header,
	 * content) return empty map if no records
	 *
	 * @return the HashMap
	 */
	public Map<String, List<String>> mapContent() {
		if (this.content == null || this.content.isEmpty()) {
			return new HashMap<>();
		}

		Map<String, List<String>> r = new HashMap<>();
		
		// initialisation
		int jMax = this.content.get(0).size();
		for (int j = 0; j < jMax; j++) {
			r.put(this.headers.get(j), new ArrayList<>());
		}
		
		// alimentation
		for (int i = 0; i < this.content.size(); i++) {
			for (int j = 0; j < jMax; j++) {
				r.get(this.headers.get(j)).add(this.content.get(i).get(j));
			}
		}
		return r;
	}

	/**
	 * Produce a HashMap containing headers and their relative content (header,
	 * content) If content is empty, initialize the map with the column entries and
	 * empty list
	 * 
	 * @return
	 */
	public Map<String, List<String>> mapContent(boolean initializeMapWithColumns) {
		Map<String, List<String>> m = this.mapContent();

		if (!initializeMapWithColumns) {
			return m;
		}

		// if headers found but no record returned from query, add column entry with
		// empty list
		if (!this.headers.isEmpty() && m.get(this.headers.get(0)) == null) {
			for (int i = 0; i < this.headers.size(); i++) {
				m.put(this.headers.get(i), new ArrayList<>());
			}
		}
		return m;
	}
	
	public Map<String, Record> mapRecord() {
		if (this.content == null || this.content.isEmpty()) {
			return new HashMap<>();
		}

		Map<String, Record> r = new HashMap<>();

		for (int i = 0; i < this.content.size(); i++) {

			for (int j = 0; j < this.content.get(i).size(); j++) {
				if (r.get(this.headers.get(j)) == null) {
					r.put(this.headers.get(j), new Record(this.types.get(j), new ArrayList<String>()));
				}
				r.get(this.headers.get(j)).data.add(this.content.get(i).get(j));
			}
		}
		return r;
	}

	public boolean isEmpty() {
		return size() == 0;
	}

	public int size() {
		return this.content.size();
	}

	public Map<String, String> keyValue() throws ArcException {
		Map<String, String> r = new HashMap<>();

		if (!this.content.isEmpty() && this.content.get(0).size() != 2) {
			throw new ArcException(ArcExceptionMessage.GENERIC_BEAN_KEY_VALUE_FAILED);
		}

		for (List<String> line : this.content) {
			if (r.get(line.get(0)) != null) {
				throw new ArcException(ArcExceptionMessage.GENERIC_BEAN_DUPLICATE_KEY, line.get(0));
			}
			r.put(line.get(0), line.get(1));
		}
		return r;
	}
	
	/**
	 * return column values as list
	 * @param columnName
	 * @return
	 */
	public List<String> getColumnValues(String columnName)
	{
		return ObjectUtils.firstNonNull(this.mapContent().get(columnName), new ArrayList<String>()); 
	}
	

	public List<String> getHeaders() {
		return headers;
	}

	public void setHeaders(List<String> headers) {
		this.headers = headers;
	}

	public List<String> getTypes() {
		return types;
	}

	public void setTypes(List<String> types) {
		this.types = types;
	}

	public List<List<String>> getContent() {
		return content;
	}

	public void setContent(List<List<String>> content) {
		this.content = content;
	}

}
