package fr.insee.arc.core.service.p2chargement.engine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.service.p2chargement.bo.Norme;
import fr.insee.arc.core.service.p2chargement.thread.ThreadChargementService;
import fr.insee.arc.core.service.p2chargement.xmlhandler.ArbreFormat;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.ManipString;

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
    private ChargeurXml chargeurXml;

    public ChargeurClefValeur(ThreadChargementService threadChargementService, String fileName) {
        super();

        this.tmpInxChargement = threadChargementService.filesInputStreamLoad.getTmpInxChargement();
        this.normeOk = threadChargementService.normeOk;
        this.separateur = this.normeOk.getRegleChargement().getDelimiter();
        this.chargeurXml = new ChargeurXml(threadChargementService, fileName);
        this.envExecution = threadChargementService.getEnvExecution();
    }
    
    private class KeyValueSubLoader implements Runnable {
    	private Exception exceptionThrown = null;

        @Override
        public void run() {

            try {
                // On lit le fichier format et on en retire une map (rubrique, père)
                ArbreFormat arbreFormat = new ArbreFormat(getNormeOk());
                
                // Lire le fichier d'entrée et le convertir en fichier XML. Pour ne pas écrire dans la mémoire on transforme un
                // stream en un autre stream

                clefValeurToXml(arbreFormat.getArbreFormat(), tmpInxChargement);
            } catch (Exception parseFileException) {
                exceptionThrown = parseFileException;
            } finally {
                try {
                    outputStream.close();
                } catch (IOException closeFileException) {
                    exceptionThrown = closeFileException;
                    LoggerHelper.errorAsComment(LOGGER, "ChargeurCleValeur.run - xml conversion failed for IO reason");
                }
            }
        }
        
        public Optional<Exception> getExceptionThrown() {
        	return Optional.ofNullable(exceptionThrown);
        }
    }

    /**
     * Loads key-value files
     * @throws ArcException 
     */
    private void chargerClefValeur() throws ArcException  {
        outputStream = new PipedOutputStream();

        KeyValueSubLoader kRunnable = new KeyValueSubLoader();
        try (PipedInputStream input = new PipedInputStream(outputStream)){

            Thread keyValtoXmlThread = new Thread(kRunnable);
            keyValtoXmlThread.start();
            chargeurXml.setF(input);

            chargeurXml.charger();
        } catch (Exception e) {        	
        	/** If an error occurred in the key-value to XML conversion, we assume this is the real error.*/
            Optional<Exception> exceptionThrown = kRunnable.getExceptionThrown();
            if (exceptionThrown.isPresent()) {
                throw new ArcException(exceptionThrown.get(), ArcExceptionMessage.XML_KEYVALUE_CONVERSION_FAILED, this.idSource);
            }
            throw new ArcException(e, ArcExceptionMessage.XML_KEYVALUE_CONVERSION_FAILED, this.idSource);
        }
    }

    /**
     * Converti le fichier clef valeur en xml sous la forme d'un outputStream.
     * 
     * @param arbreFormat
     * @param tmpInx2
     * @return un outputStream contenant une version xml de l'inputStream
     * @author S4LWO8
     * @throws IOException 
     * @throws ArcException 
     */
    private void clefValeurToXml(HashMap<String, String> arbreFormat, InputStream tmpInx2) throws ArcException {
        StaticLoggerDispatcher.info(LOGGER, "** Conversion du fichier clef valeur en XML **");
        java.util.Date beginDate = new java.util.Date();
        // contient la liste des pères pour l'élément précédent
        ArrayList<String> listePeresRubriquePrecedante = new ArrayList<>();

        // contient la liste des rubriques vues au niveau pour chaque balise encore ouverte.
        // clef : rubrique/ valeur : fils ouvert
        HashMap<String, ArrayList<String>> mapRubriquesFilles = new HashMap<>();

        // Lecture du fichier contenant les données et écriture d'un fichier xml
		InputStreamReader inputStreamReader = new InputStreamReader(tmpInx2, StandardCharsets.ISO_8859_1);

        try
        (
        		BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        )
        {
	        // On lit le fichier ligne par ligne
	        String ligne = bufferedReader.readLine();
	
	        // initialisation du fichier
	        listePeresRubriquePrecedante = initialisationOutputStream(arbreFormat, mapRubriquesFilles, ligne);
	
	        ligne = bufferedReader.readLine();
	        while (ligne != null) {
	
	            listePeresRubriquePrecedante = lectureLigne(arbreFormat, listePeresRubriquePrecedante, mapRubriquesFilles, ligne);
	            ligne = bufferedReader.readLine();
	
	        }
	
	        finaliserOutputStream(listePeresRubriquePrecedante);
        } catch (IOException readFileException) {
			throw new ArcException(readFileException, ArcExceptionMessage.FILE_READ_FAILED, this.idSource);
		}

        java.util.Date endDate = new java.util.Date();
        StaticLoggerDispatcher.info(LOGGER, "** clefValeurToXml temps : " + (endDate.getTime() - beginDate.getTime()) + " ms **");
    }

    /**
     * Initialiser le xml + lit la première ligne pour ouvrir les premières balises
     * 
     * @param arbreFormat
     * @param mapRubriquesFilles
     * @param ligne
     * @param listePeresRubriqueCourante
     * @param bw
     * @throws IOException 
     * @throws ArcException 
     */
    private ArrayList<String> initialisationOutputStream(HashMap<String, String> arbreFormat, HashMap<String, ArrayList<String>> mapRubriquesFilles,
            String ligne) throws ArcException {
        // ecriture de l'entete du fichier

        ecrireXML("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");

        String rubrique = ManipString.substringBeforeFirst(ligne, separateur);
        String donnee = ManipString.substringAfterFirst(ligne, separateur);

        // On retire les quotes de début et fin de manière violente
        donnee = donnee.substring(1, donnee.length() - 1);

        // Echappement caractère spéciaux
        donnee = StringEscapeUtils.escapeXml11(donnee);

        if (!arbreFormat.containsKey(rubrique)) {
            throw new ArcException(ArcExceptionMessage.LOAD_KEYVALUE_VAR_NOT_EXISTS_IN_FORMAT_RULES, rubrique);
        }
        ArrayList<String> listePeresRubriqueCourante = new ArrayList<>();
        // On remonte dans l'arbre des pères jusqu'à la racine
        while (rubrique != null) {
            listePeresRubriqueCourante.add(rubrique);
            rubrique = arbreFormat.get(rubrique);
        }

        // On ouvre les premières balises
        for (int i = listePeresRubriqueCourante.size() - 1; i > 0; i--) {
            String rubriqueCourante = listePeresRubriqueCourante.get(i);
            ecrireXML("<" + rubriqueCourante + ">\n");

            // on initialise la map des rubriques ouvertes
            mapRubriquesFilles.put(rubriqueCourante, new ArrayList<>());
        }

        String rubriqueCourante = listePeresRubriqueCourante.get(0);
        mapRubriquesFilles.get(listePeresRubriqueCourante.get(1)).add(rubriqueCourante);

        ecrireXML("<" + rubriqueCourante + ">" + donnee + "</" + rubriqueCourante + ">\n");

        return listePeresRubriqueCourante;
    }

    /**
     * Méthode pour lire les lignes. Utiliser le plus petit père commun pour savoir quelles balises doivent être fermées et quelles doivent
     * être ouvertes.
     * 
     * @param arbreFormat
     * @param listePeresRubriquePrecedante
     * @param mapRubriquesFilles
     * @param ligne
     * @param listePeresRubriqueCourante
     * @param bw
     * @throws IOException 
     * @throws ArcException 
     */
    private ArrayList<String> lectureLigne(HashMap<String, String> arbreFormat,
            ArrayList<String> listePeresRubriquePrecedante, HashMap<String, ArrayList<String>> mapRubriquesFilles, String ligne) 
            		throws ArcException
            {
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
            throw new ArcException(ArcExceptionMessage.LOAD_KEYVALUE_VAR_NOT_EXISTS_IN_FORMAT_RULES, rubrique);
        }
        ArrayList<String> listePeresRubriqueCourante = new ArrayList<>();
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

        if (!listePeresRubriquePrecedante.isEmpty()) {
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
                    mapRubriquesFilles.put(rubriqueCourante, new ArrayList<>());

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
    private void finaliserOutputStream(ArrayList<String> listePeresRubriqueCourante) throws ArcException {

        String rubriqueCourante;
        for (int i = 1; i < listePeresRubriqueCourante.size(); i++) {
            rubriqueCourante = listePeresRubriqueCourante.get(i);
            ecrireXML("</" + rubriqueCourante + ">\n");

        }

    }

    private void ecrireXML(String donnee) throws ArcException {
        try {
			getOutputStream().write((donnee).getBytes());
		} catch (IOException writeFileException) {
			throw new ArcException(writeFileException, ArcExceptionMessage.FILE_WRITE_FAILED, this.idSource);
		}
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
    }

    @Override
    public void finalisation() {
    }

    @Override
    public void execution() throws ArcException {
        chargerClefValeur();

    }

    @Override
    public void charger() throws ArcException {
        initialisation();
        execution();
        finalisation();

    }

}
