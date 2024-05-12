package fr.insee.arc.core.service.p6export.parquet;

public class ParquetEncryptionKey {

	public enum EncryptionType {
		KEY256("key256");
		
		private EncryptionType(String alias)
		{
			this.alias=alias;
		}
		
		private String alias;

		public String getAlias() {
			return alias;
		}

	}
	
	private EncryptionType type;
	private String value;
	
	public ParquetEncryptionKey(EncryptionType type, String value) {
		super();
		this.type = type;
		this.value = value;
	}

	public EncryptionType getType() {
		return type;
	}


	public String getValue() {
		return value;
	}
	
	
}
