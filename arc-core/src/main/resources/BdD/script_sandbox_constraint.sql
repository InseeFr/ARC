
commit;do $$ begin alter table {{envExecution}}.export add CONSTRAINT export_zip_fkey FOREIGN KEY (zip) REFERENCES arc.ext_export_format(id) ON DELETE CASCADE ON UPDATE cascade ; EXCEPTION WHEN OTHERS then end; $$;

select public.check_function('{{envExecution}}.export', 'file_name', 'public.check_identifier_with_schema');
select public.check_function('{{envExecution}}.export', 'table_to_export', 'public.check_identifier');
select public.check_function('{{envExecution}}.export', 'nomenclature_export', 'public.check_identifier');
select public.check_function('{{envExecution}}.export', 'columns_array_header', 'public.check_identifier');
select public.check_function('{{envExecution}}.export', 'columns_array_value', 'public.check_identifier');
select public.check_function('{{envExecution}}.export', 'filter_table', 'public.check_sql');
select public.check_function('{{envExecution}}.export', 'order_table', 'public.check_sql');

select public.check_function('{{envExecution}}.export_option', 'nom_table_metier', 'public.check_identifier');

commit;do $$ begin alter table {{envExecution}}.export_option add CONSTRAINT export_parquet_option_fkey FOREIGN KEY (export_parquet_option) REFERENCES arc.ext_etat(id) ON DELETE CASCADE ON UPDATE cascade ; EXCEPTION WHEN OTHERS then end; $$;
commit;do $$ begin alter table {{envExecution}}.export_option add CONSTRAINT export_coordinator_option_fkey FOREIGN KEY (export_coordinator_option) REFERENCES arc.ext_etat(id) ON DELETE CASCADE ON UPDATE cascade ; EXCEPTION WHEN OTHERS then end; $$;

