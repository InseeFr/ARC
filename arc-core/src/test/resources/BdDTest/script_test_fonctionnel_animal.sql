
update arc.ext_etat_jeuderegle set env_description = 'Tests fonctionnels ANIMAL' where id='arc_bas8';

--
-- TOC entry 3658 (class 0 OID 43074435)
-- Dependencies: 237
-- Data for Name: ihm_famille; Type: TABLE DATA; Schema: arc; Owner: -
--

INSERT INTO arc.ihm_famille VALUES ('ANIMAL');

--
-- TOC entry 3665 (class 0 OID 43074489)
-- Dependencies: 245
-- Data for Name: ihm_norme; Type: TABLE DATA; Schema: arc; Owner: -
--
INSERT INTO arc.ihm_norme VALUES ('ANIMAL', 'A', 'select 1 from alias_table where id_source like ''DEFAULT_animals%''', 'select ''2023-01-01''', 41, '1', 'ANIMAL');

--
-- TOC entry 7353 (class 0 OID 346532)
-- Dependencies: 311
-- Data for Name: service_env_locked; Type: TABLE DATA; Schema: arc; Owner: -
--
INSERT INTO arc.ihm_calendrier VALUES ('ANIMAL', 'A', '2020-01-01', '3000-01-01', 24, '1');

--
-- TOC entry 3659 (class 0 OID 43074447)
-- Dependencies: 238
-- Data for Name: ihm_jeuderegle; Type: TABLE DATA; Schema: arc; Owner: -
--
INSERT INTO arc.ihm_jeuderegle VALUES ('ANIMAL', 'A', '2020-01-01', '3000-01-01', 'v001', 'arc.bas8', NULL, NULL);

--
-- TOC entry 3653 (class 0 OID 43074403)
-- Dependencies: 232
-- Data for Name: ihm_chargement_regle; Type: TABLE DATA; Schema: arc; Owner: -
--

INSERT INTO arc.ihm_chargement_regle  VALUES (1, 'ANIMAL', '2020-01-01', '3000-01-01', 'v001', 'A', 'plat', ';', '<encoding>ISO-8859-1</encoding>', 'load rules for animals');
