package fr.insee.arc.batch;

import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;

import fr.insee.arc.utils.dao.PreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;

public class AppTest {


	@Autowired
	PropertiesHandler properties;
	 

	
	public void execute(String[] args) throws Exception {
	System.out.println(UtilitaireDao.get("arc").getInt(null, new PreparedStatementBuilder("select 10")));
	
	PreparedStatementBuilder requete=new PreparedStatementBuilder();
	requete.append("SELECT ");
	requete.append(requete.quoteText("salut"));

	System.out.println(UtilitaireDao.get("arc").getString(null, requete));

	
	System.out.println(UtilitaireDao.get("arc").getLong(null, new PreparedStatementBuilder(("SELECT  145"))));
	
	
	System.out.println(UtilitaireDao.get("arc").getString(null, new PreparedStatementBuilder("select 'aa'")));
	// assert "aa"
	
	System.out.println(UtilitaireDao.get("arc").getBoolean(null, new PreparedStatementBuilder("select 1=0")));
	// assert false
	System.out.println(UtilitaireDao.get("arc").getBoolean(null, new PreparedStatementBuilder("select 1=1")));

	
	System.out.println(UtilitaireDao.get("arc").isTableExiste(null, "arc.ext_etat"));
	
	UtilitaireDao.get("arc").executeImmediate(null, "DROP TABLE IF EXISTS arc_bas2.toto; CREATE TABLE arc_bas2.toto(a text); DROP TABLE IF EXISTS arc_bas2.toto;");
	
	
	}
	
}
