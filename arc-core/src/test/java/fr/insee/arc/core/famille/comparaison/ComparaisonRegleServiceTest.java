package fr.insee.arc.core.famille.comparaison;

import fr.insee.arc.core.famille.model.*;
import fr.insee.arc.core.service.engine.initialisation.BddPatcherTest;
import fr.insee.arc.core.service.global.bo.JeuDeRegle;
import fr.insee.arc.utils.query.InitializeQueryTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ComparaisonRegleServiceTest extends InitializeQueryTest {

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
    void comparerJeuxDeReglesAvecTousTypesDeDifferences() throws Exception {

        BddPatcherTest.createDatabase();
        BddPatcherTest.insertTestComparaisonFamille();

        JeuDeRegle reference = new JeuDeRegle(
                "TEST-REF",
                "A",
                "2020-01-01",
                "2100-01-01",
                "v1"
        );

        JeuDeRegle compare = new JeuDeRegle(
                "TEST-COMP",
                "A",
                "2020-01-01",
                "2100-01-01",
                "v1"
        );

        List<DifferenceRegle<?>> differences =
                service.comparerJeuDeRegles(
                        c,
                        reference,
                        compare
                );

        assertAll(
                // Chargement
                () -> assertEquals(1, compterDifferences(
                        differences, ChargementRegle.class, TypeDifferenceEnum.MODIFICATION)),

                // Normage
                () -> assertEquals(1, compterDifferences(
                        differences, NormageRegle.class, TypeDifferenceEnum.AJOUT)),
                () -> assertEquals(1, compterDifferences(
                        differences, NormageRegle.class, TypeDifferenceEnum.SUPPRESSION)),
                () -> assertEquals(0, compterDifferences(
                        differences, NormageRegle.class, TypeDifferenceEnum.MODIFICATION)),

                // Controle
                () -> assertEquals(1, compterDifferences(
                        differences, ControleRegle.class, TypeDifferenceEnum.AJOUT)),
                () -> assertEquals(1, compterDifferences(
                        differences, ControleRegle.class, TypeDifferenceEnum.SUPPRESSION)),
                () -> assertEquals(1, compterDifferences(
                        differences, ControleRegle.class, TypeDifferenceEnum.MODIFICATION)),

                // Mapping
                () -> assertEquals(1, compterDifferences(
                        differences, MappingRegle.class, TypeDifferenceEnum.AJOUT)),
                () -> assertEquals(1, compterDifferences(
                        differences, MappingRegle.class, TypeDifferenceEnum.SUPPRESSION)),
                () -> assertEquals(1, compterDifferences(
                        differences, MappingRegle.class, TypeDifferenceEnum.MODIFICATION)),

                // Expression : jamais de MODIFICATION
                () -> assertEquals(2, compterDifferences(
                        differences, ExpressionRegle.class, TypeDifferenceEnum.AJOUT)),
                () -> assertEquals(2, compterDifferences(
                        differences, ExpressionRegle.class, TypeDifferenceEnum.SUPPRESSION)),
                () -> assertEquals(0, compterDifferences(
                        differences, ExpressionRegle.class, TypeDifferenceEnum.MODIFICATION)),

                // Norme
                () -> assertEquals(1, compterDifferences(
                        differences, Norme.class, TypeDifferenceEnum.MODIFICATION)),

                // Table métier
                () -> assertEquals(1, compterDifferences(
                        differences, TableMetier.class, TypeDifferenceEnum.AJOUT)),
                () -> assertEquals(1, compterDifferences(
                        differences, TableMetier.class, TypeDifferenceEnum.SUPPRESSION)),

                // Variable métier
                () -> assertEquals(1, compterDifferences(
                        differences, VariableMetier.class, TypeDifferenceEnum.AJOUT)),
                () -> assertEquals(1, compterDifferences(
                        differences, VariableMetier.class, TypeDifferenceEnum.SUPPRESSION)),
                () -> assertEquals(1, compterDifferences(
                        differences, VariableMetier.class, TypeDifferenceEnum.MODIFICATION)),

                // Client : jamais de MODIFICATION
                () -> assertEquals(1, compterDifferences(
                        differences, Client.class, TypeDifferenceEnum.AJOUT)),
                () -> assertEquals(1, compterDifferences(
                        differences, Client.class, TypeDifferenceEnum.SUPPRESSION)),
                () -> assertEquals(0, compterDifferences(
                        differences, Client.class, TypeDifferenceEnum.MODIFICATION))
        );
    }

    private long compterDifferences(
            List<DifferenceRegle<?>> differences,
            Class<?> classe,
            TypeDifferenceEnum type) {

        return differences.stream()
                .filter(d -> d.getType() == type)
                .filter(d -> Stream.concat(
                                d.getReglesReference().stream(),
                                d.getReglesComparees().stream())
                        .anyMatch(classe::isInstance))
                .count();
    }

}
