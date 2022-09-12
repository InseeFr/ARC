package fr.insee.arc.core.model;

import java.util.Arrays;
import java.util.Comparator;

public enum TraitementEtat {
	OK(2,2,"{OK}"), KO(2,4,"{KO}"), OK$KO(2,3,"{OK,KO}"), ENCOURS(1,1,"{ENCOURS}");
	
	private TraitementEtat(int anOrdre, int anOrdreAffichage, String sqlArrayExpression) {
		this.ordre = anOrdre;
		this.ordreAffichage = anOrdreAffichage;
		this.sqlArrayExpression=sqlArrayExpression;
	}

	private int ordre;
	private int ordreAffichage;
	private String sqlArrayExpression;

	public int getOrdre() {
		return this.ordre;
	}
	
	
	public int getOrdreAffichage() {
		return ordreAffichage;
	}


	public String getSqlArrayExpression() {
		return sqlArrayExpression;
	}

	public static TraitementEtat[] valuesByOrdreAffichage()
	{
		TraitementEtat[] v=TraitementEtat.values();
		Arrays.sort(v, Comparator.comparing(TraitementEtat::getOrdre));	
		return v;
	}
	
	
}
