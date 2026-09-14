package fr.insee.arc.core.jeuderegle.dao;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.jeuderegle.model.ExpressionRegle;
import fr.insee.arc.core.service.global.bo.JeuDeRegle;
import fr.insee.arc.core.service.global.bo.JeuDeRegleDao;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.structure.GenericBean;

public class ExpressionRegleDao {

    private ExpressionRegleDao() {
    }

    public static List<ExpressionRegle> getByJeuDeRegle(
            Connection connexion,
            JeuDeRegle jeuDeRegle) throws ArcException {

        ArcPreparedStatementBuilder requete =
                new ArcPreparedStatementBuilder();

        requete.append("""
                SELECT
                    id_regle,
                    expr_nom,
                    expr_valeur,
                    commentaire
                FROM arc.ihm_expression
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

        return extractExpressionRegles(
                result,
                jeuDeRegle
        );
    }

    private static List<ExpressionRegle> extractExpressionRegles(
            Map<String, List<String>> result,
            JeuDeRegle jeuDeRegle) {

        List<ExpressionRegle> regles = new ArrayList<>();

        if (result.isEmpty()) {
            return regles;
        }

        List<String> idsRegle = result.get("id_regle");

        if (idsRegle == null) {
            return regles;
        }

        for (int i = 0; i < idsRegle.size(); i++) {

            ExpressionRegle regle = new ExpressionRegle();

            regle.setJeuDeRegle(jeuDeRegle);

            regle.setIdRegle(
                    Long.valueOf(idsRegle.get(i))
            );

            regle.setExprNom(
                    result.get("expr_nom").get(i)
            );

            regle.setExprValeur(
                    result.get("expr_valeur").get(i)
            );

            regle.setCommentaire(
                    result.get("commentaire").get(i)
            );

            regles.add(regle);
        }

        return regles;
    }
}