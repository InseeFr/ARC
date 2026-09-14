package fr.insee.arc.core.jeuderegle.dao;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.jeuderegle.model.ControleRegle;
import fr.insee.arc.core.service.global.bo.JeuDeRegle;
import fr.insee.arc.core.service.global.bo.JeuDeRegleDao;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.structure.GenericBean;


public class ControleRegleDao {

    private ControleRegleDao() {
    }

    public static List<ControleRegle> getByJeuDeRegle(
            Connection connexion,
            JeuDeRegle jeuDeRegle) throws ArcException {

        ArcPreparedStatementBuilder requete =
                new ArcPreparedStatementBuilder();

        requete.append("""
                SELECT
                    id_regle,
                    id_classe,
                    rubrique_pere,
                    rubrique_fils,
                    borne_inf,
                    borne_sup,
                    condition,
                    pre_action,
                    todo,
                    commentaire,
                    xsd_ordre,
                    xsd_label_fils,
                    xsd_role,
                    blocking_threshold,
                    error_row_processing
                FROM arc.ihm_controle_regle
                WHERE
                """);

        requete.append(
                JeuDeRegleDao.buildJeuDeRegleCondition(jeuDeRegle)
        );

        Map<String, List<String>> result =
                new GenericBean(
                        UtilitaireDao.get(0)
                                .executeRequest(connexion, requete)
                ).mapContent();

        return extractControleRegles(
                result,
                jeuDeRegle
        );
    }

    private static List<ControleRegle> extractControleRegles(
            Map<String, List<String>> result,
            JeuDeRegle jeuDeRegle) {

        List<ControleRegle> regles = new ArrayList<>();

        if (result.isEmpty()) {
            return regles;
        }

        List<String> idsRegle = result.get("id_regle");

        if (idsRegle == null) {
            return regles;
        }

        for (int i = 0; i < idsRegle.size(); i++) {
            ControleRegle regle = new ControleRegle();

            regle.setJeuDeRegle(jeuDeRegle);
            regle.setIdRegle(
                    Integer.valueOf(idsRegle.get(i))
            );
            regle.setIdClasse(
                    result.get("id_classe").get(i)
            );
            regle.setRubriquePere(
                    result.get("rubrique_pere").get(i)
            );
            regle.setRubriqueFils(
                    result.get("rubrique_fils").get(i)
            );
            regle.setBorneInf(
                    result.get("borne_inf").get(i)
            );
            regle.setBorneSup(
                    result.get("borne_sup").get(i)
            );
            regle.setCondition(
                    result.get("condition").get(i)
            );
            regle.setPreAction(
                    result.get("pre_action").get(i)
            );
            regle.setTodo(
                    result.get("todo").get(i)
            );
            regle.setCommentaire(
                    result.get("commentaire").get(i)
            );

            String xsdOrdre =
                    result.get("xsd_ordre").get(i);

            if (xsdOrdre != null && !xsdOrdre.isEmpty()) {
                regle.setXsdOrdre(
                        Integer.valueOf(xsdOrdre)
                );
            }

            regle.setXsdLabelFils(
                    result.get("xsd_label_fils").get(i)
            );
            regle.setXsdRole(
                    result.get("xsd_role").get(i)
            );
            regle.setBlockingThreshold(
                    result.get("blocking_threshold").get(i)
            );
            regle.setErrorRowProcessing(
                    result.get("error_row_processing").get(i)
            );

            regles.add(regle);
        }

        return regles;
    }
}