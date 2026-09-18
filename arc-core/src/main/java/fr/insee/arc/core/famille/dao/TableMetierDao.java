package fr.insee.arc.core.famille.dao;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.famille.model.TableMetier;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.structure.GenericBean;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TableMetierDao {

    private TableMetierDao() {}

    public static List<TableMetier> getByFamille(
            Connection connexion,
            String idFamille) throws ArcException{
        ArcPreparedStatementBuilder requete =
                new ArcPreparedStatementBuilder();

        requete.append("""
SELECT
    id_famille,
    nom_table_metier,
    description_table_metier
FROM arc.ihm_mod_table_metier
WHERE id_famille
                """);

        requete.append(
                requete.sqlEqual(
                        idFamille,
                        "text"
                )
        );

        Map<String, List<String>> result =
                new GenericBean(
                        UtilitaireDao.get(0)
                                .executeRequest(connexion, requete)
                ).mapContent();

        return extractTablesMetier(result);
    }

    private static List<TableMetier> extractTablesMetier(
            Map<String, List<String>> result) {

        List<TableMetier> tablesMetier = new ArrayList<>();

        if (result.isEmpty()) {
            return tablesMetier;
        }

        List<String> idsFamille = result.get("id_famille");

        if (idsFamille == null) {
            return tablesMetier;
        }

        for (int i = 0; i < idsFamille.size(); i++) {
            TableMetier tableMetier = new TableMetier();

            tableMetier.setIdFamille(idsFamille.get(i));
            tableMetier.setNomTableMetier(
                    result.get("nom_table_metier").get(i));
            tableMetier.setDescriptionTableMetier(
                    result.get("description_table_metier").get(i));

            tablesMetier.add(tableMetier);
        }

        return tablesMetier;
    }

}
