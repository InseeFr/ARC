package fr.insee.arc.core.service.p5mapping.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;

import org.junit.jupiter.api.Test;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.service.global.bo.FileIdCard;
import fr.insee.arc.core.service.p5mapping.bo.RegleMapping;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.query.TestDatabase;

public class MappingRulesOperationTest {

	public static UtilitaireDao u = UtilitaireDao.get(0);

	public static Connection c = new TestDatabase().testConnection;

	@Test
	public void fillMappingRulesTest() throws ArcException {
		// insert test data
		u.executeRequest(c, "DROP SCHEMA IF EXISTS arc_bas1 CASCADE;");
		u.executeRequest(c, "CREATE SCHEMA IF NOT EXISTS arc_bas1;");
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build("CREATE TABLE arc_bas1.mapping_regle (", "id_regle int8 NULL," //
				, "id_norme text NULL," //
				, "validite_inf date NULL," //
				, "validite_sup date NULL," //
				, "version text NULL," //
				, "periodicite text NULL," //
				, "variable_sortie varchar(63) NULL," //
				, "expr_regle_col text NULL," //
				, "commentaire text NULL" //
				, ")" //
				, "WITH (autovacuum_enabled=false)", SQL.END_QUERY);

		query.build(SQL.INSERT_INTO, "arc_bas1.mapping_regle", SQL.VALUES,
				"(1, 'TEST', '2020-01-01', '2025-01-01', 'v01', 'A', 'col_sortie', 'col1', '')", SQL.END_QUERY);
		// hors calendrier
		query.build(SQL.INSERT_INTO, "arc_bas1.mapping_regle", SQL.VALUES,
				"(1, 'TEST', '1998-01-01', '2020-01-01', 'v01', 'A', 'col_invalid', 'col_invalid', '')", SQL.END_QUERY);
		// norme différente
		query.build(SQL.INSERT_INTO, "arc_bas1.mapping_regle", SQL.VALUES,
				"(1, 'FAKE_NORM', '2020-01-01', '2025-01-01', 'v01', 'A', 'col_invalid', 'col_invalid', '')",
				SQL.END_QUERY);
		// périodicité différente
		query.build(SQL.INSERT_INTO, "arc_bas1.mapping_regle", SQL.VALUES,
				"(1, 'TEST', '2020-01-01', '2025-01-01', 'v01', 'M', 'col_invalid', 'col_invalid', '')", SQL.END_QUERY);
		u.executeRequest(c, query);

		// prepare fileidcard object
		FileIdCard fileIdCard = new FileIdCard("TEST");
		fileIdCard.setIdNorme("TEST");
		fileIdCard.setValidite("2022-01-01");
		fileIdCard.setPeriodicite("A");

		// method to test
		MappingRulesOperation.fillMappingRules(c, "arc_bas1", fileIdCard);

		// assertions
		RegleMapping regleMapping = fileIdCard.getIdCardMapping().getReglesMapping().get(0);
		assertEquals("col_sortie", regleMapping.getVariableSortie());
		assertEquals("col1", regleMapping.getExprRegleCol());
	}

}
