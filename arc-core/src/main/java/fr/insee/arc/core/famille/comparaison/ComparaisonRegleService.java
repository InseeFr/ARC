package fr.insee.arc.core.famille.comparaison;

import fr.insee.arc.core.famille.dao.*;
import fr.insee.arc.core.famille.model.Norme;
import fr.insee.arc.core.famille.model.RegleComparable;
import fr.insee.arc.core.service.global.bo.JeuDeRegle;
import fr.insee.arc.utils.exception.ArcException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ComparaisonRegleService {

    private static final Logger LOGGER = LogManager.getLogger(ComparaisonRegleService.class);


    public List<DifferenceRegle<?>> comparerJeuDeRegles(
            Connection connexion,
            JeuDeRegle jeuDeRegleReference,
            JeuDeRegle jeuDeRegleCompare) throws ArcException {

        List<DifferenceRegle<?>> differences = new ArrayList<>();

        differences.addAll(comparerRegles(
                ChargementRegleDao.getByJeuDeRegle(
                        connexion,
                        jeuDeRegleReference
                ),
                ChargementRegleDao.getByJeuDeRegle(
                        connexion,
                        jeuDeRegleCompare
                )
        ));

        differences.addAll(comparerRegles(
                NormageRegleDao.getByJeuDeRegle(
                        connexion,
                        jeuDeRegleReference
                ),
                NormageRegleDao.getByJeuDeRegle(
                        connexion,
                        jeuDeRegleCompare
                )
        ));

        differences.addAll(comparerRegles(
                ControleRegleDao.getByJeuDeRegle(
                        connexion,
                        jeuDeRegleReference
                ),
                ControleRegleDao.getByJeuDeRegle(
                        connexion,
                        jeuDeRegleCompare
                )
        ));

        differences.addAll(comparerRegles(
                MappingRegleDao.getByJeuDeRegle(
                        connexion,
                        jeuDeRegleReference
                ),
                MappingRegleDao.getByJeuDeRegle(
                        connexion,
                        jeuDeRegleCompare
                )
        ));

        differences.addAll(comparerRegles(
                ExpressionRegleDao.getByJeuDeRegle(
                        connexion,
                        jeuDeRegleReference
                ),
                ExpressionRegleDao.getByJeuDeRegle(
                        connexion,
                        jeuDeRegleCompare
                )
        ));

        List<Norme> normeReference = NormeDao.getByJeuDeRegle(
                connexion,
                jeuDeRegleReference
        );

        List<Norme> normeCompare = NormeDao.getByJeuDeRegle(
                connexion,
                jeuDeRegleCompare
        );

        differences.addAll(comparerRegles(
                normeReference,
                normeCompare
        ));

        String idFamilleReference = normeReference.get(0).getIdFamille();
        String idFamilleComparee = normeCompare.get(0).getIdFamille();

        differences.addAll(comparerRegles(
                TableMetierDao.getByFamille(
                        connexion,
                        idFamilleReference
                ),
                TableMetierDao.getByFamille(
                        connexion,
                        idFamilleComparee
                )
        ));

        differences.addAll(comparerRegles(
                VariableMetierDAO.getByFamille(
                        connexion,
                        idFamilleReference
                ),
                VariableMetierDAO.getByFamille(
                        connexion,
                        idFamilleComparee
                )
        ));

        differences.addAll(comparerRegles(
                ClientDao.getByFamille(
                        connexion,
                        idFamilleReference
                ),
                ClientDao.getByFamille(
                        connexion,
                        idFamilleComparee
                )
        ));

        LOGGER.info("differences: {}", differences);
        return differences;
    }

    public <C, T extends RegleComparable<C>>
    List<DifferenceRegle<T>> comparerRegles(
            List<T> reglesReference,
            List<T> reglesComparees) {

        Map<C, List<T>> referenceParCle =
                indexerParCle(reglesReference);

        Map<C, List<T>> compareesParCle =
                indexerParCle(reglesComparees);

        Set<C> cles = new HashSet<>();
        cles.addAll(referenceParCle.keySet());
        cles.addAll(compareesParCle.keySet());

        List<DifferenceRegle<T>> differences = new ArrayList<>();

        for (C cle : cles) {

            List<T> reference =
                    referenceParCle.getOrDefault(cle, List.of());

            List<T> comparees =
                    compareesParCle.getOrDefault(cle, List.of());

            /*
             * La clé n'existe que dans le JDR comparé.
             */
            if (reference.isEmpty()) {
                differences.add(new DifferenceRegle<>(
                        TypeDifferenceEnum.AJOUT,
                        List.of(),
                        comparees
                ));

                /*
                 * La clé n'existe que dans le JDR de référence.
                 */
            } else if (comparees.isEmpty()) {
                differences.add(new DifferenceRegle<>(
                        TypeDifferenceEnum.SUPPRESSION,
                        reference,
                        List.of()
                ));

                /*
                 * La clé existe des deux côtés :
                 * on cherche ce qui est strictement identique.
                 */
            } else {
                List<T> referenceRestantes = new ArrayList<>(reference);
                List<T> compareesRestantes = new ArrayList<>(comparees);

                retirerReglesIdentiques(
                        referenceRestantes,
                        compareesRestantes
                );

                /*
                 * S'il reste quelque chose, c'est une modification.
                 */
                if (!referenceRestantes.isEmpty()
                        || !compareesRestantes.isEmpty()) {

                    differences.add(new DifferenceRegle<>(
                            TypeDifferenceEnum.MODIFICATION,
                            referenceRestantes,
                            compareesRestantes
                    ));
                }
            }
        }

        return differences;
    }

    private <C, T extends RegleComparable<C>>
    void retirerReglesIdentiques(
            List<T> reference,
            List<T> comparees) {

        Iterator<T> iteratorReference = reference.iterator();

        while (iteratorReference.hasNext()) {

            T regleReference = iteratorReference.next();

            Optional<T> identique = comparees.stream()
                    .filter(regleComparee ->
                            Objects.equals(
                                    regleReference.getContenuComparaison(),
                                    regleComparee.getContenuComparaison()
                            )
                    )
                    .findFirst();

            if (identique.isPresent()) {
                iteratorReference.remove();
                comparees.remove(identique.get());
            }
        }
    }

    private <C, T extends RegleComparable<C>>
    Map<C, List<T>> indexerParCle(List<T> regles) {
        return regles.stream()
                .collect(Collectors.groupingBy(
                        RegleComparable::getCleComparaison
                ));
    }
}
