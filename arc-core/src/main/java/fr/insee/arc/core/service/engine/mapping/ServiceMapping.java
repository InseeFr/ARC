package fr.insee.arc.core.service.engine.mapping;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.model.JeuDeRegle;
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
    public RegleMappingFactory construireRegleMappingFactory(Connection connexion, String envExecution, String tableTempControleOk, String prefixIdentifiantRubrique) throws ArcException {
        Set<String> ensembleIdentifiantRubriqueExistante = new HashSet<>();
        Set<String> ensembleNomRubriqueExistante = new HashSet<>();
        for (String nomColonne : calculerListeColonnes(connexion, tableTempControleOk)) {
            if (nomColonne.startsWith(prefixIdentifiantRubrique)) {
                ensembleIdentifiantRubriqueExistante.add(nomColonne);
            } else {
                ensembleNomRubriqueExistante.add(nomColonne);
            }
        }
        return new RegleMappingFactory(connexion, envExecution, ensembleIdentifiantRubriqueExistante, ensembleNomRubriqueExistante);
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
    

    /**
     *
     * @param aJeuDeRegle
     * @return Le bon id_famille
     * @throws ArcException
     */
    public String fetchIdFamille(Connection connexion, JeuDeRegle aJeuDeRegle, String tableNorme) throws ArcException {
        ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
        requete
        	.append("SELECT id_famille FROM " + tableNorme)
        	.append("\n WHERE id_norme = " + requete.quoteText(aJeuDeRegle.getIdNorme()))
        	.append("\n AND periodicite = " + requete.quoteText(aJeuDeRegle.getPeriodicite()));
        return UtilitaireDao.get(0).getString(connexion, requete);
    }
    
}