CREATE TABLE IF NOT EXISTS arc.ext_type_controle 
( 
  id text NOT NULL, 
  ordre integer, 
  CONSTRAINT ext_type_controle_pkey PRIMARY KEY (id) 
); 
INSERT INTO arc.ext_type_controle values ('ALPHANUM','2') ON CONFLICT DO NOTHING;
INSERT INTO arc.ext_type_controle values ('CARDINALITE','1') ON CONFLICT DO NOTHING;
INSERT INTO arc.ext_type_controle values ('CONDITION','5') ON CONFLICT DO NOTHING;
INSERT INTO arc.ext_type_controle values ('DATE','4') ON CONFLICT DO NOTHING;
INSERT INTO arc.ext_type_controle values ('NUM','3') ON CONFLICT DO NOTHING;
INSERT INTO arc.ext_type_controle values ('REGEXP', '6') ON CONFLICT DO NOTHING;
INSERT INTO arc.ext_type_controle values ('ENUM_BRUTE', '7') ON CONFLICT DO NOTHING;
INSERT INTO arc.ext_type_controle values ('ENUM_TABLE', '8') ON CONFLICT DO NOTHING;

CREATE TABLE IF NOT EXISTS arc.ihm_controle_regle 
( 
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
  CONSTRAINT ihm_controle_regle_pkey PRIMARY KEY (id_norme, periodicite, validite_inf, validite_sup, version, id_regle), 
  CONSTRAINT ihm_controle_regle_jeuderegle_fkey FOREIGN KEY (id_norme, periodicite, validite_inf, validite_sup, version) 
      REFERENCES arc.ihm_jeuderegle (id_norme, periodicite, validite_inf, validite_sup, version) MATCH SIMPLE 
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT ihm_controle_regle_id_classe_fkey FOREIGN KEY (id_classe) 
      REFERENCES arc.ext_type_controle (id) MATCH SIMPLE 
     ON UPDATE CASCADE ON DELETE CASCADE     
);

ALTER TABLE arc.ihm_controle_regle add column IF NOT exists xsd_ordre int;
ALTER TABLE arc.ihm_controle_regle add column IF NOT exists xsd_label_fils text;
ALTER TABLE arc.ihm_controle_regle add column IF NOT exists xsd_role text;
ALTER TABLE arc.ihm_controle_regle add column IF NOT exists blocking_threshold text;
ALTER TABLE arc.ihm_controle_regle add column IF NOT exists error_row_processing text default 'e';
ALTER TABLE arc.ihm_controle_regle drop constraint if exists ihm_controle_regle_seuil_bloquant_check;

do $$ begin alter table arc.ihm_controle_regle ADD CONSTRAINT ihm_controle_regle_id_classe_fkey FOREIGN KEY (id_classe) REFERENCES arc.ext_type_controle (id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE; EXCEPTION WHEN OTHERS then end; $$;
do $$ begin ALTER TABLE arc.ihm_controle_regle add constraint ihm_controle_regle_seuil_bloquant_check check (blocking_threshold ~ '^(>|>=)[0123456789.]+[%u]$'); exception when others then end; $$;

DROP TRIGGER IF EXISTS doublon ON arc.ihm_controle_regle CASCADE;

do $$ begin CREATE TRIGGER tg_insert_controle BEFORE INSERT ON arc.ihm_controle_regle FOR EACH ROW EXECUTE PROCEDURE arc.insert_controle(); exception when others then end; $$;



-- PATCH 06/10/2023 Remove deprecated rules
DELETE FROM arc.ext_type_controle where id='STRUCTURE';

do $$
declare
	table_regle_controle text;
BEGIN
for table_regle_controle in (select schemaname||'.'||tablename from pg_tables where (tablename='controle_regle' and schemaname like 'arc_bas%') or (tablename='ihm_controle_regle' and schemaname = 'arc') )
loop
execute 'DELETE FROM '||table_regle_controle||' WHERE id_classe= ''STRUCTURE'';';
commit;
end loop;
end;
$$;

