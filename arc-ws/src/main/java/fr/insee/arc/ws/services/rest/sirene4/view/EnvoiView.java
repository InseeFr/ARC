package fr.insee.arc.ws.services.rest.sirene4.view;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

public class EnvoiView {

    @JsonFormat(pattern = "YYYY-MM-DD'T'HH:mm:ss")
    private Date dateHeureReception;

    private int nombreDeclarations;

    private String versionConformite;

    private EnteteView entete;

    private List<DeclarationView> declarations;

    public List<DeclarationView> getDeclarations() {
        return declarations;
    }

    public void setDeclarations(List<DeclarationView> declarations) {
        this.declarations = declarations;
    }

    public EnteteView getEntete() {
        return entete;
    }

    public void setEntete(EnteteView entete) {
        this.entete = entete;
    }

    public Date getDateHeureReception() {
        return dateHeureReception;
    }

    public void setDateHeureReception(Date dateHeureReception) {
        this.dateHeureReception = dateHeureReception;
    }

    public int getNombreDeclarations() {
        return nombreDeclarations;
    }

    public void setNombreDeclarations(int nombreDeclarations) {
        this.nombreDeclarations = nombreDeclarations;
    }

    public String getVersionConformite() {
        return versionConformite;
    }

    public void setVersionConformite(String versionConformite) {
        this.versionConformite = versionConformite;
    }

}
