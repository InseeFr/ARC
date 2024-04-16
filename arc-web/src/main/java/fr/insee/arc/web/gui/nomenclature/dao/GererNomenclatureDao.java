package fr.insee.arc.web.gui.nomenclature.dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.DataObjectService;
import fr.insee.arc.core.dataobjects.SchemaEnum;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.database.Delimiters;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.web.gui.all.util.VObject;
import fr.insee.arc.web.gui.all.util.VObjectHelperDao;
import fr.insee.arc.web.gui.all.util.VObjectService;

@Component
public class GererNomenclatureDao extends VObjectHelperDao {

	/**
	 * dao call to build list nomenclatures vobject
	 * 
	 * @param viewListNomenclatures
	 */
	public void initializeViewListNomenclatures(VObject viewListNomenclatures) {
		ViewEnum dataModelListNomenclatures = ViewEnum.IHM_NMCL;
		// view query
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.append(SQL.SELECT);
		query.append(query.sqlListeOfColumnsFromModel(dataModelListNomenclatures));
		query.append(SQL.FROM);
		query.append(dataObjectService.getView(dataModelListNomenclatures));
		// default value
		Map<String, String> defaultInputFields = new HashMap<>();
		// initialize vobject
		vObjectService.initialize(viewListNomenclatures, query, dataObjectService.getView(dataModelListNomenclatures),
				defaultInputFields);
	}

	/**
	 * dao call to build nomenclature vobject
	 * 
	 * @param viewNomenclature
	 * @param nomTable
	 * @param tableSelected
	 */
	public void initializeViewNomenclature(VObject viewNomenclature, String nomTable, String tableSelected) {
		// view query
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.append(SQL.SELECT);
		query.append("*"); // ?
		query.append(SQL.FROM);
		query.append("arc." + tableSelected); // ?
		// default value
		Map<String, String> defaultInputFields = new HashMap<>();
		defaultInputFields.put(nomTable, tableSelected);
		// initialize vobject
		vObjectService.initialize(viewNomenclature, query, "arc." + tableSelected, defaultInputFields);
	}

	/**
	 * dao call to build nomenclature schema vobject
	 * 
	 * @param viewSchemaNmcl
	 */
	public void initializeViewSchemaNmcl(VObject viewSchemaNmcl) {
		String nmclTableName = selectedRecords.get(ColumnEnum.NOM_TABLE.getColumnName()).get(0);
		// view query
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.SELECT);
		query.build(query.quoteText(typeNomenclature(nmclTableName)), SQL.AS, ColumnEnum.TYPE_NMCL);
		query.build(SQL.COMMA, "column_name", SQL.AS, ColumnEnum.NOM_COLONNE);
		query.build(SQL.COMMA, "data_type", SQL.AS, ColumnEnum.TYPE_COLONNE);
		query.build(SQL.FROM);
		query.build("information_schema.columns");
		query.build(SQL.WHERE);
		query.build(ColumnEnum.TABLE_SCHEMA, "='arc'");
		query.build(SQL.AND);
		query.build(ColumnEnum.TABLE_NAME, "=", query.quoteText(nmclTableName));

		// default value
		Map<String, String> defaultInputFields = new HashMap<>();
		defaultInputFields.put(ColumnEnum.TYPE_NMCL.getColumnName(), typeNomenclature(nmclTableName));

		// initialize vobject
		vObjectService.initialize(viewSchemaNmcl, query, "arc." + nmclTableName, defaultInputFields);
	}

	/**
	 * compute the nomenclature type
	 * 
	 * @param nomTable
	 * @return
	 */
	private static String typeNomenclature(String nomTable) {
		String[] tokens = nomTable.split(Delimiters.SQL_TOKEN_DELIMITER);
		StringBuilder typeNomenclature = new StringBuilder();
		for (int i = 0; i < tokens.length - 1; i++) {
			typeNomenclature
					.append((i > 0 ? fr.insee.arc.utils.textUtils.IConstanteCaractere.underscore : "") + tokens[i]);
		}
		return typeNomenclature.toString();
	}
	
	/**
	 * renames the nomenclature table when it is updated in vObject
	 * 
	 * @param nameBefore
	 * @param nameAfter
	 * @throws ArcException 
	 */
	public void updateNomenclatureDansBase(String nameBefore, String nameAfter) throws ArcException {
		String fullNameBefore = ViewEnum.getFullName(SchemaEnum.ARC_METADATA.getSchemaName(), nameBefore);
		ArcPreparedStatementBuilder queryRename = new ArcPreparedStatementBuilder();
		queryRename.build(SQL.ALTER, SQL.TABLE, SQL.IF_EXISTS, fullNameBefore);
		queryRename.build(SQL.RENAME_TO, nameAfter);
		UtilitaireDao.get(0).executeImmediate(null, queryRename);
	}

	public void importNomenclatureDansBase(VObject viewListNomenclatures, MultipartFile fileUpload)
			throws ArcException {

		try (BufferedReader rd = new BufferedReader(new InputStreamReader(fileUpload.getInputStream()))) {

			// Colonnes et types pour le schéma de la table temporaire
			String[] colonnes = rd.readLine().split(";");
			String[] types = rd.readLine().split(";");

			// Création de la table temporaire
			creationTableDeNomenclatureTemporaire(viewListNomenclatures, colonnes, types);

			// Remplissage de la table
			remplissageTableTemporaire(viewListNomenclatures, rd);

			// Création de la table définitive
			creationTableDefinitif(viewListNomenclatures);
		} catch (IOException e) {
			throw new ArcException(e, ArcExceptionMessage.IHM_NMCL_IMPORT_FAILED);
		}

	}

	private void creationTableDeNomenclatureTemporaire(VObject viewListNomenclatures, String[] colonnes, String[] types)
			throws ArcException {
		String newNomenclatureName = viewListNomenclatures.mapContentSelected()
				.get(ColumnEnum.NOM_TABLE.getColumnName()).get(0);
		ArcPreparedStatementBuilder createTableRequest = new ArcPreparedStatementBuilder();
		createTableRequest.build(SQL.DROP, SQL.TABLE, SQL.IF_EXISTS, "arc.temp_", newNomenclatureName, ";");
		createTableRequest.build(SQL.CREATE, SQL.TABLE, "arc.temp_", newNomenclatureName, " (");
		for (int i = 0; i < colonnes.length; i++) {
			if (i > 0) {
				createTableRequest.append(", ");
			}
			createTableRequest.append(colonnes[i] + " " + types[i]);
		}
		createTableRequest.append(");");

		UtilitaireDao.get(0).executeImmediate(null, createTableRequest);
	}

	private void creationTableDefinitif(VObject viewListNomenclatures) throws ArcException {
		String newNomenclatureName = viewListNomenclatures.mapContentSelected()
				.get(ColumnEnum.NOM_TABLE.getColumnName()).get(0);
		ArcPreparedStatementBuilder creationTableDef = new ArcPreparedStatementBuilder();

		String temporaryNewNomenclatureName = "temp_" + newNomenclatureName;

		creationTableDef.build(SQL.DROP, SQL.TABLE, SQL.IF_EXISTS,
				ViewEnum.getFullName(SchemaEnum.ARC_METADATA.getSchemaName(), newNomenclatureName), ";");
		creationTableDef.build(SQL.CREATE, SQL.TABLE,
				ViewEnum.getFullName(SchemaEnum.ARC_METADATA.getSchemaName(), newNomenclatureName));
		creationTableDef.build(SQL.AS, SQL.SELECT, "*", SQL.FROM,
				ViewEnum.getFullName(SchemaEnum.ARC_METADATA.getSchemaName(), temporaryNewNomenclatureName), ";");
		creationTableDef.build(SQL.DROP, SQL.TABLE,
				ViewEnum.getFullName(SchemaEnum.ARC_METADATA.getSchemaName(), temporaryNewNomenclatureName), ";");
		UtilitaireDao.get(0).executeBlock(null, creationTableDef);
	}

	private void remplissageTableTemporaire(VObject viewListNomenclatures, BufferedReader rd) throws ArcException {
		String newNomenclatureName = viewListNomenclatures.mapContentSelected()
				.get(ColumnEnum.NOM_TABLE.getColumnName()).get(0);
		UtilitaireDao.get(0).importingWithReader(null, "arc.temp_" + newNomenclatureName, rd, false, ";");
	}

	public VObjectService getvObjectService() {
		return vObjectService;
	}

	public void setvObjectService(VObjectService vObjectService) {
		this.vObjectService = vObjectService;
	}

	public DataObjectService getDataObjectService() {
		return dataObjectService;
	}

	public void setDataObjectService(DataObjectService dataObjectService) {
		this.dataObjectService = dataObjectService;
	}

	public void execQueryDeleteListNomenclature(VObject viewListNomenclatures) throws ArcException {
		// Suppression de la table nom table
		String nomTable = ViewEnum.getFullName(SchemaEnum.ARC_METADATA.getSchemaName(),
				viewListNomenclatures.mapContentSelected().get(ColumnEnum.NOM_TABLE.getColumnName()).get(0));
		
		UtilitaireDao.get(0).executeImmediate(null, FormatSQL.dropTable(nomTable));

        			
		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
		requete.build(SQL.SELECT, ColumnEnum.NOM_TABLE.getColumnName(), SQL.FROM, ViewEnum.IHM_NMCL.getFullName());
		requete.build(SQL.WHERE, ColumnEnum.NOM_TABLE.getColumnName(), SQL.LIKE, requete.quoteText(typeNomenclature(nomTable)+"%"));
		requete.build(SQL.AND, ColumnEnum.NOM_TABLE.getColumnName(), "<>", requete.quoteText(nomTable));

	}

	public boolean execQueryIsSelectedNomenclatureTableExists(String selectedTable) {
		return UtilitaireDao.get(0).isTableExiste(null,
				ViewEnum.getFullName(SchemaEnum.ARC_METADATA.getSchemaName(), selectedTable));
	}

	/**
	 * 
	 * Vérifie si un nom de colonnes est valide. <br/>
	 * Si ce n'est pas le cas une exception est jetée.
	 * 
	 * @param nomColonne
	 * @throws ArcException
	 */
	public boolean isColonneValide(String nomColonne) {
		try {
			UtilitaireDao.get(0).executeImmediate(null, "SELECT null as " + nomColonne);
		} catch (ArcException e) {
			return false;
		}
		return true;
	}

	/**
	 * 
	 * Vérifie si un nom de colonnes est valide. <br/>
	 * Si ce n'est pas le cas une exception est jetée.
	 * 
	 * @param nomColonne
	 * @throws ArcException
	 */
	public boolean isTypeValide(String typeColonne) {
		try {
			UtilitaireDao.get(0).executeImmediate(null, "SELECT null::" + typeColonne);
		} catch (ArcException e) {
			return false;
		}
		return true;
	}


	
}
