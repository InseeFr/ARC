package fr.insee.arc.core.service.p6export;

import java.util.List;

import org.springframework.stereotype.Component;

import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.ApiService;
import fr.insee.arc.core.service.p6export.parquet.ParquetDao;
import fr.insee.arc.core.service.p6export.parquet.ParquetEncryptionKey;
import fr.insee.arc.utils.database.TableToRetrieve;
import fr.insee.arc.utils.exception.ArcException;

@Component
public class ApiExportService  extends ApiService {

    public ApiExportService() {
        super();
    }
	
    public ApiExportService(TraitementPhase aCurrentPhase, String anEnvironnementExecution,
            Integer aNbEnr, String paramBatch) {
        super(aCurrentPhase, anEnvironnementExecution, aNbEnr, paramBatch);
    }
	
	
	@Override
	public void executer() throws ArcException {
		// empty for now
	}

	protected void exportToParquet(List<TableToRetrieve> tables, String outputDirectory,
			ParquetEncryptionKey encryptionKey) throws ArcException
	{
		new ParquetDao().exportToParquet(tables, outputDirectory, encryptionKey);
	}
	
	
}
