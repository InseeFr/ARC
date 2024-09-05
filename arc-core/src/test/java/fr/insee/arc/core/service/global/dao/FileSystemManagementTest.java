package fr.insee.arc.core.service.global.dao;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import fr.insee.arc.core.model.TraitementPhase;

public class FileSystemManagementTest {

	@Test
	public void directoryPhaseRootSubdirectories() {

		String testPath;

		testPath = FileSystemManagement.directoryPhaseRootSubdirectories("root", "arc_bas1", TraitementPhase.EXPORT,
				"client");
		assertEquals("root" + File.separator + "ARC_BAS1" + File.separator + "EXPORT" + File.separator + "client",
				testPath);

		testPath = FileSystemManagement.directoryPhaseRootSubdirectories("root", "arc_bas1", TraitementPhase.EXPORT);
		assertEquals("root" + File.separator + "ARC_BAS1" + File.separator + "EXPORT", testPath);

	}

}
