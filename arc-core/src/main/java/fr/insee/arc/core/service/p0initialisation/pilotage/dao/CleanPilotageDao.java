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
		query.appendNewLine("CREATE TEMPORARY TABLE fichier_to_delete AS ");
		query.appendNewLine("WITH ")

				// 1. on récupère sous forme de tableau les clients de chaque famille
				.appendNewLine("clients_par_famille AS ( ") //
				.appendNewLine("SELECT array_agg(id_application) as client, id_famille ") //
				.appendNewLine("FROM arc.ihm_client ") //
				.appendNewLine("GROUP BY id_famille ") //
				.appendNewLine(") ") //

				// 2. on fait une première selection des fichiers candidats au Delete
				.appendNewLine(", fichiers_recuperes_clients AS ( ") //
				.appendNewLine("SELECT " + ColumnEnum.ID_SOURCE.getColumnName() + ", container, date_client as date_action") //
				.appendNewLine("FROM ") //
				.appendNewLine(ViewEnum.PILOTAGE_FICHIER.getFullName(sandbox.getSchema())).append(" a ") //
				.appendNewLine(", arc.ihm_norme b, clients_par_famille c ") //
				.appendNewLine("WHERE a.phase_traitement='" + TraitementPhase.MAPPING + "' ") //
				.appendNewLine("AND a.etat_traitement='{" + TraitementEtat.OK + "}' AND a.client is not null ") //
				.appendNewLine("AND a.id_norme=b.id_norme AND a.periodicite=b.periodicite ") //
				.appendNewLine("AND b.id_famille=c.id_famille ") //
				// on filtre selon RG1
				.appendNewLine("AND (a.client <@ c.client AND c.client <@ a.client) ") //
				// test d'égalité des 2 tableaux (a.client,c.client)
				.appendNewLine(") ") //
				// par double inclusion (A dans B & B dans A)

				.appendNewLine(", fichiers_ko AS ( ") //
				.appendNewLine("SELECT " + ColumnEnum.ID_SOURCE.getColumnName() + ", container, array[date_traitement] as date_action") //
				.appendNewLine("FROM ") //
				.appendNewLine(ViewEnum.PILOTAGE_FICHIER.getFullName(sandbox.getSchema())).append(" a ") //
				.appendNewLine(", arc.ihm_norme b, clients_par_famille c ") //
				.appendNewLine("WHERE a.etape=2 ") //
				.appendNewLine("AND a.etat_traitement='{" + TraitementEtat.KO + "}' ") //
				.appendNewLine("AND a.id_norme=b.id_norme AND a.periodicite=b.periodicite ") //
				.appendNewLine("AND b.id_famille=c.id_famille ") //
				.appendNewLine(") ") //
				
				.appendNewLine(", fichiers_a_analyser AS ( ") //
				.appendNewLine("SELECT * FROM fichiers_recuperes_clients ") //
				.appendNewLine("UNION ALL ") //
				.appendNewLine("SELECT * FROM fichiers_ko ") //
				.appendNewLine(") ") //
				
				// 3. on selectionne les fichiers éligibles
				.appendNewLine("SELECT " + ColumnEnum.ID_SOURCE.getColumnName()
						+ ", container FROM (SELECT unnest(date_action) as t, " + ColumnEnum.ID_SOURCE.getColumnName()
						+ ", container FROM fichiers_a_analyser) ww ")
				.appendNewLine("GROUP BY " + ColumnEnum.ID_SOURCE.getColumnName() + ", container ")
				// on filtre selon RG2
				.appendNewLine("HAVING (current_date - max(t) ::date ) >=" + numberOfDaysToKeepFiles + " ").append("; ");

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

				// 5b. suppression de la table des fichiers eligibles
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
