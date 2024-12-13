package fr.insee.arc.core.service.p0initialisation.useroperation.dao;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.model.TraitementOperationFichier;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.query.InitializeQueryTest;

public class ResetEnvironementDaoTest extends InitializeQueryTest {

	@Test
	public void retrieveIdSourceFromPilotageQueryTest() throws ArcException {

		String tablePil = "arc_bas1.pilotage_fichier";

		u.executeImmediate(c, "DROP SCHEMA IF EXISTS arc_bas1 CASCADE;");
		u.executeImmediate(c, "CREATE SCHEMA arc_bas1;");
		u.executeImmediate(c, "CREATE TABLE "+tablePil+" (id_source text, phase_traitement text, etat_traitement text[], to_delete text);");
		u.executeImmediate(c, "INSERT INTO "+tablePil+" select 'f1', 'RECEPTION', '{OK}', null");
		u.executeImmediate(c, "INSERT INTO "+tablePil+" select 'f1', 'CHARGEMENT', '{OK}', null");
		u.executeImmediate(c, "INSERT INTO "+tablePil+" select 'f1', 'CONTROLE', '{OK}', null");
		u.executeImmediate(c, "INSERT INTO "+tablePil+" select 'f3', 'RECEPTION', '{OK}', null");
		u.executeImmediate(c, "INSERT INTO "+tablePil+" select 'f3', 'CHARGEMENT', '{OK}', null");
		
		ResetEnvironementDao dao = new ResetEnvironementDao(new Sandbox(c, "arc_bas1"));
		
		dao.executeReplayPhaseEntriesInPilotage(TraitementPhase.RECEPTION, Arrays.asList("f1","f2","f3"));
		
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.append("SELECT count(*) as c from "+tablePil+" where to_delete="+query.quoteText(TraitementOperationFichier.R.getDbValue()));
		
		int numberOfLine = UtilitaireDao.get(0).getInt(c, query);
		
		assertEquals(2, numberOfLine);
		
		u.executeImmediate(c, "DROP SCHEMA IF EXISTS arc_bas1 CASCADE;");
		u.executeImmediate(c, "DISCARD TEMP;");

	}
	

}
