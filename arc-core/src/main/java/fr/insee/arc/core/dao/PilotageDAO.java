package fr.insee.arc.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Function;

import org.apache.log4j.Logger;

import fr.insee.arc.core.model.PilotageEntity;
import fr.insee.arc.core.model.TraitementPhaseEntity;
import fr.insee.arc.core.model.TraitementState;
import fr.insee.arc.core.model.TypeTraitementPhase;
import fr.insee.arc.core.service.AbstractPhaseService;
import fr.insee.arc.utils.dao.AbstractDAO;
import fr.insee.arc.utils.dao.IQueryHandler;
import fr.insee.arc.utils.sqlengine.ContextName;

public class PilotageDAO extends AbstractDAO<PilotageEntity> {
    public static final Logger LOGGER = Logger.getLogger(PilotageDAO.class);
    public static final String ID_SOURCE = "id_source";
    public static final String ID_NORME = "id_norme";
    public static final String VALIDITE = "validite";
    public static final String PERIODICITE = "periodicite";
    public static final String PHASE_TRAITEMENT = "phase_traitement";
    public static final String ETAT_TRAITEMENNT = "etat_traitement";
    public static final String DATE_TRAITEMENNT = "date_traitement";
    public static final String RAPPORT = "rapport";
    public static final String TAUX_KO = "taux_ko";
    public static final String NB_ENR = "nb_enr";
    public static final String NB_ESSAIS = "nb_essais";
    public static final String ETAPE = "etape";
    public static final String VALIDITE_INF = "validite_inf";
    public static final String VALIDITE_SUP = "validite_sup";
    public static final String VERSION = "version";
    public static final String DATE_ENTREE = "date_entree";
    public static final String CONTAINER = "container";
    public static final String V_CONTAINER = "v_container";
    public static final String O_CONTAINER = "o_container";
    public static final String TO_DELETE = "to_delete";
    public static final String CLIENT = "client";
    public static final String DATE_CLIENT = "date_client";
    public static final String JOINTURE = "jointure";

    public static final String ID_ENTITY = String.join(",", ID_NORME, VALIDITE_INF,VALIDITE_SUP, VERSION, PERIODICITE, PHASE_TRAITEMENT);
    private static final String ORDER_BY_COLUMN = ID_SOURCE ;
    
    public PilotageDAO(IQueryHandler handler, ContextName aContextName) {
	super(handler, aContextName);
    }

    public static final Function<ResultSet, PilotageEntity> GET_PILOTAGE_ENTITY = res -> {
	try {
	    PilotageEntity returned = new PilotageEntity();
	    returned.setIdSource(res.getString(ID_SOURCE));
	    returned.setIdNorme(res.getString(ID_NORME));
	    returned.setValidite(res.getString(VALIDITE));
	    returned.setPeriodicite(res.getString(PERIODICITE));
	    returned.setPhaseTraitement(TypeTraitementPhase.valueOf((res.getString(PHASE_TRAITEMENT))));
	    returned.setEtatTraitemennt((String[]) res.getArray(ETAT_TRAITEMENNT).getArray());
	    returned.setDateTraitemennt(res.getString(DATE_TRAITEMENNT));
	    returned.setRapport(res.getString(RAPPORT));
	    returned.setTauxKo(res.getFloat(TAUX_KO));
	    returned.setNbEnr(res.getString(NB_ENR));
	    returned.setNbEssais(res.getString(NB_ESSAIS));
	    returned.setEtape(res.getInt(ETAPE));
	    returned.setValiditeInf(res.getString(VALIDITE_INF));
	    returned.setValiditeSup(res.getString(VALIDITE_SUP));
	    returned.setVersion(res.getString(VERSION));
	    returned.setDateEntree(res.getString(DATE_ENTREE));
	    returned.setContainer(res.getString(CONTAINER));
	    returned.setvContainer(res.getString(V_CONTAINER));
	    returned.setoContainer(res.getString(O_CONTAINER));
	    returned.setClient(res.getString(CLIENT));
	    returned.setDateClient(res.getString(DATE_CLIENT));
	    returned.setJointure(res.getString(JOINTURE));
	   
	    return returned;
	} catch (SQLException ex) {
	    throw new DAOException(ex);
	}
    };
    
    @Override
    public Function<ResultSet, PilotageEntity> getOnRecord() {
	return GET_PILOTAGE_ENTITY;
    }

    @Override
    public String getOrderByColumn() {
	return ORDER_BY_COLUMN;
    }

    @Override
    public String getIdEntity() {
	return ID_ENTITY;
    }

    public List<PilotageEntity> getFilesToProcess(String aPhase, String aState) throws Exception{
	StringBuilder whereCondition = new StringBuilder();
	whereCondition.append(PHASE_TRAITEMENT+"='" + aPhase + "' ");
	whereCondition.append("AND '" + aState + AbstractPhaseService.ANY_ETAT_TRAITEMENT);
	return getListWhere(whereCondition.toString());
    }
    
}
