
update arc.ext_etat_jeuderegle set env_description = 'Tests fonctionnels DSN' where id='arc_bas2';

--
-- TOC entry 3656 (class 0 OID 43074423)
-- Dependencies: 235
-- Data for Name: ihm_entrepot; Type: TABLE DATA; Schema: arc; Owner: -
--

INSERT INTO arc.ihm_entrepot VALUES ('GIP', 'XML');

--
-- TOC entry 3658 (class 0 OID 43074435)
-- Dependencies: 237
-- Data for Name: ihm_famille; Type: TABLE DATA; Schema: arc; Owner: -
--

INSERT INTO arc.ihm_famille VALUES ('DSN');


--
-- TOC entry 3661 (class 0 OID 43074465)
-- Dependencies: 241
-- Data for Name: ihm_mod_table_metier; Type: TABLE DATA; Schema: arc; Owner: -
--

INSERT INTO arc.ihm_mod_table_metier VALUES ('DSN', 'mapping_dsn_penibilite_ok', NULL);
INSERT INTO arc.ihm_mod_table_metier VALUES ('DSN', 'mapping_dsn_employeur_ok', NULL);
INSERT INTO arc.ihm_mod_table_metier VALUES ('DSN', 'mapping_dsn_personne_ok', NULL);
INSERT INTO arc.ihm_mod_table_metier VALUES ('DSN', 'mapping_dsn_poste_ok', NULL);
INSERT INTO arc.ihm_mod_table_metier VALUES ('DSN', 'mapping_dsn_remuneration_ok', NULL);
INSERT INTO arc.ihm_mod_table_metier VALUES ('DSN', 'mapping_dsn_arret_ok', NULL);


--
-- TOC entry 3662 (class 0 OID 43074471)
-- Dependencies: 242
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
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_poste_ok', 'id_employeur', 'bigint', 'identification technique de employeur', '{exclus}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_employeur_ok', 'id_employeur', 'bigint', 'identification technique de employeur', '{exclus}');
INSERT INTO arc.ihm_mod_variable_metier VALUES ('DSN', 'mapping_dsn_employeur_ok', 'test_regle_globale', 'bigint', 'test de la classe RegleMappingGlobale', '{exclus}');


--
-- TOC entry 3665 (class 0 OID 43074489)
-- Dependencies: 245
-- Data for Name: ihm_norme; Type: TABLE DATA; Schema: arc; Owner: -
--

INSERT INTO arc.ihm_norme VALUES ('PHASE3V1', 'M', 'select 1 from alias_table where substring(ligne from ''>(.*)</n4ds:S10_G00_00_006>'') IN (''P03V01'',''P18V01'',''P19V01'',''P20V01'',''P21V01'',''P22V01'') and split_part(id_source,''_'',2) not like ''PA%''', 'select substring(ligne from ''>(.*)</n4ds:S20_G00_05_005>'') from alias_table where substring(ligne from ''>(.*)</n4ds:S20_G00_05_005>'') is not null', 22, '1', 'DSN');

--
-- TOC entry 7353 (class 0 OID 346532)
-- Dependencies: 311
-- Data for Name: service_env_locked; Type: TABLE DATA; Schema: arc; Owner: -
--
INSERT INTO arc.ihm_calendrier VALUES ('PHASE3V1', 'M', '2015-01-01', '2050-12-31', 16, '1');

--
-- TOC entry 3659 (class 0 OID 43074447)
-- Dependencies: 238
-- Data for Name: ihm_jeuderegle; Type: TABLE DATA; Schema: arc; Owner: -
--

INSERT INTO arc.ihm_jeuderegle VALUES ('PHASE3V1', 'M', '2015-01-01', '2050-12-31', 'v002', 'arc.bas2', NULL, NULL);

--
-- TOC entry 3653 (class 0 OID 43074403)
-- Dependencies: 232
-- Data for Name: ihm_chargement_regle; Type: TABLE DATA; Schema: arc; Owner: -
--

INSERT INTO arc.ihm_chargement_regle VALUES (1, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'xml', NULL, NULL, NULL);

--
-- TOC entry 3570 (class 0 OID 43163522)
-- Dependencies: 400
-- Data for Name: ihm_controle_regle; Type: TABLE DATA; Schema: arc; Owner: -
--

INSERT INTO arc.ihm_controle_regle VALUES ('PHASE3V1', 'M', '2015-01-01', '2050-12-31', 'v002', 'ALPHANUM', 'V_S20_G00_05_001', NULL, '2', '2', NULL, 'lpad({V_S20_G00_05_001},2,''0'')', 1, NULL, NULL, NULL, NULL, NULL, NULL, 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('PHASE3V1', 'M', '2015-01-01', '2050-12-31', 'v002', 'ALPHANUM', 'V_S20_G00_05_002', NULL, '2', '2', NULL, 'lpad({V_S20_G00_05_002},2,''0'')', 2, NULL, NULL, NULL, NULL, NULL, NULL, 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('PHASE3V1', 'M', '2015-01-01', '2050-12-31', 'v002', 'ALPHANUM', 'V_S10_G00_00_008', NULL, '2', '2', NULL, 'lpad({V_S10_G00_00_008},2,''0'')', 3, NULL, NULL, NULL, NULL, NULL, NULL, 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('PHASE3V1', 'M', '2015-01-01', '2050-12-31', 'v002', 'ALPHANUM', 'V_S21_G00_30_301', NULL, NULL, NULL, NULL, 'case when arc.isdate({v_s21_g00_30_301},''DDMMYYYY'') is true then substr({v_s21_g00_30_301},5,4)||''-''||substr({v_s21_g00_30_301},3,2)||''-''||substr({v_s21_g00_30_301},1,2)  end', 5, NULL, NULL, NULL, NULL, NULL, NULL, 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('PHASE3V1', 'M', '2015-01-01', '2050-12-31', 'v002', 'ALPHANUM', 'V_S21_G00_30_006', NULL, NULL, NULL, NULL, 'case when {v_s21_g00_30_006} like ''%-%'' then {v_s21_g00_30_006} else case WHEN substr({v_s21_g00_30_006},5,4)=''9999'' THEN (substr(current_date::text,1,4)::integer - 40)::text ELSE substr({v_s21_g00_30_006},5,4) END|| ''-''|| CASE WHEN substr({v_s21_g00_30_006},3,2)=''99'' THEN ''07'' ELSE substr({v_s21_g00_30_006},3,2) END|| ''-''|| CASE WHEN substr({v_s21_g00_30_006},1,2)=''99'' THEN ''15'' ELSE substr({v_s21_g00_30_006},1,2) END end', 4, NULL, NULL, NULL, NULL, NULL, NULL, 'e');
INSERT INTO arc.ihm_controle_regle VALUES ('PHASE3V1', 'M', '2015-01-01', '2050-12-31', 'v002', 'CONDITION', NULL, NULL, NULL, NULL, 'NOT({V_S10_G00_00_005}=''01'' or substring({validite},1,4) in (''2016'',''2017'',''2018'') or (to_char(current_date,''dd'')::int<16
  and substring({validite},6,2)::int=to_char(current_date::date-interval ''1 month'',''MM'')::int
  and substring({validite},1,4)::int=to_char(current_date::date-interval ''1 month'',''YYYY'')::int) )', NULL, 6, NULL, 'règle de filtrage', NULL, NULL, NULL, '>0u', 'e');

  
--
-- TOC entry 3569 (class 0 OID 43163516)
-- Dependencies: 399
-- Data for Name: ihm_normage_regle; Type: TABLE DATA; Schema: arc; Owner: -
--

INSERT INTO arc.ihm_normage_regle VALUES ('PHASE3V1', 'M', '2015-01-01', '2050-12-31', 'v002', 'relation', 'V_S21_G00_40_019', 'V_S21_G00_85_001', 1, NULL, 'relation lieu de travail maison mere');
INSERT INTO arc.ihm_normage_regle VALUES ('PHASE3V1', 'M', '2015-01-01', '2050-12-31', 'v002', 'relation', 'V_S21_G00_40_046', 'siretutil.V_S21_G00_85_001', 2, NULL, 'relation lieu de travail siret utilisateur');
INSERT INTO arc.ihm_normage_regle VALUES ('PHASE3V1', 'M', '2015-01-01', '2050-12-31', 'v002', 'relation', 'V_S21_G00_40_009', 'V_S21_G00_52_006', 3, NULL, 'numéro de contrat / prime');
INSERT INTO arc.ihm_normage_regle VALUES ('PHASE3V1', 'M', '2015-01-01', '2050-12-31', 'v002', 'relation', 'V_S21_G00_40_009', 'V_S21_G00_51_010', 4, NULL, 'numero de contrat / rémunération');
INSERT INTO arc.ihm_normage_regle VALUES ('PHASE3V1', 'M', '2015-01-01', '2050-12-31', 'v002', 'relation', 'V_S21_G00_40_009', 'V_S21_G00_34_002', 5, NULL, 'numéro de contrat / pénibilité');
INSERT INTO arc.ihm_normage_regle VALUES ('PHASE3V1', 'M', '2015-01-01', '2050-12-31', 'v002', 'unicité', 'siretutil.V_S21_G00_85_001', NULL, 6, NULL, NULL);
INSERT INTO arc.ihm_normage_regle VALUES ('PHASE3V1', 'M', '2015-01-01', '2050-12-31', 'v002', 'unicité', 'V_S21_G00_85_001', NULL, 7, NULL, NULL);
INSERT INTO arc.ihm_normage_regle VALUES ('PHASE3V1', 'M', '2015-01-01', '2050-12-31', 'v002', 'partition', 's21_g00_30', '500,100', 8, NULL, 'si > 500 individus, decoupage en paquet de 100 individus');
INSERT INTO arc.ihm_normage_regle VALUES ('PHASE3V1', 'M', '2015-01-01', '2050-12-31', 'v002', 'independance', 'm_s21_g00_51', 'v_s21_g00_51_010', 9, NULL, NULL);
INSERT INTO arc.ihm_normage_regle VALUES ('PHASE3V1', 'M', '2015-01-01', '2050-12-31', 'v002', 'independance', 'm_s21_g00_52', 'v_s21_g00_52_006', 10, NULL, NULL);

--
-- TOC entry 3568 (class 0 OID 43163509)
-- Dependencies: 398
-- Data for Name: ihm_mapping_regle; Type: TABLE DATA; Schema: arc; Owner: -
--

INSERT INTO arc.ihm_mapping_regle VALUES (46, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'id_employeur', '{pk:mapping_dsn_employeur_ok}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (115, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'siret', 'coalesce(concat(coalesce({v_s21_g00_06_001},''NOTSIREN''),coalesce({v_s21_g00_11_001},''NOTNIC'')),''S''||to_char(clock_timestamp(),''SSSS'')||to_char(clock_timestamp(),''US'')||''#''||(random()*1000)::integer)', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (55, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'indicebrut_cotisemploisup_fp', '{v_s21_g00_40_061}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (1, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'adresse_etab', 'trim(concat(trim({v_s21_g00_11_003}),'' '',trim({v_s21_g00_11_006}),'' '',trim({v_s21_g00_11_007})))', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (2, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'adresse_sal', 'trim(concat(trim({v_s21_g00_30_008}),'' '',trim({v_s21_g00_30_016}),'' '',trim({v_s21_g00_30_017})))', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (3, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'adresse_siege', 'trim(concat(trim({v_s21_g00_06_004}),'' '',trim({v_s21_g00_06_007}),'' '',trim({v_s21_g00_06_008})))', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (4, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'an_penibilite', '{{1}{v_s21_g00_34_003}}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (5, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'apen', 'COALESCE({v_s21_g00_06_003},''0000Z'')', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (6, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'apet', 'COALESCE({v_s21_g00_11_002},''0000Z'')', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (7, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'apet_lieu_trav', '{v_s21_g00_85_002}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (8, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'ccpayes', '''NON''', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (9, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'cj', '{v_s21_g00_11_012}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (10, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'code_categservice_fp', '{v_s21_g00_40_056}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (11, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'code_ccp', '{v_s21_g00_40_022}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (12, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'code_risque_acc', '{v_s21_g00_40_040}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (13, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'codification_ue', '{v_s21_g00_30_013}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (14, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'commune_lieu_trav', '{v_s21_g00_85_011}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (15, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'compl_rem_oblig', '{v_s21_g00_40_016}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (16, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'convcoll', '{v_s21_g00_40_017}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (17, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'cpost_etab', 'COALESCE({v_s21_g00_11_004},''VIIDE'')', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (18, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'cpost_lieu_trav', '{v_s21_g00_85_004}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (19, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'cpost_sal', '{v_s21_g00_30_009}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (20, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'cpost_siege', '{v_s21_g00_06_005}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (21, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'cumul_empl_retr', '{v_s21_g00_30_023}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (22, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'date_debut_arret', '{{1}{v_s21_g00_60_002}::date + ''1 day''::interval}{{2}{v_s21_g00_65_002}::date}{{3}{v_s21_g00_66_001}::date}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (23, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'date_debut_contrat', 'to_date({v_s21_g00_40_001}, ''YYYY-MM-DD'')', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (24, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'date_debut_rem', '{{1}to_date({v_s21_g00_51_001}, ''YYYY-MM-DD'')}{{2}to_date({v_s21_g00_52_003}, ''YYYY-MM-DD'')}{{3}to_date({v_s21_g00_54_003}, ''YYYY-MM-DD'')}{{4}to_date({v_s21_g00_50_001},''YYYY-MM-DD'')}{{5}to_date({v_s21_g00_50_001},''YYYY-MM-DD'')}{{6}to_date({v_s21_g00_78_002}, ''YYYY-MM-DD'')}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (25, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'date_fin_arret', '{{1}{v_s21_g00_60_010}::date - ''1 day''::interval}{{2}{v_s21_g00_65_003}::date}{{3}{v_s21_g00_66_002}::date}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (26, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'date_fin_contrat', 'to_date({v_s21_g00_62_001},''YYYY-MM-DD'')', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (27, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'date_fin_rem', '{{1}to_date({v_s21_g00_51_002}, ''YYYY-MM-DD'')}{{2}to_date({v_s21_g00_52_004}, ''YYYY-MM-DD'')}{{3}to_date({v_s21_g00_54_004}, ''YYYY-MM-DD'')}{{4}to_date({v_s21_g00_50_001},''YYYY-MM-DD'')}{{5} to_date({v_s21_g00_50_001},''YYYY-MM-DD'')}{{6}to_date({v_s21_g00_78_003}, ''YYYY-MM-DD'')}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (28, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'date_finprev_contrat', 'to_date({v_s21_g00_40_010}, ''YYYY-MM-DD'')', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (29, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'date_versement', 'to_date({v_s21_g00_50_001},''YYYY-MM-DD'')', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (30, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'dateinteg', '{date_integration}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (31, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'datenais', 'to_date(coalesce({v_s21_g00_30_006},''1800-01-01''), ''YYYY-MM-DD'')', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (32, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'datenais_sngi', 'case when {v_s21_g00_30_305} IN (''00'',''10'') then to_date({v_s21_g00_30_301},''YYYY-MM-DD'') end', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (33, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'depnais', 'COALESCE(CASE WHEN LENGTH(TRIM({v_s21_g00_30_014})) = 1 THEN CONCAT(''0'',TRIM({v_s21_g00_30_014})) ELSE TRIM({v_s21_g00_30_014}) END , ''00'')', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (34, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'detaches_expatries', '{v_s21_g00_40_024}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (35, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'dispol', '{v_s21_g00_40_008}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (36, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'editeur', '{v_s10_g00_00_002}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (37, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'eff_finperiode_empl', '{v_s21_g00_11_008}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (38, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'emploi_mult', 'case when trim({v_s21_g00_40_036}) =''02'' then ''OUI'' when trim({v_s21_g00_40_036})=''01'' then ''NON'' end', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (39, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'employeur_mult', 'case when trim({v_s21_g00_40_037}) in (''02'') then ''OUI'' when trim({v_s21_g00_40_037}) in (''01'') then ''NON'' end', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (40, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'fichier_source', '{id_source}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (41, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'formation', '{v_s21_g00_30_024}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (42, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'grparr', '{{1}''ARR''||{i_s21_g00_60}}{{2}''SUS''||{i_s21_g00_65}}{{3}''TPT''||{i_s21_g00_66}}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (43, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'grppen', '{{1}''PEN''||{i_s21_g00_34}}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (44, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'grprem', '{{1}''REM''||coalesce({i_s21_g00_51}::text,'''')}{{2}''PRI''||coalesce({i_s21_g00_52}::text,''''){{3}''ARV''||coalesce({i_s21_g00_54}::text,'''')}{{4}''VFV''||coalesce({i_s21_g00_50}::text,'''')}{{5}''VPE''||coalesce({i_s21_g00_50}::text,'''')}{{6}''BAS''||coalesce({i_s21_g00_78}::text,'''')}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (45, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'id_arret', '{pk:mapping_dsn_arret_ok}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (47, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'id_penibilite', '{pk:mapping_dsn_penibilite_ok}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (48, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'id_personne', '{pk:mapping_dsn_personne_ok}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (49, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'id_poste', '{pk:mapping_dsn_poste_ok}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (50, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'id_remuneration', '{pk:mapping_dsn_remuneration_ok}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (51, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'id_source', '{id_source}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (52, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'indicebrut_ancienorange_fp', '{v_s21_g00_40_062}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (53, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'indicebrut_ancienposte_fp', '{v_s21_g00_40_063}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (54, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'indicebrut_contractueltitu_fp', '{v_s21_g00_40_065}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (56, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'indicebrut_fp', '{v_s21_g00_40_057}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (57, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'indicebrut_origine_fp', '{v_s21_g00_40_060}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (58, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'indicebrut_originespp_fp', '{v_s21_g00_40_064}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (59, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'indicemajore', '{v_s21_g00_40_058}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (60, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'libcom_etab', 'TRIM({v_s21_g00_11_005})', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (61, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'libcom_sal', 'Trim({v_s21_g00_30_010})', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (62, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'libcom_siege', 'trim({v_s21_g00_06_006})', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (63, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'libemploi', 'trim({v_s21_g00_40_006})', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (64, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'lieu_travail', ' coalesce({v_s21_g00_40_019},''NR'')', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (65, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'localite_lieu_trav', '{v_s21_g00_85_005}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (66, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'logiciel', '{v_s10_g00_00_001}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (67, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'mesureactv', '{{1}(public.array_agg_distinct(row({i_s21_g00_53},{v_s21_g00_53_002})::public.cle_valeur) over (partition by {i_s21_g00_51}))}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (68, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'mod_tp_trav', '{v_s21_g00_40_014}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (69, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'montant', '{{1}{v_s21_g00_51_013}}{{2}{v_s21_g00_52_002}}{{3}{v_s21_g00_54_002}}{{4}{v_s21_g00_50_002}}{{5}{v_s21_g00_50_004}}{{6}{v_s21_g00_78_004}}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (70, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'motif_exclu', '{v_s21_g00_40_025}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (71, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'motif_reprise', '{{1}{v_s21_g00_60_011}}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (72, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'motifcdd', ' {v_s21_g00_40_021}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (73, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'motifin', '{v_s21_g00_62_002}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (74, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'nat_contrat', '{v_s21_g00_40_007}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (75, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'nat_jur_employeur', '{v_s21_g00_11_017}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (76, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'nat_lieu_trav', '{V_S21_G00_85_010}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (77, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'nat_poste_fp', '{v_s21_g00_40_053}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (78, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'nat_siret_util', '{V_S21_G00_85_010_siretutil}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (79, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'nbhrsup', '{{1}{v_s21_g00_51_012}}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (80, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'nbi_fp', '{v_s21_g00_40_059}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (81, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'nir_sngi', 'case when {v_s21_g00_30_305} IN (''00'',''10'') then {v_s21_g00_30_300} end', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (82, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'nom_sal', 'TRIM({v_s21_g00_30_002})', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (83, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'nom_usage', 'TRIM({v_s21_g00_30_003})', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (84, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'norme', '{id_norme}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (85, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'ntt', '{v_s21_g00_30_020}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (86, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'num_contrat', '{v_s21_g00_40_009}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (87, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'num_versement', '{v_s21_g00_50_003}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (88, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'numpersonne', 'coalesce({v_s21_g00_30_001},{v_s21_g00_30_020},''N''||to_char(clock_timestamp(),''SSSS'')||to_char(clock_timestamp(),''US'')||''#''||(random()*1000)::integer)', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (89, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'payer', '{{1}(public.array_agg_distinct(row({i_s21_g00_53},case when trim({v_s21_g00_53_001}) IN (''01'',''03'') Then ''OUI'' when trim({v_s21_g00_53_001})=''02'' then ''NON'' end)::public.cle_valeur) over (partition by {i_s21_g00_51}))}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (90, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'pays_lieu_trav', '{v_s21_g00_85_006}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (91, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'pays_naissance_insee', 'case when {v_s21_g00_30_014}=''99'' then coalesce((select code1 from nmcl_code_pays_etranger_2017 a WHERE {v_s21_g00_30_015}=a.code0),''99000'') else {v_s21_g00_30_015} end', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (92, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'pays_residence_sal', 'COALESCE ({v_s21_g00_30_011},''FR'')', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (93, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'pcs', 'COALESCE ({v_s21_g00_40_004},''000X'')', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (94, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'pcs_complement', '{v_s21_g00_40_005}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (95, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'pcs_complement_ese_fp', '{v_s21_g00_40_052}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (96, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'penibilite', '{{1}{v_s21_g00_34_001}}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (97, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'periodicite', '{periodicite}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (98, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'perte_tpt', '{{3}{v_s21_g00_66_003}}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (99, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'pos_detach_fp', '{{2}{v_s21_g00_65_004}}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (100, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'positionnement_convcoll', '{v_s21_g00_40_041}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (101, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'pourboire', '{v_s21_g00_40_045}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (102, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'prenom_sal', 'TRIM({v_s21_g00_30_004})', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (103, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'presence', '{{1}(public.array_agg_distinct(row({i_s21_g00_53},case when TRIM({v_s21_g00_53_001}) = ''01'' Then ''OUI'' when TRIM({v_s21_g00_53_001}) IN (''02'',''03'') then ''NON'' end)::public.cle_valeur) over (partition by {i_s21_g00_51}))}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (104, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'quotite', '{v_s21_g00_40_013}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (105, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'quotite_ref', '{v_s21_g00_40_012}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (106, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'quotite_ref_fp', '{v_s21_g00_40_054}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (107, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'raisonabsence', '{{1}''ARR''||{v_s21_g00_60_001}}{{2}''SUS''||{v_s21_g00_65_001}}{{3}''TPT''}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (108, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'regmal', ' {v_s21_g00_40_018}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (109, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'regviel', ' {v_s21_g00_40_020}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (110, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'resid_etab', 'coalesce({v_s21_g00_11_015},''FR'')', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (111, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'resid_siege', 'coalesce({v_s21_g00_06_010},''FR'')', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (112, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'rs_etab', '{v_s21_g00_11_904}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (113, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'rs_ul', '{v_s21_g00_06_903}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (114, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'sexe', 'COALESCE(CASE WHEN {v_s21_g00_30_005} IS NOT NULL THEN LTRIM({v_s21_g00_30_005},''0'')   WHEN {v_s21_g00_30_001} IS NOT NULL THEN SUBSTR({v_s21_g00_30_001},1,1) ELSE SUBSTR({v_s21_g00_30_020},1,1) END, ''0'')', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (116, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'siret_utilisateur', 'coalesce({v_s21_g00_40_046},''NR'')', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (117, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'srcrem', 'public.curr_val(''arc.number_generator'')', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (118, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'statcat', 'CASE WHEN {v_s21_g00_40_018}=''300'' then ''MSA''||{v_s21_g00_40_042} else ''RGG''||{v_s21_g00_40_003} end', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (119, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'statconv', '{v_s21_g00_40_002}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (120, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'statempl', '{v_s21_g00_40_026}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (121, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'statut_boeth', '{v_s21_g00_40_072}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (122, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'statut_etranger', '{v_s21_g00_30_022}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (123, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'tx_cotis_acc', '{v_s21_g00_40_043}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (124, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'tx_remun_fp', '{{1}{v_s21_g00_51_014}}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (125, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'txfraisprof', '{v_s21_g00_40_023}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (126, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'txtempspartiel_fp', '{v_s21_g00_40_055}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (127, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'type_detachement_fp', '{v_s21_g00_40_066}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (128, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'typrem', '{{1}''REM''||{v_s21_g00_51_011}}{{2}''PRI''||{v_s21_g00_52_001}}{{3}''ARV''||{v_s21_g00_54_001}}{{4}''VFV''}{{5}''VPE''}{{6}''BAS''||{v_s21_g00_78_001}}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (129, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'unitmesureactv', '{{1}(public.array_agg_distinct(row({i_s21_g00_53},{v_s21_g00_53_003})::public.cle_valeur) over (partition by {i_s21_g00_51}))}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (130, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'unitmesureref', '{v_s21_g00_40_011}', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (131, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'validite', 'coalesce(CASE WHEN EXTRACT (DAY FROM ({validite}  :: TIMESTAMP)) <> ''1'' THEN date_trunc(''month'', {validite} :: TIMESTAMP)::text ELSE {validite} END,''2000-01-01'')', NULL);
INSERT INTO arc.ihm_mapping_regle VALUES (131, 'PHASE3V1', '2015-01-01', '2050-12-31', 'v002', 'M', 'test_regle_globale', '{:select count(*) from arc.nmcl_code_pays_etranger_2017}', NULL);

--
-- TOC entry 3663 (class 0 OID 43074477)
-- Dependencies: 243
-- Data for Name: ihm_nmcl; Type: TABLE DATA; Schema: arc; Owner: -
--

INSERT INTO arc.ihm_nmcl VALUES ('nmcl_code_pays_etranger_2017', NULL);

--
-- TOC entry 256 (class 1259 OID 43074553)
-- Name: nmcl_code_pays_etranger_2017; Type: TABLE; Schema: arc; Owner: -
--

CREATE TABLE arc.nmcl_code_pays_etranger_2017 (
    nomcol0 text,
    code0 text,
    lib0 text,
    nomcol1 text,
    code1 text,
    lib1 text
);


--
-- TOC entry 3674 (class 0 OID 43074553)
-- Dependencies: 256
-- Data for Name: nmcl_code_pays_etranger_2017; Type: TABLE DATA; Schema: arc; Owner: -
--

INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'FO', 'FEROE (ILES)', 'code_pays_etranger', '99101', 'FEROE (ILES)');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'DK', 'DANEMARK', 'code_pays_etranger', '99101', 'DANEMARK');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'IS', 'ISLANDE', 'code_pays_etranger', '99102', 'ISLANDE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'NO', 'NORVEGE', 'code_pays_etranger', '99103', 'NORVEGE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'SJ', 'SVALBARD et ILE JAN MAYEN', 'code_pays_etranger', '99103', 'SVALBARD et ILE JAN MAYEN');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'BV', 'BOUVET (ILE)', 'code_pays_etranger', '99103', 'BOUVET (ILE)');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'SE', 'SUEDE', 'code_pays_etranger', '99104', 'SUEDE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'FI', 'FINLANDE', 'code_pays_etranger', '99105', 'FINLANDE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'EE', 'ESTONIE', 'code_pays_etranger', '99106', 'ESTONIE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'LV', 'LETTONIE', 'code_pays_etranger', '99107', 'LETTONIE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'LT', 'LITUANIE', 'code_pays_etranger', '99108', 'LITUANIE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'DE', 'ALLEMAGNE', 'code_pays_etranger', '99109', 'ALLEMAGNE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'AT', 'AUTRICHE', 'code_pays_etranger', '99110', 'AUTRICHE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'BG', 'BULGARIE', 'code_pays_etranger', '99111', 'BULGARIE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'HU', 'HONGRIE', 'code_pays_etranger', '99112', 'HONGRIE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'LI', 'LIECHTENSTEIN', 'code_pays_etranger', '99113', 'LIECHTENSTEIN');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'RO', 'ROUMANIE', 'code_pays_etranger', '99114', 'ROUMANIE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'CZ', 'TCHEQUE (REPUBLIQUE)', 'code_pays_etranger', '99116', 'TCHEQUE (REPUBLIQUE)');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'SK', 'SLOVAQUIE', 'code_pays_etranger', '99117', 'SLOVAQUIE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'BA', 'BOSNIE-HERZEGOVINE', 'code_pays_etranger', '99118', 'BOSNIE-HERZEGOVINE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'HR', 'CROATIE', 'code_pays_etranger', '99119', 'CROATIE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'ME', 'MONTENEGRO', 'code_pays_etranger', '99120', 'MONTENEGRO');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'RS', 'SERBIE', 'code_pays_etranger', '99121', 'SERBIE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'PL', 'POLOGNE', 'code_pays_etranger', '99122', 'POLOGNE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'RU', 'RUSSIE', 'code_pays_etranger', '99123', 'RUSSIE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'AL', 'ALBANIE', 'code_pays_etranger', '99125', 'ALBANIE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'GR', 'GRECE', 'code_pays_etranger', '99126', 'GRECE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'IT', 'ITALIE', 'code_pays_etranger', '99127', 'ITALIE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'SM', 'SAINT-MARIN', 'code_pays_etranger', '99128', 'SAINT-MARIN');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'VA', 'VATICAN, ou SAINT-SIEGE', 'code_pays_etranger', '99129', 'VATICAN, ou SAINT-SIEGE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'AD', 'ANDORRE', 'code_pays_etranger', '99130', 'ANDORRE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'BE', 'BELGIQUE', 'code_pays_etranger', '99131', 'BELGIQUE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'GG', 'GUERNESEY', 'code_pays_etranger', '99132', 'GUERNESEY');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'IM', 'MAN (ILE)', 'code_pays_etranger', '99132', 'MAN (ILE)');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'GB', 'ROYAUME-UNI', 'code_pays_etranger', '99132', 'ROYAUME-UNI');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'JE', 'JERSEY', 'code_pays_etranger', '99132', 'JERSEY');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'GI', 'GIBRALTAR', 'code_pays_etranger', '99133', 'GIBRALTAR');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'ES', 'ESPAGNE', 'code_pays_etranger', '99134', 'ESPAGNE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'AW', 'ARUBA', 'code_pays_etranger', '99135', 'ARUBA');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'NL', 'PAYS-BAS', 'code_pays_etranger', '99135', 'PAYS-BAS');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'IE', 'IRLANDE, ou EIRE', 'code_pays_etranger', '99136', 'IRLANDE, ou EIRE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'LU', 'LUXEMBOURG', 'code_pays_etranger', '99137', 'LUXEMBOURG');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'MC', 'MONACO', 'code_pays_etranger', '99138', 'MONACO');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'PT', 'PORTUGAL', 'code_pays_etranger', '99139', 'PORTUGAL');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'CH', 'SUISSE', 'code_pays_etranger', '99140', 'SUISSE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'MT', 'MALTE', 'code_pays_etranger', '99144', 'MALTE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'SI', 'SLOVENIE', 'code_pays_etranger', '99145', 'SLOVENIE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'BY', 'BIELORUSSIE', 'code_pays_etranger', '99148', 'BIELORUSSIE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'MD', 'MOLDAVIE', 'code_pays_etranger', '99151', 'MOLDAVIE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'UA', 'UKRAINE', 'code_pays_etranger', '99155', 'UKRAINE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'MK', 'EX-REPUBLIQUE YOUGOSLAVE DE', 'code_pays_etranger', '99156', 'EX-REPUBLIQUE YOUGOSLAVE DE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'XK', 'KOSOVO', 'code_pays_etranger', '99157', 'KOSOVO');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'SA', 'ARABIE SAOUDITE', 'code_pays_etranger', '99201', 'ARABIE SAOUDITE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'IQ', 'IRAQ', 'code_pays_etranger', '99203', 'IRAQ');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'IR', 'IRAN', 'code_pays_etranger', '99204', 'IRAN');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'LB', 'LIBAN', 'code_pays_etranger', '99205', 'LIBAN');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'SY', 'SYRIE', 'code_pays_etranger', '99206', 'SYRIE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'IL', 'ISRAEL', 'code_pays_etranger', '99207', 'ISRAEL');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'TR', 'TURQUIE', 'code_pays_etranger', '99208', 'TURQUIE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'AF', 'AFGHANISTAN', 'code_pays_etranger', '99212', 'AFGHANISTAN');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'PK', 'PAKISTAN', 'code_pays_etranger', '99213', 'PAKISTAN');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'BT', 'BHOUTAN', 'code_pays_etranger', '99214', 'BHOUTAN');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'NP', 'NEPAL', 'code_pays_etranger', '99215', 'NEPAL');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'CN', 'CHINE', 'code_pays_etranger', '99216', 'CHINE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'JP', 'JAPON', 'code_pays_etranger', '99217', 'JAPON');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'TH', 'THAILANDE', 'code_pays_etranger', '99219', 'THAILANDE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'PH', 'PHILIPPINES', 'code_pays_etranger', '99220', 'PHILIPPINES');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'JO', 'JORDANIE', 'code_pays_etranger', '99222', 'JORDANIE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'IN', 'INDE', 'code_pays_etranger', '99223', 'INDE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'MM', 'BIRMANIE', 'code_pays_etranger', '99224', 'BIRMANIE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'BN', 'BRUNEI', 'code_pays_etranger', '99225', 'BRUNEI');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'SG', 'SINGAPOUR', 'code_pays_etranger', '99226', 'SINGAPOUR');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'MY', 'MALAISIE', 'code_pays_etranger', '99227', 'MALAISIE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'MV', 'MALDIVES', 'code_pays_etranger', '99229', 'MALDIVES');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'HK', 'HONG-KONG', 'code_pays_etranger', '99230', 'HONG-KONG');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'ID', 'INDONESIE', 'code_pays_etranger', '99231', 'INDONESIE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'MO', 'MACAO', 'code_pays_etranger', '99232', 'MACAO');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'KH', 'CAMBODGE', 'code_pays_etranger', '99234', 'CAMBODGE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'LK', 'SRI LANKA', 'code_pays_etranger', '99235', 'SRI LANKA');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'TW', 'TAIWAN', 'code_pays_etranger', '99236', 'TAIWAN');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'KP', 'COREE (REPUBLIQUE POPULAIRE', 'code_pays_etranger', '99238', 'COREE (REPUBLIQUE POPULAIRE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'KR', 'COREE (REPUBLIQUE DE)', 'code_pays_etranger', '99239', 'COREE (REPUBLIQUE DE)');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'KW', 'KOWEIT', 'code_pays_etranger', '99240', 'KOWEIT');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'LA', 'LAOS', 'code_pays_etranger', '99241', 'LAOS');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'MN', 'MONGOLIE', 'code_pays_etranger', '99242', 'MONGOLIE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'VN', 'VIET NAM', 'code_pays_etranger', '99243', 'VIET NAM');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'BD', 'BANGLADESH', 'code_pays_etranger', '99246', 'BANGLADESH');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'AE', 'EMIRATS ARABES UNIS', 'code_pays_etranger', '99247', 'EMIRATS ARABES UNIS');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'QA', 'QATAR', 'code_pays_etranger', '99248', 'QATAR');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'BH', 'BAHREIN', 'code_pays_etranger', '99249', 'BAHREIN');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'OM', 'OMAN', 'code_pays_etranger', '99250', 'OMAN');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'YE', 'YEMEN', 'code_pays_etranger', '99251', 'YEMEN');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'AM', 'ARMENIE', 'code_pays_etranger', '99252', 'ARMENIE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'AZ', 'AZERBAIDJAN', 'code_pays_etranger', '99253', 'AZERBAIDJAN');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'CY', 'CHYPRE', 'code_pays_etranger', '99254', 'CHYPRE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'GE', 'GEORGIE', 'code_pays_etranger', '99255', 'GEORGIE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'KZ', 'KAZAKHSTAN', 'code_pays_etranger', '99256', 'KAZAKHSTAN');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'KG', 'KIRGHIZISTAN', 'code_pays_etranger', '99257', 'KIRGHIZISTAN');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'UZ', 'OUZBEKISTAN', 'code_pays_etranger', '99258', 'OUZBEKISTAN');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'TJ', 'TADJIKISTAN', 'code_pays_etranger', '99259', 'TADJIKISTAN');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'TM', 'TURKMENISTAN', 'code_pays_etranger', '99260', 'TURKMENISTAN');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'PS', 'PALESTINE (Etat de)', 'code_pays_etranger', '99261', 'PALESTINE (Etat de)');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'TL', 'TIMOR ORIENTAL', 'code_pays_etranger', '99262', 'TIMOR ORIENTAL');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'EG', 'EGYPTE', 'code_pays_etranger', '99301', 'EGYPTE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'LR', 'LIBERIA', 'code_pays_etranger', '99302', 'LIBERIA');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'ZA', 'AFRIQUE DU SUD', 'code_pays_etranger', '99303', 'AFRIQUE DU SUD');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'GM', 'GAMBIE', 'code_pays_etranger', '99304', 'GAMBIE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'SH', 'SAINTE HELENE, ASCENSION ET', 'code_pays_etranger', '99306', 'SAINTE HELENE, ASCENSION ET');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'IO', 'OCEAN INDIEN (TERRITOIRE BR', 'code_pays_etranger', '99308', 'OCEAN INDIEN (TERRITOIRE BR');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'TZ', 'TANZANIE', 'code_pays_etranger', '99309', 'TANZANIE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'ZW', 'ZIMBABWE', 'code_pays_etranger', '99310', 'ZIMBABWE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'NA', 'NAMIBIE', 'code_pays_etranger', '99311', 'NAMIBIE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'CD', 'CONGO (REPUBLIQUE DEMOCRATI', 'code_pays_etranger', '99312', 'CONGO (REPUBLIQUE DEMOCRATI');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'GQ', 'GUINEE EQUATORIALE', 'code_pays_etranger', '99314', 'GUINEE EQUATORIALE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'ET', 'ETHIOPIE', 'code_pays_etranger', '99315', 'ETHIOPIE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'LY', 'LIBYE', 'code_pays_etranger', '99316', 'LIBYE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'ER', 'ERYTHREE', 'code_pays_etranger', '99317', 'ERYTHREE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'SO', 'SOMALIE', 'code_pays_etranger', '99318', 'SOMALIE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'BI', 'BURUNDI', 'code_pays_etranger', '99321', 'BURUNDI');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'CM', 'CAMEROUN', 'code_pays_etranger', '99322', 'CAMEROUN');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'CF', 'CENTRAFRICAINE (REPUBLIQUE)', 'code_pays_etranger', '99323', 'CENTRAFRICAINE (REPUBLIQUE)');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'CG', 'CONGO', 'code_pays_etranger', '99324', 'CONGO');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'CI', 'COTE D''IVOIRE', 'code_pays_etranger', '99326', 'COTE D''IVOIRE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'BJ', 'BENIN', 'code_pays_etranger', '99327', 'BENIN');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'GA', 'GABON', 'code_pays_etranger', '99328', 'GABON');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'GH', 'GHANA', 'code_pays_etranger', '99329', 'GHANA');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'GN', 'GUINEE', 'code_pays_etranger', '99330', 'GUINEE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'BF', 'BURKINA', 'code_pays_etranger', '99331', 'BURKINA');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'KE', 'KENYA', 'code_pays_etranger', '99332', 'KENYA');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'MG', 'MADAGASCAR', 'code_pays_etranger', '99333', 'MADAGASCAR');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'MW', 'MALAWI', 'code_pays_etranger', '99334', 'MALAWI');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'ML', 'MALI', 'code_pays_etranger', '99335', 'MALI');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'MR', 'MAURITANIE', 'code_pays_etranger', '99336', 'MAURITANIE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'NE', 'NIGER', 'code_pays_etranger', '99337', 'NIGER');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'NG', 'NIGERIA', 'code_pays_etranger', '99338', 'NIGERIA');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'UG', 'OUGANDA', 'code_pays_etranger', '99339', 'OUGANDA');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'RW', 'RWANDA', 'code_pays_etranger', '99340', 'RWANDA');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'SN', 'SENEGAL', 'code_pays_etranger', '99341', 'SENEGAL');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'SL', 'SIERRA LEONE', 'code_pays_etranger', '99342', 'SIERRA LEONE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'SD', 'SOUDAN', 'code_pays_etranger', '99343', 'SOUDAN');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'TD', 'TCHAD', 'code_pays_etranger', '99344', 'TCHAD');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'TG', 'TOGO', 'code_pays_etranger', '99345', 'TOGO');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'ZM', 'ZAMBIE', 'code_pays_etranger', '99346', 'ZAMBIE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'BW', 'BOTSWANA', 'code_pays_etranger', '99347', 'BOTSWANA');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'LS', 'LESOTHO', 'code_pays_etranger', '99348', 'LESOTHO');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'SS', 'SOUDAN DU SUD', 'code_pays_etranger', '99349', 'SOUDAN DU SUD');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'MA', 'MAROC', 'code_pays_etranger', '99350', 'MAROC');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'TN', 'TUNISIE', 'code_pays_etranger', '99351', 'TUNISIE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'DZ', 'ALGERIE', 'code_pays_etranger', '99352', 'ALGERIE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'EH', 'SAHARA OCCIDENTAL', 'code_pays_etranger', '99389', 'SAHARA OCCIDENTAL');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'MU', 'MAURICE', 'code_pays_etranger', '99390', 'MAURICE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'SZ', 'SWAZILAND', 'code_pays_etranger', '99391', 'SWAZILAND');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'GW', 'GUINEE-BISSAU', 'code_pays_etranger', '99392', 'GUINEE-BISSAU');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'MZ', 'MOZAMBIQUE', 'code_pays_etranger', '99393', 'MOZAMBIQUE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'ST', 'SAO TOME-ET-PRINCIPE', 'code_pays_etranger', '99394', 'SAO TOME-ET-PRINCIPE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'AO', 'ANGOLA', 'code_pays_etranger', '99395', 'ANGOLA');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'CV', 'CAP-VERT', 'code_pays_etranger', '99396', 'CAP-VERT');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'KM', 'COMORES', 'code_pays_etranger', '99397', 'COMORES');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'SC', 'SEYCHELLES', 'code_pays_etranger', '99398', 'SEYCHELLES');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'DJ', 'DJIBOUTI', 'code_pays_etranger', '99399', 'DJIBOUTI');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'CA', 'CANADA', 'code_pays_etranger', '99401', 'CANADA');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'US', 'ETATS-UNIS', 'code_pays_etranger', '99404', 'ETATS-UNIS');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'MX', 'MEXIQUE', 'code_pays_etranger', '99405', 'MEXIQUE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'CR', 'COSTA RICA', 'code_pays_etranger', '99406', 'COSTA RICA');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'CU', 'CUBA', 'code_pays_etranger', '99407', 'CUBA');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'DO', 'DOMINICAINE (REPUBLIQUE)', 'code_pays_etranger', '99408', 'DOMINICAINE (REPUBLIQUE)');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'GT', 'GUATEMALA', 'code_pays_etranger', '99409', 'GUATEMALA');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'HT', 'HAITI', 'code_pays_etranger', '99410', 'HAITI');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'HN', 'HONDURAS', 'code_pays_etranger', '99411', 'HONDURAS');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'NI', 'NICARAGUA', 'code_pays_etranger', '99412', 'NICARAGUA');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'PA', 'PANAMA', 'code_pays_etranger', '99413', 'PANAMA');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'SV', 'EL SALVADOR', 'code_pays_etranger', '99414', 'EL SALVADOR');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'AR', 'ARGENTINE', 'code_pays_etranger', '99415', 'ARGENTINE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'BR', 'BRESIL', 'code_pays_etranger', '99416', 'BRESIL');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'CL', 'CHILI', 'code_pays_etranger', '99417', 'CHILI');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'BO', 'BOLIVIE', 'code_pays_etranger', '99418', 'BOLIVIE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'CO', 'COLOMBIE', 'code_pays_etranger', '99419', 'COLOMBIE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'EC', 'EQUATEUR', 'code_pays_etranger', '99420', 'EQUATEUR');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'PY', 'PARAGUAY', 'code_pays_etranger', '99421', 'PARAGUAY');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'PE', 'PEROU', 'code_pays_etranger', '99422', 'PEROU');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'UY', 'URUGUAY', 'code_pays_etranger', '99423', 'URUGUAY');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'VE', 'VENEZUELA', 'code_pays_etranger', '99424', 'VENEZUELA');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'BM', 'BERMUDES', 'code_pays_etranger', '99425', 'BERMUDES');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'AI', 'ANGUILLA', 'code_pays_etranger', '99425', 'ANGUILLA');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'KY', 'CAIMANES (ILES)', 'code_pays_etranger', '99425', 'CAIMANES (ILES)');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'VG', 'VIERGES BRITANNIQUES (ILES)', 'code_pays_etranger', '99425', 'VIERGES BRITANNIQUES (ILES)');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'TC', 'TURKS ET CAIQUES (ILES)', 'code_pays_etranger', '99425', 'TURKS ET CAIQUES (ILES)');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'MS', 'MONTSERRAT', 'code_pays_etranger', '99425', 'MONTSERRAT');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'JM', 'JAMAIQUE', 'code_pays_etranger', '99426', 'JAMAIQUE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'FK', 'MALOUINES, OU FALKLAND (ILE', 'code_pays_etranger', '99427', 'MALOUINES, OU FALKLAND (ILE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'GS', 'GEORGIE DU SUD ET LES ILES', 'code_pays_etranger', '99427', 'GEORGIE DU SUD ET LES ILES');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'GY', 'GUYANA', 'code_pays_etranger', '99428', 'GUYANA');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'BZ', 'BELIZE', 'code_pays_etranger', '99429', 'BELIZE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'GL', 'GROENLAND', 'code_pays_etranger', '99430', 'GROENLAND');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'AN', 'ANTILLES NEERLANDAISES', 'code_pays_etranger', '99431', 'ANTILLES NEERLANDAISES');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'PR', 'PORTO RICO', 'code_pays_etranger', '99432', 'PORTO RICO');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'VI', 'VIERGES DES ETATS-UNIS (ILE', 'code_pays_etranger', '99432', 'VIERGES DES ETATS-UNIS (ILE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'TT', 'TRINITE-ET-TOBAGO', 'code_pays_etranger', '99433', 'TRINITE-ET-TOBAGO');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'BB', 'BARBADE', 'code_pays_etranger', '99434', 'BARBADE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'GD', 'GRENADE', 'code_pays_etranger', '99435', 'GRENADE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'BS', 'BAHAMAS', 'code_pays_etranger', '99436', 'BAHAMAS');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'SR', 'SURINAME', 'code_pays_etranger', '99437', 'SURINAME');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'DM', 'DOMINIQUE', 'code_pays_etranger', '99438', 'DOMINIQUE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'LC', 'SAINTE-LUCIE', 'code_pays_etranger', '99439', 'SAINTE-LUCIE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'VC', 'SAINT-VINCENT-ET-LES GRENAD', 'code_pays_etranger', '99440', 'SAINT-VINCENT-ET-LES GRENAD');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'AG', 'ANTIGUA-ET-BARBUDA', 'code_pays_etranger', '99441', 'ANTIGUA-ET-BARBUDA');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'KN', 'SAINT-CHRISTOPHE-ET-NIEVES', 'code_pays_etranger', '99442', 'SAINT-CHRISTOPHE-ET-NIEVES');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'BQ', 'BONAIRE, SAINT EUSTACHE ET', 'code_pays_etranger', '99443', 'BONAIRE, SAINT EUSTACHE ET');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'CW', 'CURA�AO', 'code_pays_etranger', '99444', 'CURA�AO');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'SX', 'SAINT-MARTIN (PARTIE NEERLA', 'code_pays_etranger', '99445', 'SAINT-MARTIN (PARTIE NEERLA');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'CX', 'CHRISTMAS (ILE)', 'code_pays_etranger', '99501', 'CHRISTMAS (ILE)');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'AU', 'AUSTRALIE', 'code_pays_etranger', '99501', 'AUSTRALIE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'CC', 'COCOS ou KEELING (ILES)', 'code_pays_etranger', '99501', 'COCOS ou KEELING (ILES)');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'HM', 'HEARD ET MACDONALD (ILES)', 'code_pays_etranger', '99501', 'HEARD ET MACDONALD (ILES)');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'NF', 'NORFOLK (ILE)', 'code_pays_etranger', '99501', 'NORFOLK (ILE)');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'CK', 'COOK (ILES)', 'code_pays_etranger', '99502', 'COOK (ILES)');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'TK', 'TOKELAU', 'code_pays_etranger', '99502', 'TOKELAU');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'NU', 'NIUE', 'code_pays_etranger', '99502', 'NIUE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'NZ', 'NOUVELLE-ZELANDE', 'code_pays_etranger', '99502', 'NOUVELLE-ZELANDE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'PN', 'PITCAIRN (ILE)', 'code_pays_etranger', '99503', 'PITCAIRN (ILE)');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'MP', 'MARIANNES DU NORD (ILES)', 'code_pays_etranger', '99505', 'MARIANNES DU NORD (ILES)');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'GU', 'GUAM', 'code_pays_etranger', '99505', 'GUAM');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'AS', 'SAMOA AMERICAINES', 'code_pays_etranger', '99505', 'SAMOA AMERICAINES');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'WS', 'SAMOA OCCIDENTALES', 'code_pays_etranger', '99506', 'SAMOA OCCIDENTALES');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'NR', 'NAURU', 'code_pays_etranger', '99507', 'NAURU');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'FJ', 'FIDJI', 'code_pays_etranger', '99508', 'FIDJI');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'TO', 'TONGA', 'code_pays_etranger', '99509', 'TONGA');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'PG', 'PAPOUASIE-NOUVELLE-GUINEE', 'code_pays_etranger', '99510', 'PAPOUASIE-NOUVELLE-GUINEE');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'TV', 'TUVALU', 'code_pays_etranger', '99511', 'TUVALU');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'SB', 'SALOMON (ILES)', 'code_pays_etranger', '99512', 'SALOMON (ILES)');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'KI', 'KIRIBATI', 'code_pays_etranger', '99513', 'KIRIBATI');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'VU', 'VANUATU', 'code_pays_etranger', '99514', 'VANUATU');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'MH', 'MARSHALL (ILES)', 'code_pays_etranger', '99515', 'MARSHALL (ILES)');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'FM', 'MICRONESIE (ETATS FEDERES D', 'code_pays_etranger', '99516', 'MICRONESIE (ETATS FEDERES D');
INSERT INTO arc.nmcl_code_pays_etranger_2017 VALUES ('code_pays_iso', 'PW', 'PALAOS (ILES)', 'code_pays_etranger', '99517', 'PALAOS (ILES)');


