package fr.insee.arc_essnet.utils.bdd;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public interface IRequeteSQLCalibree
{
    public static final List<String> NO_ARGS = new ArrayList<>();

    /**
     * @return the query executor
     */
    Consumer<StringBuilder> getQueryExecutor();

    /**
     * 
     * @param requestExecutor
     */
    void setQueryExecutor(Consumer<StringBuilder> queryExecutor);

    /**
     *
     * @param aRequete
     * @return Cette {@link RequeteSQLCalibree}, augmentée de
     *         {@code aRequete.requete}. Si la concaténation des deux requêtes a
     *         fait dépasser la taille maximale de bloc, cette requête est vidée
     *         après exécution.
     * @throws RuntimeException
     */
    default IRequeteSQLCalibree append(IRequeteSQLCalibree aRequete) throws RuntimeException
    {
        return append(aRequete, NO_ARGS);
    }

    /**
     *
     * @param aRequete
     * @param args
     * @return Cette {@link RequeteSQLCalibree}, augmentée de
     *         {@code aRequete.requete}. Si la concaténation des deux requêtes a
     *         fait dépasser la taille maximale de bloc, cette requête est vidée
     *         après exécution.
     * @throws RuntimeException
     */
    IRequeteSQLCalibree append(IRequeteSQLCalibree aRequete, List<String> args) throws RuntimeException;

    /**
     *
     * @param aRequete
     * @param args
     * @return Cette {@link RequeteSQLCalibree}, augmentée de
     *         {@code aRequete.toString()}. Si la concaténation des deux
     *         requêtes a fait dépasser la taille maximale de bloc, cette
     *         requête est vidée après exécution.
     * @throws RuntimeException
     */
    default IRequeteSQLCalibree append(StringBuilder aRequete) throws RuntimeException
    {
        return append(aRequete, NO_ARGS);
    }

    /**
     *
     * @param aRequete
     * @param args
     * @return Cette {@link RequeteSQLCalibree}, augmentée de
     *         {@code aRequete.toString()}. Si la concaténation des deux
     *         requêtes a fait dépasser la taille maximale de bloc, cette
     *         requête est vidée après exécution.
     * @throws RuntimeException
     */
    IRequeteSQLCalibree append(StringBuilder aRequete, List<String> args) throws RuntimeException;

    /**
     *
     * @param aRequete
     * @return Cette {@link RequeteSQLCalibree}, augmentée de {@code aRequete}.
     *         Si la concaténation des deux requêtes a fait dépasser la taille
     *         maximale de bloc, cette requête est vidée après exécution.
     * @throws RuntimeException
     */
    default IRequeteSQLCalibree append(String aRequete) throws RuntimeException
    {
        return append(aRequete, NO_ARGS);
    }

    /**
     *
     * @param aRequete
     * @param args
     * @return Cette {@link RequeteSQLCalibree}, augmentée de {@code aRequete}.
     *         Si la concaténation des deux requêtes a fait dépasser la taille
     *         maximale de bloc, cette requête est vidée après exécution.
     * @throws RuntimeException
     */
    IRequeteSQLCalibree append(String aRequete, List<String> args) throws RuntimeException;

    /**
     * Exécute cette requête, puis la vide.
     *
     * @throws SQLException
     */
    void execute() throws RuntimeException;

    /**
     * Exécute et vide cette requête
     *
     * @throws SQLException
     */
    default void flush() throws RuntimeException
    {
        if (size() > 0)
        {
            execute();
        }
    }

    /**
     * Vide la requête
     */
    default void clear()
    {
        getRequete().setLength(0);
    }

    /**
     * @return the isInABlock
     */
    boolean isInABlock();

    /**
     * @param isInABlock
     *            the isInABlock to set
     */
    void setInABlock(boolean isInABlock);

    /**
     * @return La taille maximale permise pour la requête
     */
    Integer getTaille();

    /**
     * 
     * @return la taille courante de la requête
     */
    default int size()
    {
        return getRequete().length();
    }

    /**
     * @return the requete
     */
    StringBuilder getRequete();
}
