package fr.insee.arc_essnet.utils.bdd;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import fr.insee.arc_essnet.utils.textUtils.IConstanteCaractere;

/**
 *
 * Buffer à requêtes SQL.
 *
 */
public abstract class AbstractRequeteSQLCalibree implements IConstanteCaractere
{
    public static final Integer tailleParDefaut = new Integer(256 * 1024);
    public static final String regexTokenArg = "\\{\\}";
    private Connection connexion;
    protected Integer taille = tailleParDefaut;
    protected StringBuilder requete;
    private boolean isInABlock = false;

    public AbstractRequeteSQLCalibree(Connection aConnexion)
    {
        this.setConnexion(aConnexion);
        this.requete = new StringBuilder();
    }

    public AbstractRequeteSQLCalibree(Connection aConnexion, Integer aTaille)
    {
        this.setConnexion(aConnexion);
        this.taille = aTaille;
        this.requete = new StringBuilder();
    }

    /**
     *
     * @param aRequete
     * @return Cette {@link RequeteSQLCalibree}, augmentée de
     *         {@code aRequete.requete}. Si la concaténation des deux requêtes a
     *         fait dépasser la taille maximale de bloc, cette requête est vidée
     *         après exécution.
     * @throws SQLException
     */
    public AbstractRequeteSQLCalibree append(AbstractRequeteSQLCalibree aRequete) throws SQLException
    {
        return append(aRequete, IRequeteSQLCalibree.NO_ARGS);
    }

    /**
     *
     * @param aRequete
     * @param args
     * @return Cette {@link RequeteSQLCalibree}, augmentée de
     *         {@code aRequete.requete}. Si la concaténation des deux requêtes a
     *         fait dépasser la taille maximale de bloc, cette requête est vidée
     *         après exécution.
     * @throws SQLException
     */
    public abstract AbstractRequeteSQLCalibree append(AbstractRequeteSQLCalibree aRequete, List<String> args)
            throws SQLException;

    /**
     *
     * @param aRequete
     * @param args
     * @return Cette {@link RequeteSQLCalibree}, augmentée de
     *         {@code aRequete.toString()}. Si la concaténation des deux
     *         requêtes a fait dépasser la taille maximale de bloc, cette
     *         requête est vidée après exécution.
     * @throws SQLException
     */
    public AbstractRequeteSQLCalibree append(StringBuilder aRequete) throws SQLException
    {
        return append(aRequete, IRequeteSQLCalibree.NO_ARGS);
    }

    /**
     *
     * @param aRequete
     * @param args
     * @return Cette {@link RequeteSQLCalibree}, augmentée de
     *         {@code aRequete.toString()}. Si la concaténation des deux
     *         requêtes a fait dépasser la taille maximale de bloc, cette
     *         requête est vidée après exécution.
     * @throws SQLException
     */
    public abstract AbstractRequeteSQLCalibree append(StringBuilder aRequete, List<String> args) throws SQLException;

    /**
     *
     * @param aRequete
     * @return Cette {@link RequeteSQLCalibree}, augmentée de {@code aRequete}.
     *         Si la concaténation des deux requêtes a fait dépasser la taille
     *         maximale de bloc, cette requête est vidée après exécution.
     * @throws SQLException
     */
    public AbstractRequeteSQLCalibree append(String aRequete) throws SQLException
    {
        return append(aRequete, IRequeteSQLCalibree.NO_ARGS);
    }

    /**
     *
     * @param aRequete
     * @param args
     * @return Cette {@link RequeteSQLCalibree}, augmentée de {@code aRequete}.
     *         Si la concaténation des deux requêtes a fait dépasser la taille
     *         maximale de bloc, cette requête est vidée après exécution.
     * @throws SQLException
     */
    public abstract AbstractRequeteSQLCalibree append(String aRequete, List<String> args) throws SQLException;

    /**
     * Exécute cette requête, puis la vide.
     *
     * @throws SQLException
     */
    protected abstract void execute() throws SQLException;

    /**
     * @return the connexion
     */
    public Connection getConnexion()
    {
        return this.connexion;
    }

    /**
     * @param connexion
     *            the connexion to set
     */
    public void setConnexion(Connection connexion)
    {
        this.connexion = connexion;
    }

    /**
     * @return the isInABlock
     */
    public boolean isInABlock()
    {
        return this.isInABlock;
    }

    /**
     * @param isInABlock
     *            the isInABlock to set
     */
    public void setInABlock(boolean isInABlock)
    {
        this.isInABlock = isInABlock;
    }

    /**
     * @return the taille
     */
    public Integer getTaille()
    {
        return this.taille;
    }

    public int size()
    {
        return this.requete.length();
    }

    /**
     * @return the requete
     */
    public StringBuilder getRequete()
    {
        return this.requete;
    }
}
