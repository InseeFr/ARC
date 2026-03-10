package fr.insee.arc.core.service.p1reception.provider;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.time.Year;

import org.junit.Test;

import fr.insee.arc.utils.utils.PrivateConstructorTest;

public class DirectoryPathTest {

	@Test
	public void testDirectoryPathIsUtilityClass()
			throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
		PrivateConstructorTest.testConstructorIsPrivate(DirectoryPath.class);
	}

	@Test
	public void directoryReceptionRootTest() {
		assertEquals(new File("/root/ARC_BAS1/RECEPTION").toPath(),
				new File(DirectoryPath.directoryReceptionRoot("/root", "arc_bas1")).toPath());
	}

	@Test
	public void s3ReceptionEntrepotArchiveTest() {
		assertEquals(new File("/ROOT/RECEPTION_ENTREPOT_ARCHIVE").toPath(),
				new File(DirectoryPath.s3ReceptionEntrepotArchive("root", "ENTREPOT")).toPath());
	}

	@Test
	public void s3ReceptionEntrepotKOTest() {
		assertEquals(new File("/ROOT/RECEPTION_ENTREPOT_KO").toPath(),
				new File(DirectoryPath.s3ReceptionEntrepotKO("root", "ENTREPOT")).toPath());
	}

	@Test
	public void directoryReceptionEntrepotArchiveOldTest() {
		assertEquals(new File("/root/ARC_BAS1/RECEPTION_ENTREPOT_ARCHIVE/OLD").toPath(),
				new File(DirectoryPath.directoryReceptionEntrepotArchiveOld("/root", "arc_bas1", "ENTREPOT")).toPath());
	}

	@Test
	public void directoryReceptionEntrepotArchiveOldYearStampedTest() {
		assertEquals(new File(("/root/ARC_BAS1/RECEPTION_ENTREPOT_ARCHIVE/OLD/" + Year.now().getValue())).toPath(),
				new File(DirectoryPath.directoryReceptionEntrepotArchiveOldYearStamped("/root", "arc_bas1", "ENTREPOT"))
						.toPath());
	}

}
