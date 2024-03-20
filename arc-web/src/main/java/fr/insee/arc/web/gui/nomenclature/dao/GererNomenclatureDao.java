package fr.insee.arc.web.gui.nomenclature.dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
		ViewEnum dataModelSchemaNmcl = ViewEnum.IHM_SCHEMA_NMCL;
		// view query
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.append(SQL.SELECT);
		query.append(query.sqlListeOfColumnsFromModel(dataModelSchemaNmcl));
		query.append(SQL.FROM);
		query.append(dataObjectService.getView(dataModelSchemaNmcl));
		query.append(SQL.WHERE);
		query.append(ColumnEnum.TYPE_NMCL).append("=");
		query.append(
				query.quoteText(typeNomenclature(selectedRecords.get(ColumnEnum.NOM_TABLE.getColumnName()).get(0))));

		// default value
		Map<String, String> defaultInputFields = new HashMap<>();
		defaultInputFields.put(ColumnEnum.TYPE_NMCL.getColumnName(),
				typeNomenclature(selectedRecords.get(ColumnEnum.NOM_TABLE.getColumnName()).get(0)));

		// initialize vobject
		vObjectService.initialize(viewSchemaNmcl, query, dataObjectService.getView(dataModelSchemaNmcl),
				defaultInputFields);
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

	public void importNomenclatureDansBase(VObject viewListNomenclatures, MultipartFile fileUpload)
			throws ArcException {

		try (BufferedReader rd = new BufferedReader(new InputStreamReader(fileUpload.getInputStream()))) {

			// Verification des colonnes
			String[] colonnes = rd.readLine().split(";");
			String[] types = rd.readLine().split(";");
			verificationColonnes(viewListNomenclatures, colonnes, types);

			// Verification du nombre de colonnes
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

	/**
	 * @param colonnesFichier
	 * @throws ArcException
	 */
	private void verificationColonnes(VObject viewListNomenclatures, String[] colonnesFichier, String[] typesFichier)
			throws ArcException {
		String newNomenclatureName = viewListNomenclatures.mapContentSelected()
				.get(ColumnEnum.NOM_TABLE.getColumnName()).get(0);
		String typeNomenclature = typeNomenclature(newNomenclatureName);

		List<String> colonnesDansFichier = convertListToLowerTrim(colonnesFichier);
		List<String> typesDansFichier = convertListToLowerTrim(typesFichier);

		// Verification des noms de colonnes et des types
		String selectNomColonne = "SELECT nom_colonne FROM arc.ihm_schema_nmcl WHERE type_nmcl = '" + typeNomenclature
				+ "' ORDER BY nom_colonne";
		List<String> colonnesDansTableIhmSchemaNmcl = new ArrayList<String>();
		UtilitaireDao.get(0).getList(null, selectNomColonne, colonnesDansTableIhmSchemaNmcl);
		areListsEquals(colonnesDansFichier, colonnesDansTableIhmSchemaNmcl, "field");

		// Verification des types
		String selectTypeColonne = "SELECT type_colonne FROM arc.ihm_schema_nmcl WHERE type_nmcl = '" + typeNomenclature
				+ "' ORDER BY nom_colonne";
		List<String> typesDansTableIhmSchemaNmcl = new ArrayList<String>();
		UtilitaireDao.get(0).getList(null, selectTypeColonne, typesDansTableIhmSchemaNmcl);
		areListsEquals(typesDansFichier, typesDansTableIhmSchemaNmcl, "type");

	}

	private void areListsEquals(List<String> listeFichier, List<String> listIhmSchemaNmcl, String elementDescription)
			throws ArcException {
		for (String e : listeFichier) {
			if (!listIhmSchemaNmcl.contains(e)) {
				throw new ArcException(ArcExceptionMessage.IHM_NMCL_COLUMN_IN_FILE_BUT_NOT_IN_SCHEMA, e);
			}
		}

		// Et réciproquement si toutes les colonnes sont présentes dans le fichier
		for (String e : listIhmSchemaNmcl) {
			if (!listeFichier.contains(e)) {
				throw new ArcException(ArcExceptionMessage.IHM_NMCL_COLUMN_IN_SCHEMA_BUT_NOT_IN_FILE, e);
			}
		}
	}

	private List<String> convertListToLowerTrim(String[] tab) {
		List<String> list = new ArrayList<>();
		for (int i = 0; i < tab.length; i++) {
			list.add(tab[i].toLowerCase().trim());
		}
		return list;
	}

	private void creationTableDeNomenclatureTemporaire(VObject viewListNomenclatures, String[] colonnes, String[] types)
			throws ArcException {
		String newNomenclatureName = viewListNomenclatures.mapContentSelected()
				.get(ColumnEnum.NOM_TABLE.getColumnName()).get(0);
		StringBuilder createTableRequest = new StringBuilder();
		createTableRequest.append("\n DROP TABLE IF EXISTS arc.temp_" + newNomenclatureName + ";");
		createTableRequest.append("\n CREATE TABLE arc.temp_" + newNomenclatureName + " (");
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
		StringBuilder creationTableDef = new StringBuilder();

		String temporaryNewNomenclatureName = "temp_" + newNomenclatureName;

		creationTableDef.append("\n DROP TABLE IF EXISTS "
				+ ViewEnum.getFullName(SchemaEnum.ARC_METADATA.getSchemaName(), newNomenclatureName) + ";");
		creationTableDef.append("\n CREATE TABLE "
				+ ViewEnum.getFullName(SchemaEnum.ARC_METADATA.getSchemaName(), newNomenclatureName));
		creationTableDef.append("\n AS SELECT * FROM "
				+ ViewEnum.getFullName(SchemaEnum.ARC_METADATA.getSchemaName(), temporaryNewNomenclatureName) + ";");
		creationTableDef.append("\n DROP TABLE "
				+ ViewEnum.getFullName(SchemaEnum.ARC_METADATA.getSchemaName(), temporaryNewNomenclatureName) + ";");
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
		String nomTable = viewListNomenclatures.mapContentSelected().get(ColumnEnum.NOM_TABLE.getColumnName()).get(0);
		
        UtilitaireDao.get(0).executeImmediate(null, FormatSQL.dropTable(nomTable));
        
                    
        StringBuilder requete = new StringBuilder();
        requete.append("\n SELECT nom_table FROM arc.ihm_nmcl ");
        requete.append("\n WHERE nom_table like '" + typeNomenclature(nomTable) + "%'");
        requete.append("\n AND nom_table <> '" + nomTable + "'");
        
        List<String> listeTables = UtilitaireDao.get(0).getList(null, requete.toString(), new ArrayList<>());
        
        if (listeTables.isEmpty()) {
            requete = new StringBuilder();
            requete.append("\n DELETE FROM arc.ihm_schema_nmcl");
            requete.append("\n WHERE type_nmcl = '" + typeNomenclature(nomTable) + "'");
            UtilitaireDao.get(0).executeImmediate(null, requete.toString());
        }
        
	}

	public boolean execQueryIsSelectedNomenclatureTableExists(String selectedTable) {
		return UtilitaireDao.get(0)
		.isTableExiste(null, ViewEnum.getFullName(SchemaEnum.ARC_METADATA.getSchemaName(), selectedTable));
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
