package fr.insee.arc.core.service.p5mapping.dao;

import java.sql.Connection;
import java.util.Set;

import fr.insee.arc.core.service.p5mapping.bo.TableMapping;
import fr.insee.arc.core.service.p5mapping.bo.VariableMapping;
import fr.insee.arc.core.service.p5mapping.bo.rules.AbstractRegleMapping;
import fr.insee.arc.core.service.p5mapping.bo.rules.RegleMappingClePrimaire;
import fr.insee.arc.core.service.p5mapping.bo.rules.RegleMappingCorrespondanceFonctionnelle;
import fr.insee.arc.core.service.p5mapping.bo.rules.RegleMappingGlobale;
import fr.insee.arc.core.service.p5mapping.bo.rules.RegleMappingGroupe;
import fr.insee.arc.core.service.p5mapping.bo.rules.AbstractRegleMappingSimple.CodeSQL;
import fr.insee.arc.core.service.p5mapping.bo.rules.AbstractRegleMappingSimple.RubriqueMapping;

/**
 *
 * Génère les règles de mapping, en vue de construire la requête SQL de mapping.<br/>
 * Entrée : une chaîne de caractères (colonne {@code expr_regle_col}).<br/>
 * Sortie : un objet héritant de {@link AbstractRegleMapping}.
 *
 */
public class MappingQueriesFactory {

    /**
     * Ensemble des rubriques qui ont été rencontrées dans les tables de chargement pour les données issues d'un même entrepot.<br/>
     * Plus concrètement, c'est le nom des colonnes de la table physique de l'étape précédente.
     */
    private Set<String> ensembleNomRubriqueExistante;
    private Set<String> ensembleIdentifiantRubriqueExistante;

    private Set<TableMapping> ensembleTableMapping;

    private String environnement;

    private Connection connexion;

    private String idFamille;

    public MappingQueriesFactory(Connection aConnexion, String anEnvironnement, Set<String> anEnsembleIdentifiantRubriqueExistante,
            Set<String> anEnsembleNomRubriqueExistante) {
        this.environnement = anEnvironnement;
        this.ensembleIdentifiantRubriqueExistante = anEnsembleIdentifiantRubriqueExistante;
        this.ensembleNomRubriqueExistante = anEnsembleNomRubriqueExistante;
        this.connexion = aConnexion;
    }

    public AbstractRegleMapping get(String anExpression, VariableMapping aVariableMapping) {
        /*
         * Est-ce que c'est une règle de clef primaire ?
         */
        if (anExpression.matches(RegleMappingClePrimaire.REGEXP_PRIMARY_KEY_RULE)) {
            return new RegleMappingClePrimaire(anExpression, this.idFamille, aVariableMapping);
        }
        /*
         * Est-ce que c'est une règle de groupe ?
         */
        else if (anExpression.matches(RegleMappingGroupe.REGEX_REGLE_GROUPE)) {
            return new RegleMappingGroupe(this, anExpression, aVariableMapping);
        }
        /*
         * Est-ce que c'est une règle globale ?
         */
        else if (anExpression.matches(RegleMappingGlobale.REGEX_REGLE_GLOBALE)) {
            return new RegleMappingGlobale(this.connexion, anExpression, this.environnement, this.ensembleTableMapping, aVariableMapping);
        }
        /*
         * Est-ce que c'est un nom de rubrique ?
         */
        else if (anExpression.matches(RubriqueMapping.REGEXP_RUBRIQUE_MAPPING_ACCEPTANTE)) {
            return new RubriqueMapping(anExpression, aVariableMapping, this.ensembleIdentifiantRubriqueExistante, this.ensembleNomRubriqueExistante);
        }
        /*
         * Est-ce que c'est une règle de correspondance fonctionnelle ?
         */
        else if (anExpression.matches(CodeSQL.REGEXP_TO_FIND_IDENTIFIER_RUBRIQUE) && anExpression.matches(CodeSQL.REGEXP_TO_FIND_NONIDENTIFIER_RUBRIQUE)) {
            return new CodeSQL(anExpression, aVariableMapping);
        }
        return new RegleMappingCorrespondanceFonctionnelle(this, anExpression, aVariableMapping);
    }

    /**
     * @param ensembleTableMapping
     *            the ensembleTableMapping to set
     */
    public void setEnsembleTableMapping(Set<TableMapping> ensembleTableMapping) {
        this.ensembleTableMapping = ensembleTableMapping;
    }

    /**
     * @param idFamille
     *            the idFamille to set
     */
    public void setIdFamille(String idFamille) {
        this.idFamille = idFamille;
    }

}
