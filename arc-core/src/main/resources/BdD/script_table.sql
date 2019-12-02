CREATE SCHEMA IF NOT EXISTS arc;

CREATE TABLE IF NOT EXISTS arc.ihm_paramettrage_ordre_phase (
  id_norme text,
  validite_inf date,
  validite_sup date,
  version text,
  periodicite text,
  nom_phase text,
  type_phase text,
  ordre integer,
  nb_ligne_traitee integer,
  phase_precedente text,
  is_needed boolean,
  is_in_ihm boolean,
  is_ra_ihm boolean,
   CONSTRAINT pk_ihm_paramettrage_ordre_phase PRIMARY KEY (ordre)
  /* CONSTRAINT pk_ihm_paramettrage_ordre_phase PRIMARY KEY (nom_phase, id_norme, validite_inf, validite_sup, version, periodicite) */
);

   
do $$ begin 
insert into arc.ihm_paramettrage_ordre_phase
(id_norme,validite_inf,validite_sup,version,periodicite,nom_phase,type_phase,ordre,nb_ligne_traitee,phase_precedente,is_needed,is_in_ihm,is_ra_ihm)
values 
(null,null,null,null,null,'DUMMY','DUMMY',0,1,null,true,true,true)
,(null,null,null,null,null,'INITIALIZE','INITIALIZE',1,1,'DUMMY',true,true,true)
,(null,null,null,null,null,'REGISTER','REGISTER',2,1,'INITIALIZE',true,true,true)
,(null,null,null,null,null,'IDENTIFY','IDENTIFY',3,1,'REGISTER',true,true,true)
,(null,null,null,null,null,'LOAD','LOAD',4,1,'IDENTIFY',true,true,true)
,(null,null,null,null,null,'STRUCTURIZE_XML','STRUCTURIZE_XML',5,1,'LOAD',false,true,true)
,(null,null,null,null,null,'CONTROL','CONTROL',6,1,'STRUCTURIZE_XML',false,true,true)
,(null,null,null,null,null,'FILTER','FILTER',7,1,'CONTROL',false,true,true)
,(null,null,null,null,null,'FORMAT_TO_MODEL','FORMAT_TO_MODEL',8,1,'FILTER',false,true,true)
;
EXCEPTION WHEN OTHERS then end; $$;

/* Consistancy update. Phases names should be single token */
update arc.ihm_paramettrage_ordre_phase set nom_phase='STRUCTURIZE', type_phase='STRUCTURIZE' where nom_phase='STRUCTURIZE_XML';
update arc.ihm_paramettrage_ordre_phase set phase_precedente='STRUCTURIZE' where phase_precedente='STRUCTURIZE_XML';
update arc.ihm_paramettrage_ordre_phase set nom_phase='MAPMODEL', type_phase='MAPMODEL' where nom_phase='FORMAT_TO_MODEL';
update arc.ihm_paramettrage_ordre_phase set nom_phase='MAPPING', type_phase='MAPPING' where nom_phase='MAPMODEL';


CREATE TABLE IF NOT EXISTS arc.ext_etat (
  id text,
  val text,
  CONSTRAINT ext_etat_pkey PRIMARY KEY (id)
);
do $$ begin
INSERT INTO
  arc.ext_etat
VALUES
  ('0', 'INACTIF'),('1', 'ACTIF');
exception
  when others then
end;
$$;
CREATE TABLE IF NOT EXISTS arc.ext_etat_jeuderegle (
  id text NOT NULL,
  val text,
  isenv boolean,
  mise_a_jour_immediate boolean,
  CONSTRAINT ext_etat_jeuderegle_pkey PRIMARY KEY (id)
);
do $$ begin
INSERT INTO
  arc.ext_etat_jeuderegle
values
  ('arc.bas', 'BAC A SABLE', 'TRUE', 'TRUE'),('arc.bas2', 'BAC A SABLE 2', 'TRUE', 'TRUE'),('arc.bas3', 'BAC A SABLE 3', 'TRUE', 'TRUE'),('arc.bas4', 'BAC A SABLE 4', 'TRUE', 'TRUE'),('arc.bas5', 'BAC A SABLE 5', 'TRUE', 'TRUE'),('arc.bas6', 'BAC A SABLE 6', 'TRUE', 'TRUE'),('arc.bas7', 'BAC A SABLE 7', 'TRUE', 'TRUE'),('arc.bas8', 'BAC A SABLE 8', 'TRUE', 'TRUE'),('arc.prod', 'PRODUCTION', 'TRUE', 'FALSE'),('inactif', 'INACTIF', 'FALSE', 'FALSE');
exception
  when others then
end;
$$;
UPDATE arc.ext_etat_jeuderegle set id='arc.bas1', val='BAC A SABLE 1' where id='arc.bas';
CREATE TABLE IF NOT EXISTS arc.ext_etat_jeuderegle (
  id text NOT NULL,
  val text,
  isenv boolean,
  mise_a_jour_immediate boolean,
  CONSTRAINT ext_etat_jeuderegle_pkey PRIMARY KEY (id)
);
do $$ begin
INSERT INTO
  arc.ext_etat_jeuderegle
values
  ('arc.bas', 'BAC A SABLE', 'TRUE', 'TRUE'),('arc.bas2', 'BAC A SABLE 2', 'TRUE', 'TRUE'),('arc.bas3', 'BAC A SABLE 3', 'TRUE', 'TRUE'),('arc.bas4', 'BAC A SABLE 4', 'TRUE', 'TRUE'),('arc.bas5', 'BAC A SABLE 5', 'TRUE', 'TRUE'),('arc.bas6', 'BAC A SABLE 6', 'TRUE', 'TRUE'),('arc.bas7', 'BAC A SABLE 7', 'TRUE', 'TRUE'),('arc.bas8', 'BAC A SABLE 8', 'TRUE', 'TRUE'),('arc.prod', 'PRODUCTION', 'TRUE', 'FALSE'),('inactif', 'INACTIF', 'FALSE', 'FALSE');
exception
  when others then
end;
$$;
CREATE TABLE IF NOT EXISTS arc.ext_mod_periodicite (
  id text,
  val text,
  CONSTRAINT ext_mod_periodicite_pkey PRIMARY KEY (id)
);
do $$ begin
INSERT INTO
  arc.ext_mod_periodicite
values
  ('M', 'MENSUEL'),('A', 'ANNUEL');
exception
  when others then
end;
$$;
CREATE TABLE IF NOT EXISTS arc.ext_mod_periodicite (
  id text,
  val text,
  CONSTRAINT ext_mod_periodicite_pkey PRIMARY KEY (id)
);
do $$ begin
INSERT INTO
  arc.ext_mod_periodicite
values
  ('M', 'MENSUEL'),('A', 'ANNUEL');
exception
  when others then
end;
$$;
CREATE TABLE IF NOT EXISTS arc.ext_mod_type_autorise (
  nom_type name NOT NULL,
  description_type text NOT NULL,
  CONSTRAINT pk_ext_mod_type_autorise PRIMARY KEY (nom_type)
);
do $$ begin
INSERT INTO
  arc.ext_mod_type_autorise
values
  ('bigint', 'Entier'),('bigint[]', 'Tableau d''entier long'),(
    'boolean',
    'Vrai (t ou true) ou faux (f ou false)'
  ),('date', 'Date'),('date[]', 'Tableau de date'),('float', 'Nombre décimal virgule flottante'),('float[]', 'Tableau de nombre décimaux'),('interval', 'Durée (différence de deux dates)'),('text', 'Texte sans taille limite'),('text[]', 'Tableau de texte sans limite'),('timestamp without time zone', 'Date et heure');
exception
  when others then
end;
$$;
CREATE TABLE IF NOT EXISTS arc.ext_type_controle (
  id text NOT NULL,
  ordre integer,
  CONSTRAINT ext_type_controle_pkey PRIMARY KEY (id)
);
do $$ begin
INSERT INTO
  arc.ext_type_controle
values
  ('ALPHANUM', '2'),('CARDINALITE', '1'),('CONDITION', '5'),('DATE', '4'),('NUM', '3');
exception
  when others then
end;
$$;
CREATE TABLE IF NOT EXISTS arc.ext_type_fichier_chargement (
  id text NOT NULL,
  ordre integer,
  CONSTRAINT ext_type_fichier_chargement_pkey PRIMARY KEY (id)
);
do $$ begin
INSERT INTO
  arc.ext_type_fichier_chargement
values
  ('xml', '1'),('clef-valeur', '2'),('plat', '3');
exception
  when others then
end;
$$;
CREATE TABLE IF NOT EXISTS arc.ext_type_normage (
  id text NOT NULL,
  ordre integer,
  CONSTRAINT ext_type_normage_pkey PRIMARY KEY (id)
);
do $$ begin
INSERT INTO
  arc.ext_type_normage
values
  ('relation', '1'),('cartesian', '2'),('suppression', '3'),('unicité', '4');
exception
  when others then
end;
$$;

CREATE TABLE IF NOT EXISTS arc.ihm_famille (
  id_famille text NOT NULL,
  CONSTRAINT ihm_famille_pkey PRIMARY KEY (id_famille)
);
CREATE TABLE IF NOT EXISTS arc.ihm_client (
  id_famille text NOT NULL,
  id_application text NOT NULL,
  CONSTRAINT pk_ihm_client PRIMARY KEY (id_famille, id_application),
  CONSTRAINT fk_client_famille FOREIGN KEY (id_famille) REFERENCES arc.ihm_famille (id_famille) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION
);
CREATE TABLE IF NOT EXISTS arc.ihm_norme (
  id_norme text NOT NULL,
  periodicite text NOT NULL,
  def_norme text NOT NULL,
  def_validite text NOT NULL,
  id serial NOT NULL,
  etat text,
  id_famille text,
  CONSTRAINT ihm_norme_pkey PRIMARY KEY (id_norme, periodicite),
  CONSTRAINT ihm_norme_id_famille_fkey FOREIGN KEY (id_famille) REFERENCES arc.ihm_famille (id_famille) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS arc.ihm_calendrier (
  id_norme text NOT NULL,
  periodicite text NOT NULL,
  validite_inf date NOT NULL,
  validite_sup date NOT NULL,
  id serial NOT NULL,
  etat text,
  CONSTRAINT ihm_calendrier_pkey PRIMARY KEY (
    id_norme,
    periodicite,
    validite_inf,
    validite_sup
  ),
  CONSTRAINT ihm_calendrier_norme_fkey FOREIGN KEY (id_norme, periodicite) REFERENCES arc.ihm_norme (id_norme, periodicite) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS arc.ihm_jeuderegle (
  id_norme text NOT NULL,
  periodicite text NOT NULL,
  validite_inf date NOT NULL,
  validite_sup date NOT NULL,
  version text NOT NULL,
  etat text,
  date_production date,
  date_inactif date,
  CONSTRAINT ihm_jeuderegle_pkey PRIMARY KEY (
    id_norme,
    periodicite,
    validite_inf,
    validite_sup,
    version
  ),
  CONSTRAINT ihm_jeuderegle_calendrier_fkey FOREIGN KEY (
    id_norme,
    periodicite,
    validite_inf,
    validite_sup
  ) REFERENCES arc.ihm_calendrier (
    id_norme,
    periodicite,
    validite_inf,
    validite_sup
  ) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS arc.ihm_chargement_regle (
  id_regle bigint NOT NULL,
  id_norme text NOT NULL,
  validite_inf date NOT NULL,
  validite_sup date NOT NULL,
  version text NOT NULL,
  periodicite text NOT NULL,
  type_fichier text,
  delimiter text,
  format text,
  commentaire text,
  CONSTRAINT pk_ihm_chargement_regle PRIMARY KEY (
    id_regle,
    id_norme,
    validite_inf,
    validite_sup,
    version,
    periodicite
  ),
  CONSTRAINT ihm_chargement_regle_jeuderegle_fkey FOREIGN KEY (
    id_norme,
    periodicite,
    validite_inf,
    validite_sup,
    version
  ) REFERENCES arc.ihm_jeuderegle (
    id_norme,
    periodicite,
    validite_inf,
    validite_sup,
    version
  ) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS arc.ihm_normage_regle (
  id_norme text NOT NULL,
  periodicite text NOT NULL,
  validite_inf date NOT NULL,
  validite_sup date NOT NULL,
  version text NOT NULL,
  id_classe text NOT NULL,
  rubrique text,
  rubrique_nmcl text,
  id_regle integer NOT NULL,
  todo text,
  commentaire text,
  CONSTRAINT ihm_normage_regle_pkey PRIMARY KEY (
    id_norme,
    periodicite,
    validite_inf,
    validite_sup,
    version,
    id_regle
  ),
  CONSTRAINT ihm_normage_regle_jeuderegle_fkey FOREIGN KEY (
    id_norme,
    periodicite,
    validite_inf,
    validite_sup,
    version
  ) REFERENCES arc.ihm_jeuderegle (
    id_norme,
    periodicite,
    validite_inf,
    validite_sup,
    version
  ) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS arc.ihm_controle_regle (
  id_norme text NOT NULL,
  periodicite text NOT NULL,
  validite_inf date NOT NULL,
  validite_sup date NOT NULL,
  version text NOT NULL,
  id_classe text,
  rubrique_pere text,
  rubrique_fils text,
  borne_inf text,
  borne_sup text,
  condition text,
  pre_action text,
  id_regle integer NOT NULL,
  todo text,
  commentaire text,
  CONSTRAINT ihm_controle_regle_pkey PRIMARY KEY (
    id_norme,
    periodicite,
    validite_inf,
    validite_sup,
    version,
    id_regle
  ),
  CONSTRAINT ihm_controle_regle_jeuderegle_fkey FOREIGN KEY (
    id_norme,
    periodicite,
    validite_inf,
    validite_sup,
    version
  ) REFERENCES arc.ihm_jeuderegle (
    id_norme,
    periodicite,
    validite_inf,
    validite_sup,
    version
  ) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS arc.ihm_filtrage_regle (
  id_regle bigint NOT NULL,
  id_norme text NOT NULL,
  validite_inf date NOT NULL,
  validite_sup date NOT NULL,
  version text NOT NULL,
  periodicite text NOT NULL,
  expr_regle_filtre text,
  commentaire text,
  CONSTRAINT pk_ihm_mapping_filtrage_regle PRIMARY KEY (
    id_regle,
    id_norme,
    validite_inf,
    validite_sup,
    version,
    periodicite
  ),
  CONSTRAINT fk_ihm_mapping_filtrage_regle_jeuderegle FOREIGN KEY (
    id_norme,
    periodicite,
    validite_inf,
    validite_sup,
    version
  ) REFERENCES arc.ihm_jeuderegle (
    id_norme,
    periodicite,
    validite_inf,
    validite_sup,
    version
  ) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS arc.ihm_mapping_regle (
  id_regle bigint,
  id_norme text NOT NULL,
  validite_inf date NOT NULL,
  validite_sup date NOT NULL,
  version text NOT NULL,
  periodicite text NOT NULL,
  variable_sortie character varying(63) NOT NULL,
  expr_regle_col text,
  commentaire text,
  CONSTRAINT pk_ihm_mapping_regle PRIMARY KEY (
    id_norme,
    periodicite,
    validite_inf,
    validite_sup,
    version,
    variable_sortie
  ),
  CONSTRAINT fk_ihm_mapping_regle_jeuderegle FOREIGN KEY (
    id_norme,
    periodicite,
    validite_inf,
    validite_sup,
    version
  ) REFERENCES arc.ihm_jeuderegle (
    id_norme,
    periodicite,
    validite_inf,
    validite_sup,
    version
  ) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS arc.ihm_mod_table_metier (
  id_famille text NOT NULL,
  nom_table_metier text NOT NULL,
  description_table_metier text,
  CONSTRAINT pk_ihm_mod_table_metier PRIMARY KEY (id_famille, nom_table_metier),
  CONSTRAINT fk_ihm_table_metier_famille FOREIGN KEY (id_famille) REFERENCES arc.ihm_famille (id_famille) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION
);
CREATE TABLE IF NOT EXISTS arc.ihm_mod_variable_metier (
  id_famille text NOT NULL,
  nom_table_metier text NOT NULL,
  nom_variable_metier text NOT NULL,
  type_variable_metier name NOT NULL,
  description_variable_metier text,
  type_consolidation text,
  CONSTRAINT pk_ihm_mod_variable_metier PRIMARY KEY (
    id_famille,
    nom_table_metier,
    nom_variable_metier
  ),
  CONSTRAINT fk_ihm_mod_variable_table_metier FOREIGN KEY (id_famille, nom_table_metier) REFERENCES arc.ihm_mod_table_metier (id_famille, nom_table_metier) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION
);

alter table arc.ihm_mod_variable_metier alter column type_consolidation set default '{exclus}';


CREATE TABLE IF NOT EXISTS arc.ihm_nmcl (
  nom_table text NOT NULL,
  description text,
  CONSTRAINT ihm_nmcl_pkey PRIMARY KEY (nom_table)
);
CREATE TABLE IF NOT EXISTS arc.ihm_schema_nmcl (
  type_nmcl text,
  nom_colonne text,
  type_colonne text
);
CREATE TABLE IF NOT EXISTS arc.ihm_seuil (
  nom text,
  valeur numeric,
  CONSTRAINT ihm_seuil_pkey PRIMARY KEY (nom)
);
do $$ begin
INSERT INTO
  arc.ihm_seuil
values
  ('filtrage_taux_exclusion_accepte', 1.0),('s_taux_erreur', 0.5);
exception
  when others then
end;
$$;
CREATE TABLE IF NOT EXISTS arc.ihm_user (
  idep text NOT NULL,
  profil text,
  CONSTRAINT ihm_user_pkey PRIMARY KEY (idep)
);
CREATE TABLE IF NOT EXISTS arc.ihm_entrepot (
  id_entrepot text NOT NULL,
  id_loader text,
  CONSTRAINT ihm_entrepot_pkey PRIMARY KEY (id_entrepot)
);
do $$ begin
INSERT INTO
  arc.ihm_entrepot
values
  ('DEFAULT', 'DEFAULT');
exception
  when others then
end;
$$;