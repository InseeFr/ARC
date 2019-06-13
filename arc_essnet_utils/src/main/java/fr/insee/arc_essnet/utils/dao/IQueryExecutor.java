package fr.insee.arc_essnet.utils.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import fr.insee.arc_essnet.utils.queryhandler.UtilitaireDAOQueryHandler.HowToClose;
import fr.insee.arc_essnet.utils.queryhandler.UtilitaireDAOQueryHandler.OnException;

public interface IQueryExecutor
{
    /**
     * Exécute une requête qui renvoie un résultat, et transforme chaque
     * enregistrement du résultat dans un objet de type {@link T}, en utilisant
     * une factory <i>ad hoc</i>.<br/>
     * En cas d'erreur, le {@link BiConsumer} effectue un traitement dépendant
     * de l'erreur et de la requête.
     *
     * @param onRecord
     *            La fonction qui transforme un enregistrement en un objet de
     *            type {@code T}
     * @param query
     *            la requête textuelle en SQL
     * @param onException
     *            le gestionnaire d'exception
     * @return
     * @throws Exception
     * @see {@link OnException#DO_NOTHING}
     * @see {@link OnException#LOG}
     * @see {@link OnException#THROW}
     */
      <T> List<T> executeQuery(Function<ResultSet, T> onRecord, String query,
            BiConsumer<Throwable, String> onException) throws SQLException;
    
    /**
     * Exécute une requête qui renvoie un résultat, et transforme chaque
     * enregistrement du résultat dans un objet de type {@link T}, en utilisant
     * une factory <i>ad hoc</i>.<br/>
     * En cas d'erreur, le {@link BiConsumer} effectue un traitement dépendant
     * de l'erreur et de la requête.
     *
     * @param onResult
     *            La fonction qui transforme un enregistrement en un objet de
     *            type {@code T}
     * @param query
     *            la requête textuelle en SQL
     * @param onException
     *            le gestionnaire d'exception
     * @return
     * @throws Exception
     * @see {@link OnException#DO_NOTHING}
     * @see {@link OnException#LOG}
     * @see {@link OnException#THROW}
     */
     <T> T execute(Function<ResultSet, T> onResult, String query,
            BiConsumer<Throwable, String> onException) throws SQLException;

    /**
     * Exécute une requête ne renvoyant aucun résultat.<br/>
     * En cas d'erreur, le {@link BiConsumer} effectue un traitement dépendant
     * de l'erreur et de la requête.
     *
     * @param query
     *            la requête textuelle en SQL
     * @param onException
     *            le gestionnaire d'exception
     * @return
     * @throws Exception
     */
      void executeUpdate(String query, BiConsumer<Throwable, String> onException) throws Exception;

    /**
     * @return the onClose
     */
      Consumer<? super Connection> getOnClose();

    /**
     *
     * @param howToClose
     *            doit gérer la clôture de la connection.
     * @see HowToClose#JUST_CLOSE
     * @see HowToClose#ASSERT_CLOSE
     * @see HowToClose#COMMIT
     * @see HowToClose#COMMIT_CLOSE
     * @see HowToClose#ROLLBACK
     * @see HowToClose#ROLLBACK_CLOSE
     * @see HowToClose#COMMIT_ROLLBACK_CLOSE
     */
      void setOnClose(Consumer<? super Connection> howToClose);
}