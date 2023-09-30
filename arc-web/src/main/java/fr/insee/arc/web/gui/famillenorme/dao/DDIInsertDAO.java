package fr.insee.arc.web.gui.famillenorme.dao;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.DataObjectService;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.web.gui.famillenorme.ddi.DDIModeler;
import fr.insee.arc.web.gui.famillenorme.ddi.databaseobjects.ModelTable;
import fr.insee.arc.web.gui.famillenorme.ddi.databaseobjects.ModelVariable;

public class DDIInsertDAO {

	// give the table alias and column
	private DataObjectService databaseObjectService;

	public DDIInsertDAO(DataObjectService databaseObjectService) {
		super();
		this.databaseObjectService = databaseObjectService;
	}

	public void setDatabaseObjectService(DataObjectService databaseObjectService) {
		this.databaseObjectService = databaseObjectService;
	}

	/**
	 * Insert the parsed ddi model into arc
	 * 
	 * @param DDIModeler
	 */
	public void insertDDI(DDIModeler modeler) throws ArcException {

		// test if famille already exists otherwise do nothing
		List<String> familyToInsert = selectFamilleToInsert(modeler);

		if (familyToInsert.isEmpty()) {
			throw new ArcException(ArcExceptionMessage.DDI_FAMILY_ALREADY_EXISTS);
		}

		// insert ddi rules
		insertRulesDAO(familyToInsert, modeler);

	}

	/**
	 * return the list of famille to insert a famille can be inserted if it doesn't
	 * exists
	 * 
	 * @return
	 */
	private List<String> selectFamilleToInsert(DDIModeler modeler) throws ArcException {

		// collect the family found in DDI
		List<String> famillesInDDI = modeler.getModelTables().stream().map(ModelTable::getIdFamille)
				.map(String::toUpperCase).distinct().collect(Collectors.toList());

		ArcPreparedStatementBuilder query;

		// keep family that doesn't exist
		query = new ArcPreparedStatementBuilder();
		query.append("SELECT upper(id_famille) as famille_deja_existante ");
		query.append("FROM ");
		query.append(this.databaseObjectService.getView(ViewEnum.IHM_FAMILLE));
		query.append(" WHERE UPPER(id_famille) IN (");
		query.append(query.sqlListeOfValues(famillesInDDI));
		query.append(")");

		List<String> famillesDejaExistante = new GenericBean(
				UtilitaireDao.get(0).executeRequest(null, query)).mapContent()
						.get("famille_deja_existante");

		// keep only family found in ddi but not found in already existing arc family
		if (famillesDejaExistante != null) {
			famillesInDDI.removeAll(famillesDejaExistante);
		}

		return famillesInDDI;

	}

	/**
	 * insert the ddi rules into the database
	 * 
	 * @param familyToInsert
	 * @param modeler
	 * @throws ArcException
	 */
	private void insertRulesDAO(List<String> familyToInsert, DDIModeler modeler) throws ArcException {
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.append(insertFamily(familyToInsert));
		query.append(insertTables(familyToInsert, modeler));
		query.append(insertVariables(familyToInsert, modeler));
		UtilitaireDao.get(0).executeRequest(null, query);
	}

	/**
	 * prepare the query to insert the family into arc rules
	 * 
	 * @param familyToInsert
	 */
	private ArcPreparedStatementBuilder insertFamily(List<String> familyToInsert) {
		ArcPreparedStatementBuilder query;
		query = new ArcPreparedStatementBuilder();
		query.append(SQL.INSERT_INTO);
		query.append(this.databaseObjectService.getView(ViewEnum.IHM_FAMILLE));
		query.append("(id_famille) VALUES ");
		query.append(query.sqlListeOfValues(familyToInsert, "(", ")"));
		query.append(SQL.ON_CONFLICT_DO_NOTHING);
		query.append(";");
		query.append("\n");
		return query;
	}

	/**
	 * prepare the query to insert the model tables into arc rules
	 * 
	 * @param familyToInsert
	 * @param modeler
	 * @return
	 */
	private ArcPreparedStatementBuilder insertTables(List<String> familyToInsert, DDIModeler modeler) {
		ArcPreparedStatementBuilder query;
		query = new ArcPreparedStatementBuilder();

		for (ModelTable t : modeler.getModelTables()) {
			if (familyToInsert.contains(t.getIdFamille())) {
				query.append(SQL.INSERT_INTO);
				query.append(this.databaseObjectService.getView(ViewEnum.IHM_MOD_TABLE_METIER));
				query.append("(id_famille,nom_table_metier,description_table_metier) ");
				query.append("VALUES (");
				query.append(query
						.sqlListeOfValues(Arrays.asList(t.getIdFamille().toUpperCase(), arcTableName(t.getIdFamille(),t.getNomTableMetier()), t.getDescriptionTable())));
				query.append(")");
				query.append(SQL.ON_CONFLICT_DO_NOTHING);
				query.append(";");
				query.append("\n");
			}
		}
		return query;
	}

	/**
	 * prepare the query to insert the model tables into arc rules
	 * 
	 * @param familyToInsert
	 * @param modeler
	 * @return
	 */
	private ArcPreparedStatementBuilder insertVariables(List<String> familyToInsert, DDIModeler modeler) {
		ArcPreparedStatementBuilder query;
		query = new ArcPreparedStatementBuilder();

		for (ModelVariable v : modeler.getModelVariables()) {
			if (familyToInsert.contains(v.getIdFamille())) {
				query.append(SQL.INSERT_INTO);
				query.append(this.databaseObjectService.getView(ViewEnum.IHM_MOD_VARIABLE_METIER));
				query.append(
						"(id_famille,nom_table_metier,nom_variable_metier,type_variable_metier,description_variable_metier,type_consolidation) ");
				query.append("VALUES (");
				query.append(query.sqlListeOfValues(Arrays.asList(v.getIdFamille().toUpperCase(), arcTableName(v.getIdFamille(),v.getNomTableMetier()),
						v.getNomVariableMetier().toLowerCase(), v.getTypeVariableMetier().toLowerCase(), v.getDescriptionVariableMetier(),
						v.getTypeConsolidation())));
				query.append(")");
				query.append(SQL.ON_CONFLICT_DO_NOTHING);
				query.append(";");
				query.append("\n");
			}
		}
		return query;
	}
	
	/**
	 * Convert tablename parse in ddi to arc mapping table
	 * @param ddiTableName
	 * @return
	 */
	private String arcTableName(String ddiParsedFamilyName, String ddiParsedTableName)
	{
		List<String> arcTableNameTokens = Arrays.asList(TraitementPhase.MAPPING.toString().toLowerCase(),ddiParsedFamilyName.toLowerCase(),ddiParsedTableName.toLowerCase(),TraitementEtat.OK.toString().toLowerCase());
		
		return StringUtils.join(arcTableNameTokens,"_");
	}
	
	
}
