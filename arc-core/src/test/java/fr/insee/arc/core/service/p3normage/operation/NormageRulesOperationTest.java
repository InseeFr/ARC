package fr.insee.arc.core.service.p3normage.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;

import org.junit.jupiter.api.Test;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.service.global.bo.FileIdCard;
import fr.insee.arc.core.service.p3normage.bo.RegleNormage;
import fr.insee.arc.core.service.p3normage.bo.TypeNormage;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.query.TestDatabase;

public class NormageRulesOperationTest {

	public static UtilitaireDao u = UtilitaireDao.get(0);

	public static Connection c = new TestDatabase().testConnection;

	@Test
	public void fillNormageRulesTest() throws ArcException {
		// insert test data
		u.executeRequest(c, "DROP SCHEMA IF EXISTS arc_bas1 CASCADE;");
		u.executeRequest(c, "CREATE SCHEMA IF NOT EXISTS arc_bas1;");

		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build("CREATE TABLE IF NOT EXISTS arc_bas1.normage_regle (" //
				, "id_norme text NULL," //
				, "periodicite text NULL," //
				, "validite_inf date NULL," //
				, "validite_sup date NULL," //
				, "\"version\" text NULL," //
				, "id_classe text NULL," //
				, "rubrique text NULL," //
				, "rubrique_nmcl text NULL," //
				, "id_regle int4 NULL," //
				, "todo text NULL," //
				, "commentaire text NULL" //
				, ")" //
				, "WITH (autovacuum_enabled=false)" //
				, SQL.END_QUERY
		);

		query.build(SQL.INSERT_INTO, "arc_bas1.normage_regle", SQL.VALUES,
				"('TEST', 'A', '2020-01-01', '2025-01-01', 'v01', 'relation', 'col1', 'col2', 1, '', '')",
				SQL.END_QUERY);
		// hors calendrier
		query.build(SQL.INSERT_INTO, "arc_bas1.normage_regle", SQL.VALUES,
				"('TEST', 'A', '1998-01-01', '2020-01-01', 'v01', 'relation', 'col_invalid', 'col_invalid', 1, '', '')",
				SQL.END_QUERY);
		// norme différente
		query.build(SQL.INSERT_INTO, "arc_bas1.normage_regle", SQL.VALUES,
				"('FAKE_NORM', 'A', '2020-01-01', '2025-01-01', 'v01', 'relation', 'col_invalid', 'col_invalid', 1, '', '')",
				SQL.END_QUERY);
		// périodicité différente
		query.build(SQL.INSERT_INTO, "arc_bas1.normage_regle", SQL.VALUES,
				"('TEST', 'M', '2020-01-01', '2025-01-01', 'v01', 'relation', 'col_invalid', 'col_invalid', 1, '', '')",
				SQL.END_QUERY);

		u.executeRequest(c, query);
		// prepare fileidcard object
		FileIdCard fileIdCard = new FileIdCard("TEST");
		fileIdCard.setIdNorme("TEST");
		fileIdCard.setValidite("2022-01-01");
		fileIdCard.setPeriodicite("A");
		// method to test
		NormageRulesOperation.fillNormageRules(c, "arc_bas1", fileIdCard);
		// assertions
		RegleNormage regleNormage = fileIdCard.getIdCardNormage().getReglesNormage().get(0);
		assertEquals(TypeNormage.RELATION, regleNormage.getTypeNormage());
		assertEquals("col1", regleNormage.getRubrique());
		assertEquals("col2", regleNormage.getRubriqueNmcl());
	}

}
