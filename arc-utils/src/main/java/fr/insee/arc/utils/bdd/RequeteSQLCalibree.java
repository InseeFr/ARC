package fr.insee.arc.utils.bdd;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import fr.insee.arc.utils.utils.LoggerDispatcher;

/**
 *
 * Cette classe permet d'exécuter des blocs de requêtes qui ne renvoient pas de
 * résultat. Elle possède un paramètre : la taille.<br/>
 * Lorsque la taille du bloc de requêtes qui la constitue dépasse cette taille,
 * la requête est envoyée puis vidée. Ainsi, l'utilisateur n'a pas à se soucier
 * de gérer lui-même comment constituer les blocs de requêtes, il alimente juste
 * une instance de cette classe.
 *
 */
public class RequeteSQLCalibree extends AbstractRequeteSQLCalibree
{
    private static final Logger LOGGER = Logger.getLogger(RequeteSQLCalibree.class);

    public RequeteSQLCalibree(Connection aConnexion, Integer aTaille)
    {
        super(aConnexion);
        this.taille = aTaille;
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
    public RequeteSQLCalibree append(AbstractRequeteSQLCalibree aRequete, List<String> args) throws SQLException
    {
        return append(aRequete.requete, args);
    }

    /**
     *
     * @param aRequete
     * @return Cette {@link RequeteSQLCalibree}, augmentée de
     *         {@code aRequete.toString()}. Si la concaténation des deux
     *         requêtes a fait dépasser la taille maximale de bloc, cette
     *         requête est vidée après exécution.
     * @throws SQLException
     */
    public RequeteSQLCalibree append(StringBuilder aRequete, List<String> args) throws SQLException
    {
        return append(aRequete.toString(), args);
    }

    /**
     *
     * @param aRequete
     * @return Cette {@link RequeteSQLCalibree}, augmentée de {@code aRequete}.
     *         Si la concaténation des deux requêtes a fait dépasser la taille
     *         maximale de bloc, cette requête est vidée après exécution.
     * @throws SQLException
     */
    public RequeteSQLCalibree append(String aRequete, List<String> args) throws SQLException
    {
        if (StringUtils.isBlank(aRequete)) { return this; }
        String requeteParsee = aRequete;
        for (int i = 0; args != null && i < args.size(); i++)
        {
            requeteParsee = requeteParsee.replaceFirst(regexTokenArg, args.get(i));
        }
        this.requete.append(NEWLINE).append(requeteParsee);
        if (this.requete.length() > this.taille)
        {
            this.execute();
        }
        return this;
    }

    /**
     * Exécute cette requête, puis la vide.
     *
     * @throws SQLException
     */
    protected void execute() throws SQLException
    {
        try
        {
            Statement stmt = this.getConnexion().createStatement();
            try
            {
                stmt.execute(this.requete.toString());
            	LoggerDispatcher.trace("execute() ["+this.requete.toString()+"]",LOGGER);
            	LoggerDispatcher.info("execute() Taille du commit :"+this.requete.length()+"]",LOGGER);
            } finally
            {
                stmt.close();
            }
        } catch (SQLException ex)
        {
            throw new SQLException(this.requete.toString(), ex);
        }
        this.requete = new StringBuilder();
    }

    /**
     * Exécute et vide cette requête
     *
     * @throws SQLException
     */
    public void flush() throws SQLException
    {
        if (this.requete.length() > 0)
        {
            execute();
        }
    }

    public String toString()
    {
        return this.requete.toString();
    }
}
