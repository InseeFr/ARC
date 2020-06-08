package fr.insee.arc.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fr.insee.arc.utils.dao.AbstractEntity;

public class RegleMappingEntity extends AbstractEntity {

    private static final String colIdRegle = "id_regle";
    private static final String colIdNorme = "id_norme";
    private static final String colValiditeInf = "validite_inf";
    private static final String colValiditeSup = "validite_sup";
    private static final String colVersion = "version";
    private static final String colPeriodicite = "periodicite";
    private static final String colVariableSortie = "variable_sortie";
    private static final String colExprRegleCol = "expr_regle_col";
    private static final String colCommentaire = "commentaire";

    private static final Set<String> colNames = new HashSet<String>() {
        /**
         *
         */
        private static final long serialVersionUID = 3677223986776708059L;

        {
            add(colCommentaire);
            add(colExprRegleCol);
            add(colIdNorme);
            add(colIdRegle);
            add(colPeriodicite);
            add(colValiditeInf);
            add(colValiditeSup);
            add(colVariableSortie);
            add(colVersion);
        }
    };

    public RegleMappingEntity() {
        super();
    }

    public RegleMappingEntity(List<String> someNames, List<String> someValues) {
        super(someNames, someValues);
    }

    public RegleMappingEntity(HashMap<String, ArrayList<String>> mapInputFields) {
        super(mapInputFields);
        this.setIdRegle(mapInputFields.get(colIdRegle).get(0));
        this.setIdNorme(mapInputFields.get(colIdNorme).get(0));
        this.setValiditeInf(mapInputFields.get(colValiditeInf).get(0));
        this.setValiditeSup(mapInputFields.get(colValiditeSup).get(0));
        this.setVersion(mapInputFields.get(colVersion).get(0));
        this.setPeriodicite(mapInputFields.get(colPeriodicite).get(0));
        this.setVariableSortie(mapInputFields.get(colVariableSortie).get(0));
        this.setExprRegleCol(mapInputFields.get(colExprRegleCol).get(0));
        this.setCommentaire(mapInputFields.get(colCommentaire).get(0));
    }

    public String getIdRegle() {
        return this.getMap().get(colIdRegle);
    }

    public void setIdRegle(String idRegle) {
        this.getMap().put(colIdRegle, idRegle);
    }

    public String getIdNorme() {
        return this.getMap().get(colIdNorme);
    }

    public void setIdNorme(String idNorme) {
        this.getMap().put(colIdNorme, idNorme);
    }

    public String getValiditeInf() {
        return this.getMap().get(colValiditeInf);
    }

    public void setValiditeInf(String validiteInf) {
        this.getMap().put(colValiditeInf, validiteInf);
    }

    public String getValiditeSup() {
        return this.getMap().get(colValiditeSup);
    }

    public void setValiditeSup(String validiteSup) {
        this.getMap().put(colValiditeSup, validiteSup);
    }

    public String getVersion() {
        return this.getMap().get(colVersion);
    }

    public void setVersion(String version) {
        this.getMap().put(colVersion, version);
    }

    public String getPeriodicite() {
        return this.getMap().get(colPeriodicite);
    }

    public void setPeriodicite(String periodicite) {
        this.getMap().put(colPeriodicite, periodicite);
    }

    public String getVariableSortie() {
        return this.getMap().get(colVariableSortie);
    }

    public void setVariableSortie(String variableSortie) {
        this.getMap().put(colVariableSortie, variableSortie);
    }

    public String getExprRegleCol() {
        return this.getMap().get(colExprRegleCol);
    }

    public void setExprRegleCol(String exprRegleCol) {
        this.getMap().put(colExprRegleCol, exprRegleCol);
    }

    public String getCommentaire() {
        return this.getMap().get(colCommentaire);
    }

    public void setCommentaire(String commentaire) {
        this.getMap().put(colCommentaire, commentaire);
    }

    @Override
    public Set<String> colNames() {
        return colNames;
    }

}
