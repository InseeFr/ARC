package fr.insee.arc.ws.services.rest.generic.view;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

public class ReturnView {

    @JsonFormat(pattern = "YYYY-MM-DD'T'HH:mm:ss")
    private Date receptionTime;

    @JsonFormat(pattern = "YYYY-MM-DD'T'HH:mm:ss")
    private Date returnTime;
    
    public List<DataSetView> dataSetView;

	public Date getReceptionTime() {
		return receptionTime;
	}

	public void setReceptionTime(Date receptionTime) {
		this.receptionTime = receptionTime;
	}

	public Date getReturnTime() {
		return returnTime;
	}

	public void setReturnTime(Date returnTime) {
		this.returnTime = returnTime;
	}

	public List<DataSetView> getDataSetView() {
		return dataSetView;
	}

	public void setDataSetView(List<DataSetView> dataSetView) {
		this.dataSetView = dataSetView;
	}



    
 

}
