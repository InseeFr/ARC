-- schéma global arc
CREATE SCHEMA IF NOT EXISTS arc; 
CREATE SCHEMA IF NOT EXISTS public;

-- table de parametrage de l'application
CREATE TABLE IF NOT EXISTS arc.parameter 
( 
key text, 
val text, 
CONSTRAINT parameter_pkey PRIMARY KEY (key) 
); 

ALTER TABLE arc.parameter add column IF NOT exists description text;

-- mvcc rule for no update if same value, no insert if key
CREATE or REPLACE RULE parameter_description_update AS 
ON UPDATE TO arc.parameter
where NEW.val = OLD.val and NEW.description = OLD.description
do instead nothing;

CREATE OR REPLACE RULE parameter_description_insert AS 
ON INSERT TO arc.parameter where 
exists (select from arc.parameter a where a.key=new.key)
do instead nothing;


-- optimization parameters at database level
do $$
declare
c record;
BEGIN
for c in (select current_database() as n) loop
execute 'alter database '||c.n||' set enable_bitmapscan=off;';
execute 'alter database '||c.n||' set synchronous_commit=off;';
execute 'alter database '||c.n||' set max_parallel_workers_per_gather=0;';
execute 'alter database '||c.n||' set max_parallel_maintenance_workers=0;';
execute 'alter database '||c.n||' set jit=off;';
end loop;
end;
$$;

-- batch initialization parameters
INSERT INTO arc.parameter VALUES ('ApiInitialisationService.Nb_Jour_A_Conserver','365');
UPDATE arc.parameter set description='parameter.batch.initialization.numberOfDayToKeepAfterRetrievedByAllCLients' where key='ApiInitialisationService.Nb_Jour_A_Conserver';

INSERT INTO arc.parameter VALUES ('ApiInitialisationService.NB_FICHIER_PER_ARCHIVE','10000');
UPDATE arc.parameter set description='parameter.batch.initialization.numberOfFilesToProceedAtSameTimeForDeletion' where key='ApiInitialisationService.NB_FICHIER_PER_ARCHIVE';

INSERT INTO arc.parameter VALUES ('ApiService.HEURE_INITIALISATION_PRODUCTION','22');
UPDATE arc.parameter set description='parameter.batch.initialization.StartsJustAfterThisHour' where key='ApiService.HEURE_INITIALISATION_PRODUCTION';

INSERT INTO arc.parameter VALUES ('LanceurARC.INTERVAL_JOUR_INITIALISATION','7');
UPDATE arc.parameter set description='parameter.batch.initialization.minimumDayIntervalBetweenInitializations' where key='LanceurARC.INTERVAL_JOUR_INITIALISATION';

-- batch execution parameters
INSERT INTO arc.parameter VALUES ('LanceurARC.keepInDatabase','false');
UPDATE arc.parameter set description='parameter.batch.execution.keepPreviousModuleData' where key='LanceurARC.keepInDatabase';

INSERT INTO arc.parameter VALUES ('LanceurARC.maxFilesPerPhase','1000000');
UPDATE arc.parameter set description='parameter.batch.execution.maxNumberOfFilesToProceedPerModule' where key='LanceurARC.maxFilesPerPhase';

INSERT INTO arc.parameter VALUES ('LanceurARC.deltaStepAllowed','10000');
UPDATE arc.parameter set description='parameter.batch.execution.howManyStepsFurtherModuleAreExecuted' where key='LanceurARC.deltaStepAllowed';

INSERT INTO arc.parameter VALUES ('LanceurARC.poolingDelay','1000');
UPDATE arc.parameter set description='parameter.batch.execution.sleepingDelayDuringPipelineLoopInMs' where key='LanceurARC.poolingDelay';

INSERT INTO arc.parameter VALUES ('LanceurARC.envFromDatabase','false');
UPDATE arc.parameter set description='parameter.batch.execution.booleanUseEnvironmentDeclaredInDatabase' where key='LanceurARC.envFromDatabase';

INSERT INTO arc.parameter VALUES ('LanceurARC.envExecution','arc_prod');
UPDATE arc.parameter set description='parameter.batch.execution.environmentForExecution' where key='LanceurARC.envExecution';

INSERT INTO arc.parameter VALUES ('ApiReceptionService.batch.maxNumberOfFiles','25000');
UPDATE arc.parameter set description='parameter.batch.execution.maxNumberOfFilesRegisteredInReceptionModule' where key='ApiReceptionService.batch.maxNumberOfFiles';

INSERT INTO arc.parameter VALUES ('LanceurARC.tailleMaxReceptionEnMb','100');
UPDATE arc.parameter set description='parameter.batch.execution.maxCompressedArchiveSizeRegisteredInReceptionModule' where key='LanceurARC.tailleMaxReceptionEnMb';

INSERT INTO arc.parameter VALUES ('LanceurARC.maxFilesToLoad','101');
UPDATE arc.parameter set description='parameter.batch.execution.maxNumberOfFilesProceedInLoadModule' where key='LanceurARC.maxFilesToLoad';

INSERT INTO arc.parameter VALUES ('LanceurARC.maxFilesPerPhase','1000000');
UPDATE arc.parameter set description='parameter.batch.execution.defaultMaxNumberOfFilesProcessedByModules' where key='LanceurARC.maxFilesPerPhase';

-- ihm sandbox parameters
INSERT INTO arc.parameter VALUES ('ApiInitialisationService.nbSandboxes','8');
UPDATE arc.parameter set description='parameter.ihm.sandbox.numberOfSandboxes' where key='ApiInitialisationService.nbSandboxes';

INSERT INTO arc.parameter VALUES ('ApiReceptionService.ihm.maxNumberOfFiles','5000');
UPDATE arc.parameter set description='parameter.ihm.sandbox.maxNumberOfFilesRegisteredAtTheSameTime' where key='ApiReceptionService.ihm.maxNumberOfFiles';

INSERT INTO arc.parameter VALUES ('ArcAction.productionEnvironments','["arc_prod"]');
UPDATE arc.parameter set description='parameter.ihm.sandbox.sandboxListWithProductionGUI' where key='ArcAction.productionEnvironments';


-- parallelism parameters
INSERT INTO arc.parameter VALUES ('ApiChargementService.MAX_PARALLEL_WORKERS','2');
UPDATE arc.parameter set description='parameter.parallel.numberOfThread.p1.load' where key='ApiChargementService.MAX_PARALLEL_WORKERS';

INSERT INTO arc.parameter VALUES ('ApiNormageService.MAX_PARALLEL_WORKERS','5');
UPDATE arc.parameter set description='parameter.parallel.numberOfThread.p2.xmlStructurize' where key='ApiNormageService.MAX_PARALLEL_WORKERS';

INSERT INTO arc.parameter VALUES ('ApiControleService.MAX_PARALLEL_WORKERS','3');
UPDATE arc.parameter set description='parameter.parallel.numberOfThread.p3.control' where key='ApiControleService.MAX_PARALLEL_WORKERS';

-- effacer le parametrage de la phase de filtrage
delete from arc.parameter where key='ApiFiltrageService.MAX_PARALLEL_WORKERS';

INSERT INTO arc.parameter VALUES ('MappingService.MAX_PARALLEL_WORKERS','4');
UPDATE arc.parameter set description='parameter.parallel.numberOfThread.p4.mapmodel' where key='MappingService.MAX_PARALLEL_WORKERS';

-- patch 23/06/2023
-- remove deprecated parameters
DELETE FROM arc.parameter WHERE key='LanceurARC.MAX_PARALLEL_RUNNER_PER_PHASE';
DELETE FROM arc.parameter WHERE key='LanceurARC.PARALLEL_LOCK_CHECK_INTERVAL';
DELETE FROM arc.parameter WHERE key='LanceurARC.env';

-- table de pilotage du batch de production
CREATE TABLE IF NOT EXISTS arc.pilotage_batch (last_init text, operation text);
insert into arc.pilotage_batch select '1900-01-01:00','O' where not exists (select from arc.pilotage_batch);
  
-- table de modalités IHM       
CREATE TABLE IF NOT EXISTS arc.ext_etat 
( 
id text, 
val text, 
CONSTRAINT ext_etat_pkey PRIMARY KEY (id) 
);

INSERT INTO arc.ext_etat values ('0','INACTIF'),('1','ACTIF') ON CONFLICT DO NOTHING;

-- table des jeux de regles
CREATE TABLE IF NOT EXISTS arc.ext_etat_jeuderegle 
( 
id text NOT NULL, 
val text, 
isenv boolean, 
mise_a_jour_immediate boolean, 
CONSTRAINT ext_etat_jeuderegle_pkey PRIMARY KEY (id) 
);
ALTER TABLE arc.ext_etat_jeuderegle add column IF NOT exists env_description text;


-- enregistrement des bacs à sable
do $$
BEGIN
for i in 1..{{nbSandboxes}} loop
INSERT INTO arc.ext_etat_jeuderegle values ('arc.bas'||i,'BAC A SABLE '||i,'TRUE','TRUE') ON CONFLICT DO NOTHING;
end loop;
end;
$$;

INSERT INTO arc.ext_etat_jeuderegle values ('arc.prod','PRODUCTION','TRUE','FALSE') ON CONFLICT DO NOTHING;
INSERT INTO arc.ext_etat_jeuderegle values ('inactif','INACTIF','FALSE','FALSE') ON CONFLICT DO NOTHING;


-- patch to delete historical double records
do $$ 
declare doublon int;
begin
SELECT count(*) into doublon FROM (SELECT id from arc.ext_mod_periodicite group by id having count(*)>1) u;

if (doublon>0) then 
DROP TABLE arc.ext_mod_periodicite;
END IF;

exception when others then end;
$$;


CREATE TABLE IF NOT EXISTS arc.ext_mod_periodicite 
( 
id text, 
val text, 
CONSTRAINT ext_mod_periodicite_pkey PRIMARY KEY (id) 
); 

INSERT INTO arc.ext_mod_periodicite values ('M','MENSUEL'),('A','ANNUEL') ON CONFLICT DO NOTHING;


-- tables des familles de norme
CREATE TABLE IF NOT EXISTS arc.ext_mod_type_autorise 
( 
 nom_type name NOT NULL, 
 description_type text NOT NULL, 
 CONSTRAINT pk_ext_mod_type_autorise PRIMARY KEY (nom_type) 
); 

INSERT INTO arc.ext_mod_type_autorise values ('bigint','Entier') ON CONFLICT DO NOTHING;
INSERT INTO arc.ext_mod_type_autorise values ('bigint[]','Tableau d''entier long') ON CONFLICT DO NOTHING;
INSERT INTO arc.ext_mod_type_autorise values ('boolean','Vrai (t ou true) ou faux (f ou false)') ON CONFLICT DO NOTHING;
INSERT INTO arc.ext_mod_type_autorise values ('date','Date'),('date[]','Tableau de date') ON CONFLICT DO NOTHING;
INSERT INTO arc.ext_mod_type_autorise values ('float','Nombre décimal virgule flottante') ON CONFLICT DO NOTHING;
INSERT INTO arc.ext_mod_type_autorise values ('float[]','Tableau de nombre décimaux') ON CONFLICT DO NOTHING;
INSERT INTO arc.ext_mod_type_autorise values ('interval','Durée (différence de deux dates)') ON CONFLICT DO NOTHING;
INSERT INTO arc.ext_mod_type_autorise values ('text','Texte sans taille limite') ON CONFLICT DO NOTHING;
INSERT INTO arc.ext_mod_type_autorise values ('text[]','Tableau de texte sans limite') ON CONFLICT DO NOTHING;
INSERT INTO arc.ext_mod_type_autorise values ('timestamp without time zone','Date et heure') ON CONFLICT DO NOTHING;

-- a "family" identifies the one output data model
CREATE TABLE IF NOT EXISTS arc.ihm_famille 
( 
  id_famille text NOT NULL, 
  CONSTRAINT ihm_famille_pkey PRIMARY KEY (id_famille) 
); 
        
-- client names for the data retrieval webservice 
CREATE TABLE IF NOT EXISTS arc.ihm_client 
(id_famille text NOT NULL, 
id_application text NOT NULL, 
CONSTRAINT pk_ihm_client PRIMARY KEY (id_famille, id_application), 
CONSTRAINT fk_client_famille FOREIGN KEY (id_famille) 
REFERENCES arc.ihm_famille (id_famille) MATCH SIMPLE 
ON UPDATE NO ACTION ON DELETE NO ACTION 
);        

-- define the white list for hosts accessing the data retrieval webservice
CREATE TABLE IF NOT EXISTS arc.ihm_webservice_whitelist
(
host_allowed text, id_famille text, id_application text, is_secured text
, PRIMARY KEY (host_allowed)
, FOREIGN KEY (id_famille,id_application) REFERENCES arc.ihm_client(id_famille,id_application)
ON DELETE CASCADE ON UPDATE CASCADE
);

-- data retrieval webservice logs 
CREATE TABLE IF NOT EXISTS arc.ihm_webservice_log
(
id_webservice_logging bigserial, id_famille text, id_application text, host_allowed text, event_timestamp timestamp
, PRIMARY KEY (id_webservice_logging)
, FOREIGN KEY (id_famille,id_application) REFERENCES arc.ihm_client(id_famille,id_application)
ON DELETE CASCADE ON UPDATE CASCADE
);


-- tables de gestion des règles        
CREATE TABLE IF NOT EXISTS arc.ihm_norme 
( 
  id_norme text NOT NULL, 
  periodicite text  NOT NULL, 
  def_norme text  NOT NULL, 
  def_validite text  NOT NULL, 
  id serial NOT NULL, 
  etat text , 
  id_famille text , 
  CONSTRAINT ihm_norme_pkey PRIMARY KEY (id_norme, periodicite), 
  CONSTRAINT ihm_norme_id_famille_fkey FOREIGN KEY (id_famille) 
      REFERENCES arc.ihm_famille (id_famille) MATCH SIMPLE 
      ON UPDATE CASCADE ON DELETE CASCADE 
); 
        
CREATE TABLE IF NOT EXISTS arc.ihm_calendrier 
( 
  id_norme text NOT NULL, 
  periodicite text NOT NULL, 
  validite_inf date NOT NULL, 
  validite_sup date NOT NULL, 
  id serial NOT NULL, 
  etat text, 
  CONSTRAINT ihm_calendrier_pkey PRIMARY KEY (id_norme, periodicite, validite_inf, validite_sup), 
  CONSTRAINT ihm_calendrier_norme_fkey FOREIGN KEY (id_norme, periodicite) 
      REFERENCES arc.ihm_norme (id_norme, periodicite) MATCH SIMPLE 
      ON UPDATE CASCADE ON DELETE CASCADE 
); 
        
CREATE TABLE IF NOT EXISTS arc.ihm_jeuderegle 
( 
  id_norme text NOT NULL, 
  periodicite text NOT NULL, 
  validite_inf date NOT NULL, 
  validite_sup date NOT NULL, 
  version text NOT NULL, 
  etat text, 
  date_production date, 
  date_inactif date, 
  CONSTRAINT ihm_jeuderegle_pkey PRIMARY KEY (id_norme, periodicite, validite_inf, validite_sup, version), 
  CONSTRAINT ihm_jeuderegle_calendrier_fkey FOREIGN KEY (id_norme, periodicite, validite_inf, validite_sup) 
      REFERENCES arc.ihm_calendrier (id_norme, periodicite, validite_inf, validite_sup) MATCH SIMPLE 
      ON UPDATE CASCADE ON DELETE CASCADE 
);             
        
CREATE TABLE IF NOT EXISTS arc.ihm_mod_table_metier 
( 
id_famille text NOT NULL, 
nom_table_metier text NOT NULL, 
description_table_metier text, 
CONSTRAINT pk_ihm_mod_table_metier PRIMARY KEY (id_famille, nom_table_metier)
);

ALTER TABLE arc.ihm_mod_table_metier DROP CONSTRAINT IF EXISTS fk_ihm_table_metier_famille;

do $$ begin ALTER TABLE arc.ihm_mod_table_metier ADD CONSTRAINT fk2_ihm_table_metier_famille FOREIGN KEY (id_famille) REFERENCES arc.ihm_famille (id_famille) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE; EXCEPTION WHEN OTHERS then end; $$;

CREATE TABLE IF NOT EXISTS arc.ihm_mod_variable_metier 
( 
id_famille text NOT NULL, 
nom_table_metier text NOT NULL, 
nom_variable_metier text NOT NULL, 
type_variable_metier name NOT NULL, 
description_variable_metier text, 
type_consolidation text, 
CONSTRAINT pk_ihm_mod_variable_metier PRIMARY KEY (id_famille, nom_table_metier, nom_variable_metier)
);

ALTER TABLE arc.ihm_mod_variable_metier DROP CONSTRAINT IF EXISTS fk_ihm_mod_variable_table_metier;

do $$ begin ALTER TABLE arc.ihm_mod_variable_metier ADD CONSTRAINT fk2_ihm_mod_variable_table_metier FOREIGN KEY (id_famille,nom_table_metier) REFERENCES arc.ihm_mod_table_metier(id_famille,nom_table_metier) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE; EXCEPTION WHEN OTHERS then end; $$;
        
CREATE TABLE IF NOT EXISTS arc.ihm_nmcl 
( 
nom_table text NOT NULL, 
description text, 
CONSTRAINT ihm_nmcl_pkey PRIMARY KEY (nom_table) 
); 
       
        
CREATE TABLE IF NOT EXISTS arc.ihm_schema_nmcl 
( 
type_nmcl text, 
nom_colonne text, 
type_colonne text 
); 

DROP TABLE IF EXISTS arc.ihm_seuil;

-- table des users
CREATE TABLE IF NOT EXISTS arc.ihm_user 
( 
idep text NOT NULL, 
profil text, 
CONSTRAINT ihm_user_pkey PRIMARY KEY (idep) 
); 

-- table des entrepots de données        
CREATE TABLE IF NOT EXISTS arc.ihm_entrepot 
( 
  id_entrepot text NOT NULL, 
  id_loader text, -- deprecated
  CONSTRAINT ihm_entrepot_pkey PRIMARY KEY (id_entrepot) 
); 
INSERT INTO arc.ihm_entrepot values ('DEFAULT','DEFAULT') ON CONFLICT DO NOTHING;

-- table de gestion des webservices
CREATE TABLE IF NOT EXISTS arc.ihm_ws_context 
( 
  service_name text NOT NULL, 
  service_type integer, 
call_id integer NOT NULL, 
environment text, 
target_phase text, 
norme text, 
validite text, 
periodicite text, 
CONSTRAINT ws_engine_context_pkey PRIMARY KEY (service_name, call_id) 
); 


CREATE TABLE IF NOT EXISTS arc.ihm_ws_query 
( 
query_id integer NOT NULL, 
query_name text NOT NULL, 
expression text, 
query_view integer, 
service_name text NOT NULL, 
call_id integer NOT NULL, 
CONSTRAINT ws_engine_queries_pkey PRIMARY KEY (service_name, call_id, query_id), 
CONSTRAINT ws_engine_queries_fkey FOREIGN KEY (service_name, call_id) 
REFERENCES arc.ihm_ws_context (service_name, call_id) MATCH SIMPLE 
ON UPDATE CASCADE ON DELETE CASCADE 
); 


CREATE TABLE IF NOT EXISTS arc.ext_webservice_type 
( 
id text NOT NULL, 
val text, 
CONSTRAINT ext_webservice_type_pkey PRIMARY KEY (id) 
); 

INSERT INTO arc.ext_webservice_type VALUES ('1','ENGINE'), ('2','SERVICE') ON CONFLICT DO NOTHING; 

CREATE TABLE IF NOT EXISTS arc.ext_webservice_queryview 
( 
id text NOT NULL, 
val text, 
CONSTRAINT ext_webservice_queryview_pkey PRIMARY KEY (id) 
);

INSERT INTO arc.ext_webservice_queryview VALUES ('1','COLUMN'), ('2','LINE') ON CONFLICT DO NOTHING; 
        

-- table de modalités format d'export
CREATE TABLE IF NOT EXISTS arc.ext_export_format 
( 
id text NOT NULL, 
val text, 
CONSTRAINT ext_export_format_pkey PRIMARY KEY (id) 
); 

INSERT INTO arc.ext_export_format VALUES ('0','NA'), ('1','ZIP'), ('2','GZ') ON CONFLICT DO NOTHING; 


/*
 *  PATCHS version
 */
-- PATCH SUPPRESION DES TABLES OBSOLETES arc.test_ihm_...
do $$
declare
	table_test text;
BEGIN
for table_test in (select schemaname||'.'||tablename from pg_tables where tablename like 'test_ihm%' and schemaname='arc')
loop
execute 'drop table if exists '||table_test;
end loop;
end;
$$;


-- PATCH SUPPRESSION FILTRAGE
-- 1. mise à jour des tables de pilotage
do $$
declare table_to_update text;
BEGIN
for table_to_update in (select schemaname||'.'||tablename from pg_tables where tablename='pilotage_fichier' and schemaname like 'arc%')
loop

execute 'delete from '||table_to_update||' where phase_traitement=''FILTRAGE'' and etat_traitement=''{OK}'' and etape=0;';
commit;

execute '
with tmp_filtrage_etape1 as
(
	select id_source, etape, rapport, etat_traitement from '||table_to_update||' where phase_traitement=''FILTRAGE'' and etape in (1,2)
)
, update_controle_etape1 as
(
	update '||table_to_update||' a set etape=b.etape, rapport=b.rapport
	, etat_traitement=case when b.etat_traitement=''{OK}'' and a.etat_traitement=''{OK,KO}'' then ''{OK,KO}'' else b.etat_traitement end
	from tmp_filtrage_etape1 b
	where a.id_source=b.id_source and a.phase_traitement=''CONTROLE''
)
delete from '||table_to_update||' where phase_traitement=''FILTRAGE'' and etape in (1,2);
';
commit;
end loop;
end;
$$;

-- 2. recopie des règles de filtrage dans le contrôle + suppression des tables de regles de filtrage
do $$
declare
	table_filtrage text;
	table_controle text;
BEGIN
for table_filtrage in (select schemaname||'.'||tablename from pg_tables where tablename like '%filtrage_regle' and schemaname like 'arc%')
loop

table_controle:=replace(table_filtrage,'filtrage_regle','controle_regle');

-- patch table model if control table model is still in an old state
execute 'ALTER TABLE '||table_controle||' add column IF NOT exists xsd_ordre int;';
execute 'ALTER TABLE '||table_controle||' add column IF NOT exists xsd_label_fils text;';
execute 'ALTER TABLE '||table_controle||' add column IF NOT exists xsd_role text;';
execute 'ALTER TABLE '||table_controle||' add column IF NOT exists blocking_threshold text;';
execute 'ALTER TABLE '||table_controle||' add column IF NOT exists error_row_processing text;';
	
-- add the filter rule to control rule table
execute 'insert into '||table_controle||' (id_regle, id_norme, validite_inf, validite_sup, version, periodicite, id_classe, condition, blocking_threshold, error_row_processing, commentaire)
select 
(select coalesce(max(id_regle),0)+1 from '||table_controle||' b  where a.id_norme=b.id_norme 
and a.validite_inf=b.validite_inf 
and a.validite_sup=b.validite_sup
and a.version=b.version
and a.periodicite=b.periodicite),
id_norme, validite_inf, validite_sup, version, periodicite, ''CONDITION'', ''NOT(''||expr_regle_filtre||'')'', ''>0u'', ''e'', coalesce(a.commentaire, ''règle de filtrage'')
from '||table_filtrage||' a
where not exists (select from '||table_controle||' b 
where a.id_norme=b.id_norme 
and a.validite_inf=b.validite_inf 
and a.validite_sup=b.validite_sup
and a.version=b.version
and a.periodicite=b.periodicite
and ''NOT(''||a.expr_regle_filtre||'')''=b.condition
)
and trim(expr_regle_filtre) is not null and trim(expr_regle_filtre)!='''' and lower(trim(expr_regle_filtre))!=''false''
'
;
commit;

execute 'DROP TABLE IF EXISTS '||table_filtrage||' cascade;';
commit;

end loop;
end;
$$;

-- PATCH 03/07/2023
-- 3. suppression et recopie des tables de filtrage de données
do $$
declare
	table_filtrage text;
BEGIN
for table_filtrage in (select schemaname||'.'||tablename from pg_tables where tablename in ('filtrage_ok','filtrage_ko','filtrage_ok_todo') and schemaname like 'arc%')
loop
execute 'drop table if exists '||table_filtrage||' cascade;';
commit;
end loop;
end;
$$;

-- cas des filtrages ko
do $$
declare
	table_filtrage_ko text;
	table_filtrage_ok text;
	table_controle_ok text;
	table_controle_ko text;
	test_controle_ko int;
	test_filtrage_ok int;

BEGIN
	
-- cas des tables filtrage ko
for table_filtrage_ko in (select schemaname||'.'||tablename from pg_tables where tablename like 'filtrage_ko_child%' and schemaname like 'arc%')
loop

-- pour retrouver les tables de filtrage et controles correspondantes
table_filtrage_ok:=replace(table_filtrage_ko,'_ko_child','_ok_child');
table_controle_ok:=replace(table_filtrage_ok,'filtrage','controle');
table_controle_ko:=replace(table_filtrage_ko,'filtrage','controle');

select count(1) into test_controle_ko from pg_tables where schemaname||'.'||tablename=table_controle_ko;
select count(1) into test_filtrage_ok from pg_tables where schemaname||'.'||tablename=table_filtrage_ok;

-- si la table controle_ko_child existe déjà, on la merge avec filtrage_ko_child
-- sinon on crée controle_ko_child en recopiant filtrage_ko_child
if (test_controle_ko>0) then
	execute 'INSERT INTO '||table_controle_ko||' SELECT * FROM '||table_filtrage_ko||';';
	commit;
	else
	execute 'CREATE TABLE '||table_controle_ko||' with (autovacuum_enabled = false, toast.autovacuum_enabled = false) AS SELECT * FROM '||table_filtrage_ko||';';
	commit;
end if;

-- on drop la table filtrage_ko
execute 'DROP TABLE IF EXISTS '||table_filtrage_ko||';';

-- si la table filtrage_ok existe, on remplace la table controle ok par la table filtrage_ok
if (test_filtrage_ok>0) then
	execute 'drop table if exists '||table_controle_ok||';';
	commit;
	execute 'alter table '||table_filtrage_ok||' rename to '||substring(table_controle_ok,strpos(table_controle_ok,'.')+1)||';';
	commit;
end if;

end loop;
end;
$$;

-- cas simple des filtrages ok
do $$
declare
	table_filtrage_ok text;
	table_controle_ok text;
BEGIN
	
for table_filtrage_ok in (select schemaname||'.'||tablename from pg_tables where tablename like 'filtrage_ok_child%' and schemaname like 'arc%')
loop
-- on remplace la table controle ok par la table filtrage_ok
table_controle_ok:=replace(table_filtrage_ok,'filtrage','controle');

execute 'drop table if exists '||table_controle_ok||';';
commit;
execute 'alter table '||table_filtrage_ok||' rename to '||substring(table_controle_ok,strpos(table_controle_ok,'.')+1)||';';
commit;

end loop;
end;
$$;


