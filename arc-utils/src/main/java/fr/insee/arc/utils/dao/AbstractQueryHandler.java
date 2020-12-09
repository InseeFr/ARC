package fr.insee.arc.utils.dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.utils.utils.LoggerHelper;

/**
 *
 * Cette classe permet d'exécuter des requêtes en base dans différents
 * contextes. Elle est paramétrisable :<br/>
 * 1. L'exécuteur de requête parse les enregistrements du {@link ResultSet} à
 * l'aide d'un parser fourni par l'utilisateur.<br/>
 * 2. La clôture du {@code QueryExecutor} déclenche des traitements définis par
 * l'utilisateur. Des comportements par défaut sont implémentés.<br/>
 * Des comportements par défauts sont implémentés :<br/>
 * 1. Dans l'interface {@link OnException} pour la gestion des exceptions.<br/>
 * 2. Dans l'interface {@link HowToClose} pour la clôture de la connection.
 *
 *
 */
public abstract class AbstractQueryHandler implements IQueryHandler, AutoCloseable, IQueryExecutor, IWrapper<Connection>
{
    static final Logger LOGGER = LogManager.getLogger(AbstractQueryHandler.class);

    public static class AbstractQueryHandlerException extends RuntimeException
    {
        /**
         *
         */
        private static final long serialVersionUID = 461834091267244029L;

        public AbstractQueryHandlerException()
        {
            super();
        }

        public AbstractQueryHandlerException(String message)
        {
            super(message);
        }

        public AbstractQueryHandlerException(String message, Throwable cause)
        {
            super(message, cause);
        }

        public AbstractQueryHandlerException(Throwable cause)
        {
            super(cause);
        }

        protected AbstractQueryHandlerException(String message, Throwable cause, boolean enableSuppression,
                boolean writableStackTrace)
        {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }

    /**
     *
     * Contrat : gérer une exception.<br/>
     * Implémentations :<br/>
     * 1. Ne rien faire.<br/>
     * 2. Propager.<br/>
     * 3. Journaliser.
     *
     */
    public static interface OnException extends BiConsumer<Throwable, String>
    {
        /**
         * Capture l'exception sans la propager ni faire aucun traitement.
         */
        public static final OnException DO_NOTHING = (t, u) -> {
            LoggerHelper.trace(LOGGER, OnException.class, "DO_NOTHING");
        };
        /**
         * Capture l'exception et la propage en l'encapsulant dans une
         * {@link RuntimeException}
         */
        public static final OnException THROW = (t, u) -> {
            LoggerHelper.trace(LOGGER, OnException.class, "THROW");
            throw new AbstractQueryHandlerException(t);
        };
        /**
         * Capture l'exception pour la journaliser avec le {@link Logger} de
         * {@link AbstractQueryHandler}. L'exception n'est pas propagée.
         */
        public static final OnException LOG = (t, u) -> {
            LoggerHelper.trace(LOGGER, OnException.class, "LOG");
            LoggerHelper.error(LOGGER, "Erreur sur la requête :", u, t);
        };
        /**
         * Capture l'exception pour la journaliser avec le {@link Logger} de
         * {@link AbstractQueryHandler}. L'exception est ensuite propagée.
         */
        public static final OnException LOG_THEN_THROW = (t, u) -> {
            LoggerHelper.trace(LOGGER, OnException.class, "LOG_THEN_THROW");
            LOG.andThen(THROW).accept(t, u);
        };
    }

    /**
     *
     * Contrat : clore la connection.<br/>
     * Implémentations :<br/>
     * 1. Clore, ne pas propager les problèmes.<br/>
     * 2. Clore et propager.<br/>
     * 3. Commit, clore, propager.<br/>
     * 4. Rollback, clore, propager.<br/>
     * 5. Commit, rollback, propager.<br/>
     *
     */
    public static interface HowToClose extends Consumer<Connection>
    {
        /**
         * Ne tente même pas de fermer la connexion. Pratique si vous voulez
         * gérer manuellement cette connexion.
         */
        public static final Consumer<? super Connection> DO_NOT_CLOSE = (t) -> {
            LoggerHelper.trace(LOGGER, HowToClose.class, "DO_NOT_CLOSE");
        };
        /**
         * Tente de fermer la connection
         */
        public static final HowToClose JUST_CLOSE = (t) -> {
            LoggerHelper.trace(LOGGER, HowToClose.class, "JUST_CLOSE");
            try
            {
                t.close();
            } catch (Exception ex)
            {
                LoggerHelper.error(LOGGER, ex, "JUST_CLOSE");
            }
        };
        /**
         * Tente de fermer la connection, envoie une erreur si la clôture échoue
         */
        public static final HowToClose ASSERT_CLOSE = (t) -> {
            LoggerHelper.trace(LOGGER, HowToClose.class, "ASSERT_CLOSE");
            try
            {
                t.close();
            } catch (Exception ex)
            {
                throw new RuntimeException(ex);
            }
        };
        /**
         * Tente de commit la connection, envoie une erreur si la clôture échoue
         */
        public static final HowToClose COMMIT = (t) -> {
            LoggerHelper.trace(LOGGER, HowToClose.class, "COMMIT");
            try
            {
                t.commit();
            } catch (Exception ex)
            {
                throw new RuntimeException(ex);
            }
        };
        /**
         * Tente de commit puis de fermer la connection, propage les erreurs
         */
        public static final HowToClose COMMIT_CLOSE = (t) -> {
            LoggerHelper.trace(LOGGER, HowToClose.class, "COMMIT_CLOSE");
            COMMIT.andThen(ASSERT_CLOSE).accept(t);
        };
        /**
         * Tente de rollback la connection, envoie une erreur si la clôture
         * échoue
         */
        public static final HowToClose ROLLBACK = (t) -> {
            LoggerHelper.trace(LOGGER, HowToClose.class, "ROLLBACK");
            try
            {
                t.rollback();
                t.close();
            } catch (Exception ex4)
            {
                throw new RuntimeException(ex4);
            }
        };
        /**
         * Tente de rollback la connection puis de la clore, envoie une erreur
         * si la clôture échoue
         */
        public static final HowToClose ROLLBACK_CLOSE = (t) -> {
            LoggerHelper.trace(LOGGER, HowToClose.class, "ROLLBACK_CLOSE");
            ROLLBACK.andThen(ASSERT_CLOSE).accept(t);
        };
        /**
         * Tente de commit puis de fermer la connection. Si erreur : tente de
         * rollback puis fermer la connexion, propage les erreurs.
         */
        public static final HowToClose COMMIT_ROLLBACK_CLOSE = (t) -> {
            LoggerHelper.trace(LOGGER, HowToClose.class, "COMMIT_ROLLBACK_CLOSE");
            try
            {
                COMMIT_CLOSE.accept(t);
            } catch (Exception ex)
            {
                ROLLBACK_CLOSE.accept(t);
            }
        };
    }

    private Consumer<? super Connection> onClose = HowToClose.JUST_CLOSE;

    protected <T> T execute(Function<ResultSet, T> onResult, String query, BiConsumer<Throwable, String> onException,
            Consumer<? super Connection> howToClose)
    {
        T returned = null;
        LoggerHelper.trace(LOGGER, query);
        Connection connection = getWrapped();
        try
        {
            Statement stmt = connection.createStatement();
            try
            {
                ResultSet result = stmt.executeQuery(query);
                returned = onResult.apply(result);
            } finally
            {
                stmt.close();
                stmt = null;
            }
        } catch (SQLException ex)
        {
            onException.accept(ex, query);
        } finally
        {
            howToClose.accept(connection);
        }
        return returned;
    }

    protected <T> List<T> executeQuery(Function<ResultSet, T> onRecord, PreparedStatementBuilder query,
            BiConsumer<Throwable, String> onException, Consumer<? super Connection> howToClose)
    {
        LoggerHelper.trace(LOGGER, query);
        List<T> returned = new ArrayList<>();
        Connection connection = getWrapped();
        try
        {
            Statement stmt = connection.createStatement();
            try
            {
                ResultSet result = stmt.executeQuery(query.getQuery().toString());
                while (result.next())
                {
                    returned.add(onRecord.apply(result));
                }
            } finally
            {
                stmt.close();
                stmt = null;
            }
        } catch (SQLException ex)
        {
            onException.accept(ex, query.getQuery().toString());
        } finally
        {
            howToClose.accept(connection);
        }
        return returned;
    }

    protected void execute(String query, BiConsumer<Throwable, String> onException,
            Consumer<? super Connection> howToClose)
    {
        LoggerHelper.trace(LOGGER, query);
        Connection connection = getWrapped();
        try
        {
            Statement stmt = connection.createStatement();
            try
            {
                stmt.execute(query);
            } finally
            {
                stmt.close();
                stmt = null;
            }
        } catch (SQLException ex)
        {
            onException.accept(ex, query);
        } finally
        {
            howToClose.accept(connection);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.insee.siera.sqlengine.IQueryExecutor#getOnClose()
     */
    @Override
    public final Consumer<? super Connection> getOnClose()
    {
        return this.onClose;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.insee.siera.sqlengine.IQueryExecutor#setOnClose(java.util.function
     * .Consumer)
     */
    @Override
    public void setOnClose(Consumer<? super Connection> howToClose)
    {
        this.onClose = howToClose;
    }

    /**
     * Clôt la connection. Les modalités de clôture sont implémentées dans un
     * {@link Consumer} qui doit être passé à cet objet en utilisant la méthode
     * {@link #setOnClose(Consumer)}.
     *
     */
    @Override
    public void close() throws IOException
    {
        this.onClose.accept(getWrapped());
    }
}
