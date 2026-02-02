package fr.insee.arc.core.service.p0initialisation.useroperation.dao;

import java.util.List;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.model.TraitementOperationFichier;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.core.service.global.dao.PilotageOperations;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.utils.FormatSQL;

public class ResetEnvironementDao {

	public ResetEnvironementDao(Sandbox sandbox) {
		super();
		this.sandbox = sandbox;
	}

	private Sandbox sandbox;

	public void executeQueryResetPilotage() throws ArcException {
		UtilitaireDao.get(0).executeRequest(sandbox.getConnection(),
				FormatSQL.truncate(ViewEnum.PILOTAGE_FICHIER.getFullName(sandbox.getSchema())));

		UtilitaireDao.get(0).executeRequest(sandbox.getConnection(),
				FormatSQL.truncate(ViewEnum.PILOTAGE_ARCHIVE.getFullName(sandbox.getSchema())));
	}

	/**
	 * reset all encours files to the previsous phase
	 * 
	 * @throws ArcException
	 */
	public void executeQueryResetAllPreviousPhaseMark() throws ArcException {
		UtilitaireDao.get(0).executeRequest(sandbox.getConnection(), PilotageOperations
				.queryResetPreviousPhaseMark(ViewEnum.PILOTAGE_FICHIER.getFullName(sandbox.getSchema()), null, null));
	}

	/**
	 * 
	 * @param targetPhase
	 * @param selectedEntries
	 * @return
	 * @throws ArcException
	 */
	public Integer executeDeletePhaseEntriesInPilotageAndCount(TraitementPhase targetPhase,
			List<String> selectedEntries) {
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.append("WITH TMP_DELETE AS (DELETE FROM " + ViewEnum.PILOTAGE_FICHIER.getFullName(sandbox.getSchema())
				+ " WHERE phase_traitement = " + query.quoteText(targetPhase.toString()) + " ");
		if (!selectedEntries.isEmpty()) {
			query.append("AND " + ColumnEnum.ID_SOURCE.getColumnName() + " IN (SELECT distinct "
					+ ColumnEnum.ID_SOURCE.getColumnName() + " FROM (");
			query.append(querySelection(selectedEntries));
			query.append(") q1 ) ");
		}
		query.append("RETURNING 1) select count(1) from TMP_DELETE;");
		return UtilitaireDao.get(0).getInt(sandbox.getConnection(), query);
	}

	
	/**
	 * put select file entries in a replay state
	 * @param phase
	 * @param selectedEntries
	 * @throws ArcException
	 */
	public void executeReplayPhaseEntriesInPilotage(TraitementPhase phase, List<String> selectedEntries)
			throws ArcException {
		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
		requete.append("UPDATE  " + ViewEnum.PILOTAGE_FICHIER.getFullName(sandbox.getSchema())
				+ " set to_delete="+requete.quoteText(TraitementOperationFichier.R.getDbValue())+" WHERE phase_traitement = " + requete.quoteText(phase.toString()) + " ");
		if (!selectedEntries.isEmpty()) {
			requete.append("AND " + ColumnEnum.ID_SOURCE.getColumnName() + " IN (SELECT distinct "
					+ ColumnEnum.ID_SOURCE.getColumnName() + " FROM (");
			requete.append(querySelection(selectedEntries));
			requete.append(") q1 ) ");
		}
		UtilitaireDao.get(0).executeRequest(sandbox.getConnection(), requete);
	}

	/**
	 * 
	 * @param selectedEntries
	 * @return
	 */
	private ArcPreparedStatementBuilder querySelection(List<String> selectedEntries) {
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		for (int i = 0; i < selectedEntries.size(); i++) {
			if (query.length() > 0) {
				query.append("\n UNION ALL SELECT ");
			} else {
				query.append("SELECT ");
			}
			query.append(" " + query.quoteText(selectedEntries.get(i)) + "::text as id_source ");
		}

		return query;
	}

}
