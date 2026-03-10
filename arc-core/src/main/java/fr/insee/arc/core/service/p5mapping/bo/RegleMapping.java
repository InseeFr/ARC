package fr.insee.arc.core.service.p5mapping.bo;

public class RegleMapping {
	
	private String variableSortie;
	private String exprRegleCol; // aka expression SQL
	
	public RegleMapping(String variableSortie, String exprRegleCol) {
		super();
		this.variableSortie = variableSortie;
		this.exprRegleCol = exprRegleCol;
	}
	
	public String getVariableSortie() {
		return variableSortie;
	}
	
	public void setVariableSortie(String variableSortie) {
		this.variableSortie = variableSortie;
	}
	
	public String getExprRegleCol() {
		return exprRegleCol;
	}
	
	public void setExprRegleCol(String exprRegleCol) {
		this.exprRegleCol = exprRegleCol;
	}
	
}
