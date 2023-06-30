package fr.insee.arc.core.testfonctionnel.sirene4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.factory.ApiServiceFactory;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.api.ApiReceptionService;
import fr.insee.arc.core.service.api.ApiService;
import fr.insee.arc.core.service.engine.initialisation.BddPatcherTest;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.query.InitializeQueryTest;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.utils.structure.GenericBean;

public class TestFonctionnelSirene4 extends InitializeQueryTest 
{

	@Rule
	public TemporaryFolder testFolder= new TemporaryFolder();
	
	
	private void copyFiles(String root) throws IOException
	{
		
		
		// 1- copy files to folder
		Files.copy(this.getClass().getClassLoader().getResourceAsStream("testFiles/Cas_test_V2008.11.zip"), new File(root, "Cas_test_V2008.11.zip").toPath());
		Files.copy(this.getClass().getClassLoader().getResourceAsStream("testFiles/Cas_test_V2016.02.zip"), new File(root, "Cas_test_V2016.02.zip").toPath());
		
	}
	
	
		
	@Test
	public void executeSirene4() throws IOException, SQLException, ArcException {
		
		BddPatcherTest.createDatabase();
		
		File root=testFolder.newFolder("root");
		String repertoire = root.getAbsolutePath();
		
		PropertiesHandler testProperties=PropertiesHandler.getInstance();
		testProperties.setDatabaseDriverClassName("org.postgresql.Driver");
		testProperties.setDatabaseUrl(c.getMetaData().getURL());
		testProperties.setDatabaseUsername(c.getMetaData().getUserName());
		// user password is not relevant in zonky
		testProperties.setDatabasePassword("NA");
		testProperties.setBatchParametersDirectory(repertoire);
		
		u.setProperties(testProperties);

		
		ApiServiceFactory.getService(TraitementPhase.INITIALISATION.toString(), ApiService.IHM_SCHEMA, "arc_bas1", repertoire,
				10000000, null
		).invokeApi();
		
		String repertoireDeDepot = ApiReceptionService.directoryReceptionEntrepot(repertoire, "arc_bas1", "DEFAULT");
		
		// copy the test files
		copyFiles(repertoireDeDepot);
		
		ApiServiceFactory.getService(TraitementPhase.RECEPTION.toString(), ApiService.IHM_SCHEMA, "arc_bas1", repertoire,
				10000000, null
		).invokeApi();
		
		int nbFilesInReceptionOk=UtilitaireDao.get(0).getInt(c, new ArcPreparedStatementBuilder("SELECT count(*) from arc_bas1.pilotage_fichier where phase_traitement='RECEPTION' and etat_traitement='{OK}'"));
		System.out.println(nbFilesInReceptionOk);
		assertEquals(114, nbFilesInReceptionOk);
		
	}
	

}
