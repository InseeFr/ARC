CREATE TABLE arc.ihm_paramettrage_ordre_phase (
id_norme text COLLATE pg_catalog."C" NOT NULL,
  validite_inf date NOT NULL,
  validite_sup date NOT NULL,
  version text COLLATE pg_catalog."C" NOT NULL,
  periodicite text COLLATE pg_catalog."C" NOT NULL,
  nom_phase text COLLATE pg_catalog."C",
  type_phase text COLLATE pg_catalog."C",
  ordre integer,
  nb_ligne_traitee integer,
  CONSTRAINT pk_ihm_paramettrage_ordre_phase PRIMARY KEY (nom_phase, id_norme, validite_inf, validite_sup, version, periodicite)
)

insert into arc.ihm_paramettrage_ordre_phase (id_norme,
  validite_inf ,
  validite_sup ,
  version ,
  periodicite ,
  nom_phase ,
  type_phase ,
  ordre ,
  nb_ligne_traitee)

  values (
'PHAS3v1_v2', '2014-01-01'::date,'2020-12-31'::date,'v001','M','DUMMY', 'DUMMY', 0,0)
, (
'PHAS3v1_v2', '2014-01-01'::date,'2020-12-31'::date,'v001','M','INITIALISATION', 'INITIALISATION', 1,1000000)
, (
'PHAS3v1_v2', '2014-01-01'::date,'2020-12-31'::date,'v001','M','RECEPTION', 'RECEPTION', 2,1000000)
, (
'PHAS3v1_v2', '2014-01-01'::date,'2020-12-31'::date,'v001','M','IDENTIFICATION', 'IDENTIFICATION', 3,1000000)
, (
'PHAS3v1_v2', '2014-01-01'::date,'2020-12-31'::date,'v001','M','CHARGEMENT', 'CHARGEMENT', 4,1000000)
, (
'PHAS3v1_v2', '2014-01-01'::date,'2020-12-31'::date,'v001','M','NORMAGE', 'NORMAGE', 5,1000000)
, (
'PHAS3v1_v2', '2014-01-01'::date,'2020-12-31'::date,'v001','M','CONTROLE', 'CONTROLE', 6,1000000)
, (
'PHAS3v1_v2', '2014-01-01'::date,'2020-12-31'::date,'v001','M','FILTRAGE', 'FILTRAGE', 7,1000000)
, (
'PHAS3v1_v2', '2014-01-01'::date,'2020-12-31'::date,'v001','M','MAPPING', 'MAPPING', 8,1000000)