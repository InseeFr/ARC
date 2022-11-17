package fr.insee.arc.core.serviceinteractif.ddi.dao;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import fr.insee.arc.core.databaseobjects.DatabaseObjectService;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.core.databaseobjects.TableEnum;
import fr.insee.arc.core.model.famille.ModelTable;
import fr.insee.arc.core.model.famille.ModelVariable;
import fr.insee.arc.core.serviceinteractif.ddi.DDIModeler;
import fr.insee.arc.utils.dao.PreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.structure.GenericBean;

public class DDIInsertDAO {

	// give the table alias and column
	private DatabaseObjectService databaseObjectService;

	public DDIInsertDAO(DatabaseObjectService databaseObjectService) {
		super();
		this.databaseObjectService = databaseObjectService;
	}

	public void setDatabaseObjectService(DatabaseObjectService databaseObjectService) {
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
			throw new ArcException("Les familles présentent dans le DDI existe déjà");
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

		PreparedStatementBuilder query;

		// keep family that doesn't exist
		query = new PreparedStatementBuilder();
		query.append("SELECT upper(id_famille) as famille_deja_existante ");
		query.append("FROM ");
		query.append(this.databaseObjectService.getTable(TableEnum.IHM_FAMILLE));
		query.append(" WHERE UPPER(id_famille) IN (");
		query.append(query.sqlListe(famillesInDDI));
		query.append(")");

		List<String> famillesDejaExistante = new GenericBean(
				UtilitaireDao.get(DatabaseObjectService.POOL_NAME_USED).executeRequest(null, query)).mapContent()
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
		PreparedStatementBuilder query = new PreparedStatementBuilder();
		query.append(insertFamily(familyToInsert));
		query.append(insertTables(familyToInsert, modeler));
		query.append(insertVariables(familyToInsert, modeler));
		UtilitaireDao.get(DatabaseObjectService.POOL_NAME_USED).executeRequest(null, query);
	}

	/**
	 * prepare the query to insert the family into arc rules
	 * 
	 * @param familyToInsert
	 */
	private PreparedStatementBuilder insertFamily(List<String> familyToInsert) {
		PreparedStatementBuilder query;
		query = new PreparedStatementBuilder();
		query.append(SQL.INSERT_INTO);
		query.append(this.databaseObjectService.getTable(TableEnum.IHM_FAMILLE));
		query.append("(id_famille) VALUES ");
		query.append(query.sqlListe(familyToInsert, "(", ")"));
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
	private PreparedStatementBuilder insertTables(List<String> familyToInsert, DDIModeler modeler) {
		PreparedStatementBuilder query;
		query = new PreparedStatementBuilder();

		for (ModelTable t : modeler.getModelTables()) {
			if (familyToInsert.contains(t.getIdFamille())) {
				query.append(SQL.INSERT_INTO);
				query.append(this.databaseObjectService.getTable(TableEnum.IHM_MOD_TABLE_METIER));
				query.append("(id_famille,nom_table_metier,description_table_metier) ");
				query.append("VALUES (");
				query.append(query
						.sqlListe(Arrays.asList(t.getIdFamille().toUpperCase(), t.getNomTableMetier().toLowerCase(), t.getDescriptionTable())));
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
	private PreparedStatementBuilder insertVariables(List<String> familyToInsert, DDIModeler modeler) {
		PreparedStatementBuilder query;
		query = new PreparedStatementBuilder();

		for (ModelVariable v : modeler.getModelVariables()) {
			if (familyToInsert.contains(v.getIdFamille())) {
				query.append(SQL.INSERT_INTO);
				query.append(this.databaseObjectService.getTable(TableEnum.IHM_MOD_VARIABLE_METIER));
				query.append(
						"(id_famille,nom_table_metier,nom_variable_metier,type_variable_metier,description_variable_metier,type_consolidation) ");
				query.append("VALUES (");
				query.append(query.sqlListe(Arrays.asList(v.getIdFamille().toUpperCase(), v.getNomTableMetier().toLowerCase(),
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
}
