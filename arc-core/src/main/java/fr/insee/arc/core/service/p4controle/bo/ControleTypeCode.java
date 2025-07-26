package fr.insee.arc.core.service.p4controle.bo;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;

/**
 * code des types de controle proposés par ARC
 * 
 * @author FY2QEQ
 *
 */
public enum ControleTypeCode {
	NUM("NUM"), DATE("DATE"), ALPHANUM("ALPHANUM"), CARDINALITE("CARDINALITE"), CONDITION("CONDITION"),
	REGEXP("REGEXP"), ENUM_BRUTE("ENUM_BRUTE"), ENUM_TABLE("ENUM_TABLE");

	private String nom;

	private ControleTypeCode(String nom) {
		this.nom = nom;
	}

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}
	
	private final static Map<String, ControleTypeCode> controleTypeCodeMapByNom = Stream.of(ControleTypeCode.values()).collect(Collectors.toMap(ControleTypeCode::getNom, t -> t)); 

	public static ControleTypeCode getEnum(String code) throws ArcException {

		ControleTypeCode result = controleTypeCodeMapByNom.get(code);
		
		if (result==null) {
			throw new ArcException(ArcExceptionMessage.CONTROLE_TYPE_NOT_FOUND, code);
		}

		return result;
	}

}
