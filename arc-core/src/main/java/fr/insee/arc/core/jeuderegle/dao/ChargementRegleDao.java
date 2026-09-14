package fr.insee.arc.core.jeuderegle.dao;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.jeuderegle.model.ChargementRegle;
import fr.insee.arc.core.service.global.bo.JeuDeRegle;
import fr.insee.arc.core.service.global.bo.JeuDeRegleDao;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.structure.GenericBean;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChargementRegleDao {

    private ChargementRegleDao() {
        throw new IllegalStateException("Utility class");
    }

    public static List<ChargementRegle> getByJeuDeRegle(
            Connection connexion,
            JeuDeRegle jeuDeRegle) throws ArcException {

        ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();

        requete.append("""
                SELECT
                    id_regle,
                    type_fichier,
                    delimiter,
                    format,
                    commentaire
                FROM arc.ihm_chargement_regle
                WHERE
                """);

        requete.append(JeuDeRegleDao.buildJeuDeRegleCondition(jeuDeRegle));

        Map<String, List<String>> result = new GenericBean(
                UtilitaireDao.get(0).executeRequest(connexion, requete)
        ).mapContent();

        return extractChargementRegles(result, jeuDeRegle);
    }

    private static List<ChargementRegle> extractChargementRegles(
            Map<String, List<String>> result,
            JeuDeRegle jeuDeRegle) {

        List<ChargementRegle> regles = new ArrayList<>();

        if (result.isEmpty()) {
            return regles;
        }

        List<String> idsRegle = result.get("id_regle");

        if (idsRegle == null) {
            return regles;
        }

        for (int i = 0; i < idsRegle.size(); i++) {
            ChargementRegle regle = new ChargementRegle();

            regle.setJeuDeRegle(jeuDeRegle);
            regle.setIdRegle(Long.valueOf(idsRegle.get(i)));
            regle.setTypeFichier(result.get("type_fichier").get(i));
            regle.setDelimiter(result.get("delimiter").get(i));
            regle.setFormat(result.get("format").get(i));
            regle.setCommentaire(result.get("commentaire").get(i));

            regles.add(regle);
        }

        return regles;
    }
}
