package fr.insee.arc.core.model;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;

public enum BatchEtat {

	
	ON("O",1), OFF("N",1), RESET("R",2), NORESET("",2);
	
	private String code;
	private Integer digit;
	
	private BatchEtat(String code, Integer digit)
	{
		this.code = code;
		this.digit = digit;
	}
	
	/**
	 * return sql string to test if code is found in column
	 * @param c
	 * @return
	 */
	public ArcPreparedStatementBuilder isCodeInOperation()
	{
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build("substr(",ColumnEnum.OPERATION, ",", this.digit, ",1)", "=", query.quoteText(this.code));
		return query;
	}
	
	/**
	 * return sql string to test if code is found in column
	 * @param c
	 * @return
	 */
	public ArcPreparedStatementBuilder updateCodeInOperation()
	{
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(ColumnEnum.OPERATION, "=", "overlay(", ColumnEnum.OPERATION ," placing ", query.quoteText(this.code) ," from ", this.digit ," for 1)");
		return query;
	}

	public String getCode() {
		return code;
	}

	public int getDigit() {
		return digit;
	}
	
	
	
	
}
