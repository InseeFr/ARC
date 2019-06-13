package fr.insee.arc_essnet.core.model.rules;

/**
 * A structurize rule. This is a java representation of a structurize rule in
 * database
 * 
 * 
 * @author Pépin Rémi
 *
 */
public class RuleStructurizeEntity extends AbstractRuleEntity {

    private String idClasse;
    private String column;
    private String columneNmcl;
    private int idRegle;
    private String todo;

    /**
     * Check if the rule use one specific column
     * 
     * @param aColumn
     *            : one column
     * @return true is a column is use in the rule
     */
    public boolean containSpecificColumn(String aColumn) {
	return column.equals(aColumn) || columneNmcl.equals(aColumn);
    }

    /**
     * Check if the rule use one column of a list
     * 
     * @param someColumns
     *            : an array of column
     * @return true is a column is use in the rule
     */
    public boolean containListColumn(String... someColumns) {
	boolean containColumn = false;
	for (String aColumn : someColumns) {
	    containColumn = containSpecificColumn(aColumn);
	    if (containColumn) {
		break;
	    }
	}
	return containColumn;
    }

    public String getIdClasse() {
	return idClasse;
    }

    public void setIClasse(String id_classe) {
	this.idClasse = id_classe;
    }

    public String getRubrique() {
	return column;
    }

    public void setRubrique(String rubrique) {
	this.column = rubrique;
    }

    public String getRubriqueNmcl() {
	return columneNmcl;
    }

    public void setRubriqueNmcl(String rubriqueNmcl) {
	this.columneNmcl = rubriqueNmcl;
    }

    public int getIdRegle() {
	return idRegle;
    }

    public void setIdRegle(int idRegle) {
	this.idRegle = idRegle;
    }

    public String getTodo() {
	return todo;
    }

    public void setTodo(String todo) {
	this.todo = todo;
    }

}
