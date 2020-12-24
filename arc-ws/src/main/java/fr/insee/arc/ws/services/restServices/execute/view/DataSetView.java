package fr.insee.arc.ws.services.restServices.execute.view;

import java.util.Map;

import fr.insee.arc.utils.structure.Record;



public class DataSetView {

	private int datasetId;
	
	private String datasetName;

	private Map<String,Record> content;

	
	
	
	public DataSetView(int datasetId, String datasetName, Map<String, Record> content) {
		super();
		this.datasetId = datasetId;
		this.datasetName = datasetName;
		this.content = content;
	}

	public int getDatasetId() {
		return datasetId;
	}

	public void setDatasetId(int datasetId) {
		this.datasetId = datasetId;
	}

	public String getDatasetName() {
		return datasetName;
	}

	public void setDatasetName(String datasetName) {
		this.datasetName = datasetName;
	}

	public Map<String, Record> getContent() {
		return content;
	}

	public void setContent(Map<String, Record> content) {
		this.content = content;
	}

	
   
   
   
}
