package fr.insee.arc.core.service.p1reception.registerfiles;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementRapport;
import fr.insee.arc.core.model.TraitementTypeFichier;
import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.core.service.global.dao.TableNaming;
import fr.insee.arc.core.service.global.dao.TableOperations;
import fr.insee.arc.core.service.p0initialisation.pilotage.SynchronizeDataByPilotage;
import fr.insee.arc.core.service.p1reception.ApiReceptionService;
import fr.insee.arc.core.service.p1reception.provider.DirectoryPath;
import fr.insee.arc.core.service.p1reception.registerarchive.bo.FileDescriber;
import fr.insee.arc.core.service.p1reception.registerarchive.bo.FilesDescriber;
import fr.insee.arc.core.service.p1reception.registerarchive.bo.GzReader;
import fr.insee.arc.core.service.p1reception.registerarchive.bo.TgzReader;
import fr.insee.arc.core.service.p1reception.registerarchive.bo.ZipReader;
import fr.insee.arc.core.service.p1reception.registerarchive.dao.DirectoriesDao;
import fr.insee.arc.core.service.p1reception.registerarchive.dao.MoveFilesToRegisterDao;
import fr.insee.arc.core.service.p1reception.registerarchive.operation.ArchiveCheckOperation;
import fr.insee.arc.core.service.p1reception.registerarchive.operation.ReworkArchiveOperation;
import fr.insee.arc.core.service.p1reception.registerfiles.dao.FileRegistrationDao;
import fr.insee.arc.core.service.p1reception.registerfiles.provider.ContainerName;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.files.CompressedUtils;
import fr.insee.arc.utils.files.CompressionExtension;
import fr.insee.arc.utils.files.FileUtilsArc;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.ManipString;

public class FileRegistration {

	private static final Logger LOGGER = LogManager.getLogger(FileRegistration.class);

	public FileRegistration(Sandbox sandbox, String tablePilTemp) {
		super();
		this.sandbox = sandbox;
		this.tablePilTemp = tablePilTemp;
		this.tablePil = ViewEnum.PILOTAGE_FICHIER.getFullName(sandbox.getSchema());
		directories = new DirectoriesDao(sandbox);
	}

	private Sandbox sandbox;
	private DirectoriesDao directories;
	private String tablePilTemp;
	private String tablePil;
	
	/**
	 * Enregistrer les fichiers en entrée Déplacer les fichier reçus dans les
	 * repertoires OK ou pas OK selon le bordereau Supprimer les fichiers déjà
	 * existants de la table de pilotage Marquer les fichiers dans la table de
	 * pilotage
	 * @throws ArcException 
	 */
	public void registerAndDispatchFiles(FilesDescriber providedArchiveContent) throws ArcException {
		StaticLoggerDispatcher.info(LOGGER, "registerAndDispatchFiles");
	
		// la bean (fileName,type, etat) contient pour chaque fichier, le type
		// du fichier et l'action à réaliser

			FilesDescriber archiveContent = findDuplicates(providedArchiveContent);
			
			StringBuilder requete = new StringBuilder();
			requete.append(TableOperations.creationTableResultat(this.tablePil, this.tablePilTemp));
			soumettreRequete(requete);

			if (archiveContent.getFilesAttribute().isEmpty()) {
				return;
			}
				String dirIn = directories.getDirEnCours();
				
				
				for (FileDescriber f : archiveContent.getFilesAttribute()) {

					String containerNewName = ContainerName.buildContainerName(f.getContainerName());
					
					if (f.getTypeOfFile().equals(TraitementTypeFichier.DA)) {
						FileRegistrationDao.insertPilotage(requete, this.tablePilTemp, f.getContainerName()
								, containerNewName, f.getVirtualContainer(), f.getFileName(),
								f.getEtat(), f.getReport());
					}
					if (f.getTypeOfFile().equals(TraitementTypeFichier.A)) {
						String dirOut = DirectoryPath.directoryReceptionEtat(directories.getDirectoryRoot(),
								sandbox.getSchema(), f.getEtat());
						FileUtilsArc.deplacerFichier(dirIn, dirOut, f.getContainerName(), containerNewName);
					}
					if (f.getTypeOfFile().equals(TraitementTypeFichier.AC)) {
						String dirOut = DirectoryPath.directoryReceptionEtat(this.directories.getDirectoryRoot(),
								sandbox.getSchema(), f.getEtat());
						FileUtilsArc.deplacerFichier(dirIn, dirOut, f.getContainerName(), containerNewName);
						FileRegistrationDao.insertPilotage(requete, this.tablePilTemp, f.getContainerName(), containerNewName, f.getVirtualContainer(), f.getFileName(),
								f.getEtat(), f.getReport());
					}
					// pour les fichier seul, on en fait une archive
					if (f.getTypeOfFile().equals(TraitementTypeFichier.D)) {
						// en termes de destination, les fichiers seuls vont tout le temps dans
						// RECEPTION_OK, même s'ils sont KO pour la table
						// de pilotage
						String dirOut = DirectoryPath.directoryReceptionEtatOK(this.directories.getDirectoryRoot(),
								sandbox.getSchema());
						File fileIn = new File(dirIn + File.separator + f.getFileName());
						File fileOut = new File(dirOut + File.separator + containerNewName);

						if (fileOut.exists()) {
							FileUtilsArc.delete(fileOut);
						}
						CompressedUtils.generateTarGzFromFile(fileIn, fileOut,
								ManipString.substringAfterFirst(fileIn.getName(), "_"));
						FileUtilsArc.delete(fileIn);
						FileRegistrationDao.insertPilotage(requete, this.tablePilTemp, f.getContainerName(), containerNewName, f.getVirtualContainer()
								, f.getFileName(), f.getEtat(), f.getReport());

					}
				}
				requete.append(";");
				soumettreRequete(requete);

				StringBuilder query= new StringBuilder("select distinct " + ColumnEnum.ID_SOURCE.getColumnName() + " from " + this.tablePil
							+ " a where to_delete='R' and exists (select 1 from " + this.tablePilTemp + " b where a."
							+ ColumnEnum.ID_SOURCE.getColumnName() + "=b." + ColumnEnum.ID_SOURCE.getColumnName()
							+ ")");
				
				List<String> idSourceToBeDeleted = new GenericBean(UtilitaireDao.get(0).executeRequest(sandbox.getConnection(), new ArcPreparedStatementBuilder(query))).mapContent().get(ColumnEnum.ID_SOURCE.getColumnName());

				if (idSourceToBeDeleted!=null) {
					// marque les fichiers à effacer (ils vont etre rechargés)
					requete.append("CREATE TEMPORARY TABLE a_rejouer " + FormatSQL.WITH_NO_VACUUM +" AS ");
					requete.append(query);
					requete.append(";");

					// effacer de la table pilotage des to_delete à R
					requete.append("DELETE FROM " + this.tablePil + " a using a_rejouer b where a."
							+ ColumnEnum.ID_SOURCE.getColumnName() + "=b." + ColumnEnum.ID_SOURCE.getColumnName()
							+ "; ");
				}

				// pb des archives sans nom de fichier
				requete.append("UPDATE " + this.tablePilTemp + " set " + ColumnEnum.ID_SOURCE.getColumnName()
						+ "='' where " + ColumnEnum.ID_SOURCE.getColumnName() + " is null; ");
				requete.append("INSERT INTO " + this.tablePil + " select * from " + this.tablePilTemp + "; \n");
				requete.append("DISCARD TEMP; \n");
				soumettreRequete(requete);
				
				if (idSourceToBeDeleted!=null) {
					SynchronizeDataByPilotage synchronizationInstance =  new SynchronizeDataByPilotage(this.sandbox);
					synchronizationInstance.dropUnusedDataTablesAllNods(idSourceToBeDeleted);
					synchronizationInstance.deleteUnusedDataRecordsAllNods(idSourceToBeDeleted);
				}
	}
	
	
	/**
	 * Find the duplicates files in the database
	 * 
	 * @param fileList
	 * @return
	 */
	private FilesDescriber findDuplicates(FilesDescriber fileList) {
		
		
		FilesDescriber content = new FilesDescriber();
		content.addAll(fileList);
		
		// Localiser les doublons
		// Note : l'insertion est redondante mais au niveau métier, c'est
		// beaucoup plus logique
		StaticLoggerDispatcher.info(LOGGER, "Recherche de doublons de fichiers");

		StringBuilder requete = new StringBuilder();
		requete.append(TableOperations.creationTableResultat(this.tablePil, this.tablePilTemp));
		String fileName;
		String container;

		// insertion des fichiers dans la table tablePilTemp
		for (FileDescriber f:content.getFilesAttribute()) {
			container = f.getContainerName();
			fileName = f.getFileName();
			
			
			if (fileName != null) {
				requete.append(
						"insert into " + this.tablePilTemp + " (container, " + ColumnEnum.ID_SOURCE.getColumnName()
								+ ") values (" + FormatSQL.cast(container) + "," + FormatSQL.cast(fileName) + "); \n");
			}
		}
		soumettreRequete(requete);
		// detection des doublons de fichiers sur les id_source juste insérés
		// faut comparer les id_sources en retirant le #nnn représentant le numéro de
		// l'archive (on utilise le regexp_replace pour retirer le #nnn)

		requete.append("select container, " + ColumnEnum.ID_SOURCE.getColumnName() + " FROM " + this.tablePilTemp
				+ " where " + ColumnEnum.ID_SOURCE.getColumnName() + " in ( ");
		requete.append("select distinct " + ColumnEnum.ID_SOURCE.getColumnName() + " from ( ");
		requete.append("select " + ColumnEnum.ID_SOURCE.getColumnName() + ", count(1) over (partition by "
				+ ColumnEnum.ID_SOURCE.getColumnName() + ") as n from " + this.tablePilTemp + " ");
		requete.append(") ww where n>1 ");
		requete.append(") ");
		// detection des doublons de fichiers dans la table de pilotage
		requete.append("UNION ");
		requete.append(
				"SELECT container, " + ColumnEnum.ID_SOURCE.getColumnName() + " from " + this.tablePilTemp + " a ");
		requete.append("where exists (select 1 from " + this.tablePil + " b where a."
				+ ColumnEnum.ID_SOURCE.getColumnName() + "=b." + ColumnEnum.ID_SOURCE.getColumnName() + ") \n");
		requete.append("and a." + ColumnEnum.ID_SOURCE.getColumnName() + " not in (select distinct "
				+ ColumnEnum.ID_SOURCE.getColumnName() + " from " + this.tablePil + " b where b.to_delete='R') ;\n");

		// récupérer les doublons pour mettre à jour le dispatcher
		try {
			ArrayList<String> listIdsourceDoublons = new GenericBean(UtilitaireDao.get(0).executeRequest(
					sandbox.getConnection(), new ArcPreparedStatementBuilder(requete))).mapContent()
					.get(ColumnEnum.ID_SOURCE.getColumnName());

			// on va parcourir la liste des fichiers
			// si on retrouve l'id_source dans la liste, on le marque en erreur
			if (listIdsourceDoublons != null) {
				for (FileDescriber f: content.getFilesAttribute()) {
					// si le nom de fichier est renseigné et retrouvé dans la liste
					// on passe l'état à KO et on marque l'anomalie
					if (f.getFileName() != null) {
						if (listIdsourceDoublons.contains(f.getFileName())) {
							f.setEtat(TraitementEtat.KO);
							f.setReport(TraitementRapport.INITIALISATION_DUPLICATE.toString());
						}
					}
				}
			}
		} catch (ArcException ex) {
			LoggerHelper.errorGenTextAsComment(getClass(), "dispatchFiles()", LOGGER, ex);
		}

		// on ignore les doublons de l'archive pour les fichiers à rejouer
		// on recrée un nouvelle liste en ne lui ajoutant pas ces doublons à ignorer
		requete = new StringBuilder();
		requete.append("SELECT container, container||'>'||" + ColumnEnum.ID_SOURCE.getColumnName() + " as "
				+ ColumnEnum.ID_SOURCE.getColumnName() + " from " + this.tablePilTemp + " a ");
		requete.append("where exists (select 1 from " + this.tablePil + " b where to_delete='R' and a."
				+ ColumnEnum.ID_SOURCE.getColumnName() + "=b." + ColumnEnum.ID_SOURCE.getColumnName() + ") ;\n");

		FilesDescriber content2 = new FilesDescriber();
		try {
			HashMap<String, ArrayList<String>> m = new GenericBean(UtilitaireDao.get(0).executeRequest(
					this.sandbox.getConnection(), new ArcPreparedStatementBuilder(requete))).mapContent();
			ArrayList<String> listContainerARejouer = m.get(ColumnEnum.CONTAINER.getColumnName());
			ArrayList<String> listIdsourceARejouer = m.get(ColumnEnum.ID_SOURCE.getColumnName());

			if (listIdsourceARejouer == null) {
				content2 = content;
			} else {
				for (FileDescriber z : content.getFilesAttribute()) {
					// si le fichier est dans la liste des doublons à ignorer, on le l'ajoute pas à
					// la nouvelle liste
					if (z.getFileName() != null) {
						if (listContainerARejouer.contains(z.getContainerName())) {
							// si on trouve le fichier à rejouer, on l'ajoute; on ignore les autres
							if (listIdsourceARejouer.contains(
									z.getContainerName() + ">" + z.getFileName())) {
								content2.add(z);
							}
						} else {
							content2.add(z);
						}
					} else {
						// bien ajouter les caracteriqtique de l'archive à la nouvelle liste
						content2.add(z);
					}
				}
			}

		} catch (ArcException ex) {
			LoggerHelper.errorGenTextAsComment(getClass(), "dispatchFiles()", LOGGER, ex);
		}
		content = content2;

		// detection des doublons d'archive. Génération d'un numéro pour
		// l'archive en cas de doublon

		requete = new StringBuilder();
		// insertion des fichiers d'archive corrompue dans la table
		// tablePilTemp
		// on doit aussi leur donner un numéro
		for (FileDescriber z : content.getFilesAttribute()) {
			container = z.getContainerName();
			fileName = z.getFileName();

			if (z.getTypeOfFile().equals(TraitementTypeFichier.AC.toString())) {
				requete.append(
						"insert into " + this.tablePilTemp + " (container, " + ColumnEnum.ID_SOURCE.getColumnName()
								+ ") values (" + FormatSQL.cast(container) + "," + FormatSQL.cast(fileName) + "); \n");
			}
		}
		soumettreRequete(requete);

		requete.append("select container ");
		requete.append(" , coalesce((select max(v_container::integer)+1 from  " + this.tablePil
				+ " b where a.container=b.o_container),1)::text as v_container ");
		requete.append(
				"from (select distinct container from " + this.tablePilTemp + " where container is not null) a ");
		
		
		try {
			HashMap<String, ArrayList<String>> m = new GenericBean(UtilitaireDao.get(0).executeRequest(
					sandbox.getConnection(), new ArcPreparedStatementBuilder(requete))).mapContent();
			ArrayList<String> listContainerDoublons = m.get(ColumnEnum.CONTAINER.getColumnName());
			ArrayList<String> listVersionContainerDoublons = m.get(ColumnEnum.V_CONTAINER.getColumnName());
			if (listContainerDoublons != null) {
				for (FileDescriber z : content.getFilesAttribute()) {
					container = z.getContainerName();
					if (container != null) {
						z.setVirtualContainer(listVersionContainerDoublons.get(listContainerDoublons.indexOf(container)));
					}
				}
			}
		} catch (ArcException ex) {
			LoggerHelper.errorGenTextAsComment(getClass(), "dispatchFiles()", LOGGER, ex);
		}
		requete.setLength(0);
		requete.append(FormatSQL.dropTable(this.tablePilTemp));
		soumettreRequete(requete);
		return content;
	}
	
	private void soumettreRequete(StringBuilder requete) {
		try {
			UtilitaireDao.get(0).executeImmediate(this.sandbox.getConnection(), requete);
		} catch (ArcException ex) {
			LoggerHelper.errorGenTextAsComment(getClass(), "soumettreRequete()", LOGGER, ex);
		}
		requete.setLength(0);
	}
	
}
