package fr.insee.arc.core.service.p4controle.bo;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

	public static ControleTypeCode getEnum(String code) throws ArcException {

		List<ControleTypeCode> filtered = Arrays.asList(ControleTypeCode.values()).stream()
				.filter(t -> t.getNom().equals(code)).collect(Collectors.toList());

		if (filtered.isEmpty()) {
			throw new ArcException(ArcExceptionMessage.CONTROLE_TYPE_NOT_FOUND, code);
		}

		return filtered.get(0);
	}

}
