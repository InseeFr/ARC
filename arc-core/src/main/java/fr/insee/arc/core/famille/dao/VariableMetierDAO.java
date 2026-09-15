package fr.insee.arc.core.famille.dao;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.famille.model.VariableMetier;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.structure.GenericBean;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VariableMetierDAO {

    private VariableMetierDAO() {
    }

    public static List<VariableMetier> getByFamille(
            Connection connexion,
            String idFamille) throws ArcException {

        ArcPreparedStatementBuilder requete =
                new ArcPreparedStatementBuilder();

        requete.append("""
                SELECT
                    id_famille,
                    nom_table_metier,
                    nom_variable_metier,
                    type_variable_metier,
                    description_variable_metier,
                    type_consolidation
                FROM arc.ihm_mod_variable_metier
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

        return extractVariablesMetier(result);
    }

    private static List<VariableMetier> extractVariablesMetier(
            Map<String, List<String>> result) {

        List<VariableMetier> variablesMetier = new ArrayList<>();

        if (result.isEmpty()) {
            return variablesMetier;
        }

        List<String> idsFamille = result.get("id_famille");

        if (idsFamille == null) {
            return variablesMetier;
        }

        for (int i = 0; i < idsFamille.size(); i++) {
            VariableMetier variableMetier = new VariableMetier();

            variableMetier.setIdFamille(
                    idsFamille.get(i));

            variableMetier.setNomTableMetier(
                    result.get("nom_table_metier").get(i));

            variableMetier.setNomVariableMetier(
                    result.get("nom_variable_metier").get(i));

            variableMetier.setTypeVariableMetier(
                    result.get("type_variable_metier").get(i));

            variableMetier.setDescriptionVariableMetier(
                    result.get("description_variable_metier").get(i));

            variableMetier.setTypeConsolidation(
                    result.get("type_consolidation").get(i));

            variablesMetier.add(variableMetier);
        }

        return variablesMetier;
    }
}
