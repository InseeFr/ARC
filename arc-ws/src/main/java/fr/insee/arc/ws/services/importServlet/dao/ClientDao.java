package fr.insee.arc.ws.services.importServlet.dao;

import java.sql.Connection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.model.Delimiters;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.dao.TableNaming;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.ManipString;
import fr.insee.arc.ws.services.importServlet.bo.ArcClientIdentifier;
import fr.insee.arc.ws.services.importServlet.bo.JsonKeys;

public class ClientDao {

	protected static final Logger LOGGER = LogManager.getLogger(ClientDao.class);

	private long timestamp;
	private String environnement;
	private String client;
	private String famille;

	private String tableOfIdSource;

	Connection connection;

	public ClientDao(ArcClientIdentifier arcClientIdentifier) {
		this.timestamp = arcClientIdentifier.getTimestamp();
		this.environnement = arcClientIdentifier.getEnvironnement();
		this.client = arcClientIdentifier.getClient();
		this.famille = arcClientIdentifier.getFamille();

		this.tableOfIdSource = TableNaming.buildTableNameWithTokens(environnement,
				ColumnEnum.ID_SOURCE, client, timestamp);

	}

	/**
	 * Vérifie que le client peut consulter les tables métiers de la famille de
	 * normes
	 */
	public void verificationClientFamille() throws ArcException {
		LoggerHelper.debugAsComment(LOGGER, timestamp, "ClientDaoImpl#verificationClientFamille()");

		ArcPreparedStatementBuilder request = new ArcPreparedStatementBuilder();
		request.append("SELECT EXISTS (SELECT 1 FROM arc.ihm_client")
				.append(" WHERE id_application=" + request.quoteText(client))
				.append(" AND id_famille=" + request.quoteText(famille)).append(" LIMIT 1);");

		String bool = UtilitaireDao.get(0).executeRequestWithoutMetadata(connection, request).get(0).get(0);

		if (!bool.equals("t")) {
			throw new ArcException(ArcExceptionMessage.WS_RETRIEVE_DATA_FAMILY_FORBIDDEN);
		}

	}

	/**
	 * Créer une image des ids sources répondants aux critères et récupère la liste
	 * des noms des tables métiers
	 * 
	 * @param JSONObject contient les paramètres de la requête
	 * @return La liste des noms des tables métiers.
	 * @throws ArcException
	 */
	public List<String> getIdSrcTableMetier(JSONObject requeteJSON) throws ArcException {

		LoggerHelper.debugAsComment(LOGGER, timestamp, "ClientDaoImpl#getIdSrcTableMetier()");

		// Initialisation des variables

		// Préparation du block de requêtes à executer

		// Création de la requête de création de la table temporaire contenant la liste
		// des id_sources
		execQueryCreateTableOfIdSource(requeteJSON);

		return execQuerySelectBusinessDataTables();
		
	}

	/**
	 * return the list of business data table related to the famille provided
	 * @return
	 * @throws ArcException
	 */
	private List<String> execQuerySelectBusinessDataTables() throws ArcException {

		ArcPreparedStatementBuilder request = new ArcPreparedStatementBuilder();
		request.append("SELECT "+ColumnEnum.NOM_TABLE_METIER +" ");
		request.append("FROM " + ViewEnum.MOD_TABLE_METIER.getFullName(environnement) + " T1 ");
		request.append("WHERE T1.id_famille='" + this.famille + "' ");
		request.append("AND exists (select 1 from pg_tables T2 where ");
		request.append("T2.schemaname='" + ManipString.substringBeforeFirst(environnement, ".") + "' ");
		request.append("AND T1.nom_table_metier=T2.tablename);");

		return new GenericBean(UtilitaireDao.get(0).executeRequest(connection, request)).getColumnValues(ColumnEnum.NOM_TABLE_METIER.getColumnName());
	}

	/**
	 * Create the container with all the files name (idSource) that will be retrieve
	 * This query is built around the parameters given in the json request
	 * PERIODICITE : A for annual file, M for monthly file VALINF : the minimum
	 * validity date required for the file VALSUP : the maximum validity date
	 * required for the file NBFICHIERS : number of file to retrieve. most recent
	 * file first REPRISE : tell if arc wont mark file as retrieved
	 * 
	 * @param query
	 * @param requeteJSON
	 * @throws ArcException 
	 */
	private void execQueryCreateTableOfIdSource(JSONObject requeteJSON) throws ArcException {

		String periodicite = requeteJSON.getString(JsonKeys.PERIODICITE.getKey());
		String validiteInf = requeteJSON.keySet().contains(JsonKeys.VALINF.getKey())
				? requeteJSON.getString(JsonKeys.VALINF.getKey())
				: null;
		String validiteSup = requeteJSON.getString(JsonKeys.VALSUP.getKey());
		int nbFichiers = requeteJSON.keySet().contains(JsonKeys.NBFICHIERS.getKey())
				? requeteJSON.getInt(JsonKeys.NBFICHIERS.getKey())
				: 0;
		boolean reprise = requeteJSON.getBoolean(JsonKeys.REPRISE.getKey());

		StringBuilder query = new StringBuilder();
		query.append("DROP TABLE IF EXISTS " + tableOfIdSource + "; ");

		query.append("CREATE TABLE " + tableOfIdSource + " ");
		query.append("AS SELECT " + ColumnEnum.ID_SOURCE.getColumnName() + " FROM ");

		query.append("(");
		query.append("SELECT " + ColumnEnum.ID_SOURCE.getColumnName()
				+ (nbFichiers > 0 ? ", substr(date_entree,1,10)::date as date_entree " : " ") //
				+ "FROM " + ViewEnum.PILOTAGE_FICHIER.getFullName(this.environnement) + " T1 ");
		query.append(
				"WHERE '" + TraitementEtat.OK + "'=ANY(T1.etat_traitement) AND T1.periodicite='" + periodicite + "' ");

		if (validiteInf != null) {
			query.append("AND validite>='" + validiteInf + "' ");
		}

		query.append("AND validite<='" + validiteSup + "' AND T1.phase_traitement='" + TraitementPhase.MAPPING + "' ");
		query.append("AND EXISTS (SELECT 1 FROM " + ViewEnum.NORME.getFullName(environnement)
				+ " T2 WHERE T2.id_famille='" + famille + "' AND T1.id_norme=T2.id_norme) ");

		// if reprise is true, we want to retrieve all files, even the one which had
		// been already retrieved
		if (!reprise) {
			LoggerHelper.debugAsComment(LOGGER, "ClientDaoImpl.getIdSrcTableMetier() : Reprise = false");
			query.append("AND NOT '" + client + "' = ANY(coalesce(T1.client, ARRAY[]::text[])) ");
		} else {
			LoggerHelper.debugAsComment(LOGGER, "ClientDaoImpl.getIdSrcTableMetier() : Reprise = true");
		}

		query.append("GROUP BY " + ColumnEnum.ID_SOURCE.getColumnName() + (nbFichiers > 0 ? ", date_entree " : " ")); // )

		// on trie par ordre decroissant de date d'entree
		if (nbFichiers > 0) {
			query.append("ORDER BY date_entree DESC LIMIT ");
			query.append(nbFichiers);
		}
		query.append(") as foo; ");
		
		UtilitaireDao.get(0).executeBlock(connection, query);

	}

	/**
	 * Créer une image des tables métiers.
	 * 
	 * @param tablesMetierNames La liste des noms des tables métiers.
	 *
	 * @return liste des noms de tables images crées
	 * @throws ArcException
	 */
	public void createImages(List<String> tablesMetierNames) throws ArcException {
		LoggerHelper.debugAsComment(LOGGER, timestamp, "ClientDaoImpl.createImage()");

		for (String tableMetier : tablesMetierNames) {
			addImage(tableMetier);
		}
	}

	/**
	 * Créer une image des tables métiers.
	 * 
	 * @param tablesMetierNames La liste des noms des tables métiers.
	 *
	 * @return liste des noms de tables images crées
	 * @throws ArcException
	 */
	public void addImage(String tableMetier) throws ArcException {
		StringBuilder request = new StringBuilder();

		String nomTableImage = TableNaming.buildTableNameWithTokens(environnement, tableMetier, client, timestamp);

		request.append("DROP TABLE IF EXISTS " + nomTableImage + "; ");

		request.append("CREATE TABLE " + nomTableImage + FormatSQL.WITH_NO_VACUUM + " AS ");
		request.append("SELECT * ");
		request.append("FROM " + ViewEnum.getFullName(environnement, tableMetier) + " T1 WHERE true ");
		request.append("AND exists (SELECT 1 FROM " + tableOfIdSource + " T2 where T2."
				+ ColumnEnum.ID_SOURCE.getColumnName() + "=T1." + ColumnEnum.ID_SOURCE.getColumnName() + "); ");

		UtilitaireDao.get(0).executeBlock(connection, request);

	}

	/**
	 * Met à jours les colonnes client et date_client de la table
	 * environnement_pilotage_fichier.
	 * 
	 * @param tableSource
	 * @throws ArcException
	 */
	public void updatePilotage(String tableSource) throws ArcException {
		LoggerHelper.debugAsComment(LOGGER, timestamp, ": ClientDaoImpl.updatePilotage()");
		
		String clientOfTableSource = extractClientFromToken();

		StringBuilder query = new StringBuilder();
		query.append("UPDATE " + ViewEnum.PILOTAGE_FICHIER.getFullName(environnement) + " T1 ");
		query.append("SET client = array_append(client, '" + clientOfTableSource + "') ");
		query.append(", date_client = array_append( date_client, localtimestamp ) ");
		query.append("WHERE true ");
		query.append("AND EXISTS (SELECT 1 FROM " + tableSource + " T2 where T1."
				+ ColumnEnum.ID_SOURCE.getColumnName() + "=T2." + ColumnEnum.ID_SOURCE.getColumnName() + ") ");
		query.append("AND T1.phase_traitement='" + TraitementPhase.MAPPING + "';");

		UtilitaireDao.get(0).executeBlock(connection, query.toString());
	}

	/**
	 * extract the client token name from the client
	 * @param client2
	 * @return
	 */
	private String extractClientFromToken() {
		return 		
				ManipString.substringBeforeFirst(
						ManipString.substringAfterFirst(this.client, Delimiters.SQL_SCHEMA_DELIMITER),
						Delimiters.SQL_TOKEN_DELIMITER);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * fr.insee.arc_essnet.ws.dao.ClientDarcl(fr.insee.arc_essnet.ws.actions.Senarc
	 */
	public void createNmcl() throws ArcException {
		LoggerHelper.debugAsComment(LOGGER, "ClientDaoImpl.createNmcl()");

		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
		requete.append("SELECT tablename FROM pg_tables ")
				.append(" WHERE schemaname = " + requete.quoteText(environnement))
				.append(" AND tablename LIKE " + requete.quoteText("nmcl_%"));

		List<List<String>> nmclNames = UtilitaireDao.get(0).executeRequestWithoutMetadata(connection, requete);

		for (List<String> nmcl : nmclNames) {
			String nomTableImage = ViewEnum.getFullName(environnement, client + "_" + timestamp + "_" + nmcl.get(0));
			UtilitaireDao.get(0).executeImmediate(connection, "CREATE TABLE " + nomTableImage + FormatSQL.WITH_NO_VACUUM
					+ " AS SELECT * FROM " + ViewEnum.getFullName(environnement, nmcl.get(0)) + ";");
		}

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see fr.insee.arc_essnet.ws.dao.ClientDarcMetier(java.lang.String,
	 * fr.insee.arc_essnet.ws.actions.Senarc
	 */
	public void createVarMetier() throws ArcException {
		LoggerHelper.debugAsComment(LOGGER, "ClientDaoImpl.createVarMetier()");

		String nomTableImage = TableNaming.buildTableNameWithTokens(environnement,
				ViewEnum.MOD_VARIABLE_METIER, client, timestamp);

		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
		requete.append("CREATE TABLE " + nomTableImage + FormatSQL.WITH_NO_VACUUM + " AS");
		requete.append("\n SELECT * FROM " + ViewEnum.MOD_VARIABLE_METIER.getFullName(environnement));
		requete.append("\n WHERE id_famille = " + requete.quoteText(famille));
		requete.append(";");
		UtilitaireDao.get(0).executeRequest(connection, requete);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see fr.insee.arc_essnet.ws.dao.ClientDarcablesFamilles(long,
	 * java.lang.String)
	 */
	public void createTableFamille() throws ArcException {
		LoggerHelper.debugAsComment(LOGGER, "ClientDaoImpl.createTableFamille()");

		String nomTableImage = TableNaming.buildTableNameWithTokens(environnement,
				ViewEnum.EXT_MOD_FAMILLE, client, timestamp);
		
		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
		requete.append("CREATE TABLE " + nomTableImage + FormatSQL.WITH_NO_VACUUM
				+ " AS SELECT DISTINCT f.id_famille FROM arc.ihm_famille f INNER JOIN  "
				+ "arc.ihm_client c ON f.id_famille = c.id_famille WHERE lower(c.id_application) = lower("
				+ requete.quoteText(client) + ");");
		UtilitaireDao.get(0).executeRequest(connection, requete);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see fr.insee.arc_essnet.ws.dao.ClientDarcablesFamilles(long,
	 * java.lang.String)
	 */
	public void createTablePeriodicite() throws ArcException {
		LoggerHelper.debugAsComment(LOGGER, "ClientDaoImpl.createTablePeriodicite()");

		String nomTableImage = ViewEnum.getFullName(environnement,
				client + "_" + timestamp + "_" + ViewEnum.EXT_MOD_PERIODICITE.getTableName());

		UtilitaireDao.get(0).executeImmediate(connection, "CREATE TABLE " + nomTableImage + FormatSQL.WITH_NO_VACUUM
				+ " AS SELECT DISTINCT id, val FROM " + ViewEnum.EXT_MOD_PERIODICITE.getFullName() + ";");

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see fr.insee.arc_essnet.ws.dao.ClientDarcleMetier(java.lang.String,
	 * fr.insee.arc_essnet.ws.actions.Senarc
	 */
	public void createTableMetier() throws ArcException {
		LoggerHelper.debugAsComment(LOGGER, "ClientDaoImpl.sendTableMetier()");

		String nomTableImage = TableNaming.buildTableNameWithTokens(environnement, ViewEnum.MOD_TABLE_METIER, client, timestamp);

		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder(
				"\n CREATE TABLE " + nomTableImage + FormatSQL.WITH_NO_VACUUM + " AS");
		requete.append("\n SELECT * FROM " + ViewEnum.MOD_TABLE_METIER.getFullName(environnement) + " ");
		requete.append("\n WHERE id_famille = " + requete.quoteText(famille));
		requete.append(";");
		UtilitaireDao.get(0).executeRequest(connection, requete);

	}

	/**
	 * 
	 * @param client
	 * @param isSourceListTable : is it the table containing the list of id_source
	 *                          of the files to be marked ?
	 * @return
	 * @throws ArcException
	 */
	public String getAClientTable(boolean isSourceListTable) throws ArcException {
		
		String schema = ManipString.substringBeforeFirst(client, ".");
		String tableToFind = ViewEnum.normalizeTableName(ManipString.substringAfterFirst(client, ".").replace("_", "\\_") + "%");

		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
		requete.append("SELECT schemaname||'.'||tablename FROM pg_tables")
				.append(" WHERE tablename like " + requete.quoteText(tableToFind))
				.append(" AND schemaname=" + requete.quoteText(schema)).append(" AND tablename "
						+ (isSourceListTable ? "" : "NOT") + " like " + requete.quoteText("%id\\_source%"));

		String selectedTableName = UtilitaireDao.get(0).getString(connection, requete);
		
		return this.client + selectedTableName.substring(this.client.length());
		
	}

	public String getAClientTable() throws ArcException {
		return getAClientTable(false);
	}

	public String getIdTable() throws ArcException {
		return getAClientTable(true);
	}

	public void dropTable(String clientTable) throws ArcException {
		if (StringUtils.isBlank(clientTable)) {
			return;
		}
		UtilitaireDao.get(0).dropTable(connection, clientTable);
	}

	/**
	 * drop table from the client if some already exists
	 * 
	 * @throws ArcException
	 */
	public void dropPendingClientTables() throws ArcException {

		String findClientTable = ViewEnum.normalizeTableName(client + "\\_%");

		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
		requete.append("SELECT schemaname||'.'||tablename as " + ColumnEnum.TABLE_NAME + " FROM pg_tables");
		requete.append(" WHERE tablename like " + requete.quoteText(findClientTable));
		requete.append(" AND schemaname = " + requete.quoteText(this.environnement));

		List<String> tablesToDrop = new GenericBean(UtilitaireDao.get(0).executeRequest(connection, requete))
				.getColumnValues(ColumnEnum.TABLE_NAME.getColumnName());

		UtilitaireDao.get(0).executeImmediate(connection, FormatSQL.dropTable(tablesToDrop.toArray(new String[0])));

	}

}
