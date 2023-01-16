package fr.insee.arc.core.service.thread;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.structure.GenericBean;

public interface ArcThread<T> {

	
	abstract void configThread(ScalableConnection connexion, int currentIndice, T aApi);

	abstract ScalableConnection getConnexion();

	abstract Thread getT();
	
	abstract void start();

}
