package fr.insee.arc.core.famille.dao;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.famille.model.MappingRegle;
import fr.insee.arc.core.service.global.bo.JeuDeRegle;
import fr.insee.arc.core.service.global.bo.JeuDeRegleDao;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.structure.GenericBean;

public class MappingRegleDao {

    private MappingRegleDao() {
    }

    public static List<MappingRegle> getByJeuDeRegle(
            Connection connexion,
            JeuDeRegle jeuDeRegle) throws ArcException {

        ArcPreparedStatementBuilder requete =
                new ArcPreparedStatementBuilder();

        requete.append("""
                SELECT
                    id_regle,
                    variable_sortie,
                    expr_regle_col,
                    commentaire
                FROM arc.ihm_mapping_regle
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

        return extractMappingRegles(
                result,
                jeuDeRegle
        );
    }

    private static List<MappingRegle> extractMappingRegles(
            Map<String, List<String>> result,
            JeuDeRegle jeuDeRegle) {

        List<MappingRegle> regles = new ArrayList<>();

        if (result.isEmpty()) {
            return regles;
        }

        List<String> variablesSortie =
                result.get("variable_sortie");

        if (variablesSortie == null) {
            return regles;
        }

        for (int i = 0; i < variablesSortie.size(); i++) {

            MappingRegle regle = new MappingRegle();

            regle.setJeuDeRegle(jeuDeRegle);

            String idRegle = result.get("id_regle").get(i);

            if (idRegle != null && !idRegle.isEmpty()) {
                regle.setIdRegle(Long.valueOf(idRegle));
            }

            regle.setVariableSortie(
                    variablesSortie.get(i)
            );

            regle.setExprRegleCol(
                    result.get("expr_regle_col").get(i)
            );

            regle.setCommentaire(
                    result.get("commentaire").get(i)
            );

            regles.add(regle);
        }

        return regles;
    }
}