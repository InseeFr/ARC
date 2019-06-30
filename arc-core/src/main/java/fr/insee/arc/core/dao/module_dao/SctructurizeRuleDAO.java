package fr.insee.arc.core.dao.module_dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Function;

import fr.insee.arc.core.model.rules.RuleStructurizeEntity;
import fr.insee.arc.utils.dao.IQueryHandler;
import fr.insee.arc.utils.sqlengine.ContextName;

/**
 * The DAO to get the structurize rules
 * @author Pépin Rémi
 *
 */
public class SctructurizeRuleDAO extends AbstractRuleDAO<RuleStructurizeEntity> {

    /*
     * Specific column to the structurize rules
     */
    public static final String ID_CLASSE = "id_classe";
    public static final String RUBRIQUE = "rubrique";
    public static final String RUBRIQUE_NMCL = "rubrique_nmcl";
    public static final String TODO = "todo";
    
    public static final String ID_ENTITY = ID_REGLE;
    private static final String ORDER_BY_COLUMN = ID_REGLE  ;
    
    public SctructurizeRuleDAO(IQueryHandler handler, ContextName aContextName) {
	super(handler, aContextName);
    }

    /*
     * How to map the result from the database with a RuleStructurizeEntity
     */
    public static final Function<ResultSet, RuleStructurizeEntity> GET_STRUCTURIZE_RULE_ENTITY = res -> {
	try {
	   RuleStructurizeEntity returned = new RuleStructurizeEntity();
	   returned.setIdNorme(res.getString(ID_NORME));
	   returned.setPeriodicite(res.getString(PERIODICITE));
	   returned.setValiditeInf(res.getString(VALIDITE_INF));
	   returned.setValiditeSup(res.getString(VALIDITE_SUP));
	   returned.setVersion(res.getString(VERSION));
	   returned.setIClasse(res.getString(ID_CLASSE));
	   returned.setRubrique(res.getString(RUBRIQUE));
	   returned.setRubriqueNmcl(res.getString(RUBRIQUE_NMCL));
	   returned.setIdRule(res.getString(ID_REGLE));
	   returned.setTodo(res.getString(TODO));
	   returned.setCommentaire(res.getString(COMMENTAIRE));
	   return returned;
	} catch (SQLException ex) {
	    throw new DAOException(ex);
	}
    };
    
    
    public List<RuleStructurizeEntity> getListRules(String idNorme) throws Exception {
	return getListWhere(String.format("id_norme = '%s'", idNorme));
    }
    
    @Override
    public Function<ResultSet, RuleStructurizeEntity> getOnRecord() {
	return GET_STRUCTURIZE_RULE_ENTITY;
    }

    @Override
    public String getOrderByColumn() {
	return ORDER_BY_COLUMN;
    }

    @Override
    public String getIdEntity() {
	return ID_ENTITY;
    }

}
