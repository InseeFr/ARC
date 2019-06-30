package fr.insee.arc.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Function;

import fr.insee.arc.core.model.Norme;
import fr.insee.arc.utils.dao.AbstractDAO;
import fr.insee.arc.utils.dao.IQueryHandler;
import fr.insee.arc.utils.sqlengine.ContextName;

/**
 * The DAO to get the norm
 * @author Pépin Rémi
 *
 */
public class NormeDAO extends AbstractDAO<Norme> {
    
    public static final String ID_NORME = "id_norme";
    public static final String PERIODICITE= "periodicite";
    public static final String DEF_NORME= "def_norme";
    public static final String DEF_VALIDITE= "def_validite";
    public static final String ID= "id";
    public static final String ETAT= "etat";
    public static final String ID_FAMILLE= "id_famille";
    
    public static final String ID_ENTITY = ID_NORME;
    public static final String ORDER_BY_COLUMN = ID_NORME ;
    
    
    public NormeDAO(IQueryHandler handler, ContextName aContextName) {
	super(handler, aContextName);
    }

    public static final Function<ResultSet, Norme> GET_NORME = res -> {
	try {
	    Norme returned = new Norme();
	    returned.setIdNorme(res.getString(ID_NORME));
	    returned.setPeriodicite(res.getString(PERIODICITE));
	    returned.setDefNorme(res.getString(DEF_NORME));
	    returned.setDefValidite(res.getString(DEF_VALIDITE));
	    returned.setId(res.getString(ID));
	    returned.setEtat(res.getString(ETAT));
	    returned.setIdFamille(res.getString(ID_FAMILLE));
	   
	    return returned;
	} catch (SQLException ex) {
	    throw new DAOException(ex);
	}
    };
    

    @Override
    public Function<ResultSet, Norme> getOnRecord() {
	return GET_NORME;
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
