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
  xsd_ordre int,
  xsd_label_fils text,
  xsd_role text,
  CONSTRAINT ihm_controle_regle_pkey PRIMARY KEY (id_norme, periodicite, validite_inf, validite_sup, version, id_regle), 
  CONSTRAINT ihm_controle_regle_jeuderegle_fkey FOREIGN KEY (id_norme, periodicite, validite_inf, validite_sup, version) 
      REFERENCES arc.ihm_jeuderegle (id_norme, periodicite, validite_inf, validite_sup, version) MATCH SIMPLE 
      ON UPDATE CASCADE ON DELETE CASCADE 
);

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
INSERT INTO arc.ext_type_controle values ('STRUCTURE', '9') ON CONFLICT DO NOTHING;

do $$ begin CREATE TRIGGER tg_insert_controle BEFORE INSERT ON arc.ihm_controle_regle FOR EACH ROW EXECUTE PROCEDURE arc.insert_controle(); exception when others then end; $$;
