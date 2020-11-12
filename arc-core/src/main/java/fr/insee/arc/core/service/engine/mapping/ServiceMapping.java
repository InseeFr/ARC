package fr.insee.arc.core.service.engine.mapping;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import fr.insee.arc.core.model.IDbConstant;
import fr.insee.arc.core.model.JeuDeRegle;
import fr.insee.arc.core.service.engine.ServiceCommunFiltrageMapping;
import fr.insee.arc.utils.dao.UtilitaireDao;

public class ServiceMapping implements IDbConstant {
	
	/**
     * Récupère l'ensemble des colonnes de la table de la phase précédente, et les répartit dans deux containers :<br/>
     * 1. Un pour les identifiants de rubriques<br/>
     * 2. Un pour les autres types de colonnes<br/>
     *
     * @return
     *
     * @return
     *
     * @throws SQLException
     */
    public RegleMappingFactory construireRegleMappingFactory(Connection connexion, String envExecution, String tableTempFiltrageOk, String prefixIdentifiantRubrique) throws SQLException {
        Set<String> ensembleIdentifiantRubriqueExistante = new HashSet<>();
        Set<String> ensembleNomRubriqueExistante = new HashSet<>();
        for (String nomColonne : ServiceCommunFiltrageMapping.calculerListeColonnes(connexion, tableTempFiltrageOk)) {
            if (nomColonne.startsWith(prefixIdentifiantRubrique)) {
                ensembleIdentifiantRubriqueExistante.add(nomColonne);
            } else {
                ensembleNomRubriqueExistante.add(nomColonne);
            }
        }
        return new RegleMappingFactory(connexion, envExecution, ensembleIdentifiantRubriqueExistante, ensembleNomRubriqueExistante);
    }


    /**
     *
     * @param aJeuDeRegle
     * @return Le bon id_famille
     * @throws SQLException
     */
    //TODO : à placer ailleurs, utile plus généralement que pour le mapping
    public String fetchIdFamille(Connection connexion, JeuDeRegle aJeuDeRegle, String tableNorme) throws SQLException {
        StringBuilder requete = new StringBuilder("SELECT id_famille FROM " + tableNorme)//
                .append("\n WHERE id_norme    = '" + aJeuDeRegle.getIdNorme() + "'")//
                .append("\n AND periodicite = '" + aJeuDeRegle.getPeriodicite() + "';");
        return UtilitaireDao.get(poolName).getString(connexion, requete);
    }
    
}