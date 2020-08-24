package fr.insee.arc.ws.services.rest.generic.view;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;

import fr.insee.arc.utils.structure.Record;

public class DataSetView {

	private int datasetId;
	
	private String datasetName;

	private Map<String,Record> content;

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
