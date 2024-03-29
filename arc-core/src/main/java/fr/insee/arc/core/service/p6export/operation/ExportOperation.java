package fr.insee.arc.core.service.p6export.operation;

import java.util.List;
import java.util.Set;

import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.core.service.p6export.dao.ExportDao;
import fr.insee.arc.utils.database.TableToRetrieve;
import fr.insee.arc.utils.exception.ArcException;

public class ExportOperation {

	private ExportDao exportDao;
	
	public ExportOperation(Sandbox coordinatorSandbox) {
		this.exportDao = new ExportDao(coordinatorSandbox);
	}

	public void exportParquet() throws ArcException {
		// get a timestamp to identify export
		String dateExport = exportDao.dateExport();

		// select business table to be exported
		Set<String> mappingTablesName = exportDao.selectBusinessTableToExport();

		// assign business table to the right nod
		List<TableToRetrieve> tablesToExport = exportDao.fetchBusinessTableToNod(mappingTablesName);
		
		// export to parquet
		exportDao.exportTablesToParquet(dateExport, tablesToExport);

	}

}
