package fr.insee.arc.utils.dataobjects;

public class ColumnAttributes {
	
	public ColumnAttributes(String cols, String colsWithType) {
		super();
		this.cols = cols;
		this.colsWithType = colsWithType;
	}

	private final String cols;
	
	private final String colsWithType;

	public String getCols() {
		return cols;
	}

	public String getColsWithType() {
		return colsWithType;
	}
	
}