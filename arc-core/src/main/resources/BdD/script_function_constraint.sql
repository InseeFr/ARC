-- trigger that generates a new version number when inserting a new ruleset with no version provided
drop trigger if exists own_insert_jdr on arc.ihm_jeuderegle;
create trigger own_insert_jdr before insert on arc.ihm_jeuderegle for each row when ((new.version is null)) execute function arc.insert_jeuderegle();

-- trigger that prevent ruleset to be modified if already in arc_prod sandbox
-- removed for now as production sandbox are no longer only called "arc_prod"
-- drop trigger if exists tg_update_jeuderegle on arc.ihm_jeuderegle;
-- create trigger tg_update_jeuderegle before update on arc.ihm_jeuderegle for each row execute function arc.update_jeuderegle();

CREATE OR REPLACE FUNCTION public.check_object_exists(table_identifier text) RETURNS boolean
as
$BODY$
begin
perform table_identifier::regclass;
return true;
exception when others 
then return false;
END; 
$BODY$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.check_sandbox(unsafe text) RETURNS boolean
as
$BODY$
begin
-- fix dump restore : return true if reference table doesn't exist
if (not public.check_object_exists('arc.ext_etat_jeuderegle')) then return true; end if;

if (unsafe is null or not (unsafe in (select replace(id,'.','_') from arc.ext_etat_jeuderegle where isenv)))
then
RAISE EXCEPTION '% format is not correct. Must be a declared sandbox.', unsafe; 
end if;
return true;
END; 
$BODY$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.check_sandboxes(unsafe text) RETURNS boolean
as
$BODY$
begin
-- fix dump restore : return true if reference table doesn't exist
if (not public.check_object_exists('arc.ext_etat_jeuderegle')) then return true; end if;

if unsafe is null or not ((select ARRAY(select jsonb_array_elements_text(unsafe::jsonb))) <@ (select array_agg(replace(id,'.','_')) from arc.ext_etat_jeuderegle where isenv))
then
RAISE EXCEPTION '% format is not correct. As an example, correct syntax is ["arc_prod","arc_bas1"]', unsafe; 
end if;
return true;
END; 
$BODY$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.check_word(unsafe text) RETURNS boolean
as
$BODY$
begin
if (regexp_replace(regexp_replace(lower(unsafe), '^[^a-z0-9]+','', 'g'),'[^\w$\-]+','','g') != lower(unsafe))
then
RAISE EXCEPTION '% format is not correct. Allowed chars (between parenthesis are the chars allowed at the start of expression) : (A-Z a-z 0-9) $ _ -', unsafe; 
end if;
return true;
END; 
$BODY$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.check_directory_token(unsafe text) RETURNS boolean
as
$BODY$
begin
if (regexp_replace(regexp_replace(lower(unsafe), '^[^a-z]+','', 'g'),'[^a-z0-9\-]+','','g') != lower(unsafe))
then
RAISE EXCEPTION '% format is not correct. Allowed chars (between parenthesis are the chars allowed at the start of expression) : (A-Z a-z) 0-9 -', unsafe; 
end if;
return true;
END; 
$BODY$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.check_identifier(unsafe text) RETURNS boolean
as
$BODY$
begin
if (regexp_replace(regexp_replace(lower(unsafe), '^[^a-z]+','', 'g'),'[^\w$]+','','g') != lower(unsafe))
then
RAISE EXCEPTION '% format is not correct. Allowed chars (between parenthesis are the chars allowed at the start of expression) : (A-Z a-z) 0-9 $ _', unsafe; 
end if;
return true;
END; 
$BODY$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION public.check_identifier_with_schema(unsafe text) RETURNS boolean
as
$BODY$
declare token text[];
begin
token:=regexp_split_to_array(unsafe,'\.');
	
if (array_length(token,1)>2) then 
	RAISE EXCEPTION '% format is not correct. Too many dot.', unsafe;
end if;

perform public.check_identifier(t) from unnest(token) as t;

return true;
END; 
$BODY$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION public.check_identifier_token(unsafe text) RETURNS boolean
as
$BODY$
begin
if (regexp_replace(lower(unsafe), '[^a-z0-9]+','', 'g') != lower(unsafe))
then
RAISE EXCEPTION '% format is not correct. Allowed chars (between parenthesis are the chars allowed at the start of expression) : (A-Z a-z 0-9)', unsafe; 
end if;
return true;
END; 
$BODY$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.check_integer(unsafe text) RETURNS boolean
as
$BODY$
begin
if (regexp_replace(lower(unsafe),'[^0-9]+','','g') != lower(unsafe))
then
RAISE EXCEPTION '% format is not correct. Allowed chars (between parenthesis are the chars allowed at the start of expression) : 0-9', unsafe; 
end if;
return true;
END; 
$BODY$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.check_type(unsafe text) RETURNS boolean
as
$BODY$
begin
if (regexp_replace(regexp_replace(lower(unsafe), '^[^a-z\_]+','', 'g'),'[^\w\[\] ]+','','g') != lower(unsafe))
then
RAISE EXCEPTION '% format is not correct. Allowed chars (between parenthesis are the chars allowed at the start of expression) : (A-Z a-z _) 0-9 [ ] space', unsafe; 
end if;
return true;
END; 
$BODY$
LANGUAGE plpgsql;

/* For a given column and a given list */
/* - Remove the contraints on all ihm tables containing the column */
/* - Create the contraints on all ihm tables containing the column */
/* The constraint checks if column value are in the provided list */
/* If the give constraint_function is null, it remove constraint */
CREATE OR REPLACE function public.check_list(col text, constraint_list text) returns boolean
as
$BODY$
declare query text;
begin
for query in (
select 
'do $$ begin alter table arc.'||table_name||' drop constraint '||table_name||'_'||column_name||'_c; exception when others then end;  $$; '
||
case
when constraint_list is null 
then '' 
else 'alter table arc.'||table_name||' add constraint '||table_name||'_'||column_name||'_c check ('||column_name||' IN ('||constraint_list||'));'
end
from information_schema.columns where column_name = col and table_schema='arc'
) loop
	execute query;
end loop;
return true;
END; 
$BODY$
LANGUAGE plpgsql;

/* For a given column and a given function */
/* - Remove the contraints on all tables from schema containing the column */
/* - Create the contraints on all tables from schema containing the column */
/* The constraint checks if the function applied to the column returns true */
/* If the give constraint_function is null, it remove constraint */
CREATE OR REPLACE function public.check_function(schemaname text, col text, constraint_function text) returns boolean
as
$BODY$
declare query text;
begin
for query in (
select 
'do $$ begin alter table '||table_schema||'.'||table_name||' drop constraint '||table_name||'_'||column_name||'_c; exception when others then end;  $$; '
||
case
when constraint_function is null
then '' 
else 'alter table '||table_schema||'.'||table_name||' add constraint '||table_name||'_'||column_name||'_c check ('||constraint_function||'('||column_name||'));'
end
from information_schema.columns where column_name = col and table_schema||'.'||table_name like schemaname||'%'
) loop
	execute query;
end loop;
return true;
END; 
$BODY$
LANGUAGE plpgsql;

CREATE OR REPLACE function public.check_function(col text, constraint_function text) returns boolean
as
$BODY$
begin
return public.check_function('arc.ihm', col, constraint_function);
END; 
$BODY$
LANGUAGE plpgsql;

-- white liste to check if sql injection in query
-- comments are forbidden in arc queries
CREATE OR REPLACE FUNCTION public.check_sql(query text) RETURNS boolean
as
$BODY$
declare query_analyze text:=query;
begin
	
query_analyze := replace(query_analyze, '/*', chr(1));
query_analyze := replace(query_analyze, '*/', chr(2));
query_analyze := regexp_replace(query_analyze, '\x01[^\x01\x02]*\x02','', 'g');
query_analyze := regexp_replace(query_analyze,'''[^'']*''','','g');

if (strpos(query_analyze,'''')>0 or  strpos(query_analyze,chr(1))>0 or strpos(query_analyze,chr(2))>0)
then 
RAISE EXCEPTION '% \n A quote or a comment bloc of this SQL expression is not correctly enclosed.', query; 
end if;

if (strpos(query_analyze,';')>0 or strpos(query_analyze,'--')>0)
then
RAISE EXCEPTION '% \n This SQL contains multiple statements or not allowed comment line. Forbidden.', query; 
end if;

return true;
END; 
$BODY$
LANGUAGE plpgsql;

-- patch drop deprecated function
DROP FUNCTION IF EXISTS public.check_no_injection(query text);

-- formatting function

-- strong white list function for identifier (tablename, column name)
-- starts by a-z or A-Z
-- _ and $ are allowed
-- other chars are deleted
-- set in lower case
CREATE OR REPLACE FUNCTION public.formatAsDatabaseIdentifier(unsafeIdentifier text) RETURNS text
as
$BODY$
BEGIN
return regexp_replace(regexp_replace(lower(unsafeIdentifier), '^[^a-z]+','', 'g'),'[^\w$]+','','g');
END; 
$BODY$
LANGUAGE plpgsql;

-- strong white list function for database type
-- can start by _
-- [ ] allowed
-- other chars are deleted
-- set in lower case
CREATE OR REPLACE FUNCTION public.formatAsDatabaseType(unsafeIdentifier text) RETURNS text
as
$BODY$
BEGIN
return regexp_replace(lower(unsafeIdentifier),'[^\w\[\] ]+','','g');
END; 
$BODY$
LANGUAGE plpgsql;

-- procedure to create a table as select safely
CREATE OR REPLACE PROCEDURE public.safe_select(query text)
AS
$BODY$
begin
perform check_sql(query);
EXECUTE FORMAT('DROP TABLE IF EXISTS safe_select; CREATE TEMPORARY TABLE safe_select AS %s',query);
END; 
$BODY$
LANGUAGE plpgsql;

-- safe exec functions
-- procedure to safely create a table given its column name and types
CREATE OR REPLACE PROCEDURE public.safe_create_table(tablename text, cols text[], types text[])
AS
$BODY$
DECLARE query text;
declare schemaname text;
begin
if (array_length(cols,1)!=array_length(types,1)) then
	RAISE EXCEPTION 'not the same number of column attributes and type atributes'; 
end if;

if (strpos(tablename,'.')>0) then
	schemaname:=split_part(tablename,'.',1);
	tablename:=split_part(tablename,'.',2);
end if;

if (length(schemaname)>0) then
	query:= 'DROP TABLE IF EXISTS '||public.formatAsDatabaseIdentifier(schemaname)||'.'||public.formatAsDatabaseIdentifier(tablename)||';';
	query:=query||'CREATE TABLE '||public.formatAsDatabaseIdentifier(schemaname)||'.'||public.formatAsDatabaseIdentifier(tablename)||' (';
else
	query:= 'DROP TABLE IF EXISTS '||public.formatAsDatabaseIdentifier(tablename)||';'; 
	query:=query||'CREATE TEMPORARY TABLE '||public.formatAsDatabaseIdentifier(tablename)||' (';
end if;

FOR i in 1..array_length(cols,1) loop
if (i>1) then 
query:=query||',';
end if;
query:=query||public.formatAsDatabaseIdentifier(cols[i])||' '||public.formatAsDatabaseType(types[i]);
end loop;
query:=query||');';

EXECUTE query;
END; 
$BODY$
LANGUAGE plpgsql;


/* ADD CONSTRAINTS */
-- norme
select public.check_function('id_norme', 'public.check_word');
select public.check_function('version', 'public.check_word');
select public.check_function('def_norme', 'public.check_sql');
select public.check_function('def_validite', 'public.check_sql');

-- famille & model
select public.check_function('id_famille', 'public.check_identifier_token');
select public.check_function('nom_table_metier', 'public.check_identifier');
select public.check_function('nom_variable_metier', 'public.check_identifier');
select public.check_function('type_variable_metier', 'public.check_type');

-- nmcl
select public.check_function('nom_table', 'public.check_identifier');

-- entrepot
select public.check_function('regle_priorite', 'public.check_sql');

-- chargement rules
select public.check_function('format', 'public.check_sql');

-- controle rules
select public.check_function('rubrique_pere', 'public.check_identifier');
select public.check_function('rubrique_fils', 'public.check_identifier');
select public.check_function('borne_sup', 'public.check_integer');
select public.check_function('borne_inf', 'public.check_integer');
select public.check_function('condition', 'public.check_sql');
select public.check_function('preaction', 'public.check_sql');

-- normage rules
select public.check_function('rubrique', 'public.check_identifier_with_schema');


do $$ begin alter table arc.ihm_normage_regle drop constraint ihm_normage_regle_rubrique_nmcl_c; exception when others then end;  $$; 
alter table arc.ihm_normage_regle add constraint ihm_normage_regle_rubrique_nmcl_c 
check (
case when id_classe= 'partition' 
	then public.check_integer(split_part(rubrique_nmcl,',',1)) and public.check_integer(split_part(rubrique_nmcl,',',2))
	else public.check_identifier_with_schema(rubrique_nmcl)
end
);

-- mapping rules
select public.check_function('variable_sortie', 'public.check_identifier'); 
select public.check_function('expr_regle_col', 'public.check_sql'); 

-- expression rules
select public.check_function('expr_nom', 'public.check_identifier'); 
select public.check_function('expr_valeur', 'public.check_sql');

-- webservice declaration rules
select public.check_function('norme', 'public.check_word');
select public.check_function('service_name', 'public.check_identifier_token');
select public.check_function('target_phase', 'public.check_integer');

select public.check_function('query_name', 'public.check_word');
select public.check_function('expression', 'public.check_sql');

-- ihm_nmcl
select public.check_function('nom_table', 'public.check_identifier');

-- ihm_entrepot
select public.check_function('id_entrepot', 'public.check_directory_token');
select public.check_function('id_loader', 'public.check_directory_token');

-- parameters rules
do $$ begin alter table arc.parameter drop constraint parameter_val_c; exception when others then end;  $$; 
alter table arc.parameter add constraint parameter_val_c 
check (
case 
when key= 'ApiInitialisationService.Nb_Jour_A_Conserver' then public.check_integer(val) 
when key= 'ApiInitialisationService.NB_FICHIER_PER_ARCHIVE' then public.check_integer(val)
when key= 'ApiService.HEURE_INITIALISATION_PRODUCTION' then case when public.check_integer(val) then val::int>=0 and val::int<24 else false end
when key= 'LanceurARC.keepInDatabase' then val::boolean in (true,false)
when key= 'LanceurARC.deltaStepAllowed' then public.check_integer(val)
when key= 'ApiReceptionService.batch.maxNumberOfFiles' then public.check_integer(val)
when key= 'ApiReceptionService.ihm.maxNumberOfFiles' then public.check_integer(val)
when key= 'LanceurARC.DATABASE_CHECKTODO_ROUTINE_INTERVAL' then public.check_integer(val)
when key= 'LanceurARC.DATABASE_WAIT_FOR_EXECUTORS_IN_MS' then public.check_integer(val)
when key= 'LanceurARC.tailleMaxReceptionEnMb' then public.check_integer(val)
when key= 'LanceurARC.DATABASE_MAINTENANCE_ROUTINE_INTERVAL' then public.check_integer(val)
when key= 'ArcAction.productionEnvironments' then public.check_sandboxes(val)
when key= 'LanceurARC.envFromDatabase' then val::boolean in (true,false)
when key= 'LanceurARC.envExecution' then public.check_sandbox(val)
when key= 'LanceurARC.poolingDelay' then public.check_integer(val)
when key= 'LanceurARC.maxFilesPerPhase' then public.check_integer(val)
when key= 'ApiService.MAX_PARALLEL_WORKERS' then public.check_integer(val)
when key= 'LanceurARC.INTERVAL_JOUR_INITIALISATION' then public.check_integer(val)
when key= 'ArcAction.batchMode' then public.check_sandboxes(val)
when key= 'LanceurARC.maxFilesToLoad' then public.check_integer(val)
when key= 'LanceurIHM.maxFilesPerPhase' then public.check_integer(val)
when key= 'LanceurIHM.tailleMaxReceptionEnMb' then public.check_integer(val)
else true
end
);

