package fr.insee.arc.ws.services.rest.sirene4.view;

public class DeclarationView {

    private DeclarationIdentificationView declarationIdentification;

    private DeclarationBilanView declarationBilan;

    private DeclarationAnomalieView declarationAnomalie;

    public DeclarationIdentificationView getDeclarationIdentification() {
        return declarationIdentification;
    }

    public void setDeclarationIdentification(DeclarationIdentificationView declarationIdentification) {
        this.declarationIdentification = declarationIdentification;
    }

    public DeclarationBilanView getDeclarationBilan() {
        return declarationBilan;
    }

    public void setDeclarationBilan(DeclarationBilanView declarationBilan) {
        this.declarationBilan = declarationBilan;
    }

    public DeclarationAnomalieView getDeclarationAnomalie() {
        return declarationAnomalie;
    }

    public void setDeclarationAnomalie(DeclarationAnomalieView declarationAnomalie) {
        this.declarationAnomalie = declarationAnomalie;
        this.declarationBilan = new DeclarationBilanView(declarationAnomalie.getAnomalies());
    }

}
