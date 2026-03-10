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

do $$ begin alter table {{envExecution}}.export add CONSTRAINT export_zip_fkey FOREIGN KEY (zip) REFERENCES arc.ext_export_format(id) ON DELETE CASCADE ON UPDATE cascade ; EXCEPTION WHEN OTHERS then end; $$;

select public.check_function('{{envExecution}}.export', 'file_name', 'public.check_identifier_with_schema');
select public.check_function('{{envExecution}}.export', 'table_to_export', 'public.check_identifier');
select public.check_function('{{envExecution}}.export', 'nomenclature_export', 'public.check_identifier');
select public.check_function('{{envExecution}}.export', 'columns_array_header', 'public.check_identifier');
select public.check_function('{{envExecution}}.export', 'columns_array_value', 'public.check_identifier');
select public.check_function('{{envExecution}}.export', 'filter_table', 'public.check_sql');
select public.check_function('{{envExecution}}.export', 'order_table', 'public.check_sql');
