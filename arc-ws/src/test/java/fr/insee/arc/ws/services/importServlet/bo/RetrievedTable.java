package fr.insee.arc.ws.services.importServlet.bo;

public class RetrievedTable {
	
	private String tableName;
	private int numberOfRecord;
	private String firstValue;
	
	public RetrievedTable(String tableName, int numberOfRecord, String firstValue) {
		super();
		this.tableName = tableName;
		this.numberOfRecord = numberOfRecord;
		this.firstValue = firstValue;
	}

	public String getTableName() {
		return tableName;
	}

	public int getNumberOfRecord() {
		return numberOfRecord;
	}

	public String getFirstValue() {
		return firstValue;
	}

    @Override
    public boolean equals(Object obj) {
    	
    	RetrievedTable obj2= (RetrievedTable) obj;
    	
        return this.tableName.equals(obj2.getTableName())
        		&& this.numberOfRecord == obj2.getNumberOfRecord()
        		&& this.firstValue.equals(obj2.getFirstValue());
    }
	
	

}
