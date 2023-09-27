package fr.insee.arc.core.service.p0initialisation.pilotage;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.dataobjects.ArcDatabase;
import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.core.service.global.dao.TableNaming;
import fr.insee.arc.core.service.p1reception.provider.DirectoryPath;
import fr.insee.arc.core.util.BDParameters;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.files.FileUtilsArc;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.utils.LoggerHelper;

/**
 * remove deprecated files from a target sandbox
 * @author FY2QEQ
 *
 */
public class CleanPilotage {

	private static final Logger LOGGER = LogManager.getLogger(CleanPilotage.class);

	public CleanPilotage(Sandbox sandbox) {
		super();
		this.sandbox = sandbox;
	}
	
	private Sandbox sandbox;
	
	/**
	 * Suppression dans la table de pilotage des fichiers consommés 1- une copie des
	 * données du fichier doit avoir été récupérée par tous les clients décalrés 2-
	 * pour un fichier donné, l'ancienneté de son dernier transfert doit dépasser
	 * Nb_Jour_A_Conserver jours RG2.
	 *
	 * @param targetSandbox.getConnection()
	 * @param tablePil
	 * @param tablePil
	 * @throws ArcException
	 */
	public void removeDeprecatedFiles() throws ArcException {
		LoggerHelper.info(LOGGER, "nettoyerTablePilotage");

		BDParameters bdParameters = new BDParameters(ArcDatabase.COORDINATOR);

		// indique combien de jour doivent etre conservé les fichiers apres avoir été
		int numberOfDaysToKeepFiles = bdParameters.getInt(sandbox.getConnection(),
				"ApiInitialisationService.Nb_Jour_A_Conserver", 365);

		// nombre de fichier à traiter lors à chaque itération d'archivage
		int numberOfFilesToProceed = bdParameters.getInt(sandbox.getConnection(),
				"ApiInitialisationService.NB_FICHIER_PER_ARCHIVE", 10000);

		String nomTablePilotage = TableNaming.dbEnv(sandbox.getSchema()) + "pilotage_fichier";
		String nomTableArchive = TableNaming.dbEnv(sandbox.getSchema()) + "pilotage_archive";

		ArcPreparedStatementBuilder requete;

		requete = new ArcPreparedStatementBuilder();

		requete.append("DROP TABLE IF EXISTS fichier_to_delete; ");
		requete.append("CREATE TEMPORARY TABLE fichier_to_delete AS ");
		requete.append("WITH ")

				// 1. on récupère sous forme de tableau les clients de chaque famille
				.append("clientsParFamille AS ( ").append("SELECT array_agg(id_application) as client, id_famille ")
				.append("FROM arc.ihm_client ").append("GROUP BY id_famille ").append(") ")

				// 2. on fait une première selection des fichiers candidats au Delete
				.append(",isFichierToDelete AS (	 ")
				.append("SELECT " + ColumnEnum.ID_SOURCE.getColumnName() + ", container, date_client ").append("FROM ")
				.append(nomTablePilotage).append(" a ").append(", arc.ihm_norme b ").append(", clientsParFamille c ")
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

		UtilitaireDao.get(0).executeRequest(sandbox.getConnection(), requete);

		// requete sur laquelle on va itérer : on selectionne un certain nombre de
		// fichier et on itere
		requete = new ArcPreparedStatementBuilder();

		// 3b. on selectionne les fichiers éligibles et on limite le nombre de retour
		// pour que l'update ne soit pas trop massif (perf)
		requete.append("WITH fichier_to_delete_limit AS ( ")
				.append(" SELECT * FROM fichier_to_delete LIMIT " + numberOfFilesToProceed + " ").append(") ")

				// 4. suppression des archive de la table d'archive (bien retirer le nom de
				// l'entrepot du début du container)
				.append(",delete_archive AS (").append("DELETE FROM ").append(nomTableArchive).append(" a ")
				.append("USING fichier_to_delete_limit b ")
				.append("WHERE a.nom_archive=substring(b.container,strpos(b.container,'_')+1) ").append("returning *) ")

				// 5. suppression des fichier de la table de pilotage
				.append(",delete_idsource AS (").append("DELETE FROM ").append(nomTablePilotage).append(" a ")
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

		// initialisation de la liste contenant les archives à déplacer
		HashMap<String, ArrayList<String>> m = new HashMap<>();
		m.put("entrepot", new ArrayList<String>());
		m.put("nom_archive", new ArrayList<String>());

		HashMap<String, ArrayList<String>> n = new HashMap<>();

		// on continue jusqu'a ce qu'on ne trouve plus rien à effacer
		do {
			// récupérer le résultat de la requete
			LoggerHelper.info(LOGGER, "Archivage de " + numberOfFilesToProceed + " fichiers - Début");
			n = new GenericBean(UtilitaireDao.get(0).executeRequest(sandbox.getConnection(), requete)).mapContent();

			// ajouter à la liste m les enregistrements qu'ils n'existent pas déjà dans m

			// on parcours n
			if (!n.isEmpty()) {
				for (int k = 0; k < n.get("entrepot").size(); k++) {
					boolean toInsert = true;

					// vérifier en parcourant m si on doit réaliser l'insertion
					for (int l = 0; l < m.get("entrepot").size(); l++) {
						if (n.get("entrepot").get(k).equals(m.get("entrepot").get(l))
								&& n.get("nom_archive").get(k).equals(m.get("nom_archive").get(l))) {
							toInsert = false;
							break;
						}
					}

					// si aprés avoir parcouru tout m, l'enreigstrement de n n'est pas trouvé on
					// l'insere
					if (toInsert) {
						m.get("entrepot").add(n.get("entrepot").get(k));
						m.get("nom_archive").add(n.get("nom_archive").get(k));
					}

				}
			}
			LoggerHelper.info(LOGGER, "Archivage Fin");

		} while (UtilitaireDao.get(0).hasResults(sandbox.getConnection(),
				new ArcPreparedStatementBuilder("select 1 from fichier_to_delete limit 1")));

		// y'a-til des choses à faire ?
		if (m.get("entrepot").size() > 0) {

			// 7. Déplacer les archives effacées dans le répertoire de sauvegarde "OLD"
			PropertiesHandler properties = PropertiesHandler.getInstance();
			String repertoire = properties.getBatchParametersDirectory();

			String entrepotSav = "";
			for (int i = 0; i < m.get("entrepot").size(); i++) {
				String entrepot = m.get("entrepot").get(i);
				String archive = m.get("nom_archive").get(i);
				String dirIn = DirectoryPath.directoryReceptionEntrepotArchive(repertoire, this.sandbox.getSchema(),
						entrepot);
				String dirOut = DirectoryPath.directoryReceptionEntrepotArchiveOldYearStamped(repertoire,
						this.sandbox.getSchema(), entrepot);

				// création du répertoire "OLD" s'il n'existe pas
				if (!entrepotSav.equals(entrepot)) {
					File f = new File(dirOut);
					FileUtilsArc.createDirIfNotexist(f);
					entrepotSav = entrepot;
				}

				// déplacement de l'archive de dirIn vers dirOut
				FileUtilsArc.deplacerFichier(dirIn, dirOut, archive, archive);

			}

			StringBuilder requeteMaintenance = new StringBuilder();
			requete.append("vacuum analyze " + nomTablePilotage + "; ");
			requete.append("vacuum analyze " + nomTableArchive + "; ");
			UtilitaireDao.get(0).executeImmediate(sandbox.getConnection(), requeteMaintenance);
		}

	}
	
	
}
