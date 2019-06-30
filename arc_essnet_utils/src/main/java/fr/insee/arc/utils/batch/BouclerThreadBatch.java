package fr.insee.arc.utils.batch;

import org.apache.log4j.Logger;

import fr.insee.arc.utils.utils.LoggerHelper;

public class BouclerThreadBatch<T extends Batch> extends ThreadBatch<T> {

    private static final Logger LOGGER = Logger.getLogger(BouclerThreadBatch.class);

    private long timeOut;

    public BouclerThreadBatch(T aBatch, long aTimeOut, long aTimeSleep) {
        super(aBatch, aTimeSleep);
        this.timeOut = aTimeOut;
    }

    public void run() {
        int iterationNumber = 0;
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < this.timeOut) {
            LoggerHelper.info(LOGGER,
                    "Début de l'itération numéro " + (++iterationNumber) + " pour le batch " + this.getBatch().getClass().getCanonicalName());
            this.getBatch().execute();
            try {

                sleep(this.timeSleep);
            } catch (InterruptedException ex) {
                LoggerHelper.errorGenTextAsComment(getClass(), "run()", LOGGER, ex);
            }
            LoggerHelper.info(LOGGER, "Fin de l'itération numéro " + iterationNumber + " pour le batch " + this.getBatch().getClass().getCanonicalName());
        }
    }

}
