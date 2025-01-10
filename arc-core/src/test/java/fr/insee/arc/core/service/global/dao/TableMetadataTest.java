package fr.insee.arc.core.service.global.dao;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.query.InitializeQueryTest;
import fr.insee.arc.utils.structure.GenericBean;

public class TableMetadataTest extends InitializeQueryTest {

	@Test
	public void rebuildTableAndIndexTest () throws ArcException {
		
		String tablePil = "public.pilotage_fichier";

		u.executeImmediate(c, "CREATE TABLE "+tablePil+" (id_source text, phase_traitement text, etat_traitement text[]);");
		
		u.executeImmediate(c, "CREATE INDEX idx1_pilotage_fichier on "+tablePil+" (id_source);");
		u.executeImmediate(c, "CREATE INDEX idx2_pilotage_fichier on "+tablePil+" (phase_traitement);");
		
		u.executeImmediate(c, "INSERT INTO "+tablePil+" select 'f1', 'MAPPING', '{OK}'");
		u.executeImmediate(c, "INSERT INTO "+tablePil+" select 'f2', 'MAPPING', '{OK}'");
		u.executeImmediate(c, "INSERT INTO "+tablePil+" select 'f3', 'CHARGEMENT', '{OK}'");
		

		// there should be 2 indexes
		int numberOfIndexes = u.getInt(c, new ArcPreparedStatementBuilder("SELECT count(*) from ("+TableMetadata.queryIndexesInformations(tablePil)+") vv "));
		assertEquals(2, numberOfIndexes);

		// there should be 3 records		
		int numberOfRecords = u.getInt(c, new ArcPreparedStatementBuilder("SELECT count(*) from "+tablePil));
		assertEquals(3, numberOfRecords);
				
		// now rebuild the table with indexes and see if number of indexes and records are still the same
		GenericBean indexDefinition = new GenericBean(u.executeRequest(c, TableMetadata.queryIndexesInformations(tablePil)));
		u.executeRequest(c, TableMetadata.rebuildTable(tablePil, indexDefinition));

		// there should be still 2 indexes
		int numberOfIndexesAfterRebuild = u.getInt(c, new ArcPreparedStatementBuilder("SELECT count(*) from ("+TableMetadata.queryIndexesInformations(tablePil)+") vv "));
		assertEquals(2, numberOfIndexesAfterRebuild);

		// there should be 3 still records		
		int numberOfRecordsAfterRebuild = u.getInt(c, new ArcPreparedStatementBuilder("SELECT count(*) from "+tablePil));
		assertEquals(3, numberOfRecordsAfterRebuild);
		
		// rebuild without indexes
		u.executeRequest(c, TableMetadata.rebuildTable(tablePil));
		
		// there should be now 0 indexes
		int numberOfIndexesAfterRebuildWithoutIndex = u.getInt(c, new ArcPreparedStatementBuilder("SELECT count(*) from ("+TableMetadata.queryIndexesInformations(tablePil)+") vv "));
		assertEquals(0, numberOfIndexesAfterRebuildWithoutIndex);

		// there should be 3 still records		
		int numberOfRecordsAfterRebuildWithoutIndex = u.getInt(c, new ArcPreparedStatementBuilder("SELECT count(*) from "+tablePil));
		assertEquals(3, numberOfRecordsAfterRebuildWithoutIndex);
		
		u.dropTable(c, tablePil);
		
		
	}

}
