package fr.insee.arc.core.service.p5mapping.engine;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.service.global.bo.JeuDeRegle;
import fr.insee.arc.core.service.p5mapping.engine.dao.MappingQueriesFactory;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;

public class ServiceMapping {
	
	/**
     * Récupère l'ensemble des colonnes de la table de la phase précédente, et les répartit dans deux containers :<br/>
     * 1. Un pour les identifiants de rubriques<br/>
     * 2. Un pour les autres types de colonnes<br/>
     *
     * @return
     *
     * @return
     *
     * @throws ArcException
     */
    public MappingQueriesFactory construireRegleMappingFactory(Connection connexion, String envExecution, String tableTempControleOk, String prefixIdentifiantRubrique) throws ArcException {
        Set<String> ensembleIdentifiantRubriqueExistante = new HashSet<>();
        Set<String> ensembleNomRubriqueExistante = new HashSet<>();
        for (String nomColonne : calculerListeColonnes(connexion, tableTempControleOk)) {
            if (nomColonne.startsWith(prefixIdentifiantRubrique)) {
                ensembleIdentifiantRubriqueExistante.add(nomColonne);
            } else {
                ensembleNomRubriqueExistante.add(nomColonne);
            }
        }
        return new MappingQueriesFactory(connexion, envExecution, ensembleIdentifiantRubriqueExistante, ensembleNomRubriqueExistante);
    }

    

    /**
     * return distinct column of a table in a set
     * @param aConnexion
     * @param table
     *
     * @return
     * @throws ArcException
     */
    private static Set<String> calculerListeColonnes(Connection aConnexion, String aTable) throws ArcException {
		return new HashSet<>(UtilitaireDao.get(0).getColumns(aConnexion, new ArrayList<>(), aTable));
    }
    

    
}