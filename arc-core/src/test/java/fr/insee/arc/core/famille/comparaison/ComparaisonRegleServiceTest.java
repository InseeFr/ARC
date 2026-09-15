package fr.insee.arc.core.famille.comparaison;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.famille.model.NormageRegle;
import fr.insee.arc.core.service.global.bo.JeuDeRegle;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.query.TestDatabase;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.text.ParseException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ComparaisonRegleServiceTest {

    public static UtilitaireDao u = UtilitaireDao.get(0);

    public static Connection c = new TestDatabase().testConnection;

    private final ComparaisonRegleService service = new ComparaisonRegleService();

    @Test
    void comparer_Regles_quandRegleAjoutee_retourneAjout() {
        NormageRegle regle = creerRegle("RUB1", "NMCL1", "CLASSE1");

        List<DifferenceRegle<NormageRegle>> differences =
                service.comparerRegles(
                        List.of(),
                        List.of(regle)
                );

        assertEquals(1, differences.size());

        DifferenceRegle<NormageRegle> difference = differences.get(0);

        assertEquals(TypeDifferenceEnum.AJOUT, difference.getType());
        assertTrue(difference.getReglesReference().isEmpty());
        assertEquals(List.of(regle), difference.getReglesComparees());
    }

    @Test
    void comparer_Regles_quandRegleSupprimee_retourneSuppression() {
        NormageRegle regle = creerRegle("RUB1", "NMCL1", "CLASSE1");

        List<DifferenceRegle<NormageRegle>> differences =
                service.comparerRegles(
                        List.of(regle),
                        List.of()
                );

        assertEquals(1, differences.size());

        DifferenceRegle<NormageRegle> difference = differences.get(0);

        assertEquals(TypeDifferenceEnum.SUPPRESSION, difference.getType());
        assertTrue(difference.getReglesComparees().isEmpty());
        assertEquals(List.of(regle), difference.getReglesReference());
    }

    @Test
    void comparer_Regles_quandContenuDifferent_retourneModification() {
        NormageRegle regleReference =
                creerRegle("RUB1", "NMCL1", "CLASSE1");

        NormageRegle regleComparee =
                creerRegle("RUB1", "NMCL1", "CLASSE2");

        List<DifferenceRegle<NormageRegle>> differences =
                service.comparerRegles(
                        List.of(regleReference),
                        List.of(regleComparee)
                );

        assertEquals(1, differences.size());

        DifferenceRegle<NormageRegle> difference = differences.get(0);

        assertEquals(TypeDifferenceEnum.MODIFICATION, difference.getType());
        assertEquals(List.of(regleReference), difference.getReglesReference());
        assertEquals(List.of(regleComparee), difference.getReglesComparees());
    }

    @Test
    void comparer_Regles_quandReglesIdentiques_neRetourneAucuneDifference() {
        NormageRegle regleReference =
                creerRegle("RUB1", "NMCL1", "CLASSE1");

        NormageRegle regleComparee =
                creerRegle("RUB1", "NMCL1", "CLASSE1");

        List<DifferenceRegle<NormageRegle>> differences =
                service.comparerRegles(
                        List.of(regleReference),
                        List.of(regleComparee)
                );

        assertTrue(differences.isEmpty());
    }

    private NormageRegle creerRegle(
            String rubrique,
            String rubriqueNmcl,
            String idClasse) {

        NormageRegle regle = new NormageRegle();
        regle.setRubrique(rubrique);
        regle.setRubriqueNmcl(rubriqueNmcl);
        regle.setIdClasse(idClasse);

        return regle;
    }

    @Test
    void comparerJeuDeReglesTest() throws ArcException, ParseException {

        u.executeRequest(c, "DROP SCHEMA IF EXISTS arc CASCADE;");
        u.executeRequest(c, "CREATE SCHEMA IF NOT EXISTS arc;");

        ArcPreparedStatementBuilder query =
                new ArcPreparedStatementBuilder();

        createTables();

        // JDR de référence
        query.build(
                """
                INSERT INTO arc.ihm_chargement_regle
                VALUES (
                    1,
                    'TEST',
                    'M',
                    '2026-01-01',
                    '2026-12-31',
                    '1',
                    'CSV',
                    ';',
                    'UTF-8',
                    NULL
                );
                """
        );

        // delimiter différent => MODIFICATION
        query.build(
                """
                INSERT INTO arc.ihm_chargement_regle
                VALUES (
                    2,
                    'TEST',
                    'M',
                    '2026-01-01',
                    '2026-12-31',
                    '2',
                    'CSV',
                    ',',
                    'UTF-8',
                    NULL
                );
                """
        );

        query.build(
                """
                INSERT INTO arc.ihm_normage_regle
                VALUES (
                    'TEST',
                    'M',
                    '2026-01-01',
                    '2026-12-31',
                    '1',
                    'CLASSE_1',
                    'RUB1',
                    'NMCL1',
                    10,
                    NULL,
                    NULL
                );
                """
        );

        // même clé, mais id_classe différent => MODIFICATION
        query.build(
                """
                INSERT INTO arc.ihm_normage_regle
                VALUES (
                    'TEST',
                    'M',
                    '2026-01-01',
                    '2026-12-31',
                    '2',
                    'CLASSE_2',
                    'RUB1',
                    'NMCL1',
                    20,
                    NULL,
                    NULL
                );
                """
        );

        u.executeRequest(c, query);

        JeuDeRegle reference = creerJeuDeRegle("1");
        JeuDeRegle compare = creerJeuDeRegle("2");

        List<DifferenceRegle<?>> differences =
                service.comparerJeuDeRegles(
                        c,
                        reference,
                        compare
                );

        assertEquals(2, differences.size());

        assertEquals(
                2,
                differences.stream()
                        .filter(d -> d.getType() == TypeDifferenceEnum.MODIFICATION)
                        .count()
        );
    }

    private JeuDeRegle creerJeuDeRegle(String version) throws ParseException {
        return new JeuDeRegle(
                "TEST",
                "M",
                "2026-01-01",
                "2026-12-31",
                version
        );
    }

    private void createTables() throws ArcException {

        ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();

        query.build("""
            CREATE TABLE arc.ihm_norme (
                id_norme text NOT NULL,
                periodicite text NOT NULL,
                def_norme text NOT NULL,
                def_validite text NOT NULL,
                etat text NOT NULL,
                id_famille text NOT NULL,
                id serial4 NOT NULL,
                PRIMARY KEY (id_norme, periodicite)
            );
            """);

        query.build("""
            CREATE TABLE arc.ihm_chargement_regle (
                id_regle int8 NOT NULL,
                id_norme text NOT NULL,
                periodicite text NOT NULL,
                validite_inf date NOT NULL,
                validite_sup date NOT NULL,
                version text NOT NULL,
                type_fichier text NOT NULL,
                delimiter text NULL,
                format text NULL,
                commentaire text NULL
            );
            """);

        query.build("""
            CREATE TABLE arc.ihm_normage_regle (
                id_norme text NOT NULL,
                periodicite text NOT NULL,
                validite_inf date NOT NULL,
                validite_sup date NOT NULL,
                version text NOT NULL,
                id_classe text NOT NULL,
                rubrique text NULL,
                rubrique_nmcl text NULL,
                id_regle int4 NOT NULL,
                todo text NULL,
                commentaire text NULL
            );
            """);

        // + controle
        // + mapping
        // + expression

        u.executeRequest(c, query);
    }
}
