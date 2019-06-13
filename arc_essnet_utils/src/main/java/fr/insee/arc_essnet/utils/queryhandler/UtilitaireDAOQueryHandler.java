package fr.insee.arc_essnet.utils.queryhandler;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.log4j.Logger;

import fr.insee.arc_essnet.utils.dao.AbstractQueryHandler;
import fr.insee.arc_essnet.utils.dao.IQueryExecutor;
import fr.insee.arc_essnet.utils.dao.IQueryHandler;
import fr.insee.arc_essnet.utils.dao.IWrapper;
import fr.insee.arc_essnet.utils.dao.UtilitaireDao;
import fr.insee.arc_essnet.utils.utils.LoggerHelper;

/**
 *
 * Classe bridge qui lie {@link IQueryHandler} à {@link UtilitaireDao}. Si un
 * jour vous avez le temps, ce serait bien de faire en sorte que
 * {@link UtilitaireDao} utilise les services de {@link AbstractQueryHandler},
 * et donc d'implémenter dans cette dernière quelques fonctionnalités comme la
 * gestion fine des commit et rollback.
 *
 *
 */
public abstract class UtilitaireDAOQueryHandler
        implements IQueryHandler, AutoCloseable, IQueryExecutor, IWrapper<Connection>
{
    static final Logger LOGGER = Logger.getLogger(UtilitaireDAOQueryHandler.class);
    private static final int DEFAULT_MAX_RETRY = 120;
    private int maxRetry = 120;

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
         * {@link UtilitaireDAOQueryHandler}. L'exception n'est pas propagée.
         */
        public static final OnException LOG = (t, u) -> {
            LoggerHelper.trace(LOGGER, OnException.class, "LOG");
            LoggerHelper.error(LOGGER, "Erreur sur la requête :", u, t);
        };
        /**
         * Capture l'exception pour la journaliser avec le {@link Logger} de
         * {@link UtilitaireDAOQueryHandler}. L'exception est ensuite propagée.
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
    private String poolName;

    /**
     * @param poolName
     */
    protected UtilitaireDAOQueryHandler(String poolName)
    {
        this.poolName = poolName;
    }

    protected <T> T execute(Function<ResultSet, T> onResult, String query, BiConsumer<Throwable, String> onException,
            Consumer<? super Connection> howToClose)
    {
        T returned = null;

        try
        {

            return UtilitaireDao.get(this.poolName, maxRetry).executeRequest(getWrapped(), query,
                    UtilitaireDao.EntityProvider.getDefaultEntityProvider(onResult));
        } catch (SQLException ex)
        {
            onException.accept(ex, query);
        }

        return returned;
    }

    protected <T> List<T> executeQuery(Function<ResultSet, T> onRecord, String query,
            BiConsumer<Throwable, String> onException, Consumer<? super Connection> howToClose)
    {
         LoggerHelper.trace(LOGGER, query);
        List<T> returned = new ArrayList<>();

        try
        {
            return UtilitaireDao.get(this.poolName, maxRetry).executeRequest(getWrapped(), query,
                    UtilitaireDao.EntityProvider.getTypedListProvider(onRecord));
        } catch (SQLException ex)
        {
            onException.accept(ex, query);
        }
        return returned;
    }

    protected void execute(String query, BiConsumer<Throwable, String> onException,
            Consumer<? super Connection> howToClose)
    {
         LoggerHelper.trace(LOGGER, query);

        try
        {
  
            UtilitaireDao.get(this.poolName, maxRetry).executeImmediate(getWrapped(), query);
        } catch (SQLException ex)
        {
            onException.accept(ex, query);
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

    public int getMaxRetry() {
	return maxRetry;
    }

    public void setMaxRetry(int maxRetry) {
	this.maxRetry = maxRetry;
    }
    
    public void restsetMaxRetry() {
	setMaxRetry(DEFAULT_MAX_RETRY);
    }
}
