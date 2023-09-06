package fr.insee.arc.core.service.api.query;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.ManipString;

public class ServicePhase {

	private ServicePhase() {
		throw new IllegalStateException("Utility class");
	}
	
	/**
	 * retrieve phase token from database object name
	 * @param tableName
	 * @return
	 */
	public static TraitementPhase extractPhaseFromTableName(String tableName)
	{
		return TraitementPhase.valueOf(ManipString.substringBeforeFirst(FormatSQL.extractTableNameToken(tableName) , "_")
				.toUpperCase());
	}
	
	/**
	 * retrieve state token from database object name
	 * @param tableName
	 * @return
	 */
	public static TraitementEtat extractEtatFromTableName(String tableName)
	{
		return TraitementEtat.valueOf(ManipString.substringAfterLast(tableName, "_").toUpperCase());
	}
	
	/**
	 * recupere toutes les tables d'état d'un envrionnement
	 *
	 * @param env
	 * @return
	 */
	public static ArcPreparedStatementBuilder selectPhaseTablesFoundInEnv(String env) {
		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
		TraitementPhase[] phase = TraitementPhase.values();
		
		boolean insert = false;

		for (int i = 0; i < phase.length; i++) {
			if (insert) {
				requete.append(" UNION ALL ");
			}
			ArcPreparedStatementBuilder r = selectTablesFoundInPhaseAndEnv(env, phase[i].toString());
			insert = (r.length() > 0);
			requete.append(r);
		}
		return requete;
	}

	private static ArcPreparedStatementBuilder selectTablesFoundInPhaseAndEnv(String env, String phase) {
		// Les tables dans l'environnement sont de la forme
		TraitementEtat[] etat = TraitementEtat.values();
		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
		for (int j = 0; j < etat.length; j++) {
			if (!etat[j].equals(TraitementEtat.ENCOURS)) {
				if (j > 0) {
					requete.append(" UNION ALL ");
				}
				requete.append(FormatSQL.tableExists(ServiceTableNaming.dbEnv(env) + phase + "%\\_" + etat[j]));
			}
		}
		return requete;
	}
	
}
