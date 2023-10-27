package fr.insee.arc.core.service.p2chargement.operation;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;

import org.junit.Test;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.service.global.bo.FileIdCard;
import fr.insee.arc.core.service.p2chargement.bo.IdCardChargement;
import fr.insee.arc.core.service.p2chargement.factory.TypeChargement;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.query.TestDatabase;

public class ChargementRulesOperationTest {

	public static UtilitaireDao u = UtilitaireDao.get(0);
    
    public static Connection c = new TestDatabase().testConnection;

	@Test
	public void fillChargementRulesTest() throws ArcException {
		// insert test data
		u.executeImmediate(c, "DROP SCHEMA IF EXISTS arc_bas1 CASCADE;");
		u.executeImmediate(c, "CREATE SCHEMA IF NOT EXISTS arc_bas1;");
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build("CREATE TABLE arc_bas1.chargement_regle (\r\n"
				+ "	id_regle int8 NULL,\r\n"
				+ "	id_norme text NULL,\r\n"
				+ "	validite_inf date NULL,\r\n"
				+ "	validite_sup date NULL,\r\n"
				+ "	\"version\" text NULL,\r\n"
				+ "	periodicite text NULL,\r\n"
				+ "	type_fichier text NULL,\r\n"
				+ "	\"delimiter\" text NULL,\r\n"
				+ "	format text NULL,\r\n"
				+ "	commentaire text NULL\r\n"
				+ ")\r\n"
				+ "WITH (\r\n"
				+ "	autovacuum_enabled=false\r\n"
				+ ");");
		query.build(SQL.INSERT_INTO, "arc_bas1.chargement_regle", SQL.VALUES, "(1, 'TEST', '2020-01-01', '2025-01-01', 'v01', 'A', 'plat', 'E''\1''', '<encoding>WIN1252</encoding>', '')", SQL.END_QUERY);
		// hors calendrier
		query.build(SQL.INSERT_INTO, "arc_bas1.chargement_regle", SQL.VALUES, "(1, 'TEST', '1998-01-01', '2020-01-01', 'v01', 'A', 'plat', 'separator_invalid', '<encoding>WIN1252</encoding>', '')", SQL.END_QUERY);
		// norme différente
		query.build(SQL.INSERT_INTO, "arc_bas1.chargement_regle", SQL.VALUES, "(1, 'FAKE_NORM', '2020-01-01', '2025-01-01', 'v01', 'A', 'plat', 'separator_invalid', '<encoding>WIN1252</encoding>', '')", SQL.END_QUERY);
		// périodicité différente
		query.build(SQL.INSERT_INTO, "arc_bas1.chargement_regle", SQL.VALUES, "(1, 'TEST', '2020-01-01', '2025-01-01', 'v01', 'M', 'plat', 'separator_invalid', '<encoding>WIN1252</encoding>', '')", SQL.END_QUERY);
		u.executeRequest(c, query);
		// prepare fileidcard object
		FileIdCard fileIdCard = new FileIdCard("TEST");
		fileIdCard.setIdNorme("TEST");
		fileIdCard.setValidite("2022-01-01");
		fileIdCard.setPeriodicite("A");
		// method to test
		ChargementRulesOperation.fillChargementRules(c, "arc_bas1", fileIdCard);
		// assertions
		IdCardChargement regleChargement = fileIdCard.getIdCardChargement();
		assertEquals(TypeChargement.PLAT, regleChargement.getTypeChargement());
		assertEquals("<encoding>WIN1252</encoding>", regleChargement.getFormat());
		assertEquals("E'\1'", regleChargement.getDelimiter());
	}

}
