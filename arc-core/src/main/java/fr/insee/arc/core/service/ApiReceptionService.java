package fr.insee.arc.core.service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;
import org.springframework.stereotype.Component;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.model.TraitementRapport;
import fr.insee.arc.core.model.TraitementTypeFichier;
import fr.insee.arc.core.util.BDParameters;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.files.FileUtilsArc;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.ManipString;


/**
 * ApiReceptionService
 *
 *     	1- Déplacer les enveloppes de l'entrepot vers EN_COURS. Horodater et archiver les enveloppes recues</br>
 * 		2- Enregistrer n enveloppes dans la base pour traitement
 * 			2-1 Vérification de l'intégrité de l'enveloppe
 * 			2_2 Identification des doublons de fichier
 * 			2-2 Enregistrement des fichiers et de leur enveloppe dans la table de pilotage
 *
 * @author Manuel SOULIER
 *
 */
@Component
public class ApiReceptionService extends ApiService {
	
	
	// Headers for the generic bean describing the files
	private static final String GB_CONTAINER = "container";
	private static final String GB_FILENAME = "fileName";
	private static final String GB_TYPE = "type";
	private static final String GB_STATE = "etat";
	private static final String GB_REPORT = "rapport";
	private static final String GB_VCONTAINER = "v_container";
	private static final ArrayList<String> GENERIC_BEAN_HEADERS = new ArrayList<>(Arrays.asList(GB_CONTAINER, GB_FILENAME, GB_TYPE, GB_STATE, GB_REPORT, GB_VCONTAINER));
	private static final ArrayList<String> GENERIC_BEAN_TYPES = new ArrayList<>(Arrays.asList("text", "text", "text", "text", "text", "text"));
	
	public ApiReceptionService() {
		super();
	}

	public static final int READ_BUFFER_SIZE = 131072;
	
	private static final Logger LOGGER = LogManager.getLogger(ApiReceptionService.class);
	
	//Expression régulière correspondant au nom des fichiers temporaires 
	//transmis via le flux Oriade (soit XXXXXX-W, avec X dans [A-Z])
	private static final Pattern p = Pattern.compile("^[A-Z]{6}-W.*");
		
	
	public ApiReceptionService(String aCurrentPhase, String anParametersEnvironment, String aEnvExecution, String aDirectoryRoot, Integer aNbEnr, String... paramBatch) {
		super(aCurrentPhase, anParametersEnvironment, aEnvExecution, aDirectoryRoot, aNbEnr, paramBatch);
	}


	@Override
	public void executer() {
		// Déplacement et archivage des fichiers
		
		int maxNumberOfFiles;
		
		if (paramBatch != null)
		{
			maxNumberOfFiles = BDParameters.getInt(null, "ApiReceptionService.batch.maxNumberOfFiles",25000);
		}
		else
		{
			maxNumberOfFiles = BDParameters.getInt(null, "ApiReceptionService.ihm.maxNumberOfFiles",5000);
		}
		// Enregistrement des fichiers
		GenericBean archiveContent =  moveAndCheckClientFiles(this.nbEnr, maxNumberOfFiles);
		if (archiveContent != null)
		{
			registerAndDispatchFiles(this.connexion.getCoordinatorConnection(), archiveContent);
		}
	}


	/**
	 * Initialize the application directories if needed
	 * List the the files received and start to move to the processing directory
	 * @param fileSizeLimit
	 * @param maxNumberOfFiles
	 * @return
	 */
	private GenericBean moveAndCheckClientFiles(int fileSizeLimit, int maxNumberOfFiles) {
		
		GenericBean archivesContent = null;
		
		StaticLoggerDispatcher.info("moveAndCheckClientFiles", LOGGER);
		
		try {
			// Create target directories if they don't exist
			UtilitaireDao.createDirIfNotexist(ApiReceptionService.directoryReceptionEtatEnCours(this.directoryRoot, this.envExecution));
			UtilitaireDao.createDirIfNotexist(ApiReceptionService.directoryReceptionEtatOK(this.directoryRoot, this.envExecution));
			UtilitaireDao.createDirIfNotexist(ApiReceptionService.directoryReceptionEtatKO(this.directoryRoot, this.envExecution));
			HashMap<String, ArrayList<String>> entrepotList = new GenericBean(UtilitaireDao.get("arc").executeRequest(this.connexion.getCoordinatorConnection(),
					new ArcPreparedStatementBuilder("select id_entrepot from arc.ihm_entrepot"))).mapContent();
					
			if (!entrepotList.isEmpty())
			{
				archivesContent = moveAndCheckUntilLimit(fileSizeLimit, maxNumberOfFiles, entrepotList.get("id_entrepot"));
			}
		} catch (Exception ex) {
		    LoggerHelper.errorGenTextAsComment(getClass(), "moveClientFiles()", LOGGER, ex);
		}
		
		return archivesContent;
	}

	/** Moves files into RECEPTION_ENCOURS directory, check if the archives are readable and returns a description of the content of all treated files. */
	private GenericBean moveAndCheckUntilLimit(int fileSizeLimit, int maxNumberOfFiles, ArrayList<String> entrepotIdList)
			throws ArcException {
		String dirOut = directoryReceptionEtatEnCours(this.directoryRoot, this.envExecution);
		GenericBean archivesContent = null;
		int fileSize = 0;
		int fileNb = 0;
		
		StaticLoggerDispatcher.info("Taille limite de fichiers à charger : " + fileSizeLimit, LOGGER);

		for (String d : entrepotIdList) {

			if (fileSize > fileSizeLimit || fileNb > maxNumberOfFiles) {
				setReportNumberOfObject(fileNb);
				break;
			}

			String dirIn = ApiReceptionService.directoryReceptionEntrepot(this.directoryRoot, this.envExecution, d);
			String dirArchive = ApiReceptionService.directoryReceptionEntrepotArchive(this.directoryRoot, this.envExecution, d);
			
			File fDirIn = new File(dirIn);
			// créer le répertoire de l'entrepot et son repertoire archive
			UtilitaireDao.createDirIfNotexist(dirArchive);
			UtilitaireDao.createDirIfNotexist(fDirIn);
			// vérifier le type (répertoire)
			if (fDirIn.isDirectory()) {
				
				File[] filesDirIn = fDirIn.listFiles(); 
				
				// trier par nom
				Arrays.sort(filesDirIn, (f1, f2) -> f1.getName().compareTo(f2.getName()));
				
				
				
				for (File f : filesDirIn) {

					// traiter le fichier
					// s'il n'est pas en cours d'ecriture
					// si ce n'est pas un fichier temporaire Oriade
					// si ce n'est pas le fichier de déclenchement d'une mise en production
					Matcher matcher = p.matcher(f.getName());
					
					if (fr.insee.arc.utils.files.FileUtilsArc.isCompletelyWritten(f)
							// oriade : format des fichiers temporaires
//								&& !f.getName().endsWith(ApiService.SUFFIXE_TEMP_FILE_ORIADE)
							&& !matcher.matches()
							&& !f.getName().equals(ApiService.FICHIER_MISE_EN_PRODUCTION)
							)
					{
						if (fileSize > fileSizeLimit || fileNb > maxNumberOfFiles) {
							setReportNumberOfObject(fileNb);
							break;
						}

						// Archiver le fichier
						// on regarde si le fichier existe déjà dans le repertoire archive; si c'est le cas, on va renommer
						String fname;
						

							for (int i=1; i < Integer.MAX_VALUE; i++)
							{

								// on reprend le nom du fichier
								fname=f.getName();
								boolean isArchive=true;
								
								// les fichiers non archive sont archivés
		                		if (UtilitaireDao.isNotArchive(fname))
		                		{
		                			fname = fname + ".tar.gz";
		                			isArchive = false;
		                		}

								// on ajoute un index au nom du fichier toto.tar.gz devient toto#1.tar.gz
								if (i>1)
								{
									fname = ManipString.substringBeforeFirst(fname, ".") + "#" + i + "." + ManipString.substringAfterFirst(fname, ".");
								}


								File fileOutArchive = new File(dirArchive + File.separator + fname );

								// si le fichier n'existe pas dans le repertoire d'archive
								// on le copie dans archive avec son nouveau nom
								// on change le nom du fichier initial avec son nouveau nom indexé
								// on le déplace dans encours
								// on enregistre le fichier dans la table d'archive
								// on sort de la boucle d'indexation
								if (!fileOutArchive.exists())
								{

									if (isArchive)
									{
										// copie dans archive avec le nouveau nom
										try {
											Files.copy(Paths.get(f.getAbsolutePath()), Paths.get(fileOutArchive.getAbsolutePath()));
										} catch (IOException e) {
											throw new ArcException("Files copy in the ARCHIVE directory failed",e);
										}
										// déplacer le fichier dans encours
										deplacerFichier(dirIn, dirOut, f.getName(), d + "_" + fname);
									}
									else
									{
										// on génére le tar.gz dans archive
										UtilitaireDao.generateTarGzFromFile(f, fileOutArchive, f.getName());
										// on copie le tar.gz dans encours
										File fOut=new File(dirOut + File.separator + d + "_"+ fname);
										try {
											Files.copy(Paths.get(fileOutArchive.getAbsolutePath()), Paths.get(fOut.getAbsolutePath()));
										} catch (IOException e) {
											throw new ArcException("Files copy in the ENCOURS directory failed",e);
										}
										// on efface le fichier source
										f.delete();
									}
									
									fileSize = fileSize + (int)(fileOutArchive.length()/1024/1024);
									
									//
									GenericBean archiveContentTemp=checkArchiveFiles(new File[] {new File(dirOut + File.separator + d + "_"+ fname)});
									
									if (archivesContent == null)
									{
										archivesContent = archiveContentTemp;
									}
									else
									{
										archivesContent.content.addAll(archiveContentTemp.content);
									}
									
									fileNb = fileNb + archiveContentTemp.content.size();

									
									// enregistrer le fichier
									UtilitaireDao.get("arc").executeBlock(
											this.connexion.getCoordinatorConnection(),
											"INSERT INTO " + dbEnv(this.envExecution) + "pilotage_archive (entrepot,nom_archive) values ('" + d + "','" + fname
											+ "'); ");
									break;
								}


							}

					}
				}
			}
		}
		return archivesContent;
	}

	/**
	 * Checks the content of the archive and returns it.
	 *
	 * @param filesIn the archives
	 * @return a GenericBean describing the archive
	 */
	private GenericBean checkArchiveFiles(File[] filesIn) {
		ArrayList<ArrayList<String>> content = new ArrayList<>();
		ArrayList<String> l;
		for (File f : filesIn) {
			String entrepot = ManipString.substringBeforeFirst(f.getName(), "_") + "_";
			if (f.getName().endsWith(".tar.gz") || f.getName().endsWith(".tgz")) {
				content.addAll(checkTgzArchive(f, entrepot));
			} else if (f.getName().endsWith(".zip")) {
				content.addAll(checkZipArchive(f, entrepot));
			} else if (f.getName().endsWith(".gz")) {
				content.addAll(checkGzArchive(f));
			} else {// cas rebus, hors tar.gz, zip et gz
				l = new ArrayList<>();
				l.add(f.getName() + ".tar.gz");
				l.add(f.getName());
				l.add(TraitementTypeFichier.D.toString());
				l.add(TraitementEtat.OK.toString());
				l.add(null);
				l.add(null);
				StaticLoggerDispatcher.info("Insertion du cas rebus : " + l.toString(), LOGGER);
				content.add(l);
			}
		}
		
		return new GenericBean(GENERIC_BEAN_HEADERS, GENERIC_BEAN_TYPES, content);
	}


	/** 
	 * Check every file in the tgz archive and returns the archive content. 
	 * */
	private ArrayList<ArrayList<String>> checkTgzArchive(File f, String entrepot) {
		ArrayList<String> l;
		// Inscription des fichiers au contenu de l'archive
		ArrayList<ArrayList<String>> contentTemp = new ArrayList<>();
		int erreur = 0;
		String rapport = null;
		String etat = null;
		// Check if the archive is fully readable
		try (BufferedInputStream fis = new BufferedInputStream(new FileInputStream(f), READ_BUFFER_SIZE);
				GZIPInputStream gzis = new GZIPInputStream(fis);
				TarInputStream tarInput = new TarInputStream(gzis);){
			erreur = 1;
			rapport = TraitementRapport.INITIALISATION_CORRUPTED_ARCHIVE.toString();
			TarEntry currentEntry = tarInput.getNextEntry();
			// Check every entry
			while (currentEntry != null) {
				if (currentEntry.isDirectory())
				{
					currentEntry = tarInput.getNextEntry();
				}
				else
				{
				l = new ArrayList<>();
				l.add(f.getName());
				l.add(entrepot + currentEntry.getName());
				l.add(TraitementTypeFichier.DA.toString());
				// Check if the entry is readable (by calling nextEntry)
				erreur = 0;
				etat = TraitementEtat.OK.toString();
				rapport = null;
				try {
					currentEntry = tarInput.getNextEntry();
				} catch (IOException e) {
					erreur = 2;
					etat = TraitementEtat.KO.toString();
					rapport = TraitementRapport.INITIALISATION_CORRUPTED_ENTRY.toString();
					currentEntry = null;
				}
				l.add(etat);
				l.add(rapport);
				l.add(null);
				contentTemp.add(l);
				rapport = null;
				}
			}
		} catch (IOException e1) {
			erreur = 1;
			rapport = TraitementRapport.INITIALISATION_CORRUPTED_ARCHIVE.toString();
		}
		
		// Inscription de l'archive
		l = new ArrayList<>();
		l.add(f.getName());
		l.add(null);
		if (erreur == 1) {
			l.add(TraitementTypeFichier.AC.toString());
		} else {
			l.add(TraitementTypeFichier.A.toString());
		}
		if (erreur > 0) {
			l.add(TraitementEtat.KO.toString());
		} else {
			l.add(TraitementEtat.OK.toString());
		}
		l.add(rapport);
		l.add(null);
		contentTemp.add(l);
		propagateErrorToAllFiles(contentTemp, erreur);
		return contentTemp;
	}


	/** 
	 * Check every file in the zip archive and returns the archive content. 
	 * */
	private ArrayList<ArrayList<String>> checkZipArchive(File f, String entrepot) {
		// Inscription des fichiers au contenu de l'archive
		ArrayList<ArrayList<String>> contentTemp = new ArrayList<>();
		ArrayList<String> l;
		int erreur = 0;
		String rapport = null;
		String etat = null;
		// Check if the archive is fully readable
		try (BufferedInputStream fis = new BufferedInputStream(new FileInputStream(f), READ_BUFFER_SIZE);
				ZipArchiveInputStream tarInput = new ZipArchiveInputStream(fis);){
			// Check every entry
			erreur = 1;
			rapport = TraitementRapport.INITIALISATION_CORRUPTED_ARCHIVE.toString();
			ZipArchiveEntry currentEntry = tarInput.getNextZipEntry();
			while (currentEntry != null) {
				l = new ArrayList<>();
				l.add(f.getName());
				l.add(entrepot + currentEntry.getName());
				l.add(TraitementTypeFichier.DA.toString());
				erreur = 0;
				etat = TraitementEtat.OK.toString();
				rapport = null;
				// Check if the entry is readable (by calling nextEntry)
				// If not, currentEntry = null to stop the loop
				try {
					currentEntry = tarInput.getNextZipEntry();
				} catch (IOException e) {
					erreur = 2;
					etat = TraitementEtat.KO.toString();
					rapport = TraitementRapport.INITIALISATION_CORRUPTED_ENTRY.toString();
					currentEntry = null;
				}
				l.add(etat);
				l.add(rapport);
				l.add(null);
				contentTemp.add(l);
				rapport = null;
			}
		} catch (IOException e1) {
			erreur = 1;
			rapport = TraitementRapport.INITIALISATION_CORRUPTED_ARCHIVE.toString();
		}
		// Inscription de l'archive
		l = new ArrayList<>();
		l.add(f.getName());
		l.add(null);
		if (erreur == 1) {
			l.add(TraitementTypeFichier.AC.toString());
		} else {
			l.add(TraitementTypeFichier.A.toString());
		}
		if (erreur > 0) {
			l.add(TraitementEtat.KO.toString());
		} else {
			l.add(TraitementEtat.OK.toString());
		}
		l.add(rapport);
		l.add(null);
		contentTemp.add(l);
		propagateErrorToAllFiles(contentTemp, erreur);
		return contentTemp;
	}


	/** 
	 * Check every file in the gzip archive and returns the archive content. 
	 * */
	private ArrayList<ArrayList<String>> checkGzArchive(File f) {
		// Inscription des fichier au contenu de l'archive
		ArrayList<ArrayList<String>> contentTemp = new ArrayList<>();
		ArrayList<String> l;
		int erreur = 0;
		String rapport = null;
		String etat = null;
		// Check if the archive is fully readable
		try (BufferedInputStream fis = new BufferedInputStream(new FileInputStream(f), READ_BUFFER_SIZE);
				GZIPInputStream tarInput = new GZIPInputStream(fis);){				
	
			l = new ArrayList<>();
			l.add(f.getName());
			l.add(ManipString.substringBeforeLast(f.getName(), ".gz"));
			l.add(TraitementTypeFichier.DA.toString());
			// Check every entry
			try {
				tarInput.read();
				erreur = 0;
				etat = TraitementEtat.OK.toString();
				rapport = null;
			} catch (IOException e) {
				erreur = 2;
				etat = TraitementEtat.KO.toString();
				rapport = TraitementRapport.INITIALISATION_CORRUPTED_ENTRY.toString();
			}
			l.add(etat);
			l.add(rapport);
			l.add(null);
			contentTemp.add(l);
			rapport = null;
		} catch (IOException e1) {
			erreur = 1;
			rapport = TraitementRapport.INITIALISATION_CORRUPTED_ARCHIVE.toString();
		}
		// Inscription de l'archive
		l = new ArrayList<>();
		l.add(f.getName());
		l.add(null);
		if (erreur == 1) {
			l.add(TraitementTypeFichier.AC.toString());
		} else {
			l.add(TraitementTypeFichier.A.toString());
		}
		if (erreur > 0) {
			l.add(TraitementEtat.KO.toString());
		} else {
			l.add(TraitementEtat.OK.toString());
		}
		l.add(rapport);
		l.add(null);
		contentTemp.add(l);
		propagateErrorToAllFiles(contentTemp, erreur);
		return contentTemp;
	}


	/** If there is any error, all files are marked KO. Otherwise, all files are marked OK.
	 * */
	private void propagateErrorToAllFiles(ArrayList<ArrayList<String>> archiveContent, int erreur) {
		for (ArrayList<String> fileInfo : archiveContent) {
			if (erreur > 0) {
				fileInfo.set(GENERIC_BEAN_HEADERS.indexOf(GB_STATE), TraitementEtat.KO.toString());
				if (fileInfo.get(GENERIC_BEAN_HEADERS.indexOf(GB_REPORT)) == null){
					fileInfo.set(GENERIC_BEAN_HEADERS.indexOf(GB_REPORT), TraitementRapport.INITIALISATION_FICHIER_OK_ARCHIVE_KO.toString());
				}
			} else {
				fileInfo.set(GENERIC_BEAN_HEADERS.indexOf(GB_STATE), TraitementEtat.OK.toString());
			}
		}
	}


	/**
	 * Enregistrer les fichiers en entrée Déplacer les fichier reçus dans les repertoires OK ou pas OK selon le bordereau Supprimer les
	 * fichiers déjà existants de la table de pilotage Marquer les fichiers dans la table de pilotage
	 */
	private void registerAndDispatchFiles(Connection connexion, GenericBean archiveContent) {
		StaticLoggerDispatcher.info("registerAndDispatchFiles", LOGGER);
		// la bean (fileName,type, etat) contient pour chaque fichier, le type
		// du fichier et l'action à réaliser
		GenericBean g = findDuplicates(archiveContent);
		
		try {
		
			StringBuilder requete = new StringBuilder();
			requete.append(FormatSQL.dropTable(this.tablePilTemp));
			requete.append(creationTableResultat(this.tablePil, this.tablePilTemp));
			soumettreRequete(requete);

			if (!g.content.isEmpty()) {
				String dirIn = ApiReceptionService.directoryReceptionEtatEnCours(this.directoryRoot, this.envExecution);
				for (int i = 0; i < g.content.size(); i++) {
					String container = g.content.get(i).get(g.getHeaders().indexOf(GB_CONTAINER));
					String v_container = g.content.get(i).get(g.getHeaders().indexOf(GB_VCONTAINER));
					String fileName = g.content.get(i).get(g.getHeaders().indexOf(GB_FILENAME));
					String type = g.content.get(i).get(g.getHeaders().indexOf(GB_TYPE));
					String etat = g.content.get(i).get(g.getHeaders().indexOf(GB_STATE));
					String rapport = g.content.get(i).get(g.getHeaders().indexOf(GB_REPORT));
					String containerNewName = buildContainerName(container);
					if (type.equals(TraitementTypeFichier.DA.toString())) {
						insertPilotage(requete, this.tablePilTemp, container, containerNewName, v_container, fileName, etat, rapport);
					}
					if (type.equals(TraitementTypeFichier.A.toString())) {
						String dirOut = ApiReceptionService.directoryReceptionEtat(this.directoryRoot, this.envExecution, TraitementEtat.valueOf(etat));
						deplacerFichier(dirIn, dirOut, container, containerNewName);
					}
					if (type.equals(TraitementTypeFichier.AC.toString())) {
						String dirOut = ApiReceptionService.directoryReceptionEtat(this.directoryRoot, this.envExecution, TraitementEtat.valueOf(etat));
						deplacerFichier(dirIn, dirOut, container, containerNewName);
						insertPilotage(requete, this.tablePilTemp, container, containerNewName, v_container, fileName, etat, rapport);
					}
					// pour les fichier seul, on en fait une archive
					if (type.equals(TraitementTypeFichier.D.toString())) {
						// en termes de destination, les fichiers seuls vont tout le temps dans RECEPTION_OK, même s'ils sont KO pour la table
						// de pilotage
						String dirOut = ApiReceptionService.directoryReceptionEtatOK(this.directoryRoot, this.envExecution);
						File fileIn = new File(dirIn + File.separator + fileName);
						File fileOut = new File(dirOut + File.separator + containerNewName);

						if (fileOut.exists()) {
							fileOut.delete();
						}
						UtilitaireDao.generateTarGzFromFile(fileIn, fileOut, ManipString.substringAfterFirst(fileIn.getName(), "_"));
						fileIn.delete();
						insertPilotage(requete, this.tablePilTemp, container, containerNewName, v_container, fileName, etat, rapport);

					}
				}
				requete.append(";");
				soumettreRequete(requete);

				boolean fichierARejouer = UtilitaireDao.get("arc").hasResults(connexion, new ArcPreparedStatementBuilder("select 1 from " + this.tablePil + " where phase_traitement='RECEPTION' and to_delete in ('R','F') limit 1;"));

				if (fichierARejouer)
				{
					// marque les fichiers à effacer (ils vont etre rechargés)
					requete.append("CREATE TEMPORARY TABLE a_rejouer "+FormatSQL.WITH_NO_VACUUM+" as select distinct "+ColumnEnum.ID_SOURCE.getColumnName()+" from "+this.tablePil+" a where to_delete='R' and exists (select 1 from " + this.tablePilTemp + " b where a."+ColumnEnum.ID_SOURCE.getColumnName()+"=b."+ColumnEnum.ID_SOURCE.getColumnName()+"); ");

					// balayer toutes les tables; effacer les enregistrements 

					g = new GenericBean(UtilitaireDao.get("arc").executeRequest(connexion, ApiInitialisationService.requeteListAllTablesEnv(envExecution)));
					if (!g.mapContent().isEmpty()) {
						ArrayList<String> envTables = g.mapContent().get("table_name");
						for (String nomTable : envTables) {

							requete.append("DELETE FROM "+nomTable+" a where exists (select 1 from a_rejouer b where a."+ColumnEnum.ID_SOURCE.getColumnName()+"=b."+ColumnEnum.ID_SOURCE.getColumnName()+"); ");
							requete.append("vacuum "+nomTable+"; ");

						}
					}

					// effacer de la table pilotage des to_delete à R
					requete.append("DELETE FROM " + this.tablePil + " a using a_rejouer b where a."+ColumnEnum.ID_SOURCE.getColumnName()+"=b."+ColumnEnum.ID_SOURCE.getColumnName()+"; ");
				}


				// pb des archives sans nom de fichier
				requete.append("UPDATE " + this.tablePilTemp + " set "+ColumnEnum.ID_SOURCE.getColumnName()+"='' where "+ColumnEnum.ID_SOURCE.getColumnName()+" is null; ");
				requete.append("INSERT INTO " + this.tablePil + " select * from " + this.tablePilTemp + "; \n");
				requete.append("DISCARD TEMP; \n");
				soumettreRequete(requete);

			}
		} catch (Exception ex) {
		    LoggerHelper.errorGenTextAsComment(getClass(), "registerFiles()", LOGGER, ex);
		}
	}

	private String buildContainerName(String container) {
		String newContainerName = "";
		newContainerName = "";
		if (container.endsWith(".tar.gz")) {
			newContainerName = normalizeContainerName(container, ".tar.gz");
		} else if (container.endsWith(".tgz")) {
			newContainerName = normalizeContainerName(container, ".tgz");
		} else if (container.endsWith(".zip")) {
			newContainerName = normalizeContainerName(container, ".zip");
		} else if (container.endsWith(".gz")) {
			newContainerName = normalizeContainerName(container, ".gz");
		} else if (container.endsWith(".tar")) {
			newContainerName = normalizeContainerName(container, ".tar");
		}
		return newContainerName;
	}

	private String normalizeContainerName(String container, String extension) {
		return ManipString.substringBeforeLast(container, extension) +  extension;
	}

	private void soumettreRequete(StringBuilder requete) {
		try {
			UtilitaireDao.get("arc").executeImmediate(this.connexion.getCoordinatorConnection(), requete);
		} catch (ArcException ex) {
		    LoggerHelper.errorGenTextAsComment(getClass(), "soumettreRequete()", LOGGER, ex);
		}
		requete.setLength(0);
	}

	private void insertPilotage(StringBuilder requete, String tablePilotage, String originalContainer, String newContainer, String v_container,
			String fileName, String etat, String rapport) {
		Date d = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd:HH");
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		
		// si ko, etape vaut 2
		String etape=etat.equals(TraitementEtat.KO.toString())?"2":"1";
		
		if (requete.length()==0)
		{		
			requete.append("INSERT INTO " + tablePilotage + " ");
			requete.append("(o_container, container, v_container, "+ColumnEnum.ID_SOURCE.getColumnName()+", date_entree,phase_traitement,etat_traitement,date_traitement, rapport, nb_enr, etape) VALUES ");
		}
		else
		{
			requete.append("\n,");
		}
		requete.append(" (" + FormatSQL.cast(originalContainer) + "," + FormatSQL.cast(newContainer) + "," + FormatSQL.cast(v_container) + ", "
				+ FormatSQL.cast(fileName) + "," + FormatSQL.cast(dateFormat.format(d)) + "," + FormatSQL.cast(TraitementPhase.RECEPTION.toString())
				+ "," + FormatSQL.cast("{" + etat + "}") + "," + "to_timestamp("+FormatSQL.cast(formatter.format(d))+",'"+ApiService.bdDateFormat+"')" + "," + FormatSQL.cast(rapport) + ",1,"+etape+") ");
	}

	/**
	 * Deplacer un fichier d'un repertoire source vers répertoire cible (pas de slash en fin du nom de repertoire) Si le fichier existe
	 * déjà, il est écrasé
	 *
	 * @param dirIn
	 *            , répertoire en entrée, pas de slash à la fin
	 * @param dirOut
	 *            , répertoire en sortie, pas de slash à la fin
	 * @param FileName
	 *            , nom du fichier
	 */
	public static void deplacerFichier(String dirIn, String dirOut, String fileNameIn, String fileNameOut) {
		if (!dirIn.equals(dirOut)) {
			File fileIn = new File(dirIn + File.separator + fileNameIn); 
			File fileOut = new File(dirOut + File.separator + fileNameOut);
			if (fileOut.exists()) {
				fileOut.delete();
			}
			FileUtilsArc.renameTo(fileIn, fileOut);
		}
	}

	/**
	 * Find the duplicates files in the database
	 * @param fileList
	 * @return
	 */
	private GenericBean findDuplicates(GenericBean fileList) {
		ArrayList<String> headers = fileList.getHeaders();
		ArrayList<String> types = fileList.getTypes();
		ArrayList<ArrayList<String>> content = fileList.content;
		
		// Localiser les doublons
		// Note : l'insertion est redondante mais au niveau métier, c'est
		// beaucoup plus logique
		StaticLoggerDispatcher.info("Recherche de doublons de fichiers", LOGGER);

		StringBuilder requete = new StringBuilder();
		requete.append(FormatSQL.dropTable(this.tablePilTemp));
		requete.append(creationTableResultat(this.tablePil, this.tablePilTemp));
		String fileName;
		String container;
		String type;
		// insertion des fichiers dans la table tablePilTemp
		for (int i = 0; i < content.size(); i++) {
			container = content.get(i).get(headers.indexOf(GB_CONTAINER));
			fileName = content.get(i).get(headers.indexOf(GB_FILENAME));
			if (fileName != null) {
				requete.append("insert into " + this.tablePilTemp + " (container, "+ColumnEnum.ID_SOURCE.getColumnName()+") values (" + FormatSQL.cast(container) + ","
						+ FormatSQL.cast(fileName) + "); \n");
			}
		}
		soumettreRequete(requete);
		// detection des doublons de fichiers sur les id_source juste insérés
		// faut comparer les id_sources en retirant le #nnn représentant le numéro de l'archive (on utilise le regexp_replace pour retirer le #nnn)

		requete.append("select container, "+ColumnEnum.ID_SOURCE.getColumnName()+" FROM " + this.tablePilTemp + " where "+ColumnEnum.ID_SOURCE.getColumnName()+" in ( ");
		requete.append("select distinct "+ColumnEnum.ID_SOURCE.getColumnName()+" from ( ");
		requete.append("select "+ColumnEnum.ID_SOURCE.getColumnName()+", count(1) over (partition by "+ColumnEnum.ID_SOURCE.getColumnName()+") as n from " + this.tablePilTemp + " ");
		requete.append(") ww where n>1 ");
		requete.append(") ");
		// detection des doublons de fichiers dans la table de pilotage
		requete.append("UNION ");
		requete.append("SELECT container, "+ColumnEnum.ID_SOURCE.getColumnName()+" from " + this.tablePilTemp + " a ");
		requete.append("where exists (select 1 from " + this.tablePil + " b where a."+ColumnEnum.ID_SOURCE.getColumnName()+"=b."+ColumnEnum.ID_SOURCE.getColumnName()+") \n");
		requete.append("and a."+ColumnEnum.ID_SOURCE.getColumnName()+" not in (select distinct "+ColumnEnum.ID_SOURCE.getColumnName()+" from " + this.tablePil + " b where b.to_delete='R') ;\n");
		
		// récupérer les doublons pour mettre à jour le dispatcher
		try {
			ArrayList<String> listIdsourceDoublons = new GenericBean(UtilitaireDao.get("arc").executeRequest(this.connexion.getCoordinatorConnection(), new ArcPreparedStatementBuilder(requete))).mapContent().get(ColumnEnum.ID_SOURCE.getColumnName());
			
			// on va parcourir la liste des fichiers
			// si on retrouve l'id_source dans la liste, on le marque en erreur
			if (listIdsourceDoublons != null) {
				for (ArrayList<String> z : content) {
					// si le nom de fichier est renseigné et retrouvé dans la liste
					// on passe l'état à KO et on marque l'anomalie
					if (z.get(headers.indexOf(GB_FILENAME)) != null) {
						if (listIdsourceDoublons.contains(z.get(headers.indexOf(GB_FILENAME)))) {
							z.set(headers.indexOf(GB_STATE), TraitementEtat.KO.toString());
							z.set(headers.indexOf(GB_REPORT), TraitementRapport.INITIALISATION_DUPLICATE.toString());
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
		requete.append("SELECT container, container||'>'||"+ColumnEnum.ID_SOURCE.getColumnName()+" as "+ColumnEnum.ID_SOURCE.getColumnName()+" from " + this.tablePilTemp + " a ");
		requete.append("where exists (select 1 from " + this.tablePil + " b where to_delete='R' and a."+ColumnEnum.ID_SOURCE.getColumnName()+"=b."+ColumnEnum.ID_SOURCE.getColumnName()+") ;\n");

		ArrayList<ArrayList<String>> content2 = new ArrayList<>();
		try {
			HashMap<String, ArrayList<String>> m =  new GenericBean(UtilitaireDao.get("arc").executeRequest(this.connexion.getCoordinatorConnection(), new ArcPreparedStatementBuilder(requete))).mapContent();
			ArrayList<String> listContainerARejouer = m.get(GB_CONTAINER);
			ArrayList<String> listIdsourceARejouer = m.get(ColumnEnum.ID_SOURCE.getColumnName());

			if (listIdsourceARejouer==null)
			{
				content2=content;
			}
			else
			{
			for (ArrayList<String> z : content) {
				// si le fichier est dans la liste des doublons à ignorer, on le l'ajoute pas à la nouvelle liste
				if (z.get(headers.indexOf(GB_FILENAME)) != null) {
					if (listContainerARejouer.contains(z.get(headers.indexOf(GB_CONTAINER))))
					{
						// si on trouve le fichier à rejouer, on l'ajoute; on ignore les autres
						if (listIdsourceARejouer.contains(
								z.get(headers.indexOf(GB_CONTAINER))+">"+z.get(headers.indexOf(GB_FILENAME)
										))) {
							content2.add(z);
						}
					}
					else
					{
						content2.add(z);
					}
				}
				else
				{
					// bien ajouter les caracteriqtique de l'archive à la nouvelle liste
					content2.add(z);
				}
			}
			}
		
		} catch (ArcException ex) {
		    LoggerHelper.errorGenTextAsComment(getClass(), "dispatchFiles()", LOGGER, ex);
		}
		content=content2;



		
		// detection des doublons d'archive. Génération d'un numéro pour
		// l'archive en cas de doublon
		
		requete = new StringBuilder();
		// insertion des fichiers d'archive corrompue dans la table
		// tablePilTemp
		// on doit aussi leur donner un numéro
		for (int i = 0; i < content.size(); i++) {
			container = content.get(i).get(headers.indexOf(GB_CONTAINER));
			fileName = content.get(i).get(headers.indexOf(GB_FILENAME));
			type = content.get(i).get(headers.indexOf(GB_TYPE));
			if (type.equals(TraitementTypeFichier.AC.toString())) {
				requete.append("insert into " + this.tablePilTemp + " (container, "+ColumnEnum.ID_SOURCE.getColumnName()+") values (" + FormatSQL.cast(container) + ","
						+ FormatSQL.cast(fileName) + "); \n");
			}
		}
		soumettreRequete(requete);
				
		requete.append("select container ");
		requete.append(" , coalesce((select max(v_container::integer)+1 from  " + this.tablePil
				+ " b where a.container=b.o_container),1)::text as v_container ");
		requete.append("from (select distinct container from " + this.tablePilTemp + " where container is not null) a ");
		try {
			HashMap<String, ArrayList<String>> m = new GenericBean(UtilitaireDao.get("arc").executeRequest(this.connexion.getCoordinatorConnection(), new ArcPreparedStatementBuilder(requete))).mapContent();
			ArrayList<String> listContainerDoublons = m.get(GB_CONTAINER);
			ArrayList<String> listVersionContainerDoublons = m.get(GB_VCONTAINER);
			if (listContainerDoublons != null) {
				for (ArrayList<String> z : content) {
					container = z.get(headers.indexOf(GB_CONTAINER));
					if (container != null) {
						z.set(headers.indexOf(GB_VCONTAINER), listVersionContainerDoublons.get(listContainerDoublons.indexOf(container)));
					}
				}
			}
		} catch (ArcException ex) {
		    LoggerHelper.errorGenTextAsComment(getClass(), "dispatchFiles()", LOGGER, ex);
		}
		requete.setLength(0);
		requete.append(FormatSQL.dropTable(this.tablePilTemp));
		soumettreRequete(requete);
		return new GenericBean(headers, types, content);
	}
	
	
    /**
     * Methods to provide directories paths
     * @param rootDirectory
     * @param env
     * @return
     */



	public static String directoryReceptionRoot(String rootDirectory, String env)
	{
		return ApiService.directoryPhaseRoot(rootDirectory, env, TraitementPhase.RECEPTION);
	}
	
	public static String directoryReceptionEntrepot(String rootDirectory, String env, String entrepot)
	{
		return directoryPhaseEntrepot(rootDirectory, env, TraitementPhase.RECEPTION, entrepot);
	}
	
	public static String directoryReceptionEntrepotArchive(String rootDirectory, String env, String entrepot)
	{
		return directoryPhaseEntrepotArchive(rootDirectory, env, TraitementPhase.RECEPTION, entrepot);
	}

	public static String directoryReceptionEntrepotArchiveOld(String rootDirectory, String env, String entrepot)
	{
		return directoryPhaseEntrepotArchiveOld(rootDirectory, env, TraitementPhase.RECEPTION, entrepot);
	}
	
	public static String directoryReceptionEtat(String rootDirectory, String env, TraitementEtat e)
	{
		return directoryPhaseEtat(rootDirectory, env, TraitementPhase.RECEPTION, e);
	}
	
	public static String directoryReceptionEtatOK(String rootDirectory, String env)
	{
		return directoryPhaseEtatOK(rootDirectory, env, TraitementPhase.RECEPTION);
	}
	
	public static String directoryReceptionEtatKO(String rootDirectory, String env)
	{
		return directoryPhaseEtatKO(rootDirectory, env, TraitementPhase.RECEPTION);
	}
	
	public static String directoryReceptionEtatEnCours(String rootDirectory, String env)
	{
		return directoryPhaseEtatEnCours(rootDirectory, env, TraitementPhase.RECEPTION);
	}

}
