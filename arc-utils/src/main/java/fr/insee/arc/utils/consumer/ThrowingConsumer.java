package fr.insee.arc.utils.consumer;

import fr.insee.arc.utils.exception.ArcException;

@FunctionalInterface
public interface ThrowingConsumer<T> {
    void accept(T t) throws ArcException;
}
