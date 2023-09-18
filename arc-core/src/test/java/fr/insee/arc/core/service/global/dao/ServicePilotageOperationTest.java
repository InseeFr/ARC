package fr.insee.arc.core.service.global.dao;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.dao.PilotageOperations;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.query.InitializeQueryTest;
import fr.insee.arc.utils.structure.GenericBean;

public class ServicePilotageOperationTest extends InitializeQueryTest {

	@Test
	public void retrieveIdSourceFromPilotageQueryTest() throws ArcException {
		String tablePil = "public.table_pilotage";

		u.executeImmediate(c, "CREATE TABLE "+tablePil+" (id_source text, phase_traitement text, etat_traitement text[]);");
		u.executeImmediate(c, "INSERT INTO "+tablePil+" select 'f1', 'MAPPING', '{OK}'");
		u.executeImmediate(c, "INSERT INTO "+tablePil+" select 'f2', 'MAPPING', '{OK}'");
		u.executeImmediate(c, "INSERT INTO "+tablePil+" select 'f3', 'CHARGEMENT', '{OK}'");
		
		
		ArrayList<String> listOfIdSource  = new GenericBean(u.executeRequest(c,
				PilotageOperations.querySelectIdSourceFromPilotage(tablePil, TraitementPhase.MAPPING, TraitementEtat.OK)
				)).mapContent().get("id_source");
		
		assertEquals(2, listOfIdSource.size());
	}

}
