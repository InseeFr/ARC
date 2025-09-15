package fr.insee.arc.utils.dataobjects;

import java.util.stream.Collectors;

import fr.insee.arc.utils.structure.GenericBean;

public class ColumnAttributes {
	
	public ColumnAttributes(String cols, String colsWithType) {
		super();
		this.cols = cols;
		this.colsWithType = colsWithType;
	}
	
	public ColumnAttributes(GenericBean gb) {
		super();
		this.cols = gb.getHeaders().stream().collect(Collectors.joining(","));
		this.colsWithType = gb.getHeaders().stream().map(h -> h + " " + gb.getTypes().get(gb.getHeaders().indexOf(h))).collect(Collectors.joining(","));
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