INSERT INTO arc.ihm_jeuderegle VALUES ('PHASE3V1', 'M', '2015-01-01', '2050-12-31', 'v003', 'arc.bas3', NULL, NULL);

--
-- TOC entry 3653 (class 0 OID 43074403)
-- Dependencies: 232
-- Data for Name: ihm_chargement_regle; Type: TABLE DATA; Schema: arc; Owner: -
--

INSERT INTO arc.ihm_chargement_regle VALUES (1, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'xml', NULL, NULL, NULL);


INSERT INTO arc.ihm_controle_regle VALUES ('PHASE3V1', 'M', '2015-01-01', '2050-12-31', 'v003', 'ALPHANUM', 'V_S20_G00_05_001', NULL, '2', '2', NULL, 'lpad({V_S20_G00_05_001},2,''0'')', 1, NULL, NULL, NULL, NULL, NULL, NULL, 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('PHASE3V1', 'M', '2015-01-01', '2050-12-31', 'v003', 'ALPHANUM', 'V_S20_G00_05_002', NULL, '2', '2', NULL, 'lpad({V_S20_G00_05_002},2,''0'')', 2, NULL, NULL, NULL, NULL, NULL, NULL, 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('PHASE3V1', 'M', '2015-01-01', '2050-12-31', 'v003', 'ALPHANUM', 'V_S10_G00_00_008', NULL, '2', '2', NULL, 'lpad({V_S10_G00_00_008},2,''0'')', 3, NULL, NULL, NULL, NULL, NULL, NULL, 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('PHASE3V1', 'M', '2015-01-01', '2050-12-31', 'v003', 'ALPHANUM', 'V_S21_G00_30_301', NULL, NULL, NULL, NULL, 'case when arc.isdate({v_s21_g00_30_301},''DDMMYYYY'') is true then substr({v_s21_g00_30_301},5,4)||''-''||substr({v_s21_g00_30_301},3,2)||''-''||substr({v_s21_g00_30_301},1,2)  end', 5, NULL, NULL, NULL, NULL, NULL, NULL, 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('PHASE3V1', 'M', '2015-01-01', '2050-12-31', 'v003', 'ALPHANUM', 'V_S21_G00_30_006', NULL, NULL, NULL, NULL, 'case when {v_s21_g00_30_006} like ''%-%'' then {v_s21_g00_30_006} else case WHEN substr({v_s21_g00_30_006},5,4)=''9999'' THEN (substr(current_date::text,1,4)::integer - 40)::text ELSE substr({v_s21_g00_30_006},5,4) END|| ''-''|| CASE WHEN substr({v_s21_g00_30_006},3,2)=''99'' THEN ''07'' ELSE substr({v_s21_g00_30_006},3,2) END|| ''-''|| CASE WHEN substr({v_s21_g00_30_006},1,2)=''99'' THEN ''15'' ELSE substr({v_s21_g00_30_006},1,2) END end', 4, NULL, NULL, NULL, NULL, NULL, NULL, 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('PHASE3V1', 'M', '2015-01-01', '2050-12-31', 'v003', 'CONDITION', NULL, NULL, NULL, NULL, 'NOT({V_S10_G00_00_005}=''01'' or substring({validite},1,4) in (''2016'',''2017'',''2018'') or (to_char(current_date,''dd'')::int<16
  and substring({validite},6,2)::int=to_char(current_date::date-interval ''1 month'',''MM'')::int
  and substring({validite},1,4)::int=to_char(current_date::date-interval ''1 month'',''YYYY'')::int) )', NULL, 6, NULL, 'règle de filtrage', NULL, NULL, NULL, '>0u', 'e');
  

INSERT INTO arc.ihm_normage_regle VALUES ('PHASE3V1', 'M', '2015-01-01', '2050-12-31', 'v003', 'relation', 'V_S21_G00_40_019', 'V_S21_G00_85_001', 1, NULL, 'relation lieu de travail maison mere');
INSERT INTO arc.ihm_normage_regle VALUES ('PHASE3V1', 'M', '2015-01-01', '2050-12-31', 'v003', 'relation', 'V_S21_G00_40_046', 'siretutil.V_S21_G00_85_001', 2, NULL, 'relation lieu de travail siret utilisateur');
INSERT INTO arc.ihm_normage_regle VALUES ('PHASE3V1', 'M', '2015-01-01', '2050-12-31', 'v003', 'relation', 'V_S21_G00_40_009', 'V_S21_G00_52_006', 3, NULL, 'numéro de contrat / prime');
INSERT INTO arc.ihm_normage_regle VALUES ('PHASE3V1', 'M', '2015-01-01', '2050-12-31', 'v003', 'relation', 'V_S21_G00_40_009', 'V_S21_G00_51_010', 4, NULL, 'numero de contrat / rémunération');
INSERT INTO arc.ihm_normage_regle VALUES ('PHASE3V1', 'M', '2015-01-01', '2050-12-31', 'v003', 'relation', 'V_S21_G00_40_009', 'V_S21_G00_34_002', 5, NULL, 'numéro de contrat / pénibilité');
INSERT INTO arc.ihm_normage_regle VALUES ('PHASE3V1', 'M', '2015-01-01', '2050-12-31', 'v003', 'unicité', 'siretutil.V_S21_G00_85_001', NULL, 6, NULL, NULL);
INSERT INTO arc.ihm_normage_regle VALUES ('PHASE3V1', 'M', '2015-01-01', '2050-12-31', 'v003', 'unicité', 'V_S21_G00_85_001', NULL, 7, NULL, NULL);
INSERT INTO arc.ihm_normage_regle VALUES ('PHASE3V1', 'M', '2015-01-01', '2050-12-31', 'v003', 'partition', 's21_g00_30', '500,100', 8, NULL, 'si > 500 individus, decoupage en paquet de 100 individus');
INSERT INTO arc.ihm_normage_regle VALUES ('PHASE3V1', 'M', '2015-01-01', '2050-12-31', 'v003', 'independance', 'm_s21_g00_51', 'v_s21_g00_51_010', 9, NULL, NULL);
INSERT INTO arc.ihm_normage_regle VALUES ('PHASE3V1', 'M', '2015-01-01', '2050-12-31', 'v003', 'independance', 'm_s21_g00_52', 'v_s21_g00_52_006', 10, NULL, NULL);


--- bas3
INSERT INTO arc.ihm_mapping_regle VALUES (46, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'id_employeur', '{pk:mapping_dsn_employeur_ok}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (115, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'siret', 'coalesce(concat(coalesce({v_s21_g00_06_001},''NOTSIREN''),coalesce({v_s21_g00_11_001},''NOTNIC'')),''S''||to_char(clock_timestamp(),''SSSS'')||to_char(clock_timestamp(),''US'')||''#''||(random()*1000)::integer)', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (55, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'indicebrut_cotisemploisup_fp', '{v_s21_g00_40_061}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (1, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'adresse_etab', 'trim(concat(trim({v_s21_g00_11_003}),'' '',trim({v_s21_g00_11_006}),'' '',trim({v_s21_g00_11_007})))', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (2, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'adresse_sal', 'trim(concat(trim({v_s21_g00_30_008}),'' '',trim({v_s21_g00_30_016}),'' '',trim({v_s21_g00_30_017})))', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (3, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'adresse_siege', 'trim(concat(trim({v_s21_g00_06_004}),'' '',trim({v_s21_g00_06_007}),'' '',trim({v_s21_g00_06_008})))', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (4, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'an_penibilite', '{{1}{v_s21_g00_34_003}}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (5, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'apen', 'COALESCE({v_s21_g00_06_003},''0000Z'')', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (6, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'apet', 'COALESCE({v_s21_g00_11_002},''0000Z'')', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (7, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'apet_lieu_trav', '{v_s21_g00_85_002}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (8, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'ccpayes', '''NON''', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (9, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'cj', '{v_s21_g00_11_012}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (10, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'code_categservice_fp', '{v_s21_g00_40_056}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (11, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'code_ccp', '{v_s21_g00_40_022}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (12, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'code_risque_acc', '{v_s21_g00_40_040}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (13, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'codification_ue', '{v_s21_g00_30_013}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (14, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'commune_lieu_trav', '{v_s21_g00_85_011}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (15, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'compl_rem_oblig', '{v_s21_g00_40_016}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (16, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'convcoll', '{v_s21_g00_40_017}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (17, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'cpost_etab', 'COALESCE({v_s21_g00_11_004},''VIIDE'')', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (18, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'cpost_lieu_trav', '{v_s21_g00_85_004}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (19, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'cpost_sal', '{v_s21_g00_30_009}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (20, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'cpost_siege', '{v_s21_g00_06_005}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (21, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'cumul_empl_retr', '{v_s21_g00_30_023}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (22, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'date_debut_arret', '{{1}{v_s21_g00_60_002}::date + ''1 day''::interval}{{2}{v_s21_g00_65_002}::date}{{3}{v_s21_g00_66_001}::date}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (23, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'date_debut_contrat', 'to_date({v_s21_g00_40_001}, ''YYYY-MM-DD'')', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (24, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'date_debut_rem', '{{1}to_date({v_s21_g00_51_001}, ''YYYY-MM-DD'')}{{2}to_date({v_s21_g00_52_003}, ''YYYY-MM-DD'')}{{3}to_date({v_s21_g00_54_003}, ''YYYY-MM-DD'')}{{4}to_date({v_s21_g00_50_001},''YYYY-MM-DD'')}{{5}to_date({v_s21_g00_50_001},''YYYY-MM-DD'')}{{6}to_date({v_s21_g00_78_002}, ''YYYY-MM-DD'')}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (25, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'date_fin_arret', '{{1}{v_s21_g00_60_010}::date - ''1 day''::interval}{{2}{v_s21_g00_65_003}::date}{{3}{v_s21_g00_66_002}::date}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (26, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'date_fin_contrat', 'to_date({v_s21_g00_62_001},''YYYY-MM-DD'')', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (27, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'date_fin_rem', '{{1}to_date({v_s21_g00_51_002}, ''YYYY-MM-DD'')}{{2}to_date({v_s21_g00_52_004}, ''YYYY-MM-DD'')}{{3}to_date({v_s21_g00_54_004}, ''YYYY-MM-DD'')}{{4}to_date({v_s21_g00_50_001},''YYYY-MM-DD'')}{{5} to_date({v_s21_g00_50_001},''YYYY-MM-DD'')}{{6}to_date({v_s21_g00_78_003}, ''YYYY-MM-DD'')}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (28, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'date_finprev_contrat', 'to_date({v_s21_g00_40_010}, ''YYYY-MM-DD'')', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (29, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'date_versement', 'to_date({v_s21_g00_50_001},''YYYY-MM-DD'')', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (30, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'dateinteg', '{date_integration}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (31, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'datenais', 'to_date(coalesce({v_s21_g00_30_006},''1800-01-01''), ''YYYY-MM-DD'')', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (32, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'datenais_sngi', 'case when {v_s21_g00_30_305} IN (''00'',''10'') then to_date({v_s21_g00_30_301},''YYYY-MM-DD'') end', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (33, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'depnais', 'COALESCE(CASE WHEN LENGTH(TRIM({v_s21_g00_30_014})) = 1 THEN CONCAT(''0'',TRIM({v_s21_g00_30_014})) ELSE TRIM({v_s21_g00_30_014}) END , ''00'')', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (34, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'detaches_expatries', '{v_s21_g00_40_024}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (35, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'dispol', '{v_s21_g00_40_008}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (36, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'editeur', '{v_s10_g00_00_002}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (37, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'eff_finperiode_empl', '{v_s21_g00_11_008}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (38, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'emploi_mult', 'case when trim({v_s21_g00_40_036}) =''02'' then ''OUI'' when trim({v_s21_g00_40_036})=''01'' then ''NON'' end', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (39, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'employeur_mult', 'case when trim({v_s21_g00_40_037}) in (''02'') then ''OUI'' when trim({v_s21_g00_40_037}) in (''01'') then ''NON'' end', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (40, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'fichier_source', '{id_source}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (41, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'formation', '{v_s21_g00_30_024}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (42, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'grparr', '{{1}''ARR''||{i_s21_g00_60}}{{2}''SUS''||{i_s21_g00_65}}{{3}''TPT''||{i_s21_g00_66}}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (43, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'grppen', '{{1}''PEN''||{i_s21_g00_34}}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (44, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'grprem', '{{1}''REM''||coalesce({i_s21_g00_51}::text,'''')}{{2}''PRI''||coalesce({i_s21_g00_52}::text,''''){{3}''ARV''||coalesce({i_s21_g00_54}::text,'''')}{{4}''VFV''||coalesce({i_s21_g00_50}::text,'''')}{{5}''VPE''||coalesce({i_s21_g00_50}::text,'''')}{{6}''BAS''||coalesce({i_s21_g00_78}::text,'''')}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (45, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'id_arret', '{pk:mapping_dsn_arret_ok}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (47, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'id_penibilite', '{pk:mapping_dsn_penibilite_ok}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (48, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'id_personne', '{pk:mapping_dsn_personne_ok}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (49, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'id_poste', '{pk:mapping_dsn_poste_ok}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (50, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'id_remuneration', '{pk:mapping_dsn_remuneration_ok}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (51, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'id_source', '{id_source}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (52, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'indicebrut_ancienorange_fp', '{v_s21_g00_40_062}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (53, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'indicebrut_ancienposte_fp', '{v_s21_g00_40_063}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (54, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'indicebrut_contractueltitu_fp', '{v_s21_g00_40_065}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (56, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'indicebrut_fp', '{v_s21_g00_40_057}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (57, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'indicebrut_origine_fp', '{v_s21_g00_40_060}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (58, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'indicebrut_originespp_fp', '{v_s21_g00_40_064}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (59, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'indicemajore', '{v_s21_g00_40_058}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (60, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'libcom_etab', 'TRIM({v_s21_g00_11_005})', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (61, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'libcom_sal', 'Trim({v_s21_g00_30_010})', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (62, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'libcom_siege', 'trim({v_s21_g00_06_006})', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (63, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'libemploi', 'trim({v_s21_g00_40_006})', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (64, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'lieu_travail', ' coalesce({v_s21_g00_40_019},''NR'')', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (65, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'localite_lieu_trav', '{v_s21_g00_85_005}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (66, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'logiciel', '{v_s10_g00_00_001}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (67, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'mesureactv', '{{1}(public.array_agg_distinct(row({i_s21_g00_53},{v_s21_g00_53_002})::public.cle_valeur) over (partition by {i_s21_g00_51}))}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (68, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'mod_tp_trav', '{v_s21_g00_40_014}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (69, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'montant', '{{1}{v_s21_g00_51_013}}{{2}{v_s21_g00_52_002}}{{3}{v_s21_g00_54_002}}{{4}{v_s21_g00_50_002}}{{5}{v_s21_g00_50_004}}{{6}{v_s21_g00_78_004}}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (70, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'motif_exclu', '{v_s21_g00_40_025}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (71, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'motif_reprise', '{{1}{v_s21_g00_60_011}}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (72, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'motifcdd', ' {v_s21_g00_40_021}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (73, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'motifin', '{v_s21_g00_62_002}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (74, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'nat_contrat', '{v_s21_g00_40_007}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (75, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'nat_jur_employeur', '{v_s21_g00_11_017}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (76, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'nat_lieu_trav', '{V_S21_G00_85_010}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (77, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'nat_poste_fp', '{v_s21_g00_40_053}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (78, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'nat_siret_util', '{V_S21_G00_85_010_siretutil}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (79, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'nbhrsup', '{{1}{v_s21_g00_51_012}}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (80, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'nbi_fp', '{v_s21_g00_40_059}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (81, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'nir_sngi', 'case when {v_s21_g00_30_305} IN (''00'',''10'') then {v_s21_g00_30_300} end', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (82, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'nom_sal', 'TRIM({v_s21_g00_30_002})', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (83, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'nom_usage', 'TRIM({v_s21_g00_30_003})', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (84, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'norme', '{id_norme}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (85, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'ntt', '{v_s21_g00_30_020}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (86, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'num_contrat', '{v_s21_g00_40_009}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (87, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'num_versement', '{v_s21_g00_50_003}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (88, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'numpersonne', 'coalesce({v_s21_g00_30_001},{v_s21_g00_30_020},''N''||to_char(clock_timestamp(),''SSSS'')||to_char(clock_timestamp(),''US'')||''#''||(random()*1000)::integer)', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (89, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'payer', '{{1}(public.array_agg_distinct(row({i_s21_g00_53},case when trim({v_s21_g00_53_001}) IN (''01'',''03'') Then ''OUI'' when trim({v_s21_g00_53_001})=''02'' then ''NON'' end)::public.cle_valeur) over (partition by {i_s21_g00_51}))}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (90, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'pays_lieu_trav', '{v_s21_g00_85_006}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (91, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'pays_naissance_insee', 'case when {v_s21_g00_30_014}=''99'' then coalesce((select code1 from nmcl_code_pays_etranger_2017 a WHERE {v_s21_g00_30_015}=a.code0),''99000'') else {v_s21_g00_30_015} end', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (92, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'pays_residence_sal', 'COALESCE ({v_s21_g00_30_011},''FR'')', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (93, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'pcs', 'COALESCE ({v_s21_g00_40_004},''000X'')', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (94, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'pcs_complement', '{v_s21_g00_40_005}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (95, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'pcs_complement_ese_fp', '{v_s21_g00_40_052}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (96, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'penibilite', '{{1}{v_s21_g00_34_001}}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (97, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'periodicite', '{periodicite}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (98, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'perte_tpt', '{{3}{v_s21_g00_66_003}}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (99, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'pos_detach_fp', '{{2}{v_s21_g00_65_004}}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (100, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'positionnement_convcoll', '{v_s21_g00_40_041}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (101, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'pourboire', '{v_s21_g00_40_045}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (102, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'prenom_sal', 'TRIM({v_s21_g00_30_004})', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (103, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'presence', '{{1}(public.array_agg_distinct(row({i_s21_g00_53},case when TRIM({v_s21_g00_53_001}) = ''01'' Then ''OUI'' when TRIM({v_s21_g00_53_001}) IN (''02'',''03'') then ''NON'' end)::public.cle_valeur) over (partition by {i_s21_g00_51}))}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (104, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'quotite', '{v_s21_g00_40_013}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (105, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'quotite_ref', '{v_s21_g00_40_012}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (106, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'quotite_ref_fp', '{v_s21_g00_40_054}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (107, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'raisonabsence', '{{1}''ARR''||{v_s21_g00_60_001}}{{2}''SUS''||{v_s21_g00_65_001}}{{3}''TPT''}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (108, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'regmal', ' {v_s21_g00_40_018}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (109, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'regviel', ' {v_s21_g00_40_020}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (110, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'resid_etab', 'coalesce({v_s21_g00_11_015},''FR'')', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (111, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'resid_siege', 'coalesce({v_s21_g00_06_010},''FR'')', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (112, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'rs_etab', '{v_s21_g00_11_904}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (113, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'rs_ul', '{v_s21_g00_06_903}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (114, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'sexe', 'COALESCE(CASE WHEN {v_s21_g00_30_005} IS NOT NULL THEN LTRIM({v_s21_g00_30_005},''0'')   WHEN {v_s21_g00_30_001} IS NOT NULL THEN SUBSTR({v_s21_g00_30_001},1,1) ELSE SUBSTR({v_s21_g00_30_020},1,1) END, ''0'')', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (116, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'siret_utilisateur', 'coalesce({v_s21_g00_40_046},''NR'')', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (117, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'srcrem', 'public.curr_val(''arc.number_generator'')', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (118, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'statcat', 'CASE WHEN {v_s21_g00_40_018}=''300'' then ''MSA''||{v_s21_g00_40_042} else ''RGG''||{v_s21_g00_40_003} end', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (119, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'statconv', '{v_s21_g00_40_002}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (120, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'statempl', '{v_s21_g00_40_026}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (121, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'statut_boeth', '{v_s21_g00_40_072}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (122, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'statut_etranger', '{v_s21_g00_30_022}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (123, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'tx_cotis_acc', '{v_s21_g00_40_043}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (124, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'tx_remun_fp', '{{1}{v_s21_g00_51_014}}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (125, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'txfraisprof', '{v_s21_g00_40_023}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (126, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'txtempspartiel_fp', '{v_s21_g00_40_055}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (127, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'type_detachement_fp', '{v_s21_g00_40_066}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (128, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'typrem', '{{1}''REM''||{v_s21_g00_51_011}}{{2}''PRI''||{v_s21_g00_52_001}}{{3}''ARV''||{v_s21_g00_54_001}}{{4}''VFV''}{{5}''VPE''}{{6}''BAS''||{v_s21_g00_78_001}}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (129, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'unitmesureactv', '{{1}(public.array_agg_distinct(row({i_s21_g00_53},{v_s21_g00_53_003})::public.cle_valeur) over (partition by {i_s21_g00_51}))}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (130, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'unitmesureref', '{v_s21_g00_40_011}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (131, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'validite', 'coalesce(CASE WHEN EXTRACT (DAY FROM ({validite}  :: TIMESTAMP)) <> ''1'' THEN date_trunc(''month'', {validite} :: TIMESTAMP)::text ELSE {validite} END,''2000-01-01'')', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (131, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v003', 'M', 'test_regle_globale', '{:select count(*) from arc.nmcl_code_pays_etranger_2017}', NULL);

