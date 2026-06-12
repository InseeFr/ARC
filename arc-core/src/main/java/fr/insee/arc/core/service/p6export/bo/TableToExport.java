package fr.insee.arc.core.service.p6export.bo;

import fr.insee.arc.utils.database.ArcDatabase;
import fr.insee.arc.utils.database.TableToRetrieve;

public class TableToExport extends TableToRetrieve {

	
	public TableToExport(ArcDatabase nod, String tableName) {
		super(nod, tableName);
	}
	
	private boolean timestampDirectory;
	private String fileName;
	private String fileFormat;
	private String fileSchema;
	private boolean nullAsNull;
	private boolean headers;
	private String tableSqlFilter;
	private String tableSqlOrder;
	private String tableSqlJsonToColumns;

	public boolean isTimestampDirectory() {
		return timestampDirectory;
	}

	public TableToExport setTimestampDirectory(String timestampDirectory) {
		this.timestampDirectory = timestampDirectory!=null && !timestampDirectory.isEmpty();
		return this;
	}

	public String getFileName() {
		return fileName;
	}

	public TableToExport setFileName(String fileName) {
		this.fileName = fileName;
		return this;
	}

	public String getFileFormat() {
		return fileFormat;
	}

	public TableToExport setFileFormat(String fileFormat) {
		this.fileFormat = fileFormat;
		return this;
	}

	public String getFileSchema() {
		return fileSchema;
	}

	public TableToExport setFileSchema(String fileSchema) {
		this.fileSchema = fileSchema;
		return this;
	}

	public boolean isNullAsNull() {
		return nullAsNull;
	}

	public TableToExport setNullAsNull(String nullAsNull) {
		this.nullAsNull = nullAsNull!=null && !nullAsNull.isEmpty();
		return this;
	}

	public boolean isHeaders() {
		return headers;
	}

	public TableToExport setHeaders(String headers) {
		this.headers = headers!=null && !headers.isEmpty();
		return this;
	}

	public String getTableSqlFilter() {
		return tableSqlFilter;
	}

	public TableToExport setTableSqlFilter(String tableSqlFilter) {
		this.tableSqlFilter = tableSqlFilter;
		return this;
	}

	public String getTableSqlOrder() {
		return tableSqlOrder;
	}

	public TableToExport setTableSqlOrder(String tableSqlOrder) {
		this.tableSqlOrder = tableSqlOrder;
		return this;
	}

	public String getTableSqlJsonToColumns() {
		return tableSqlJsonToColumns;
	}

	public TableToExport setTableSqlJsonToColumns(String tableSqlJsonToColumns) {
		this.tableSqlJsonToColumns = tableSqlJsonToColumns;
		return this;
	}
	
}
