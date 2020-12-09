package fr.insee.arc.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.utils.dao.PreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.core.util.StaticLoggerDispatcher;

/**
 * Chargement brutalement d'un fichier pour déterminer la norme et la validité associées.
 * @author S4LWO8
 *
 */
public class ChargementBrutalTable {

    
	// todo : externaliser dans les regles de chargement
	/** Combien de boucle au maximum */
	public static final int LIMIT_BOUCLE = 1;
    /** Combien de ligne on charge à la fois (OPTI) */
    public static final int LIMIT_CHARGEMENT_BRUTAL = 50;
       
    private static final Logger LOGGER = LogManager.getLogger(ChargementBrutalTable.class);
    private Connection connexion;
    private List<Norme> listeNorme;

    /** Retourne une requête (SELECT) contenant l'idsource et des lignes du fichier.
     * @param idSource du fichier chargé
     * @param br reader ouvert sur le fichier
     * @param nb_boucle étape dans la lecture
     * */
    private String requeteFichierBrutalement(String idSource, BufferedReader br, int nb_boucle) throws Exception {
        StaticLoggerDispatcher.info("** chargerFichierBrutalement **", LOGGER);

    	
    	StringBuilder requete=new StringBuilder();
    	int idLigne = nb_boucle * LIMIT_CHARGEMENT_BRUTAL;
    	String line = br.readLine();

    	boolean start=true;
    	while (line != null && idLigne < (nb_boucle + 1) * LIMIT_CHARGEMENT_BRUTAL) {
          if (start)
          {
    		requete.append("\nSELECT '"+idSource.replace("'", "''")+"'::text as id_source,"+ idLigne +"::int as id_ligne,'"+line.replace("'", "''")+"'::text as ligne");
    		start=false;
          }
          else
          {
      		requete.append("\nUNION ALL SELECT '"+idSource.replace("'", "''")+"',"+ idLigne +",'"+line.replace("'", "''")+"'"); 
          }
          
          idLigne++;
          if (idLigne < (nb_boucle + 1) * LIMIT_CHARGEMENT_BRUTAL) {
              line = br.readLine();
          }
       }
    	return requete.toString();

    }
    
    /** Calcule la norme. Retourne (par référence) la norme dans normeOk[0] et la validité dans validiteOk[0].
     * @throws IOException
     * @throws Exception si aucune norme ou plus d'une norme trouvée */
    public void calculeNormeAndValiditeFichiers(String idSource, InputStream file, Norme[] normeOk, String[] validiteOk)
    		throws Exception {
    	StaticLoggerDispatcher.info("** calculeNormeFichiers **", LOGGER);
    	
	    normeOk[0] = new Norme();
	    validiteOk[0]= null;
	   
	    int nbBoucle = 0;
	    boolean erreur = false;
	
	    try(InputStreamReader isr = new InputStreamReader(file);
	    		BufferedReader br = new BufferedReader(isr);) {

			// On boucle tant que l'on a pas une norme ou une exception
			// TODO: en l'état on ne boucle jamais qu'une fois. Dès la première boucle :
			// - soit la norme est trouvée et on sort
			// - soit aucune/trop de normes est/sont trouvé(s) et on sort de calculerNormeAndValidite avec une exception
			// nbBoucle<LIMIT_BOUCLE n'entre jamais en jeu.
			// Gênant si la norme utilise une ligne qui n'est pas dans les xxx premières lignes, mais choix temporaire pour éviter
			// de charger un fichier entier à la recherche de sa norme
			while (normeOk[0].getIdNorme() == null && nbBoucle<LIMIT_BOUCLE) {
	    		calculerNormeAndValidite(normeOk, validiteOk, requeteFichierBrutalement(idSource, br, nbBoucle));

	    		nbBoucle++;
	    	}

	    } catch (IOException e) {
	    	throw e;
	    }

	    if (normeOk[0].getIdNorme()==null) {
	        throw (new Exception("Aucune norme trouvée"));
	    }
	    if (erreur) {
	        throw (new Exception("Une erreur est survenu lors du calcule de la norme du fichier"));
		}
    }

    /** Retourne (par référence) la norme dans normeOk[0] et la validité dans validiteOk[0].
     * @param requeteFichier une requête contenant la description du fichier
     * @throws SQLException
     * @throws Exception si aucune norme ou plus d'une norme trouvée*/
    private void calculerNormeAndValidite(Norme[] normeOk, String[] validiteOk, String requeteFichier) throws Exception {
        StaticLoggerDispatcher.info("** calculerNorme **", LOGGER);

        StringBuilder query=new StringBuilder();
        query.append("\n WITH alias_table AS ("+requeteFichier+" )");

        query.append("\n SELECT * FROM (");
        
        for (int i=0;i<listeNorme.size();i++)
        {
        	if (i>0)
        	{
        		query.append("\n UNION ALL ");
        	}
        	query.append("\n SELECT "+i+"::int as id_norme ");
        	query.append("\n , ("+listeNorme.get(i).getDefNorme()+" LIMIT 1)::text as norme ");
        	query.append("\n , ("+listeNorme.get(i).getDefValidite()+" LIMIT 1)::text as validite ");
        }
        query.append("\n ) vv ");
        query.append("\n where norme is not null ");
        
        
        ArrayList<ArrayList<String>> result =UtilitaireDao.get("arc").executeRequestWithoutMetadata(this.connexion, new PreparedStatementBuilder(query));
        if (result.size()>1)
        {
        	throw new Exception("More than one norm match the expression");
        } else if (result.isEmpty())
        {
        	throw new Exception("Zero norm match the expression");
        }

        normeOk[0]=listeNorme.get(Integer.parseInt(result.get(0).get(0)));
        validiteOk[0]=result.get(0).get(2);
    }
    
    
    
    /**
     * @return the connexion
     */
    public Connection getConnexion() {
        return connexion;
    }

    /**
     * @param connexion
     *            the connexion to set
     */
    public void setConnexion(Connection connexion) {
        this.connexion = connexion;
    }

    /** Retourne les normes potentielles à vérifier pour le fichier.
     * @return the listeNorme
     */
    public List<Norme> getListeNorme() {
        return listeNorme;
    }

    /** Définit les normes potentielles à vérifier pour le fichier.
     * @param listeNorme
     *            the listeNorme to set
     */
    public void setListeNorme(List<Norme> listeNorme) {
        this.listeNorme = listeNorme;
    }

}
