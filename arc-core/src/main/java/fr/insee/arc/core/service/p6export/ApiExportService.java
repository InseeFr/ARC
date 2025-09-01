package fr.insee.arc.core.service.p6export;

import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.ApiService;
import fr.insee.arc.core.service.p6export.operation.ExportOperation;
import fr.insee.arc.utils.exception.ArcException;

public class ApiExportService  extends ApiService {

    public ApiExportService() {
        super();
    }
	
    public ApiExportService(String anEnvironnementExecution,
            Integer aNbEnr, String paramBatch) {
        super(anEnvironnementExecution, aNbEnr, paramBatch, TraitementPhase.EXPORT);
        exportOperation= new ExportOperation(this.coordinatorSandbox, paramBatch);
    }
	
    private ExportOperation exportOperation;
	
	@Override
	public void executer() throws ArcException {

		exportOperation.initializeExport();
		
		exportOperation.exportParquet();
		
		exportOperation.exportToMasterNod();
		
		exportOperation.markExport();
		
	}

	
}
