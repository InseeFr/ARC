package fr.insee.arc_essnet.utils.batch;

import org.apache.log4j.Logger;

public abstract class ThreadBatch<T extends Batch> extends Thread {
    private T batch;
    protected long timeSleep;

    private static final Logger LOGGER = Logger.getLogger(ThreadBatch.class);

    public ThreadBatch(T aBatch, long aTimeSleep) {
        this.setBatch(aBatch);
        this.timeSleep = aTimeSleep;
    }

    /**
     * @return the batch
     */
    public T getBatch() {
        return this.batch;
    }

    /**
     * @param batch the batch to set
     */
    private void setBatch(T batch) {
        this.batch = batch;
    }
}
