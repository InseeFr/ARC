package fr.insee.arc.core.service.p4controle.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;

import org.junit.jupiter.api.Test;

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
		u.executeRequest(c, "DROP SCHEMA IF EXISTS arc_bas1 CASCADE;");
		u.executeRequest(c, "CREATE SCHEMA IF NOT EXISTS arc_bas1;");
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build("CREATE TABLE arc_bas1.controle_regle (", "id_norme text NULL," //
				, "periodicite text NULL," //
				, "validite_inf date NULL," //
				, "validite_sup date NULL," //
				, "version text NULL," //
				, "id_classe text NULL," //
				, "rubrique_pere text NULL," //
				, "rubrique_fils text NULL," //
				, "borne_inf text NULL," //
				, "borne_sup text NULL," //
				, "condition text NULL," //
				, "pre_action text NULL," //
				, "id_regle int4 NULL," //
				, "todo text NULL," //
				, "commentaire text NULL," //
				, "xsd_ordre int4 NULL," //
				, "xsd_label_fils text NULL," //
				, "xsd_role text NULL," //
				, "blocking_threshold text NULL," //
				, "error_row_processing text NULL" //
				, ")" //
				, "WITH (autovacuum_enabled=false)" //
				, SQL.END_QUERY);

		query.build(SQL.INSERT_INTO, "arc_bas1.controle_regle", SQL.VALUES,
				"('TEST', 'A', '2020-01-01', '2025-01-01', 'v01', 'CARDINALITE', 'col1', 'col2', '2', '3', 'requete', 'pretraitement', 1, '', '', 1, 'xsd1', 'xsd2', '>0u', 'e')",
				SQL.END_QUERY);
		// hors calendrier
		query.build(SQL.INSERT_INTO, "arc_bas1.controle_regle", SQL.VALUES,
				"('TEST', 'A', '1998-01-01', '2020-01-01', 'v01', 'CARDINALITE', 'col_invalid', 'col_invalid', '2', '3', 'requete', 'pretraitement', 1, '', '', 1, 'xsd1', 'xsd2', '>0u', 'e')",
				SQL.END_QUERY);
		// norme différente
		query.build(SQL.INSERT_INTO, "arc_bas1.controle_regle", SQL.VALUES,
				"('FAKE_NORM', 'A', '2020-01-01', '2025-01-01', 'v01', 'CARDINALITE', 'col_invalid', 'col_invalid', '2', '3', 'requete', 'pretraitement', 1, '', '', 1, 'xsd1', 'xsd2', '>0u', 'e')",
				SQL.END_QUERY);
		// périodicité différente
		query.build(SQL.INSERT_INTO, "arc_bas1.controle_regle", SQL.VALUES,
				"('TEST', 'M', '2020-01-01', '2025-01-01', 'v01', 'CARDINALITE', 'col_invalid', 'col_invalid', '2', '3', 'requete', 'pretraitement', 1, '', '', 1, 'xsd1', 'xsd2', '>0u', 'e')",
				SQL.END_QUERY);
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

		// rubriques are meant to be set in uppercase
		assertEquals("COL1", regleControle.getRubriquePere());
		assertEquals("COL2", regleControle.getRubriqueFils());
		assertEquals("2", regleControle.getBorneInf());
		assertEquals("3", regleControle.getBorneSup());
		assertEquals("requete", regleControle.getCondition());
		assertEquals("pretraitement", regleControle.getPreAction());
		assertEquals(1, regleControle.getIdRegle());
		assertEquals(">0u", regleControle.getSeuilBloquant());
		assertEquals("e", regleControle.getTraitementLignesErreur());
	}

}
