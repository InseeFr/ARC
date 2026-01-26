package fr.insee.arc.ws.services.importServlet.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.SQLException;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.query.InitializeQueryTest;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.ws.services.importServlet.bo.ArcClientIdentifier;
import fr.insee.arc.ws.services.importServlet.bo.ArcClientIdentifierUnsafe;

class ClientDaoTest extends InitializeQueryTest {

	@Test
	void tableOfIdSourceTest() throws ArcException, SQLException {
		
		buildPropertiesWithoutScalability(null);

		createTestSchema(c, "arc_bas1");

		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();

		query.build(FormatSQL.dropTable(ViewEnum.PILOTAGE_FICHIER.getFullName("arc_bas1")));

		query.build(SQL.CREATE, SQL.TABLE, ViewEnum.PILOTAGE_FICHIER.getFullName("arc_bas1"));
		query.build("(", query.sqlDDLOfColumnsFromModel(ViewEnum.PILOTAGE_FICHIER), ")");
		query.build(SQL.END_QUERY);

		query.build(SQL.INSERT_INTO, ViewEnum.PILOTAGE_FICHIER.getFullName("arc_bas1"));
		query.build("(", ColumnEnum.ID_SOURCE, ",", ColumnEnum.VALIDITE, ",", ColumnEnum.PHASE_TRAITEMENT, ",",
				ColumnEnum.ETAT_TRAITEMENT, ",", ColumnEnum.CLIENT, ")");
		query.build(SQL.VALUES, "(", "'1', '2026-01-01', 'MAPPING', '{OK}', '{EXPORT,DSNFLASH}'", ")");
		query.build(",", "(", "'2', '2026-01-01', 'MAPPING', '{OK}', null", ")");
		query.build(",", "(", "'3', '2026-01-01', 'MAPPING', '{OK}', '{EXPORT}'", ")");
		query.build(",", "(", "'4', '2026-01-01', 'MAPPING', '{OK}', '{EXPORT,ARTEMIS}'", ")");
		query.build(SQL.END_QUERY);

		u.executeRequest(c, query);

		JSONObject clientJsonInputStep1 = new JSONObject(
				"{\"familleNorme\":\"DSN\",\"periodicite\":\"M\",\"service\":\"arcClient\",\"validiteSup\":\"2032-03-01\",\"format\":\"csv_gzip\",\"reprise\":false,\"client\":\"ARTEMIS\",\"environnement\":\"arc_bas1\"}");

		ArcClientIdentifier clientJsonInputValidated = new ArcClientIdentifier(
				new ArcClientIdentifierUnsafe(clientJsonInputStep1), null);
		ClientDao clientDao = new ClientDao(clientJsonInputValidated);

		clientDao.createTableOfIdSource();

		query = new ArcPreparedStatementBuilder();
		query.build(SQL.SELECT, "count(*)", SQL.FROM, "zz");
		int r = u.getInt(c, query);

		assertEquals(3, r);

		dropTestSchema(c);

	}

}
