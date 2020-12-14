CREATE TABLE IF NOT EXISTS arc.ihm_mapping_regle 
( 
id_regle bigint, 
id_norme text NOT NULL, 
validite_inf date NOT NULL, 
validite_sup date NOT NULL, 
version text NOT NULL, 
periodicite text NOT NULL, 
variable_sortie character varying(63) NOT NULL, 
expr_regle_col text, 
commentaire text, 
CONSTRAINT pk_ihm_mapping_regle PRIMARY KEY (id_norme, periodicite, validite_inf, validite_sup, version, variable_sortie), 
CONSTRAINT fk_ihm_mapping_regle_jeuderegle FOREIGN KEY (id_norme, periodicite, validite_inf, validite_sup, version) 
REFERENCES arc.ihm_jeuderegle (id_norme, periodicite, validite_inf, validite_sup, version) MATCH SIMPLE 
ON UPDATE CASCADE ON DELETE CASCADE 
); 
        
do $$ begin CREATE TRIGGER tg_insert_mapping BEFORE INSERT ON arc.ihm_mapping_regle FOR EACH ROW EXECUTE PROCEDURE arc.insert_controle(); exception when others then end; $$;
