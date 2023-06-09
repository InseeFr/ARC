--
-- TOC entry 7325 (class 0 OID 16976)
-- Dependencies: 278
-- Data for Name: ext_etat_jeuderegle; Type: TABLE DATA; Schema: arc; Owner: -
--


update arc.ext_etat_jeuderegle set env_description = 'BAS associé à vConformité (version des règles en prod)' where id='arc_bas1';
update arc.ext_etat_jeuderegle set env_description = 'BAS associé à vBeta (version des règles de la Beta)' where id='arc_bas2';

--
-- TOC entry 7331 (class 0 OID 17025)
-- Dependencies: 284
-- Data for Name: ihm_famille; Type: TABLE DATA; Schema: arc; Owner: -
--

INSERT INTO arc.ihm_famille VALUES ('SIRENE4');

--
-- TOC entry 7334 (class 0 OID 17048)
-- Dependencies: 287
-- Data for Name: ihm_norme; Type: TABLE DATA; Schema: arc; Owner: -
--

INSERT INTO arc.ihm_norme VALUES ('v2008-11', 'A', 'select 1 from alias_table where  id_source like ''%''  and substring(ligne from ''<VersionNorme>(.*)</VersionNorme>'')=''V2008.11''', 'select ''2020-01-01''', 3, '1', 'SIRENE4');
INSERT INTO arc.ihm_norme VALUES ('v2016-02', 'A', 'select 1 from alias_table where  id_source like ''%'' and substring(ligne from ''<VersionNorme>(.*)</VersionNorme>'')=''V2016.02''', 'select ''2020-01-01''', 1, '1', 'SIRENE4');


--
-- TOC entry 7336 (class 0 OID 17064)
-- Dependencies: 289
-- Data for Name: ihm_calendrier; Type: TABLE DATA; Schema: arc; Owner: -
--

INSERT INTO arc.ihm_calendrier VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 1, '1');
INSERT INTO arc.ihm_calendrier VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 2, '1');


--
-- TOC entry 7337 (class 0 OID 17078)
-- Dependencies: 290
-- Data for Name: ihm_jeuderegle; Type: TABLE DATA; Schema: arc; Owner: -
--

INSERT INTO arc.ihm_jeuderegle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'arc.bas1', NULL, NULL);
INSERT INTO arc.ihm_jeuderegle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'arc.bas1', NULL, NULL);
INSERT INTO arc.ihm_jeuderegle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vTestMapping', 'arc.bas5', NULL, NULL);
INSERT INTO arc.ihm_jeuderegle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vBeta', 'arc.bas2', NULL, NULL);
INSERT INTO arc.ihm_jeuderegle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vNno', 'arc.bas4', NULL, NULL);
INSERT INTO arc.ihm_jeuderegle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vTestsMarie', 'arc.bas2', NULL, NULL);




--
-- TOC entry 314 (class 1259 OID 547752)
-- Name: nmcl_cj_v001; Type: TABLE; Schema: arc; Owner: -
--

CREATE TABLE arc.nmcl_cj_v001 (
    code text,
    "libellé" text
);


--
-- TOC entry 1184 (class 1259 OID 326399714)
-- Name: nmcl_cog_v004; Type: TABLE; Schema: arc; Owner: -
--

CREATE TABLE arc.nmcl_cog_v004 (
    code text,
    libelle text
);


--
-- TOC entry 378 (class 1259 OID 12024215)
-- Name: nmcl_evenements_v001; Type: TABLE; Schema: arc; Owner: -
--

CREATE TABLE arc.nmcl_evenements_v001 (
    code text,
    "libellé" text
);


--
-- TOC entry 379 (class 1259 OID 12235585)
-- Name: nmcl_evenements_v002; Type: TABLE; Schema: arc; Owner: -
--

CREATE TABLE arc.nmcl_evenements_v002 (
    code text,
    "libellé" text
);


--
-- TOC entry 315 (class 1259 OID 740269)
-- Name: nmcl_rubriqueentete_v001; Type: TABLE; Schema: arc; Owner: -
--

CREATE TABLE arc.nmcl_rubriqueentete_v001 (
    rubrique text
);


--
-- TOC entry 313 (class 1259 OID 547706)
-- Name: nmcl_supportdec_v001; Type: TABLE; Schema: arc; Owner: -
--

CREATE TABLE arc.nmcl_supportdec_v001 (
    code text,
    "libellé" text
);

--
-- TOC entry 7352 (class 0 OID 247629)
-- Dependencies: 310
-- Data for Name: ext_etat; Type: TABLE DATA; Schema: arc; Owner: -
--

update arc.ext_etat set val = 'INACTIF' where id = '0';
update arc.ext_etat set val = 'ACTIF' where id = '1';



--
-- TOC entry 7377 (class 0 OID 324408316)
-- Dependencies: 1134
-- Data for Name: ext_export_format; Type: TABLE DATA; Schema: arc; Owner: -
--

update arc.ext_export_format set val = 'NA' where id = '0';
update arc.ext_export_format set val = 'ZIP' where id = '1';
update arc.ext_export_format set val = 'GZ' where id = '2';


--
-- TOC entry 7326 (class 0 OID 16984)
-- Dependencies: 279
-- Data for Name: ext_mod_periodicite; Type: TABLE DATA; Schema: arc; Owner: -
--

update arc.ext_mod_periodicite set val = 'MENSUEL' where id = 'M';
update arc.ext_mod_periodicite set val = 'ANNUEL' where id = 'A';


--
-- TOC entry 7327 (class 0 OID 16992)
-- Dependencies: 280
-- Data for Name: ext_mod_type_autorise; Type: TABLE DATA; Schema: arc; Owner: -
--

update arc.ext_mod_type_autorise set description_type = 'Entier' where nom_type = 'bigint';
update arc.ext_mod_type_autorise set description_type = 'Tableau d''entier long' where nom_type = 'bigint[]';
update arc.ext_mod_type_autorise set description_type = 'Vrai (t ou true) ou faux (f ou false)' where nom_type = 'boolean';
update arc.ext_mod_type_autorise set description_type = 'Date' where nom_type = 'date';
update arc.ext_mod_type_autorise set description_type = 'Tableau de date' where nom_type = 'date[]';
update arc.ext_mod_type_autorise set description_type = 'Nombre décimal virgule flottante' where nom_type = 'float';
update arc.ext_mod_type_autorise set description_type = 'Tableau de nombre décimaux' where nom_type = 'float[]';
update arc.ext_mod_type_autorise set description_type = 'Durée (différence de deux dates)' where nom_type = 'interval';
update arc.ext_mod_type_autorise set description_type = 'Texte sans taille limite' where nom_type = 'text';
update arc.ext_mod_type_autorise set description_type = 'Tableau de texte sans limite' where nom_type = 'text[]';
update arc.ext_mod_type_autorise set description_type = 'Date et heure' where nom_type = 'timestamp without time zone';


--
-- TOC entry 7328 (class 0 OID 17000)
-- Dependencies: 281
-- Data for Name: ext_type_controle; Type: TABLE DATA; Schema: arc; Owner: -
--

update arc.ext_type_controle set ordre = 2 where id = 'ALPHANUM';
update arc.ext_type_controle set ordre = 1 where id = 'CARDINALITE';
update arc.ext_type_controle set ordre = 5 where id = 'CONDITION';
update arc.ext_type_controle set ordre = 4 where id = 'DATE';
update arc.ext_type_controle set ordre = 3 where id = 'NUM';
update arc.ext_type_controle set ordre = 6 where id = 'REGEXP';
update arc.ext_type_controle set ordre = 7 where id = 'ENUM_BRUTE';
update arc.ext_type_controle set ordre = 8 where id = 'ENUM_TABLE';
update arc.ext_type_controle set ordre = 8 where id = 'STRUCTURE';


--
-- TOC entry 7329 (class 0 OID 17008)
-- Dependencies: 282
-- Data for Name: ext_type_fichier_chargement; Type: TABLE DATA; Schema: arc; Owner: -
--

update arc.ext_type_fichier_chargement set ordre = 1 where id = 'xml';
update arc.ext_type_fichier_chargement set ordre = 2 where id = 'clef-valeur';
update arc.ext_type_fichier_chargement set ordre = 3 where id = 'plat';
update arc.ext_type_fichier_chargement set ordre = 4 where id = 'xml-complexe';



--
-- TOC entry 7330 (class 0 OID 17016)
-- Dependencies: 283
-- Data for Name: ext_type_normage; Type: TABLE DATA; Schema: arc; Owner: -
--

update arc.ext_type_normage set ordre = 1 where id = 'relation';
update arc.ext_type_normage set ordre = 2 where id = 'cartesian';
update arc.ext_type_normage set ordre = 3 where id = 'suppression';
update arc.ext_type_normage set ordre = 4 where id = 'unicité';
update arc.ext_type_normage set ordre = 5 where id = 'reduction';
update arc.ext_type_normage set ordre = 5 where id = 'partition';
update arc.ext_type_normage set ordre = 7 where id = 'exclusion';
update arc.ext_type_normage set ordre = 8 where id = 'independance';


--
-- TOC entry 7363 (class 0 OID 12416798)
-- Dependencies: 431
-- Data for Name: ext_webservice_queryview; Type: TABLE DATA; Schema: arc; Owner: -
--

update arc.ext_webservice_queryview set val = 'COLUMN' where id = '1';
update arc.ext_webservice_queryview set val = 'LINE' where id = '2';


--
-- TOC entry 7362 (class 0 OID 12416777)
-- Dependencies: 430
-- Data for Name: ext_webservice_type; Type: TABLE DATA; Schema: arc; Owner: -
--

update arc.ext_webservice_type set val = 'ENGINE' where id = '1';
update arc.ext_webservice_type set val = 'SERVICE' where id = '2';


--
-- TOC entry 7338 (class 0 OID 17091)
-- Dependencies: 291
-- Data for Name: ihm_chargement_regle; Type: TABLE DATA; Schema: arc; Owner: -
--

INSERT INTO arc.ihm_chargement_regle VALUES (12, 'v2008-11', '2020-01-01', '2100-01-01', 'vConformite', 'A', 'xml-complexe', NULL, 'GMC,DIU
GMC,IPD
GMC,IDU
GMC,NPD
GDR,DIU
GDR,ICR
GDR,IDU
PersonnePhysiqueDirigeante,DIU
PersonnePhysiqueDirigeante,IDU
PersonnePhysiqueDirigeante,IPD
PersonnePhysiqueDirigeante,NPD
PersonneMoraleDirigeante,DIU
PersonneMoraleDirigeante,ICR
PersonneMoraleDirigeante, IDU
Etablissement,IAE
Etablissement,DEE
Etablissement,REE
EtablissementFerme,IAE
EtablissementFerme,DEE
EtablissementFerme,REE', NULL);
INSERT INTO arc.ihm_chargement_regle VALUES (11, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'xml-complexe', NULL, 'GMC,DIU
GMC,IPD
GMC,IDU
GMC,NPD
GDR,DIU
GDR,ICR
GDR,IDU
PersonnePhysiqueDirigeante,DIU
PersonnePhysiqueDirigeante,IDU
PersonnePhysiqueDirigeante,IPD
PersonnePhysiqueDirigeante,NPD
PersonneMoraleDirigeante,DIU
PersonneMoraleDirigeante,ICR
PersonneMoraleDirigeante, IDU
Etablissement,IAE
Etablissement,DEE
Etablissement,REE
EtablissementFerme,IAE
EtablissementFerme,DEE
EtablissementFerme,REE', NULL);
INSERT INTO arc.ihm_chargement_regle VALUES (12, 'v2008-11', '2020-01-01', '2100-01-01', 'vBeta', 'A', 'xml-complexe', NULL, 'GMC,DIU
GMC,IPD
GMC,IDU
GMC,NPD
GDR,DIU
GDR,ICR
GDR,IDU
PersonnePhysiqueDirigeante,DIU
PersonnePhysiqueDirigeante,IDU
PersonnePhysiqueDirigeante,IPD
PersonnePhysiqueDirigeante,NPD
PersonneMoraleDirigeante,DIU
PersonneMoraleDirigeante,ICR
PersonneMoraleDirigeante, IDU
Etablissement,IAE
Etablissement,DEE
Etablissement,REE
EtablissementFerme,IAE
EtablissementFerme,DEE
EtablissementFerme,REE', NULL);
INSERT INTO arc.ihm_chargement_regle VALUES (1, 'v2016-02', '2020-01-01', '2100-01-01', 'vNno', 'A', 'xml-complexe', NULL, 'GMC,DIU
GMC,IPD
GMC,IDU
GMC,NPD
GDR,DIU
GDR,ICR
GDR,IDU
PersonnePhysiqueDirigeante,DIU
PersonnePhysiqueDirigeante,IDU
PersonnePhysiqueDirigeante,IPD
PersonnePhysiqueDirigeante,NPD
PersonneMoraleDirigeante,DIU
PersonneMoraleDirigeante,ICR
PersonneMoraleDirigeante, IDU
Etablissement,IAE
Etablissement,DEE
Etablissement,REE
EtablissementFerme,IAE
EtablissementFerme,DEE
EtablissementFerme,REE', NULL);
INSERT INTO arc.ihm_chargement_regle VALUES (13, 'v2016-02', '2020-01-01', '2100-01-01', 'vTestsMarie', 'A', 'xml-complexe', NULL, 'GMC,DIU
GMC,IPD
GMC,IDU
GMC,NPD
GDR,DIU
GDR,ICR
GDR,IDU
PersonnePhysiqueDirigeante,DIU
PersonnePhysiqueDirigeante,IDU
PersonnePhysiqueDirigeante,IPD
PersonnePhysiqueDirigeante,NPD
PersonneMoraleDirigeante,DIU
PersonneMoraleDirigeante,ICR
PersonneMoraleDirigeante, IDU
Etablissement,IAE
Etablissement,DEE
Etablissement,REE
EtablissementFerme,IAE
EtablissementFerme,DEE
EtablissementFerme,REE', NULL);
INSERT INTO arc.ihm_chargement_regle VALUES (13, 'v2016-02', '2020-01-01', '2100-01-01', 'vConformite', 'A', 'xml-complexe', NULL, 'GMC,DIU
GMC,IPD
GMC,IDU
GMC,NPD
GDR,DIU
GDR,ICR
GDR,IDU
PersonnePhysiqueDirigeante,DIU
PersonnePhysiqueDirigeante,IDU
PersonnePhysiqueDirigeante,IPD
PersonnePhysiqueDirigeante,NPD
PersonneMoraleDirigeante,DIU
PersonneMoraleDirigeante,ICR
PersonneMoraleDirigeante, IDU
Etablissement,IAE
Etablissement,DEE
Etablissement,REE
EtablissementFerme,IAE
EtablissementFerme,DEE
EtablissementFerme,REE', NULL);


--
-- TOC entry 7332 (class 0 OID 17033)
-- Dependencies: 285
-- Data for Name: ihm_client; Type: TABLE DATA; Schema: arc; Owner: -
--



--
-- TOC entry 7340 (class 0 OID 17117)
-- Dependencies: 293
-- Data for Name: ihm_controle_regle; Type: TABLE DATA; Schema: arc; Owner: -
--

INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'ENUM_TABLE', 'v_e02_3', NULL, NULL, NULL, 'select code from arc.nmcl_cog_v004', NULL, 29, NULL, 'Le code géographique de l''établissement est invalide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vNno', 'ENUM_TABLE', 'v_e02_3', NULL, NULL, NULL, 'select code from arc.nmcl_cog_v004', NULL, 29, NULL, 'Le code géographique de l''établissement est invalide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vNno', 'ENUM_TABLE', 'v_e12_3_etablissement', NULL, NULL, NULL, 'select code from arc.nmcl_cog_v004', NULL, 35, NULL, 'Le code géographique de l''ancien établissement est invalide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vNno', 'CONDITION', NULL, NULL, NULL, NULL, 'case when length({v_c02})=12
then (
select ((ceil(cle::float/10)*10)::int-cle) = substr({v_c02},12,1)::int
from (
select sum(case when (12-i)%2=0 then case when substr(t,i,1)::int<5 then substr(t,i,1)::int*2 else (substr(t,i,1)::int-5)*2+1 end else substr(t,i,1)::int end) as cle
from (
select i,
case substr({v_c02},1,1)
when ''J'' then ''0''
when ''M'' then ''2''
when ''C'' then ''6''
when ''G'' then ''1''
when ''U'' then ''5''
when ''I'' then ''3''
when ''X'' then ''3''
when ''D'' then ''9''
when ''B'' then ''2''
when ''P'' then ''0''
when ''Q'' then ''8''
when ''R'' then ''4''
when ''A'' then ''6''
when ''S'' then ''7''
when ''V'' then ''1''
when ''Y'' then ''4''
when ''Z'' then ''5''
when ''E'' then ''3''
when ''K'' then ''0''
when ''L'' then ''9''
when ''N'' then ''5''
when ''O'' then ''2''
when ''W'' then ''2''
end
||
substr({v_c02},2,1)
||
case substr({v_c02},3,1)
when ''A'' then ''10''
when ''B'' then ''11'' 
else ''0''||substr({v_c02},3,1)
end
||
substr({v_c02},4,8)
as t
from generate_series(1,12) i
) u
) v
)
else false
end', NULL, 1, NULL, 'Le numéro de liasse est invalide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vNno', 'ENUM_TABLE', 'v_c10_1', NULL, NULL, NULL, 'select code from arc.nmcl_evenements_v002', NULL, 2, NULL, 'Le code évènement est invalide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_u11_8', 'v_u11_8', '1', '1', NULL, NULL, 18, NULL, 'Le code postal du siège est vide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_entreprise', 'i_etablissement', '1', '1', 'substr({v_c10_1},1,1)=''0'' and {v_c10_1}!=''02M''', NULL, 21, NULL, 'Le bloc Etablissement est obligatoire pour une création', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_etablissement', 'i_ice', '1', '1', 'substr({v_c10_1},1,1)=''0'' and {v_c10_1}!=''02M''', NULL, 22, NULL, 'Le bloc ICE est obligatoire pour une création', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_entreprise', 'i_etablissement', '1', '1', 'substr({v_c10_1},1,2)=''11''', NULL, 23, NULL, 'Le bloc Etablissement est obligatoire pour un transfert d''entreprise', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_etablissement', 'i_ice', '1', '1', 'substr({v_c10_1},1,2)=''11''', NULL, 24, NULL, 'Le bloc ICE est obligatoire pour un transfert d''entreprise', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_entreprise', 'i_etablissement', '1', '1', 'substr({v_c10_1},1,2)::int>=50 and substr({v_c10_1},1,2)::int<90', NULL, 25, NULL, 'Le bloc Etablissement est obligatoire pour un évènement de niveau établissement', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_etablissement', 'i_ice', '1', '1', 'substr({v_c10_1},1,2)::int>=50 and substr({v_c10_1},1,2)::int<80', NULL, 26, NULL, 'Le bloc ICE est obligatoire pour une modification d''établissement', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_entreprise', 'i_etablissement', '1', '1', 'substr({v_c10_1},1,2)::int>=91 and substr({v_c10_1},1,2)::int<=94', NULL, 27, NULL, 'Le bloc Etablissement est obligatoire pour un rejet ou une radiation', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_etablissement', 'i_ice', '1', '1', 'substr({v_c10_1},1,2)::int>=91 and substr({v_c10_1},1,2)::int<=94', NULL, 28, NULL, 'Le bloc ICE est obligatoire pour un rejet ou une radiation', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vNno', 'CARDINALITE', 'i_edf', 'i_c10', '1', '1', 'bool_or({v_c10_1} in (''01P'',''07P'',''01M'',''02M'',''03M'',''04M'',''05M'',''07M'',''01F'')) over (partition by {i_edf})', NULL, 3, NULL, 'Un évènement de création doit être déclaré seul', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_e02_8', 'v_e02_8', '1', '1', NULL, NULL, 30, NULL, 'Le code postal de l''établissement est vide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_etablissement', 'i_iae_etablissement', '1', '1', 'substr({v_c10_1},1,2) in (''11'',''56'')', NULL, 33, NULL, 'Le bloc IAE est obligatoire pour un transfert', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_etablissement', 'i_iae_etablissement', '1', '1', 'substr({v_c10_1},1,2)::int>=80 and substr({v_c10_1},1,2)::int<90', NULL, 34, NULL, 'Le bloc IAE est obligatoire pour une fermeture d''établissement', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vNno', 'CARDINALITE', 'i_edf', 'i_c10', '1', '1', 'bool_or({v_c10_1} in (''41P'',''42M'',''24F'',''27F'',''40F'')) over (partition by {i_edf})', NULL, 4, NULL, 'Un évènement de cessation doit être déclaré seul', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vNno', 'CARDINALITE', 'i_edf', 'i_c10', '1', '1', 'bool_or({v_c10_1} in (''93P'',''93M'',''94P'',''94M'',''96P'',''96M'')) over (partition by {i_edf})', NULL, 5, NULL, 'Un évènement de radiation doit être déclaré seul', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vNno', 'CARDINALITE', 'i_liasse', 'i_personnephysique', '1', '1', 'substr({v_c10_1},3,1)=''P''', NULL, 6, NULL, 'Un évènement n''est pas cohérent avec le bloc PersonnePhysique', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vNno', 'CARDINALITE', 'i_liasse', 'i_personnemorale', '1', '1', 'substr({v_c10_1},3,1)=''M''', NULL, 7, NULL, 'Un évènement n''est pas cohérent avec le bloc PersonneMorale', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vNno', 'CARDINALITE', 'i_liasse', 'i_exploitationencommun', '1', '1', 'substr({v_c10_1},3,1)=''F''', NULL, 8, NULL, 'Un évènement n''est pas cohérent avec le bloc ExploitationEnCommun', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vNno', 'CARDINALITE', 'i_p01_2', 'v_p01_2', '1', '1', NULL, NULL, 9, NULL, 'Le nom est vide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vNno', 'CARDINALITE', 'i_p01_3', 'v_p01_3', '1', '1', '{i_p01_3}::int=1', NULL, 10, NULL, 'Le premier prénom est vide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vNno', 'REGEXP', 'v_p01_2', NULL, NULL, NULL, '^[a-zàâäçéèêëîïôöùûüÿæœA-ZÀÂÄÇÉÈÊËÎÏÔÖÙÛÜŸÆŒ \-'''']*$', NULL, 11, NULL, 'Le nom contient un caractère non autorisé', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vNno', 'REGEXP', 'v_p01_3', NULL, NULL, NULL, '^[a-zàâäçéèêëîïôöùûüÿæœA-ZÀÂÄÇÉÈÊËÎÏÔÖÙÛÜŸÆŒ \-'''']*$', NULL, 12, NULL, 'Un prénom contient un caractère non autorisé', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vNno', 'CONDITION', NULL, NULL, NULL, NULL, '{v_m01_1} is null or {v_m01_1}::text!=''.''', NULL, 13, NULL, 'La dénomination de la personne morale est non renseignée', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vNno', 'CONDITION', NULL, NULL, NULL, NULL, '{v_f01} is null or {v_f01}::text!=''.''', NULL, 14, NULL, 'La dénomination de l''exploitation en commun est non renseignée', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vNno', 'CARDINALITE', 'i_entreprise', 'i_siu', '1', '1', 'substr({v_c10_1},1,1)=''0''', NULL, 15, NULL, 'Le bloc SIU est obligtaoire pour une création', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vNno', 'CARDINALITE', 'i_siu', 'i_u11', '1', '1', 'substr({v_c10_1},1,1)=''0''', NULL, 16, NULL, 'L''adresse du siège est obligatoire pour une création', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vNno', 'CARDINALITE', 'i_u11_8', 'v_u11_8', '1', '1', NULL, NULL, 18, NULL, 'Le code postal du siège est vide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CONDITION', NULL, NULL, NULL, NULL, 'case when length({v_c02})=12
then (
select ((ceil(cle::float/10)*10)::int-cle) = substr({v_c02},12,1)::int
from (
select sum(case when (12-i)%2=0 then case when substr(t,i,1)::int<5 then substr(t,i,1)::int*2 else (substr(t,i,1)::int-5)*2+1 end else substr(t,i,1)::int end) as cle
from (
select i,
case substr({v_c02},1,1)
when ''J'' then ''0''
when ''M'' then ''2''
when ''C'' then ''6''
when ''G'' then ''1''
when ''U'' then ''5''
when ''I'' then ''3''
when ''X'' then ''3''
when ''D'' then ''9''
when ''B'' then ''2''
when ''P'' then ''0''
when ''Q'' then ''8''
when ''R'' then ''4''
when ''A'' then ''6''
when ''S'' then ''7''
when ''V'' then ''1''
when ''Y'' then ''4''
when ''Z'' then ''5''
when ''E'' then ''3''
when ''K'' then ''0''
when ''L'' then ''9''
when ''N'' then ''5''
when ''O'' then ''2''
when ''W'' then ''2''
end
||
substr({v_c02},2,1)
||
case substr({v_c02},3,1)
when ''A'' then ''10''
when ''B'' then ''11'' 
else ''0''||substr({v_c02},3,1)
end
||
substr({v_c02},4,8)
as t
from generate_series(1,12) i
) u
) v
)
else false
end', NULL, 1, NULL, 'Le numéro de liasse est invalide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vNno', 'CONDITION', NULL, NULL, NULL, NULL, '{v_u11_8} is null or {v_u11_8}::text!=''.''', NULL, 19, NULL, 'Le code postal du siège est non renseigné', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vNno', 'CONDITION', NULL, NULL, NULL, NULL, '{v_u11_13} is null or {v_u11_13}::text!=''.''', NULL, 20, NULL, 'Le libellé de commune du siège est non renseigné', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vNno', 'CARDINALITE', 'i_entreprise', 'i_etablissement', '1', '1', 'substr({v_c10_1},1,1)=''0'' and {v_c10_1}!=''02M''', NULL, 21, NULL, 'Le bloc Etablissement est obligatoire pour une création', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vNno', 'CARDINALITE', 'i_etablissement', 'i_ice', '1', '1', 'substr({v_c10_1},1,1)=''0'' and {v_c10_1}!=''02M''', NULL, 22, NULL, 'Le bloc ICE est obligatoire pour une création', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vNno', 'CARDINALITE', 'i_entreprise', 'i_etablissement', '1', '1', 'substr({v_c10_1},1,2)=''11''', NULL, 23, NULL, 'Le bloc Etablissement est obligatoire pour un transfert d''entreprise', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vNno', 'CARDINALITE', 'i_etablissement', 'i_ice', '1', '1', 'substr({v_c10_1},1,2)=''11''', NULL, 24, NULL, 'Le bloc ICE est obligatoire pour un transfert d''entreprise', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vNno', 'CARDINALITE', 'i_entreprise', 'i_etablissement', '1', '1', 'substr({v_c10_1},1,2)::int>=50 and substr({v_c10_1},1,2)::int<90', NULL, 25, NULL, 'Le bloc Etablissement est obligatoire pour un évènement de niveau établissement', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vNno', 'CARDINALITE', 'i_etablissement', 'i_ice', '1', '1', 'substr({v_c10_1},1,2)::int>=50 and substr({v_c10_1},1,2)::int<80', NULL, 26, NULL, 'Le bloc ICE est obligatoire pour une modification d''établissement', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vNno', 'CARDINALITE', 'i_entreprise', 'i_etablissement', '1', '1', 'substr({v_c10_1},1,2)::int>=91 and substr({v_c10_1},1,2)::int<=94', NULL, 27, NULL, 'Le bloc Etablissement est obligatoire pour un rejet ou une radiation', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vNno', 'CARDINALITE', 'i_etablissement', 'i_ice', '1', '1', 'substr({v_c10_1},1,2)::int>=91 and substr({v_c10_1},1,2)::int<=94', NULL, 28, NULL, 'Le bloc ICE est obligatoire pour un rejet ou une radiation', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vNno', 'CARDINALITE', 'i_e02_8', 'v_e02_8', '1', '1', NULL, NULL, 30, NULL, 'Le code postal de l''établissement est vide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vNno', 'CONDITION', NULL, NULL, NULL, NULL, '{v_e02_8} is null or {v_e02_8}::text!=''.''', NULL, 31, NULL, 'Le code postal de l''établissement est non renseigné', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vNno', 'CONDITION', NULL, NULL, NULL, NULL, '{v_e02_13} is null or {v_e02_13}::text!=''.''', NULL, 32, NULL, 'Le libellé de commune de l''établissement est non renseigné', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vNno', 'CARDINALITE', 'i_etablissement', 'i_iae_etablissement', '1', '1', 'substr({v_c10_1},1,2) in (''11'',''56'')', NULL, 33, NULL, 'Le bloc IAE est obligatoire pour un transfert', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vNno', 'CARDINALITE', 'i_etablissement', 'i_iae_etablissement', '1', '1', 'substr({v_c10_1},1,2)::int>=80 and substr({v_c10_1},1,2)::int<90', NULL, 34, NULL, 'Le bloc IAE est obligatoire pour une fermeture d''établissement', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vNno', 'CARDINALITE', 'i_e12_8_etablissement', 'v_e12_8_etablissement', '1', '1', NULL, NULL, 36, NULL, 'Le code postal de l''ancien établissement est vide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vNno', 'CONDITION', NULL, NULL, NULL, NULL, '{v_e12_8_etablissement} is null or {v_e12_8_etablissement}::text!=''.''', NULL, 37, NULL, 'Le code postal de l''ancien établissement est non renseigné', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vNno', 'CONDITION', NULL, NULL, NULL, NULL, '{v_e12_13_etablissement} is null or {v_e12_13_etablissement}::text!=''.''', NULL, 38, NULL, 'Le libellé de commune de l''ancien établissement est non renseigné', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vNno', 'CONDITION', NULL, NULL, NULL, NULL, '{v_p03_1} is null or {v_p03_1}::text!=''.''', NULL, 39, NULL, 'La date de naissance est non renseignée', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vNno', 'CONDITION', NULL, NULL, NULL, NULL, '{v_p03_2} is null or {v_p03_2}::text!=''.''', NULL, 40, NULL, 'Le code géographique du lieu de naissance est non renseigné', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vNno', 'CARDINALITE', 'i_entreprise', 'i_ipu', '1', '1', 'substr({v_c10_1},1,1)!=''0''', NULL, 41, NULL, 'Le bloc IPU est obligatoire pour les évènements de modification', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vNno', 'CARDINALITE', 'i_personnephysique', 'i_aip', '1', '1', '{v_c10_1}=''05P''', NULL, 42, NULL, 'Le bloc AIP est obligatoire pour une réactivation d''une entreprise individuelle', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vNno', 'CARDINALITE', 'i_aip', 'i_p12', '1', '1', '{v_c10_1}=''05P''', NULL, 43, NULL, 'L''ancien SIREN est obligatoire pour une réactivation d''une entreprise individuelle', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vNno', 'CONDITION', NULL, NULL, NULL, NULL, 'case when length({v_u02})=9
then (
select (cle::int%10) = 0
from (
select sum(case when (9-i)%2=1 then case when substr(t,i,1)::int<5 then substr(t,i,1)::int*2 else (substr(t,i,1)::int-5)*2+1 end else substr(t,i,1)::int end) as cle
from (
select i,
{v_u02} as t 
from generate_series(1,9) i
) u
) v
)
else false
end', NULL, 44, NULL, 'Le SIREN est invalide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vNno', 'CONDITION', NULL, NULL, NULL, NULL, 'case when length({v_p12})=9
then (
select (cle::int%10) = 0
from (
select sum(case when (9-i)%2=1 then case when substr(t,i,1)::int<5 then substr(t,i,1)::int*2 else (substr(t,i,1)::int-5)*2+1 end else substr(t,i,1)::int end) as cle
from (
select i,
{v_p12} as t 
from generate_series(1,9) i
) u
) v
)
else false
end', NULL, 45, NULL, 'L''ancien SIREN est invalide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CONDITION', NULL, NULL, NULL, NULL, '{v_u11_8}::text!=''.'' and trim({v_u11_8})!=''''', NULL, 19, NULL, 'Le code postal du siège est non renseigné', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vNno', 'ENUM_TABLE', 'v_u11_3', NULL, NULL, NULL, 'select code from arc.nmcl_cog_v004', NULL, 17, NULL, 'Le code géographique du siège est invalide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CONDITION', NULL, NULL, NULL, NULL, 'case when length({v_c02})=12
then (
select ((ceil(cle::float/10)*10)::int-cle) = substr({v_c02},12,1)::int
from (
select sum(case when (12-i)%2=0 then case when substr(t,i,1)::int<5 then substr(t,i,1)::int*2 else (substr(t,i,1)::int-5)*2+1 end else substr(t,i,1)::int end) as cle
from (
select i,
case substr({v_c02},1,1)
when ''J'' then ''0''
when ''M'' then ''2''
when ''C'' then ''6''
when ''G'' then ''1''
when ''U'' then ''5''
when ''I'' then ''3''
when ''X'' then ''3''
when ''D'' then ''9''
when ''B'' then ''2''
when ''P'' then ''0''
when ''Q'' then ''8''
when ''R'' then ''4''
when ''A'' then ''6''
when ''S'' then ''7''
when ''V'' then ''1''
when ''Y'' then ''4''
when ''Z'' then ''5''
when ''E'' then ''3''
when ''K'' then ''0''
when ''L'' then ''9''
when ''N'' then ''5''
when ''O'' then ''2''
when ''W'' then ''2''
end
||
substr({v_c02},2,1)
||
case substr({v_c02},3,1)
when ''A'' then ''10''
when ''B'' then ''11'' 
else ''0''||substr({v_c02},3,1)
end
||
substr({v_c02},4,8)
as t
from generate_series(1,12) i
) u
) v
)
else false
end', NULL, 1, NULL, 'Le numéro de liasse est invalide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CONDITION', NULL, NULL, NULL, NULL, '{v_e02_8}::text!=''.'' and trim({v_e02_8})!=''''', NULL, 31, NULL, 'Le code postal de l''établissement est non renseigné', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CONDITION', NULL, NULL, NULL, NULL, '{v_e02_13}::text!=''.'' and trim({v_e02_13})!=''''', NULL, 32, NULL, 'Le libellé de commune de l''établissement est non renseigné', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vTestsMarie', 'CONDITION', NULL, NULL, NULL, NULL, 'case when length({v_c02})=12
then (
select ((ceil(cle::float/10)*10)::int-cle) = substr({v_c02},12,1)::int
from (
select sum(case when (12-i)%2=0 then case when substr(t,i,1)::int<5 then substr(t,i,1)::int*2 else (substr(t,i,1)::int-5)*2+1 end else substr(t,i,1)::int end) as cle
from (
select i,
case substr({v_c02},1,1)
when ''J'' then ''0''
when ''M'' then ''2''
when ''C'' then ''6''
when ''G'' then ''1''
when ''U'' then ''5''
when ''I'' then ''3''
when ''X'' then ''3''
when ''D'' then ''9''
when ''B'' then ''2''
when ''P'' then ''0''
when ''Q'' then ''8''
when ''R'' then ''4''
when ''A'' then ''6''
when ''S'' then ''7''
when ''V'' then ''1''
when ''Y'' then ''4''
when ''Z'' then ''5''
when ''E'' then ''3''
when ''K'' then ''0''
when ''L'' then ''9''
when ''N'' then ''5''
when ''O'' then ''2''
when ''W'' then ''2''
end
||
substr({v_c02},2,1)
||
case substr({v_c02},3,1)
when ''A'' then ''10''
when ''B'' then ''11'' 
else ''0''||substr({v_c02},3,1)
end
||
substr({v_c02},4,8)
as t
from generate_series(1,12) i
) u
) v
)
else false
end', NULL, 1, NULL, 'Le numéro de liasse est invalide et erroné !!!', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CONDITION', NULL, NULL, NULL, NULL, '{v_u11_13}::text!=''.'' and trim({v_u11_13})!=''''', NULL, 20, NULL, 'Le libellé de commune du siège est non renseigné', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vBeta', 'CONDITION', NULL, NULL, NULL, NULL, 'case when length({v_c02})=12 then ( select ((ceil(cle::float/10)*10)::int-cle) = substr({v_c02},12,1)::int from ( select sum(case when (12-i)%2=0 then case when substr(t,i,1)::int<5 then substr(t,i,1)::int*2 else (substr(t,i,1)::int-5)*2+1 end else substr(t,i,1)::int end) as cle from ( select i, case substr({v_c02},1,1) when ''J'' then ''0'' when ''M'' then ''2'' when ''C'' then ''6'' when ''G'' then ''1'' when ''U'' then ''5'' when ''I'' then ''3'' when ''X'' then ''3'' when ''D'' then ''9'' when ''B'' then ''2'' when ''P'' then ''0'' when ''Q'' then ''8'' when ''R'' then ''4'' when ''A'' then ''6'' when ''S'' then ''7'' when ''V'' then ''1'' when ''Y'' then ''4'' when ''Z'' then ''5'' when ''E'' then ''3'' when ''K'' then ''0'' when ''L'' then ''9'' when ''N'' then ''5'' when ''O'' then ''2'' when ''W'' then ''2'' end || substr({v_c02},2,1) || case substr({v_c02},3,1) when ''A'' then ''10'' when ''B'' then ''11''  else ''0''||substr({v_c02},3,1) end || substr({v_c02},4,8) as t from generate_series(1,12) i ) u ) v ) else false end', NULL, 1, NULL, 'Le numéro de liasse est invalide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vBeta', 'CARDINALITE', 'i_edf', 'i_c10', '1', '1', 'bool_or({v_c10_1} in (''93P'',''93M'',''94P'',''94M'')) over (partition by {i_edf})', NULL, 5, NULL, 'Un évènement de radiation doit être déclaré seul', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vBeta', 'CARDINALITE', 'i_liasse', 'i_personnephysique', '1', '1', 'substr({v_c10_1},3,1)=''P''', NULL, 6, NULL, 'Un évènement n''est pas cohérent avec le bloc PersonnePhysique', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vBeta', 'CARDINALITE', 'i_liasse', 'i_personnemorale', '1', '1', 'substr({v_c10_1},3,1)=''M''', NULL, 7, NULL, 'Un évènement n''est pas cohérent avec le bloc PersonneMorale', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vBeta', 'CARDINALITE', 'i_liasse', 'i_exploitationencommun', '1', '1', 'substr({v_c10_1},3,1)=''F''', NULL, 8, NULL, 'Un évènement n''est pas cohérent avec le bloc ExploitationEnCommun', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vBeta', 'ENUM_TABLE', 'v_c10_1', NULL, NULL, NULL, 'select code from arc.nmcl_evenements_v001', NULL, 2, NULL, 'Le code évènement est invalide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vBeta', 'CARDINALITE', 'i_edf', 'i_c10', '1', '1', 'bool_or({v_c10_1} in (''01P'',''07P'',''01M'',''02M'',''03M'',''04M'',''05M'',''07M'',''01F'')) over (partition by {i_edf})', NULL, 3, NULL, 'Un évènement de création doit être déclaré seul', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vBeta', 'CARDINALITE', 'i_etablissement', 'i_ice', '1', '1', 'substr({v_c10_1},1,1)=''0'' and {v_c10_1}!=''02M''', NULL, 22, NULL, 'Le bloc ICE est obligatoire pour une création', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vBeta', 'CARDINALITE', 'i_edf', 'i_c10', '1', '1', 'bool_or({v_c10_1} in (''41P'',''42M'',''40F'')) over (partition by {i_edf})', NULL, 4, NULL, 'Un évènement de cessation doit être déclaré seul', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vBeta', 'CARDINALITE', 'i_siu', 'i_u11', '1', '1', 'substr({v_c10_1},1,1)=''0''', NULL, 16, NULL, 'L''adresse du siège est obligatoire pour une création', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vBeta', 'CONDITION', NULL, NULL, NULL, NULL, '{v_e12_8_etablissement} is null or {v_e12_8_etablissement}::text!=''.''', NULL, 37, NULL, 'Le code postal de l''ancien établissement est non renseigné', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vBeta', 'CONDITION', NULL, NULL, NULL, NULL, '{v_e12_13_etablissement} is null or {v_e12_13_etablissement}::text!=''.''', NULL, 38, NULL, 'Le libellé de commune de l''ancien établissement est non renseigné', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vBeta', 'CARDINALITE', 'i_etablissement', 'i_iae_etablissement', '1', '1', 'substr({v_c10_1},1,2) in (''11'',''56'')', NULL, 33, NULL, 'Le bloc IAE est obligatoire pour un transfert', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vBeta', 'CONDITION', NULL, NULL, NULL, NULL, 'case when length({v_u02})=9 then ( select (cle::int%10) = 0 from ( select sum(case when (9-i)%2=1 then case when substr(t,i,1)::int<5 then substr(t,i,1)::int*2 else (substr(t,i,1)::int-5)*2+1 end else substr(t,i,1)::int end) as cle from ( select i, {v_u02} as t  from generate_series(1,9) i ) u ) v ) else false end', NULL, 44, NULL, 'Le SIREN est invalide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vBeta', 'CONDITION', NULL, NULL, NULL, NULL, 'case when length({v_p12})=9 then ( select (cle::int%10) = 0 from ( select sum(case when (9-i)%2=1 then case when substr(t,i,1)::int<5 then substr(t,i,1)::int*2 else (substr(t,i,1)::int-5)*2+1 end else substr(t,i,1)::int end) as cle from ( select i, {v_p12} as t  from generate_series(1,9) i ) u ) v ) else false end', NULL, 45, NULL, 'L''ancien SIREN est invalide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vBeta', 'CARDINALITE', 'i_p01_2', 'v_p01_2', '1', '1', NULL, NULL, 9, NULL, 'Le nom est vide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vBeta', 'CARDINALITE', 'i_p01_3', 'v_p01_3', '1', '1', '{i_p01_3}::int=1', NULL, 10, NULL, 'Le premier prénom est vide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vBeta', 'REGEXP', 'v_p01_2', NULL, NULL, NULL, '^[a-zàâäçéèêëîïôöùûüÿæœA-ZÀÂÄÇÉÈÊËÎÏÔÖÙÛÜŸÆŒ \-'''']*$', NULL, 11, NULL, 'Le nom contient un caractère non autorisé', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vBeta', 'REGEXP', 'v_p01_3', NULL, NULL, NULL, '^[a-zàâäçéèêëîïôöùûüÿæœA-ZÀÂÄÇÉÈÊËÎÏÔÖÙÛÜŸÆŒ \-'''']*$', NULL, 12, NULL, 'Un prénom contient un caractère non autorisé', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vBeta', 'CONDITION', NULL, NULL, NULL, NULL, '{v_m01_1} is null or {v_m01_1}::text!=''.''', NULL, 13, NULL, 'La dénomination de la personne morale est non renseignée', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vBeta', 'CONDITION', NULL, NULL, NULL, NULL, '{v_f01} is null or {v_f01}::text!=''.''', NULL, 14, NULL, 'La dénomination de l''exploitation en commun est non renseignée', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vBeta', 'CARDINALITE', 'i_u11_8', 'v_u11_8', '1', '1', NULL, NULL, 18, NULL, 'Le code postal du siège est vide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vBeta', 'CONDITION', NULL, NULL, NULL, NULL, '{v_u11_8} is null or {v_u11_8}::text!=''.''', NULL, 19, NULL, 'Le code postal du siège est non renseigné', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vBeta', 'CONDITION', NULL, NULL, NULL, NULL, '{v_u11_13} is null or {v_u11_13}::text!=''.''', NULL, 20, NULL, 'Le libellé de commune du siège est non renseigné', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vBeta', 'ENUM_TABLE', 'v_u11_3', NULL, NULL, NULL, 'select code from arc.nmcl_cog_v004', NULL, 17, NULL, 'Le code géographique du siège est invalide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vBeta', 'CARDINALITE', 'i_entreprise', 'i_etablissement', '1', '1', 'substr({v_c10_1},1,1)=''0'' and {v_c10_1}!=''02M''', NULL, 21, NULL, 'Le bloc Etablissement est obligatoire pour une création', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vBeta', 'CARDINALITE', 'i_entreprise', 'i_etablissement', '1', '1', 'substr({v_c10_1},1,2)=''11''', NULL, 23, NULL, 'Le bloc Etablissement est obligatoire pour un transfert d''entreprise', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vBeta', 'CARDINALITE', 'i_etablissement', 'i_ice', '1', '1', 'substr({v_c10_1},1,2)=''11''', NULL, 24, NULL, 'Le bloc ICE est obligatoire pour un transfert d''entreprise', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vBeta', 'CARDINALITE', 'i_entreprise', 'i_etablissement', '1', '1', 'substr({v_c10_1},1,2)::int>=50 and substr({v_c10_1},1,2)::int<90', NULL, 25, NULL, 'Le bloc Etablissement est obligatoire pour un évènement de niveau établissement', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vBeta', 'CARDINALITE', 'i_etablissement', 'i_ice', '1', '1', 'substr({v_c10_1},1,2)::int>=50 and substr({v_c10_1},1,2)::int<80', NULL, 26, NULL, 'Le bloc ICE est obligatoire pour une modification d''établissement', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vBeta', 'CARDINALITE', 'i_entreprise', 'i_etablissement', '1', '1', 'substr({v_c10_1},1,2)::int>=91 and substr({v_c10_1},1,2)::int<=94', NULL, 27, NULL, 'Le bloc Etablissement est obligatoire pour un rejet ou une radiation', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vBeta', 'CARDINALITE', 'i_etablissement', 'i_ice', '1', '1', 'substr({v_c10_1},1,2)::int>=91 and substr({v_c10_1},1,2)::int<=94', NULL, 28, NULL, 'Le bloc ICE est obligatoire pour un rejet ou une radiation', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vBeta', 'CARDINALITE', 'i_e02_8', 'v_e02_8', '1', '1', NULL, NULL, 30, NULL, 'Le code postal de l''établissement est vide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vBeta', 'CONDITION', NULL, NULL, NULL, NULL, '{v_e02_8} is null or {v_e02_8}::text!=''.''', NULL, 31, NULL, 'Le code postal de l''établissement est non renseigné', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vBeta', 'CONDITION', NULL, NULL, NULL, NULL, '{v_e02_13} is null or {v_e02_13}::text!=''.''', NULL, 32, NULL, 'Le libellé de commune de l''établissement est non renseigné', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vBeta', 'CARDINALITE', 'i_etablissement', 'i_iae_etablissement', '1', '1', 'substr({v_c10_1},1,2)::int>=80 and substr({v_c10_1},1,2)::int<90', NULL, 34, NULL, 'Le bloc IAE est obligatoire pour une fermeture d''établissement', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vBeta', 'CARDINALITE', 'i_e12_8_etablissement', 'v_e12_8_etablissement', '1', '1', NULL, NULL, 36, NULL, 'Le code postal de l''ancien établissement est vide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vBeta', 'CONDITION', NULL, NULL, NULL, NULL, '{v_p03_1} is null or {v_p03_1}::text!=''.''', NULL, 39, NULL, 'La date de naissance est non renseignée', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vBeta', 'CONDITION', NULL, NULL, NULL, NULL, '{v_p03_2} is null or {v_p03_2}::text!=''.''', NULL, 40, NULL, 'Le code géographique du lieu de naissance est non renseigné', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vBeta', 'CARDINALITE', 'i_entreprise', 'i_ipu', '1', '1', 'substr({v_c10_1},1,1)!=''0''', NULL, 41, NULL, 'Le bloc IPU est obligatoire pour les évènements de modification', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vBeta', 'CARDINALITE', 'i_personnephysique', 'i_aip', '1', '1', '{v_c10_1}=''05P''', NULL, 42, NULL, 'Le bloc AIP est obligatoire pour une réactivation d''une entreprise individuelle', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vBeta', 'CARDINALITE', 'i_aip', 'i_p12', '1', '1', '{v_c10_1}=''05P''', NULL, 43, NULL, 'L''ancien SIREN est obligatoire pour une réactivation d''une entreprise individuelle', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vTestsMarie', 'CARDINALITE', 'i_aip', 'i_p12', '1', '1', '{v_c10_1}=''05P''', NULL, 43, NULL, 'L''ancien SIREN est obligatoire pour une réactivation d''une entreprise individuelle', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vTestsMarie', 'CONDITION', NULL, NULL, NULL, NULL, 'case when length({v_u02})=9
then (
select (cle::int%10) = 0
from (
select sum(case when (9-i)%2=1 then case when substr(t,i,1)::int<5 then substr(t,i,1)::int*2 else (substr(t,i,1)::int-5)*2+1 end else substr(t,i,1)::int end) as cle
from (
select i,
{v_u02} as t 
from generate_series(1,9) i
) u
) v
)
else false
end', NULL, 44, NULL, 'Le SIREN est invalide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vTestsMarie', 'CONDITION', NULL, NULL, NULL, NULL, 'case when length({v_p12})=9
then (
select (cle::int%10) = 0
from (
select sum(case when (9-i)%2=1 then case when substr(t,i,1)::int<5 then substr(t,i,1)::int*2 else (substr(t,i,1)::int-5)*2+1 end else substr(t,i,1)::int end) as cle
from (
select i,
{v_p12} as t 
from generate_series(1,9) i
) u
) v
)
else false
end', NULL, 45, NULL, 'L''ancien SIREN est invalide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vTestsMarie', 'CARDINALITE', 'i_entreprise', 'i_cpu', '1', '1', 'substr({v_c10_1},1,1)=''0'' and {v_c10_1}!=''02M''', NULL, 46, NULL, 'Le bloc CPU est obligatoire pour une création', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vTestsMarie', 'CARDINALITE', 'i_cpu', 'i_u21', '1', '1', 'substr({v_c10_1},1,1)=''0'' and {v_c10_1}!=''02M''', NULL, 47, NULL, 'L''activité principale de l''entreprise est obligatoire pour une création', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vTestsMarie', 'CARDINALITE', 'i_u21', 'v_u21', '1', '1', NULL, NULL, 48, NULL, 'L''activité principale de l''entreprise est vide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vTestsMarie', 'CARDINALITE', 'i_etablissement', 'i_ace', '1', '1', 'substr({v_c10_1},1,1)=''0'' and {v_c10_1} not in (''02M'',''01F'')', NULL, 49, NULL, 'Le bloc ACE est obligatoire pour une création', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vBeta', 'ENUM_TABLE', 'v_e02_3', NULL, NULL, NULL, 'select code from arc.nmcl_cog_v004', NULL, 29, NULL, 'Le code géographique de l''établissement est invalide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vBeta', 'ENUM_TABLE', 'v_e12_3_etablissement', NULL, NULL, NULL, 'select code from arc.nmcl_cog_v004', NULL, 35, NULL, 'Le code géographique de l''ancien établissement est invalide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_entreprise', 'i_cpu', '1', '1', 'substr({v_c10_1},1,1)=''0'' and {v_c10_1}!=''02M''', NULL, 46, NULL, 'Le bloc CPU est obligatoire pour une création', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vTestsMarie', 'ENUM_TABLE', 'v_u11_3', NULL, NULL, NULL, 'select code from arc.nmcl_cog_v004', NULL, 17, NULL, 'Le code géographique du siège est invalide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vTestsMarie', 'ENUM_TABLE', 'v_e02_3', NULL, NULL, NULL, 'select code from arc.nmcl_cog_v004', NULL, 29, NULL, 'Le code géographique de l''établissement est invalide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vTestsMarie', 'ENUM_TABLE', 'v_c10_1', NULL, NULL, NULL, 'select code from arc.nmcl_evenements_v002', NULL, 2, NULL, 'Le code évènement est invalide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vTestsMarie', 'CARDINALITE', 'i_edf', 'i_c10', '1', '1', 'bool_or({v_c10_1} in (''01P'',''07P'',''01M'',''02M'',''03M'',''04M'',''05M'',''07M'',''01F'')) over (partition by {i_edf})', NULL, 3, NULL, 'Un évènement de création doit être déclaré seul', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vTestsMarie', 'CARDINALITE', 'i_edf', 'i_c10', '1', '1', 'bool_or({v_c10_1} in (''41P'',''42M'',''24F'',''27F'',''40F'')) over (partition by {i_edf})', NULL, 4, NULL, 'Un évènement de cessation doit être déclaré seul', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vTestsMarie', 'CARDINALITE', 'i_edf', 'i_c10', '1', '1', 'bool_or({v_c10_1} in (''93P'',''93M'',''94P'',''94M'',''96P'',''96M'')) over (partition by {i_edf})', NULL, 5, NULL, 'Un évènement de radiation doit être déclaré seul', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vTestsMarie', 'CARDINALITE', 'i_liasse', 'i_personnephysique', '1', '1', 'substr({v_c10_1},3,1)=''P''', NULL, 6, NULL, 'Un évènement n''est pas cohérent avec le bloc PersonnePhysique', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vTestsMarie', 'CARDINALITE', 'i_liasse', 'i_personnemorale', '1', '1', 'substr({v_c10_1},3,1)=''M''', NULL, 7, NULL, 'Un évènement n''est pas cohérent avec le bloc PersonneMorale', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vTestsMarie', 'CARDINALITE', 'i_liasse', 'i_exploitationencommun', '1', '1', 'substr({v_c10_1},3,1)=''F''', NULL, 8, NULL, 'Un évènement n''est pas cohérent avec le bloc ExploitationEnCommun', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vTestsMarie', 'CARDINALITE', 'i_p01_2', 'v_p01_2', '1', '1', NULL, NULL, 9, NULL, 'Le nom est vide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vTestsMarie', 'CARDINALITE', 'i_p01_3', 'v_p01_3', '1', '1', '{i_p01_3}::int=1', NULL, 10, NULL, 'Le premier prénom est vide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vTestsMarie', 'REGEXP', 'v_p01_2', NULL, NULL, NULL, '^[a-zàâäçéèêëîïôöùûüÿæœA-ZÀÂÄÇÉÈÊËÎÏÔÖÙÛÜŸÆŒ \-'''']*$', NULL, 11, NULL, 'Le nom contient un caractère non autorisé', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vTestsMarie', 'REGEXP', 'v_p01_3', NULL, NULL, NULL, '^[a-zàâäçéèêëîïôöùûüÿæœA-ZÀÂÄÇÉÈÊËÎÏÔÖÙÛÜŸÆŒ \-'''']*$', NULL, 12, NULL, 'Un prénom contient un caractère non autorisé', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vTestsMarie', 'CONDITION', NULL, NULL, NULL, NULL, '{v_m01_1}::text!=''.'' and trim({v_m01_1})!=''''', NULL, 13, NULL, 'La dénomination de la personne morale est non renseignée', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vTestsMarie', 'CONDITION', NULL, NULL, NULL, NULL, '{v_f01}::text!=''.'' and trim({v_f01})!=''''', NULL, 14, NULL, 'La dénomination de l''exploitation en commun est non renseignée', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vTestsMarie', 'CARDINALITE', 'i_entreprise', 'i_siu', '1', '1', 'substr({v_c10_1},1,1)=''0''', NULL, 15, NULL, 'Le bloc SIU est obligatoire pour une création', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vTestsMarie', 'CARDINALITE', 'i_siu', 'i_u11', '1', '1', 'substr({v_c10_1},1,1)=''0''', NULL, 16, NULL, 'L''adresse du siège est obligatoire pour une création', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vTestsMarie', 'CARDINALITE', 'i_u11_8', 'v_u11_8', '1', '1', NULL, NULL, 18, NULL, 'Le code postal du siège est vide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vTestsMarie', 'CONDITION', NULL, NULL, NULL, NULL, '{v_u11_8}::text!=''.'' and trim({v_u11_8})!=''''', NULL, 19, NULL, 'Le code postal du siège est non renseigné', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vTestsMarie', 'CONDITION', NULL, NULL, NULL, NULL, '{v_u11_13}::text!=''.'' and trim({v_u11_13})!=''''', NULL, 20, NULL, 'Le libellé de commune du siège est non renseigné', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vTestsMarie', 'CARDINALITE', 'i_entreprise', 'i_etablissement', '1', '1', 'substr({v_c10_1},1,1)=''0'' and {v_c10_1}!=''02M''', NULL, 21, NULL, 'Le bloc Etablissement est obligatoire pour une création', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vTestsMarie', 'CARDINALITE', 'i_etablissement', 'i_ice', '1', '1', 'substr({v_c10_1},1,1)=''0'' and {v_c10_1}!=''02M''', NULL, 22, NULL, 'Le bloc ICE est obligatoire pour une création', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vTestsMarie', 'CARDINALITE', 'i_entreprise', 'i_etablissement', '1', '1', 'substr({v_c10_1},1,2)=''11''', NULL, 23, NULL, 'Le bloc Etablissement est obligatoire pour un transfert d''entreprise', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vTestsMarie', 'CARDINALITE', 'i_etablissement', 'i_ice', '1', '1', 'substr({v_c10_1},1,2)=''11''', NULL, 24, NULL, 'Le bloc ICE est obligatoire pour un transfert d''entreprise', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vTestsMarie', 'CARDINALITE', 'i_entreprise', 'i_etablissement', '1', '1', 'substr({v_c10_1},1,2)::int>=50 and substr({v_c10_1},1,2)::int<90', NULL, 25, NULL, 'Le bloc Etablissement est obligatoire pour un évènement de niveau établissement', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vTestsMarie', 'CARDINALITE', 'i_etablissement', 'i_ice', '1', '1', 'substr({v_c10_1},1,2)::int>=50 and substr({v_c10_1},1,2)::int<80', NULL, 26, NULL, 'Le bloc ICE est obligatoire pour une modification d''établissement', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vTestsMarie', 'CARDINALITE', 'i_entreprise', 'i_etablissement', '1', '1', 'substr({v_c10_1},1,2)::int>=91 and substr({v_c10_1},1,2)::int<=94', NULL, 27, NULL, 'Le bloc Etablissement est obligatoire pour un rejet ou une radiation', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vTestsMarie', 'CARDINALITE', 'i_etablissement', 'i_ice', '1', '1', 'substr({v_c10_1},1,2)::int>=91 and substr({v_c10_1},1,2)::int<=94', NULL, 28, NULL, 'Le bloc ICE est obligatoire pour un rejet ou une radiation', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vTestsMarie', 'CARDINALITE', 'i_e02_8', 'v_e02_8', '1', '1', NULL, NULL, 30, NULL, 'Le code postal de l''établissement est vide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_cpu', 'i_u21', '1', '1', 'substr({v_c10_1},1,1)=''0'' and {v_c10_1}!=''02M''', NULL, 47, NULL, 'L''activité principale de l''entreprise est obligatoire pour une création', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vTestsMarie', 'ENUM_TABLE', 'v_e12_3_etablissement', NULL, NULL, NULL, 'select code from arc.nmcl_cog_v004', NULL, 35, NULL, 'Le code géographique de l''ancien établissement est invalide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vTestsMarie', 'CONDITION', NULL, NULL, NULL, NULL, '{v_e02_8}::text!=''.'' and trim({v_e02_8})!=''''', NULL, 31, NULL, 'Le code postal de l''établissement est non renseigné', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vTestsMarie', 'CONDITION', NULL, NULL, NULL, NULL, '{v_e02_13}::text!=''.'' and trim({v_e02_13})!=''''', NULL, 32, NULL, 'Le libellé de commune de l''établissement est non renseigné', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vTestsMarie', 'CARDINALITE', 'i_etablissement', 'i_iae_etablissement', '1', '1', 'substr({v_c10_1},1,2) in (''11'',''56'')', NULL, 33, NULL, 'Le bloc IAE est obligatoire pour un transfert', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vTestsMarie', 'CARDINALITE', 'i_etablissement', 'i_iae_etablissement', '1', '1', 'substr({v_c10_1},1,2)::int>=80 and substr({v_c10_1},1,2)::int<90', NULL, 34, NULL, 'Le bloc IAE est obligatoire pour une fermeture d''établissement', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vTestsMarie', 'CARDINALITE', 'i_e12_8_etablissement', 'v_e12_8_etablissement', '1', '1', NULL, NULL, 36, NULL, 'Le code postal de l''ancien établissement est vide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vTestsMarie', 'CONDITION', NULL, NULL, NULL, NULL, '{v_e12_8_etablissement}::text!=''.''  and trim({v_e12_8_etablissement})!=''''', NULL, 37, NULL, 'Le code postal de l''ancien établissement est non renseigné', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vTestsMarie', 'CONDITION', NULL, NULL, NULL, NULL, '{v_e12_13_etablissement}::text!=''.'' and trim({v_e12_13_etablissement})!=''''', NULL, 38, NULL, 'Le libellé de commune de l''ancien établissement est non renseigné', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vTestsMarie', 'CONDITION', NULL, NULL, NULL, NULL, '{v_p03_1} is null or {v_p03_1}::text!=''.''', NULL, 39, NULL, 'La date de naissance est non renseignée', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vTestsMarie', 'CONDITION', NULL, NULL, NULL, NULL, '{v_p03_2} is null or {v_p03_2}::text!=''.''', NULL, 40, NULL, 'Le code géographique du lieu de naissance est non renseigné', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vTestsMarie', 'CARDINALITE', 'i_entreprise', 'i_ipu', '1', '1', 'substr({v_c10_1},1,1)!=''0''', NULL, 41, NULL, 'Le bloc IPU est obligatoire pour les évènements de modification', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vTestsMarie', 'CARDINALITE', 'i_personnephysique', 'i_aip', '1', '1', '{v_c10_1}=''05P''', NULL, 42, NULL, 'Le bloc AIP est obligatoire pour une réactivation d''une entreprise individuelle', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vTestsMarie', 'CONDITION', NULL, NULL, NULL, NULL, 'case when substr({v_c10_1},1,2) in (''44'',''45'',''46'',''47'') and {v_c05}::text!=''A'' then false else true end', NULL, 50, NULL, 'Le type de liasse est incohérent avec l''évènement (formalité agricole)', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vBeta', 'CARDINALITE', 'i_entreprise', 'i_siu', '1', '1', 'substr({v_c10_1},1,1)=''0''', NULL, 15, NULL, 'Le bloc SIU est obligatoire pour une création', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_u21', 'v_u21', '1', '1', NULL, NULL, 48, NULL, 'L''activité principale de l''entreprise est vide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_etablissement', 'i_ace', '1', '1', 'substr({v_c10_1},1,1)=''0'' and {v_c10_1} not in (''02M'',''01F'')', NULL, 49, NULL, 'Le bloc ACE est obligatoire pour une création', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'ENUM_TABLE', 'v_c10_1', NULL, NULL, NULL, 'select code from arc.nmcl_evenements_v001', NULL, 2, NULL, 'Le code évènement est invalide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_edf', 'i_c10', '1', '1', 'bool_or({v_c10_1} in (''93P'',''93M'',''94P'',''94M'')) over (partition by {i_edf})', NULL, 5, NULL, 'Un évènement de radiation doit être déclaré seul', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_liasse', 'i_personnephysique', '1', '1', 'substr({v_c10_1},3,1)=''P''', NULL, 6, NULL, 'Un évènement n''est pas cohérent avec le bloc PersonnePhysique', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_liasse', 'i_personnemorale', '1', '1', 'substr({v_c10_1},3,1)=''M''', NULL, 7, NULL, 'Un évènement n''est pas cohérent avec le bloc PersonneMorale', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_liasse', 'i_exploitationencommun', '1', '1', 'substr({v_c10_1},3,1)=''F''', NULL, 8, NULL, 'Un évènement n''est pas cohérent avec le bloc ExploitationEnCommun', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'ENUM_TABLE', 'v_u11_3', NULL, NULL, NULL, 'select code from arc.nmcl_cog_v004', NULL, 17, NULL, 'Le code géographique du siège est invalide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_edf', 'i_c10', '1', '1', 'bool_or({v_c10_1} in (''01P'',''07P'',''01M'',''02M'',''03M'',''04M'',''05M'',''07M'',''01F'')) over (partition by {i_edf})', NULL, 3, NULL, 'Un évènement de création doit être déclaré seul', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_edf', 'i_c10', '1', '1', 'bool_or({v_c10_1} in (''41P'',''42M'',''40F'')) over (partition by {i_edf})', NULL, 4, NULL, 'Un évènement de cessation doit être déclaré seul', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_siu', 'i_u11', '1', '1', 'substr({v_c10_1},1,1)=''0''', NULL, 16, NULL, 'L''adresse du siège est obligatoire pour une création', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_etablissement', 'i_ice', '1', '1', 'substr({v_c10_1},1,1)=''0'' and {v_c10_1}!=''02M''', NULL, 22, NULL, 'Le bloc ICE est obligatoire pour une création', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_etablissement', 'i_iae_etablissement', '1', '1', 'substr({v_c10_1},1,2) in (''11'',''56'')', NULL, 33, NULL, 'Le bloc IAE est obligatoire pour un transfert', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CONDITION', NULL, NULL, NULL, NULL, '{v_e12_8_etablissement} is null or {v_e12_8_etablissement}::text!=''.''', NULL, 37, NULL, 'Le code postal de l''ancien établissement est non renseigné', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CONDITION', NULL, NULL, NULL, NULL, '{v_e12_13_etablissement} is null or {v_e12_13_etablissement}::text!=''.''', NULL, 38, NULL, 'Le libellé de commune de l''ancien établissement est non renseigné', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'ENUM_TABLE', 'v_c10_1', NULL, NULL, NULL, 'select code from arc.nmcl_evenements_v002', NULL, 2, NULL, 'Le code évènement est invalide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_edf', 'i_c10', '1', '1', 'bool_or({v_c10_1} in (''01P'',''07P'',''01M'',''02M'',''03M'',''04M'',''05M'',''07M'',''01F'')) over (partition by {i_edf})', NULL, 3, NULL, 'Un évènement de création doit être déclaré seul', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_edf', 'i_c10', '1', '1', 'bool_or({v_c10_1} in (''41P'',''42M'',''24F'',''27F'',''40F'')) over (partition by {i_edf})', NULL, 4, NULL, 'Un évènement de cessation doit être déclaré seul', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_edf', 'i_c10', '1', '1', 'bool_or({v_c10_1} in (''93P'',''93M'',''94P'',''94M'',''96P'',''96M'')) over (partition by {i_edf})', NULL, 5, NULL, 'Un évènement de radiation doit être déclaré seul', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_liasse', 'i_personnephysique', '1', '1', 'substr({v_c10_1},3,1)=''P''', NULL, 6, NULL, 'Un évènement n''est pas cohérent avec le bloc PersonnePhysique', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_personnephysique', 'i_aip', '1', '1', '{v_c10_1}=''05P''', NULL, 42, NULL, 'Le bloc AIP est obligatoire pour une réactivation d''une entreprise individuelle', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CONDITION', NULL, NULL, NULL, NULL, '{v_e12_8_etablissement}::text!=''.''  and trim({v_e12_8_etablissement})!=''''', NULL, 37, NULL, 'Le code postal de l''ancien établissement est non renseigné', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CONDITION', NULL, NULL, NULL, NULL, '{v_e12_13_etablissement}::text!=''.'' and trim({v_e12_13_etablissement})!=''''', NULL, 38, NULL, 'Le libellé de commune de l''ancien établissement est non renseigné', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'ENUM_TABLE', 'v_e12_3_etablissement', NULL, NULL, NULL, 'select code from arc.nmcl_cog_v004', NULL, 35, NULL, 'Le code géographique de l''ancien établissement est invalide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_aip', 'i_p12', '1', '1', '{v_c10_1}=''05P''', NULL, 43, NULL, 'L''ancien SIREN est obligatoire pour une réactivation d''une entreprise individuelle', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CONDITION', NULL, NULL, NULL, NULL, 'case when length({v_u02})=9
then (
select (cle::int%10) = 0
from (
select sum(case when (9-i)%2=1 then case when substr(t,i,1)::int<5 then substr(t,i,1)::int*2 else (substr(t,i,1)::int-5)*2+1 end else substr(t,i,1)::int end) as cle
from (
select i,
{v_u02} as t 
from generate_series(1,9) i
) u
) v
)
else false
end', NULL, 44, NULL, 'Le SIREN est invalide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CONDITION', NULL, NULL, NULL, NULL, 'case when length({v_p12})=9
then (
select (cle::int%10) = 0
from (
select sum(case when (9-i)%2=1 then case when substr(t,i,1)::int<5 then substr(t,i,1)::int*2 else (substr(t,i,1)::int-5)*2+1 end else substr(t,i,1)::int end) as cle
from (
select i,
{v_p12} as t 
from generate_series(1,9) i
) u
) v
)
else false
end', NULL, 45, NULL, 'L''ancien SIREN est invalide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_e12_8_etablissement', 'v_e12_8_etablissement', '1', '1', NULL, NULL, 36, NULL, 'Le code postal de l''ancien établissement est vide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CONDITION', NULL, NULL, NULL, NULL, '{v_p03_1} is null or {v_p03_1}::text!=''.''', NULL, 39, NULL, 'La date de naissance est non renseignée', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CONDITION', NULL, NULL, NULL, NULL, '{v_p03_2} is null or {v_p03_2}::text!=''.''', NULL, 40, NULL, 'Le code géographique du lieu de naissance est non renseigné', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_entreprise', 'i_ipu', '1', '1', 'substr({v_c10_1},1,1)!=''0''', NULL, 41, NULL, 'Le bloc IPU est obligatoire pour les évènements de modification', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CONDITION', NULL, NULL, NULL, NULL, 'case when length({v_u02})=9
then (
select (cle::int%10) = 0
from (
select sum(case when (9-i)%2=1 then case when substr(t,i,1)::int<5 then substr(t,i,1)::int*2 else (substr(t,i,1)::int-5)*2+1 end else substr(t,i,1)::int end) as cle
from (
select i,
{v_u02} as t 
from generate_series(1,9) i
) u
) v
)
else false
end', NULL, 44, NULL, 'Le SIREN est invalide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CONDITION', NULL, NULL, NULL, NULL, 'case when length({v_p12})=9
then (
select (cle::int%10) = 0
from (
select sum(case when (9-i)%2=1 then case when substr(t,i,1)::int<5 then substr(t,i,1)::int*2 else (substr(t,i,1)::int-5)*2+1 end else substr(t,i,1)::int end) as cle
from (
select i,
{v_p12} as t 
from generate_series(1,9) i
) u
) v
)
else false
end', NULL, 45, NULL, 'L''ancien SIREN est invalide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_entreprise', 'i_siu', '1', '1', 'substr({v_c10_1},1,1)=''0''', NULL, 15, NULL, 'Le bloc SIU est obligatoire pour une création', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_p01_3', 'v_p01_3', '1', '1', '{i_p01_3}::int=1', NULL, 10, NULL, 'Le premier prénom est vide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'REGEXP', 'v_p01_2', NULL, NULL, NULL, '^[a-zàâäçéèêëîïôöùûüÿæœA-ZÀÂÄÇÉÈÊËÎÏÔÖÙÛÜŸÆŒ \-'''']*$', NULL, 11, NULL, 'Le nom contient un caractère non autorisé', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'REGEXP', 'v_p01_3', NULL, NULL, NULL, '^[a-zàâäçéèêëîïôöùûüÿæœA-ZÀÂÄÇÉÈÊËÎÏÔÖÙÛÜŸÆŒ \-'''']*$', NULL, 12, NULL, 'Un prénom contient un caractère non autorisé', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CONDITION', NULL, NULL, NULL, NULL, '{v_m01_1} is null or {v_m01_1}::text!=''.''', NULL, 13, NULL, 'La dénomination de la personne morale est non renseignée', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CONDITION', NULL, NULL, NULL, NULL, '{v_f01} is null or {v_f01}::text!=''.''', NULL, 14, NULL, 'La dénomination de l''exploitation en commun est non renseignée', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_u11_8', 'v_u11_8', '1', '1', NULL, NULL, 18, NULL, 'Le code postal du siège est vide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CONDITION', NULL, NULL, NULL, NULL, '{v_u11_8} is null or {v_u11_8}::text!=''.''', NULL, 19, NULL, 'Le code postal du siège est non renseigné', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CONDITION', NULL, NULL, NULL, NULL, '{v_u11_13} is null or {v_u11_13}::text!=''.''', NULL, 20, NULL, 'Le libellé de commune du siège est non renseigné', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_p01_2', 'v_p01_2', '1', '1', NULL, NULL, 9, NULL, 'Le nom est vide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_entreprise', 'i_siu', '1', '1', 'substr({v_c10_1},1,1)=''0''', NULL, 15, NULL, 'Le bloc SIU est obligatoire pour une création', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'ENUM_TABLE', 'v_u11_3', NULL, NULL, NULL, 'select code from arc.nmcl_cog_v004', NULL, 17, NULL, 'Le code géographique du siège est invalide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CONDITION', NULL, NULL, NULL, NULL, '{v_m01_1}::text!=''.'' and trim({v_m01_1})!=''''', NULL, 13, NULL, 'La dénomination de la personne morale est non renseignée', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CONDITION', NULL, NULL, NULL, NULL, '{v_f01}::text!=''.'' and trim({v_f01})!=''''', NULL, 14, NULL, 'La dénomination de l''exploitation en commun est non renseignée', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_liasse', 'i_personnemorale', '1', '1', 'substr({v_c10_1},3,1)=''M''', NULL, 7, NULL, 'Un évènement n''est pas cohérent avec le bloc PersonneMorale', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_liasse', 'i_exploitationencommun', '1', '1', 'substr({v_c10_1},3,1)=''F''', NULL, 8, NULL, 'Un évènement n''est pas cohérent avec le bloc ExploitationEnCommun', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_p01_2', 'v_p01_2', '1', '1', NULL, NULL, 9, NULL, 'Le nom est vide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_p01_3', 'v_p01_3', '1', '1', '{i_p01_3}::int=1', NULL, 10, NULL, 'Le premier prénom est vide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'REGEXP', 'v_p01_2', NULL, NULL, NULL, '^[a-zàâäçéèêëîïôöùûüÿæœA-ZÀÂÄÇÉÈÊËÎÏÔÖÙÛÜŸÆŒ \-'''']*$', NULL, 11, NULL, 'Le nom contient un caractère non autorisé', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'REGEXP', 'v_p01_3', NULL, NULL, NULL, '^[a-zàâäçéèêëîïôöùûüÿæœA-ZÀÂÄÇÉÈÊËÎÏÔÖÙÛÜŸÆŒ \-'''']*$', NULL, 12, NULL, 'Un prénom contient un caractère non autorisé', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2016-02', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_siu', 'i_u11', '1', '1', 'substr({v_c10_1},1,1)=''0''', NULL, 16, NULL, 'L''adresse du siège est obligatoire pour une création', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'ENUM_TABLE', 'v_e02_3', NULL, NULL, NULL, 'select code from arc.nmcl_cog_v004', NULL, 29, NULL, 'Le code géographique de l''établissement est invalide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'ENUM_TABLE', 'v_e12_3_etablissement', NULL, NULL, NULL, 'select code from arc.nmcl_cog_v004', NULL, 35, NULL, 'Le code géographique de l''ancien établissement est invalide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_e12_8_etablissement', 'v_e12_8_etablissement', '1', '1', NULL, NULL, 36, NULL, 'Le code postal de l''ancien établissement est vide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CONDITION', NULL, NULL, NULL, NULL, '{v_p03_1} is null or {v_p03_1}::text!=''.''', NULL, 39, NULL, 'La date de naissance est non renseignée', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_entreprise', 'i_etablissement', '1', '1', 'substr({v_c10_1},1,1)=''0'' and {v_c10_1}!=''02M''', NULL, 21, NULL, 'Le bloc Etablissement est obligatoire pour une création', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_entreprise', 'i_etablissement', '1', '1', 'substr({v_c10_1},1,2)=''11''', NULL, 23, NULL, 'Le bloc Etablissement est obligatoire pour un transfert d''entreprise', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_etablissement', 'i_ice', '1', '1', 'substr({v_c10_1},1,2)=''11''', NULL, 24, NULL, 'Le bloc ICE est obligatoire pour un transfert d''entreprise', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_entreprise', 'i_etablissement', '1', '1', 'substr({v_c10_1},1,2)::int>=50 and substr({v_c10_1},1,2)::int<90', NULL, 25, NULL, 'Le bloc Etablissement est obligatoire pour un évènement de niveau établissement', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_etablissement', 'i_ice', '1', '1', 'substr({v_c10_1},1,2)::int>=50 and substr({v_c10_1},1,2)::int<80', NULL, 26, NULL, 'Le bloc ICE est obligatoire pour une modification d''établissement', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_entreprise', 'i_etablissement', '1', '1', 'substr({v_c10_1},1,2)::int>=91 and substr({v_c10_1},1,2)::int<=94', NULL, 27, NULL, 'Le bloc Etablissement est obligatoire pour un rejet ou une radiation', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_etablissement', 'i_ice', '1', '1', 'substr({v_c10_1},1,2)::int>=91 and substr({v_c10_1},1,2)::int<=94', NULL, 28, NULL, 'Le bloc ICE est obligatoire pour un rejet ou une radiation', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_e02_8', 'v_e02_8', '1', '1', NULL, NULL, 30, NULL, 'Le code postal de l''établissement est vide', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CONDITION', NULL, NULL, NULL, NULL, '{v_e02_8} is null or {v_e02_8}::text!=''.''', NULL, 31, NULL, 'Le code postal de l''établissement est non renseigné', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CONDITION', NULL, NULL, NULL, NULL, '{v_e02_13} is null or {v_e02_13}::text!=''.''', NULL, 32, NULL, 'Le libellé de commune de l''établissement est non renseigné', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_etablissement', 'i_iae_etablissement', '1', '1', 'substr({v_c10_1},1,2)::int>=80 and substr({v_c10_1},1,2)::int<90', NULL, 34, NULL, 'Le bloc IAE est obligatoire pour une fermeture d''établissement', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CONDITION', NULL, NULL, NULL, NULL, '{v_p03_2} is null or {v_p03_2}::text!=''.''', NULL, 40, NULL, 'Le code géographique du lieu de naissance est non renseigné', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_entreprise', 'i_ipu', '1', '1', 'substr({v_c10_1},1,1)!=''0''', NULL, 41, NULL, 'Le bloc IPU est obligatoire pour les évènements de modification', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_personnephysique', 'i_aip', '1', '1', '{v_c10_1}=''05P''', NULL, 42, NULL, 'Le bloc AIP est obligatoire pour une réactivation d''une entreprise individuelle', NULL, NULL, NULL, '>0%', 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('v2008-11', 'A', '2020-01-01', '2100-01-01', 'vConformite', 'CARDINALITE', 'i_aip', 'i_p12', '1', '1', '{v_c10_1}=''05P''', NULL, 43, NULL, 'L''ancien SIREN est obligatoire pour une réactivation d''une entreprise individuelle', NULL, NULL, NULL, '>0%', 'e');


--
-- TOC entry 7349 (class 0 OID 17212)
-- Dependencies: 302
-- Data for Name: ihm_entrepot; Type: TABLE DATA; Schema: arc; Owner: -
--

update arc.ihm_entrepot set id_loader = 'DEFAULT' where id_entrepot = 'DEFAULT';


--
-- TOC entry 7368 (class 0 OID 16975828)
-- Dependencies: 786
-- Data for Name: ihm_expression; Type: TABLE DATA; Schema: arc; Owner: -
--

INSERT INTO arc.ihm_expression VALUES (1, 'v2008-11', '2020-01-01', '2100-01-01', 'vConformite', 'A', 'evenements_creation', '{v_c10_1} in (''01P'',''07P'',''01M'',''02M'',''03M'',''04M'',''05M'',''07M'',''01F'')', NULL);
INSERT INTO arc.ihm_expression VALUES (1, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'prenom_distinct', '(public.array_agg_distinct(row(#i_p01_3#, #v_p01_3#)::cle_valeur) over (partition by {i_personnephysique}))', NULL);
INSERT INTO arc.ihm_expression VALUES (2, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'prenom_anc_distinct', '(public.array_agg_distinct(row(#i_p14#, #v_p14#)::cle_valeur) over (partition by {i_personnephysique}))', NULL);


--
-- TOC entry 7341 (class 0 OID 17130)
-- Dependencies: 294
-- Data for Name: ihm_filtrage_regle; Type: TABLE DATA; Schema: arc; Owner: -
--



--
-- TOC entry 7342 (class 0 OID 17143)
-- Dependencies: 295
-- Data for Name: ihm_mapping_regle; Type: TABLE DATA; Schema: arc; Owner: -
--

INSERT INTO arc.ihm_mapping_regle VALUES (32, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'prenom1_anc', '{@prenom_anc_distinct@}[1]', 'règle similaire à prenom1 en erreur si i_p14 n''existe pas');
INSERT INTO arc.ihm_mapping_regle VALUES (95, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'rivoli', 'unnest(array[{v_c37_4},{v_p04_4},{v_u11_4},{v_e02_4},{v_e12_4_etablissement}])', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (111, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'post', 'unnest(array[{v_c37_8},{v_p04_8},{v_u11_8},{v_e02_8},{v_e12_8_etablissement}])', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (132, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'com_lib', 'unnest(array[{v_c37_13},{v_p04_13},{v_u11_13},{v_e02_13},{v_e12_13_etablissement}])', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (144, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'evenement_date', 'unnest((public.array_agg_distinct(row(#i_c10#, to_date(#v_c10_2#,''YYYY-MM-DD''))::cle_valeur) over (partition by {i_service}))||(public.array_agg_distinct(row(case when #v_c44_1_1#=''C1210'' then #i_c44_1# end, case when #v_c44_1_1#=''C1210'' then null::text end)::cle_valeur) over (partition by {i_service})))', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (164, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'nais_depcom', '{v_p03_2}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (175, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'nais_date', 'to_date({v_p03_1},''YYYY-MM-DD'')', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (216, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'obs_valeur', '{v_c44_1_2}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (256, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'corr_tel1', 'null', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (296, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'activ_pr_lib', 'null', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (45, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'numero_liasse', '{v_c02}', '{v_c02}');
INSERT INTO arc.ihm_mapping_regle VALUES (99, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'voie_num', 'unnest(array[{v_c37_5},{v_p04_5},{v_u11_5},{v_e02_5},{v_e12_5_etablissement}])', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (136, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'pays_lib', 'unnest(array[{v_c37_14},{v_p04_14},{v_u11_14},{v_e02_14},{v_e12_14_etablissement}])', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (179, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'forain', '{v_p06}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (148, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'evenement_autre', 'unnest((public.array_agg_distinct(row(#i_c10#, #v_c10_3#)::cle_valeur) over (partition by {i_service}))||(public.array_agg_distinct(row(case when #v_c44_1_1#=''C1210'' then #i_c44_1# end, null::text)::cle_valeur) over (partition by {i_service})))', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (168, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'nais_pays_lib', '{v_p03_3}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (220, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'dist_lib', 'unnest(array[{v_c37_9},{v_p04_9},{v_u11_9},{v_e02_9},{v_e12_9_etablissement}])', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (260, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'corr_tel2', 'null', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (300, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'objet_lib', 'null', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (184, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'artiste_auteur', 'null', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (103, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'voie_repet', 'unnest(array[{v_c37_6},{v_p04_6},{v_u11_6},{v_e02_6},{v_e12_6_etablissement}])', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (120, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'compl', 'unnest(array[{v_c37_10},{v_p04_10},{v_u11_10},{v_e02_10},{v_e12_10_etablissement}])', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (140, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'evenement_type', 'unnest((public.array_agg_distinct(row(#i_c10#, #v_c10_1#)::cle_valeur) over (partition by {i_service}))||(public.array_agg_distinct(row(case when #v_c44_1_1#=''C1210'' then #i_c44_1# end, case when #v_c44_1_1#=''C1210'' then #v_c44_1_2# end)::cle_valeur) over (partition by {i_service})))', 'unnest(array[{v_c10_1},case when {v_c44_1_1}=''C1210'' then {v_c44_1_2} end])');
INSERT INTO arc.ihm_mapping_regle VALUES (224, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'evenement_agrege', 'case when ''15P'' in {v_c10_1}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (152, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'nom', '{v_p01_2}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (172, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'nais_com_lib', '{v_p03_4}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (264, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'corr_mel', 'null', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (304, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'activ_nat', 'null', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (60, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'dirigeant_prenom4', 'null', 'règle générée automatiquement lors de la création de cette variable');
INSERT INTO arc.ihm_mapping_regle VALUES (188, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'micro_entrepreneur', '{v_p07}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (61, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'dirigeant_nom', 'unnest((public.array_agg_distinct(row(#i_personnephysiquedirigeante#, #v_d01_2_personnephysiquedirigeante#)::cle_valeur) over (partition by {i_liasse}))||(public.array_agg_distinct(row(#i_gmc#,#v_d01_2_gmc#)::cle_valeur) over (partition by {i_liasse}))||(public.array_agg_distinct(row(#i_ide#,#v_e93_2#)::cle_valeur) over (partition by {i_liasse})))', 'règle générée automatiquement lors de la création de cette variable');
INSERT INTO arc.ihm_mapping_regle VALUES (62, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'dirigeant_nom_usage', 'unnest((public.array_agg_distinct(row(#i_personnephysiquedirigeante#, #v_d01_4_personnephysiquedirigeante#)::cle_valeur) over (partition by {i_liasse}))||(public.array_agg_distinct(row(#i_gmc#,#v_d01_4_gmc#)::cle_valeur) over (partition by {i_liasse}))||(public.array_agg_distinct(row(#i_ide#,#v_e93_4#)::cle_valeur) over (partition by {i_liasse})))', 'règle générée automatiquement lors de la création de cette variable');
INSERT INTO arc.ihm_mapping_regle VALUES (63, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'dirigeant_pseudo', 'unnest((public.array_agg_distinct(row(#i_personnephysiquedirigeante#, #v_d02_personnephysiquedirigeante#)::cle_valeur) over (partition by {i_liasse}))||(public.array_agg_distinct(row(#i_gmc#,#v_d02_gmc#)::cle_valeur) over (partition by {i_liasse}))||(public.array_agg_distinct(row(#i_ide#,null::text)::cle_valeur) over (partition by {i_liasse})))', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (64, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'nature', 'unnest((public.array_agg_distinct(row(#i_personnephysiquedirigeante#, #v_u52_personnephysiquedirigeante#)::cle_valeur) over (partition by {i_liasse}))||(public.array_agg_distinct(row(#i_gmc#,#v_u52_gmc#)::cle_valeur) over (partition by {i_liasse}))||(public.array_agg_distinct(row(#i_ide#,null::text)::cle_valeur) over (partition by {i_liasse})))', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (65, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'sexe', '{v_p01_1}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (228, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'liasse_decl_date', 'to_date({v_c01},''YYYY-MM-DD'')', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (268, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'decl_obs', 'null', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (308, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'fiscal_liberat_opt', 'null', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (70, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'prenom2_anc', '{@prenom_anc_distinct@}[2]', 'règle similaire à prenom2 en erreur si i_p14 n''existe pas');
INSERT INTO arc.ihm_mapping_regle VALUES (192, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'nom_anc', '{v_p11}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (72, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'dirigeant_situation_date', 'null', 'unnest(array[{v_u50_2_PersonnePhysiqueDirigeante},{v_u50_2_GMC},{v_e91_2}])');
INSERT INTO arc.ihm_mapping_regle VALUES (232, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'liasse_incomp', '{v_c03}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (272, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'siren', 'null', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (312, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'bru_loc_dpb', 'null', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (196, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'siren_anc', '{v_p12}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (236, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'liasse_trans_date', 'to_date({v_c04},''YYYY-MM-DD'')', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (276, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'domicil_denom', 'null', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (33, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'prenom4_anc', '{@prenom_anc_distinct@}[4]', 'règle similaire à prenom4 en erreur si i_p14 n''existe pas');
INSERT INTO arc.ihm_mapping_regle VALUES (4, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'prenom3_anc', '{@prenom_anc_distinct@}[3]', 'règle similaire à prenom3 en erreur si i_p14 n''existe pas');
INSERT INTO arc.ihm_mapping_regle VALUES (1, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'courriel', 'null', 'règle générée automatiquement lors de la création de cette variable');
INSERT INTO arc.ihm_mapping_regle VALUES (2, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'nom_usage', '{v_p02_1}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (3, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'dirigeant_sexe', 'unnest((public.array_agg_distinct(row(#i_personnephysiquedirigeante#, #v_d01_1_personnephysiquedirigeante#)::cle_valeur) over (partition by {i_liasse}))||(public.array_agg_distinct(row(#i_gmc#,#v_d01_1_gmc#)::cle_valeur) over (partition by {i_liasse}))||(public.array_agg_distinct(row(#i_ide#,null::text)::cle_valeur) over (partition by {i_liasse})))', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (200, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'nom_pseudo_anc', '{v_p15}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (7, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'id_identification_pp', '{pk:mapping_sirene4_identification_pp_ok}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (8, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'qualite_anc2', 'null', 'règle générée automatiquement lors de la création de cette variable');
INSERT INTO arc.ihm_mapping_regle VALUES (13, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'id_adresse', '{pk:mapping_sirene4_adresse_ok}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (14, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'dirigeant_prenom3', 'null', 'règle générée automatiquement lors de la création de cette variable');
INSERT INTO arc.ihm_mapping_regle VALUES (15, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'id_evenement', '{pk:mapping_sirene4_evenement_ok}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (16, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'depcom', 'unnest(array[{v_c37_3},{v_p04_3},{v_u11_3},{v_e02_3},{v_e12_3_etablissement}])', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (240, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'liasse_artisan_activ', 'null', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (280, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'domicil_siren', 'null', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (23, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'qualite1', 'null', 'règle générée automatiquement lors de la création de cette variable');
INSERT INTO arc.ihm_mapping_regle VALUES (24, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'id_service', '{pk:mapping_sirene4_service_ok}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (25, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'id_source', '{id_source}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (26, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'type_liasse', '{v_c05}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (34, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'qualite_anc1', 'null', 'règle générée automatiquement lors de la création de cette variable');
INSERT INTO arc.ihm_mapping_regle VALUES (35, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'qualite_lib1', 'null', 'règle générée automatiquement lors de la création de cette variable');
INSERT INTO arc.ihm_mapping_regle VALUES (36, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'date_heure_ajout', 'null', 'règle générée automatiquement lors de la création de cette variable');
INSERT INTO arc.ihm_mapping_regle VALUES (42, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'id_dirigeant', '{pk:mapping_sirene4_dirigeant_ok}', 'règle générée automatiquement lors de la création de cette variable');
INSERT INTO arc.ihm_mapping_regle VALUES (87, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'adresse_type', 'unnest (array[''correspondance'',''domicile'',''siege'',''etablissement'',''ancienne_adresse''])', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (124, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'voie_type', 'unnest(array[{v_c37_11},{v_p04_11},{v_u11_11},{v_e02_11},{v_e12_11_etablissement}])', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (156, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'nom_pseudo', '{v_p02_2}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (204, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'cess_pp_motif', '{v_p51}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (284, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'domicil_legal', 'null', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (244, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'liasse_numero_ant
liasse_numero_ant
liasse_numero_ant', 'null', '{v_c11}');
INSERT INTO arc.ihm_mapping_regle VALUES (288, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'regist_eur_id', 'null', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (208, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'id_observations', '{pk:mapping_sirene4_observations_ok}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (44, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'observations', '{v_c43}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (47, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'nom_usage_anc', '{v_p13}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (48, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'nationalite', '{v_p21}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (49, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'qualite2', 'null', 'règle générée automatiquement lors de la création de cette variable');
INSERT INTO arc.ihm_mapping_regle VALUES (50, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'prenom1', '{@prenom_distinct@}[1]', '(public.array_agg_distinct(row(i_p01_3, v_p01_3)::cle_valeur) over (partition by i_personnephysique))[1]');
INSERT INTO arc.ihm_mapping_regle VALUES (51, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'prenom2', '{@prenom_distinct@}[2]', '(public.array_agg_distinct(row(i_p01_3, v_p01_3)::cle_valeur) over (partition by i_personnephysique))[2]');
INSERT INTO arc.ihm_mapping_regle VALUES (52, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'prenom4', '{@prenom_distinct@}[4]', '(public.array_agg_distinct(row(i_p01_3, v_p01_3)::cle_valeur) over (partition by i_personnephysique))[4]');
INSERT INTO arc.ihm_mapping_regle VALUES (248, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'rsi_radiation_ind', 'null', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (54, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'prenom3', '{@prenom_distinct@}[3]', '(public.array_agg_distinct(row(i_p01_3, v_p01_3)::cle_valeur) over (partition by i_personnephysique))[3]');
INSERT INTO arc.ihm_mapping_regle VALUES (56, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'dirigeant_nais_com_lib', 'unnest((public.array_agg_distinct(row(#i_personnephysiquedirigeante#, #v_d03_4_personnephysiquedirigeante#)::cle_valeur) over (partition by {i_liasse}))||(public.array_agg_distinct(row(#i_gmc#,#v_d03_4_gmc#)::cle_valeur) over (partition by {i_liasse}))||(public.array_agg_distinct(row(#i_ide#,#v_e95_4#)::cle_valeur) over (partition by {i_liasse})))', 'règle générée automatiquement lors de la création de cette variable');
INSERT INTO arc.ihm_mapping_regle VALUES (57, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'dirigeant_nais_date', 'unnest((public.array_agg_distinct(row(#i_personnephysiquedirigeante#, #v_d03_1_personnephysiquedirigeante#)::cle_valeur) over (partition by {i_liasse}))||(public.array_agg_distinct(row(#i_gmc#,#v_d03_1_gmc#)::cle_valeur) over (partition by {i_liasse}))||(public.array_agg_distinct(row(#i_ide#,#v_e95_1#)::cle_valeur) over (partition by {i_liasse})))', 'règle générée automatiquement lors de la création de cette variable');
INSERT INTO arc.ihm_mapping_regle VALUES (59, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'dirigeant_nais_pays_lib', 'unnest((public.array_agg_distinct(row(#i_personnephysiquedirigeante#, #v_d03_3_personnephysiquedirigeante#)::cle_valeur) over (partition by {i_liasse}))||(public.array_agg_distinct(row(#i_gmc#,#v_d03_3_gmc#)::cle_valeur) over (partition by {i_liasse}))||(public.array_agg_distinct(row(#i_ide#,#v_e95_3#)::cle_valeur) over (partition by {i_liasse})))', 'règle générée automatiquement lors de la création de cette variable');
INSERT INTO arc.ihm_mapping_regle VALUES (74, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'situation', 'unnest((public.array_agg_distinct(row(#i_personnephysiquedirigeante#, #v_u50_1_personnephysiquedirigeante#)::cle_valeur) over (partition by {i_liasse}))||(public.array_agg_distinct(row(#i_gmc#,#v_u50_1_gmc#)::cle_valeur) over (partition by {i_liasse}))||(public.array_agg_distinct(row(#i_ide#,#v_e91_1#)::cle_valeur) over (partition by {i_liasse})))', 'unnest(array[{v_u50_1_PersonnePhysiqueDirigeante},{v_u50_1_GMC},{v_e91_1}])');
INSERT INTO arc.ihm_mapping_regle VALUES (75, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'qualite_lib2', 'null', 'règle générée automatiquement lors de la création de cette variable');
INSERT INTO arc.ihm_mapping_regle VALUES (77, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'dirigeant_nais_depcom', 'unnest((public.array_agg_distinct(row(#i_personnephysiquedirigeante#, #v_d03_2_personnephysiquedirigeante#)::cle_valeur) over (partition by {i_liasse}))||(public.array_agg_distinct(row(#i_gmc#,#v_d03_2_gmc#)::cle_valeur) over (partition by {i_liasse}))||(public.array_agg_distinct(row(#i_ide#,#v_e95_2#)::cle_valeur) over (partition by {i_liasse})))', 'règle générée automatiquement lors de la création de cette variable');
INSERT INTO arc.ihm_mapping_regle VALUES (80, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'numero_telephone', 'null', 'règle générée automatiquement lors de la création de cette variable');
INSERT INTO arc.ihm_mapping_regle VALUES (81, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'situation_date', 'unnest((public.array_agg_distinct(row(#i_personnephysiquedirigeante#, #v_u50_2_personnephysiquedirigeante#)::cle_valeur) over (partition by {i_liasse}))||(public.array_agg_distinct(row(#i_gmc#,#v_u50_2_gmc#)::cle_valeur) over (partition by {i_liasse}))||(public.array_agg_distinct(row(#i_ide#,#v_e91_2#)::cle_valeur) over (partition by {i_liasse})))', 'unnest(array[{v_u50_2_PersonnePhysiqueDirigeante},{v_u50_2_GMC},{v_e91_2}])');
INSERT INTO arc.ihm_mapping_regle VALUES (82, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'dirigeant_prenom2', 'null', 'règle générée automatiquement lors de la création de cette variable');
INSERT INTO arc.ihm_mapping_regle VALUES (76, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'dirigeant_prenom1', 'null', 'règle générée automatiquement lors de la création de cette variable');
INSERT INTO arc.ihm_mapping_regle VALUES (91, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'destinataire_denom', 'unnest(array[{v_c36},null,null,null])', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (107, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'distsp', 'unnest(array[{v_c37_7},{v_p04_7},{v_u11_7},{v_e02_7},{v_e12_7_etablissement}])', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (128, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'voie_lib', 'unnest(array[{v_c37_12},{v_p04_12},{v_u11_12},{v_e02_12},{v_e12_12_etablissement}])', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (212, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'obs_code', '{v_c44_1_1}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (252, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'diff_donnee_insee', 'null', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (292, 'v2008-11', '2020-01-01', '2100-01-01', 'vTestMapping', 'A', 'regist_eur_lieu', 'null', NULL);


--
-- TOC entry 7343 (class 0 OID 17156)
-- Dependencies: 296
-- Data for Name: ihm_mod_table_metier; Type: TABLE DATA; Schema: arc; Owner: -
--

INSERT INTO arc.ihm_mod_table_metier VALUES ('SIRENE4', 'mapping_sirene4_evenement_ok', NULL);
INSERT INTO arc.ihm_mod_table_metier VALUES ('SIRENE4', 'mapping_sirene4_service_ok', NULL);
INSERT INTO arc.ihm_mod_table_metier VALUES ('SIRENE4', 'mapping_sirene4_adresse_ok', NULL);
INSERT INTO arc.ihm_mod_table_metier VALUES ('SIRENE4', 'mapping_sirene4_identification_pp_ok', NULL);
INSERT INTO arc.ihm_mod_table_metier VALUES ('SIRENE4', 'mapping_sirene4_dirigeant_ok', NULL);
INSERT INTO arc.ihm_mod_table_metier VALUES ('SIRENE4', 'mapping_sirene4_observations_ok', NULL);


--
-- TOC entry 7344 (class 0 OID 17169)
-- Dependencies: 297
-- Data for Name: ihm_mod_variable_metier; Type: TABLE DATA; Schema: arc; Owner: -
--

INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_identification_pp_ok', 'sexe', 'text', 'sexe de la pesonne physique', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_identification_pp_ok', 'nais_date', 'text', 'date de naissance de la personne physique', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_identification_pp_ok', 'artiste_auteur', 'text', 'Activité d’artiste-auteur', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_identification_pp_ok', 'nom_anc', 'text', 'ancien nom de naissance de la personne physique', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_identification_pp_ok', 'nom_pseudo_anc', 'text', 'ancien pseudonyme de la personne physique', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_evenement_ok', 'id_evenement', 'bigint', 'identifiant evenement', '{exclus}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_adresse_ok', 'id_adresse', 'bigint', 'identifiant adresse', '{exclus}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_service_ok', 'evenement_agrege', 'text', 'evenement agrege pour le workflow', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_service_ok', 'liasse_incomp', 'bigint', 'signalement d’incomplétude', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_identification_pp_ok', 'prenom1_anc', 'text', 'ancien premier prénom de la personne physique', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_service_ok', 'liasse_artisan_activ', 'text', 'activité artisanale à titre principal', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_identification_pp_ok', 'prenom4', 'text', 'quatrième prénom de la personne physique', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_identification_pp_ok', 'nom_usage_anc', 'text', 'acnien nom d''usage de la personne physique', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_service_ok', 'rsi_radiation_ind', 'text', 'formalité suite à radiation d’office du RSI', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_service_ok', 'corr_mel', 'text', 'adresse mél pour les relations administratives', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_service_ok', 'domicil_denom', 'text', 'Nom du domiciliataire', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_service_ok', 'corr_tel1', 'text', 'numéro de téléphone n°1 pour les relations administratives', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_service_ok', 'domicil_legal', 'text', 'adresse de l’entreprise au domicile personnel', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_service_ok', 'regist_eur_lieu', 'text', 'lieu de l’immatriculation sur un registre public', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_service_ok', 'objet_lib', 'text', 'objet de l’entreprise', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_adresse_ok', 'dist_lib', 'text', 'libellé de localité ou de bureau distributeur', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_observations_ok', 'obs_valeur', 'text', 'valeur rubrique observation', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_identification_pp_ok', 'prenom1', 'text', 'premier prénom de la personne physique', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_observations_ok', 'date_heure_ajout', 'timestamp without time zone', 'timestamp', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_service_ok', 'date_heure_ajout', 'timestamp without time zone', 'timestamp', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_dirigeant_ok', 'date_heure_ajout', 'timestamp without time zone', 'timestamp', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_observations_ok', 'id_source', 'text', 'nom du fichier source', '{exclus}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_evenement_ok', 'id_source', 'text', 'nom du fichier source', '{exclus}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_evenement_ok', 'numero_liasse', 'text', 'numéro de liasse', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_identification_pp_ok', 'prenom2', 'text', 'deuxième prénom de la personne physique', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_identification_pp_ok', 'prenom4_anc', 'text', 'ancien quatrième prénom de la personne physique', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_identification_pp_ok', 'nationalite', 'text', 'nationalité de la personne physique', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_service_ok', 'numero_liasse', 'text', 'numéro de liasse', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_observations_ok', 'numero_liasse', 'text', 'numéro de liasse', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_adresse_ok', 'numero_liasse', 'text', 'numéro de liasse', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_identification_pp_ok', 'numero_liasse', 'text', 'numéro de liasse', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_service_ok', 'fiscal_liberat_opt', 'text', 'option pour le versement libératoire', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_service_ok', 'numero_telephone', 'text', 'numéro de téléphone pour les relations administratives', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_identification_pp_ok', 'forain', 'text', 'Qualité de non sédentarité', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_service_ok', 'id_service', 'bigint', 'identifiant service', '{exclus}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_identification_pp_ok', 'micro_entrepreneur', 'text', 'Micro-entrepreneur', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_service_ok', 'type_liasse', 'text', 'type de liasse', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_service_ok', 'observations', 'text', 'observations', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_identification_pp_ok', 'siren_anc', 'text', 'SIREN de l’ancienne identité', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_identification_pp_ok', 'id_source', 'text', 'nom du fichier source', '{exclus}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_identification_pp_ok', 'cess_pp_motif', 'boolean', 'Cessation consécutive au décès de l’exploitant', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_dirigeant_ok', 'id_dirigeant', 'bigint', 'identifiant dirigeant', '{exclus}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_service_ok', 'liasse_numero_ant
liasse_numero_ant
liasse_numero_ant', 'text', 'rappel du numéro de liasse antérieure', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_service_ok', 'diff_donnee_insee', 'text', 'diffusion des données Insee', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_service_ok', 'corr_tel2', 'text', 'numéro de téléphone n°2 pour les relations administratives', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_adresse_ok', 'depcom', 'text', 'code officiel géographique (commune ou pays)', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_service_ok', 'decl_obs', 'text', 'observations', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_service_ok', 'domicil_siren', 'text', 'SIREN du domiciliataire', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_service_ok', 'regist_eur_id', 'text', 'numéro d’immatriculation sur un registre public', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_service_ok', 'activ_pr_lib', 'text', 'activités principales de l’entreprise', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_service_ok', 'activ_nat', 'text', 'nature des activités de l’entreprise', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_observations_ok', 'id_observations', 'text', 'identifiant observations', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_observations_ok', 'obs_code', 'text', 'code rubrique observation', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_identification_pp_ok', 'id_identification_pp', 'bigint', 'identifiant identification_pp', '{exclus}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_service_ok', 'bru_loc_dpb', 'boolean', 'loueur de Droit à Paiement de Base pour les biens ruraux', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_identification_pp_ok', 'nom_usage', 'text', 'nom d''usage de la personne physique', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_adresse_ok', 'date_heure_ajout', 'timestamp without time zone', 'timestamp', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_evenement_ok', 'date_heure_ajout', 'timestamp without time zone', 'timestamp', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_identification_pp_ok', 'date_heure_ajout', 'timestamp without time zone', 'timestamp', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_identification_pp_ok', 'prenom2_anc', 'text', 'ancien deuxième prénom de la personne physique', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_adresse_ok', 'id_source', 'text', 'nom du fichier source', '{exclus}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_dirigeant_ok', 'numero_liasse', 'text', 'numéro de liasse', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_service_ok', 'liasse_decl_date', 'date', 'date de présentation de la déclaration au CFE', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_identification_pp_ok', 'prenom3', 'text', 'troisième prénom de la personne physique', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_identification_pp_ok', 'prenom3_anc', 'text', 'ancien troisième prénom de la personne physique', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_service_ok', 'liasse_trans_date', 'date', 'Date de transmission
date de transmission', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_service_ok', 'courriel', 'text', 'adresse courriel pour les relations administratives', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_dirigeant_ok', 'situation_date', 'date', 'date d''agrément', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_dirigeant_ok', 'qualite_anc1', 'text', 'qualité ancienne n°1', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_service_ok', 'siren', 'text', 'SIREN de l’entreprise', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_dirigeant_ok', 'situation', 'bigint', 'code situation', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_dirigeant_ok', 'qualite1', 'text', 'qualité actuelle ou nouvelle n°1', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_dirigeant_ok', 'qualite2', 'text', 'qualité actuelle ou nouvelle n°2', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_dirigeant_ok', 'qualite_anc2', 'text', 'qualité ancienne n°2', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_dirigeant_ok', 'qualite_lib1', 'text', 'libellé n°1 d''une autre qualité actuelle', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_dirigeant_ok', 'qualite_lib2', 'text', 'libellé n°2 d''une autre qualité actuelle', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_dirigeant_ok', 'nature', 'text', 'nature de la personne membre', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_dirigeant_ok', 'dirigeant_sexe', 'text', 'sexe', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_dirigeant_ok', 'dirigeant_nom', 'text', 'null', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_dirigeant_ok', 'dirigeant_prenom1', 'text', 'null', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_dirigeant_ok', 'dirigeant_prenom2', 'text', 'null', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_dirigeant_ok', 'dirigeant_prenom3', 'text', 'null', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_dirigeant_ok', 'dirigeant_prenom4', 'text', 'null', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_dirigeant_ok', 'dirigeant_nom_usage', 'text', 'null', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_dirigeant_ok', 'dirigeant_pseudo', 'text', 'null', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_dirigeant_ok', 'dirigeant_nais_date', 'text', 'null', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_service_ok', 'id_source', 'text', 'nom du fichier source', '{exclus}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_dirigeant_ok', 'dirigeant_nais_depcom', 'text', 'null', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_dirigeant_ok', 'dirigeant_nais_pays_lib', 'text', 'null', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_dirigeant_ok', 'dirigeant_nais_com_lib', 'text', 'null', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_dirigeant_ok', 'id_source', 'text', 'nom du fichier source', '{exclus}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_dirigeant_ok', 'dirigeant_situation_date', 'date', 'code situation', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_evenement_ok', 'evenement_date', 'date', 'date evenement', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_evenement_ok', 'evenement_autre', 'text', 'libelle autre evenement', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_adresse_ok', 'adresse_type', 'text', 'type d''adresse (corresp, dom...) ', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_adresse_ok', 'destinataire_denom', 'text', 'destinataire', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_adresse_ok', 'rivoli', 'text', 'code rivoli', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_adresse_ok', 'voie_num', 'text', 'libellé de voie ou de lieu-dit', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_adresse_ok', 'voie_repet', 'text', 'indice de répétition', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_adresse_ok', 'distsp', 'text', 'distribution spéciale - boîte  postale, service X, secteur postal', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_adresse_ok', 'post', 'text', 'code postal', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_adresse_ok', 'compl', 'text', 'complément de localisation', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_adresse_ok', 'voie_type', 'text', 'type de voie abrégé', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_adresse_ok', 'voie_lib', 'text', 'libellé de voie ou de lieu-dit', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_adresse_ok', 'com_lib', 'text', 'libellé de commune', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_adresse_ok', 'pays_lib', 'text', 'libellé de pays', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_evenement_ok', 'evenement_type', 'text', 'code evenement', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_identification_pp_ok', 'nom', 'text', 'nom de naissance de la personne physique', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_identification_pp_ok', 'nom_pseudo', 'text', 'pseudonyme de la personne physique', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_identification_pp_ok', 'nais_depcom', 'text', 'code officiel géographique de la commune de naissance de la pp', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_identification_pp_ok', 'nais_pays_lib', 'text', 'pays de naissance de la personne physique', 'null');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('SIRENE4', 'mapping_sirene4_identification_pp_ok', 'nais_com_lib', 'text', 'libellé de la commune de naissance de la personne physique', 'null');


--
-- TOC entry 7345 (class 0 OID 17182)
-- Dependencies: 298
-- Data for Name: ihm_nmcl; Type: TABLE DATA; Schema: arc; Owner: -
--

INSERT INTO arc.ihm_nmcl VALUES ('nmcl_rubriqueentete_v001', NULL);
INSERT INTO arc.ihm_nmcl VALUES ('nmcl_evenements_v001', 'Code évènement (V2008.11)');
INSERT INTO arc.ihm_nmcl VALUES ('nmcl_evenements_v002', 'Code évènement (V2016.02)');
INSERT INTO arc.ihm_nmcl VALUES ('nmcl_cog_v004', 'COG année 2023');


--
-- TOC entry 7339 (class 0 OID 17104)
-- Dependencies: 292
-- Data for Name: ihm_normage_regle; Type: TABLE DATA; Schema: arc; Owner: -
--


--
-- TOC entry 7346 (class 0 OID 17190)
-- Dependencies: 299
-- Data for Name: ihm_schema_nmcl; Type: TABLE DATA; Schema: arc; Owner: -
--

INSERT INTO arc.ihm_schema_nmcl VALUES ('nmcl_supportdec', 'code', 'text');
INSERT INTO arc.ihm_schema_nmcl VALUES ('nmcl_supportdec', 'libellé', 'text');
INSERT INTO arc.ihm_schema_nmcl VALUES ('nmcl_rubriqueentete', 'rubrique', 'text');
INSERT INTO arc.ihm_schema_nmcl VALUES ('nmcl_evenements', 'code', 'text');
INSERT INTO arc.ihm_schema_nmcl VALUES ('nmcl_evenements', 'libellé', 'text');
INSERT INTO arc.ihm_schema_nmcl VALUES ('nmcl_cog', 'code', 'text');
INSERT INTO arc.ihm_schema_nmcl VALUES ('nmcl_cog', 'libelle', 'text');


--
-- TOC entry 7361 (class 0 OID 12416756)
-- Dependencies: 429
-- Data for Name: ihm_ws_context; Type: TABLE DATA; Schema: arc; Owner: -
--

INSERT INTO arc.ihm_ws_context VALUES ('SIRENE4', 1, 1, 'arc.bas1', '4', 'v2008-11', '2020-01-01', 'A');
INSERT INTO arc.ihm_ws_context VALUES ('SIRENE4', 1, 3, 'arc.bas5', '6', 'v2008-11', '2020-01-01', 'A');


--
-- TOC entry 7364 (class 0 OID 12416845)
-- Dependencies: 432
-- Data for Name: ihm_ws_query; Type: TABLE DATA; Schema: arc; Owner: -
--

INSERT INTO arc.ihm_ws_query VALUES (5, 'tablePP', 'SELECT * FROM mapping_sirene4_identification_pp_ok where date_heure_ajout=(select max(date_heure_ajout) from mapping_sirene4_identification_pp_ok)', 1, 'SIRENE4', 3);
INSERT INTO arc.ihm_ws_query VALUES (1, 'reglesEnErreur', 'SELECT distinct(id_source) as nomFichier, v_c02 as numeroDeclaration, array(select distinct(unnest(brokenrules)) from E) as listeReglesKo FROM E', 1, 'SIRENE4', 1);
INSERT INTO arc.ihm_ws_query VALUES (2, 'detailRegles', 'SELECT id_regle as numeroRegle, id_classe as typeRegle, commentaire as message FROM CONTROLE_REGLE where id_regle::text in (select distinct(unnest(brokenrules)) from E where brokenrules is not null) and id_norme in (select distinct(id_norme) from E) order by numeroRegle', 1, 'SIRENE4', 1);
INSERT INTO arc.ihm_ws_query VALUES (1, 'tableService', 'SELECT * FROM mapping_sirene4_service_ok where date_heure_ajout=(select max(date_heure_ajout) from mapping_sirene4_service_ok)', 1, 'SIRENE4', 3);
INSERT INTO arc.ihm_ws_query VALUES (2, 'tableEvenement', 'SELECT * FROM mapping_sirene4_evenement_ok where date_heure_ajout=(select max(date_heure_ajout) from mapping_sirene4_evenement_ok)', NULL, 'SIRENE4', 3);
INSERT INTO arc.ihm_ws_query VALUES (3, 'tableAdresse', 'SELECT * FROM mapping_sirene4_adresse_ok where date_heure_ajout=(select max(date_heure_ajout) from mapping_sirene4_adresse_ok)', NULL, 'SIRENE4', 3);
INSERT INTO arc.ihm_ws_query VALUES (4, 'tableObs', 'SELECT * FROM mapping_sirene4_rubrique_obs_ok where date_heure_ajout=(select max(date_heure_ajout) from mapping_sirene4_rubrique_obs_ok)', NULL, 'SIRENE4', 3);


--
-- TOC entry 7356 (class 0 OID 547752)
-- Dependencies: 314
-- Data for Name: nmcl_cj_v001; Type: TABLE DATA; Schema: arc; Owner: -
--

INSERT INTO arc.nmcl_cj_v001 VALUES ('.', 'Non renseigné');
INSERT INTO arc.nmcl_cj_v001 VALUES ('1000', 'Entrepreneur individuel');
INSERT INTO arc.nmcl_cj_v001 VALUES ('2110', 'Indivision entre personnes physiques ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('2120', 'Indivision avec personne morale ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('2210', 'Société créée de fait entre personnes physiques ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('2220', 'Société créée de fait avec personne morale ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('2310', 'Société en participation entre personnes physiques ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('2320', 'Société en participation avec personne morale ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('2385', 'Société en participation de professions libérales ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('2400', 'Fiducie ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('2700', 'Paroisse hors zone concordataire ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('2900', 'Autre groupement de droit privé non doté de la personnalité morale ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('3110', 'Représentation ou agence commerciale d''état ou organisme public étranger immatriculé au RCS ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('3120', 'Société commerciale étrangère immatriculée au RCS');
INSERT INTO arc.nmcl_cj_v001 VALUES ('3205', 'Organisation internationale ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('3210', 'État, collectivité ou établissement public étranger');
INSERT INTO arc.nmcl_cj_v001 VALUES ('3220', 'Société étrangère non immatriculée au RCS ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('3290', 'Autre personne morale de droit étranger ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('4110', 'Établissement public national à caractère industriel ou commercial doté d''un comptable public ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('4120', 'Établissement public national à caractère industriel ou commercial non doté d''un comptable public ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('4130', 'Exploitant public ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('4140', 'Établissement public local à caractère industriel ou commercial ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('4150', 'Régie d''une collectivité locale à caractère industriel ou commercial ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('4160', 'Institution Banque de France ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5191', 'Société de caution mutuelle ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5192', 'Société coopérative de banque populaire ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5193', 'Caisse de crédit maritime mutuel ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5194', 'Caisse (fédérale) de crédit mutuel ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5195', 'Association coopérative inscrite (droit local Alsace Moselle) ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5196', 'Caisse d''épargne et de prévoyance à forme coopérative ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5202', 'Société en nom collectif ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5203', 'Société en nom collectif coopérative ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5306', 'Société en commandite simple ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5307', 'Société en commandite simple coopérative ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5308', 'Société en commandite par actions ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5309', 'Société en commandite par actions coopérative ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5370', 'Société de Participations Financières de Profession Libérale Société en commandite par actions (SPFPL SCA)');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5385', 'Société d''exercice libéral en commandite par actions ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5410', 'SARL nationale ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5415', 'SARL d''économie mixte ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5422', 'SARL immobilière pour le commerce et l''industrie (SICOMI) ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5426', 'SARL immobilière de gestion');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5430', 'SARL d''aménagement foncier et d''équipement rural (SAFER)');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5431', 'SARL mixte d''intérêt agricole (SMIA) ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5432', 'SARL d''intérêt collectif agricole (SICA) ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5442', 'SARL d''attribution ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5443', 'SARL coopérative de construction ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5451', 'SARL coopérative de consommation ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5453', 'SARL coopérative artisanale ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5454', 'SARL coopérative d''intérêt maritime ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5455', 'SARL coopérative de transport');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5458', 'SARL coopérative ouvrière de production (SCOP)');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5459', 'SARL union de sociétés coopératives ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5460', 'Autre SARL coopérative ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5470', 'Société de Participations Financières de Profession Libérale Société à responsabilité limitée (SPFPL SARL)');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5485', 'Société d''exercice libéral à responsabilité limitée ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5498', 'SARL unipersonnelle ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5499', 'Société à responsabilité limitée (sans autre indication)');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5505', 'SA à participation ouvrière à conseil d''administration ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5510', 'SA nationale à conseil d''administration ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5515', 'SA d''économie mixte à conseil d''administration ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5520', 'Fonds à forme sociétale à conseil d''administration');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5522', 'SA immobilière pour le commerce et l''industrie (SICOMI) à conseil d''administration');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5525', 'SA immobilière d''investissement à conseil d''administration');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5530', 'SA d''aménagement foncier et d''équipement rural (SAFER) à conseil d''administration');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5531', 'Société anonyme mixte d''intérêt agricole (SMIA) à conseil d''administration ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5532', 'SA d''intérêt collectif agricole (SICA) à conseil d''administration');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5542', 'SA d''attribution à conseil d''administration');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5543', 'SA coopérative de construction à conseil d''administration');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5546', 'SA de HLM à conseil d''administration ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5547', 'SA coopérative de production de HLM à conseil d''administration ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5548', 'SA de crédit immobilier à conseil d''administration ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5551', 'SA coopérative de consommation à conseil d''administration ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5552', 'SA coopérative de commerçants-détaillants à conseil d''administration');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5553', 'SA coopérative artisanale à conseil d''administration ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5554', 'SA coopérative (d''intérêt) maritime à conseil d''administration ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5555', 'SA coopérative de transport à conseil d''administration');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5558', 'SA coopérative ouvrière de production (SCOP) à conseil d''administration');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5559', 'SA union de sociétés coopératives à conseil d''administration ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5560', 'Autre SA coopérative à conseil d''administration ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5570', 'Société de Participations Financières de Profession Libérale Société anonyme à conseil d''administration (SPFPL SA à conseil d''administration)');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5585', 'Société d''exercice libéral à forme anonyme à conseil d''administration ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5599', 'SA à conseil d''administration (s.a.i.)');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5605', 'SA à participation ouvrière à directoire ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5610', 'SA nationale à directoire ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5615', 'SA d''économie mixte à directoire ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5620', 'Fonds à forme sociétale à directoire');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5622', 'SA immobilière pour le commerce et l''industrie (SICOMI) à directoire');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5625', 'SA immobilière d''investissement à directoire ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5630', 'Safer anonyme à directoire ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5631', 'SA mixte d''intérêt agricole (SMIA)');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5632', 'SA d''intérêt collectif agricole (SICA)');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5642', 'SA d''attribution à directoire');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5643', 'SA coopérative de construction à directoire');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5646', 'SA de HLM à directoire');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5647', 'Société coopérative de production de HLM anonyme à directoire ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5648', 'SA de crédit immobilier à directoire ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5651', 'SA coopérative de consommation à directoire ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5652', 'SA coopérative de commerçants-détaillants à directoire');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5653', 'SA coopérative artisanale à directoire ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5654', 'SA coopérative d''intérêt maritime à directoire ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5655', 'SA coopérative de transport à directoire ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5658', 'SA coopérative ouvrière de production (SCOP) à directoire');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5659', 'SA union de sociétés coopératives à directoire ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5660', 'Autre SA coopérative à directoire');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5670', 'Société de Participations Financières de Profession Libérale Société anonyme à Directoire (SPFPL SA à directoire)');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5685', 'Société d''exercice libéral à forme anonyme à directoire ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5699', 'SA à directoire (s.a.i.)');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5710', 'SAS, société par actions simplifiée');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5720', 'Société par actions simplifiée à associé unique ou société par actions simplifiée unipersonnelle ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5770', 'Société de Participations Financières de Profession Libérale Société par actions simplifiée (SPFPL SAS)');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5785', 'Société d''exercice libéral par action simplifiée ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('5800', 'Société européenne ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('6100', 'Caisse d''Épargne et de Prévoyance ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('6210', 'Groupement européen d''intérêt économique (GEIE) ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('6220', 'Groupement d''intérêt économique (GIE) ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('6316', 'Coopérative d''utilisation de matériel agricole en commun (CUMA) ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('6317', 'Société coopérative agricole ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('6318', 'Union de sociétés coopératives agricoles ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('6411', 'Société d''assurance à forme mutuelle');
INSERT INTO arc.nmcl_cj_v001 VALUES ('6511', 'Sociétés Interprofessionnelles de Soins Ambulatoires ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('6521', 'Société civile de placement collectif immobilier (SCPI) ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('6532', 'Société civile d''intérêt collectif agricole (SICA) ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('6533', 'Groupement agricole d''exploitation en commun (GAEC) ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('6534', 'Groupement foncier agricole ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('6535', 'Groupement agricole foncier ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('6536', 'Groupement forestier ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('6537', 'Groupement pastoral ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('6538', 'Groupement foncier et rural');
INSERT INTO arc.nmcl_cj_v001 VALUES ('6539', 'Société civile foncière ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('6540', 'Société civile immobilière ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('6541', 'Société civile immobilière de construction-vente');
INSERT INTO arc.nmcl_cj_v001 VALUES ('6542', 'Société civile d''attribution ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('6543', 'Société civile coopérative de construction ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('6544', 'Société civile immobilière d'' accession progressive à la propriété');
INSERT INTO arc.nmcl_cj_v001 VALUES ('6551', 'Société civile coopérative de consommation ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('6554', 'Société civile coopérative d''intérêt maritime ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('6558', 'Société civile coopérative entre médecins ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('6560', 'Autre société civile coopérative ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('6561', 'SCP d''avocats ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('6562', 'SCP d''avocats aux conseils ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('6563', 'SCP d''avoués d''appel ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('6564', 'SCP d''huissiers ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('6565', 'SCP de notaires ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('6566', 'SCP de commissaires-priseurs ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('6567', 'SCP de greffiers de tribunal de commerce ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('6568', 'SCP de conseils juridiques ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('6569', 'SCP de commissaires aux comptes ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('6571', 'SCP de médecins ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('6572', 'SCP de dentistes ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('6573', 'SCP d''infirmiers ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('6574', 'SCP de masseurs-kinésithérapeutes');
INSERT INTO arc.nmcl_cj_v001 VALUES ('6575', 'SCP de directeurs de laboratoire d''analyse médicale ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('6576', 'SCP de vétérinaires ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('6577', 'SCP de géomètres experts');
INSERT INTO arc.nmcl_cj_v001 VALUES ('6578', 'SCP d''architectes ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('6585', 'Autre société civile professionnelle');
INSERT INTO arc.nmcl_cj_v001 VALUES ('6588', 'Société civile laitière ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('6589', 'Société civile de moyens ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('6595', 'Caisse locale de crédit mutuel ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('6596', 'Caisse de crédit agricole mutuel ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('6597', 'Société civile d''exploitation agricole ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('6598', 'Exploitation agricole à responsabilité limitée ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('6599', 'Autre société civile ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('6901', 'Autre personne de droit privé inscrite au registre du commerce et des sociétés');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7111', 'Autorité constitutionnelle ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7112', 'Autorité administrative ou publique indépendante');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7113', 'Ministère ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7120', 'Service central d''un ministère ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7150', 'Service du ministère de la Défense ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7160', 'Service déconcentré à compétence nationale d''un ministère (hors Défense)');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7171', 'Service déconcentré de l''État à compétence (inter) régionale ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7172', 'Service déconcentré de l''État à compétence (inter) départementale ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7179', '(Autre) Service déconcentré de l''État à compétence territoriale ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7190', 'Ecole nationale non dotée de la personnalité morale ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7210', 'Commune et commune nouvelle ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7220', 'Département ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7225', 'Collectivité et territoire d''Outre Mer');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7229', '(Autre) Collectivité territoriale ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7230', 'Région ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7312', 'Commune associée et commune déléguée ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7313', 'Section de commune ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7314', 'Ensemble urbain ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7321', 'Association syndicale autorisée ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7322', 'Association foncière urbaine ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7323', 'Association foncière de remembrement ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7331', 'Établissement public local d''enseignement ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7340', 'Pôle métropolitain');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7341', 'Secteur de commune ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7342', 'District urbain ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7343', 'Communauté urbaine ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7344', 'Métropole');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7345', 'Syndicat intercommunal à vocation multiple (SIVOM) ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7346', 'Communauté de communes ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7347', 'Communauté de villes ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7348', 'Communauté d''agglomération ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7349', 'Autre établissement public local de coopération non spécialisé ou entente ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7351', 'Institution interdépartementale ou entente');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7352', 'Institution interrégionale ou entente ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7353', 'Syndicat intercommunal à vocation unique (SIVU) ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7354', 'Syndicat mixte fermé ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7355', 'Syndicat mixte ouvert');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7356', 'Commission syndicale pour la gestion des biens indivis des communes ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7357', 'Pôle d''équilibre territorial et rural (PETR)');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7361', 'Centre communal d''action sociale ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7362', 'Caisse des écoles ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7363', 'Caisse de crédit municipal ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7364', 'Établissement d''hospitalisation ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7365', 'Syndicat inter hospitalier ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7366', 'Établissement public local social et médico-social ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7367', 'Centre Intercommunal d''action sociale (CIAS)');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7371', 'Office public d''habitation à loyer modéré (OPHLM) ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7372', 'Service départemental d''incendie et de secours (SDIS)');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7373', 'Établissement public local culturel ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7378', 'Régie d''une collectivité locale à caractère administratif ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7379', '(Autre) Établissement public administratif local ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7381', 'Organisme consulaire ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7382', 'Établissement public national ayant fonction d''administration centrale ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7383', 'Établissement public national à caractère scientifique culturel et professionnel ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7384', 'Autre établissement public national d''enseignement ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7385', 'Autre établissement public national administratif à compétence territoriale limitée ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7389', 'Établissement public national à caractère administratif ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7410', 'Groupement d''intérêt public (GIP) ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7430', 'Établissement public des cultes d''Alsace-Lorraine ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7450', 'Etablissement public administratif, cercle et foyer dans les armées ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7470', 'Groupement de coopération sanitaire à gestion publique ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('7490', 'Autre personne morale de droit administratif ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('8110', 'Régime général de la Sécurité Sociale');
INSERT INTO arc.nmcl_cj_v001 VALUES ('8120', 'Régime spécial de Sécurité Sociale');
INSERT INTO arc.nmcl_cj_v001 VALUES ('8130', 'Institution de retraite complémentaire ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('8140', 'Mutualité sociale agricole ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('8150', 'Régime maladie des non-salariés non agricoles ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('8160', 'Régime vieillesse ne dépendant pas du régime général de la Sécurité Sociale');
INSERT INTO arc.nmcl_cj_v001 VALUES ('8170', 'Régime d''assurance chômage ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('8190', 'Autre régime de prévoyance sociale ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('8210', 'Mutuelle ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('8250', 'Assurance mutuelle agricole ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('8290', 'Autre organisme mutualiste ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('8310', 'Comité central d''entreprise ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('8311', 'Comité d''établissement ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('8410', 'Syndicat de salariés ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('8420', 'Syndicat patronal ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('8450', 'Ordre professionnel ou assimilé ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('8470', 'Centre technique industriel ou comité professionnel du développement économique ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('8490', 'Autre organisme professionnel ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('8510', 'Institution de prévoyance ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('8520', 'Institution de retraite supplémentaire ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('9110', 'Syndicat de copropriété ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('9150', 'Association syndicale libre ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('9210', 'Association non déclarée ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('9220', 'Association déclarée ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('9221', 'Association déclarée d''insertion par l''économique');
INSERT INTO arc.nmcl_cj_v001 VALUES ('9222', 'Association intermédiaire ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('9223', 'Groupement d''employeurs ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('9224', 'Association d''avocats à responsabilité professionnelle individuelle');
INSERT INTO arc.nmcl_cj_v001 VALUES ('9230', 'Association déclarée, reconnue d''utilité publique');
INSERT INTO arc.nmcl_cj_v001 VALUES ('9240', 'Congrégation ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('9260', 'Association de droit local (Bas-Rhin, Haut-Rhin et Moselle)');
INSERT INTO arc.nmcl_cj_v001 VALUES ('9300', 'Fondation ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('9900', 'Autre personne morale de droit privé ');
INSERT INTO arc.nmcl_cj_v001 VALUES ('9970', 'Groupement de coopération sanitaire à gestion privée ');

--
-- TOC entry 7379 (class 0 OID 326399714)
-- Dependencies: 1184
-- Data for Name: nmcl_cog_v004; Type: TABLE DATA; Schema: arc; Owner: -
--

--
-- TOC entry 7359 (class 0 OID 12024215)
-- Dependencies: 378
-- Data for Name: nmcl_evenements_v001; Type: TABLE DATA; Schema: arc; Owner: -
--

INSERT INTO arc.nmcl_evenements_v001 VALUES ('01P', 'Création d’une entreprise individuelle');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('05P', 'Création d’une entreprise individuelle, personne ayant déjà exercé une activité non salariée');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('07P', 'Inscription d’une entreprise individuelle étrangère employeur sans établissement en France');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('10P', 'Modification du nom ou du prénom de la personne');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('11P', 'Transfert de l’entreprise');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('15P', 'Modification du nom d’usage ou du pseudonyme');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('16P', 'Modification du domicile personnel');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('17P', 'Modification de la nationalité');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('20P', 'Modification de la date de début d‘activité');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('21P', 'Reprise d’activité de l’entreprise après une cessation temporaire');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('22P', 'Décès de l’exploitant avec poursuite de l’exploitation');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('23P', 'Demande de renouvellement du maintien provisoire de l’immatriculation au RCS');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('24P', 'Entrée de champ RCS, RM ou RSAC');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('27P', 'Sortie de champ du Répertoire des Métiers ou du RCS');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('25P', 'Déclaration, modification relative à l’EIRL');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('28P', 'Déclaration, modification de l’insaisissabilité');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('29P', 'Autre modification concernant la personne');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('30P', 'Modification relative au conjoint collaborateur');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('31P', 'Modification relative aux exploitants de l’indivision ou héritiers');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('36P', 'Modification relative au représentant social ou au représentant fiscal d’une entreprise personnelle étrangère employeur sans établissement en France');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('37P', 'Suppression de la mention du contrat d’appui');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('40P', 'Cessation temporaire d’activité de l’entreprise');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('41P', 'Cessation totale d’activité non salariée');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('42P', 'Décès de l’exploitant individuel sans poursuite de l’exploitation avec demande de maintien provisoire au RCS ou au RM');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('43P', 'Cessation totale d’activité avec demande de maintien provisoire au RCS ou au RM');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('44P', 'Cessation d’activité agricole avec mise en location des terres et assujettissement à la TVA pour ce bail');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('45P', 'Cessation d’activité agricole avec conservation de stocks ou de cheptel');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('46P', 'Départ en retraite avec conservation d’une exploitation de subsistance');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('47P', 'Option TVA bailleur de biens ruraux');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('01M', 'Création d’une entreprise personne morale');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('02M', 'Constitution d’une société sans activité');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('03M', 'Constitution d’une société sans activité au siège avec début d’activité hors siège');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('04M', 'Ouverture du 1er établissement en France d’une société commerciale ayant son siège à l’étranger');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('05M', 'Constitution d’une personne morale dont l’immatriculation est prévue par un texte');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('07M', 'Inscription d’une société étrangère employeur sans établissement en France');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('10M', 'Modification de l’identification de la personne morale');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('11M', 'Transfert du siège de l’entreprise');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('12M', 'Modification des principales activités de l’entreprise ou de l’objet d’un GEIE');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('13M', 'Modification de la forme juridique ou du statut particulier');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('15M', 'Modification du capital social');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('16M', 'Modification de la durée de la personne morale ou de la date de clôture de l’exercice social');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('19M', 'Changement de la nature de la gérance');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('22M', 'Dissolution');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('23M', 'Demande de prorogation de l’immatriculation au RCS');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('24M', 'Entrée de champ RCS, RM ou RSAC');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('25M', 'Continuation de l’exploitation malgré un actif net devenu inférieur à la moitié du capital');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('26M', 'Reconstitution des capitaux propres');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('27M', 'Sortie de champ du Répertoire des Métiers');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('28M', 'Transmission du patrimoine à l’associé unique restant');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('29M', 'Autre modification concernant la personne morale');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('30M', 'Modification relative aux membres d’un groupement');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('32M', 'Modification relative aux associés non gérants relevant du régime TNS MSA');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('33M', 'Modification relative aux dirigeants d’un groupement');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('34M', 'Modification relative aux dirigeants d’une société de personnes');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('35M', 'Modification relative aux dirigeants d’une SARL ou d’une société de capitaux');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('36M', 'Modification relative au représentant social ou au représentant fiscal d’une société étrangère employeur sans établissement en France');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('37M', 'Suppression de la mention du contrat d’appui');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('40M', 'Cessation totale d’activité de l’entreprise sans disparition de la personne morale');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('41M', 'Disparition de la personne morale par suite de fusion ou de scission');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('42M', 'Disparition de la personne morale');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('43M', 'Fermeture de l’établissement principal d’une société étrangère');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('44M', 'Cessation d’activité agricole avec mise en location des terres et assujettissement à la TVA pour ce bail');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('47M', 'Option TVA bailleur de biens ruraux');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('98M', 'Refus d’immatriculation au RCS');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('01F', 'Déclaration d’exploitation en commun');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('10F', 'Modification de l’identification de l’exploitation en commun');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('11F', 'Transfert de l’entreprise');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('12F', 'Modification des principales activités de l’exploitation en commun');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('20F', 'Modification de la date de début d’activité');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('30F', 'Modification relative aux membres de l’exploitation en commun');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('40F', 'Fin d’exploitation en commun');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('44F', 'Cessation d’activité agricole avec mise en location des terres et assujettissement à la TVA pour ce bail');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('45F', 'Cessation d’activité agricole avec conservation de stocks ou de cheptel');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('47F', 'Option TVA bailleur de biens ruraux');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('51M', 'Début d’activité au siège d’une entreprise sans activité ou reprise d’activité au siège après une cessation temporaire');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('52M', 'Ouverture d’un établissement par une entreprise sans activité');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('53P', 'Reprise de l’exploitation d’un fonds mis en location gérance');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('53M', 'Reprise de l’exploitation d’un fonds mis en location gérance');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('53F', 'Reprise de l’exploitation d’un fonds mis en location gérance');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('54P', 'Ouverture d’un nouvel établissement');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('54M', 'Ouverture d’un nouvel établissement');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('54F', 'Ouverture d’un nouvel établissement');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('56P', 'Transfert d’un établissement');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('56M', 'Transfert d’un établissement');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('56F', 'Transfert d’un établissement');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('60P', 'Modification de l’identification de l’établissement');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('60M', 'Modification de l’identification de l’établissement');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('60F', 'Modification de l’identification de l’établissement');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('61P', 'Adjonction d’activité');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('61M', 'Adjonction d’activité');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('61F', 'Adjonction d’activité');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('62P', 'Suppression partielle d’activité');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('62M', 'Suppression partielle d’activité');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('62F', 'Suppression partielle d’activité');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('63P', 'Acquisition du fonds par l’exploitant');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('63M', 'Acquisition du fonds par l’exploitant');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('63F', 'Acquisition du fonds par l’exploitant');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('64P', 'Renouvellement du contrat de location gérance');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('64M', 'Renouvellement du contrat de location gérance');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('64F', 'Renouvellement du contrat de location gérance');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('65P', 'Embauche d’un premier salarié dans un établissement');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('65M', 'Embauche d’un premier salarié dans un établissement');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('65F', 'Embauche d’un premier salarié dans un établissement');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('66P', 'Fin d’emploi de tout salarié dans un établissement');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('66M', 'Fin d’emploi de tout salarié dans un établissement');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('66F', 'Fin d’emploi de tout salarié dans un établissement');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('67P', 'Modification des activités de l’établissement');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('67M', 'Modification des activités de l’établissement');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('67F', 'Modification des activités de l’établissement');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('68P', 'Changement de locataire gérant');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('68M', 'Changement de locataire gérant');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('69P', 'Changement de loueur du fonds');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('69M', 'Changement de loueur du fonds');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('70P', 'Modification relative à une personne ayant le pouvoir d’engager l’établissement');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('70M', 'Modification relative à une personne ayant le pouvoir d’engager l’établissement');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('77P', 'Modification relative aux propriétaires indivis du fonds');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('80P', 'Fermeture d’un établissement');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('80M', 'Fermeture d’un établissement');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('80F', 'Fermeture d’un établissement');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('81M', 'Fin d’activité au siège, qui reste siège');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('82P', 'Mise en location gérance d’un établissement des fonds exploités');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('82M', 'Mise en location gérance d’un établissement des fonds exploités');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('82F', 'Mise en location gérance d’un établissement des fonds exploités');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('83P', 'Mise en location gérance du fonds unique sans maintien au RCS ou au RM');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('84P', 'Mise en location gérance du fonds unique avec maintien au RCS');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('84M', 'Mise en location gérance du fonds unique avec maintien au RCS');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('91P', 'Rejet de l’immatriculation au RM');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('91M', 'Rejet de l’immatriculation au RM');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('92P', 'Rejet de la demande d’inscription au RCS');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('92M', 'Rejet de la demande d’inscription au RCS');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('93P', 'Radiation d’office du RM');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('93M', 'Radiation d’office du RM');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('94P', 'Radiation d’office du RCS');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('94M', 'Radiation d’office du RCS');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('95P', 'Demande d’inscription d’un gérant majoritaire au répertoire SIRENE');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('96P', 'Cessation d’activité d’un « gérant majoritaire » ou d’un « dirigeant TNS »');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('97P', 'Changement d’adresse d’activité d’un « gérant majoritaire » ou d’un « dirigeant TNS »');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('99P', 'Correction ou complément d’une formalité');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('99M', 'Correction ou complément d’une formalité');
INSERT INTO arc.nmcl_evenements_v001 VALUES ('99F', 'Correction ou complément d’une formalité');


--
-- TOC entry 7360 (class 0 OID 12235585)
-- Dependencies: 379
-- Data for Name: nmcl_evenements_v002; Type: TABLE DATA; Schema: arc; Owner: -
--

INSERT INTO arc.nmcl_evenements_v002 VALUES ('01P', 'Création d’une entreprise individuelle');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('05P', 'Création d’une entreprise individuelle, personne ayant déjà exercé une activité non salariée');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('07P', 'Inscription d’une entreprise individuelle étrangère sans établissement en France');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('10P', 'Modification du nom ou du prénom de la personne');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('11P', 'Transfert de l’entreprise');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('15P', 'Modification du nom d’usage ou du pseudonyme');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('16P', 'Modification du domicile personnel');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('17P', 'Modification de la nationalité');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('20P', 'Modification de la date de début d‘activité');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('21P', 'Reprise d’activité de l’entreprise après une cessation temporaire');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('22P', 'Décès de l’exploitant avec poursuite de l’exploitation');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('23P', 'Demande de renouvellement du maintien provisoire de l’immatriculation au RCS ou au RM');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('24P', 'Entrée de champ RCS, RM ou RSAC');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('25P', 'Déclaration, modification relative à l’EIRL');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('27P', 'Sortie de champ du RM, RCS, RSAC');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('28P', 'Déclaration, modification de l’insaisissabilité');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('29P', 'Autre modification concernant la personne1');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('30P', 'Modification relative au conjoint collaborateur');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('31P', 'Modification relative aux exploitants de l’indivision ou héritiers');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('36P', 'Modification relative au représentant social ou au représentant fiscal d’une entreprise personnelle étrangère employeur sans établissement en France');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('37P', 'Suppression de la mention du contrat d’appui');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('40P', 'Cessation temporaire d’activité de l’entreprise');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('41P', 'Cessation totale d’activité non salariée');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('42P', 'Décès de l’exploitant individuel sans poursuite de l’exploitation avec demande de maintien provisoire au RCS ou au RM');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('43P', 'Cessation totale d’activité avec demande de maintien provisoire au RCS ou au RM');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('44P', 'Cessation d’activité agricole avec mise en location des terres et assujettissement à la TVA pour ce bail');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('45P', 'Cessation d’activité agricole avec conservation de stocks ou de cheptel');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('46P', 'Départ en retraite avec conservation d’une exploitation de subsistance');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('47P', 'Option TVA bailleur de biens ruraux');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('01M', 'Création d’une entreprise personne morale');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('02M', 'Constitution d’une société sans activité');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('03M', 'Constitution d’une société sans activité au siège avec début d’activité hors siège');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('04M', 'Ouverture du 1er établissement en France d’une société commerciale ayant son siège à l’étranger');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('05M', 'Constitution d’une personne morale dont l’immatriculation est prévue par un texte');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('07M', 'Inscription d’une société étrangère sans établissement en France');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('10M', 'Modification de l’identification de la personne morale');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('11M', 'Transfert du siège de l’entreprise');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('12M', 'Modification des principales activités');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('13M', 'Modification de la forme juridique ou du statut particulier');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('14M', 'Modification du ou des noms de domaine des sites internet de la personne morale');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('15M', 'Modification du capital social');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('16M', 'Modification de la durée de la personne morale ou de la date de clôture de l’exercice social');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('17M', 'Modification de la mention « associé unique » (déclaration ou suppression)');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('18M', 'Economie Sociale et Solidaire (ESS)');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('19M', 'Changement de la nature de la gérance');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('20M', 'Modification de la date de début d‘activité');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('22M', 'Dissolution');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('23M', 'Demande de prorogation de l’immatriculation au RCS');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('24M', 'Entrée de champ RCS, RM ou RSAC');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('25M', 'Continuation de l’exploitation malgré un actif net devenu inférieur à la moitié du capital');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('26M', 'Reconstitution des capitaux propres');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('27M', 'Sortie de champ du RM, RCS, RSAC');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('28M', 'Dissolution sans liquidation suite à décision de l''associé unique Personne Morale (TUP)');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('29M', 'Autre modification concernant la personne morale');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('30M', 'Modification relative aux membres d’un groupement');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('32M', 'Modification relative aux associés non gérants relevant du régime TNS MSA');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('33M', 'Modification relative aux dirigeants d’un groupement');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('34M', 'Modification relative aux dirigeants d’une société de personnes');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('35M', 'Modification relative aux dirigeants d’une SARL ou d’une société de capitaux');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('36M', 'Modification relative au représentant social ou au représentant fiscal d’une société étrangère employeur sans établissement en France');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('37M', 'Suppression de la mention du contrat d’appui');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('40M', 'Cessation totale d’activité de l’entreprise sans disparition de la personne morale');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('41M', 'Disparition de la personne morale par suite de fusion ou de scission');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('42M', 'Disparition de la personne morale');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('43M', 'Fermeture de l’établissement principal d’une société étrangère');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('44M', 'Cessation d’activité agricole avec mise en location des terres et assujettissement à la TVA pour ce bail');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('47M', 'Option TVA bailleur de biens ruraux');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('01F', 'Déclaration d’exploitation en commun');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('10F', 'Modification de l’identification de l’exploitation en commun');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('11F', 'Transfert de l’entreprise');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('12F', 'Modification des principales activités de l’exploitation en commun');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('20F', 'Modification de la date de début d’activité');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('24F', 'Entrée de champ MSA');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('27F', 'Sortie de champ MSA');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('30F', 'Modification relative aux membres de l’exploitation en commun');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('40F', 'Fin d’exploitation en commun');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('44F', 'Cessation d’activité agricole avec mise en location des terres et assujettissement à la TVA pour ce bail');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('45F', 'Cessation d’activité agricole avec conservation de stocks ou de cheptel');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('47F', 'Option TVA bailleur de biens ruraux');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('51M', 'Début d’activité au siège d’une entreprise sans activité ou reprise d’activité au siège après une cessation temporaire');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('52M', 'Ouverture d’un établissement distinct du siège par une entreprise sans activité');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('53P', 'Reprise de l’exploitation d’un fonds mis en location gérance');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('53M', 'Reprise de l’exploitation d’un fonds mis en location gérance');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('53F', 'Reprise de l’exploitation d’un fonds mis en location gérance');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('54P', 'Ouverture d’un nouvel établissement');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('54M', 'Ouverture d’un nouvel établissement');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('54F', 'Ouverture d’un nouvel établissement');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('55P', 'Modification du nom de domaine du site internet d''un établissement');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('55M', 'Modification du nom de domaine du site internet d''un établissement');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('56P', 'Transfert d’un établissement');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('56M', 'Transfert d’un établissement');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('56F', 'Transfert d’un établissement');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('60P', 'Modification de l’identification de l’établissement');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('60M', 'Modification de l’identification de l’établissement');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('60F', 'Modification de l’identification de l’établissement');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('61P', 'Adjonction d’activité');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('61M', 'Adjonction d’activité');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('61F', 'Adjonction d’activité');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('62P', 'Suppression partielle d’activité');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('62M', 'Suppression partielle d’activité');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('62F', 'Suppression partielle d’activité');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('63P', 'Acquisition du fonds par l’exploitant');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('63M', 'Acquisition du fonds par l’exploitant');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('63F', 'Acquisition du fonds par l’exploitant');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('64P', 'Renouvellement du contrat de location gérance');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('64M', 'Renouvellement du contrat de location gérance');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('64F', 'Renouvellement du contrat de location gérance');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('65P', 'Embauche d’un premier salarié dans un établissement');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('65M', 'Embauche d’un premier salarié dans un établissement');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('65F', 'Embauche d’un premier salarié dans un établissement');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('66P', 'Fin d’emploi de tout salarié dans un établissement');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('66M', 'Fin d’emploi de tout salarié dans un établissement');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('66F', 'Fin d’emploi de tout salarié dans un établissement');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('67P', 'Modification des activités de l’établissement');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('67M', 'Modification des activités de l’établissement');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('67F', 'Modification des activités de l’établissement');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('68P', 'Changement de locataire gérant');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('68M', 'Changement de locataire gérant');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('69P', 'Changement de loueur du fonds');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('69M', 'Changement de loueur du fonds');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('70P', 'Modification relative à une personne ayant le pouvoir d’engager l’établissement');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('70M', 'Modification relative à une personne ayant le pouvoir d’engager l’établissement');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('77P', 'Modification relative aux propriétaires indivis du fonds');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('77F', 'Modification relative aux propriétaires indivis du fonds');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('80P', 'Fermeture d’un établissement');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('80M', 'Fermeture d’un établissement');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('80F', 'Fermeture d’un établissement');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('82P', 'Mise en location gérance ou en gérance mandat d’un des fonds exploités');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('82M', 'Mise en location gérance ou en gérance mandat d’un des fonds exploités');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('82F', 'Mise en location gérance ou en gérance mandat d’un des fonds exploités');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('83P', 'Mise en location gérance du fonds unique sans maintien au RCS ou au RM');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('84P', 'Mise en location gérance du fonds unique ou en gérance mandat avec maintien au RCS');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('84M', 'Mise en location gérance du fonds unique ou en gérance mandat avec maintien au RCS');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('90P', 'Radiation d’office du RSI');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('91P', 'Rejet de l’immatriculation au RM');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('91M', 'Rejet de l’immatriculation au RM');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('92P', 'Rejet par le Greffe de la formalité présentée au RCS');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('92M', 'Rejet par le Greffe de la formalité présentée au RCS');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('93P', 'Radiation d’office du RM');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('93M', 'Radiation d’office du RM');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('94P', 'Radiation d’office du RCS');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('94M', 'Radiation d’office du RCS');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('95M', 'Invalidation de la mention Economie Sociale et Solidaire (ESS)');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('96P', 'Réactivation d’une Personne Physique suite à radiation d’office');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('96M', 'Réactivation d’une Personne Morale suite à radiation d’office');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('97P', 'Sortie du Micro Social (MSS) par franchissement de seuils ou par option');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('98M', 'Refus d’immatriculation au RCS');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('99P', 'Correction d’une formalité');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('99M', 'Correction d’une formalité');
INSERT INTO arc.nmcl_evenements_v002 VALUES ('99F', 'Correction d’une formalité');


--
-- TOC entry 7357 (class 0 OID 740269)
-- Dependencies: 315
-- Data for Name: nmcl_rubriqueentete_v001; Type: TABLE DATA; Schema: arc; Owner: -
--

INSERT INTO arc.nmcl_rubriqueentete_v001 VALUES ('regent_xml');
INSERT INTO arc.nmcl_rubriqueentete_v001 VALUES ('emetteur');
INSERT INTO arc.nmcl_rubriqueentete_v001 VALUES ('destinataire');
INSERT INTO arc.nmcl_rubriqueentete_v001 VALUES ('dateHeureEmission');
INSERT INTO arc.nmcl_rubriqueentete_v001 VALUES ('versionmessage');
INSERT INTO arc.nmcl_rubriqueentete_v001 VALUES ('versionNorme');
INSERT INTO arc.nmcl_rubriqueentete_v001 VALUES ('serviceApplicatif');
INSERT INTO arc.nmcl_rubriqueentete_v001 VALUES ('specification');
INSERT INTO arc.nmcl_rubriqueentete_v001 VALUES ('nomService');
INSERT INTO arc.nmcl_rubriqueentete_v001 VALUES ('versionService');
INSERT INTO arc.nmcl_rubriqueentete_v001 VALUES ('specification');


--
-- TOC entry 7355 (class 0 OID 547706)
-- Dependencies: 313
-- Data for Name: nmcl_supportdec_v001; Type: TABLE DATA; Schema: arc; Owner: -
--

INSERT INTO arc.nmcl_supportdec_v001 VALUES ('.', 'Non renseigné');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('1000', 'Entrepreneur individuel');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('2110', 'Indivision entre personnes physiques ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('2120', 'Indivision avec personne morale ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('2210', 'Société créée de fait entre personnes physiques ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('2220', 'Société créée de fait avec personne morale ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('2310', 'Société en participation entre personnes physiques ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('2320', 'Société en participation avec personne morale ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('2385', 'Société en participation de professions libérales ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('2400', 'Fiducie ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('2700', 'Paroisse hors zone concordataire ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('2900', 'Autre groupement de droit privé non doté de la personnalité morale ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('3110', 'Représentation ou agence commerciale d''état ou organisme public étranger immatriculé au RCS ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('3120', 'Société commerciale étrangère immatriculée au RCS');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('3205', 'Organisation internationale ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('3210', 'État, collectivité ou établissement public étranger');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('3220', 'Société étrangère non immatriculée au RCS ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('3290', 'Autre personne morale de droit étranger ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('4110', 'Établissement public national à caractère industriel ou commercial doté d''un comptable public ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('4120', 'Établissement public national à caractère industriel ou commercial non doté d''un comptable public ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('4130', 'Exploitant public ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('4140', 'Établissement public local à caractère industriel ou commercial ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('4150', 'Régie d''une collectivité locale à caractère industriel ou commercial ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('4160', 'Institution Banque de France ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5191', 'Société de caution mutuelle ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5192', 'Société coopérative de banque populaire ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5193', 'Caisse de crédit maritime mutuel ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5194', 'Caisse (fédérale) de crédit mutuel ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5195', 'Association coopérative inscrite (droit local Alsace Moselle) ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5196', 'Caisse d''épargne et de prévoyance à forme coopérative ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5202', 'Société en nom collectif ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5203', 'Société en nom collectif coopérative ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5306', 'Société en commandite simple ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5307', 'Société en commandite simple coopérative ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5308', 'Société en commandite par actions ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5309', 'Société en commandite par actions coopérative ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5370', 'Société de Participations Financières de Profession Libérale Société en commandite par actions (SPFPL SCA)');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5385', 'Société d''exercice libéral en commandite par actions ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5410', 'SARL nationale ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5415', 'SARL d''économie mixte ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5422', 'SARL immobilière pour le commerce et l''industrie (SICOMI) ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5426', 'SARL immobilière de gestion');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5430', 'SARL d''aménagement foncier et d''équipement rural (SAFER)');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5431', 'SARL mixte d''intérêt agricole (SMIA) ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5432', 'SARL d''intérêt collectif agricole (SICA) ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5442', 'SARL d''attribution ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5443', 'SARL coopérative de construction ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5451', 'SARL coopérative de consommation ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5453', 'SARL coopérative artisanale ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5454', 'SARL coopérative d''intérêt maritime ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5455', 'SARL coopérative de transport');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5458', 'SARL coopérative ouvrière de production (SCOP)');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5459', 'SARL union de sociétés coopératives ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5460', 'Autre SARL coopérative ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5470', 'Société de Participations Financières de Profession Libérale Société à responsabilité limitée (SPFPL SARL)');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5485', 'Société d''exercice libéral à responsabilité limitée ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5498', 'SARL unipersonnelle ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5499', 'Société à responsabilité limitée (sans autre indication)');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5505', 'SA à participation ouvrière à conseil d''administration ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5510', 'SA nationale à conseil d''administration ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5515', 'SA d''économie mixte à conseil d''administration ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5520', 'Fonds à forme sociétale à conseil d''administration');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5522', 'SA immobilière pour le commerce et l''industrie (SICOMI) à conseil d''administration');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5525', 'SA immobilière d''investissement à conseil d''administration');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5530', 'SA d''aménagement foncier et d''équipement rural (SAFER) à conseil d''administration');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5531', 'Société anonyme mixte d''intérêt agricole (SMIA) à conseil d''administration ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5532', 'SA d''intérêt collectif agricole (SICA) à conseil d''administration');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5542', 'SA d''attribution à conseil d''administration');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5543', 'SA coopérative de construction à conseil d''administration');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5546', 'SA de HLM à conseil d''administration ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5547', 'SA coopérative de production de HLM à conseil d''administration ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5548', 'SA de crédit immobilier à conseil d''administration ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5551', 'SA coopérative de consommation à conseil d''administration ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5552', 'SA coopérative de commerçants-détaillants à conseil d''administration');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5553', 'SA coopérative artisanale à conseil d''administration ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5554', 'SA coopérative (d''intérêt) maritime à conseil d''administration ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5555', 'SA coopérative de transport à conseil d''administration');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5558', 'SA coopérative ouvrière de production (SCOP) à conseil d''administration');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5559', 'SA union de sociétés coopératives à conseil d''administration ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5560', 'Autre SA coopérative à conseil d''administration ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5570', 'Société de Participations Financières de Profession Libérale Société anonyme à conseil d''administration (SPFPL SA à conseil d''administration)');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5585', 'Société d''exercice libéral à forme anonyme à conseil d''administration ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5599', 'SA à conseil d''administration (s.a.i.)');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5605', 'SA à participation ouvrière à directoire ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5610', 'SA nationale à directoire ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5615', 'SA d''économie mixte à directoire ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5620', 'Fonds à forme sociétale à directoire');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5622', 'SA immobilière pour le commerce et l''industrie (SICOMI) à directoire');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5625', 'SA immobilière d''investissement à directoire ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5630', 'Safer anonyme à directoire ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5631', 'SA mixte d''intérêt agricole (SMIA)');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5632', 'SA d''intérêt collectif agricole (SICA)');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5642', 'SA d''attribution à directoire');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5643', 'SA coopérative de construction à directoire');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5646', 'SA de HLM à directoire');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5647', 'Société coopérative de production de HLM anonyme à directoire ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5648', 'SA de crédit immobilier à directoire ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5651', 'SA coopérative de consommation à directoire ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5652', 'SA coopérative de commerçants-détaillants à directoire');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5653', 'SA coopérative artisanale à directoire ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5654', 'SA coopérative d''intérêt maritime à directoire ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5655', 'SA coopérative de transport à directoire ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5658', 'SA coopérative ouvrière de production (SCOP) à directoire');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5659', 'SA union de sociétés coopératives à directoire ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5660', 'Autre SA coopérative à directoire');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5670', 'Société de Participations Financières de Profession Libérale Société anonyme à Directoire (SPFPL SA à directoire)');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5685', 'Société d''exercice libéral à forme anonyme à directoire ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5699', 'SA à directoire (s.a.i.)');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5710', 'SAS, société par actions simplifiée');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5720', 'Société par actions simplifiée à associé unique ou société par actions simplifiée unipersonnelle ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5770', 'Société de Participations Financières de Profession Libérale Société par actions simplifiée (SPFPL SAS)');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5785', 'Société d''exercice libéral par action simplifiée ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('5800', 'Société européenne ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('6100', 'Caisse d''Épargne et de Prévoyance ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('6210', 'Groupement européen d''intérêt économique (GEIE) ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('6220', 'Groupement d''intérêt économique (GIE) ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('6316', 'Coopérative d''utilisation de matériel agricole en commun (CUMA) ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('6317', 'Société coopérative agricole ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('6318', 'Union de sociétés coopératives agricoles ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('6411', 'Société d''assurance à forme mutuelle');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('6511', 'Sociétés Interprofessionnelles de Soins Ambulatoires ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('6521', 'Société civile de placement collectif immobilier (SCPI) ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('6532', 'Société civile d''intérêt collectif agricole (SICA) ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('6533', 'Groupement agricole d''exploitation en commun (GAEC) ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('6534', 'Groupement foncier agricole ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('6535', 'Groupement agricole foncier ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('6536', 'Groupement forestier ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('6537', 'Groupement pastoral ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('6538', 'Groupement foncier et rural');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('6539', 'Société civile foncière ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('6540', 'Société civile immobilière ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('6541', 'Société civile immobilière de construction-vente');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('6542', 'Société civile d''attribution ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('6543', 'Société civile coopérative de construction ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('6544', 'Société civile immobilière d'' accession progressive à la propriété');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('6551', 'Société civile coopérative de consommation ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('6554', 'Société civile coopérative d''intérêt maritime ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('6558', 'Société civile coopérative entre médecins ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('6560', 'Autre société civile coopérative ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('6561', 'SCP d''avocats ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('6562', 'SCP d''avocats aux conseils ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('6563', 'SCP d''avoués d''appel ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('6564', 'SCP d''huissiers ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('6565', 'SCP de notaires ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('6566', 'SCP de commissaires-priseurs ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('6567', 'SCP de greffiers de tribunal de commerce ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('6568', 'SCP de conseils juridiques ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('6569', 'SCP de commissaires aux comptes ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('6571', 'SCP de médecins ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('6572', 'SCP de dentistes ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('6573', 'SCP d''infirmiers ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('6574', 'SCP de masseurs-kinésithérapeutes');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('6575', 'SCP de directeurs de laboratoire d''analyse médicale ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('6576', 'SCP de vétérinaires ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('6577', 'SCP de géomètres experts');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('6578', 'SCP d''architectes ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('6585', 'Autre société civile professionnelle');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('6588', 'Société civile laitière ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('6589', 'Société civile de moyens ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('6595', 'Caisse locale de crédit mutuel ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('6596', 'Caisse de crédit agricole mutuel ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('6597', 'Société civile d''exploitation agricole ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('6598', 'Exploitation agricole à responsabilité limitée ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('6599', 'Autre société civile ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('6901', 'Autre personne de droit privé inscrite au registre du commerce et des sociétés');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7111', 'Autorité constitutionnelle ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7112', 'Autorité administrative ou publique indépendante');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7113', 'Ministère ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7120', 'Service central d''un ministère ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7150', 'Service du ministère de la Défense ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7160', 'Service déconcentré à compétence nationale d''un ministère (hors Défense)');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7171', 'Service déconcentré de l''État à compétence (inter) régionale ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7172', 'Service déconcentré de l''État à compétence (inter) départementale ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7179', '(Autre) Service déconcentré de l''État à compétence territoriale ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7190', 'Ecole nationale non dotée de la personnalité morale ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7210', 'Commune et commune nouvelle ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7220', 'Département ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7225', 'Collectivité et territoire d''Outre Mer');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7229', '(Autre) Collectivité territoriale ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7230', 'Région ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7312', 'Commune associée et commune déléguée ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7313', 'Section de commune ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7314', 'Ensemble urbain ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7321', 'Association syndicale autorisée ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7322', 'Association foncière urbaine ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7323', 'Association foncière de remembrement ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7331', 'Établissement public local d''enseignement ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7340', 'Pôle métropolitain');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7341', 'Secteur de commune ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7342', 'District urbain ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7343', 'Communauté urbaine ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7344', 'Métropole');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7345', 'Syndicat intercommunal à vocation multiple (SIVOM) ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7346', 'Communauté de communes ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7347', 'Communauté de villes ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7348', 'Communauté d''agglomération ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7349', 'Autre établissement public local de coopération non spécialisé ou entente ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7351', 'Institution interdépartementale ou entente');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7352', 'Institution interrégionale ou entente ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7353', 'Syndicat intercommunal à vocation unique (SIVU) ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7354', 'Syndicat mixte fermé ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7355', 'Syndicat mixte ouvert');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7356', 'Commission syndicale pour la gestion des biens indivis des communes ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7357', 'Pôle d''équilibre territorial et rural (PETR)');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7361', 'Centre communal d''action sociale ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7362', 'Caisse des écoles ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7363', 'Caisse de crédit municipal ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7364', 'Établissement d''hospitalisation ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7365', 'Syndicat inter hospitalier ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7366', 'Établissement public local social et médico-social ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7367', 'Centre Intercommunal d''action sociale (CIAS)');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7371', 'Office public d''habitation à loyer modéré (OPHLM) ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7372', 'Service départemental d''incendie et de secours (SDIS)');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7373', 'Établissement public local culturel ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7378', 'Régie d''une collectivité locale à caractère administratif ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7379', '(Autre) Établissement public administratif local ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7381', 'Organisme consulaire ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7382', 'Établissement public national ayant fonction d''administration centrale ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7383', 'Établissement public national à caractère scientifique culturel et professionnel ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7384', 'Autre établissement public national d''enseignement ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7385', 'Autre établissement public national administratif à compétence territoriale limitée ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7389', 'Établissement public national à caractère administratif ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7410', 'Groupement d''intérêt public (GIP) ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7430', 'Établissement public des cultes d''Alsace-Lorraine ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7450', 'Etablissement public administratif, cercle et foyer dans les armées ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7470', 'Groupement de coopération sanitaire à gestion publique ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('7490', 'Autre personne morale de droit administratif ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('8110', 'Régime général de la Sécurité Sociale');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('8120', 'Régime spécial de Sécurité Sociale');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('8130', 'Institution de retraite complémentaire ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('8140', 'Mutualité sociale agricole ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('8150', 'Régime maladie des non-salariés non agricoles ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('8160', 'Régime vieillesse ne dépendant pas du régime général de la Sécurité Sociale');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('8170', 'Régime d''assurance chômage ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('8190', 'Autre régime de prévoyance sociale ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('8210', 'Mutuelle ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('8250', 'Assurance mutuelle agricole ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('8290', 'Autre organisme mutualiste ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('8310', 'Comité central d''entreprise ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('8311', 'Comité d''établissement ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('8410', 'Syndicat de salariés ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('8420', 'Syndicat patronal ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('8450', 'Ordre professionnel ou assimilé ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('8470', 'Centre technique industriel ou comité professionnel du développement économique ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('8490', 'Autre organisme professionnel ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('8510', 'Institution de prévoyance ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('8520', 'Institution de retraite supplémentaire ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('9110', 'Syndicat de copropriété ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('9150', 'Association syndicale libre ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('9210', 'Association non déclarée ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('9220', 'Association déclarée ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('9221', 'Association déclarée d''insertion par l''économique');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('9222', 'Association intermédiaire ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('9223', 'Groupement d''employeurs ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('9224', 'Association d''avocats à responsabilité professionnelle individuelle');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('9230', 'Association déclarée, reconnue d''utilité publique');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('9240', 'Congrégation ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('9260', 'Association de droit local (Bas-Rhin, Haut-Rhin et Moselle)');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('9300', 'Fondation ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('9900', 'Autre personne morale de droit privé ');
INSERT INTO arc.nmcl_supportdec_v001 VALUES ('9970', 'Groupement de coopération sanitaire à gestion privée ');



--
-- TOC entry 7324 (class 0 OID 16960)
-- Dependencies: 277
-- Data for Name: parameter; Type: TABLE DATA; Schema: arc; Owner: -
--

INSERT INTO arc.parameter VALUES ('git.commit.id.arc_bas11', 'ea5420f4d121208d069908eb0d651ab4ace22fcf', 'parameter.database.version.sandbox');
INSERT INTO arc.parameter VALUES ('ApiInitialisationService.Nb_Jour_A_Conserver', '365', 'parameter.batch.initialization.numberOfDayToKeepAfterRetrievedByAllCLients');
INSERT INTO arc.parameter VALUES ('ApiInitialisationService.NB_FICHIER_PER_ARCHIVE', '10000', 'parameter.batch.initialization.numberOfFilesToProceedAtSameTimeForDeletion');
INSERT INTO arc.parameter VALUES ('ApiService.HEURE_INITIALISATION_PRODUCTION', '22', 'parameter.batch.initialization.StartsJustAfterThisHour');
INSERT INTO arc.parameter VALUES ('LanceurARC.INTERVAL_JOUR_INITIALISATION', '7', 'parameter.batch.initialization.minimumDayIntervalBetweenInitializations');
INSERT INTO arc.parameter VALUES ('LanceurARC.keepInDatabase', 'false', 'parameter.batch.execution.keepPreviousModuleData');
INSERT INTO arc.parameter VALUES ('git.commit.id.arc_bas4', 'ea5420f4d121208d069908eb0d651ab4ace22fcf', 'parameter.database.version.sandbox');
INSERT INTO arc.parameter VALUES ('LanceurARC.deltaStepAllowed', '10000', 'parameter.batch.execution.howManyStepsFurtherModuleAreExecuted');
INSERT INTO arc.parameter VALUES ('LanceurARC.poolingDelay', '1000', 'parameter.batch.execution.sleepingDelayDuringPipelineLoopInMs');
INSERT INTO arc.parameter VALUES ('LanceurARC.envFromDatabase', 'false', 'parameter.batch.execution.booleanUseEnvironmentDeclaredInDatabase');
INSERT INTO arc.parameter VALUES ('LanceurARC.env', 'arc.ihm', 'parameter.batch.execution.environmentOfRuleset');
INSERT INTO arc.parameter VALUES ('LanceurARC.envExecution', 'arc_prod', 'parameter.batch.execution.environmentForExecution');
INSERT INTO arc.parameter VALUES ('ApiReceptionService.batch.maxNumberOfFiles', '25000', 'parameter.batch.execution.maxNumberOfFilesRegisteredInReceptionModule');
INSERT INTO arc.parameter VALUES ('LanceurARC.tailleMaxReceptionEnMb', '100', 'parameter.batch.execution.maxCompressedArchiveSizeRegisteredInReceptionModule');
INSERT INTO arc.parameter VALUES ('LanceurARC.maxFilesToLoad', '101', 'parameter.batch.execution.maxNumberOfFilesProceedInLoadModule');
INSERT INTO arc.parameter VALUES ('git.commit.id.arc_bas8', '1fee2249a98298f4a33b739874155877f261c795', 'parameter.database.version.sandbox');
INSERT INTO arc.parameter VALUES ('ApiInitialisationService.nbSandboxes', '20', 'parameter.ihm.sandbox.numberOfSandboxes');
INSERT INTO arc.parameter VALUES ('ApiReceptionService.ihm.maxNumberOfFiles', '5000', 'parameter.ihm.sandbox.maxNumberOfFilesRegisteredAtTheSameTime');
INSERT INTO arc.parameter VALUES ('ArcAction.productionEnvironments', '[]', 'parameter.ihm.sandbox.sandboxListWithProductionGUI');
INSERT INTO arc.parameter VALUES ('ApiChargementService.MAX_PARALLEL_WORKERS', '8', 'parameter.parallel.numberOfThread.p1.load');
INSERT INTO arc.parameter VALUES ('ApiNormageService.MAX_PARALLEL_WORKERS', '8', 'parameter.parallel.numberOfThread.p2.xmlStructurize');
INSERT INTO arc.parameter VALUES ('ApiControleService.MAX_PARALLEL_WORKERS', '8', 'parameter.parallel.numberOfThread.p3.control');
INSERT INTO arc.parameter VALUES ('ApiFiltrageService.MAX_PARALLEL_WORKERS', '8', 'parameter.parallel.numberOfThread.p4.filter');
INSERT INTO arc.parameter VALUES ('MappingService.MAX_PARALLEL_WORKERS', '8', 'parameter.parallel.numberOfThread.p5.mapmodel');
INSERT INTO arc.parameter VALUES ('git.commit.id.arc_bas7', 'a52a8fa72c1a28772c17293f037dadebcdc1c525', 'parameter.database.version.sandbox');
INSERT INTO arc.parameter VALUES ('git.commit.id.arc_bas6', '4bb9abd962f0a9a8396a7555ff36b696588c7807', 'parameter.database.version.sandbox');
INSERT INTO arc.parameter VALUES ('git.commit.id.arc_bas10', 'af5f3e838ab541fb47948c650b0ab465bdef05b8', 'parameter.database.version.sandbox');
INSERT INTO arc.parameter VALUES ('git.commit.id', 'ea5420f4d121208d069908eb0d651ab4ace22fcf', 'parameter.database.version.global');
INSERT INTO arc.parameter VALUES ('LanceurARC.maxFilesPerPhase', '1000000', 'parameter.batch.execution.defaultMaxNumberOfFilesProcessedByModules');
INSERT INTO arc.parameter VALUES ('git.commit.id.arc_bas1', 'ea5420f4d121208d069908eb0d651ab4ace22fcf', 'parameter.database.version.sandbox');
INSERT INTO arc.parameter VALUES ('git.commit.id.arc_bas2', 'ea5420f4d121208d069908eb0d651ab4ace22fcf', 'parameter.database.version.sandbox');
INSERT INTO arc.parameter VALUES ('git.commit.id.arc_bas3', 'ea5420f4d121208d069908eb0d651ab4ace22fcf', 'parameter.database.version.sandbox');


--
-- TOC entry 7367 (class 0 OID 16975816)
-- Dependencies: 785
-- Data for Name: pilotage_batch; Type: TABLE DATA; Schema: arc; Owner: -
--

INSERT INTO arc.pilotage_batch VALUES ('1900-01-01:00', 'O');


--
-- TOC entry 7353 (class 0 OID 346532)
-- Dependencies: 311
-- Data for Name: service_env_locked; Type: TABLE DATA; Schema: arc; Owner: -
--

