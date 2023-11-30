package fr.insee.arc.core.service.p2chargement.factory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;

public enum TypeChargement {
	CLEF_VALEUR("clef-valeur"), XML("xml"), PLAT("plat"), XML_COMPLEXE("xml-complexe");

	private String nom;

	private TypeChargement(String nom) {
		this.nom = nom;
	}

	public String getNom() {
		return nom;
	}

	public static TypeChargement getEnum(String code) throws ArcException {

		List<TypeChargement> filtered = Arrays.asList(TypeChargement.values()).stream()
				.filter(t -> t.getNom().equals(code)).collect(Collectors.toList());

		if (filtered.isEmpty()) {
			throw new ArcException(ArcExceptionMessage.LOAD_TYPE_NOT_FOUND, code);
		}

		return filtered.get(0);
	}

}
