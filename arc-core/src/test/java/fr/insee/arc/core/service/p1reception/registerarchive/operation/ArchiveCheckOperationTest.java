package fr.insee.arc.core.service.p1reception.registerarchive.operation;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import fr.insee.arc.utils.exception.ArcException;

public class ArchiveCheckOperationTest extends ArchiveCheckOperation {

	public ArchiveCheckOperationTest() {
		super(null);
	}

	@Test
	public void addEntrepotPrefixToEntryNameTest() throws ArcException {
		assertEquals("DEFAULT_archive.gz", addEntrepotPrefixToEntryName("DEFAULT_","DEFAULT_archive.gz","DEFAULT_archive.gz"));
		assertEquals("DEFAULT_entry1", addEntrepotPrefixToEntryName("DEFAULT_","DEFAULT_archive.zip","entry1"));
		assertEquals("DEFAULT_entry1", addEntrepotPrefixToEntryName("DEFAULT_","DEFAULT_archive.tgz","entry1"));
		assertEquals("DEFAULT_entry1", addEntrepotPrefixToEntryName("DEFAULT_","DEFAULT_archive.tar.gz","entry1"));
	}

	@Test(expected = ArcException.class)
	public void addEntrepotPrefixToEntryNameTestUnknownArchiveType() throws ArcException {
		addEntrepotPrefixToEntryName("DEFAULT_","DEFAULT_archive.unkown_extension","entry");
	}
}
