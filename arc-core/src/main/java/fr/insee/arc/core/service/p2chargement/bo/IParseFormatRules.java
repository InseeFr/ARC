package fr.insee.arc.core.service.p2chargement.bo;

public interface IParseFormatRules {

	public String getAfterTag();

	public String getBeforeTag();
	
	public boolean isStop();
	
}
