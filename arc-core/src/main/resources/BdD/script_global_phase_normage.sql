CREATE TABLE IF NOT EXISTS arc.ext_type_normage 
( 
  id text NOT NULL, 
  ordre integer, 
  CONSTRAINT ext_type_normage_pkey PRIMARY KEY (id) 
);
INSERT INTO arc.ext_type_normage values ('relation','1'),('cartesian','2'),('suppression','3'),('unicité','4') ON CONFLICT DO NOTHING;
INSERT INTO arc.ext_type_normage values ('reduction','5') ON CONFLICT DO NOTHING;
INSERT INTO arc.ext_type_normage values ('partition','6') ON CONFLICT DO NOTHING;
INSERT INTO arc.ext_type_normage values ('exclusion','7') ON CONFLICT DO NOTHING;
INSERT INTO arc.ext_type_normage values ('independance','8') ON CONFLICT DO NOTHING;

CREATE TABLE IF NOT EXISTS arc.ihm_normage_regle 
( 
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
CONSTRAINT ihm_normage_regle_pkey PRIMARY KEY (id_norme, periodicite, validite_inf, validite_sup, version, id_regle), 
CONSTRAINT ihm_normage_regle_jeuderegle_fkey FOREIGN KEY (id_norme, periodicite, validite_inf, validite_sup, version) 
      REFERENCES arc.ihm_jeuderegle (id_norme, periodicite, validite_inf, validite_sup, version) MATCH SIMPLE 
     ON UPDATE CASCADE ON DELETE CASCADE   
);

alter table arc.ihm_normage_regle alter column id_classe set NOT NULL;

commit;do $$ begin alter table arc.ihm_normage_regle ADD CONSTRAINT ihm_normage_regle_id_classe_fkey FOREIGN KEY (id_classe) REFERENCES arc.ext_type_normage (id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE; EXCEPTION WHEN OTHERS then end; $$;
commit;do $$ begin CREATE TRIGGER tg_insert_normage BEFORE INSERT ON arc.ihm_normage_regle FOR EACH ROW EXECUTE PROCEDURE arc.insert_controle(); exception when others then end; $$;

-- PATCH 06/10/2023 Remove deprecated rules
DELETE FROM arc.ext_type_normage where id in ('suppression','exclusion', 'reduction');

commit;do $$
declare
	table_regle_normage text;
BEGIN
for table_regle_normage in (select schemaname||'.'||tablename from pg_tables where (tablename='normage_regle' and schemaname like 'arc_bas%') or (tablename='ihm_normage_regle' and schemaname = 'arc') )
loop
execute 'DELETE FROM '||table_regle_normage||' WHERE id_classe in (''suppression'',''exclusion'', ''reduction'');';
commit;
end loop;
end;
$$;

