package fr.insee.arc_essnet.batch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import fr.insee.arc_essnet.utils.batch.BouclerThreadBatch;
import fr.insee.arc_essnet.utils.batch.ThreadBatch;
import fr.insee.arc_essnet.utils.batch.UniqueThreadBatch;
import fr.insee.arc_essnet.utils.utils.LoggerDispatcher;
import fr.insee.config.InseeConfig;

public class LanceurMultiThread {
    private static final Logger LOGGER = Logger.getLogger(LanceurMultiThread.class);

    private static List<ThreadBatch<?>> listeThread;

    /**
     *
     * @param args
     *            {@code args[0]} : environnement de travail de départ<br/>
     *            {@code args[1]} : environnement de travail d'arrivée<br/>
     *            {@code args[2]} : répertoire racine<br/>
     *            {@code args[3]} : nombre de lignes maximal à traiter<br/>
     *            {@code args[4]} : timeOut<br/>
     *            {@code args[5]} : sleep time<br/>
     *            {@code args[6]} : temps après lequel on coupe tout
     */
    public static void main(String[] args) {

	HashMap<String, String> mapParam = new HashMap<>();
	String env = InseeConfig.getConfig().getString("fr.insee.arc.batch.parametre.env");
	mapParam.put("env", env);
	String envExecution = InseeConfig.getConfig().getString("fr.insee.arc.batch.parametre.envExecution");
	mapParam.put("envExecution", envExecution);
	String repertoire = InseeConfig.getConfig().getString("fr.insee.arc.batch.parametre.repertoire");
	mapParam.put("repertoire", repertoire);
	String nbFic = InseeConfig.getConfig().getString("fr.insee.arc.batch.parametre.nbFic");
	mapParam.put("nbFic", nbFic);
	String nbEnr = InseeConfig.getConfig().getString("fr.insee.arc.batch.parametre.nbEnr");
	mapParam.put("nbEnr", nbEnr);
	String timeOut = InseeConfig.getConfig().getString("fr.insee.arc.batch.parametre.timeOut");
	mapParam.put("timeOut", timeOut);
	String sleepTime = InseeConfig.getConfig().getString("fr.insee.arc.batch.parametre.sleepTime");
	mapParam.put("sleepTime", sleepTime);
	String breakTime = InseeConfig.getConfig().getString("fr.insee.arc.batch.parametre.breakTime");
	mapParam.put("breakTime", breakTime);

	for (Map.Entry<String, String> entry : mapParam.entrySet()) {
	    LoggerDispatcher.info("args : " + entry.getKey() + " " + entry.getValue(), LOGGER);
	}
	listeThread = new ArrayList<ThreadBatch<?>>();
	UniqueThreadBatch<InitialiserBatch> initialiser = new UniqueThreadBatch<>(
		new InitialiserBatch(env, envExecution, repertoire, nbEnr), 1);

	BouclerThreadBatch<ChargerBatch> charger = new BouclerThreadBatch<>(
		new ChargerBatch(env, envExecution, repertoire, nbFic), Integer.valueOf(timeOut), 1);
	listeThread.add(charger);
	BouclerThreadBatch<ControlerBatch> controler = new BouclerThreadBatch<>(
		new ControlerBatch(env, envExecution, repertoire, nbEnr), Integer.valueOf(timeOut),
		Integer.valueOf(sleepTime));
	listeThread.add(controler);
	BouclerThreadBatch<FiltrerBatch> filtrer = new BouclerThreadBatch<>(
		new FiltrerBatch(env, envExecution, repertoire, nbEnr), Integer.valueOf(timeOut),
		Integer.valueOf(sleepTime));
	listeThread.add(filtrer);
	BouclerThreadBatch<MapperBatch> mapper = new BouclerThreadBatch<>(
		new MapperBatch(env, envExecution, repertoire, nbEnr), Integer.valueOf(timeOut),
		Integer.valueOf(sleepTime));
	listeThread.add(mapper);
	BouclerThreadBatch<NormerBatch> normer = new BouclerThreadBatch<>(
		new NormerBatch(env, envExecution, repertoire, nbEnr), Integer.valueOf(timeOut),
		Integer.valueOf(sleepTime));
	listeThread.add(normer);
	initialiser.start();
	while (initialiser.isAlive()) {
	    LoggerDispatcher.info("Initialization", LOGGER);
	    try {
		Thread.sleep(1000);
	    } catch (InterruptedException e) {
		LoggerDispatcher.error("interruption error", e, LOGGER);
		Thread.currentThread().interrupt();

	    }
	}
	for (ThreadBatch<?> tb : listeThread) {
	    tb.start();
	}
	boolean isAlive = true;
	long startTime = System.currentTimeMillis();
	while (isAlive) {
	    isAlive = false;
	    for (ThreadBatch<?> tb : listeThread) {
		isAlive = tb.isAlive() || isAlive;
		LoggerDispatcher.info(
			tb.getBatch().getClass().getCanonicalName() + (tb.isAlive() ? " alive." : " dead"),
			LOGGER);
	    }
	    if (System.currentTimeMillis() - startTime > Integer.valueOf(breakTime)) {
		for (ThreadBatch<?> tb : listeThread) {
		    if (tb.isAlive()) {
			LoggerDispatcher.info("Interruption de " + tb.getClass().getCanonicalName(), LOGGER);
			tb.interrupt();
		    }
		}
	    }
	    LoggerDispatcher.info("MultiThread en cours", LOGGER);
	    try {
		Thread.sleep(Integer.valueOf(sleepTime));
	    } catch (InterruptedException e) {
		LoggerDispatcher.error("interruption error", e, LOGGER);
		Thread.currentThread().interrupt();
	    }
	}
	LoggerDispatcher.info("MultiThread terminé", LOGGER);
    }
}
