package fr.insee.arc.core.util;


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


    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((delimiter == null) ? 0 : delimiter.hashCode());
	result = prime * result + ((format == null) ? 0 : format.hashCode());
	result = prime * result + ((typeChargement == null) ? 0 : typeChargement.hashCode());
	return result;
    }


    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	RegleChargement other = (RegleChargement) obj;
	if (delimiter == null) {
	    if (other.delimiter != null)
		return false;
	} else if (!delimiter.equals(other.delimiter))
	    return false;
	if (format == null) {
	    if (other.format != null)
		return false;
	} else if (!format.equals(other.format))
	    return false;
	if (typeChargement != other.typeChargement)
	    return false;
	return true;
    }

    

    
}
