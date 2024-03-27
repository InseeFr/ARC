package fr.insee.arc.core.service.p6export;

import java.util.ArrayList;
import java.util.List;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.ApiService;
import fr.insee.arc.core.service.p6export.parquet.ParquetDao;
import fr.insee.arc.core.service.p6export.parquet.ParquetEncryptionKey;
import fr.insee.arc.core.service.p6export.provider.DirectoryPathExport;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.database.ArcDatabase;
import fr.insee.arc.utils.database.TableToRetrieve;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.utils.structure.GenericBean;

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

		// get timestamp
		ArcPreparedStatementBuilder query;
		query = new ArcPreparedStatementBuilder();
		query.build(SQL.SELECT, "to_char(localtimestamp,'YYYY_MM_DD_HH24_MI_SS_MS')");
		String dateExport = UtilitaireDao.get(0).getString(this.connexion.getCoordinatorConnection(), query);
		
		
		// select table to be exported
		query = new ArcPreparedStatementBuilder();
		query.build(SQL.SELECT, ColumnEnum.NOM_TABLE_METIER, SQL.FROM, ViewEnum.IHM_MOD_TABLE_METIER.getFullName());
		
		List<String> mappingTablesName = new GenericBean(UtilitaireDao.get(0).executeRequest(this.connexion.getCoordinatorConnection(), query))
		.getColumnValues(ColumnEnum.NOM_TABLE_METIER.getColumnName());
		
		List<TableToRetrieve> tablesToExport = new ArrayList<>();
		// business mapping table are found on executors nod
		mappingTablesName.stream().forEach(t -> tablesToExport.add(new TableToRetrieve(ArcDatabase.EXECUTOR , ViewEnum.getFullName(this.envExecution, t))));
		
		
		PropertiesHandler properties = PropertiesHandler.getInstance();
		
		exportToParquet(tablesToExport, DirectoryPathExport.directoryExport(properties.getBatchParametersDirectory(), this.envExecution, dateExport), null);
		
		
	}

	protected void exportToParquet(List<TableToRetrieve> tables, String outputDirectory,
			ParquetEncryptionKey encryptionKey) throws ArcException
	{
		new ParquetDao().exportToParquet(tables, outputDirectory, encryptionKey);
	}
	
	
}
