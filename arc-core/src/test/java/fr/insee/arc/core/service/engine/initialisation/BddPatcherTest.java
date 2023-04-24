package fr.insee.arc.core.service.engine.initialisation;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

import fr.insee.arc.core.dataobjects.DataObjectService;
import fr.insee.arc.core.dataobjects.SchemaEnum;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.utils.dao.GenericPreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.query.InitializeQueryTest;
import fr.insee.arc.utils.structure.GenericBean;

public class BddPatcherTest extends InitializeQueryTest {

	@Test
	/**
	 * test the database iniitalization
	 * @throws ArcException 
	 */
	public void bddScript() throws ArcException {

		String oldVersion="v1";
		String newVersion="v2";	
		String userWithRestrictedRights="arc_restricted";
		String testSandbox="arc_bas1";
				
		// test the meta data schema creation
		BddPatcher.bddScript(oldVersion, newVersion, userWithRestrictedRights, c);
		
		GenericPreparedStatementBuilder query;
		
		query = new GenericPreparedStatementBuilder();
		query.append("select tablename from pg_tables where schemaname=").append(query.quoteText(DataObjectService.ARC_METADATA_SCHEMA));
		
		HashMap<String,ArrayList<String>> content;
		
		content=new GenericBean(UtilitaireDao.get(UtilitaireDao.DEFAULT_CONNECTION_POOL).executeRequest(c, query)).mapContent();
		
		// check if all metadata view had been created
		for (ViewEnum v : ViewEnum.values())
		{
			if (v.getTableLocation().equals(SchemaEnum.METADATA))
			{
				assertTrue(content.get("tablename").contains(v.getTableName()));
			}
		}
		
		// test a sandbox schema creation
		BddPatcher.bddScript(oldVersion, newVersion, userWithRestrictedRights, c, testSandbox);

		query = new GenericPreparedStatementBuilder();
		query.append("select tablename from pg_tables where schemaname=").append(query.quoteText(testSandbox));
		
		content=new GenericBean(UtilitaireDao.get(UtilitaireDao.DEFAULT_CONNECTION_POOL).executeRequest(c, query)).mapContent();
		
		// check if the sandbox views had been created
		for (ViewEnum v : ViewEnum.values())
		{
			if (v.getTableLocation().equals(SchemaEnum.SANDBOX))
			{
				assertTrue(content.get("tablename").contains(v.getTableName()));
			}
		}
	}

}
