package fr.insee.arc.core.service.p5mapping.engine.dao;

import java.sql.Connection;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.service.global.bo.JeuDeRegle;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;

public class ThreadMappingQueries {

	private ThreadMappingQueries() {
		throw new IllegalStateException("Query builder class class");
	}

	/**
	 *
	 * @param aJeuDeRegle
	 * @return Le bon id_famille
	 * @throws ArcException
	 */
	public static String fetchIdFamille(Connection connexion, JeuDeRegle aJeuDeRegle, String envExecution)
			throws ArcException {
		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
		requete.append("SELECT id_famille FROM " + ViewEnum.NORME.getFullName(envExecution))
				.append("\n WHERE id_norme = " + requete.quoteText(aJeuDeRegle.getIdNorme()))
				.append("\n AND periodicite = " + requete.quoteText(aJeuDeRegle.getPeriodicite()));
		return UtilitaireDao.get(0).getString(connexion, requete);
	}

}
