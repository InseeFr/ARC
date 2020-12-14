package fr.insee.arc.utils.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Sleep {

    private static final Logger LOGGER = LogManager.getLogger(Sleep.class);

    private Sleep() {
        throw new IllegalStateException("Utility class");
      }

    public static final void sleep(int millis) {
    	 try {
             Thread.sleep(millis);
         } catch (InterruptedException ex) {
             LoggerHelper.errorGenTextAsComment(Sleep.class, "sleep(int)", LOGGER, ex);
        	 Thread.currentThread().interrupt();
         }
    }

}
