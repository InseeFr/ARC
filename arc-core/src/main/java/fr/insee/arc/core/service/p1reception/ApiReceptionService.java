package fr.insee.arc.core.service.p1reception;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;
import org.springframework.stereotype.Component;

import fr.insee.arc.core.dataobjects.ArcDatabase;
import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.model.TraitementRapport;
import fr.insee.arc.core.model.TraitementTypeFichier;
import fr.insee.arc.core.service.global.ApiService;
import fr.insee.arc.core.service.global.dao.FileSystemManagement;
import fr.insee.arc.core.service.global.dao.PhaseOperations;
import fr.insee.arc.core.service.global.dao.TableNaming;
import fr.insee.arc.core.service.global.dao.TableOperations;
import fr.insee.arc.core.service.global.scalability.ServiceScalability;
import fr.insee.arc.core.service.p0initialisation.ApiInitialisationService;
import fr.insee.arc.core.service.p0initialisation.pilotage.SynchronizeDataByPilotage;
import fr.insee.arc.core.service.p0initialisation.pilotage.bo.ListIdSourceInPilotage;
import fr.insee.arc.core.service.p1reception.registerarchive.ArchiveRegistration;
import fr.insee.arc.core.service.p1reception.registerarchive.bo.FilesDescriber;
import fr.insee.arc.core.service.p1reception.registerfiles.FileRegistration;
import fr.insee.arc.core.service.p1reception.registerfiles.dao.FileRegistrationDao;
import fr.insee.arc.core.util.BDParameters;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.consumer.ThrowingConsumer;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.dataobjects.TypeEnum;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.files.CompressedUtils;
import fr.insee.arc.utils.files.FileUtilsArc;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.ManipString;

/**
 * ApiReceptionService
 *
 * 1- Déplacer les enveloppes de l'entrepot vers EN_COURS. Horodater et archiver
 * les enveloppes recues</br>
 * 2- Enregistrer n enveloppes dans la base pour traitement 2-1 Vérification de
 * l'intégrité de l'enveloppe 2_2 Identification des doublons de fichier 2-2
 * Enregistrement des fichiers et de leur enveloppe dans la table de pilotage
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
	private static final ArrayList<String> GENERIC_BEAN_HEADERS = new ArrayList<>(
			Arrays.asList(GB_CONTAINER, GB_FILENAME, GB_TYPE, GB_STATE, GB_REPORT, GB_VCONTAINER));
	private static final ArrayList<String> GENERIC_BEAN_TYPES = new ArrayList<>(
			Arrays.asList(TypeEnum.TEXT.getTypeName(), TypeEnum.TEXT.getTypeName(), TypeEnum.TEXT.getTypeName(), TypeEnum.TEXT.getTypeName(), TypeEnum.TEXT.getTypeName(), TypeEnum.TEXT.getTypeName()));

	public ApiReceptionService() {
		super();
	}

	public static final int READ_BUFFER_SIZE = 131072;

	private static final Logger LOGGER = LogManager.getLogger(ApiReceptionService.class);

	public ApiReceptionService(String aCurrentPhase, String aEnvExecution,
			String aDirectoryRoot, Integer aNbEnr, String paramBatch) {
		super(aCurrentPhase, aEnvExecution, aDirectoryRoot, aNbEnr, paramBatch);
	}
	
	@Override
	public void executer() throws ArcException {
		// Déplacement et archivage des fichiers

		int maxNumberOfFiles;
		
        BDParameters bdParameters=new BDParameters(ArcDatabase.COORDINATOR);

		if (paramBatch != null) {
			maxNumberOfFiles = bdParameters.getInt(null, "ApiReceptionService.batch.maxNumberOfFiles", 25000);
		} else {
			maxNumberOfFiles = bdParameters.getInt(null, "ApiReceptionService.ihm.maxNumberOfFiles", 5000);
		}
		
		// Enregistrement des fichiers
		ArchiveRegistration archiveRegistration = new ArchiveRegistration(coordinatorSandbox, maxNumberOfFiles, maxNumberOfFiles);
		
		FilesDescriber archiveContent = archiveRegistration.moveAndCheckClientFiles();
		this.setReportNumberOfObject(archiveRegistration.getFileNb());
		
		if (! archiveContent.getFilesAttribute().isEmpty()) {
			new FileRegistration(this.coordinatorSandbox, this.tablePilTemp)
			.registerAndDispatchFiles(archiveContent);
		}
	}

}
