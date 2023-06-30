package fr.insee.arc.core.rulesobjects;

import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.model.JeuDeRegle;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.utils.LoggerHelper;
public class JeuDeRegleDao {

    private static final Logger LOGGER = LogManager.getLogger(JeuDeRegleDao.class);
    
    private JeuDeRegleDao() {
        throw new IllegalStateException("Utility class");
      }
    
    /**
     * Fetch all the rulesets, without the rules.
     *
     * @param connexion
     * @param nomTableATraiter
     * @param tableJeuDeRegle
     * @return
     * @throws ArcException
     */
    public static ArrayList<JeuDeRegle> recupJeuDeRegle(Connection connexion, String tableJeuDeRegle) throws ArcException {
        StaticLoggerDispatcher.info(LOGGER, "Recherche des jeux de règles à appliquer");

        StringBuilder requete = new StringBuilder();
        requete.append("SELECT a.id_norme, a.periodicite, a.validite_inf, a.validite_sup, a.version");
        requete.append("\n FROM " + tableJeuDeRegle + " a ");

		HashMap<String,ArrayList<String>> g=new GenericBean(UtilitaireDao.get(0).executeRequest(connexion, new ArcPreparedStatementBuilder(requete))).mapContent();
        return extractRuleSetObjects(g);
    }
    
    /**
     * Récupération de l'ensemble des jeux de règles applicables à une table à controler Une même table peu contenir des validités
     * différentes attention, après il faudra les remplir avec les règles associées
     *
     * @param connexion
     * @param nomTableATraiter
     * @param tableJeuDeRegle
     * @return
     * @throws ArcException
     */
    public static ArrayList<JeuDeRegle> recupJeuDeRegle(Connection connexion, String nomTableATraiter, String tableJeuDeRegle) throws ArcException {
        StaticLoggerDispatcher.info(LOGGER, "Recherche des jeux de règles à appliquer");

        StringBuilder requete = new StringBuilder();
        requete.append("SELECT a.id_norme, a.periodicite, a.validite_inf, a.validite_sup, a.version");
        requete.append("\n FROM " + tableJeuDeRegle + " a ");
        // optimization : a thread per file so reading the first line is enough
        requete.append("\n WHERE EXISTS (SELECT 1 FROM (SELECT * FROM " + nomTableATraiter + " LIMIT 1) b ");
        requete.append("\n  WHERE a.id_norme=b.id_norme ");
        requete.append("\n    AND a.periodicite=b.periodicite ");
        requete.append("\n    AND to_date(b.validite,'YYYY-MM-DD')>=a.validite_inf ");
        requete.append("\n    AND to_date(b.validite,'YYYY-MM-DD')<=a.validite_sup); ");


		HashMap<String,ArrayList<String>> g=new GenericBean(UtilitaireDao.get(0).executeRequest(connexion, new ArcPreparedStatementBuilder(requete))).mapContent();

		ArrayList<JeuDeRegle> listJdr = extractRuleSetObjects(g);

        StaticLoggerDispatcher.info(LOGGER, "J'ai trouvé " + listJdr.size() + " jeux de règle, utiles pour controler");
        return listJdr;
    }

	private static ArrayList<JeuDeRegle> extractRuleSetObjects(HashMap<String, ArrayList<String>> g) {
		SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd");
        ArrayList<JeuDeRegle> listJdr = new ArrayList<>();
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
		return listJdr;
	}

}
