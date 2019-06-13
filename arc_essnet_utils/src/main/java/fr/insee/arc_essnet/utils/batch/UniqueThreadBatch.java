package fr.insee.arc_essnet.utils.batch;

import org.apache.log4j.Logger;

import fr.insee.arc_essnet.utils.utils.LoggerHelper;

public class UniqueThreadBatch<T extends Batch> extends ThreadBatch<T> {

    private static final Logger LOGGER = Logger.getLogger(UniqueThreadBatch.class);

    public UniqueThreadBatch(T aBatch, long aTimeSleep) {
        super(aBatch, aTimeSleep);

    }

    public void run() {
        LoggerHelper.info(LOGGER,"Début du batch " + this.getBatch().getClass().getCanonicalName());
        this.getBatch().execute();
        try {
            sleep(this.timeSleep);
        } catch (InterruptedException ex) {
            LoggerHelper.errorGenTextAsComment(getClass(), "run()", LOGGER, ex);
        }
        LoggerHelper.info(LOGGER, "Fin du batch " + this.getBatch().getClass().getCanonicalName());
    }

}
