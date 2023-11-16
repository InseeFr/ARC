package fr.insee.arc.core.service.p4controle.operation;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;

import org.junit.Test;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.service.global.bo.FileIdCard;
import fr.insee.arc.core.service.p4controle.bo.ControleTypeCode;
import fr.insee.arc.core.service.p4controle.bo.RegleControle;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.query.TestDatabase;

public class ControleRulesOperationTest {

	public static UtilitaireDao u = UtilitaireDao.get(0);
    
    public static Connection c = new TestDatabase().testConnection;
    
	@Test
	public void fillControleRulesTest() throws ArcException {
		// insert test data
		u.executeImmediate(c, "DROP SCHEMA IF EXISTS arc_bas1 CASCADE;");
		u.executeImmediate(c, "CREATE SCHEMA IF NOT EXISTS arc_bas1;");
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build("CREATE TABLE arc_bas1.controle_regle (\r\n"
				+ "	id_norme text NULL,\r\n"
				+ "	periodicite text NULL,\r\n"
				+ "	validite_inf date NULL,\r\n"
				+ "	validite_sup date NULL,\r\n"
				+ "	\"version\" text NULL,\r\n"
				+ "	id_classe text NULL,\r\n"
				+ "	rubrique_pere text NULL,\r\n"
				+ "	rubrique_fils text NULL,\r\n"
				+ "	borne_inf text NULL,\r\n"
				+ "	borne_sup text NULL,\r\n"
				+ "	\"condition\" text NULL,\r\n"
				+ "	pre_action text NULL,\r\n"
				+ "	id_regle int4 NULL,\r\n"
				+ "	todo text NULL,\r\n"
				+ "	commentaire text NULL,\r\n"
				+ "	xsd_ordre int4 NULL,\r\n"
				+ "	xsd_label_fils text NULL,\r\n"
				+ "	xsd_role text NULL,\r\n"
				+ "	blocking_threshold text NULL,\r\n"
				+ "	error_row_processing text NULL\r\n"
				+ ")\r\n"
				+ "WITH (\r\n"
				+ "	autovacuum_enabled=false\r\n"
				+ ");");
		query.build(SQL.INSERT_INTO, "arc_bas1.controle_regle", SQL.VALUES, "('TEST', 'A', '2020-01-01', '2025-01-01', 'v01', 'CARDINALITE', 'col1', 'col2', '2', '3', 'requete', 'pretraitement', 1, '', '', 1, 'xsd1', 'xsd2', '>0u', 'e')", SQL.END_QUERY);
		// hors calendrier
		query.build(SQL.INSERT_INTO, "arc_bas1.controle_regle", SQL.VALUES, "('TEST', 'A', '1998-01-01', '2020-01-01', 'v01', 'CARDINALITE', 'col_invalid', 'col_invalid', '2', '3', 'requete', 'pretraitement', 1, '', '', 1, 'xsd1', 'xsd2', '>0u', 'e')", SQL.END_QUERY);
		// norme différente
		query.build(SQL.INSERT_INTO, "arc_bas1.controle_regle", SQL.VALUES, "('FAKE_NORM', 'A', '2020-01-01', '2025-01-01', 'v01', 'CARDINALITE', 'col_invalid', 'col_invalid', '2', '3', 'requete', 'pretraitement', 1, '', '', 1, 'xsd1', 'xsd2', '>0u', 'e')", SQL.END_QUERY);
		// périodicité différente
		query.build(SQL.INSERT_INTO, "arc_bas1.controle_regle", SQL.VALUES, "('TEST', 'M', '2020-01-01', '2025-01-01', 'v01', 'CARDINALITE', 'col_invalid', 'col_invalid', '2', '3', 'requete', 'pretraitement', 1, '', '', 1, 'xsd1', 'xsd2', '>0u', 'e')", SQL.END_QUERY);
		u.executeRequest(c, query);
		// prepare fileidcard object
		FileIdCard fileIdCard = new FileIdCard("TEST");
		fileIdCard.setIdNorme("TEST");
		fileIdCard.setValidite("2022-01-01");
		fileIdCard.setPeriodicite("A");
		// method to test
		ControleRulesOperation.fillControleRules(c, "arc_bas1", fileIdCard);
		// assertions
		RegleControle regleControle = fileIdCard.getIdCardControle().getReglesControle().get(0);
		assertEquals(ControleTypeCode.CARDINALITE, regleControle.getTypeControle());
		assertEquals("col1", regleControle.getRubriquePere());
		assertEquals("col2", regleControle.getRubriqueFils());
		assertEquals("2", regleControle.getBorneInf());
		assertEquals("3", regleControle.getBorneSup());
		assertEquals("requete", regleControle.getCondition());
		assertEquals("pretraitement", regleControle.getPreAction());
		assertEquals(1, regleControle.getIdRegle());
		assertEquals(">0u", regleControle.getSeuilBloquant());
		assertEquals("e", regleControle.getTraitementLignesErreur());
	}

}
