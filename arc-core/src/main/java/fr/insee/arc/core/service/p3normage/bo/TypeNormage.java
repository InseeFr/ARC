package fr.insee.arc.core.service.p3normage.bo;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;

public enum TypeNormage {
	RELATION("relation"),
	CARTESIAN("cartesian"),
	UNICITE("unicité"),
	PARTITION("partition"),
	INDEPENDANCE("independance"),
	BLOC_INDEPENDANCE("bloc_independance"),
	DELETION("deletion"),
	DUPLICATION("duplication")
	;
	
	private String nom;

    private TypeNormage(String nom) {
        this.nom = nom;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    
    public static TypeNormage getEnum(String code) throws ArcException {

    	List<TypeNormage> filtered = Arrays.asList(TypeNormage.values()).stream().filter(t -> t.getNom().equals(code)).collect(Collectors.toList());
    	
    	if (filtered.isEmpty())
    	{
    		throw new ArcException(ArcExceptionMessage.NORMAGE_TYPE_NOT_FOUND, code);
    	}
    	
    	return filtered.get(0);
    }
    
    
    
}
