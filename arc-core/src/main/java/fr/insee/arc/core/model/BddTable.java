package fr.insee.arc.core.model;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.utils.sqlengine.ContextName;
import fr.insee.arc.utils.sqlengine.Naming;
import fr.insee.arc.utils.sqlengine.Namings;
import fr.insee.arc.utils.utils.LoggerHelper;

public class BddTable {
	public Map<String, ContextName> getNames() {
		return names;
	}

	public void setNames(Map<String, ContextName> names) {
		this.names = names;
	}

	protected static final Logger LOGGER = LogManager.getLogger(BddTable.class);

	private String schema;

	public static final String SCHEMA_DEFAUT = "arc";


	public static final String NOM_TABLE_PILOTAGE_BATCH = "pilotage_batch";
	public static final String ID_TABLE_PILOTAGE_BATCH = "PILOTAGE_BATCH";

	public static final String NOM_TABLE_IHM_CALENDRIER = "ihm_calendrier";
	public static final String ID_TABLE_IHM_CALENDRIER = "IHM_CALENDRIER";

	public static final String NOM_TABLE_IHM_RULESET = "ihm_jeuderegle";
	public static final String ID_TABLE_IHM_RULESETS = "IHM_JEUDEREGLE";

	public static final String NOM_TABLE_IHM_CHARGEMENT_REGLE= "ihm_chargement_regle";
	public static final String ID_TABLE_IHM_CHARGEMENT_REGLE= "IHM_CHARGEMENT_REGLE";

	public static final String NOM_TABLE_IHM_CLIENT= "ihm_client";
	public static final String ID_TABLE_IHM_CLIENT= "IHM_CLIENT";

	public static final String NOM_TABLE_IHM_CONTROLE_REGLE ="ihm_controle_regle";
	public static final String ID_TABLE_IHM_CONTROLE_REGLE ="IHM_CONTROLE_REGLE";

	public static final String NOM_TABLE_IHM_ENTREPOT ="ihm_entrepot";
	public static final String ID_TABLE_IHM_ENTREPOT ="IHM_ENTREPOT";

	public static final String NOM_TABLE_IHM_EXPRESSION = "ihm_expression";
	public static final String ID_TABLE_IHM_EXPRESSION = "IHM_EXPRESSION";
	
	public static final String NOM_TABLE_IHM_FAMILLE ="ihm_famille";
	public static final String ID_TABLE_IHM_FAMILLE ="IHM_FAMILLE";

	public static final String NOM_TABLE_IHM_FILTRAGE_REGLE= "ihm_filtrage_regle";
	public static final String ID_TABLE_IHM_FILTRAGE_REGLE= "IHM_FILTRAGE_REGLE";

	public static final String NOM_TABLE_IHM_MAPPING_REGLE= "ihm_mapping_regle";
	public static final String ID_TABLE_IHM_MAPPING_REGLE= "IHM_MAPPING_REGLE";

	public static final String NOM_TABLE_IHM_MAPPING_REGLE_BIS= "ihm_mapping_regle_bis";
	public static final String ID_TABLE_IHM_MAPPING_REGLE_BIS= "IHM_MAPPING_REGLE_BIS";

	public static final String NOM_TABLE_IHM_MAPPING_SAV ="ihm_mapping_sav";
	public static final String ID_TABLE_IHM_MAPPING_SAV ="IHM_MAPPING_SAV";

	public static final String NOM_TABLE_IHM_MOD_TABLE_METIER ="ihm_mod_table_metier";
	public static final String ID_TABLE_IHM_MOD_TABLE_METIER ="IHM_MOD_TABLE_METIER";

	public static final String NOM_TABLE_IHM_NMCL ="ihm_nmcl";
	public static final String ID_TABLE_IHM_NMCL ="IHM_NMCL";

	public static final String NOM_TABLE_IHM_NORMAGE_REGLE ="ihm_normage_regle";
	public static final String ID_TABLE_IHM_NORMAGE_REGLE ="IHM_NORMAGE_REGLE";

	public static final String NOM_TABLE_IHM_NORME= "ihm_norme";
	public static final String ID_TABLE_IHM_NORME= "IHM_NORME";

	public static final String NOM_TABLE_IHM_PARAMETTRAGE_ORDRE_PHASE ="ihm_paramettrage_ordre_phase";
	public static final String ID_TABLE_IHM_PARAMETTRAGE_ORDRE_PHASE ="IHM_PARAMETTRAGE_ORDRE_PHASE";

	public static final String NOM_TABLE_IHM_SCHEMA_NMCL ="ihm_schema_nmcl";
	public static final String ID_TABLE_IHM_SCHEMA_NMCL ="IHM_SCHEMA_NMCL";

	public static final String NOM_TABLE_IHM_SEUIL= "ihm_seuil";
	public static final String ID_TABLE_IHM_SEUIL= "IHM_SEUIL";

	public static final String NOM_TABLE_IHM_USER ="ihm_user";
	public static final String ID_TABLE_IHM_USER ="IHM_USER";

	public static final String NOM_TABLE_ORDRE_PHASE = "paramettrage_ordre_phase";
	public static final String ID_TABLE_PHASE_ORDER = "PARAMETTRAGE_ORDRE_PHASE";

	public static final String NOM_TABLE_PILOTAGE_FICHIER = "pilotage_fichier";
	public static final String ID_TABLE_PILOTAGE_FICHIER = "PILOTAGE_FICHIER";

	public static final String NOM_TABLE_PILOTAGE_FICHIER_T = "pilotage_fichier_t";
	public static final String ID_TABLE_PILOTAGE_FICHIER_T = "PILOTAGE_FICHIER_T";

	public static final String NOM_TABLE_PILOTAGE_ARCHIVE = "pilotage_archive";
	public static final String ID_TABLE_PILOTAGE_ARCHIVE = "PILOTAGE_ARCHIVE";

	public static final String NOM_TABLE_NORME = "norme";
	public static final String ID_TABLE_NORME_SPECIFIC = "NORME_SPECIFIQUE";



	public static final String NOM_TABLE_CALENDRIER = "calendrier";
	public static final String ID_TABLE_CALENDRIER_BAS = "CALENDRIER_SPECIFIQUE";

	public static final String NOM_TABLE_RULESET = "jeuDeRegle";
	public static final String ID_TABLE_RULESETS_BAS = "JEUDEREGLE_SPCIFIC";

	public static final String NOM_TABLE_CHARGEMENT_REGLE = "chargement_regle";
	public static final String ID_TABLE_CHARGEMENT_REGLE = "CHARGEMENT_REGLE";

	public static final String NOM_TABLE_NORMAGE_REGLE = "normage_regle";
	public static final String ID_TABLE_NORMAGE_REGLE = "NORMAGE_REGLE";

	public static final String NOM_TABLE_CONTROLE_REGLE = "controle_regle";
	public static final String ID_TABLE_CONTROLE_REGLE = "CONTROLE_REGLE";

	public static final String NOM_TABLE_FILTRAGE_REGLE = "filtrage_regle";
	public static final String ID_TABLE_FILTRAGE_REGLE = "FILTRAGE_REGLE";

	public static final String NOM_TABLE_MAPPING_REGLE = "mapping_regle";
	public static final String ID_TABLE_MAPPING_REGLE = "MAPPING_REGLE";

	public static final String NOM_TABLE_EXPRESSION = "expression";
	public static final String ID_TABLE_EXPRESSION = "EXPRESSION";

	public static final String NOM_TABLE_SEUIL = "seuil";
	public static final String ID_TABLE_SEUIL = "SEUIL";

	public static final String SCHEMA_ARC_PROD = "arc_prod";

	public static final String ID_TABLE_PREVIOUS_PHASE = "id_table_previous_phase";
	public static final String ID_TABLE_PILOTAGE_TEMP = "ID_TABLE_PILOTAGE_TEMP";
	public static final String ID_TABLE_PILOTAGE_TEMP_THREAD = "ID_TABLE_PILOTAGE_TEMP_THREAD";
	public static final String ID_TABLE_OUT_KO = "ID_TABLE_OUT_KO";
	public static final String ID_TABLE_OUT_OK = "ID_TABLE_OUT_OK";

	public static final String ID_WORKING_TABLE_KO = "ID_WORKING_TABLE_KO";
	public static final String ID_WORKING_TABLE_OK = "ID_WORKING_TABLE_OK";

	public static final String ID_TABLE_POOL_DATA = "ID_TABLE_POOL_DATA";

	public static final String ID_TABLE_TEMP= "TEMP";
	public static final String NOM_TABLE_TEMP= "temp";

	public static final String ID_TABLE_CHARGEMENT_BRUTAL = "BRUTAL";
	public static final String NOM_TABLE_CHARGEMENT_BRUTAL = "brutal";

	public static final String ID_TABLE_TEMP_ALL = "TEMP_ALL";
	public static final String NOM_TABLE_TEMP_ALL = "temp_all";


	/**
	 * Map contenant les noms de tables qualifiées (c-a-d avec le schéma).
	 */
	private Map<String, ContextName> names = new HashMap<>();

	public BddTable(String schema) {
		if (schema != null) {
			this.schema = schema.replace(".", "_");
		}
		initNomsTables();
	}

	private void initNomsTables() {
		/*
		 * Default schema
		 */
		addTable(ID_TABLE_IHM_NORME, Namings.GET_TABLE_NAME.apply(SCHEMA_DEFAUT, NOM_TABLE_IHM_NORME));
		addTable(ID_TABLE_IHM_CALENDRIER, Namings.GET_TABLE_NAME.apply(SCHEMA_DEFAUT, NOM_TABLE_IHM_CALENDRIER));
		addTable(ID_TABLE_IHM_RULESETS, Namings.GET_TABLE_NAME.apply(SCHEMA_DEFAUT, NOM_TABLE_IHM_RULESET));
		addTable(ID_TABLE_IHM_CHARGEMENT_REGLE, Namings.GET_TABLE_NAME.apply(SCHEMA_DEFAUT, NOM_TABLE_IHM_CHARGEMENT_REGLE));
		addTable(ID_TABLE_IHM_NORMAGE_REGLE, Namings.GET_TABLE_NAME.apply(SCHEMA_DEFAUT, NOM_TABLE_IHM_NORMAGE_REGLE));
		addTable(ID_TABLE_IHM_CONTROLE_REGLE, Namings.GET_TABLE_NAME.apply(SCHEMA_DEFAUT, NOM_TABLE_IHM_CONTROLE_REGLE));
		addTable(ID_TABLE_IHM_FILTRAGE_REGLE, Namings.GET_TABLE_NAME.apply(SCHEMA_DEFAUT, NOM_TABLE_IHM_FILTRAGE_REGLE));
		addTable(ID_TABLE_IHM_MAPPING_REGLE, Namings.GET_TABLE_NAME.apply(SCHEMA_DEFAUT, NOM_TABLE_IHM_MAPPING_REGLE));
		addTable(ID_TABLE_IHM_MAPPING_REGLE_BIS, Namings.GET_TABLE_NAME.apply(SCHEMA_DEFAUT, NOM_TABLE_IHM_MAPPING_REGLE_BIS));
		addTable(ID_TABLE_IHM_MAPPING_SAV, Namings.GET_TABLE_NAME.apply(SCHEMA_DEFAUT, NOM_TABLE_IHM_MAPPING_SAV));
		addTable(ID_TABLE_IHM_EXPRESSION, Namings.GET_TABLE_NAME.apply(SCHEMA_DEFAUT, NOM_TABLE_IHM_EXPRESSION));
		addTable(ID_TABLE_IHM_CLIENT, Namings.GET_TABLE_NAME.apply(SCHEMA_DEFAUT, NOM_TABLE_IHM_CLIENT));
		addTable(ID_TABLE_IHM_ENTREPOT, Namings.GET_TABLE_NAME.apply(SCHEMA_DEFAUT, NOM_TABLE_IHM_ENTREPOT));
		addTable(ID_TABLE_IHM_FAMILLE, Namings.GET_TABLE_NAME.apply(SCHEMA_DEFAUT, NOM_TABLE_IHM_FAMILLE));
		addTable(ID_TABLE_IHM_MOD_TABLE_METIER, Namings.GET_TABLE_NAME.apply(SCHEMA_DEFAUT, NOM_TABLE_IHM_MOD_TABLE_METIER));
		addTable(ID_TABLE_IHM_NMCL, Namings.GET_TABLE_NAME.apply(SCHEMA_DEFAUT, NOM_TABLE_IHM_NMCL));
		addTable(ID_TABLE_IHM_PARAMETTRAGE_ORDRE_PHASE, Namings.GET_TABLE_NAME.apply(SCHEMA_DEFAUT, NOM_TABLE_IHM_PARAMETTRAGE_ORDRE_PHASE));
		addTable(ID_TABLE_IHM_SCHEMA_NMCL, Namings.GET_TABLE_NAME.apply(SCHEMA_DEFAUT, NOM_TABLE_IHM_SCHEMA_NMCL));
		addTable(ID_TABLE_IHM_SEUIL, Namings.GET_TABLE_NAME.apply(SCHEMA_DEFAUT, NOM_TABLE_IHM_SEUIL));
		addTable(ID_TABLE_PILOTAGE_BATCH, Namings.GET_TABLE_NAME.apply(SCHEMA_DEFAUT, NOM_TABLE_PILOTAGE_BATCH));

		
		/*
		 * Specific schema
		 */
		if (getSchema() != null) {
			addTable(ID_TABLE_PHASE_ORDER, Namings.GET_TABLE_NAME.apply(getSchema(), NOM_TABLE_ORDRE_PHASE));
			addTable(ID_TABLE_PILOTAGE_FICHIER, Namings.GET_TABLE_NAME.apply(getSchema(), NOM_TABLE_PILOTAGE_FICHIER));
			addTable(ID_TABLE_PILOTAGE_FICHIER_T, Namings.GET_TABLE_NAME.apply(getSchema(), NOM_TABLE_PILOTAGE_FICHIER_T));
			addTable(ID_TABLE_PILOTAGE_ARCHIVE, Namings.GET_TABLE_NAME.apply(getSchema(), NOM_TABLE_PILOTAGE_ARCHIVE));
			addTable(ID_TABLE_NORME_SPECIFIC, Namings.GET_TABLE_NAME.apply(getSchema(), NOM_TABLE_NORME));
			addTable(ID_TABLE_CALENDRIER_BAS, Namings.GET_TABLE_NAME.apply(getSchema(), NOM_TABLE_CALENDRIER));
			addTable(ID_TABLE_RULESETS_BAS, Namings.GET_TABLE_NAME.apply(getSchema(), NOM_TABLE_RULESET));
			addTable(ID_TABLE_CHARGEMENT_REGLE, Namings.GET_TABLE_NAME.apply(getSchema(), NOM_TABLE_CHARGEMENT_REGLE));
			addTable(ID_TABLE_NORMAGE_REGLE, Namings.GET_TABLE_NAME.apply(getSchema(), NOM_TABLE_NORMAGE_REGLE));
			addTable(ID_TABLE_CONTROLE_REGLE, Namings.GET_TABLE_NAME.apply(getSchema(), NOM_TABLE_CONTROLE_REGLE));
			addTable(ID_TABLE_FILTRAGE_REGLE, Namings.GET_TABLE_NAME.apply(getSchema(), NOM_TABLE_FILTRAGE_REGLE));
			addTable(ID_TABLE_MAPPING_REGLE, Namings.GET_TABLE_NAME.apply(getSchema(), NOM_TABLE_MAPPING_REGLE));
			addTable(ID_TABLE_EXPRESSION, Namings.GET_TABLE_NAME.apply(getSchema(), NOM_TABLE_EXPRESSION));
			addTable(ID_TABLE_SEUIL, Namings.GET_TABLE_NAME.apply(getSchema(), NOM_TABLE_SEUIL));
		}

	}

	public ContextName getContextName(String idTable) {
		return this.names.get(idTable);
	}

	public String getQualifedName(String idTable) {
		return getContextName(idTable).name();
	}

	public ContextName addTable(String idTable, ContextName tableName) {
		LoggerHelper.debug(LOGGER, String.format("Ajout de la table id = %s, name %s", idTable, tableName.toString()));
		return this.names.put(idTable, tableName);
	}

	public ContextName addTable(String idTable, String schema, String tableName) {
		return addTable(idTable, Namings.GET_TABLE_NAME.apply(schema, tableName));
	}

	public ContextName addTemporaryTable(String idTable, String tableName) {
		return addTable(idTable, Namings.GET_TABLE_NAME.apply(null, tableName));
	}

	/**
	 * @return the schema
	 */
	public final String getSchema() {
		return this.schema;
	}

	/**
	 * @param schema
	 *            the schema to set
	 */
	public final void setSchema(String schema) {
		this.schema = schema;
		initNomsTables();
	}

	public Naming getNaming(String idTable) {
		return getContextName(idTable).getNaming();
	}

	/**
	 * Exporte l'ensemble des noms de table (en tant que {@link String}) dans la
	 * session passée en argument.
	 *
	 * @param session
	 */
	public void export(Map<String, Object> session) {
		this.names.entrySet().forEach(t -> session.put(t.getKey(), t.getValue().name()));
	}

}
