package fr.insee.arc.core.service.p6export.parquet;

import static org.junit.Assert.*;

import org.junit.Test;

import fr.insee.arc.core.service.p6export.parquet.ParquetEncryptionKey.EncryptionType;

public class ParquetEncryptionKeyTest {

	@Test
	public void test() {
		ParquetEncryptionKey p128 = new ParquetEncryptionKey(EncryptionType.KEY128, "0123456789abcdef");
		ParquetEncryptionKey p192 = new ParquetEncryptionKey(EncryptionType.KEY192, "0123456789abcdefghijklmop");
		ParquetEncryptionKey p256 = new ParquetEncryptionKey(EncryptionType.KEY256, "0123456789abcdefghijklmopqrstuvwx");
		
		assertEquals(EncryptionType.KEY128, p128.getType());
		assertEquals(EncryptionType.KEY192, p192.getType());
		assertEquals(EncryptionType.KEY256, p256.getType());

		assertEquals("0123456789abcdef", p128.getValue());
		assertEquals("0123456789abcdefghijklmop", p192.getValue());
		assertEquals("0123456789abcdefghijklmopqrstuvwx", p256.getValue());
		
	}

}
