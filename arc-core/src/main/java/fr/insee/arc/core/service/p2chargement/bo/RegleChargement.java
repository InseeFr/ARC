package fr.insee.arc.core.service.p2chargement.bo;

import fr.insee.arc.core.service.p2chargement.factory.TypeChargement;

/**
 * Classe contenant les infos des règles de chargement
 * @author S4LWO8
 *
 */
public class RegleChargement {
    
    private TypeChargement typeChargement;
    private String format;
    private String delimiter;
    
    
    public RegleChargement(TypeChargement typeChargement, String delimiter, String format) {
        super();
        this.typeChargement = typeChargement;
        this.format = format;
        this.delimiter = delimiter;
    }


    public TypeChargement getTypeChargement() {
        return typeChargement;
    }


    public void setTypeChargement(TypeChargement typeChargement) {
        this.typeChargement = typeChargement;
    }


    public String getFormat() {
        return format;
    }


    public void setFormat(String format) {
        this.format = format;
    }


    public String getDelimiter() {
        return delimiter;
    }


    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    

    
}
