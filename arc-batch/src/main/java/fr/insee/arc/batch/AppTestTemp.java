package fr.insee.arc.batch;

import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;

import fr.insee.arc.utils.dao.PreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;

public class AppTestTemp {
	 
	
	public void execute(String[] args) throws Exception {

	System.out.println(UtilitaireDao.get("arc").getInt(null, new PreparedStatementBuilder("select 10")));
	// assert 10
	
	PreparedStatementBuilder requete=new PreparedStatementBuilder();
	requete.append("SELECT ");
	requete.append(requete.quoteText("salut"));

	System.out.println(UtilitaireDao.get("arc").getString(null, requete));
	// assert "salut"
	
	System.out.println(UtilitaireDao.get("arc").getLong(null, new PreparedStatementBuilder(("SELECT  145"))));
	// assert 145
	
	System.out.println(UtilitaireDao.get("arc").getString(null, new PreparedStatementBuilder("select 'aa'")));
	// assert "aa"
	
	System.out.println(UtilitaireDao.get("arc").getBoolean(null, new PreparedStatementBuilder("select 1=0")));
	// assert false
	System.out.println(UtilitaireDao.get("arc").getBoolean(null, new PreparedStatementBuilder("select 1=1")));
	// assert true

	
	System.out.println(UtilitaireDao.get("arc").isTableExiste(null, "arc.ext_etat"));
	// assert true

	
	System.out.println("v_52eza".replaceFirst("^(i_)|^(v_)", ""));
	// assert 52eza
	
	System.out.println("eaza v_52xd".replaceFirst("^(i_)|^(v_)", ""));
	// assert  "eaza v_52xd"
	
	}
	
}
