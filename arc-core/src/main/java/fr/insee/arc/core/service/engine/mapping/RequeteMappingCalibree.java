package fr.insee.arc.core.service.engine.mapping;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import fr.insee.arc.core.model.IDbConstant;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.utils.bdd.AbstractRequeteSQLCalibree;
import fr.insee.arc.utils.bdd.RequeteSQLCalibree;
import fr.insee.arc.utils.dao.UtilitaireDao;

/**
 * Buffer à requêtes.<br/>
 * FIXME ajouter gestion de params
 *
 */
public class RequeteMappingCalibree extends AbstractRequeteSQLCalibree implements IDbConstant {

    /**
     * C'est un nom de table de pilotage, éventuellement temporaire.
     */
    private String nomTablePilotage;

    public RequeteMappingCalibree(Connection aConnexion) {
        super(aConnexion);
    }

    public RequeteMappingCalibree(Connection aConnexion, Integer aTaille, String aNomTablePilotage) {
        super(aConnexion, aTaille);
        this.nomTablePilotage = aNomTablePilotage;
    }

    private String getRequeteUpdatePilotageKO(String aNomFichier) {
        return new StringBuilder("UPDATE " + this.nomTablePilotage)//
                .append("\n  SET etat_traitement='{" + TraitementEtat.KO + "}', rapport='Erreur SQL : '||message")//
                .append("\n  WHERE id_source = '" + aNomFichier + "';").toString();
    }

    /**
     * Exécute cette requête, puis la vide.
     *
     * @throws SQLException
     */
    protected void execute() throws SQLException {
        StringBuilder executed = new StringBuilder("DO").append("\n$MAPPING$").append("\nDECLARE").append("\nmessage text;").append("\nBEGIN")//
                .append("\n").append(this.getRequete())//
                .append("\nEND;").append("\n$MAPPING$;");
        UtilitaireDao.get(poolName).executeBlock(this.getConnexion(), executed);
        this.getRequete().setLength(0);
    }

    /***
     * 
     */
    public StringBuilder buildMainQuery(String aRequete, List<String> args) {
        StringBuilder executed = new StringBuilder("DO").append("\n$MAPPING$").append("\nDECLARE").append("\nmessage text;").append("\nBEGIN")//
                .append("\n").append(buildQuery(aRequete,args))//
                .append("\nEND;").append("\n$MAPPING$;");
        return executed;
    }
    
    
    public StringBuilder buildQuery(String aRequete, List<String> args) {
        String aNomFichier = args.get(0);
        StringBuilder returned = new StringBuilder("BEGIN")//
                .append("\nmessage := '';")//
                .append("\nEXECUTE '")//
                .append(aRequete.replace(quote, quotequote))//
                // .append(newline + getRequeteUpdatePilotageOK(aNomFichier).replace(quote, quotequote))//
                .append("';")//
                .append("\nEXCEPTION WHEN OTHERS THEN GET STACKED DIAGNOSTICS message = MESSAGE_TEXT;")//
                .append("\n" + getRequeteUpdatePilotageKO(aNomFichier))//
                .append("END;");
        return returned;
    }
    
    
    /**
     * @return the isInABlock
     */
    public boolean isInABlock() {
        return this.isInABlock();
    }

    /**
     * Exécute et vide cette requête
     *
     * @throws SQLException
     */
    public void flush() throws SQLException {
        if (this.getRequete().length() > 0) {
            execute();
        }
    }

    /**
     *
     * @param aRequete
     * @return Cette {@link RequeteSQLCalibree}, augmentée de {@code aRequete.requete}. Si la concaténation des deux requêtes a fait
     *         dépasser la taille maximale de bloc, cette requête est vidée après exécution.
     * @throws SQLException
     */
    public RequeteMappingCalibree append(AbstractRequeteSQLCalibree aRequete, List<String> args) throws SQLException {
        return append(aRequete.getRequete(), args);
    }

    /**
     *
     * @param aRequete
     * @return Cette {@link RequeteSQLCalibree}, augmentée de {@code aRequete.toString()}. Si la concaténation des deux requêtes a fait
     *         dépasser la taille maximale de bloc, cette requête est vidée après exécution.
     * @throws SQLException
     */
    public RequeteMappingCalibree append(StringBuilder aRequete, List<String> args) throws SQLException {
        return append(aRequete.toString(), args);
    }

    /**
     *
     * @param aRequete
     * @return Cette {@link RequeteSQLCalibree}, augmentée de {@code aRequete}. Si la concaténation des deux requêtes a fait dépasser la
     *         taille maximale de bloc, cette requête est vidée après exécution.
     * @throws SQLException
     */
    @Override
    public RequeteMappingCalibree append(String aRequete, List<String> args) throws SQLException {
        String aNomFichier = args.get(0);
        StringBuilder returned = new StringBuilder("BEGIN")//
                .append("\nmessage := '';")//
                .append("\nEXECUTE '")//
                .append(aRequete.replace(quote, quotequote))//
                // .append(newline + getRequeteUpdatePilotageOK(aNomFichier).replace(quote, quotequote))//
                .append("';")//
                .append("\nEXCEPTION WHEN OTHERS THEN GET STACKED DIAGNOSTICS message = MESSAGE_TEXT;")//
                .append("\n" + getRequeteUpdatePilotageKO(aNomFichier))//
                .append("END;");
        this.getRequete().append(newline).append(returned);
        if (this.getRequete().length() > this.getTaille()) {
            this.execute();
        }
        return this;
    }

}
