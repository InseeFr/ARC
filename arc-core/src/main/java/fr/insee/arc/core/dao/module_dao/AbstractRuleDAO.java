package fr.insee.arc.core.dao.module_dao;

import fr.insee.arc.utils.dao.AbstractDAO;
import fr.insee.arc.utils.dao.IQueryHandler;
import fr.insee.arc.utils.sqlengine.ContextName;

public abstract class AbstractRuleDAO<T> extends AbstractDAO<T> {

    
    public static final String ID_NORME = "id_norme";
    public static final String PERIODICITE = "periodicite";
    public static final String VALIDITE_INF = "validite_inf";
    public static final String VALIDITE_SUP = "validite_sup";
    public static final String VERSION = "version";
    public static final String COMMENTAIRE = "commentaire";
    public static final String ID_REGLE = "id_regle";
    
    
    public AbstractRuleDAO(IQueryHandler handler, ContextName aContextName) {
	super(handler, aContextName);
    }


}
