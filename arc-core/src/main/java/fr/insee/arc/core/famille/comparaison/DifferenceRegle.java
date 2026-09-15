package fr.insee.arc.core.famille.comparaison;

import java.util.List;

public class DifferenceRegle<T> {

    private final TypeDifferenceEnum type;
    private final List<T> reglesReference;
    private final List<T> reglesComparees;

    public DifferenceRegle(TypeDifferenceEnum type, List<T> reglesReference, List<T> reglesComparees) {
        this.type = type;
        this.reglesReference = reglesReference;
        this.reglesComparees = reglesComparees;
    }

    public TypeDifferenceEnum getType() {
        return type;
    }

    public List<T> getReglesReference() {
        return reglesReference;
    }

    public List<T> getReglesComparees() {
        return reglesComparees;
    }

}



