-- create export rules table
CREATE TABLE IF NOT EXISTS {{envExecution}}.export
(
file_name text,
table_to_export text,
nomenclature_export text,
filter_table text,
columns_array_header text,
columns_array_value text,
etat text,
nulls text,
headers text,
order_table text,
zip text,
PRIMARY KEY (file_name)
);

CREATE TABLE IF NOT EXISTS {{envExecution}}.export_option
( 
  nom_table_metier text NOT NULL,
  export_parquet_option text,
  export_coordinator_option text,
  CONSTRAINT ihm_export_regle_pkey PRIMARY KEY (nom_table_metier)
);

