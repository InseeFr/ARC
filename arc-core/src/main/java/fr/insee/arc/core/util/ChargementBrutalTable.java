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

	// The file is read line by line to determine its norm
	// This process may be very long on large file whereas the information can oftenly found be at the very beginning of the file
	
	// That is why the file is chunked and the rules to determine to norm are applied every for every chunck
	// The following parameters set the maximum number of chunk to be read and the maximum amount of line in every single chunk 

	/**TODO the followings constant should become part of the load rules **/
    // The maximum number of lines loaded every loop
    public static final int LIMIT_CHARGEMENT_BRUTAL = 500;
	// The maximum number of chunk
	public static final int LIMIT_BOUCLE = 1;
    
    public static final String TABLE_CHARGEMENT_BRUTAL = "B";
    private static final Logger LOGGER = Logger.getLogger(ChargementBrutalTable.class);
    private Connection connexion;
    private List<Norme> listeNorme;

    /**
     * 
     * Pour pouvoir traiter des fichiers clef-valeur, on doit pouvoir déterminer la norme du fichier AVANT de le mettre en base pour être
     * capable de le mettre correctement en base.
     *
     * @return normage ko?
     * @param idSource
     * @param fileStream
     * @throws Exception
     */
    public Norme calculeNormeFichiers(String idSource, InputStream fileStream) throws Exception {

        LoggerDispatcher.info("** calculeNormeFichiers **", LOGGER);

        Norme normeOk = new Norme();
        int nbBoucle = 0;
        boolean erreur = false;

        InputStreamReader isr = new InputStreamReader(fileStream);
        BufferedReader br = new BufferedReader(isr);
        
        //On boucle tant que l'on a pas une norme ou une erreur
        while (normeOk.getIdNorme() == null && !erreur && nbBoucle<LIMIT_BOUCLE) {
            requeteCreateTableChargementBrutal(nbBoucle);

            erreur = chargerFichierBrutalement(idSource, br, nbBoucle);

            normeOk = calculerNorme(idSource);

            nbBoucle++;

        }
        
        br.close();
        isr.close();

        if (normeOk.getIdNorme()==null) {
            throw (new Exception("Aucune norme trouvée"));
        }
        if (erreur) {
            throw (new Exception("Une erreur est survenu lors du calcule de la norme du fichier"));
        }
        return normeOk;

    }

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
        
        // no delimiter and no quote. The line is loaded as raw data
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
        UtilitaireDao.get("arc").importing(this.connexion, TABLE_CHARGEMENT_BRUTAL, null, is, true, true, delimiter, quote, null);

        is.close();

        java.util.Date endDate = new java.util.Date();
        LoggerDispatcher.info("** chargerFichierBrutalement temps : " + (endDate.getTime() - beginDate.getTime()) + " ms **", LOGGER);
        
        return output;

    }

    /**
     * On calcule les normes d'un fichier. On retourne ça sous forme de liste. 3 cas possible : >0 norme, on a essayé toutes les normes sans
     * succès >1 norme, on a essayé toues les normes, une seule a matché >2 normes, 2 normes ont matché, il y en a peut être plus mais on
     * s'est arrêté avant car ficheir KO
     * 
     * @param idSource
     * @return
     * @throws SQLException
     */
    private Norme calculerNorme(String idSource) {
        LoggerDispatcher.info("** calculerNorme **", LOGGER);
        java.util.Date beginDate = new java.util.Date();
        
        Norme normeOk = new Norme();

        // calcule de la norme. Si plus d'une norme détectée, on sort de la boucle car le fichier sera en erreur
        int i = 0;
        UtilitaireDao.get("arc").setSilent(true);
        while (normeOk.getIdNorme() == null && i < listeNorme.size()) {
            LoggerDispatcher.info("Marquage de la norme " + listeNorme.get(i).getIdNorme(), LOGGER);

            StringBuilder requeteNormage = new StringBuilder();
            requeteNormage.append("with alias_table as (");
            requeteNormage.append("select * from " + TABLE_CHARGEMENT_BRUTAL);
            requeteNormage.append(")");
            requeteNormage.append("select 1 where exists (\n");
            requeteNormage.append(ManipString.extractAllRubrique(listeNorme.get(i).getDefNorme()));
            requeteNormage.append(");");

            // requeteNormage.append("\nCreate table arc_bas8.chargement_brutal as select * from "+this.tableChargementBrutal+";");

            try {
                ArrayList<ArrayList<String>> result = UtilitaireDao.get("arc").executeRequest(this.connexion, requeteNormage);

                if (UtilitaireDao.hasResults(UtilitaireDao.get("arc").executeRequest(this.connexion, requeteNormage))) {
                    normeOk = listeNorme.get(i);
                }

            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            i++;
            UtilitaireDao.get("arc").setSilent(false);
        }
        
        java.util.Date endDate = new java.util.Date();
        LoggerDispatcher.info("** calculerNorme temps : " + (endDate.getTime() - beginDate.getTime()) + " ms **", LOGGER);
        
        return normeOk;

    }

    /**
     * Rebelotte. On fait comme pour la norme, mais avec la validité (refactor possible ?)
     * @param idSource
     * @param file
     * @param norme
     * @return
     * @throws Exception
     */
    public String calculeValiditeFichiers(String idSource, InputStream file, Norme norme) throws Exception {
        LoggerDispatcher.info("** calculeValiditeFichiers **", LOGGER);
        java.util.Date beginDate = new java.util.Date();
        
        String returned = null;
        int nbBoucle = 0;
        boolean erreur = false;

        InputStreamReader isr = new InputStreamReader(file);
        BufferedReader br = new BufferedReader(isr);

        while (StringUtils.isEmpty(returned) && !erreur && nbBoucle<LIMIT_BOUCLE) {
            requeteCreateTableChargementBrutal(nbBoucle);

            erreur = chargerFichierBrutalement(idSource, br, nbBoucle);

            returned = calculerValidite(idSource, norme);

            nbBoucle++;

        }

        br.close();
        isr.close();

        if (erreur) {
            throw (new Exception("aucune norme trouvée"));
        }
        
        
        java.util.Date endDate = new java.util.Date();
        LoggerDispatcher.info("** calculeValiditeFichiers temps : " + (endDate.getTime() - beginDate.getTime()) + " ms **", LOGGER);
        
        return returned;

    }

    /**
     * On calcule la validite d'un fichier.
     * 
     * @param idSource
     * @return
     * @throws SQLException
     */
    private String calculerValidite(String idSource, Norme norme) {
        LoggerDispatcher.info("** calculerValidite **", LOGGER);
        java.util.Date beginDate = new java.util.Date();

        
        String returned = "";

        // calcule de la norme. Si plus d'une norme détectée, on sort de la boucle car le fichier sera en erreur
        int i = 0;
        UtilitaireDao.get("arc").setSilent(true);

        StringBuilder validite = new StringBuilder();
        validite.append("with alias_table as (");
        validite.append("select * from " + TABLE_CHARGEMENT_BRUTAL);
        validite.append(")");
        validite.append(norme.getDefValidite());

        // requeteNormage.append("\nCreate table arc_bas8.chargement_brutal as select * from "+this.tableChargementBrutal+";");

        try {
            ArrayList<ArrayList<String>> result = UtilitaireDao.get("arc").executeRequest(this.connexion, validite);

            if (UtilitaireDao.hasResults(UtilitaireDao.get("arc").executeRequest(this.connexion, validite))) {
                returned = result.get(2).get(0);
            }

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        UtilitaireDao.get("arc").setSilent(false);

        
        java.util.Date endDate = new java.util.Date();
        LoggerDispatcher.info("** calculerValidite temps : " + (endDate.getTime() - beginDate.getTime()) + " ms **", LOGGER);
        
        return returned;

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
