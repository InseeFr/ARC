package fr.insee.arc.utils.utils;

import org.apache.log4j.Logger;

public class Sleep {

    private static final Logger LOGGER = Logger.getLogger(Sleep.class);

    public static final void sleep(int millis) {
        sleep(millis, 0);
    }

    public static void sleep(int millis, int nanos) {
        try {
            Thread.sleep(millis, nanos);
        } catch (InterruptedException ex) {
            LoggerHelper.errorGenTextAsComment(Sleep.class, "sleep(int, int)", LOGGER, ex);
        }
    }

}
