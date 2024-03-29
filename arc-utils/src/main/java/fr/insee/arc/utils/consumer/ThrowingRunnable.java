package fr.insee.arc.utils.consumer;

import fr.insee.arc.utils.exception.ArcException;

@FunctionalInterface
public interface ThrowingRunnable {
   void run() throws ArcException;
}
