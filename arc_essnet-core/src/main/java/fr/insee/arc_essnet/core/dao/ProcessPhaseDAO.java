package fr.insee.arc_essnet.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.apache.log4j.Logger;

import fr.insee.arc_essnet.core.model.PhaseFixe;
import fr.insee.arc_essnet.core.model.TraitementPhaseContainer;
import fr.insee.arc_essnet.core.model.TraitementPhaseEntity;
import fr.insee.arc_essnet.core.model.TypeTraitementPhase;
import fr.insee.arc_essnet.utils.dao.AbstractDAO;
import fr.insee.arc_essnet.utils.dao.IQueryHandler;
import fr.insee.arc_essnet.utils.dao.AbstractQueryHandler.HowToClose;
import fr.insee.arc_essnet.utils.sqlengine.Naming;
import fr.insee.arc_essnet.utils.utils.LoggerHelper;

public class ProcessPhaseDAO extends AbstractDAO<TraitementPhaseEntity> {
    private static final Logger LOGGER = Logger.getLogger(RegleDao.class);
    public static final String ID_NORME = "id_norme";
    public static final String VALIDITE_INF = "validite_inf";
    public static final String VALIDITE_SUP = "validite_sup";
    public static final String VERSION = "version";
    public static final String PERIODICITE = "periodicite";
    public static final String NOM_PHASE = "nom_phase";
    public static final String TYPE_PHASE = "type_phase";
    public static final String ORDRE = "ordre";
    private static final String PHASE_PRECEDENTE = "phase_precedente";
    public static final String NB_LIGNE_TRAITEE = "nb_ligne_traitee";
    public static final String IS_IN_IHM = "is_in_ihm";
    public static final String IS_RA_IHM = "is_ra_ihm";
    public static final String ID_ENTITY = String.join(",", ID_NORME, VALIDITE_INF,VALIDITE_SUP, VERSION, PERIODICITE, NOM_PHASE);
    private static final String ORDER_BY_COLUMN = ORDRE ;

    public ProcessPhaseDAO(IQueryHandler handler, Naming aNaming) {
	super(handler, aNaming);
	setOnClose(HowToClose.DO_NOT_CLOSE);
    }

    @Override
    public Function<ResultSet, TraitementPhaseEntity> getOnRecord() {
	return GET_CAMPAGNE_ENTITY;
    }

    public static final Function<ResultSet, TraitementPhaseEntity> GET_CAMPAGNE_ENTITY = res -> {
	try {
	    TraitementPhaseEntity returned = new TraitementPhaseEntity();
	    returned.setNomPhase(res.getString(NOM_PHASE));
	    returned.setTypePhase(TypeTraitementPhase.valueOf(res.getString(TYPE_PHASE)));
	    returned.setOrdre(res.getInt(ORDRE));
	    returned.setNbLigneTraitee(res.getInt(NB_LIGNE_TRAITEE));
	    returned.setPreviousPhase(res.getString(PHASE_PRECEDENTE));
	    returned.setIsInIhm(res.getBoolean(IS_IN_IHM));
	    returned.setIsRAIhm(res.getBoolean(IS_RA_IHM));
	    return returned;
	} catch (SQLException ex) {
	    throw new DAOException(ex);
	}
    };

    public TraitementPhaseContainer getAllPhaseOfNorme(String idNorme, String periodicite, String validiteInf, String validiteSup, String version) throws Exception{
	List<TraitementPhaseEntity> listPhases = new ArrayList<>();
	listPhases.addAll(PhaseFixe.getAllFixedPhases());
	listPhases.addAll(getCuttedList(ORDER_BY_COLUMN,null, null, null));
	
	LoggerHelper.info(LOGGER, String.format("There is %s phases", listPhases.size()));

	return new TraitementPhaseContainer(listPhases);
	
	
    }
    
    public TraitementPhaseContainer getAllPhaseOfNorme() throws Exception{	
	List<TraitementPhaseEntity> listPhases = new ArrayList<>();
	listPhases.addAll(getCuttedList(ORDER_BY_COLUMN, null, null));
	
	LoggerHelper.info(LOGGER,String.format("There is %s phases", listPhases.size()));

	return new TraitementPhaseContainer(listPhases);
	
	
    }
    
    public TraitementPhaseEntity getPreviousPhase(String tablePilotage, String currentPhase) throws Exception {
	StringBuilder conditionWhere = new StringBuilder();
	conditionWhere.append("\n\t exists (");
	conditionWhere.append("\n\t SELECT 1 FROM " + tablePilotage + " pil ");
	conditionWhere.append("\n\t WHERE (");
	conditionWhere.append("\n\t\t pil.id_norme = " + this.getNomTableEntity().toString() + ".id_norme");
	conditionWhere.append("\n\t\tAND pil.periodicite = " + this.getNomTableEntity().toString() + ".periodicite");
	conditionWhere.append("\n\t\tAND pil.validite::date > " + this.getNomTableEntity().toString() + ".validite_inf");
	conditionWhere.append("\n\t\tAND pil.validite::date < " + this.getNomTableEntity().toString() + ".validite_sup");
	conditionWhere.append("\n\t\tAND " + this.getNomTableEntity().toString() + ".nom_phase = '" + currentPhase + "')");
	conditionWhere.append("\n\t\tOR");
	conditionWhere.append("\n\t\t( pil.id_norme isnull");
	conditionWhere.append("\n\t\tAND " + this.getNomTableEntity().toString() + ".nom_phase = '" + currentPhase + "'))");

	
	
	return getListWhere(conditionWhere.toString()).get(0);
    }
    
    @Override
    public String getOrderByColumn() {
	return ORDER_BY_COLUMN;
    }

    @Override
    public String getIdEntity() {
	return null;
    }

}
