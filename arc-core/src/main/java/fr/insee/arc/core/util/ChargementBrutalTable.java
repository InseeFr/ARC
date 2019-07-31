package fr.insee.arc.core.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import fr.insee.arc.core.model.Norme;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerDispatcher;
import fr.insee.arc.utils.utils.ManipString;


/**
 * Méthode pour charger brutalement un fichier en base. 1 ligne du fichier = une ligne en base au format texte
 * @author S4LWO8
 *
 */
public class ChargementBrutalTable {

    
	// todo : externaliser dans les regles de chargement
	// Combien de boucle au maximum
	public static final int LIMIT_BOUCLE = 1;
    // Combien de ligne on charge à la fois (OPTI)
    public static final int LIMIT_CHARGEMENT_BRUTAL = 50;
       
    public static final String TABLE_CHARGEMENT_BRUTAL = "B";
    private static final Logger LOGGER = Logger.getLogger(ChargementBrutalTable.class);
    private Connection connexion;
    private List<Norme> listeNorme;

    /**
     * Création de la table qui va accueillir la chargement brutal des fichiers
     * 
     * @throws SQLException
     */
    public void requeteCreateTableChargementBrutal(int nbBoucle) throws Exception {
        LoggerDispatcher.info("** requeteCreateTableChargementBrutal **", LOGGER);
        StringBuilder requete = new StringBuilder();
        java.util.Date beginDate = new java.util.Date();

        if (nbBoucle == 0) {
            requete.append(FormatSQL.dropTable(TABLE_CHARGEMENT_BRUTAL));
            requete.append("CREATE ");

            if (!TABLE_CHARGEMENT_BRUTAL.contains(".")) {
                requete.append("TEMPORARY ");
            } else {
                requete.append(" ");
            }

            requete.append(" TABLE " + TABLE_CHARGEMENT_BRUTAL + " (id_source text, id_ligne integer, ligne text)"
                    + " WITH (OIDS=FALSE, autovacuum_enabled = false, toast.autovacuum_enabled = false);\n");

        } else {
            requete.append("TRUNCATE TABLE " + TABLE_CHARGEMENT_BRUTAL + ";");
        }

        UtilitaireDao.get("arc").executeBlock(this.connexion, requete);
        java.util.Date endDate = new java.util.Date();

        LoggerDispatcher.info("** requeteCreateTableChargementBrutal temps : " + (endDate.getTime() - beginDate.getTime()) + " ms **", LOGGER);

    }

    /**
     * Méthode de gros bourrin, on prend le fichier et on le charge ligne par ligne en base. On utilise le COPY pour aller plus vite
     * 
     * @param idSource
     * @param br
     * @throws Exception
     */
    private boolean chargerFichierBrutalement(String idSource, BufferedReader br, int nb_boucle) throws Exception {
        LoggerDispatcher.info("** chargerFichierBrutalement **", LOGGER);
        java.util.Date beginDate = new java.util.Date();
        String delimiter = Character.toString((char) 1);
        String quote = Character.toString((char) 2);

        boolean output = false;

        String header = "id_source" + delimiter + "id_ligne" + delimiter + "ligne\n";
        int idLigne = nb_boucle * LIMIT_CHARGEMENT_BRUTAL;
        StringBuilder requete = new StringBuilder();

        String line = br.readLine();
        if (line == null) {
            output = true;
        }

        while (line != null && idLigne < (nb_boucle + 1) * LIMIT_CHARGEMENT_BRUTAL) {
            requete.append(idSource + delimiter + idLigne + delimiter + line + "\n");
            idLigne++;

            if (idLigne < (nb_boucle + 1) * LIMIT_CHARGEMENT_BRUTAL) {
                line = br.readLine();
            }

        }
        requete.insert(0, header);
        byte[] bytes = requete.toString().getBytes(StandardCharsets.UTF_8);
        InputStream is = new ByteArrayInputStream(bytes);
//        UtilitaireDao.get("arc").importing(this.connexion, TABLE_CHARGEMENT_BRUTAL, is, true, delimiter);
        UtilitaireDao.get("arc").importing(this.connexion, TABLE_CHARGEMENT_BRUTAL, null, is, true, true, delimiter, quote, null);

        is.close();

        java.util.Date endDate = new java.util.Date();
        LoggerDispatcher.info("** chargerFichierBrutalement temps : " + (endDate.getTime() - beginDate.getTime()) + " ms **", LOGGER);
        
        return output;

    }

    
    private String requeteFichierBrutalement(String idSource, BufferedReader br, int nb_boucle) throws Exception {
        LoggerDispatcher.info("** chargerFichierBrutalement **", LOGGER);

    	
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
    
    public void calculeNormeAndValiditeFichiers(String idSource, InputStream file, Norme[] normeOk, String[] validiteOk) {
    try {
    	LoggerDispatcher.info("** calculeNormeFichiers **", LOGGER);
    	
	    normeOk[0] = new Norme();
	    validiteOk[0]= null;
	   
	    int nbBoucle = 0;
	    boolean erreur = false;
	
	    InputStreamReader isr = new InputStreamReader(file);
	    BufferedReader br = new BufferedReader(isr);
	
	    //On boucle tant que l'on a pas une norme ou une erreur
	    while (normeOk[0].getIdNorme() == null && !erreur && nbBoucle<LIMIT_BOUCLE) {
//	        requeteCreateTableChargementBrutal(nbBoucle);
//	
//	        erreur = chargerFichierBrutalement(idSource, br, nbBoucle);
//	
//	    	calculerNormeAndValidite(normeOk, validiteOk);
	    	
	        calculerNormeAndValidite(normeOk, validiteOk, requeteFichierBrutalement(idSource, br, nbBoucle));

	        nbBoucle++;
	
	    }
	    
	    br.close();
	    isr.close();
	
	    if (normeOk[0].getIdNorme()==null) {
	        throw (new Exception("Aucune norme trouvée"));
	    }
	    if (erreur) {
	        throw (new Exception("Une erreur est survenu lors du calcule de la norme du fichier"));
	    }
	    
    }
    catch(Exception e)
    {
    	e.printStackTrace();
    }
    }
    
    private void calculerNormeAndValidite(Norme[] normeOk, String[] validiteOk, String b) throws Exception {
        LoggerDispatcher.info("** calculerNorme **", LOGGER);

        StringBuilder query=new StringBuilder();
//        query.append("\n WITH alias_table AS ( SELECT * FROM " + TABLE_CHARGEMENT_BRUTAL+ " )");
        query.append("\n WITH alias_table AS ("+b+" )");

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
        
        
        ArrayList<ArrayList<String>> result =UtilitaireDao.get("arc").executeRequestWithoutMetadata(this.connexion, query);
        if (result.size()>1)
        {
        	throw new Exception("More than one norm match the expression");
        }
        
        if (result.size()==0)
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

    /**
     * @return the listeNorme
     */
    public List<Norme> getListeNorme() {
        return listeNorme;
    }

    /**
     * @param listeNorme
     *            the listeNorme to set
     */
    public void setListeNorme(List<Norme> listeNorme) {
        this.listeNorme = listeNorme;
    }

}
