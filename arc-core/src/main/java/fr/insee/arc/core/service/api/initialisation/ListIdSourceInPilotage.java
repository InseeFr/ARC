package fr.insee.arc.core.service.api.initialisation;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;

import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.api.query.ServicePilotageOperation;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.structure.GenericBean;

public class ListIdSourceInPilotage {


	private Map<String, List<String>> idSourceInPilotage;

	public ListIdSourceInPilotage() {
		super();
		this.idSourceInPilotage = new HashMap<>();
	}


	public ListIdSourceInPilotage addSource(Connection coordinatorConnexion, String tablePilotage, TraitementPhase phase, TraitementEtat etat) throws ArcException
	{
		String key= serializeAsKey(phase, etat);
		this.idSourceInPilotage.put(key, ObjectUtils.firstNonNull(new GenericBean(UtilitaireDao.get(0).executeRequest(coordinatorConnexion, ServicePilotageOperation.querySelectIdSourceFromPilotage(tablePilotage, phase, etat))).mapContent().get(ColumnEnum.ID_SOURCE.getColumnName())
				, new ArrayList<String>()));
		return this;
	}

	public List<String> getIdSourceInPilotage(TraitementPhase phase, TraitementEtat etat) throws ArcException
	{
		String key= serializeAsKey(phase, etat);
		return idSourceInPilotage.get(key);
	}

	private static String serializeAsKey(TraitementPhase phase, TraitementEtat etat)
	{
		return phase+","+etat;
	}
}

