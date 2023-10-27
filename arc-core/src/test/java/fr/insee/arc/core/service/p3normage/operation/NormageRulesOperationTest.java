package fr.insee.arc.core.service.p3normage.operation;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;

import org.junit.Test;

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
		u.executeImmediate(c, "DROP SCHEMA IF EXISTS arc_bas1 CASCADE;");
		u.executeImmediate(c, "CREATE SCHEMA IF NOT EXISTS arc_bas1;");
		
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build("CREATE TABLE IF NOT EXISTS arc_bas1.normage_regle (\r\n"
				+ "	id_norme text NULL,\r\n"
				+ "	periodicite text NULL,\r\n"
				+ "	validite_inf date NULL,\r\n"
				+ "	validite_sup date NULL,\r\n"
				+ "	\"version\" text NULL,\r\n"
				+ "	id_classe text NULL,\r\n"
				+ "	rubrique text NULL,\r\n"
				+ "	rubrique_nmcl text NULL,\r\n"
				+ "	id_regle int4 NULL,\r\n"
				+ "	todo text NULL,\r\n"
				+ "	commentaire text NULL\r\n"
				+ ")\r\n"
				+ "WITH (\r\n"
				+ "	autovacuum_enabled=false\r\n"
				+ ");");
		
		
		query.build(SQL.INSERT_INTO, "arc_bas1.normage_regle" , SQL.VALUES, "('TEST', 'A', '2020-01-01', '2025-01-01', 'v01', 'relation', 'col1', 'col2', 1, '', '')", SQL.END_QUERY);
		// hors calendrier
		query.build(SQL.INSERT_INTO, "arc_bas1.normage_regle" , SQL.VALUES, "('TEST', 'A', '1998-01-01', '2020-01-01', 'v01', 'relation', 'col_invalid', 'col_invalid', 1, '', '')", SQL.END_QUERY);
		// norme différente
		query.build(SQL.INSERT_INTO, "arc_bas1.normage_regle" , SQL.VALUES, "('FAKE_NORM', 'A', '2020-01-01', '2025-01-01', 'v01', 'relation', 'col_invalid', 'col_invalid', 1, '', '')", SQL.END_QUERY);
		// périodicité différente
		query.build(SQL.INSERT_INTO, "arc_bas1.normage_regle" , SQL.VALUES, "('TEST', 'M', '2020-01-01', '2025-01-01', 'v01', 'relation', 'col_invalid', 'col_invalid', 1, '', '')", SQL.END_QUERY);
		
		
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
