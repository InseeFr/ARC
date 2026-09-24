-- ============================================================
-- Jeu d'essai : comparaison de deux jeux de règles
--
-- Référence :
--   TEST-REF / A / 2020-01-01 / 2100-01-01 / v1
--
-- Comparé :
--   TEST-COMP / A / 2020-01-01 / 2100-01-01 / v1
--
-- Objectif :
--   - règles identiques
--   - AJOUT
--   - SUPPRESSION
--   - MODIFICATION
--   - plusieurs règles partageant une même clé
--   - types ne permettant pas MODIFICATION
-- ============================================================


-- ============================================================
-- FAMILLES
-- ============================================================

INSERT INTO arc.ihm_famille (id_famille)
VALUES ('FAMILLETESTREF');

INSERT INTO arc.ihm_famille (id_famille)
VALUES ('FAMILLETESTCOMP');


-- ============================================================
-- NORMES
--
-- Les contenus sont différents.
-- Selon la règle métier retenue pour Norme :
--   SUPPRESSION de la norme de référence
--   AJOUT de la norme comparée
-- et PAS de MODIFICATION.
-- ============================================================

INSERT INTO arc.ihm_norme
(id_norme, periodicite, def_norme, def_validite, etat, id_famille)
VALUES
    ('TEST-REF', 'A',
     'definition norme reference',
     'definition validite commune',
     '1',
     'FAMILLETESTREF');

INSERT INTO arc.ihm_norme
(id_norme, periodicite, def_norme, def_validite, etat, id_famille)
VALUES
    ('TEST-COMP', 'A',
     'definition norme comparee',
     'definition validite commune',
     '1',
     'FAMILLETESTCOMP');

-- ============================================================
-- CALENDRIER
-- ============================================================

INSERT INTO arc.ihm_calendrier VALUES ('TEST-REF', 'A', '2020-01-01', '2100-01-01', 24, '1');

INSERT INTO arc.ihm_calendrier VALUES ('TEST-COMP', 'A', '2020-01-01', '2100-01-01', 25, '1');
-- ============================================================
-- JEUX DE REGLES
-- ============================================================

INSERT INTO arc.ihm_jeuderegle
(id_norme, periodicite, validite_inf, validite_sup, version, etat)
VALUES
    ('TEST-REF', 'A', '2020-01-01', '2100-01-01', 'v1', 'inactif');

INSERT INTO arc.ihm_jeuderegle
(id_norme, periodicite, validite_inf, validite_sup, version, etat)
VALUES
    ('TEST-COMP', 'A', '2020-01-01', '2100-01-01', 'v1', 'inactif');


-- ============================================================
-- CHARGEMENT
--
-- Pas de critère d'appariement.
--
-- REF  : CSV
-- COMP : XML
--
-- => MODIFICATION
-- ============================================================

INSERT INTO arc.ihm_chargement_regle
(id_regle, id_norme, validite_inf, validite_sup,
 version, periodicite, type_fichier, delimiter,
 format, commentaire)
VALUES
    (90001, 'TEST-REF', '2020-01-01', '2100-01-01',
     'v1', 'A', 'plat', ';',
     'FORMAT_REFERENCE', 'commentaire ignoré');

INSERT INTO arc.ihm_chargement_regle
(id_regle, id_norme, validite_inf, validite_sup,
 version, periodicite, type_fichier, delimiter,
 format, commentaire)
VALUES
    (90002, 'TEST-COMP', '2020-01-01', '2100-01-01',
     'v1', 'A', 'xml-complexe', NULL,
     'FORMAT_COMPARE', 'autre commentaire ignoré');


-- ============================================================
-- NORMAGE
--
-- clé = (rubrique, rubrique_nmcl)
--
-- ============================================================

-- IDENTIQUE
INSERT INTO arc.ihm_normage_regle
(id_norme, periodicite, validite_inf, validite_sup, "version",
 id_classe, rubrique, rubrique_nmcl, id_regle, todo, commentaire)
VALUES
    ('TEST-REF', 'A', '2020-01-01', '2100-01-01', 'v1',
     'relation', 'V_TEST_001', 'V_TEST_002', 90101, NULL, 'REF');

INSERT INTO arc.ihm_normage_regle
(id_norme, periodicite, validite_inf, validite_sup, "version",
 id_classe, rubrique, rubrique_nmcl, id_regle, todo, commentaire)
VALUES
    ('TEST-COMP', 'A', '2020-01-01', '2100-01-01', 'v1',
     'relation', 'V_TEST_001', 'V_TEST_002', 90102, NULL, 'COMP');


-- SUPPRESSION
INSERT INTO arc.ihm_normage_regle
(id_norme, periodicite, validite_inf, validite_sup, "version",
 id_classe, rubrique, rubrique_nmcl, id_regle, todo, commentaire)
VALUES
    ('TEST-REF', 'A', '2020-01-01', '2100-01-01', 'v1',
     'relation', 'V_TEST_003', 'V_TEST_004', 90103, NULL, NULL);


-- AJOUT
INSERT INTO arc.ihm_normage_regle
(id_norme, periodicite, validite_inf, validite_sup, "version",
 id_classe, rubrique, rubrique_nmcl, id_regle, todo, commentaire)
VALUES
    ('TEST-COMP', 'A', '2020-01-01', '2100-01-01', 'v1',
     'relation', 'V_TEST_005', 'V_TEST_006', 90104, NULL, NULL);


-- ============================================================
-- CONTROLE
--
-- clé = (rubrique_pere, rubrique_fils)
--
-- Cas particulièrement intéressant :
-- une même clé contient plusieurs règles.
--
-- REF :
--   condition A
--   condition B
--
-- COMP :
--   condition A       -> éliminée car identique
--   condition C
--
-- Résultat attendu :
--   UNE MODIFICATION :
--      référence = condition B
--      comparé   = condition C
-- ============================================================

INSERT INTO arc.ihm_controle_regle VALUES
    ('TEST-REF', 'A', '2020-01-01', '2100-01-01', 'v1',
     'CONDITION', 'PERE_MULTI', 'FILS_MULTI',
     NULL, NULL, 'condition A', NULL,
     90201, NULL, 'identique',
     NULL, NULL, NULL, '>0%', 'e');

INSERT INTO arc.ihm_controle_regle VALUES
    ('TEST-REF', 'A', '2020-01-01', '2100-01-01', 'v1',
     'CONDITION', 'PERE_MULTI', 'FILS_MULTI',
     NULL, NULL, 'condition B', NULL,
     90202, NULL, 'ancienne condition',
     NULL, NULL, NULL, '>0%', 'e');

INSERT INTO arc.ihm_controle_regle VALUES
    ('TEST-COMP', 'A', '2020-01-01', '2100-01-01', 'v1',
     'CONDITION', 'PERE_MULTI', 'FILS_MULTI',
     NULL, NULL, 'condition A', NULL,
     90203, NULL, 'commentaire différent mais ignoré',
     NULL, NULL, NULL, '>0%', 'e');

INSERT INTO arc.ihm_controle_regle VALUES
    ('TEST-COMP', 'A', '2020-01-01', '2100-01-01', 'v1',
     'CONDITION', 'PERE_MULTI', 'FILS_MULTI',
     NULL, NULL, 'condition C', NULL,
     90204, NULL, 'nouvelle condition',
     NULL, NULL, NULL, '>0%', 'e');


-- SUPPRESSION

INSERT INTO arc.ihm_controle_regle VALUES
    ('TEST-REF', 'A', '2020-01-01', '2100-01-01', 'v1',
     'CARDINALITE', 'PERE_SUPPR', 'FILS_SUPPR',
     '1', '1', NULL, NULL,
     90205, NULL, 'règle supprimée',
     NULL, NULL, NULL, '>0%', 'e');


-- AJOUT

INSERT INTO arc.ihm_controle_regle VALUES
    ('TEST-COMP', 'A', '2020-01-01', '2100-01-01', 'v1',
     'CARDINALITE', 'PERE_AJOUT', 'FILS_AJOUT',
     '1', '1', NULL, NULL,
     90206, NULL, 'règle ajoutée',
     NULL, NULL, NULL, '>0%', 'e');


-- ============================================================
-- MAPPING
--
-- clé = variable_sortie
--
-- IDENTIQUE => rien
-- MODIF     => MODIFICATION
-- SUPPR     => SUPPRESSION
-- AJOUT     => AJOUT
-- ============================================================

INSERT INTO arc.ihm_mapping_regle
(id_regle, id_norme, validite_inf, validite_sup,
 version, periodicite, variable_sortie,
 expr_regle_col, commentaire)
VALUES
    (90301, 'TEST-REF', '2020-01-01', '2100-01-01',
     'v1', 'A', 'VAR_IDENTIQUE', '{v_source}', 'REF');

INSERT INTO arc.ihm_mapping_regle
(id_regle, id_norme, validite_inf, validite_sup,
 version, periodicite, variable_sortie,
 expr_regle_col, commentaire)
VALUES
    (90302, 'TEST-COMP', '2020-01-01', '2100-01-01',
     'v1', 'A', 'VAR_IDENTIQUE', '{v_source}', 'COMP');


INSERT INTO arc.ihm_mapping_regle
(id_regle, id_norme, validite_inf, validite_sup,
 version, periodicite, variable_sortie,
 expr_regle_col, commentaire)
VALUES
    (90303, 'TEST-REF', '2020-01-01', '2100-01-01',
     'v1', 'A', 'VAR_MODIF', '{ancienne_expression}', NULL);

INSERT INTO arc.ihm_mapping_regle
(id_regle, id_norme, validite_inf, validite_sup,
 version, periodicite, variable_sortie,
 expr_regle_col, commentaire)
VALUES
    (90304, 'TEST-COMP', '2020-01-01', '2100-01-01',
     'v1', 'A', 'VAR_MODIF', '{nouvelle_expression}', NULL);


INSERT INTO arc.ihm_mapping_regle
(id_regle, id_norme, validite_inf, validite_sup,
 version, periodicite, variable_sortie,
 expr_regle_col, commentaire)
VALUES
    (90305, 'TEST-REF', '2020-01-01', '2100-01-01',
     'v1', 'A', 'VAR_SUPPR', '{expression_supprimee}', NULL);


INSERT INTO arc.ihm_mapping_regle
(id_regle, id_norme, validite_inf, validite_sup,
 version, periodicite, variable_sortie,
 expr_regle_col, commentaire)
VALUES
    (90306, 'TEST-COMP', '2020-01-01', '2100-01-01',
     'v1', 'A', 'VAR_AJOUT', '{expression_ajoutee}', NULL);


-- ============================================================
-- EXPRESSIONS
--
-- clé = (expr_nom, expr_valeur)
--
-- Une modification de valeur doit être vue comme :
--   SUPPRESSION ancienne expression
--   AJOUT nouvelle expression
--
-- PAS de MODIFICATION.
-- ============================================================

-- Identique

INSERT INTO arc.ihm_expression
VALUES
    (90401, 'TEST-REF', '2020-01-01', '2100-01-01',
     'v1', 'A', 'EXPR_IDENTIQUE', '{x}=1', NULL);

INSERT INTO arc.ihm_expression
VALUES
    (90402, 'TEST-COMP', '2020-01-01', '2100-01-01',
     'v1', 'A', 'EXPR_IDENTIQUE', '{x}=1', NULL);


-- Même nom, valeur différente :
-- SUPPRESSION + AJOUT

INSERT INTO arc.ihm_expression
VALUES
    (90403, 'TEST-REF', '2020-01-01', '2100-01-01',
     'v1', 'A', 'EXPR_CHANGE', '{x}=1', NULL);

INSERT INTO arc.ihm_expression
VALUES
    (90404, 'TEST-COMP', '2020-01-01', '2100-01-01',
     'v1', 'A', 'EXPR_CHANGE', '{x}=2', NULL);


-- Suppression pure

INSERT INTO arc.ihm_expression
VALUES
    (90405, 'TEST-REF', '2020-01-01', '2100-01-01',
     'v1', 'A', 'EXPR_SUPPR', '{suppr}=true', NULL);


-- Ajout pur

INSERT INTO arc.ihm_expression
VALUES
    (90406, 'TEST-COMP', '2020-01-01', '2100-01-01',
     'v1', 'A', 'EXPR_AJOUT', '{ajout}=true', NULL);


-- ============================================================
-- TABLES METIER
--
-- clé = nom_table_metier
-- contenu = description
-- ============================================================

INSERT INTO arc.ihm_mod_table_metier
(id_famille, nom_table_metier, description_table_metier)
VALUES
    ('FAMILLETESTREF', 'TABLE_IDENTIQUE', 'Description identique');

INSERT INTO arc.ihm_mod_table_metier
(id_famille, nom_table_metier, description_table_metier)
VALUES
    ('FAMILLETESTCOMP', 'TABLE_IDENTIQUE', 'Description identique');


INSERT INTO arc.ihm_mod_table_metier
(id_famille, nom_table_metier, description_table_metier)
VALUES
    ('FAMILLETESTREF', 'TABLE_MODIF', 'Description avant');

INSERT INTO arc.ihm_mod_table_metier
(id_famille, nom_table_metier, description_table_metier)
VALUES
    ('FAMILLETESTCOMP', 'TABLE_MODIF', 'Description après');


INSERT INTO arc.ihm_mod_table_metier
(id_famille, nom_table_metier, description_table_metier)
VALUES
    ('FAMILLETESTREF', 'TABLE_SUPPR', 'Table supprimée');


INSERT INTO arc.ihm_mod_table_metier
(id_famille, nom_table_metier, description_table_metier)
VALUES
    ('FAMILLETESTCOMP', 'TABLE_AJOUT', 'Table ajoutée');


-- ============================================================
-- VARIABLES METIER
--
-- clé = (nom_table_metier, nom_variable_metier)
-- contenu =
--   type_variable_metier
--   description_variable_metier
--   type_consolidation
-- ============================================================

INSERT INTO arc.ihm_mod_variable_metier
(id_famille, nom_table_metier, nom_variable_metier,
 type_variable_metier, description_variable_metier,
 type_consolidation)
VALUES
    ('FAMILLETESTREF', 'TABLE_IDENTIQUE', 'VAR_IDENTIQUE',
     'text', 'Variable identique', NULL);

INSERT INTO arc.ihm_mod_variable_metier
(id_famille, nom_table_metier, nom_variable_metier,
 type_variable_metier, description_variable_metier,
 type_consolidation)
VALUES
    ('FAMILLETESTCOMP', 'TABLE_IDENTIQUE', 'VAR_IDENTIQUE',
     'text', 'Variable identique', NULL);


INSERT INTO arc.ihm_mod_variable_metier
(id_famille, nom_table_metier, nom_variable_metier,
 type_variable_metier, description_variable_metier,
 type_consolidation)
VALUES
    ('FAMILLETESTREF', 'TABLE_MODIF', 'VAR_MODIF',
     'text', 'Description avant', NULL);

INSERT INTO arc.ihm_mod_variable_metier
(id_famille, nom_table_metier, nom_variable_metier,
 type_variable_metier, description_variable_metier,
 type_consolidation)
VALUES
    ('FAMILLETESTCOMP', 'TABLE_MODIF', 'VAR_MODIF',
     'integer', 'Description après', NULL);


INSERT INTO arc.ihm_mod_variable_metier
(id_famille, nom_table_metier, nom_variable_metier,
 type_variable_metier, description_variable_metier,
 type_consolidation)
VALUES
    ('FAMILLETESTREF', 'TABLE_SUPPR', 'VAR_SUPPR',
     'text', 'Variable supprimée', NULL);


INSERT INTO arc.ihm_mod_variable_metier
(id_famille, nom_table_metier, nom_variable_metier,
 type_variable_metier, description_variable_metier,
 type_consolidation)
VALUES
    ('FAMILLETESTCOMP', 'TABLE_AJOUT', 'VAR_AJOUT',
     'text', 'Variable ajoutée', NULL);


-- ============================================================
-- CLIENTS
--
-- clé = id_application
-- pas de contenu à comparer
--
-- => uniquement AJOUT / SUPPRESSION
-- ============================================================

-- Identique

INSERT INTO arc.ihm_client
(id_famille, id_application)
VALUES
    ('FAMILLETESTREF', 'CLIENT_IDENTIQUE');

INSERT INTO arc.ihm_client
(id_famille, id_application)
VALUES
    ('FAMILLETESTCOMP', 'CLIENT_IDENTIQUE');


-- Suppression

INSERT INTO arc.ihm_client
(id_famille, id_application)
VALUES
    ('FAMILLETESTREF', 'CLIENT_SUPPR');


-- Ajout

INSERT INTO arc.ihm_client
(id_famille, id_application)
VALUES
    ('FAMILLETESTCOMP', 'CLIENT_AJOUT');