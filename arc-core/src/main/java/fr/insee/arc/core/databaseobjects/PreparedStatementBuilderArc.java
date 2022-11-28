package fr.insee.arc.core.databaseobjects;

import java.util.Arrays;
import java.util.Collection;

import fr.insee.arc.utils.dao.PreparedStatementBuilder;

public class PreparedStatementBuilderArc extends PreparedStatementBuilder {
	
	public StringBuilder sqlListeOfColumnsArc(Collection<ColumnEnum> listOfColumns)
	{
		return sqlListeOfColumns(ColumnEnum.listColumnEnumByName(listOfColumns));
	}
	
	
	public StringBuilder sqlListeOfColumnsArc(ColumnEnum... columns)
	{
		return sqlListeOfColumnsArc(Arrays.asList(columns));
		
	}
	

}
