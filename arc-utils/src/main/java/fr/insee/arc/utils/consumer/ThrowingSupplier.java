package fr.insee.arc.utils.consumer;

import fr.insee.arc.utils.exception.ArcException;

@FunctionalInterface
public interface ThrowingSupplier<R> {
   R get() throws ArcException;
}
