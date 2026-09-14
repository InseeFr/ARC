package fr.insee.arc.core.jeuderegle.dao;


import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.jeuderegle.model.Norme;
import fr.insee.arc.core.service.global.bo.JeuDeRegle;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.structure.GenericBean;

public class NormeDao {

    private NormeDao() {
    }

    public static List<Norme> getByJeuDeRegle(
            Connection connexion,
            JeuDeRegle jeuDeRegle) throws ArcException {

        ArcPreparedStatementBuilder requete =
                new ArcPreparedStatementBuilder();

        requete.append("""
                SELECT
                    id,
                    id_norme,
                    periodicite,
                    def_norme,
                    def_validite,
                    etat,
                    id_famille
                FROM arc.ihm_norme
                WHERE id_norme
                """);

        requete.append(
                requete.sqlEqual(
                        jeuDeRegle.getIdNorme(),
                        "text"
                )
        );

        requete.append("""
                 AND periodicite
                """);

        requete.append(
                requete.sqlEqual(
                        jeuDeRegle.getPeriodicite(),
                        "text"
                )
        );

        Map<String, List<String>> result =
                new GenericBean(
                        UtilitaireDao.get(0)
                                .executeRequest(connexion, requete)
                ).mapContent();

        return extractNormes(result);
    }

    private static List<Norme> extractNormes(
            Map<String, List<String>> result) {

        List<Norme> normes = new ArrayList<>();

        if (result.isEmpty()) {
            return normes;
        }

        List<String> idsNorme = result.get("id_norme");

        if (idsNorme == null) {
            return normes;
        }

        for (int i = 0; i < idsNorme.size(); i++) {

            Norme norme = new Norme();

            String id = result.get("id").get(i);

            if (id != null && !id.isEmpty()) {
                norme.setId(Integer.valueOf(id));
            }

            norme.setIdNorme(
                    idsNorme.get(i)
            );

            norme.setPeriodicite(
                    result.get("periodicite").get(i)
            );

            norme.setDefNorme(
                    result.get("def_norme").get(i)
            );

            norme.setDefValidite(
                    result.get("def_validite").get(i)
            );

            norme.setEtat(
                    result.get("etat").get(i)
            );

            norme.setIdFamille(
                    result.get("id_famille").get(i)
            );

            normes.add(norme);
        }

        return normes;
    }
}