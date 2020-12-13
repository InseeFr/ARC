CREATE TABLE IF NOT EXISTS arc.ihm_filtrage_regle 
( 
  id_regle bigint NOT NULL, 
  id_norme text NOT NULL, 
  validite_inf date NOT NULL, 
  validite_sup date NOT NULL, 
  version text NOT NULL, 
  periodicite text NOT NULL, 
  expr_regle_filtre text, 
  commentaire text, 
  CONSTRAINT pk_ihm_mapping_filtrage_regle PRIMARY KEY (id_regle, id_norme, validite_inf, validite_sup, version, periodicite), 
CONSTRAINT fk_ihm_mapping_filtrage_regle_jeuderegle FOREIGN KEY (id_norme, periodicite, validite_inf, validite_sup, version) 
REFERENCES arc.ihm_jeuderegle (id_norme, periodicite, validite_inf, validite_sup, version) MATCH SIMPLE 
ON UPDATE CASCADE ON DELETE CASCADE 
);

do $$ begin CREATE TRIGGER tg_insert_filtrage BEFORE INSERT ON arc.ihm_filtrage_regle FOR EACH ROW EXECUTE PROCEDURE arc.insert_controle(); exception when others then end; $$;

