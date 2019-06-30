package fr.insee.arc.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fr.insee.arc.utils.dao.AbstractEntity;

public class RegleMappingEntity extends AbstractEntity {

    private static final String COL_ID_RULE = "id_regle";
    private static final String COL_ID_NORME = "id_norme";
    private static final String COL_VALIDITY_INF = "validite_inf";
    private static final String COL_VALIDITY_SUP = "validite_sup";
    private static final String COL_VERSION = "version";
    private static final String COL_PERIODICITY = "periodicite";
    private static final String COL_OUTPUT_VAR = "variable_sortie";
    private static final String COL_EXPR_RULE = "expr_regle_col";
    private static final String COL_COMMENTARY= "commentaire";

    private static final Set<String> colNames = new HashSet<String>() {
        /**
         *
         */
        private static final long serialVersionUID = 3677223986776708059L;

        {
            add(COL_COMMENTARY);
            add(COL_EXPR_RULE);
            add(COL_ID_NORME);
            add(COL_ID_RULE);
            add(COL_PERIODICITY);
            add(COL_VALIDITY_INF);
            add(COL_VALIDITY_SUP);
            add(COL_OUTPUT_VAR);
            add(COL_VERSION);
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
        this.setIdRegle(mapInputFields.get(COL_ID_RULE).get(0));
        this.setIdNorme(mapInputFields.get(COL_ID_NORME).get(0));
        this.setValiditeInf(mapInputFields.get(COL_VALIDITY_INF).get(0));
        this.setValiditeSup(mapInputFields.get(COL_VALIDITY_SUP).get(0));
        this.setVersion(mapInputFields.get(COL_VERSION).get(0));
        this.setPeriodicite(mapInputFields.get(COL_PERIODICITY).get(0));
        this.setVariableSortie(mapInputFields.get(COL_OUTPUT_VAR).get(0));
        this.setExprRegleCol(mapInputFields.get(COL_EXPR_RULE).get(0));
        this.setCommentaire(mapInputFields.get(COL_COMMENTARY).get(0));
    }

    public String getIdRegle() {
        return this.getMap().get(COL_ID_RULE);
    }

    public void setIdRegle(String idRegle) {
        this.getMap().put(COL_ID_RULE, idRegle);
    }

    public String getIdNorme() {
        return this.getMap().get(COL_ID_NORME);
    }

    public void setIdNorme(String idNorme) {
        this.getMap().put(COL_ID_NORME, idNorme);
    }

    public String getValiditeInf() {
        return this.getMap().get(COL_VALIDITY_INF);
    }

    public void setValiditeInf(String validiteInf) {
        this.getMap().put(COL_VALIDITY_INF, validiteInf);
    }

    public String getValiditeSup() {
        return this.getMap().get(COL_VALIDITY_SUP);
    }

    public void setValiditeSup(String validiteSup) {
        this.getMap().put(COL_VALIDITY_SUP, validiteSup);
    }

    public String getVersion() {
        return this.getMap().get(COL_VERSION);
    }

    public void setVersion(String version) {
        this.getMap().put(COL_VERSION, version);
    }

    public String getPeriodicite() {
        return this.getMap().get(COL_PERIODICITY);
    }

    public void setPeriodicite(String periodicite) {
        this.getMap().put(COL_PERIODICITY, periodicite);
    }

    public String getVariableSortie() {
        return this.getMap().get(COL_OUTPUT_VAR);
    }

    public void setVariableSortie(String variableSortie) {
        this.getMap().put(COL_OUTPUT_VAR, variableSortie);
    }

    public String getExprRegleCol() {
        return this.getMap().get(COL_EXPR_RULE);
    }

    public void setExprRegleCol(String exprRegleCol) {
        this.getMap().put(COL_EXPR_RULE, exprRegleCol);
    }

    public String getCommentaire() {
        return this.getMap().get(COL_COMMENTARY);
    }

    public void setCommentaire(String commentaire) {
        this.getMap().put(COL_COMMENTARY, commentaire);
    }

    @Override
    public Set<String> colNames() {
        return colNames;
    }

}
