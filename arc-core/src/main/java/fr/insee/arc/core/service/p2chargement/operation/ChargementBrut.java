package fr.insee.arc.core.service.p2chargement.operation;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.List;

import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.utils.dao.SQL;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.service.global.bo.FileIdCard;
import fr.insee.arc.core.service.p2chargement.bo.NormeRules;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.files.BoundedBufferedReader;
import fr.insee.arc.utils.utils.FormatSQL;

/**
 * Chargement brutalement d'un fichier pour déterminer la norme et la validité associées.
 * @author S4LWO8
 *
 */
public class ChargementBrut {

    protected int maxNumberOfLinesToRead = 50;
    protected int maxNumberOfCharacterByLineToRead = 10000;
      
    private static final Logger LOGGER = LogManager.getLogger(ChargementBrut.class);
    private Connection connexion;
    private List<NormeRules> listeNorme;

    /** Retourne une requête (SELECT) contenant l'idsource et des lignes du fichier.
     * @param idSource du fichier chargé
     * @param br reader ouvert sur le fichier
     * @param nbBoucle étape dans la lecture
     * */
    protected String requeteFichierBrutalement(String idSource, BoundedBufferedReader br) throws ArcException {
        StaticLoggerDispatcher.info(LOGGER, "** chargerFichierBrutalement **");
    	
    	StringBuilder requete=new StringBuilder();
    	int idLigne = 0;
    	String line;
		try {
			line = br.readLine(maxNumberOfCharacterByLineToRead);
		} catch (IOException e) {
    		throw new ArcException(e, ArcExceptionMessage.FILE_READ_FAILED, idSource);
		}
    	if (line == null) {
    		throw new ArcException(ArcExceptionMessage.FILE_IS_EMPTY, idSource);
    	}
    	
    	boolean start=true;
    	while (line != null && idLigne < maxNumberOfLinesToRead) {
          if (start)
          {
    		requete.append("\nSELECT "+FormatSQL.quoteText(idSource)+"::text as "+ColumnEnum.ID_SOURCE.getColumnName()+","+ idLigne +"::int as id_ligne,"+FormatSQL.quoteText(line)+"::text as ligne");
    		start=false;
          }
          else
          {
      		requete.append("\nUNION ALL SELECT "+FormatSQL.quoteText(idSource)+","+ idLigne +","+FormatSQL.quoteText(line)+""); 
          }
          
          idLigne++;
          if (idLigne < maxNumberOfLinesToRead) {
              try {
				line = br.readLine(maxNumberOfCharacterByLineToRead);
			} catch (IOException e) {
	    		throw new ArcException(e, ArcExceptionMessage.FILE_READ_FAILED, idSource);
			}
          }
       }
    	return requete.toString();

    }
    
    /** Calcule la norme. Retourne (par référence) la norme dans normeOk[0] et la validité dans validiteOk[0].
     * @throws IOException
     * @throws ArcException si aucune norme ou plus d'une norme trouvée, ou si le fichier est hors calendrier */
    public void calculeNormeAndValiditeFichiers(InputStream file, FileIdCard normeOk, String envExecution)
    		throws ArcException {
    	StaticLoggerDispatcher.info(LOGGER, "** calculeNormeFichiers **");

	    try(InputStreamReader isr = new InputStreamReader(file);
	    		BoundedBufferedReader br = new BoundedBufferedReader(isr);) {

			// - soit la norme est trouvée et on sort
			// - soit aucune/trop de normes est/sont trouvé(s) et on sort de calculerNormeAndValidite avec une exception
			// Gênant si la norme utilise une ligne qui n'est pas dans les xxx premières lignes, mais choix temporaire pour éviter
			// de charger un fichier entier à la recherche de sa norme
    		calculerNormeAndValidite(normeOk, requeteFichierBrutalement(normeOk.getIdSource(), br));

	    } catch (IOException e) {
	    	throw new ArcException(e, ArcExceptionMessage.FILE_READ_FAILED, normeOk.getIdSource());
	    }

	    if (normeOk.getIdNorme()==null) {
	        throw new ArcException(ArcExceptionMessage.LOAD_NORM_NOT_FOUND, normeOk.getIdSource());
	    }
	    
	    if (estHorsCalendrier(normeOk, envExecution)) {
            throw new ArcException(ArcExceptionMessage.LOAD_NORM_OUT_OF_CALENDAR, normeOk.getIdNorme(), normeOk.getIdSource());
        }
	    
    }

    /** Retourne (par référence) la norme dans normeOk[0] et la validité dans validiteOk[0].
     * @param requeteFichier une requête contenant la description du fichier
     * @throws ArcException
     * @throws ArcException 
     * @throws ArcException si aucune norme ou plus d'une norme trouvée*/
    private void calculerNormeAndValidite(FileIdCard normeOk, String requeteFichier) throws ArcException {
        StaticLoggerDispatcher.info(LOGGER, "** calculerNorme **");

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

        List<List<String>> result =UtilitaireDao.get(0).executeRequestWithoutMetadata(this.connexion, new ArcPreparedStatementBuilder(query));
        if (result.size()>1)
        {
        	StringBuilder normsFound = new StringBuilder();
        	for (List<String> resultLine : result) 
        	{
	    		int index = Integer.parseInt(resultLine.get(0));
	    		normsFound.append("{");
	    		normsFound.append(listeNorme.get(index).getIdNorme());
	    		normsFound.append(", ");
	    		normsFound.append(resultLine.get(2));
	    		normsFound.append("}");
        	}
        	throw new ArcException(ArcExceptionMessage.LOAD_SEVERAL_NORM_FOUND, normsFound);
        } else if (result.isEmpty())
        {
        	throw new ArcException(ArcExceptionMessage.LOAD_ZERO_NORM_FOUND);
        }

        NormeRules normFound = listeNorme.get(Integer.parseInt(result.get(0).get(0)));
        
        normeOk.setFileIdCard(normFound.getIdNorme(), result.get(0).get(2), normFound.getPeriodicite(), null);

    }

    /** Retourne VRAI si la date de validité du fichier est hors de la période de validité de la norme du fichier.
     * Retourne FAUX sinon.
     * @param normeOk les métadonnées du fichier à vérifier
     * @param envExecution le schéma d'exécution du traitement
     * @throws ArcException si aucune norme ou plus d'une norme trouvée
     */
    private boolean estHorsCalendrier(FileIdCard normeOk, String envExecution) throws ArcException {
        ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
        query.build(SQL.SELECT, ColumnEnum.VALIDITE_INF.getColumnName(), ",");
        query.build(ColumnEnum.VALIDITE_SUP.getColumnName(), SQL.FROM, ViewEnum.CALENDRIER.getFullName(envExecution));
        query.build(SQL.WHERE, ColumnEnum.ID_NORME.getColumnName(), "=", query.quoteText(normeOk.getIdNorme()));

        List<List<String>> result = UtilitaireDao.get(0).executeRequestWithoutMetadata(this.connexion, query);

        if (result.size() > 1) {
            throw new ArcException(ArcExceptionMessage.LOAD_SEVERAL_NORM_FOUND, normeOk.getIdNorme());
        } else if (result.isEmpty()) {
            throw new ArcException(ArcExceptionMessage.LOAD_ZERO_NORM_FOUND);
        }

        String validite_inf = result.get(0).get(0);
        String validite_sup = result.get(0).get(1);
        String validite = normeOk.getValidite();
        return (validite.compareTo(validite_inf) < 0) || (validite.compareTo(validite_sup) > 0);

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
    public List<NormeRules> getListeNorme() {
        return listeNorme;
    }

    /** Définit les normes potentielles à vérifier pour le fichier.
     * @param listeNorme
     *            the listeNorme to set
     */
    public void setListeNorme(List<NormeRules> listeNorme) {
        this.listeNorme = listeNorme;
    }

}
