package fr.insee.arc.core.databaseobjects;

import org.junit.Assert;
import org.junit.Test;

public class PreparedStatementBuilderArcTest {

	@Test
	public void sqlListeColumns() {
		PreparedStatementBuilderArc query = new PreparedStatementBuilderArc();
		
		System.out.println(query.sqlListeOfColumnsArc(ColumnEnum.ID_FAMILLE, ColumnEnum.ID_NORME));
		
		Assert.assertEquals(query.sqlListeOfColumnsArc(ColumnEnum.ID_FAMILLE, ColumnEnum.ID_NORME).toString(),new StringBuilder("id_famille,id_norme").toString());
		
		
	}

}
