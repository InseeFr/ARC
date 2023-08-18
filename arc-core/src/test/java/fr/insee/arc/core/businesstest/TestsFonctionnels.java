package fr.insee.arc.core.businesstest;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.factory.ApiServiceFactory;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.api.ApiReceptionService;
import fr.insee.arc.core.service.api.ApiService;
import fr.insee.arc.core.service.engine.initialisation.BddPatcherTest;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.query.InitializeQueryTest;

public class TestsFonctionnels extends InitializeQueryTest 
{

	@Rule
	public TemporaryFolder testFolder= new TemporaryFolder();
	
	@Test
	public void executeFunctionnalTests() throws IOException, SQLException, ArcException {
		BddPatcherTest.createDatabase();

		
		File root=testFolder.newFolder("root");
		String repertoire = root.getAbsolutePath();
		
		buildProperties(repertoire);
		
		executeTestSirene("arc_bas1", repertoire);
		
		executeTestSiera("arc_bas2", repertoire);
		
		executeTestAnimal("arc_bas8", repertoire);

		
	}
	
	
	

	
	/**
	 * COVERAGE
	 * complex xml load test
	 * 
	 * @param sandbox
	 * @param repertoire
	 * @throws IOException
	 * @throws ArcException
	 * @throws SQLException
	 */
	private void executeTestSirene(String sandbox, String repertoire) throws IOException, ArcException, SQLException
	{		
		BddPatcherTest.insertTestDataSirene();

		
		ApiServiceFactory.getService(TraitementPhase.INITIALISATION.toString(), ApiService.IHM_SCHEMA, sandbox, repertoire,
				10000000, null
		).invokeApi();
		
		String repertoireDeDepot = ApiReceptionService.directoryReceptionEntrepot(repertoire, sandbox, "DEFAULT");
		
		Files.copy(this.getClass().getClassLoader().getResourceAsStream("testFiles/Cas_test_V2008.11.zip"), new File(repertoireDeDepot, "Cas_test_V2008.11.zip").toPath());
		Files.copy(this.getClass().getClassLoader().getResourceAsStream("testFiles/Cas_test_V2016.02.zip"), new File(repertoireDeDepot, "Cas_test_V2016.02.zip").toPath());
		
		ApiServiceFactory.getService(TraitementPhase.RECEPTION.toString(), ApiService.IHM_SCHEMA, sandbox, repertoire,
				10000000, null
		).invokeApi();
		
		
		assertEquals(114, nbFileInPhase(sandbox, TraitementPhase.RECEPTION, TraitementEtat.OK));
		
		ApiServiceFactory.getService(TraitementPhase.CHARGEMENT.toString(), ApiService.IHM_SCHEMA, sandbox, repertoire,
				10000000, null
		).invokeApi();
		assertEquals(114, nbFileInPhase(sandbox, TraitementPhase.CHARGEMENT, TraitementEtat.OK));

		ApiServiceFactory.getService(TraitementPhase.NORMAGE.toString(), ApiService.IHM_SCHEMA, sandbox, repertoire,
				10000000, null
		).invokeApi();
		assertEquals(114, nbFileInPhase(sandbox, TraitementPhase.NORMAGE, TraitementEtat.OK));
		
		ApiServiceFactory.getService(TraitementPhase.CONTROLE.toString(), ApiService.IHM_SCHEMA, sandbox, repertoire,
				10000000, null
		).invokeApi();
		assertEquals(47, nbFileInPhase(sandbox, TraitementPhase.CONTROLE, TraitementEtat.OK));

	}
	
	/**
	 * COVERAGE
	 * xml load test
	 * normage complexe rule test
	 * filtering controle rule test
	 * complex mapping rules test
	 * 
	 * @param sandbox
	 * @param repertoire
	 * @throws IOException
	 * @throws ArcException
	 * @throws SQLException
	 */
	private void executeTestSiera(String sandbox, String repertoire) throws IOException, ArcException, SQLException
	{		
		BddPatcherTest.insertTestDataSiera();
		
		ApiServiceFactory.getService(TraitementPhase.INITIALISATION.toString(), ApiService.IHM_SCHEMA, sandbox, repertoire,
				10000000, null
		).invokeApi();
		
		String repertoireDeDepot = ApiReceptionService.directoryReceptionEntrepot(repertoire, sandbox, "DEFAULT");
		
		Files.copy(this.getClass().getClassLoader().getResourceAsStream("testFiles/siera_ano.xml"), new File(repertoireDeDepot, "siera_ano.xml").toPath());
		
		ApiServiceFactory.getService(TraitementPhase.RECEPTION.toString(), ApiService.IHM_SCHEMA, sandbox, repertoire,
				10000000, null
		).invokeApi();
		
		assertEquals(1, nbFileInPhase(sandbox, TraitementPhase.RECEPTION, TraitementEtat.OK));
		
		ApiServiceFactory.getService(TraitementPhase.CHARGEMENT.toString(), ApiService.IHM_SCHEMA, sandbox, repertoire,
				10000000, null
		).invokeApi();
		assertEquals(1, nbFileInPhase(sandbox, TraitementPhase.CHARGEMENT, TraitementEtat.OK));

		ApiServiceFactory.getService(TraitementPhase.NORMAGE.toString(), ApiService.IHM_SCHEMA, sandbox, repertoire,
				10000000, null
		).invokeApi();
		assertEquals(1, nbFileInPhase(sandbox, TraitementPhase.NORMAGE, TraitementEtat.OK));
		
		ApiServiceFactory.getService(TraitementPhase.CONTROLE.toString(), ApiService.IHM_SCHEMA, sandbox, repertoire,
				10000000, null
		).invokeApi();
		assertEquals(1, nbFileInPhase(sandbox, TraitementPhase.CONTROLE, TraitementEtat.OK));

		ApiServiceFactory.getService(TraitementPhase.MAPPING.toString(), ApiService.IHM_SCHEMA, sandbox, repertoire,
				10000000, null
		).invokeApi();
		assertEquals(1, nbFileInPhase(sandbox, TraitementPhase.MAPPING, TraitementEtat.OK));
		
	}
	
	/**
	 * COVERAGE
	 * simple csv file load test
	 * tar.gz load test
	 * doublon detection test
	 * 
	 * @param sandbox
	 * @param repertoire
	 * @throws IOException
	 * @throws ArcException
	 * @throws SQLException
	 */
	private void executeTestAnimal(String sandbox, String repertoire) throws IOException, ArcException, SQLException
	{		
		BddPatcherTest.insertTestDataAnimal();
		
		ApiServiceFactory.getService(TraitementPhase.INITIALISATION.toString(), ApiService.IHM_SCHEMA, sandbox, repertoire,
				10000000, null
		).invokeApi();
		
		String repertoireDeDepot = ApiReceptionService.directoryReceptionEntrepot(repertoire, sandbox, "DEFAULT");
		
		Files.copy(this.getClass().getClassLoader().getResourceAsStream("testFiles/animals.tar.gz"), new File(repertoireDeDepot, "animals.tar.gz").toPath());
		
		ApiServiceFactory.getService(TraitementPhase.RECEPTION.toString(), ApiService.IHM_SCHEMA, sandbox, repertoire,
				10000000, null
		).invokeApi();
		
		assertEquals(2, nbFileInPhase(sandbox, TraitementPhase.RECEPTION, TraitementEtat.OK));
		
		ApiServiceFactory.getService(TraitementPhase.CHARGEMENT.toString(), ApiService.IHM_SCHEMA, sandbox, repertoire,
				10000000, null
		).invokeApi();
		assertEquals(2, nbFileInPhase(sandbox, TraitementPhase.CHARGEMENT, TraitementEtat.OK));
		
		
		// doublon detection test
		Files.copy(this.getClass().getClassLoader().getResourceAsStream("testFiles/animals-001.csv"), new File(repertoireDeDepot, "animals-001.csv").toPath());

		ApiServiceFactory.getService(TraitementPhase.RECEPTION.toString(), ApiService.IHM_SCHEMA, sandbox, repertoire,
				10000000, null
		).invokeApi();
		
		assertEquals(1, nbFileInPhase(sandbox, TraitementPhase.RECEPTION, TraitementEtat.KO));
		
	}
	
	private int nbFileInPhase(String sandbox, TraitementPhase phase, TraitementEtat etat)
	{
		ArcPreparedStatementBuilder query=new ArcPreparedStatementBuilder();
		query.build(SQL.SELECT, "count(*)", SQL.FROM, sandbox, ".", "pilotage_fichier", SQL.WHERE, "phase_traitement=", query.quoteText(phase.toString()), SQL.AND, "etat_traitement=", query.quoteText(etat.getSqlArrayExpression()), "::text[]");
		
		return UtilitaireDao.get(0).getInt(c, query);
	}
	

}
