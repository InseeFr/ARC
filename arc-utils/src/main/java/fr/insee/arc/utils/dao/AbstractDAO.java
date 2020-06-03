package fr.insee.arc.utils.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import fr.insee.arc.utils.queryhandler.UtilitaireDAOQueryHandler;
import fr.insee.arc.utils.sqlengine.CompoundName;
import fr.insee.arc.utils.sqlengine.ContextName;
import fr.insee.arc.utils.sqlengine.DefaultNaming;
import fr.insee.arc.utils.sqlengine.Naming;
import fr.insee.arc.utils.sqlengine.StringToken;
import fr.insee.arc.utils.sqlengine.Tokens;
import fr.insee.arc.utils.sqlengine.model.SQLTable;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerHelper;

public abstract class AbstractDAO<T>
		implements IQueryHandler, AutoCloseable, IQueryExecutor, IWrapper<Connection>, IDelegate<IQueryHandler> {
	public static class DAOException extends RuntimeException {
		/**
		 *
		 */
		private static final long serialVersionUID = -5837362400860445324L;

		public DAOException() {
			super();
		}

		public DAOException(String message) {
			super(message);
		}

		public DAOException(String message, Throwable cause) {
			super(message, cause);
		}

		public DAOException(Throwable cause) {
			super(cause);
		}

		protected DAOException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
			super(message, cause, enableSuppression, writableStackTrace);
		}
	}

	private static final Logger LOGGER = Logger.getLogger(AbstractDAO.class);

	public final Function<ResultSet, Long> getId = (res) -> {
		try {
			Long returned = res.getLong(this.getIdEntity());
			return returned;
		} catch (SQLException ex) {
			throw new DAOException(ex);
		}
	};

	/**
	 * Pour traiter le resultat d'une requete qui ne contient qu'une seule colonne
	 * de résultat.
	 */
	public static final Function<ResultSet, String> GET_STRING = (res) -> {
		try {
			return res.getString(1);
		} catch (SQLException ex) {
			throw new DAOException(ex);
		}
	};

	/**
	 * Pour traiter la récupération de nom de tables.
	 */
	public static final Function<ResultSet, String> GET_QUALIFIED_TABLE = (res) -> {
		try {
			return res.getString("table_schema") + "." + res.getString("table_name");
		} catch (SQLException ex) {
			throw new DAOException(ex);
		}
	};

	public static final Function<ResultSet, ContextName> GET_CONTEXTNAME = (res) -> {
		try {
			return new CompoundName(
					new DefaultNaming(Tokens.TOK_SCHEMA, new StringToken(res.getString("table_schema")),
							Tokens.TOK_TABLE_NAME, new StringToken(res.getString("table_name"))),
					SQLTable.SCHEMA_PLUS_NOM);
		} catch (SQLException ex) {
			throw new DAOException(ex);
		}
	};

	private IQueryHandler impl;
	private ContextName nomTableEntity;

	public AbstractDAO(IQueryHandler handler, Naming aNaming) {
		this.impl = handler;
		this.nomTableEntity = new CompoundName(aNaming, SQLTable.SQL_QUALIFIED_TABLENAME_ASSEMBLER);
	}

	public AbstractDAO(IQueryHandler handler, ContextName aContextName) {
		this.impl = handler;
		this.nomTableEntity = aContextName;
	}

	/**
	 * Comment dois-je traiter un enregistrement
	 * 
	 * @return
	 */
	public abstract Function<ResultSet, T> getOnRecord();

	/**
	 * Récupération d'une entité à partir de l'identifiant de table
	 * 
	 * @param anId
	 * @return
	 * @throws Exception
	 */
	public final T getFromID(Long anId) throws Exception {
		StringBuilder requete = new StringBuilder();
		requete.append("\n SELECT *");
		requete.append("\n FROM " + getNomTableEntity().name());
		requete.append("\n WHERE " + getIdEntity() + " = " + anId);
		return executeQuery(getOnRecord(), requete.toString(), UtilitaireDAOQueryHandler.OnException.THROW).get(0);
	}

	/**
	 * Récupération d'une entité à partir de l'identifiant de table
	 * 
	 * @param anId
	 * @return
	 * @throws Exception
	 */
	public final T getFromID(String anId) throws Exception {
		StringBuilder requete = new StringBuilder();
		requete.append("\n SELECT *");
		requete.append("\n FROM " + getNomTableEntity().name());
		requete.append("\n WHERE " + getIdEntity() + " = '" + anId + "'");
		return executeQuery(getOnRecord(), requete.toString(), UtilitaireDAOQueryHandler.OnException.THROW).get(0);
	}

	/**
	 * Récupération de l'ensemble du contenu de la table.
	 * 
	 * @return
	 * @throws Exception
	 */
	public final List<T> getList() throws Exception {
		return getCuttedList(null, null, null, null);
	}

	/**
	 * Récupération de l'ensemble du contenu de la table. </br>
	 * Ordonné selon le tri par défaut de l'entity.
	 * 
	 * @return
	 * @throws Exception
	 */
	public final List<T> getOrderedList() throws Exception {
		return getCuttedList(getOrderByColumn(), null, null, null);
	}

	/**
	 * Récupère une partie de la table selon une condition WHERE
	 * 
	 * @return
	 * @throws Exception
	 */
	public final List<T> getListWhere(String aCondition) throws Exception {
		return getCuttedList(null, aCondition, null, null);
	}

	/**
	 * Renvoie une certaine partie d'une table.
	 * 
	 * @param anOrderByColumn la colonne sur laquelle trier
	 * @param aLimit          le nombre maximum d'enregistrements renvoyés
	 * @param anOffset        le nombre à partir duquel on renvoie les
	 *                        enregistrements
	 * @return
	 * @throws Exception
	 */
	public final List<T> getCuttedList(String anOrderByColumn, Integer aLimit, Integer anOffset) throws Exception {

		return getCuttedList(anOrderByColumn, null, aLimit, anOffset);

	}

	/**
	 * Renvoie une certaine partie d'une table.
	 * 
	 * @param anOrderByColumn la colonne sur laquelle trier
	 * @param aLimit          le nombre maximum d'enregistrements renvoyés
	 * @param anOffset        le nombre à partir duquel on renvoie les
	 *                        enregistrements
	 * @param aCondition      une condition where
	 * @return
	 * @throws Exception
	 */
	public final List<T> getCuttedList(String anOrderByColumn, String aCondition, Integer aLimit, Integer anOffset)
			throws Exception {
		StringBuilder requete = new StringBuilder();
		requete.append("\n SELECT * ");
		requete.append("\n FROM " + getNomTableEntity().name());
		if (StringUtils.isNotBlank(aCondition)) {
			requete.append("\n WHERE " + aCondition);
		}
		if (StringUtils.isNotBlank(anOrderByColumn)) {
			requete.append("\n ORDER BY " + anOrderByColumn);
		}
		if (aLimit != null) {
			requete.append("\n LIMIT " + aLimit);
		}
		if (anOffset != null) {
			requete.append("\n OFFSET " + anOffset);
		}
		return executeQuery(getOnRecord(), requete.toString(), UtilitaireDAOQueryHandler.OnException.THROW);
	}

	/**
	 * Poursuite du CRUD avec la supression
	 * 
	 * @param anId
	 * @throws Exception
	 */
	public final void deleteFromId(Long anId) throws Exception {
		StringBuilder requete = new StringBuilder();
		requete.append("\n DELETE FROM " + getNomTableEntity().name());
		requete.append("\n WHERE " + getIdEntity() + " = " + anId);
		executeUpdate(requete.toString(), UtilitaireDAOQueryHandler.OnException.THROW);
	}

	public final void deleteFromId(Long anId, String aTablePilotage) throws Exception {
		StringBuilder requete = new StringBuilder();
		requete.append("\n DELETE FROM " + getNomTableEntity().name());
		requete.append("\n WHERE " + getIdEntity() + " = " + anId);
		requete.append("\n AND " + anId + " NOT IN (SELECT " + getIdEntity() + " FROM " + aTablePilotage + ")");
		executeUpdate(requete.toString(), UtilitaireDAOQueryHandler.OnException.THROW);
	}

	public final Long getNewId() throws Exception {
		StringBuilder requete = new StringBuilder();
		requete.append("\n SELECT max(" + getIdEntity() + ") AS " + getIdEntity());
		requete.append("\n FROM " + getNomTableEntity().name());
		return 1 + executeQuery(this.getId, requete.toString(), UtilitaireDAOQueryHandler.OnException.THROW).get(0);
	}

	/**
	 * Renvoie le nom SQl de la colonne qui sert de tri naturel
	 * 
	 * @return
	 */
	public abstract String getOrderByColumn();

	/**
	 * Renvoie le nom SQL de la colonne identifiant de ligne.
	 * 
	 * @return
	 */
	public abstract String getIdEntity();

	public ContextName getNomTableEntity() {
		return this.nomTableEntity;
	}

	@Override
	public <U> List<U> executeQuery(Function<ResultSet, U> onRecord, String query,
			BiConsumer<Throwable, String> onException) throws SQLException {
		LoggerHelper.trace(LOGGER, query);
		return getDelegate().executeQuery(onRecord, query, onException);
	}

	@Override
	public <U> U execute(Function<ResultSet, U> onResult, String query, BiConsumer<Throwable, String> onException)
			throws SQLException {
		return getDelegate().execute(onResult, query, onException);
	}

	@Override
	public void executeUpdate(String query, BiConsumer<Throwable, String> onException) throws Exception {
		getDelegate().executeUpdate(query, onException);
	}

	@Override
	public Consumer<? super Connection> getOnClose() {
		return getDelegate().getOnClose();
	}

	@Override
	public void setOnClose(Consumer<? super Connection> howToClose) {
		getDelegate().setOnClose(howToClose);
	}

	@Override
	public void close() throws Exception {
		getDelegate().close();
	}

	@Override
	public Connection getWrapped() {
		return getDelegate().getWrapped();
	}

	@Override
	public void setWrapped(Connection connection) {
		getDelegate().setWrapped(connection);
	}

	@Override
	public IQueryHandler getDelegate() {
		return this.impl;
	}

	@Override
	public void setDelegate(IQueryHandler anImpl) {
		this.impl = anImpl;
	}
}
