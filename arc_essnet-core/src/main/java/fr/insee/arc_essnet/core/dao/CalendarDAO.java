package fr.insee.arc_essnet.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Function;

import fr.insee.arc_essnet.core.model.Calendar;
import fr.insee.arc_essnet.core.model.Norme;
import fr.insee.arc_essnet.utils.dao.AbstractDAO;
import fr.insee.arc_essnet.utils.dao.IQueryHandler;
import fr.insee.arc_essnet.utils.sqlengine.ContextName;

public class CalendarDAO extends AbstractDAO<Calendar> {
    
    public static final String ID_NORME = "id_norme";
    public static final String PERIODICITE= "periodicite";
    public static final String VALIDITE_INF= "validite_inf";
    public static final String VALIDITE_SUP= "validite_sup";
    public static final String ID= "id";
    public static final String ETAT= "etat";

    
    public static final String ID_ENTITY = ID;
    public static final String ORDER_BY_COLUMN = ID ;
    
    
    public CalendarDAO(IQueryHandler handler, ContextName aContextName) {
	super(handler, aContextName);
    }

    public static final Function<ResultSet, Calendar> GET_CALENDAR= res -> {
	try {
	    Calendar returned = new Calendar();
	    returned.setIdNorme(res.getString(ID_NORME));
	    returned.setPeriodicity(res.getString(PERIODICITE));
	    returned.setValiditeInf(res.getString(VALIDITE_INF));
	    returned.setValiditeSup(res.getString(VALIDITE_SUP));
	    returned.setId(res.getString(ID));
	    returned.setState(res.getString(ETAT));
	   
	    return returned;
	} catch (SQLException ex) {
	    throw new DAOException(ex);
	}
    };
    

    @Override
    public Function<ResultSet, Calendar> getOnRecord() {
	return GET_CALENDAR;
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
