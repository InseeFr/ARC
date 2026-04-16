-- create export rules table
CREATE TABLE IF NOT EXISTS {{envExecution}}.export
(
timestamp_directory text,
file_name text,
table_to_export text,
nomenclature_export text,
filter_table text,
json_key_value text,
nulls text,
headers text,
order_table text,
zip text,
etat text,
PRIMARY KEY (file_name)
);

-- patch 13/04/2026 : columns_array_header and columns_array_value are replaced by json_key_value
ALTER TABLE {{envExecution}}.export DROP COLUMN IF EXISTS columns_array_header;
ALTER TABLE {{envExecution}}.export DROP COLUMN IF EXISTS columns_array_value;
ALTER TABLE {{envExecution}}.export ADD COLUMN IF NOT EXISTS json_key_value text;
ALTER TABLE {{envExecution}}.export ADD COLUMN IF NOT EXISTS etat text;
ALTER TABLE {{envExecution}}.export ADD COLUMN IF NOT EXISTS timestamp_directory text;


CREATE TABLE IF NOT EXISTS {{envExecution}}.export_option
( 
  nom_table_metier text NOT NULL,
  export_parquet_option text,
  export_coordinator_option text,
  CONSTRAINT ihm_export_regle_pkey PRIMARY KEY (nom_table_metier)
);

