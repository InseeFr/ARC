-- sandbox creation
CREATE SCHEMA IF NOT EXISTS {{envExecution}} AUTHORIZATION current_user; 

-- grant full right to application user and read write for arc_restricted
REVOKE ALL ON SCHEMA {{envExecution}} FROM public; 
GRANT ALL ON SCHEMA {{envExecution}} TO current_user;

-- user with restricted right can only play with temporary tables
commit;do $$ begin
if ('{{userRestricted}}'!='') then 
	execute 'GRANT USAGE ON SCHEMA {{envExecution}} TO {{userRestricted}}; GRANT SELECT on ALL TABLES IN schema {{envExecution}} TO {{userRestricted}}; ALTER DEFAULT privileges IN SCHEMA {{envExecution}} GRANT SELECT ON TABLES to {{userRestricted}};';
end if;
exception when others then end;
$$;

-- create pilotage table
CREATE TABLE IF NOT EXISTS {{envExecution}}.pilotage_batch (last_init text, operation text);
insert into {{envExecution}}.pilotage_batch select '1900-01-01:00','O' where not exists (select from {{envExecution}}.pilotage_batch);

-- pilotage tables and view
DROP TABLE IF EXISTS {{envExecution}}.pilotage_fichier_t;

CREATE TABLE IF NOT EXISTS {{envExecution}}.pilotage_fichier (id_source text COLLATE pg_catalog."C",  id_norme text COLLATE pg_catalog."C",  validite text COLLATE pg_catalog."C",  periodicite text COLLATE pg_catalog."C",  phase_traitement text COLLATE pg_catalog."C",  etat_traitement text[] COLLATE pg_catalog."C",  date_traitement timestamp without time zone,  rapport text COLLATE pg_catalog."C",  taux_ko numeric,  nb_enr integer,  nb_essais integer,  etape integer,  validite_inf date,  validite_sup date,  version text COLLATE pg_catalog."C",  date_entree text,  container text COLLATE pg_catalog."C",  v_container text COLLATE pg_catalog."C",  o_container text COLLATE pg_catalog."C",  to_delete text COLLATE pg_catalog."C",  client text[],  date_client timestamp without time zone[],  jointure text, generation_composite text COLLATE pg_catalog."C") WITH (autovacuum_enabled = false, toast.autovacuum_enabled = false);
ALTER TABLE {{envExecution}}.pilotage_fichier alter column date_entree type text COLLATE pg_catalog."C";

CREATE TABLE IF NOT EXISTS {{envExecution}}.pilotage_archive (  entrepot text COLLATE pg_catalog."C",  nom_archive text COLLATE pg_catalog."C") WITH (autovacuum_enabled = false, toast.autovacuum_enabled = false);

DROP TRIGGER IF EXISTS tg_pilotage_fichier_calcul ON {{envExecution}}.pilotage_fichier CASCADE;
DROP TRIGGER IF EXISTS tg_pilotage_fichier_fin ON {{envExecution}}.pilotage_fichier CASCADE;

-- index
CREATE INDEX IF NOT EXISTS idx1_pilotage_fichier on {{envExecution}}.pilotage_fichier (id_source);
CREATE INDEX IF NOT EXISTS idx2_pilotage_fichier on {{envExecution}}.pilotage_fichier (phase_traitement, etape);
DROP INDEX IF EXISTS {{envExecution}}.idx3_pilotage_fichier;
CREATE INDEX IF NOT EXISTS idx4_pilotage_fichier on {{envExecution}}.pilotage_fichier (rapport) where rapport is not null;
CREATE INDEX IF NOT EXISTS idx5_pilotage_fichier on {{envExecution}}.pilotage_fichier (o_container,v_container);
CREATE INDEX IF NOT EXISTS idx6_pilotage_fichier on {{envExecution}}.pilotage_fichier (to_delete);
CREATE INDEX IF NOT EXISTS idx7_pilotage_fichier on {{envExecution}}.pilotage_fichier (date_entree, phase_traitement, etat_traitement);
DROP INDEX IF EXISTS {{envExecution}}.idx8_pilotage_fichier;

