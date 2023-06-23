--
-- PostgreSQL database dump
--

-- Dumped from database version 11.5
-- Dumped by pg_dump version 14.2

-- Started on 2023-06-23 09:00:07

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';


--
-- TOC entry 3589 (class 0 OID 43074435)
-- Dependencies: 240
-- Data for Name: ihm_famille; Type: TABLE DATA; Schema: arc; Owner: -
--

INSERT INTO arc.ihm_famille VALUES ('DSN');
INSERT INTO arc.ihm_famille VALUES ('PASRAU');


--
-- TOC entry 3588 (class 0 OID 43074409)
-- Dependencies: 236
-- Data for Name: ihm_client; Type: TABLE DATA; Schema: arc; Owner: -
--

INSERT INTO arc.ihm_client VALUES ('DSN', 'ARTEMIS');
INSERT INTO arc.ihm_client VALUES ('DSN', 'DSNFLASH');
INSERT INTO arc.ihm_client VALUES ('PASRAU', 'TEST');


--
-- TOC entry 3590 (class 0 OID 43074465)
-- Dependencies: 244
-- Data for Name: ihm_mod_table_metier; Type: TABLE DATA; Schema: arc; Owner: -
--

INSERT INTO arc.ihm_mod_table_metier VALUES ('DSN', 'mapping_dsn_penibilite_ok', NULL);
INSERT INTO arc.ihm_mod_table_metier VALUES ('DSN', 'mapping_dsn_employeur_ok', NULL);
INSERT INTO arc.ihm_mod_table_metier VALUES ('DSN', 'mapping_dsn_personne_ok', NULL);
INSERT INTO arc.ihm_mod_table_metier VALUES ('DSN', 'mapping_dsn_poste_ok', NULL);
INSERT INTO arc.ihm_mod_table_metier VALUES ('DSN', 'mapping_dsn_remuneration_ok', NULL);
INSERT INTO arc.ihm_mod_table_metier VALUES ('DSN', 'mapping_dsn_arret_ok', NULL);
INSERT INTO arc.ihm_mod_table_metier VALUES ('PASRAU', 'mapping_pasrau_etab_ok', '');
INSERT INTO arc.ihm_mod_table_metier VALUES ('PASRAU', 'mapping_pasrau_indiv_ok', '');
INSERT INTO arc.ihm_mod_table_metier VALUES ('PASRAU', 'mapping_pasrau_droit_ok', '');
INSERT INTO arc.ihm_mod_table_metier VALUES ('PASRAU', 'mapping_pasrau_versement_ok', '');
INSERT INTO arc.ihm_mod_table_metier VALUES ('PASRAU', 'mapping_pasrau_indu_ok', '');
INSERT INTO arc.ihm_mod_table_metier VALUES ('PASRAU', 'mapping_pasrau_modif_droit_ok', '');
INSERT INTO arc.ihm_mod_table_metier VALUES ('PASRAU', 'mapping_pasrau_modif_indiv_ok', '');
INSERT INTO arc.ihm_mod_table_metier VALUES ('PASRAU', 'mapping_pasrau_base_ass_ok', '');
INSERT INTO arc.ihm_mod_table_metier VALUES ('PASRAU', 'mapping_pasrau_remun_ok', '');
INSERT INTO arc.ihm_mod_table_metier VALUES ('PASRAU', 'mapping_pasrau_rev_autre_ok', '');
INSERT INTO arc.ihm_mod_table_metier VALUES ('PASRAU', 'mapping_pasrau_regul_pas_ok', '');
INSERT INTO arc.ihm_mod_table_metier VALUES ('PASRAU', 'mapping_pasrau_cotis_ind_ok', '');


--
-- TOC entry 3591 (class 0 OID 43074471)
-- Dependencies: 245
-- Data for Name: ihm_mod_variable_metier; Type: TABLE DATA; Schema: arc; Owner: -
--

INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_employeur_ok', 'cj', 'text', 'categorie juridique', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_employeur_ok', 'rs_ul', 'text', 'raison sociale de l unite legale', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_remuneration_ok', 'num_versement', 'text', 'numero identification du versement', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_remuneration_ok', 'grprem', 'text[]', 'Identifiant des groupes de rémunération', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_personne_ok', 'pays_residence_sal', 'text', 'pays residence du salarié', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_employeur_ok', 'cpost_siege', 'text', 'code postal du siege', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_remuneration_ok', 'date_fin_rem', 'date[]', 'date de fin de rémunération', '{max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_remuneration_ok', 'typrem', 'text[]', 'type de rémunération', '{max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_remuneration_ok', 'srcrem', 'bigint', 'identifiant de fichier de rémunération', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'unitmesureref', 'bigint', 'unité de mesure de la quotité de travail du contrat', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_employeur_ok', 'rs_etab', 'text', 'raison sociale de l etablissement', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_employeur_ok', 'adresse_etab', 'text', 'numero et adresse de etablissement', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_employeur_ok', 'adresse_siege', 'text', 'numero et adresse du siege', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'periodicite', 'text', 'Periodicité des données', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'statempl', 'text', 'statut emploi du salarié pour les salariés des employeurs publics', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_personne_ok', 'id_personne', 'bigint', 'identification technique de personne', '{exclus}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'id_personne', 'bigint', 'identification technique de personne', '{exclus}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_personne_ok', 'nir_sngi', 'text', 'le Nir certifié par la CNAV, ou NULL si la certification est insuffisante', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_employeur_ok', 'cpost_etab', 'text', 'code postal etab', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_personne_ok', 'datenais', 'date', 'date de naissance déclarée', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_employeur_ok', 'libcom_etab', 'text', 'libelle de commune de etablissement', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_employeur_ok', 'libcom_siege', 'text', 'libelle de commune du siege', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'positionnement_convcoll', 'text', 'code échelon ou coefficient dans la convention collective', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'quotite_ref', 'float', 'quotite de reference pour la meme catégorie de salariés dans l entreprise', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_employeur_ok', 'apen', 'text', 'activite principale entreprise', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_employeur_ok', 'apet', 'text', 'activite principale etablissement', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'convcoll', 'text', 'convention collective contrat', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'date_debut_contrat', 'date', 'date debut contrat', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'date_fin_contrat', 'date', 'date fin de contrat', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'detaches_expatries', 'text', 'salariés détachés, expatriés ou frontalier', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'dispol', 'text', 'dispositif aide , incitation ou politique publique emploi ou formation', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'mod_tp_trav', 'text', 'temps plein ou temps partiel', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_personne_ok', 'depnais', 'text', 'departement de naissance du salarie', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'libemploi', 'text', 'libelle de  emploi', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_employeur_ok', 'dateinteg', 'timestamp without time zone', 'datge integration das la base', '{array_agg}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_employeur_ok', 'eff_finperiode_empl', 'bigint', 'effectif établissement déclaré en fin de période', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_remuneration_ok', 'montant', 'float[]', 'montant de rémuneration', '{max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_remuneration_ok', 'nbhrsup', 'float[]', 'Volume horaire des heures supplémentaires', '{max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_personne_ok', 'cpost_sal', 'text', 'code postal du salarie', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_arret_ok', 'date_debut_arret', 'date[]', 'date de début d arret ou suspension', '{max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_personne_ok', 'nom_sal', 'text', 'nom de naissance du salarie', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_personne_ok', 'numpersonne', 'text', 'numero identification du salarie (nir, ntt, valeur defaut)', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'pcs', 'text', 'code des professions et catégories socioprofessionnelles des emplois salariés d’entreprise ', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_droit_ok', 'type_droit', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_personne_ok', 'ntt', 'text', 'numero temporaire identification individu', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_personne_ok', 'prenom_sal', 'text', 'prenom du salarie', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_personne_ok', 'datenais_sngi', 'date', 'Date de naissance du salarié certifiée par la CNAV, ou bien NULL si le degré de certification est insuffisant.', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'regmal', 'text', 'nature du regime maladie, table passage', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'regviel', 'text', 'nature du regime vieillesse, table passage', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_employeur_ok', 'resid_etab', 'text', 'pays de residence de etablissement', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_employeur_ok', 'resid_siege', 'text', 'pays de residence du siege', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_penibilite_ok', 'id_source', 'text', 'nom du fichier source', '{exclus}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_personne_ok', 'sexe', 'text', 'sexe du salarie', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_employeur_ok', 'siret', 'text', 'numero immatriculation de etablissement', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'statcat', 'text', 'Statut de la retraite complémentaire des salariés, ingénieurs et cadres', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'statconv', 'text', 'statut dans le sens de la convention collective applicable dans entreprise,table passage', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_arret_ok', 'grparr', 'text[]', 'groupe d arret', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_remuneration_ok', 'date_versement', 'date', 'Date de versement de la rémunération', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_personne_ok', 'adresse_sal', 'text', 'numero et adresse du salarie', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_arret_ok', 'id_source', 'text', 'nom du fichier source', '{exclus}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_arret_ok', 'id_poste', 'bigint', 'identification technique de poste', '{exclus}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_employeur_ok', 'ccpayes', 'text', 'caisses de conges payés', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'motifcdd', 'text', 'motif de recours au cdd, table passage', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'motifin', 'text', 'motif fin de contrat, table de passage', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_employeur_ok', 'id_source', 'text', 'nom du fichier source', '{exclus}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'quotite', 'float', 'durée contractuelle de travail applicable au salarie', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'id_poste', 'bigint', 'identification technique de poste', '{exclus}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_remuneration_ok', 'id_poste', 'bigint', 'identification technique de poste', '{exclus}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_penibilite_ok', 'id_penibilite', 'bigint', 'identifiant technique de penibilite', '{exclus}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_arret_ok', 'id_arret', 'bigint', 'identification technique de arret', '{exclus}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_remuneration_ok', 'id_remuneration', 'bigint', 'identification technique de remuneration', '{exclus}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'code_ccp', 'text', 'code de la caisse pro de congés payés', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'nat_siret_util', 'text', 'nature du siret utilisateur', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_arret_ok', 'date_fin_arret', 'date[]', 'date de fin d arret ou suspension', '{max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_arret_ok', 'motif_reprise', 'text[]', 'motif de la reprise après arrêt de travail', '{max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'employeur_mult', 'text', 'plusieurs employeurs pour un même salarié', '{max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'siret_utilisateur', 'text', 'siret utilisateur', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_personne_ok', 'nom_usage', 'text', 'nom usage', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_penibilite_ok', 'id_poste', 'bigint', 'identification technique de poste', '{exclus}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_personne_ok', 'pays_naissance_insee', 'text', 'pays de naissance', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_personne_ok', 'id_source', 'text', 'nom du fichier source', '{exclus}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_remuneration_ok', 'payer', 'text[]', 'activité rémunéree ou pas', '{max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_remuneration_ok', 'presence', 'text[]', 'presence du salarie dans etablissement', '{max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_remuneration_ok', 'unitmesureactv', 'text[]', 'unite expression  du volume  activité ou  inactivité', '{max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'pourboire', 'text', 'pourboire : oui / non', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_arret_ok', 'raisonabsence', 'text[]', 'raison de l absence', '{max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'txfraisprof', 'float', 'taux de déduction forfaitaire frais pro', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'emploi_mult', 'text', 'plusieurs emplois chez le même employeur', '{max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'lieu_travail', 'text', 'lieu de  travail du salarié', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'pcs_complement', 'text', 'complement à la PCS', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'motif_exclu', 'text', 'motif exclusion de la DSN', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_arret_ok', 'perte_tpt', 'float[]', 'perte de salaire lié au TP thérapeutique', '{max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'id_source', 'text', 'nom du fichier source', '{exclus}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_remuneration_ok', 'id_source', 'text', 'nom du fichier source', '{exclus}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_employeur_ok', 'validite', 'date', 'date de validite de la periode de paye', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_employeur_ok', 'fichier_source', 'text', 'fichier source', '{array_agg}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'nat_contrat', 'text', 'nature du contrat de travail, table de passage', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_employeur_ok', 'nat_jur_employeur', 'text', 'nature juridique de etablissement', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'nat_lieu_trav', 'text', 'nature du lieu de travail', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_remuneration_ok', 'mesureactv', 'text[]', 'volume activites ou inactivites', '{max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_remuneration_ok', 'date_debut_rem', 'date[]', 'date de début de rémunération', '{max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_penibilite_ok', 'an_penibilite', 'bigint[]', 'millésime de reference du bloc penibilite', '{array_agg}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_penibilite_ok', 'grppen', 'text[]', 'groupe de penibilite', '{array_agg}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_penibilite_ok', 'penibilite', 'text[]', 'facteur de penibilité', '{array_agg}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_personne_ok', 'libcom_sal', 'text', 'libelle de commune de residence du salarie', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_employeur_ok', 'norme', 'text', 'identifiant de la norme', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_employeur_ok', 'editeur', 'text', 'editeur', '{array_agg}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_personne_ok', 'codification_ue', 'text', 'origine du salarié selon frontières françaises et UE', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_personne_ok', 'statut_etranger', 'text', 'Travail frontalier à l etranger', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'apet_lieu_trav', 'text', 'apet dans le cas ou le lieu de travail n est pas le siret employeur', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'cpost_lieu_trav', 'text', 'code postal du lieu de travail', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'pcs_complement_ese_fp', 'text', 'complement à la PCS pour la FPE', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'nat_poste_fp', 'text', 'temps complet ou temps non complet pour la FP', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'quotite_ref_fp', 'float', 'duree hebdomadaire dans la FP dans le cas des temps non complets', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'txtempspartiel_fp', 'float', 'taux de travail à temps partiel pour la FP', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'code_categservice_fp', 'text', 'code pour distinguer les emplois présentant un risque particulier dans la FP', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'indicebrut_fp', 'text', 'indice brut ou indice de carrière dans la FP', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'date_finprev_contrat', 'date', 'date de fin prévisionnelle du contrat', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'nbi_fp', 'float', 'nombre de points indice majorés attribués à titre dérogatoire, complète le traitement principal fp', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'indicebrut_origine_fp', 'text', 'indice de classement dans la carrière origine pour fonctionnaire en détachement', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'indicebrut_cotisemploisup_fp', 'text', 'indice de classement précédemment détenu dans un emploi supérieur', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'indicebrut_ancienorange_fp', 'text', 'indice sur lequel ancien salariés Orange intégré dans la FPT cotise', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'indicebrut_ancienposte_fp', 'text', 'indice sur lequel ancien salarié la Poste intégré dans la FPT cotise', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'statut_boeth', 'text', 'statut Boeth', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'indicebrut_contractueltitu_fp', 'text', 'indice des agents contractuels devenus fonctionnaires qui conservent à titre personnel le bénéfice de leur traitement antérieur', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'type_detachement_fp', 'text', 'précise le type de détachement', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'indicebrut_originespp_fp', 'text', 'indice brut sapeur pompier', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_remuneration_ok', 'tx_remun_fp', 'float[]', 'rempli dans le cas où le taux remuneration FP est inférieur 100 pourcent', '{max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'num_contrat', 'text', 'numéro de contrat', '{clef}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_arret_ok', 'pos_detach_fp', 'text[]', 'position de détachement fp', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'indicemajore', 'text', 'indice de traitement, permet le calcul de la rémunération pour un agent de la FP', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_employeur_ok', 'logiciel', 'text', 'nom du logiciel utilisé', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_personne_ok', 'cumul_empl_retr', 'text', 'cumul emploi retraite', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'localite_lieu_trav', 'text', 'localité lieu de travail', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'pays_lieu_trav', 'text', 'code pays du lieu de travail', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'commune_lieu_trav', 'text', 'code insee du lieu de travail', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'code_risque_acc', 'text', 'code risque accident de travail', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'tx_cotis_acc', 'float', 'taux de cotisation accident de travail', '{max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'compl_rem_oblig', 'text', 'complément de base au régime obligatoire', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_personne_ok', 'formation', 'text', 'null', '{nombre_occurence_max}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_etab_ok', 'apet', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_etab_ok', 'cp_etab', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_etab_ok', 'id_etab', 'bigint', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_etab_ok', 'id_source', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_etab_ok', 'mois_decl', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_etab_ok', 'nic_siege', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_etab_ok', 'siren', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_etab_ok', 'siret', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_indiv_ok', 'com_nais_sngi', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_indiv_ok', 'date_nais_sngi', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_indiv_ok', 'id_source', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_indiv_ok', 'libcom', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_indiv_ok', 'mail', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_indiv_ok', 'ntt', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'id_employeur', 'bigint', 'identification technique de employeur', '{exclus}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_employeur_ok', 'id_employeur', 'bigint', 'identification technique de employeur', '{exclus}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_indiv_ok', 'date_nir_sngi', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_indiv_ok', 'dep_nais_sngi', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_indiv_ok', 'nir_sngi', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_droit_ok', 'date_fin_eff', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_versement_ok', 'class_rev', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_versement_ok', 'contrib_empl', 'float', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_versement_ok', 'contrib_soc_non_deduc', 'float', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_versement_ok', 'cotis_sal_compl', 'float', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_versement_ok', 'cotis_soc_deduc', 'float', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_indiv_ok', 'nom_fam', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_indiv_ok', 'nom_fam_sngi', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_indiv_ok', 'nom_mari_sngi', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_indiv_ok', 'nom_us', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_indiv_ok', 'pays', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_indiv_ok', 'pays_nais', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_indiv_ok', 'pays_nais_sngi', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_indiv_ok', 'prenom', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_indiv_ok', 'prenom_sngi', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_indiv_ok', 'sexe', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_droit_ok', 'date_dde_empl', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_droit_ok', 'date_deb_eff', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_droit_ok', 'id_droit', 'bigint', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_droit_ok', 'id_indiv', 'bigint', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_droit_ok', 'id_source', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_droit_ok', 'ident_droit', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_versement_ok', 'date_fin_rel', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_versement_ok', 'date_versement', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_versement_ok', 'id_indiv', 'bigint', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_versement_ok', 'id_source', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_versement_ok', 'id_versement', 'bigint', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_versement_ok', 'ident_taux_pas', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_versement_ok', 'montant_abat_base_fisc', 'float', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_versement_ok', 'montant_net', 'float', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_versement_ok', 'montant_non_resid', 'float', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_versement_ok', 'montant_part_non_imp', 'float', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_versement_ok', 'montant_pas', 'float', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_versement_ok', 'remun_net_fisc', 'float', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_versement_ok', 'remun_net_poten', 'float', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_versement_ok', 'taux_pas', 'float', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_versement_ok', 'type_taux_pas', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_indu_ok', 'class_rev_indu', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_indu_ok', 'date_deb_indu', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_indu_ok', 'date_fin_indu', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_indu_ok', 'date_rembours', 'bigint', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_indu_ok', 'id_droit', 'bigint', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_indu_ok', 'id_indiv', 'bigint', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_indu_ok', 'id_indu', 'bigint', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_indu_ok', 'id_source', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_indu_ok', 'ident_droit_indu', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_indu_ok', 'montant_part_imp_indu', 'float', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_indu_ok', 'montant_part_non_imp_indu', 'float', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_modif_droit_ok', 'anc_date_deb_eff', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_modif_droit_ok', 'anc_date_fin_eff', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_modif_droit_ok', 'anc_ident_droit', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_modif_droit_ok', 'anc_type_droit', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_modif_droit_ok', 'id_droit', 'bigint', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_modif_droit_ok', 'id_modif_droit', 'bigint', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_modif_droit_ok', 'id_source', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_modif_droit_ok', 'prof_recalc', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_modif_indiv_ok', 'anc_date_nais', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_modif_indiv_ok', 'anc_nir', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_modif_indiv_ok', 'anc_nom', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_modif_indiv_ok', 'anc_prenom', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_indiv_ok', 'cp', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_indiv_ok', 'date_nais', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_indiv_ok', 'indic_certif_sngi', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_indiv_ok', 'matricule', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_indiv_ok', 'nom_pays', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_droit_ok', 'date_deb_theo', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_versement_ok', 'montant_soumis_pas', 'float', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_versement_ok', 'no_versement', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_modif_droit_ok', 'anc_date_deb_theo', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_modif_droit_ok', 'date_modif_droit', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_modif_indiv_ok', 'date_change_ind', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_modif_indiv_ok', 'id_indiv', 'bigint', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_modif_indiv_ok', 'id_modif_indiv', 'bigint', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_modif_indiv_ok', 'id_source', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_base_ass_ok', 'code_base_ass', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_base_ass_ok', 'date_deb_base_ass', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_base_ass_ok', 'date_fin_base_ass', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_base_ass_ok', 'id_base_ass', 'bigint', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_base_ass_ok', 'id_droit', 'bigint', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_base_ass_ok', 'id_source', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_base_ass_ok', 'id_versement', 'bigint', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_base_ass_ok', 'ident_droit_base_ass', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_base_ass_ok', 'montant_base_ass', 'float', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_remun_ok', 'class_rev_remun', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_remun_ok', 'date_deb_remun', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_remun_ok', 'date_fin_remun', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_remun_ok', 'id_droit', 'bigint', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_remun_ok', 'id_remun', 'bigint', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_remun_ok', 'id_source', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_remun_ok', 'id_versement', 'bigint', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_remun_ok', 'ident_droit_remun', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_remun_ok', 'montant_remun', 'float', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_remun_ok', 'nb_jours', 'float', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_remun_ok', 'type_remun', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_rev_autre_ok', 'date_deb_rev_autre', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_rev_autre_ok', 'date_fin_rev_autre', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_rev_autre_ok', 'id_rev_autre', 'bigint', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_rev_autre_ok', 'id_source', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_rev_autre_ok', 'id_versement', 'bigint', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_rev_autre_ok', 'montant_rev_autre', 'float', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_rev_autre_ok', 'type_rev_autre', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_regul_pas_ok', 'class_rev_mois_erreur', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_regul_pas_ok', 'correction_class_rev', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_regul_pas_ok', 'id_regul_pas', 'bigint', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_etab_ok', 'adresse_etab', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_etab_ok', 'adresse_siege', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_etab_ok', 'apen', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_etab_ok', 'cat_jur', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_etab_ok', 'code_decl', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_etab_ok', 'compladr_etab', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_etab_ok', 'compladr_siege', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_etab_ok', 'cp_siege', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_etab_ok', 'date_ech', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_etab_ok', 'enseigne', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_etab_ok', 'libcom_etab', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_etab_ok', 'libcom_siege', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_etab_ok', 'nic_etab', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_etab_ok', 'raison_soc', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_etab_ok', 'secteur_activ', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_etab_ok', 'version_norm', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_indiv_ok', 'adresse', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_indiv_ok', 'code_distrib_etrang', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_indiv_ok', 'code_result_sngi', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_indiv_ok', 'compl_voie', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_indiv_ok', 'compladr', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_indiv_ok', 'dep_nais', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_indiv_ok', 'id_etab', 'bigint', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_indiv_ok', 'id_indiv', 'bigint', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_indiv_ok', 'lieu_nais', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_indiv_ok', 'nir', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_regul_pas_ok', 'id_source', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_regul_pas_ok', 'id_versement', 'bigint', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_regul_pas_ok', 'mois_erreur', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_regul_pas_ok', 'montant_abat_base_fisc_mois_erreur', 'float', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_regul_pas_ok', 'montant_part_non_imp_mois_erreur', 'float', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_regul_pas_ok', 'montant_pas_mois_erreur', 'float', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_regul_pas_ok', 'montant_regul_pas', 'float', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_regul_pas_ok', 'regul_montant_abatt', 'float', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_regul_pas_ok', 'regul_montant_pas', 'float', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_regul_pas_ok', 'regul_part_non_imp', 'float', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_regul_pas_ok', 'regul_remun_net_fisc', 'float', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_regul_pas_ok', 'regul_taux_pas', 'float', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_regul_pas_ok', 'remun_mois_erreur', 'float', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_regul_pas_ok', 'taux_mois_erreur', 'float', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_regul_pas_ok', 'type_erreur', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_cotis_ind_ok', 'code_cotis', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_cotis_ind_ok', 'id_base_ass', 'bigint', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_cotis_ind_ok', 'id_cotis_ind', 'bigint', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_cotis_ind_ok', 'id_source', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_cotis_ind_ok', 'ident_org', 'text', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_cotis_ind_ok', 'montant_assiette', 'float', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_cotis_ind_ok', 'montant_cotis', 'float', 'null', NULL);
INSERT INTO arc.ihm_mod_variable_metier VALUES ('PASRAU', 'mapping_pasrau_cotis_ind_ok', 'taux_cotis', 'float', 'null', NULL);


--
-- TOC entry 3592 (class 0 OID 43074523)
-- Dependencies: 254
-- Data for Name: ihm_webservice_whitelist; Type: TABLE DATA; Schema: arc; Owner: -
--

INSERT INTO arc.ihm_webservice_whitelist VALUES ('locahost', 'DSN', 'ARTEMIS', 'x');


-- Completed on 2023-06-23 09:00:07

--
-- PostgreSQL database dump complete
--

