package fr.insee.arc.core.model;

public enum TraitementOperationFichier {
D("1", "Delete file")
, R("R", "Replay file")
, RA("RA", "Replay archive")
;

private String dbValue;
private String comment;

private TraitementOperationFichier(String dbValue, String comment)
{
	this.dbValue=dbValue;
	this.comment=comment;
}

public String getDbValue() {
	return dbValue;
}

public String getComment() {
	return comment;
}

}
