package fr.insee.arc.core.service.p1reception;

import org.springframework.stereotype.Component;

import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.ApiService;
import fr.insee.arc.core.service.p1reception.registerarchive.ArchiveRegistrationOperation;
import fr.insee.arc.core.service.p1reception.registerarchive.bo.FilesDescriber;
import fr.insee.arc.core.service.p1reception.registerfiles.FileRegistration;
import fr.insee.arc.core.util.BDParameters;
import fr.insee.arc.utils.database.ArcDatabase;
import fr.insee.arc.utils.exception.ArcException;

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

	public ApiReceptionService() {
		super();
	}

	public ApiReceptionService(TraitementPhase aCurrentPhase, String aEnvExecution,
			Integer aNbEnr, String paramBatch) {
		super(aCurrentPhase, aEnvExecution, aNbEnr, paramBatch);
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
		ArchiveRegistrationOperation archiveRegistration = new ArchiveRegistrationOperation(coordinatorSandbox, maxNumberOfFiles, maxNumberOfFiles);
		
		FilesDescriber archiveContent = archiveRegistration.moveAndCheckClientFiles();
		this.setReportNumberOfObject(archiveRegistration.getFileNb());
		
		if (! archiveContent.getFilesAttribute().isEmpty()) {
			new FileRegistration(this.coordinatorSandbox, this.tablePilTemp)
			.registerAndDispatchFiles(archiveContent);
		}
	}

}
