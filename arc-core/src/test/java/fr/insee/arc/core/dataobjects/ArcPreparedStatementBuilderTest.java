package fr.insee.arc.core.dataobjects;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;

import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.exception.ArcException;

public class ArcPreparedStatementBuilderTest {

	@Test
	public void sqlListeColumnsByList() {
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();

		Assert.assertEquals(new StringBuilder("id_famille,id_norme").toString(),
				query.sqlListeOfColumnsFromModel(ColumnEnum.ID_FAMILLE, ColumnEnum.ID_NORME).toString());

	}

	@Test
	public void sqlDDLColumnsByList() {
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		Assert.assertEquals(new StringBuilder("id_famille text,id_norme text").toString(),
				query.sqlDDLOfColumnsFromModel(ColumnEnum.ID_FAMILLE, ColumnEnum.ID_NORME).toString());
	}
	
	@Test
	public void sqlListeColumnsByView() {
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		Assert.assertEquals(new StringBuilder("test1,test2").toString(),
				query.sqlListeOfColumnsFromModel(ViewEnum.TABLE_TEST_IN_TEMPORARY).toString());
	}

	@Test
	public void sqlDDLColumnsByView() {
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		Assert.assertEquals(new StringBuilder("test1 text,test2 text").toString(),
				query.sqlDDLOfColumnsFromModel(ViewEnum.TABLE_TEST_IN_TEMPORARY).toString());
	}

	
	@Test
	public void arcPreparedStatementAppenderTest() throws ArcException {

		ArcPreparedStatementBuilder sousRequeteAvant = new ArcPreparedStatementBuilder();
		sousRequeteAvant.build("d=", sousRequeteAvant.quoteText("valeur_de_d"));

		ArcPreparedStatementBuilder sousRequeteApres = new ArcPreparedStatementBuilder();
		sousRequeteApres.build("b=", sousRequeteApres.quoteText("valeur_de_b"));

		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.SELECT, "*", SQL.FROM, ViewEnum.CONTROLE_REGLE, SQL.WHERE);
		query.build(sousRequeteAvant);
		query.build(SQL.AND, "a=", query.quoteText("valeur_de_a"), SQL.AND);
		query.build(sousRequeteApres);
		query.build(SQL.ORDER_BY, ColumnEnum.ARCHIVE_DATE);

		// check that query have the bind variables in the right order
		assertEquals(" SELECT * FROM controle_regle WHERE d=  ?   AND a=  ?   AND b=  ?   ORDER BY archive_date",
				query.getQuery().toString());
		assertEquals("valeur_de_d", query.getParameters().get(0).getValue());
		assertEquals("valeur_de_a", query.getParameters().get(1).getValue());
		assertEquals("valeur_de_b", query.getParameters().get(2).getValue());
	}

}
