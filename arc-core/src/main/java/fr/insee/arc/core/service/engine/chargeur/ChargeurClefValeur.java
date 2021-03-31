package fr.insee.arc.core.service.engine.chargeur;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.service.ApiService;
import fr.insee.arc.core.service.thread.ThreadChargementService;
import fr.insee.arc.core.util.ArbreFormat;
import fr.insee.arc.core.util.Norme;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.utils.ManipString;
import fr.insee.arc.core.util.StaticLoggerDispatcher;

/**
 * Classe convertissant les fichier clef-valeurs en fichier xml
 * 
 * @author S4LWO8
 *
 */
public class ChargeurClefValeur implements IChargeur {
    private static final Logger LOGGER = LogManager.getLogger(ChargeurClefValeur.class);

    private Norme normeOk;
    private String separateur = ",";
    private String envExecution;
    private String idSource;
    private PipedOutputStream outputStream;
    private String paramBatch;
    private InputStream tmpInxChargement;
    private String tableChargementPilTemp;
    private String fileName;
    private String currentPhase;
    private Connection connexion;
    private ChargeurXml chargeurXml;

    public ChargeurClefValeur(ThreadChargementService threadChargementService, String fileName) {
        super();

        this.fileName = fileName;
        this.connexion = threadChargementService.getConnexion();
        this.tableChargementPilTemp = threadChargementService.getTableChargementPilTemp();
        this.currentPhase = threadChargementService.getCurrentPhase();
        this.tmpInxChargement = threadChargementService.filesInputStreamLoad.getTmpInxChargement();
        this.normeOk = threadChargementService.normeOk;
        this.separateur = this.normeOk.getRegleChargement().getDelimiter();
        this.chargeurXml = new ChargeurXml(threadChargementService, fileName);
        this.envExecution = threadChargementService.getEnvExecution();
    }

    /**
     * Méthode pour charger des fichiers clef-valeur
     * 
     * @param entrepot
     * @param currentEntryChargementName
     * @param tmpInxChargement
     * @param normeOk
     * @throws Exception
     */
    private void chargerClefValeur() throws Exception {
        String rapport = "";
        StringBuilder requeteBilan = new StringBuilder();

        outputStream = new PipedOutputStream();
        PipedInputStream input = null;

        try {
            input = new PipedInputStream(outputStream);

            Thread KVtoXMLThread = new Thread(new Runnable() {

                @Override
                public void run() {



                    try {
                        // On lit le fichier format et on en retire une map (rubrique, père)
                        ArbreFormat arbreFormat = new ArbreFormat(getNormeOk());
                        
                        // Lire le fichier d'entrée et le convertir en fichier XML. Pour ne pas écrire dans la mémoire on transforme un
                        // stream en un autre stream

                        clefValeurToXml(arbreFormat.getArbreFormat(), tmpInxChargement);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } finally {
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            });

            KVtoXMLThread.start();
            chargeurXml.setF(input);

            chargeurXml.charger();
        } catch (Exception e) {

            e.printStackTrace();
            rapport = e.getMessage().replace("'", "''");
            requeteBilan.append(ApiService.pilotageMarkIdsource(this.tableChargementPilTemp, fileName, this.currentPhase, TraitementEtat.KO.toString(),
                    rapport));
            try {
                UtilitaireDao.get("arc").executeBlock(this.connexion, requeteBilan);
            } catch (SQLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        } finally {
            try {
                if(input!= null) input.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                rapport = e.getMessage().replace("'", "''");
                requeteBilan.append(ApiService.pilotageMarkIdsource(this.tableChargementPilTemp, fileName, this.currentPhase, TraitementEtat.KO.toString(),
                        rapport));
            }
        }
    }

    /**
     * Converti le fichier clef valeur en xml sous la forme d'un outputStream.
     * 
     * @param arbreFormat
     * @param tmpInx2
     * @return un outputStream contenant une version xml de l'inputStream
     * @author S4LWO8
     * @throws Exception
     */
    public void clefValeurToXml(HashMap<String, String> arbreFormat, InputStream tmpInx2) throws Exception {
        StaticLoggerDispatcher.info("** Conversion du fichier clef valeur en XML **", LOGGER);
        java.util.Date beginDate = new java.util.Date();
        // contient la liste des pères pour l'élément précédent
        ArrayList<String> listePeresRubriquePrecedante = new ArrayList<String>();

        // contient la liste des rubriques vues au niveau pour chaque balise encore ouverte.
        // clef : rubrique/ valeur : fils ouvert
        HashMap<String, ArrayList<String>> mapRubriquesFilles = new HashMap<String, ArrayList<String>>();

        // Lecture du fichier contenant les données et écriture d'un fichier xml
        InputStreamReader inputStreamReader = new InputStreamReader(tmpInx2, "ISO-8859-1");
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        // On lit le fichier ligne par ligne
        String ligne = bufferedReader.readLine();

        // initialisation du fichier
        listePeresRubriquePrecedante = initialisationOutputStream(arbreFormat, mapRubriquesFilles, ligne, getOutputStream());

        ligne = bufferedReader.readLine();
        // int nbLignesLues = 0;
        while (ligne != null) {

            listePeresRubriquePrecedante = lectureLigne(arbreFormat, listePeresRubriquePrecedante, mapRubriquesFilles, ligne, getOutputStream());
            // nbLignesLues++;
            // System.out.println(ligne);
            ligne = bufferedReader.readLine();

        }

        finaliserOutputStream(listePeresRubriquePrecedante, getOutputStream());
        bufferedReader.close();

        java.util.Date endDate = new java.util.Date();
        StaticLoggerDispatcher.info("** clefValeurToXml temps : " + (endDate.getTime() - beginDate.getTime()) + " ms **", LOGGER);
    }

    /**
     * Initialiser le xml + lit la première ligne pour ouvrir les premières balises
     * 
     * @param arbreFormat
     * @param listePeresRubriqueCourante
     * @param mapRubriquesFilles
     * @param ligne
     * @param bw
     * @throws Exception
     */
    public ArrayList<String> initialisationOutputStream(HashMap<String, String> arbreFormat, HashMap<String, ArrayList<String>> mapRubriquesFilles,
            String ligne, PipedOutputStream output) throws Exception {
        // ecriture de l'entete du fichier

        ecrireXML("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");

        // bw.write("<N4DS>\n");

        String rubrique = ManipString.substringBeforeFirst(ligne, separateur);
        String donnee = ManipString.substringAfterFirst(ligne, separateur);

        // On retire les quotes de début et fin de manière violente
        donnee = donnee.substring(1, donnee.length() - 1);

        // Echappement caractère spéciaux
        donnee = StringEscapeUtils.escapeXml11(donnee);

        if (!arbreFormat.containsKey(rubrique)) {
            throw new Exception("La rubrique fille " + rubrique + " n'existe pas dans le fichier format");
        }
        ArrayList<String> listePeresRubriqueCourante = new ArrayList<String>();
        // On remonte dans l'arbre des pères jusqu'à la racine
        while (rubrique != null) {
            listePeresRubriqueCourante.add(rubrique);
            rubrique = arbreFormat.get(rubrique);
        }

        // On ouvre les premières balises
        for (int i = listePeresRubriqueCourante.size() - 1; i > 0; i--) {
            String rubriqueCourante = listePeresRubriqueCourante.get(i);
            // System.out.println("On ouvre le tag :" + pereCourant);
            ecrireXML("<" + rubriqueCourante + ">\n");

            // on initialise la map des rubriques ouvertes
            mapRubriquesFilles.put(rubriqueCourante, new ArrayList<String>());
        }

        String rubriqueCourante = listePeresRubriqueCourante.get(0);
        mapRubriquesFilles.get(listePeresRubriqueCourante.get(1)).add(rubriqueCourante);
        // System.out.println("On ouvre le tag :" + pereCourant);

        ecrireXML("<" + rubriqueCourante + ">" + donnee + "</" + rubriqueCourante + ">\n");

        return listePeresRubriqueCourante;
    }

    /**
     * Méthode pour lire les lignes. Utiliser le plus petit père commun pour savoir quelles balises doivent être fermées et quelles doivent
     * être ouvertes.
     * 
     * @param arbreFormat
     * @param listePeresRubriqueCourante
     * @param listePeresRubriquePrecedante
     * @param mapRubriquesFilles
     * @param ligne
     * @param bw
     * @throws Exception
     */
    public ArrayList<String> lectureLigne(HashMap<String, String> arbreFormat,
            ArrayList<String> listePeresRubriquePrecedante, HashMap<String, ArrayList<String>> mapRubriquesFilles, String ligne,
            PipedOutputStream output)
            throws Exception {
        String rubrique;
        String donnee;
        String pere;
        String rubriqueCourante;

        rubrique = ManipString.substringBeforeFirst(ligne, separateur);
        donnee = ManipString.substringAfterFirst(ligne, separateur);

        // On retire les quotes de début et fin de manière violente
        donnee = donnee.substring(1, donnee.length() - 1);

        // Echappement caractère spéciaux
        donnee = StringEscapeUtils.escapeXml11(donnee);

        // On récupère la liste des pères.
        pere = rubrique;
        // StaticLoggerDispatcher.info("rubrique " + rubrique, LOGGER);
        // On verifi si la rubrique existe bien dans notre arbre format. Sinon on lance un exception
        if (!arbreFormat.containsKey(pere)) {
            throw new Exception("La rubrique fille " + rubrique + " n'existe pas dans le fichier format");
        }
        ArrayList<String> listePeresRubriqueCourante = new ArrayList<String>();
        while (pere != null) {
            listePeresRubriqueCourante.add(pere);
            pere = arbreFormat.get(pere);
        }

        // On compare avec la liste pere precedent du fichier (ie les balises encore ouverte)
        // avec celle actuelle la recherche du plus petit pere commun (merci manu).
        // On parcourt la liste des père courant dans un sens en regardant si chaque élément ce trouve dans la liste des père précédants.
        // Si on trouve pas l'élément => il faut fermer la baliser
        // Si on le trouve : on parcourt la liste actuelle dans l'autre sens et on ouvre les balises
        // Exemple
        // listePerePrecedant (a.1.1, a.1, a, root)
        // > => => |
        // v <= <= v
        // listePereCourant (a.2.1, a.2, a, root)

        if (listePeresRubriquePrecedante.size() != 0) {
            int i = 1;

            // on parcourt la listePerePrecedant pour fermer les balises
            // On commence à 1 car on a déjà fermé la 1ière balise
            // Des qu'on trouve un élément dans les 2 listes, indexOf!=-1

            int indexOf = listePeresRubriqueCourante.indexOf(listePeresRubriquePrecedante.get(i));
            while (indexOf == -1) {

                rubriqueCourante = listePeresRubriquePrecedante.get(i);

                ecrireXML("</" + rubriqueCourante + ">\n");

                mapRubriquesFilles.remove(rubriqueCourante);
                i++;
                indexOf = listePeresRubriqueCourante.indexOf(listePeresRubriquePrecedante.get(i));
            }

            // on parcourt listePereCourant dans l'autre sens à partir de l'index trouvé
            for (int j = indexOf - 1; j > -1; j--) {
                rubriqueCourante = listePeresRubriqueCourante.get(j);
                if (j == 0) {
                    // On est arrivé au dernier fils. Si on a déjà rencontré ce fils depuis la dernière fermeture
                    // de tag on doit fermer le père courant avant de rajouter le fils

                    if (mapRubriquesFilles.get(listePeresRubriqueCourante.get(1)).contains(rubriqueCourante)) {
                        // On ferme et on ouvre la balise pere de la rubrique actuelle sinon on aurait un doublon
                        mapRubriquesFilles.get(listePeresRubriqueCourante.get(1)).clear();
                        ecrireXML("</" + listePeresRubriqueCourante.get(1) + ">\n");
                        ecrireXML("<" + listePeresRubriqueCourante.get(1) + ">\n");

                    }

                    ecrireXML("<" + rubriqueCourante + ">" + donnee + "</" + rubriqueCourante + ">\n");

                    mapRubriquesFilles.get(listePeresRubriqueCourante.get(j + 1)).add(rubriqueCourante);
                } else {
                    // On ouvre la rubrique sans y mettre de donnée car ce n'est pas un fils "terminal"
                    mapRubriquesFilles.put(rubriqueCourante, new ArrayList<String>());

                    ecrireXML("<" + rubriqueCourante + ">\n");

                }
            }
        }
        return listePeresRubriqueCourante;
    }

    /**
     * Finaliser le xml en fermant les dernières balises
     * 
     * @param listePeresRubriqueCourante
     * @param br
     * @param bw
     * @throws IOException
     */
    public void finaliserOutputStream(ArrayList<String> listePeresRubriqueCourante, PipedOutputStream output) throws IOException {

        String rubriqueCourante;
        for (int i = 1; i < listePeresRubriqueCourante.size(); i++) {
            rubriqueCourante = listePeresRubriqueCourante.get(i);
            ecrireXML("</" + rubriqueCourante + ">\n");

        }

    }

    private void ecrireXML(String donnee) throws IOException {
        getOutputStream().write((donnee).getBytes());
    }

    public Norme getNormeOk() {
        return normeOk;
    }

    public void setNormeOk(Norme normeOk) {
        this.normeOk = normeOk;
    }

    public String getEnvExecution() {
        return envExecution;
    }

    public void setEnvExecution(String envExecution) {
        this.envExecution = envExecution;
    }

    public String getIdSource() {
        return idSource;
    }

    public void setIdSource(String idSource) {
        this.idSource = idSource;
    }

    public String getParamBatch() {
        return paramBatch;
    }

    public void setParamBatch(String paramBatch) {
        this.paramBatch = paramBatch;
    }

    public PipedOutputStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(PipedOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public void initialisation() {
        // TODO Auto-generated method stub

    }

    @Override
    public void finalisation() {
        // TODO Auto-generated method stub

    }

    @Override
    public void execution() throws Exception {
        chargerClefValeur();

    }

    @Override
    public void charger() throws Exception {
        initialisation();
        execution();
        finalisation();

    }

}
