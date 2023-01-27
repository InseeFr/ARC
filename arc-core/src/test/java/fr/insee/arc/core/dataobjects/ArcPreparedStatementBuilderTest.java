package fr.insee.arc.core.dataobjects;

import org.junit.Assert;
import org.junit.Test;

public class ArcPreparedStatementBuilderTest {

	@Test
	public void sqlListeColumnsByList() {
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();

		Assert.assertEquals(new StringBuilder("id_famille,id_norme").toString(),
				query.sqlListeOfColumnsFromModel(ColumnEnum.ID_FAMILLE, ColumnEnum.ID_NORME).toString());

	}

	@Test
	public void sqlListeColumnsByView() {
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		Assert.assertEquals(new StringBuilder("test1,test2").toString(),
				query.sqlListeOfColumnsFromModel(ViewEnum.TEST).toString());
	}

}
