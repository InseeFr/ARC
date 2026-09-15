package fr.insee.arc.core.famille.comparaison;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.factory.ApiServiceFactory;
import fr.insee.arc.core.famille.model.NormageRegle;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.engine.initialisation.BddPatcherTest;
import fr.insee.arc.core.service.global.bo.JeuDeRegle;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.query.InitializeQueryTest;
import fr.insee.arc.utils.query.TestDatabase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ComparaisonRegleServiceTest extends InitializeQueryTest {

    private final ComparaisonRegleService service = new ComparaisonRegleService();

    @BeforeAll
    static void initDatabase() throws ArcException {
        BddPatcherTest.createDatabase();
        BddPatcherTest.insertTestDataLight();
    }

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
    void comparerJeuxDeReglesLight() throws Exception {

        JeuDeRegle reference = new JeuDeRegle(
                "v2008-11",
                "A",
                "2020-01-01",
                "2100-01-01",
                "vConformite"
        );

        JeuDeRegle compare = new JeuDeRegle(
                "v2016-02",
                "A",
                "2020-01-01",
                "2100-01-01",
                "vConformite"
        );

        List<DifferenceRegle<?>> differences =
                service.comparerJeuDeRegles(
                        c,
                        reference,
                        compare
                );

        assertEquals(9, differences.size());
    }

}
