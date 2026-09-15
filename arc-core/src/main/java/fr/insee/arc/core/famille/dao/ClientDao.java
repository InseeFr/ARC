package fr.insee.arc.core.famille.dao;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.famille.model.Client;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.structure.GenericBean;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClientDao {

    public static List<Client> getByFamille(
            Connection connexion,
            String idFamille) throws ArcException {

        ArcPreparedStatementBuilder requete =
                new ArcPreparedStatementBuilder();

        requete.append("""
                SELECT
                    id_famille,
                    id_application
                FROM arc.ihm_client
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

        return extractClients(result);
    }

    private static List<Client> extractClients(
            Map<String, List<String>> result) {

        List<Client> clients = new ArrayList<>();

        if (result.isEmpty()) {
            return clients;
        }

        List<String> idsFamille = result.get("id_famille");

        if (idsFamille == null) {
            return clients;
        }

        for (int i = 0; i < idsFamille.size(); i++) {
            Client client = new Client();

            client.setIdFamille(idsFamille.get(i));
            client.setIdApplication(
                    result.get("id_application").get(i));

            clients.add(client);
        }

        return clients;
    }
}
