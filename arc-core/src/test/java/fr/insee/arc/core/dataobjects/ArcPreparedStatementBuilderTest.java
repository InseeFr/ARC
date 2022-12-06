package fr.insee.arc.core.dataobjects;

import org.junit.Assert;
import org.junit.Test;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;

public class ArcPreparedStatementBuilderTest {

	@Test
	public void sqlListeColumnsByList() {
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		
		Assert.assertEquals(query.sqlListeOfColumnsArc(ColumnEnum.ID_FAMILLE, ColumnEnum.ID_NORME).toString(),new StringBuilder("id_famille,id_norme").toString());
		
	}
	
	
	@Test
	public void sqlListeColumnsByView() {
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		Assert.assertEquals(query.sqlListeOfColumnsFromModel(ViewEnum.TEST).toString(),new StringBuilder("test1,test2").toString());
	}
	

}
