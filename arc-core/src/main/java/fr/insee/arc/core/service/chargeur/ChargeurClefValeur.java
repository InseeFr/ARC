package fr.insee.arc.core.service.chargeur;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.log4j.Logger;

import fr.insee.arc.core.archive_loader.FilesInputStreamLoad;
import fr.insee.arc.core.exception.MissingChildMarkupException;
import fr.insee.arc.core.model.Norme;
import fr.insee.arc.core.model.TraitementState;
import fr.insee.arc.core.service.AbstractPhaseService;
import fr.insee.arc.core.service.thread.ThreadLoadService;
import fr.insee.arc.core.util.CustomTreeFormat;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.utils.utils.LoggerDispatcher;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.ManipString;

/**
 * Loader to convert key value file to xml one
 * 
 * @author Rémi Pépin
 *
 */
public class ChargeurClefValeur implements ILoader {
    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

    private static final Logger LOGGER = Logger.getLogger(ChargeurClefValeur.class);

    private Norme normeOk;
    private String separator = ",";
    private String quote= "\"";
    private String envExecution;
    private String idSource;
    private File fileOut;
    private PipedOutputStream outputStream;
    private String paramBatch;
    private OutputStreamWriter writer;
    private BufferedWriter bufWriter;
    private InputStream tmpInxChargement;
    private String tableChargementPilTemp;
    private String fileName;
    private String currentPhase;
    private Connection connection;
    private ChargeurXml xmlLoader;

    public ChargeurClefValeur(ThreadLoadService threadChargementService, FilesInputStreamLoad filesInputStreamLoad) {
	super();

	this.fileName = threadChargementService.getIdSource();
	this.connection = threadChargementService.getConnection();
	this.tableChargementPilTemp = threadChargementService.getTablePilTempThread();
	this.currentPhase = threadChargementService.getTokenInputPhaseName();
	this.tmpInxChargement = filesInputStreamLoad.getTmpInxLoad();
	this.normeOk = threadChargementService.getNormeFile();
	this.separator = this.normeOk.getRegleChargement().getDelimiter();
	this.xmlLoader = new ChargeurXml(threadChargementService, filesInputStreamLoad);
	this.envExecution = threadChargementService.getExecutionEnv();
    }


    @Override
    public void initialisation() {
	LoggerDispatcher.info("nothing to see here", LOGGER);
    }

    @Override
    public void finalisation() {
	LoggerDispatcher.info("nothing to see here", LOGGER);

    }

    @Override
    public void excecution() throws Exception {
	loadKeyValueFile();

    }

    @Override
    public void charger() throws Exception {
	initialisation();
	excecution();
	finalisation();

    }
    
    /**
     * Load key value file. Because the fisrt loader create and effective is the xml
     * loader, and key value files are close to xml one, we convert KV in xml to be
     * load.
     * 
     * @param entrepot
     * @param currentEntryChargementName
     * @param tmpInxChargement
     * @param normeOk
     * @throws Exception
     */
    private void loadKeyValueFile() throws IOException, MissingChildMarkupException {
	String rapport = "";
	StringBuilder finalRequest = new StringBuilder();

	outputStream = new PipedOutputStream();

	try (PipedInputStream input = new PipedInputStream(outputStream)) {

	    Runnable runnable = () -> {

		try {
		    // Read the file and get a map xml_key:xml_father
		    CustomTreeFormat arbreFormat = new CustomTreeFormat(getNormeOk());

		    // Convert inputfile in XML with stream
		    keyValueFileToXmlFile(arbreFormat.getTheTree(), tmpInxChargement);
		} catch (Exception e) {
		    LoggerDispatcher.error("error in run", e, LOGGER);
		} finally {
		    try {
			outputStream.close();
		    } catch (IOException e) {
			LoggerDispatcher.error("error when close stream", e, LOGGER);
		    }
		}

	    };

	    Thread keyValuetoXMLThread = new Thread(runnable);

	    keyValuetoXMLThread.start();
	    xmlLoader.setIs(input);

	    xmlLoader.charger();
	} catch (Exception e) {
	    LoggerDispatcher.error("loadKeyValueFile", e, LOGGER);
	    rapport = e.getMessage().replace("'", "''");
	    finalRequest.append(AbstractPhaseService.pilotageMarkIdsource(this.tableChargementPilTemp, fileName,
		    this.currentPhase, TraitementState.KO.toString(), rapport));
	    try {
		UtilitaireDao.get("arc").executeBlock(this.connection, finalRequest);
	    } catch (SQLException e1) {
		LoggerDispatcher.error("error in final request", e, LOGGER);
	    }
	}
    }

    /**
     * Convert key value file in outpustream
     * 
     * @param arbreFormat
     * @param tmpInx2
     * @return un outputStream contenant une version xml de l'inputStream
     * @author S4LWO8
     * @throws MissingChildMarkupException 
     * @throws IOException 
     * @throws Exception
     */
    public void keyValueFileToXmlFile(Map<String, String> arbreFormat, InputStream tmpInx2) throws IOException, MissingChildMarkupException {
	LoggerDispatcher.info("** key value to XML conversion **", LOGGER);
	java.util.Date beginDate = new java.util.Date();

	/*
	 * Key : an element, value : the list of its children
	 */
	HashMap<String, ArrayList<String>> mapChildElements = new HashMap<>();

	// key value file reading and writing xml file
	InputStreamReader inputStreamReader = new InputStreamReader(tmpInx2, "ISO-8859-1");

	try (BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

	    fileConversion(arbreFormat, mapChildElements, bufferedReader);
	}

	java.util.Date endDate = new java.util.Date();
	LoggerDispatcher.info("** clefValeurToXml temps : " + (endDate.getTime() - beginDate.getTime()) + " ms **",
		LOGGER);
    }

    /**
     * Read and convert the file. The file is read line by line
     * 
     * @param arbreFormat
     *            : the hierarchie of the key value
     * @param mapChildElements
     *            : the map of the hierarchie
     * @param bufferedReader
     * @throws MissingChildMarkupException 
     * @throws IOException 
     * @throws Exception
     */
    private void fileConversion(Map<String, String> arbreFormat, HashMap<String, ArrayList<String>> mapChildElements,
	    BufferedReader bufferedReader) throws IOException, MissingChildMarkupException {
	List<String> listParentPreviousElement;
	if (getParamBatch() == null || getParamBatch().isEmpty()) {
	    initializeFileOutput();
	}

	// Line by line reading
	String line = bufferedReader.readLine();

	// initialisation
	listParentPreviousElement = initialisationOutputStream(arbreFormat, mapChildElements, line);

	line = bufferedReader.readLine();
	while (line != null) {

	    listParentPreviousElement = readLine(arbreFormat, listParentPreviousElement, mapChildElements, line);

	    line = bufferedReader.readLine();

	}

	finalizeOutputStream(listParentPreviousElement);
    }

    /**
     * Initialise xml file and process first ligne to open markup
     * 
     * @param arbreFormat
     * @param listePeresRubriqueCourante
     * @param mapChildMarkup
     * @param ligne
     * @param bw
     * @throws IOException
     * @throws MissingChildMarkupException
     * @throws Exception
     */
    public List<String> initialisationOutputStream(Map<String, String> arbreFormat,
	    Map<String, ArrayList<String>> mapChildMarkup, String ligne)
	    throws IOException, MissingChildMarkupException {

	writeXML(XML_HEADER);

	String keyElement = getKeyElementFromInput(arbreFormat, ligne);
	String data = getDataFromInput(ligne);

	List<String> listParentCurrentMarkup = new ArrayList<>();
	// Go back to root markup
	while (keyElement != null) {
	    listParentCurrentMarkup.add(keyElement);
	    keyElement = arbreFormat.get(keyElement);
	}
	String currentElement;
	// Open first markup
	for (int i = listParentCurrentMarkup.size() - 1; i > 0; i--) {
	    currentElement = listParentCurrentMarkup.get(i);
	    writeXML("<" + currentElement + ">\n");

	    // Initialize map of open markup
	    mapChildMarkup.put(currentElement, new ArrayList<String>());
	}

	currentElement = listParentCurrentMarkup.get(0);
	mapChildMarkup.get(listParentCurrentMarkup.get(1)).add(currentElement);

	writeXML("<" + currentElement + ">" + data + "</" + currentElement + ">\n");

	return listParentCurrentMarkup;
    }

    /**
     * Method to read line. Use the least common parent to know which markup have to
     * be open and close
     * 
     * @param arbreFormat
     * @param listePeresRubriqueCourante
     * @param listePeresRubriquePrecedante
     * @param mapRubriquesFilles
     * @param ligne
     * @param bw
     * @throws MissingChildMarkupException 
     * @throws IOException 
     * @throws Exception
     */
    public List<String> readLine(Map<String, String> arbreFormat, List<String> listePeresRubriquePrecedante,
	    Map<String, ArrayList<String>> mapRubriquesFilles, String ligne) throws MissingChildMarkupException, IOException   {

	String keyElement = getKeyElementFromInput(arbreFormat, ligne);
	String data = getDataFromInput(ligne);

	List<String> listParentCurrentKeyElement = getListParentCurrentKeyElement(arbreFormat, keyElement);

	openAndCloseMarkup(listePeresRubriquePrecedante, mapRubriquesFilles, data, listParentCurrentKeyElement);
	return listParentCurrentKeyElement;
    }

    /**
     * Quite hard method :/
     * 
     * We want to close and open some markup according to the hierachy of the file.
     * So we compare the previous parent list (markup still open) to the current
     * searching for the least common parent. We read through the previous parent
     * list looking if each element are in the current one.
     * <ul>
     * <li>the element is absent : close the markup</li>
     * <li>the element is persent : we switch and read through the curent list in
     * backward and open each markup</li>
     * </ul>
     * 
     * Exemple : listParentPreviousKeyElement = (a.1.1, a.1, a, root)
     * listParentCurrentKeyElement = (a.2.1, a.2, a, root)
     * 
     * <ul>
     * <li>a.1.1 is present in listParentCurrentKeyElement ? no -> markup close</li>
     * <li>a.1 is present in listParentCurrentKeyElement ? no -> markup close</li>
     * <li>a is present in listParentCurrentKeyElement ? yes ->switch list</li>
     * <li>open markup a.2</li>
     * <li>open amrkup a.2.1</li>
     * </ul>
     * 
     * 
     * @param listParentPreviousKeyElement
     * @param mapRubriquesFilles
     * @param data
     * @param listParentCurrentKeyElement
     * @throws IOException
     */
    private void openAndCloseMarkup(List<String> listParentPreviousKeyElement,
	    Map<String, ArrayList<String>> mapRubriquesFilles, String data, List<String> listParentCurrentKeyElement)
	    throws IOException {
	if (listParentPreviousKeyElement.isEmpty()) {
	    int i = 1;

	    int indexOf = readParentPreviousElementToCloseMarkup(listParentPreviousKeyElement, mapRubriquesFilles,
		    listParentCurrentKeyElement, i);

	    readParentsCurrentElementToOpenMarkup(mapRubriquesFilles, data, listParentCurrentKeyElement, indexOf);
	}
    }

    /**
     * Read through listParentPreviousKeyElement to close markup. Start to 1 because
     * first markup is already close.
     * 
     * @param listParentPreviousKeyElement
     * @param mapRubriquesFilles
     * @param listParentCurrentKeyElement
     * @param i
     * @return
     * @throws IOException
     */
    private int readParentPreviousElementToCloseMarkup(List<String> listParentPreviousKeyElement,
	    Map<String, ArrayList<String>> mapRubriquesFilles, List<String> listParentCurrentKeyElement, int i)
	    throws IOException {
	String currentMarkup;
	int indexOf = listParentCurrentKeyElement.indexOf(listParentPreviousKeyElement.get(i));
	while (indexOf == -1) {

	    currentMarkup = listParentPreviousKeyElement.get(i);

	    writeXML("</" + currentMarkup + ">\n");

	    mapRubriquesFilles.remove(currentMarkup);
	    i++;
	    indexOf = listParentCurrentKeyElement.indexOf(listParentPreviousKeyElement.get(i));
	}
	return indexOf;
    }

    /**
     * Read through listParentCurrentKeyElement to open markup
     * @param mapChildElement
     * @param data
     * @param listParentCurrentKeyElement
     * @param indexOf
     * @throws IOException
     */
    private void readParentsCurrentElementToOpenMarkup(Map<String, ArrayList<String>> mapChildElement, String data,
	    List<String> listParentCurrentKeyElement, int indexOf) throws IOException {
	String currentMarkup;
	// Read through backward  started by the found index
	for (int j = indexOf - 1; j > -1; j--) {
	    currentMarkup = listParentCurrentKeyElement.get(j);
	    if (j == 0) {
		/*
		 * Last child. If already read since last tag close we must close the current parent
		 * before adding the child 
		 */
		if (mapChildElement.get(listParentCurrentKeyElement.get(1)).contains(currentMarkup)) {
		    mapChildElement.get(listParentCurrentKeyElement.get(1)).clear();
		    writeXML("</" + listParentCurrentKeyElement.get(1) + ">\n");
		    writeXML("<" + listParentCurrentKeyElement.get(1) + ">\n");

		}

		writeXML("<" + currentMarkup + ">" + data + "</" + currentMarkup + ">\n");

		mapChildElement.get(listParentCurrentKeyElement.get(j + 1)).add(currentMarkup);
	    } else {
		// Open markup without data
		mapChildElement.put(currentMarkup, new ArrayList<String>());

		writeXML("<" + currentMarkup + ">\n");

	    }
	}
    }

    private List<String> getListParentCurrentKeyElement(Map<String, String> arbreFormat, String markup) {
	ArrayList<String> listParentCurrentElement = new ArrayList<>();
	while (markup != null) {
	    listParentCurrentElement.add(markup);
	    markup = arbreFormat.get(markup);
	}
	return listParentCurrentElement;
    }

    private String getKeyElementFromInput(Map<String, String> arbreFormat, String ligne)
	    throws MissingChildMarkupException {
	String markup = ManipString.substringBeforeFirst(ligne, separator);
	if (!arbreFormat.containsKey(markup)) {
	    throw new MissingChildMarkupException(markup);
	}
	return markup;
    }

    private String getDataFromInput(String ligne) {
	String data = ManipString.substringAfterFirst(ligne, separator);
	// Delete start and end quote
	data = deleteQuote(data);
	// Escape special caracter
	data = StringEscapeUtils.escapeXml11(data);
	return data;
    }

    private String deleteQuote(String data) {
	if (data.startsWith(quote) && data.endsWith(quote)) {
	    data = data.substring(1, data.length() - 1);
	}
	return data;
    }
    
    /**
     * Finalize xml by closing all open arkup
     * 
     * @param listePeresRubriqueCourante
     * @param br
     * @param bw
     * @throws IOException
     */
    public void finalizeOutputStream(List<String> listePeresRubriqueCourante)
	    throws IOException {

	String currentElement;
	for (int i = 1; i < listePeresRubriqueCourante.size(); i++) {
	    currentElement = listePeresRubriqueCourante.get(i);
	    writeXML("</" + currentElement + ">\n");

	}
	this.bufWriter.close();
	this.writer.close();
    }

    private void initializeFileOutput() throws UnsupportedEncodingException, FileNotFoundException {
	String repertoire = PropertiesHandler.getInstance().getBatchParametersDirectory();
	String envDir = this.envExecution.replace(".", "_").toUpperCase();
	String dirOut = repertoire + envDir + File.separator + "EXPORT";
	File f = new File(dirOut);

	if (!f.exists()) {
	    f.mkdir();
	}

	LoggerHelper.debug(LOGGER, "dirOut :" + dirOut);
	this.fileOut = new File(dirOut + File.separator + "export_" + idSource + ".xml");

	// On définit les writer
	this.writer = null;
	try {
	    writer = new OutputStreamWriter(new FileOutputStream(fileOut), "UTF-8");
	} catch (Exception e1) {
	    LoggerDispatcher.error("error in initializeFileOutput", e1, LOGGER);
	}

	this.bufWriter = new BufferedWriter(writer);
    }

    private void writeXML(String donnee) throws IOException {
	getOutputStream().write((donnee).getBytes());

	if (getParamBatch() == null || getParamBatch().isEmpty()) {
	    bufWriter.write(donnee);
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

    public File getFileOut() {
	return fileOut;
    }

    public void setFileOut(File fileOut) {
	this.fileOut = fileOut;
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


}
