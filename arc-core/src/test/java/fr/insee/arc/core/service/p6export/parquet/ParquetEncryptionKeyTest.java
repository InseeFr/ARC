package fr.insee.arc.core.service.p6export.parquet;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import fr.insee.arc.core.service.p6export.parquet.ParquetEncryptionKey.EncryptionType;

public class ParquetEncryptionKeyTest {

	@Test
	public void test() {
		ParquetEncryptionKey p256 = new ParquetEncryptionKey(EncryptionType.KEY256, "0123456789abcdefghijklmopqrstuvwx");
		
		assertEquals(EncryptionType.KEY256, p256.getType());
		
		assertEquals("0123456789abcdefghijklmopqrstuvwx", p256.getValue());
		
	}

}
