package fr.insee.arc_essnet.core.service.mapping;

import java.util.HashSet;
import java.util.Set;

import fr.insee.arc_essnet.core.service.mapping.regles.AbstractRegleMapping;
import fr.insee.arc_essnet.core.service.mapping.regles.RegleMappingGroupe;

/**
 *
 * Une variable est associée à une expression SQL (le mapping de cette variable). En vue de la génération de la requête de mapping, chaque
 * variable doit dériver ({@link #deriver()}) la règle de mapping qui lui est associée.
 *
 */
public class VariableMapping implements Comparable<VariableMapping> {

    private String nomVariable;

    private String type;

    private AbstractRegleMapping expressionRegle;

    private Set<TableMapping> ensembleTableMapping;

    private RegleMappingFactory regleMappingFactory;

    /**
     *
     */
    private VariableMapping() {
        this.ensembleTableMapping = new HashSet<>();
    }

    public VariableMapping(RegleMappingFactory aRegleMappingFactory, String aNomVariable, String aType) {
        this();
        this.nomVariable = aNomVariable;
        this.regleMappingFactory = aRegleMappingFactory;
        this.type = aType;
    }

    /**
     * @return the nomVariable
     */
    public String getNomVariable() {
        return this.nomVariable;
    }

    /**
     * @return the expressionRegle
     */
    public AbstractRegleMapping getExpressionRegle() {
        return this.expressionRegle;
    }

    /**
     * Ajouter une {@link TableMapping} à la liste des tables qui ont cette variable
     *
     * @param tableMapping
     */
    public void ajouterTable(TableMapping tableMapping) {
        this.ensembleTableMapping.add(tableMapping);

    }

    public void deriver() throws Exception {
        this.expressionRegle.deriver();
        for (TableMapping table : this.ensembleTableMapping) {
            table.setGroupe(isGroupe());
        }
    }

    public Set<Integer> getEnsembleGroupes() {
        return this.expressionRegle.getEnsembleGroupes();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.nomVariable == null) ? 0 : this.nomVariable.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof VariableMapping)) {
            return false;
        }
        VariableMapping other = (VariableMapping) obj;
        if (this.nomVariable == null) {
            if (other.nomVariable != null) {
                return false;
            }
        } else if (!this.nomVariable.equals(other.nomVariable)) {
            return false;
        }
        return true;
    }

    public boolean belongsTo(TableMapping table) {
        return this.ensembleTableMapping.contains(table);
    }

    /**
     * @param expressionRegle
     *            the expressionRegle to set
     */
    public void setExpressionRegle(AbstractRegleMapping anExpressionRegle) {
        this.expressionRegle = anExpressionRegle;
    }

    /**
     * @param expressionRegle
     *            the expressionRegle to set
     */
    public void setExpressionRegle(String anExpressionRegle) {
        this.expressionRegle = this.regleMappingFactory.get(anExpressionRegle, this);
    }

    /**
     * @return the ensembleTableMapping
     */
    public Set<TableMapping> getEnsembleTableMapping() {
        return this.ensembleTableMapping;
    }

    /**
     * @return the ensembleIdentifiantsRubriques
     */
    public Set<String> getEnsembleIdentifiantsRubriques() {
        return this.expressionRegle.getEnsembleIdentifiantsRubriques();
    }

    public Set<String> getEnsembleIdentifiantsRubriques(Integer aNumeroGroupe) {
        return this.expressionRegle.getEnsembleIdentifiantsRubriques(aNumeroGroupe);
    }

    /**
     * @return the ensembleNomsRubriques
     */
    public Set<String> getEnsembleNomsRubriques() {
        return this.expressionRegle.getEnsembleNomsRubriques();
    }

    public Set<String> getEnsembleNomsRubriques(Integer aNumeroGroupe) {
        return this.expressionRegle.getEnsembleNomsRubriques(aNumeroGroupe);
    }

    @Override
    public int compareTo(VariableMapping other) {
        return this.nomVariable.compareTo(other.nomVariable);
    }

    public String getType() {
        return this.type;
    }

    public String expressionSQL() {
        return new StringBuilder("(" + this.getExpressionRegle().getExpressionSQL() + ")::" + this.getType())//
                .toString();
    }

    public String expressionSQLAsType() {
        return new StringBuilder("(" + this.getExpressionRegle().getExpressionSQL() + ")::" + this.getType())//
                .append(" AS " + this.getNomVariable()).toString();
    }

    public String expressionSQL(Integer aNumeroGroupe) {
        return new StringBuilder("(" + this.getExpressionRegle().getExpressionSQL(aNumeroGroupe) + ")::" + this.getType())//
                .toString();
    }

    public String expressionSQLtoText(Integer aNumeroGroupe) {
        return new StringBuilder("(" + this.getExpressionRegle().getExpressionSQL(aNumeroGroupe) + ")::text")//
                .toString();
    }
    
    public String expressionSQLAsType(Integer aNumeroGroupe) {
        return new StringBuilder("(" + this.getExpressionRegle().getExpressionSQL(aNumeroGroupe) + ")::" + this.getType())//
                .append(" AS " + this.getNomVariable()).toString();
    }

    public String toString() {
        return this.nomVariable;
    }

    public String minOverPartitionByIdTable() {
        return "min(" + this.nomVariable + ") OVER (PARTITION BY id_table ORDER BY ordre_intra) AS " + this.nomVariable + ", ";
    }

    public boolean isGroupe() {
        return (this.expressionRegle instanceof RegleMappingGroupe);
    }
}
