package fr.insee.arc.core.service.p6export;

import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.ApiService;
import fr.insee.arc.core.service.p6export.operation.ExportOperation;
import fr.insee.arc.utils.exception.ArcException;

public class ApiExportService  extends ApiService {

    public ApiExportService() {
        super();
    }
	
    public ApiExportService(TraitementPhase aCurrentPhase, String anEnvironnementExecution,
            Integer aNbEnr, String paramBatch) {
        super(aCurrentPhase, anEnvironnementExecution, aNbEnr, paramBatch);
        exportOperation= new ExportOperation(this.coordinatorSandbox, paramBatch);
    }
	
    private ExportOperation exportOperation;
	
	@Override
	public void executer() throws ArcException {

		exportOperation.exportParquet();
		
	}

	
}
