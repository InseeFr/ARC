package fr.insee.arc.core.service.thread;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import fr.insee.arc.core.model.PilotageEntity;
import fr.insee.arc.core.model.TypeTraitementPhase;
import fr.insee.arc.core.service.ApiIdentifyService;
import fr.insee.arc.core.service.thread.ThreadIdentifyService;
import fr.insee.arc.core.utils.CompareInputStream;
import fr.insee.arc.utils.dao.UtilitaireDao;

public class ThreadIdentifyServiceTest {

    @Test
    public void testOpenStreamFromFile() throws Exception {
	// GIVEN
	String aCurrentPhase = TypeTraitementPhase.IDENTIFY.name();
	String anParametersEnvironment = "arc.ihm";
	String aEnvExecution = "arc_test";
	String aDirectoryRoot = "src/test/resources/testFiles/";
	String fileToTest = "src/test/resources/testFiles/ARC_TEST/REGISTER_OK/testFile.xml";
	Integer aNbEnr = 10000000;
	Connection connection = UtilitaireDao.get("arc").getDriverConnexion();

	ApiIdentifyService apiIdentifyService = new ApiIdentifyService(aCurrentPhase, anParametersEnvironment,
		aEnvExecution, aDirectoryRoot, aNbEnr);
	PilotageEntity fileToProcess = new PilotageEntity();
	fileToProcess.setContainer("archive_test.zip");
	fileToProcess.setIdSource("testFile.xml");
	List<PilotageEntity> filesToProcess = new ArrayList<PilotageEntity>();
	filesToProcess.add(fileToProcess);
	apiIdentifyService.setFilesToProcess(filesToProcess);

	ThreadIdentifyService threadIdentifyServiceToTest = new ThreadIdentifyService(0, apiIdentifyService,
		connection);

	//WHEN
	threadIdentifyServiceToTest.openStreamFromFile();
	
	
	//THEN
	InputStream expectedStreamIdentify = new FileInputStream(new File(fileToTest));

	assertTrue(CompareInputStream.isSame(threadIdentifyServiceToTest.filesInputStreamLoad.getTmpInxIdentify(), expectedStreamIdentify));
	assertTrue(threadIdentifyServiceToTest.filesInputStreamLoad.getTmpInxCSV()== null);
	assertTrue(threadIdentifyServiceToTest.filesInputStreamLoad.getTmpInxLoad()== null);


	expectedStreamIdentify.close();
    }

}
