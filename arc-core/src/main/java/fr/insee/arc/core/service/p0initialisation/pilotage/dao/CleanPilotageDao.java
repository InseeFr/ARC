package fr.insee.arc.core.service.p0initialisation.pilotage.dao;

import java.util.List;
import java.util.Map;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.utils.FormatSQL;

public class CleanPilotageDao {

	public CleanPilotageDao(Sandbox sandbox) {
		super();
		this.sandbox = sandbox;
	}

	private Sandbox sandbox;

	public void execQueryMaterializeFilesToDelete(int numberOfDaysToKeepFiles) throws ArcException {
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();

		query.append("DROP TABLE IF EXISTS fichier_to_delete; ");
		query.append("CREATE TEMPORARY TABLE fichier_to_delete AS ");
		query.append("WITH ")

				// 1. on récupère sous forme de tableau les clients de chaque famille
				.append("clientsParFamille AS ( ").append("SELECT array_agg(id_application) as client, id_famille ")
				.append("FROM arc.ihm_client ").append("GROUP BY id_famille ").append(") ")

				// 2. on fait une première selection des fichiers candidats au Delete
				.append(",isFichierToDelete AS ( ")
				.append("SELECT " + ColumnEnum.ID_SOURCE.getColumnName() + ", container, date_client ").append("FROM ")
				.append(ViewEnum.PILOTAGE_FICHIER.getFullName(sandbox.getSchema())).append(" a ")
				.append(", arc.ihm_norme b ").append(", clientsParFamille c ")
				.append("WHERE a.phase_traitement='" + TraitementPhase.MAPPING + "' ")
				.append("AND a.etat_traitement='{" + TraitementEtat.OK + "}' ").append("AND a.client is not null ")
				.append("AND a.id_norme=b.id_norme ").append("AND a.periodicite=b.periodicite ")
				.append("AND b.id_famille=c.id_famille ")
				// on filtre selon RG1
				.append("AND (a.client <@ c.client AND c.client <@ a.client) ")
				// test d'égalité des 2 tableaux (a.client,c.client)
				.append(") ")
				// par double inclusion (A dans B & B dans A)

				// 3. on selectionne les fichiers éligibles
				.append("SELECT " + ColumnEnum.ID_SOURCE.getColumnName()
						+ ", container FROM (SELECT unnest(date_client) as t, " + ColumnEnum.ID_SOURCE.getColumnName()
						+ ", container FROM isFichierToDelete) ww ")
				.append("GROUP BY " + ColumnEnum.ID_SOURCE.getColumnName() + ", container ")
				// on filtre selon RG2
				.append("HAVING (current_date - max(t) ::date ) >=" + numberOfDaysToKeepFiles + " ").append("; ");

		UtilitaireDao.get(0).executeRequest(sandbox.getConnection(), query);
	}

	
	/**
	 * delete from pilotage table and archive table the files
	 * return the list of deleted files
	 * @param numberOfFilesToProceed
	 * @return
	 * @throws ArcException
	 */
	public Map<String, List<String>> execQueryDeleteDeprecatedFilesAndSelectArchives(int numberOfFilesToProceed) throws ArcException {
		// requete sur laquelle on va itérer : on selectionne un certain nombre de
		// fichier et on itere
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();

		// 3b. on selectionne les fichiers éligibles et on limite le nombre de retour
		// pour que l'update ne soit pas trop massif (perf)
		query.append("WITH fichier_to_delete_limit AS ( ")
				.append(" SELECT * FROM fichier_to_delete LIMIT " + numberOfFilesToProceed + " ").append(") ")

				// 4. suppression des archive de la table d'archive (bien retirer le nom de
				// l'entrepot du début du container)
				.append(",delete_archive AS (").append("DELETE FROM ")
				.append(ViewEnum.PILOTAGE_ARCHIVE.getFullName(sandbox.getSchema())).append(" a ")
				.append("USING fichier_to_delete_limit b ")
				.append("WHERE a.nom_archive=substring(b.container,strpos(b.container,'_')+1) ").append("returning *) ")

				// 5. suppression des fichier de la table de pilotage
				.append(",delete_idsource AS (").append("DELETE FROM ")
				.append(ViewEnum.PILOTAGE_FICHIER.getFullName(sandbox.getSchema())).append(" a ")
				.append("USING fichier_to_delete_limit b ")
				.append("WHERE a." + ColumnEnum.ID_SOURCE.getColumnName() + "=b." + ColumnEnum.ID_SOURCE.getColumnName()
						+ " ")
				.append(") ")

				// 5b. suppression de la tgable des fichiers eligibles
				.append(",delete_source as (DELETE FROM fichier_to_delete a using fichier_to_delete_limit b where row(a."
						+ ColumnEnum.ID_SOURCE.getColumnName() + ",a.container)::text=row(b."
						+ ColumnEnum.ID_SOURCE.getColumnName() + ",b.container)::text) ")
				// 6. récuperer la liste des archives
				.append("SELECT entrepot, nom_archive FROM delete_archive ");
		return new GenericBean(UtilitaireDao.get(0).executeRequest(sandbox.getConnection(), query)).mapContent();
	}
	
	public boolean execQueryIsStillSomethingToDelete() throws ArcException
	{
		return UtilitaireDao.get(0).hasResults(sandbox.getConnection(),
				new ArcPreparedStatementBuilder("select 1 from fichier_to_delete limit 1"));
	}
	
	public void execQueryMaintenancePilotage() throws ArcException 
	{
		StringBuilder query = new StringBuilder();
		query.append(FormatSQL.vacuumSecured(ViewEnum.PILOTAGE_FICHIER.getFullName(sandbox.getSchema()), FormatSQL.VACUUM_OPTION_ANALYZE));
		query.append(FormatSQL.vacuumSecured(ViewEnum.PILOTAGE_ARCHIVE.getFullName(sandbox.getSchema()), FormatSQL.VACUUM_OPTION_ANALYZE));
		
		UtilitaireDao.get(0).executeImmediate(sandbox.getConnection(), query);
	}
	

}
