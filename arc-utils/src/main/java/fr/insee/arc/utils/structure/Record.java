package fr.insee.arc.utils.structure;

import java.util.List;

public class Record {
	
	public String dataType;
	
	public List<String> data;

	public Record(String dataType, List<String> data) {
		super();
		this.dataType = dataType;
		this.data = data;
	}

	public Record() {
		super();
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public List<String> getData() {
		return data;
	}

	public void setData(List<String> data) {
		this.data = data;
	}
	
	
	
}