-- schéma global arc
CREATE SCHEMA IF NOT EXISTS arc; 
        
-- table de parametrage de l'application
CREATE TABLE IF NOT EXISTS arc.parameter 
( 
key text, 
val text, 
CONSTRAINT parameter_pkey PRIMARY KEY (key) 
); 
        
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

-- enregistrement des bacs à sable
do $$
BEGIN
for i in 1..{{nbSandboxes}} loop
INSERT INTO arc.ext_etat_jeuderegle values ('arc.bas'||i,'BAC A SABLE "+i+"','TRUE','TRUE') ON CONFLICT DO NOTHING;
end loop;
end;
$$;

INSERT INTO arc.ext_etat_jeuderegle values ('arc.prod','PRODUCTION','TRUE','FALSE') ON CONFLICT DO NOTHING;
INSERT INTO arc.ext_etat_jeuderegle values ('inactif','INACTIF','FALSE','FALSE') ON CONFLICT DO NOTHING;


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

CREATE TABLE IF NOT EXISTS arc.ihm_famille 
( 
  id_famille text NOT NULL, 
  CONSTRAINT ihm_famille_pkey PRIMARY KEY (id_famille) 
); 
        
CREATE TABLE IF NOT EXISTS arc.ihm_client 
(id_famille text NOT NULL, 
id_application text NOT NULL, 
CONSTRAINT pk_ihm_client PRIMARY KEY (id_famille, id_application), 
CONSTRAINT fk_client_famille FOREIGN KEY (id_famille) 
REFERENCES arc.ihm_famille (id_famille) MATCH SIMPLE 
ON UPDATE NO ACTION ON DELETE NO ACTION 
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
CONSTRAINT pk_ihm_mod_table_metier PRIMARY KEY (id_famille, nom_table_metier), 
CONSTRAINT fk_ihm_table_metier_famille FOREIGN KEY (id_famille) 
REFERENCES arc.ihm_famille (id_famille) MATCH SIMPLE 
ON UPDATE NO ACTION ON DELETE NO ACTION 
); 
        
CREATE TABLE IF NOT EXISTS arc.ihm_mod_variable_metier 
( 
id_famille text NOT NULL, 
nom_table_metier text NOT NULL, 
nom_variable_metier text NOT NULL, 
type_variable_metier name NOT NULL, 
description_variable_metier text, 
type_consolidation text, 
CONSTRAINT pk_ihm_mod_variable_metier PRIMARY KEY (id_famille, nom_table_metier, nom_variable_metier), 
CONSTRAINT fk_ihm_mod_variable_table_metier FOREIGN KEY (id_famille, nom_table_metier) 
REFERENCES arc.ihm_mod_table_metier (id_famille, nom_table_metier) MATCH SIMPLE 
ON UPDATE NO ACTION ON DELETE NO ACTION 
); 

        
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

CREATE TABLE IF NOT EXISTS arc.ihm_seuil (
	nom text NOT NULL,
	valeur numeric NULL,
	CONSTRAINT ihm_seuil_pkey PRIMARY KEY (nom)
);

INSERT INTO arc.ihm_seuil values ('filtrage_taux_exclusion_accepte',1.0) ,('s_taux_erreur',1.0) ON CONFLICT DO NOTHING;

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
  id_loader text, 
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
        

-- grant / revoke
REVOKE ALL ON SCHEMA public FROM public;
REVOKE ALL ON SCHEMA arc FROM public; 

-- restricted role for service execution
do $$ begin CREATE ROLE arc_restricted with NOINHERIT; exception when others then end; $$;

GRANT USAGE ON SCHEMA public TO arc_restricted;
GRANT EXECUTE ON ALL ROUTINES IN SCHEMA public to arc_restricted;
