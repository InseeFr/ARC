package fr.insee.arc.utils.consumer;

import fr.insee.arc.utils.exception.ArcException;

@FunctionalInterface
public interface ThrowingFunction<T, R> {
   R apply(T t) throws ArcException;
}
