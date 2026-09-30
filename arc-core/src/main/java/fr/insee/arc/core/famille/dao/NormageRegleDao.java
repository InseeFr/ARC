package fr.insee.arc.core.famille.dao;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.famille.model.NormageRegle;
import fr.insee.arc.core.service.global.bo.JeuDeRegle;
import fr.insee.arc.core.service.global.bo.JeuDeRegleDao;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.structure.GenericBean;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NormageRegleDao {

    private NormageRegleDao() {
        throw new IllegalStateException("Utility class");
    }

    public static List<NormageRegle> getByJeuDeRegle(
            Connection connexion,
            JeuDeRegle jeuDeRegle) throws ArcException {

        ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();

        requete.append("""
        SELECT
            id_regle,
            id_classe,
            rubrique,
            rubrique_nmcl,
            todo,
            commentaire
        FROM arc.ihm_normage_regle
        WHERE
        """);

        requete.append(JeuDeRegleDao.buildJeuDeRegleCondition(jeuDeRegle));

        Map<String, List<String>> result = new GenericBean(
                UtilitaireDao.get(0).executeRequest(connexion, requete)
        ).mapContent();

        return extractNormageRegles(result, jeuDeRegle);
    }

    private static List<NormageRegle> extractNormageRegles(
            Map<String, List<String>> result,
            JeuDeRegle jeuDeRegle) {

        List<NormageRegle> regles = new ArrayList<>();

        if (result.isEmpty()) {
            return regles;
        }

        for (int i = 0; i < result.get("id_regle").size(); i++) {

            NormageRegle regle = new NormageRegle();

            regle.setJeuDeRegle(jeuDeRegle);
            regle.setIdRegle(Integer.valueOf(result.get("id_regle").get(i)));
            regle.setIdClasse(result.get("id_classe").get(i));
            regle.setRubrique(result.get("rubrique").get(i));
            regle.setRubriqueNmcl(result.get("rubrique_nmcl").get(i));
            regle.setTodo(result.get("todo").get(i));
            regle.setCommentaire(result.get("commentaire").get(i));

            regles.add(regle);
        }

        return regles;
    }
}
