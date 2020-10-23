package fr.insee.arc.core.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.model.JeuDeRegle;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
public class JeuDeRegleDao {

    private static final Logger LOGGER = LogManager.getLogger(JeuDeRegleDao.class);

    /**
     * Récupération de l'ensemble des jeux de règles applicables à une table à controler Une même table peu contenir des validités
     * différentes attention, après il faudra les remplir avec les règles associées
     *
     * @param connexion
     * @param nomTableATraiter
     * @param tableJeuDeRegle
     * @return
     * @throws SQLException
     */
    public static ArrayList<JeuDeRegle> recupJeuDeRegle(Connection connexion, String nomTableATraiter, String tableJeuDeRegle) throws SQLException {
        StaticLoggerDispatcher.info("Recherche des jeux de règles à appliquer", LOGGER);
        ArrayList<JeuDeRegle> listJdr = new ArrayList<>();

        StringBuilder requete = new StringBuilder();
        requete.append("SELECT a.id_norme, a.periodicite, a.validite_inf, a.validite_sup, a.version");
        requete.append("\n FROM " + tableJeuDeRegle + " a ");
        // optimization : a thread per file so reading the first line is enough
        requete.append("\n WHERE EXISTS (SELECT 1 FROM (SELECT * FROM " + nomTableATraiter + " LIMIT 1) b ");
        requete.append("\n  WHERE a.id_norme=b.id_norme ");
        requete.append("\n    AND a.periodicite=b.periodicite ");
        requete.append("\n    AND to_date(b.validite,'YYYY-MM-DD')>=a.validite_inf ");
        requete.append("\n    AND to_date(b.validite,'YYYY-MM-DD')<=a.validite_sup); ");


		HashMap<String,ArrayList<String>> g=new GenericBean(UtilitaireDao.get("arc").executeRequest(connexion, requete)).mapContent();

		SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd");

		if (!g.isEmpty())
		{
			for (int i=0;i<g.get("id_norme").size();i++)
			{
	                // Instanciation
	                JeuDeRegle jdr = new JeuDeRegle();
	                // Remplissage
	                jdr.setIdNorme(g.get("id_norme").get(i));
	                jdr.setPeriodicite(g.get("periodicite").get(i));
	                try {
	                	jdr.setValiditeInf(formatDate.parse(g.get("validite_inf").get(i)));
						jdr.setValiditeSup(formatDate.parse(g.get("validite_sup").get(i)));
					} catch (ParseException ex) {
					    LoggerHelper.errorGenTextAsComment(JeuDeRegleDao.class, "recupJeuDeRegle()", LOGGER, ex);
					}
	                jdr.setVersion(g.get("version").get(i));
	                // Ajout à la liste de résultat
	                listJdr.add(jdr);
	        }
		}

        StaticLoggerDispatcher.info("J'ai trouvé " + listJdr.size() + " jeux de règle, utiles pour controler", LOGGER);
        return listJdr;
    }

}
