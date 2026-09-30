package fr.insee.arc.core.famille.model;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface RegleComparable<C> {

    C getCleComparaison();

    Object getContenuComparaison();

    default String formatPourExport() {
        return Stream.of(
                        formatRecord(getCleComparaison()),
                        formatRecord(getContenuComparaison())
                )
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(", "));
    }

    private static String formatRecord(Object object) {
        if (object == null) {
            return "";
        }

        if (!object.getClass().isRecord()) {
            return object.toString();
        }

        return Arrays.stream(object.getClass().getRecordComponents())
                .map(component -> {
                    try {
                        Object value = component.getAccessor().invoke(object);
                        return component.getName() + "=" + value;
                    } catch (ReflectiveOperationException e) {
                        throw new IllegalStateException(e);
                    }
                })
                .collect(Collectors.joining(", "));
    }
}

