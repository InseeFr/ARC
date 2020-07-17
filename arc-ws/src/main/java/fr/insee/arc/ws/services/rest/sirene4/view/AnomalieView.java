package fr.insee.arc.ws.services.rest.sirene4.view;

public class AnomalieView {

	
	
    public AnomalieView(String code, EnumCategorie categorie, String message, int ligne, int colonne) {
		super();
		this.code = code;
		this.categorie = categorie;
		this.message = message;
		this.ligne = ligne;
		this.colonne = colonne;
	}

	private String code;

    private EnumCategorie categorie;

    private String message;

    private int ligne;

    private int colonne;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public EnumCategorie getCategorie() {
        return categorie;
    }

    public void setCategorie(EnumCategorie categorie) {
        this.categorie = categorie;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getLigne() {
        return ligne;
    }

    public void setLigne(int ligne) {
        this.ligne = ligne;
    }

    public int getColonne() {
        return colonne;
    }

    public void setColonne(int colonne) {
        this.colonne = colonne;
    }

}
