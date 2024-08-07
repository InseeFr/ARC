package fr.insee.arc.core.service.global.bo;

import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.structure.GenericBean;
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
    public static List<JeuDeRegle> recupJeuDeRegle(Connection connexion, String tableJeuDeRegle) throws ArcException {
        StaticLoggerDispatcher.info(LOGGER, "Recherche des jeux de règles à appliquer");

        StringBuilder requete = new StringBuilder();
        requete.append("SELECT a.id_norme, a.periodicite, a.validite_inf, a.validite_sup, a.version");
        requete.append("\n FROM " + tableJeuDeRegle + " a ");

		Map<String,List<String>> g=new GenericBean(UtilitaireDao.get(0).executeRequest(connexion, new ArcPreparedStatementBuilder(requete))).mapContent();
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
    public static List<JeuDeRegle> recupJeuDeRegle(Connection connexion, String envExecution, String nomTableATraiter) throws ArcException {
        StaticLoggerDispatcher.info(LOGGER, "Recherche des jeux de règles à appliquer");

        StringBuilder requete = new StringBuilder();
        requete.append("SELECT a.id_norme, a.periodicite, a.validite_inf, a.validite_sup, a.version");
        requete.append("\n FROM " + ViewEnum.JEUDEREGLE.getFullName(envExecution) + " a ");
        // optimization : a thread per file so reading the first line is enough
        requete.append("\n WHERE EXISTS (SELECT 1 FROM (SELECT * FROM " + nomTableATraiter + " LIMIT 1) b ");
        requete.append("\n  WHERE a.id_norme=b.id_norme ");
        requete.append("\n    AND a.periodicite=b.periodicite ");
        requete.append("\n    AND to_date(b.validite,'"+ArcDateFormat.DATE_FORMAT_CONVERSION.getDatastoreFormat()+"')>=a.validite_inf ");
        requete.append("\n    AND to_date(b.validite,'"+ArcDateFormat.DATE_FORMAT_CONVERSION.getDatastoreFormat()+"')<=a.validite_sup); ");


		Map<String,List<String>> g=new GenericBean(UtilitaireDao.get(0).executeRequest(connexion, new ArcPreparedStatementBuilder(requete))).mapContent();

		List<JeuDeRegle> listJdr = extractRuleSetObjects(g);

        StaticLoggerDispatcher.info(LOGGER, "J'ai trouvé " + listJdr.size() + " jeux de règle, utiles pour controler");
        return listJdr;
    }

	private static List<JeuDeRegle> extractRuleSetObjects(Map<String, List<String>> g) throws ArcException {
		SimpleDateFormat formatDate = new SimpleDateFormat(ArcDateFormat.DATE_FORMAT_CONVERSION.getApplicationFormat());
        List<JeuDeRegle> listJdr = new ArrayList<>();
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
					} catch (ParseException ex) {
						ArcException e = new ArcException(ArcExceptionMessage.DATE_PARSE_FAILED_VALIDITE_INF, g.get("validite_inf").get(i));
						e.logFullException();
						throw e;
					}
	                try {
						jdr.setValiditeSup(formatDate.parse(g.get("validite_sup").get(i)));
					} catch (ParseException ex) {
						ArcException e = new ArcException(ArcExceptionMessage.DATE_PARSE_FAILED_VALIDITE_SUP, g.get("validite_sup").get(i));
						e.logFullException();
						throw e;
					}
	                jdr.setVersion(g.get("version").get(i));
	                // Ajout à la liste de résultat
	                listJdr.add(jdr);
	        }
		}
		return listJdr;
	}

}
