CREATE TABLE IF NOT EXISTS arc.ext_type_fichier_chargement 
( 
  id text NOT NULL, 
  ordre integer, 
  CONSTRAINT ext_type_fichier_chargement_pkey PRIMARY KEY (id) 
); 
INSERT INTO arc.ext_type_fichier_chargement values ('xml','1'),('clef-valeur','2'),('plat','3') ON CONFLICT DO NOTHING;
INSERT INTO arc.ext_type_fichier_chargement values ('xml-complexe','4') ON CONFLICT DO NOTHING;


CREATE TABLE IF NOT EXISTS arc.ihm_chargement_regle 
( 
id_regle bigint NOT NULL, 
id_norme text NOT NULL, 
validite_inf date NOT NULL, 
validite_sup date NOT NULL, 
version text NOT NULL, 
periodicite text NOT NULL, 
type_fichier text NOT NULL, 
delimiter text, 
format text, 
commentaire text, 
CONSTRAINT pk_ihm_chargement_regle PRIMARY KEY (id_regle, id_norme, validite_inf, validite_sup, version, periodicite), 
CONSTRAINT ihm_chargement_regle_jeuderegle_fkey FOREIGN KEY (id_norme, periodicite, validite_inf, validite_sup, version) REFERENCES arc.ihm_jeuderegle (id_norme, periodicite, validite_inf, validite_sup, version) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE
);

alter table arc.ihm_chargement_regle alter column type_fichier set NOT NULL;

do $$ begin alter table arc.ihm_chargement_regle add CONSTRAINT ihm_chargement_regle_type_fichier_fkey FOREIGN KEY (type_fichier) REFERENCES arc.ext_type_fichier_chargement(id) ON DELETE CASCADE ON UPDATE cascade ; EXCEPTION WHEN OTHERS then end; $$;
do $$ begin CREATE TRIGGER tg_insert_chargement BEFORE INSERT ON arc.ihm_chargement_regle FOR EACH ROW EXECUTE PROCEDURE arc.insert_controle(); exception when others then end; $$;
