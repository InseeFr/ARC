package fr.insee.arc.core.model;

import java.util.Arrays;
import java.util.Comparator;

public enum TraitementEtat {
	OK(2,2,"{OK}", new String[] {"OK"})
	, KO(2,4,"{KO}", new String[] {"KO"})
	, OK$KO(2,3,"{OK,KO}", new String[] {"OK","KO"})
	, ENCOURS(1,1,"{ENCOURS}", new String[] {"ENCOURS"});
	
	private TraitementEtat(int anOrdre, int anOrdreAffichage, String sqlArrayExpression, String[] arrayExpression) {
		this.ordre = anOrdre;
		this.ordreAffichage = anOrdreAffichage;
		this.sqlArrayExpression=sqlArrayExpression;
		this.arrayExpression=arrayExpression;
	}

	private int ordre;
	private int ordreAffichage;
	private String sqlArrayExpression;
	private String[] arrayExpression;

	public int getOrdre() {
		return this.ordre;
	}
	
	
	public int getOrdreAffichage() {
		return ordreAffichage;
	}


	public String getSqlArrayExpression() {
		return sqlArrayExpression;
	}
	
	public String[] getArrayExpression() {
		return arrayExpression;
	}


	public static TraitementEtat[] valuesByOrdreAffichage()
	{
		TraitementEtat[] v=TraitementEtat.values();
		Arrays.sort(v, Comparator.comparing(TraitementEtat::getOrdre));	
		return v;
	}
	
	
}
