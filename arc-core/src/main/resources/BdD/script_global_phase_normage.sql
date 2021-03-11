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

CREATE TABLE IF NOT EXISTS arc.ext_type_normage 
( 
  id text NOT NULL, 
  ordre integer, 
  CONSTRAINT ext_type_normage_pkey PRIMARY KEY (id) 
); 
INSERT INTO arc.ext_type_normage values ('relation','1'),('cartesian','2'),('suppression','3'),('unicité','4') ON CONFLICT DO NOTHING;
INSERT INTO arc.ext_type_normage values ('reduction','5') ON CONFLICT DO NOTHING;
INSERT INTO arc.ext_type_normage values ('partition','5') ON CONFLICT DO NOTHING;

do $$ begin CREATE TRIGGER tg_insert_normage BEFORE INSERT ON arc.ihm_normage_regle FOR EACH ROW EXECUTE PROCEDURE arc.insert_controle(); exception when others then end; $$;

