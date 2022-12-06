package fr.insee.arc.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;

/**
 * Chargement brutalement d'un fichier pour déterminer la norme et la validité associées.
 * @author S4LWO8
 *
 */
public class ChargementBrutalTable {

    
	/** Combien de boucle au maximum */
	private static final int LIMIT_BOUCLE = 1;
    /** Combien de ligne on charge pour chacune des boucles */
    private static final int LIMIT_CHARGEMENT_BRUTAL = 50;
       
    private static final Logger LOGGER = LogManager.getLogger(ChargementBrutalTable.class);
    private Connection connexion;
    private List<Norme> listeNorme;

    /** Retourne une requête (SELECT) contenant l'idsource et des lignes du fichier.
     * @param idSource du fichier chargé
     * @param br reader ouvert sur le fichier
     * @param nbBoucle étape dans la lecture
     * */
    private String requeteFichierBrutalement(String idSource, BufferedReader br, int nbBoucle) throws ArcException {
        StaticLoggerDispatcher.info("** chargerFichierBrutalement **", LOGGER);

    	
    	StringBuilder requete=new StringBuilder();
    	int idLigne = nbBoucle * LIMIT_CHARGEMENT_BRUTAL;
    	String line;
		try {
			line = br.readLine();
		} catch (IOException e) {
    		throw new ArcException("File line cannot be read",e);
		}
    	if (line == null) {
    		throw new ArcException("The file is empty.");
    	}
    	boolean start=true;
    	while (line != null && idLigne < (nbBoucle + 1) * LIMIT_CHARGEMENT_BRUTAL) {
          if (start)
          {
    		requete.append("\nSELECT '"+idSource.replace("'", "''")+"'::text as "+ColumnEnum.ID_SOURCE.getColumnName()+","+ idLigne +"::int as id_ligne,'"+line.replace("'", "''")+"'::text as ligne");
    		start=false;
          }
          else
          {
      		requete.append("\nUNION ALL SELECT '"+idSource.replace("'", "''")+"',"+ idLigne +",'"+line.replace("'", "''")+"'"); 
          }
          
          idLigne++;
          if (idLigne < (nbBoucle + 1) * LIMIT_CHARGEMENT_BRUTAL) {
              try {
				line = br.readLine();
			} catch (IOException e) {
	    		throw new ArcException("File line cannot be read",e);
			}
          }
       }
    	return requete.toString();

    }
    
    /** Calcule la norme. Retourne (par référence) la norme dans normeOk[0] et la validité dans validiteOk[0].
     * @throws IOException
     * @throws ArcException si aucune norme ou plus d'une norme trouvée */
    public void calculeNormeAndValiditeFichiers(String idSource, InputStream file, Norme[] normeOk, String[] validiteOk)
    		throws ArcException {
    	StaticLoggerDispatcher.info("** calculeNormeFichiers **", LOGGER);
    	
	    normeOk[0] = new Norme();
	    validiteOk[0]= null;
	   
	    int nbBoucle = 0;
	
	    try(InputStreamReader isr = new InputStreamReader(file);
	    		BufferedReader br = new BufferedReader(isr);) {

			// On boucle tant que l'on a pas une norme ou une exception
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
	    	throw new ArcException("Erreur de lecture du fichier " + idSource , e);
	    }

	    if (normeOk[0].getIdNorme()==null) {
	        throw new ArcException("Aucune norme trouvée");
	    }
    }

    /** Retourne (par référence) la norme dans normeOk[0] et la validité dans validiteOk[0].
     * @param requeteFichier une requête contenant la description du fichier
     * @throws ArcException
     * @throws ArcException 
     * @throws ArcException si aucune norme ou plus d'une norme trouvée*/
    private void calculerNormeAndValidite(Norme[] normeOk, String[] validiteOk, String requeteFichier) throws ArcException {
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

        ArrayList<ArrayList<String>> result =UtilitaireDao.get("arc").executeRequestWithoutMetadata(this.connexion, new ArcPreparedStatementBuilder(query));
        if (result.size()>1)
        {
        	StringBuilder normsFound = new StringBuilder();
        	for (ArrayList<String> resultLine : result) 
        	{
	    		int index = Integer.parseInt(resultLine.get(0));
	    		normsFound.append("{");
	    		normsFound.append(listeNorme.get(index).getIdNorme());
	    		normsFound.append(", ");
	    		normsFound.append(resultLine.get(2));
	    		normsFound.append("}");
        	}
        	throw new ArcException("More than one norm and/or validity match the expression:" + normsFound);
        } else if (result.isEmpty())
        {
        	throw new ArcException("Zero norm and/or validity match the expression");
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
