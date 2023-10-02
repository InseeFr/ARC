package fr.insee.arc.core.service.p2chargement.bo;

public enum FormatRulesCsv implements IParseFormatRules {

	// order is important
	
	// java encoding charset
	// UTF-8, WIN1252, ISO88591, ...
	ENCODING("<encoding>","</encoding>", true),
	
	// postgres quote character
	// <quote>E'\2'</quote> if you don't want to use " as csv quote delimiter
	QUOTE("<quote>","</quote>", true),
	
	// list of headers in file
	// that means the file won't provide header in the first line
	HEADERS("<headers>","</headers>", true),

	// join another arc table
	JOIN_TABLE("<join-table>","</join-table>", true),
	// join type : inner join, outer join, ...
	JOIN_TYPE("<join-type>","</join-type>", true),
	// join clause : on clause
	JOIN_CLAUSE("<join-clause>","</join-clause>", true),
	// join table columns' list to retain
	JOIN_SELECT("<join-select>","</join-select>", true),
	
	// to execute query by part
	// give a expression that will be calculated and automatically splitted in balanced part by ARC
	PARTITION_EXPRESSION("<partition-expression>","</partition-expression>", true),
	
	
	// filter expression to remove some lines to the final result
	FILTER_WHERE("<where>","</where>", true),
	// index creation expression
	INDEX("<index>","</index>", true),
	
	// comment bloc
	COMMENT("/*","*/", true),
	
	// to add a column
	// column_name=column sql expression
	// column name
	COLUMN_DEFINITION(null,"=", false),
	// column definition
	COLUMN_EXPRESSION("=",null, true)
	;

	
	private FormatRulesCsv(String afterTag, String beforeTag, boolean stop) {
		this.afterTag = afterTag;
		this.beforeTag = beforeTag;
		this.stop = stop;
	}

	// condition et parsing : tag de départ. On parse l'expression après ce tag si il est trouvé
	private String afterTag;
	// condition et parsing : tag de fin. On parse l'expression avant ce tag si il est trouvé
	private String beforeTag;
	// savoir si le programme doit arreter le parsing lorsque il match ce critère ou continer pour trouver un autre critère
	private boolean stop;
	
	
	@Override
	public String getAfterTag() {
		return this.afterTag;
	}
	
	@Override
	public String getBeforeTag() {
		return this.beforeTag;
	}

	public boolean isStop() {
		return this.stop;
	}

	
}
