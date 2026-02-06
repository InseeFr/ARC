package fr.insee.arc.core.service.p5mapping.dao;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.service.global.bo.JeuDeRegle;
import fr.insee.arc.core.service.p5mapping.bo.TableMapping;
import fr.insee.arc.core.service.p5mapping.bo.VariableMapping;
import fr.insee.arc.core.service.p5mapping.bo.rules.RegleMappingClePrimaire;
import fr.insee.arc.utils.dao.ModeRequete;
import fr.insee.arc.utils.dao.ModeRequeteImpl;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.database.Delimiters;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.format.Format;
import fr.insee.arc.utils.textUtils.IConstanteCaractere;
import fr.insee.arc.utils.textUtils.IConstanteNumerique;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.ManipString;

/**
 *
 * Objet contenant toutes les informations nécessaires à la génération d'une
 * requête SQL de mapping pour un jeu de règle. Cette requête est paramétrée par
 * le nom du fichier auquel elle s'applique.<br/>
 * Dépend de :<br/>
 * 1. Jeu de règles pour le choix du mapping.<br/>
 * 2. Famille de normes pour le modèle relationnel.<br/>
 * La génération de la requête se fait en plusieurs étapes :<br/>
 * 1. Construction du modèle relationnel (voir
 * {@link #construireTablesEtVariablesMapping()}).<br/>
 * 2. Attribution aux variables du modèle des règles non dérivées (syntaxe
 * pseudo-SQL qu'il faudra interpréter) (voir
 * {@link #attribuerExpressionRegleMapping(Map)}). 3. Dérivation des règles en
 * composition d'éléments terminaux. La requête textuelle SQL de mapping
 * s'obtient par l'invocation de la méthode {@link #getQueriesThatInsertDataIntoTemporaryModelTable(String)}. La
 * première invocation calcule la requête, les suivantes la restituent telle que
 * calculée la première fois.
 */
public class MappingQueries implements IConstanteCaractere, IConstanteNumerique {
	public static final String TOKEN_ID_SOURCE = "{:idSource}";
	public static final String ALIAS_TABLE = "{:alias}";
	public static final String FUNCTION_BEFORE = "{:functionBefore}";
	public static final String FUNCTION_AFTER = "{:functionAfter}";

	// MAJ : si toutes les regles de la table ne concernent que des variables non
	// renseignées (sauf regles sur les id_ et les fk_ )
	// alors on n'insere pas de ligne dans la table

	// fk_= clé etrangere
	// Contrairement aux champs commençant par "id_...", ce ne sont pas des clés
	// techniques intervenant dans la capitalisation
	// Les Clé étrangère peuvent donc être conservés mais comme pour les "id_",
	// on ignore leur contenu pour déterminer si on doit ajouter une ligne à une
	// table ou pas

	private static final String FOREIGN_KEY_PREFIX = "fk_";
	// identifiant lien direct
	private static final String ID_KEY_PREFIX = "id_";
	
	private static final String REGEXP_EXTRACT_RUBRIQUES_IN_EXPR = "\\{[^\\{\\} ]*\\}";

	private Set<TableMapping> ensembleTableMapping;
	private Set<VariableMapping> ensembleVariableMapping;
	private SortedSet<Integer> ensembleGroupes;

	private Map<TableMapping, Set<String>> ensembleRubriqueIdentifianteTable;
	/*
	 * LA REQUÊTE TEXTUELLE
	 */
	private String idFamille;
	private JeuDeRegle jeuDeRegle;
	private String environnement;
	private Connection connexion;
	/*
	 * LA FACTORY POUR LES REGLES DE MAPPING
	 */
	private MappingQueriesFactory regleMappingFactory;
	/*
	 * LES NOMS DES TABLES UTILES
	 */
	private String nomTableModVariableMetier;
	private String nomTableSource;
	private String nomTableRegleMapping;
	private String nomTableTemporairePrepUnion;
	private String nomTableTemporaireIdTable;
	private String tableLienIdentifiants;

	private static final String ID_TABLE = "id_table";
	
	Map<String, String> reglesIdentifiantes;
	
	Map<String, Set<String>> tableGroups;
	Map<String, Set<String>> tableNonGroup;

	private MappingQueries() {
		this.ensembleTableMapping = new HashSet<>();
		this.ensembleVariableMapping = new HashSet<>();
		this.ensembleGroupes = new TreeSet<>();
	}

	public MappingQueries(Connection aConnexion, MappingQueriesFactory aRegleMappingFactory, String anIdFamille,
			JeuDeRegle aJeuDeRegle, String anEnvironnement, String aNomTablePrecedente, int threadId) {
		this();
		this.regleMappingFactory = aRegleMappingFactory;
		/**
		 * La fabrique de règles a besoin de la liste des tables de mapping, qui
		 * dépendent de l'id_famille.<br/>
		 * Or, comme fait une requête par jeu de règles, on peut changer d'id_famille de
		 * temps en temps.
		 */
		this.idFamille = anIdFamille;
		this.regleMappingFactory.setEnsembleTableMapping(this.ensembleTableMapping);
		this.regleMappingFactory.setIdFamille(this.idFamille);
		this.connexion = aConnexion;
		this.jeuDeRegle = aJeuDeRegle;
		this.environnement = anEnvironnement;
		this.nomTableSource = aNomTablePrecedente;
		this.nomTableModVariableMetier = ViewEnum.MOD_VARIABLE_METIER.getFullName(this.environnement);
		this.nomTableRegleMapping = ViewEnum.MAPPING_REGLE.getFullName(this.environnement);
	}

	public void construire() throws ArcException {
		/*
		 * Récupération de l'ensemble des tables et variables de mapping, mapping
		 * objet-relationnel
		 */
		Map<String, VariableMapping> mapVariable = this.construireTablesEtVariablesMapping();
		/*
		 * Attribution de son expression à chaque variable
		 */
		this.attribuerExpressionRegleMapping(mapVariable);
		/*
		 *
		 */
		this.deriver();
	}

	/**
	 * Cette méthode récupère les expressions de règles de mapping, et les attribue
	 * aux variables qu'elles concernent
	 *
	 * @param mapVariable
	 * @throws ArcException
	 */
	private void attribuerExpressionRegleMapping(Map<String, VariableMapping> mapVariable) throws ArcException {
		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
		requete.append("SELECT DISTINCT variable_sortie as variable_sortie, expr_regle_col as expr_regle_col FROM ")
				.append(this.nomTableRegleMapping).append("\n WHERE ").append(this.jeuDeRegle.getSqlEquals())
				.append(";");

		List<List<String>> resultTemp = UtilitaireDao.get(0).executeRequest(this.connexion, requete);
		if (resultTemp.size() == 2) {
			throw new ArcException(ArcExceptionMessage.MAPPING_RULES_NOT_FOUND);
		}
		List<List<String>> result = new ArrayList<>();

		for (int i = 0; i < resultTemp.size(); i++) {
			// mise en minuscule des rubriques
			List<String> temp = new ArrayList<>();
			temp.add(resultTemp.get(i).get(0).toLowerCase());

			String exprCol = resultTemp.get(i).get(1);
			if (exprCol == null) {
				exprCol = "";
			}

			Matcher m = Pattern.compile(REGEXP_EXTRACT_RUBRIQUES_IN_EXPR).matcher(exprCol);

			StringBuffer sb = new StringBuffer();
			while (m.find()) {
				m.appendReplacement(sb, m.group().toLowerCase());
			}
			m.appendTail(sb);
			temp.add(sb.toString());
			result.add(temp);
		}

		for (int i = ARRAY_THIRD_COLUMN_INDEX; i < result.size(); i++) {

			if (mapVariable.get(result.get(i).get(ARRAY_FIRST_COLUMN_INDEX)) == null) {
				throw new ArcException(ArcExceptionMessage.MAPPING_RULES_NOT_FOUND,
						result.get(i).get(ARRAY_FIRST_COLUMN_INDEX));
			}

			mapVariable.get(result.get(i).get(ARRAY_FIRST_COLUMN_INDEX))
					.setExpressionRegle(result.get(i).get(ARRAY_SECOND_COLUMN_INDEX));
		}
	}

	/**
	 * 1. Dériver chacune des variables : après quoi l'expression SQL n'est qu'une
	 * concaténation de petits bouts<br/>
	 * 2. Premier passage dans TableMapping#construireEnsembleRubrique() :
	 * traitement de tous les ensembles rubriques sauf ceux liés aux clefs
	 * étrangères.<br/>
	 * 3. Second passage dans TableMapping#construireEnsembleRubrique() : traitement
	 * de tous les ensembles rubriques liés aux clefs étrangères.<br/>
	 *
	 * @throws ArcException
	 */
	private void deriver() throws ArcException {
		/*
		 * Dériver chacune des variables : après quoi l'expression SQL n'est qu'une
		 * concaténation de petits bouts
		 */
		for (VariableMapping variable : this.ensembleVariableMapping) {
			variable.deriver();

		}

		// ajout Manu : normalement faudrait recursif vers les feuilles
		construireListeRubriqueParTable();

		/*
		 * Premier passage dans TableMapping#construireEnsembleRubrique() : traitement
		 * de tous les ensembles rubriques sauf ceux liés aux clefs étrangères.
		 */
		for (TableMapping table : this.ensembleTableMapping) {
			table.construireEnsembleRubrique();
			this.ensembleGroupes.addAll(table.getEnsembleGroupes());
		}
		/*
		 * Second passage dans TableMapping#construireEnsembleRubrique() : traitement de
		 * tous les ensembles rubriques liés aux clefs étrangères.
		 */
		for (TableMapping table : this.ensembleTableMapping) {
			table.construireEnsembleRubrique();
			table.construireEnsembleVariablesTypes();
		}
	}

	/**
	 * y'a plus simple Seb ? je n'ai pas tout le temps envie de l'ensemble recursif
	 * dans certains cas. d'autant que la recursion vers le haut de l'arbre ne
	 * suffit pas : il la faut aussi dans l'autre sens. vers le haut : calcul de
	 * l'identifiant vers le bas : indique ligne null ou pas (comme une verification
	 * de contraintes cascade)
	 */
	private void construireListeRubriqueParTable() {
		this.ensembleRubriqueIdentifianteTable = new HashMap<>();

		for (TableMapping table : this.ensembleTableMapping) {

			Set<String> s = new HashSet<>();

			for (VariableMapping var : this.ensembleVariableMapping) {

				if (table.getEnsembleVariableMapping().contains(var) && !var.toString().startsWith(ID_KEY_PREFIX)
						&& !var.toString().startsWith(FOREIGN_KEY_PREFIX) || var.toString().equals(table.getPrimaryKey()))

				{
					s.addAll(var.getEnsembleIdentifiantsRubriques());
				}

			}
			this.ensembleRubriqueIdentifianteTable.put(table, s);
		}
	}

	/**
	 * Construit les objets de type {@link TableMapping} et {@link VariableMapping}
	 * en respectant les contraintes suivantes :<br/>
	 * 1. Une variable peut appartenir à plusieurs tables.<br/>
	 * 2. Une variable est traitée identiquement dans les différentes tables.<br/>
	 *
	 * @param construireAssociationEntreVariableEtTableMapping une
	 *                                                         &laquo;&nbsp;map&nbsp;&raquo;
	 *                                                         qui à chaque nom de
	 *                                                         variable associe la
	 *                                                         liste des tables qui
	 *                                                         ont cette variable.
	 */
	private Map<String, VariableMapping> construireTablesEtVariablesMapping() throws ArcException {
		Map<String, VariableMapping> mapVariable = new HashMap<>();
		Map<String, TableMapping> mapTable = new HashMap<>();

		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
		requete.append("SELECT DISTINCT nom_variable_metier, nom_table_metier, type_variable_metier FROM ");
		requete.append(this.nomTableModVariableMetier);
		requete.append("\n WHERE id_famille = " + requete.quoteText(this.idFamille) + ";");

		List<List<String>> result = UtilitaireDao.get(0).executeRequest(this.connexion, requete);

		for (int i = ARRAY_THIRD_COLUMN_INDEX; i < result.size(); i++) {
			/*
			 * Mise à jour de mes variables
			 */
			if (!mapVariable.containsKey(result.get(i).get(ARRAY_FIRST_COLUMN_INDEX))) {
				mapVariable.put(result.get(i).get(ARRAY_FIRST_COLUMN_INDEX),
						new VariableMapping(this.regleMappingFactory, result.get(i).get(ARRAY_FIRST_COLUMN_INDEX),
								result.get(i).get(ARRAY_THIRD_COLUMN_INDEX)));
			}
			/*
			 * Mise à jour de mes tables
			 */
			if (!mapTable.containsKey(result.get(i).get(ARRAY_SECOND_COLUMN_INDEX))) {
				mapTable.put(result.get(i).get(ARRAY_SECOND_COLUMN_INDEX), new TableMapping(this.environnement,
						result.get(i).get(ARRAY_SECOND_COLUMN_INDEX)));
			}
			/*
			 * Ajout de la variable à la table
			 */
			mapTable.get(result.get(i).get(ARRAY_SECOND_COLUMN_INDEX))
					.ajouterVariable(mapVariable.get(result.get(i).get(ARRAY_FIRST_COLUMN_INDEX)));
			/*
			 * Ajout de la table à la variable
			 */
			mapVariable.get(result.get(i).get(ARRAY_FIRST_COLUMN_INDEX))
					.ajouterTable(mapTable.get(result.get(i).get(ARRAY_SECOND_COLUMN_INDEX)));
		}

		this.ensembleTableMapping.addAll(mapTable.values());
		this.ensembleVariableMapping.addAll(mapVariable.values());

		return mapVariable;
	}

	
	/**
	 * Calcul les identifiants de chaque table du modèle en les mettant en correspondance
	 * @param aNomFichier
	 * @return
	 * @throws ArcException
	 */
	public String construireTableLienIdentifiants() throws ArcException {
		StringBuilder requeteGlobale = new StringBuilder();

		this.reglesIdentifiantes = new HashMap<>();
		Map<String, String> nomsVariablesGroupe = new HashMap<>();
		Map<String, String> linkedIds = new HashMap<>();

		construireListeIdentifiants(this.reglesIdentifiantes, nomsVariablesGroupe, linkedIds);
		
		calculerLesIndicatricesDeGroupeDeChaqueTable();
		
		construireTableIdentifiantsFichierCourant(requeteGlobale, this.reglesIdentifiantes, nomsVariablesGroupe,
				linkedIds);
		
		return requeteGlobale.toString();
		
	}
	
	/**
	 *
	 * @param aNomFichier
	 * @return a map with the model table as key and the query that inserts data as value
	 * @throws ArcException
	 */
	public Map<TableMapping,StringBuilder> getQueriesThatInsertDataIntoTemporaryModelTable(String aNomFichier) throws ArcException {

			Map<TableMapping,StringBuilder> queryByTable = new HashMap<>();

			for (TableMapping table : this.ensembleTableMapping) {
				
				StringBuilder requeteGlobale = new StringBuilder();
				requeteGlobale.append(ModeRequete.NESTLOOP_OFF);
				
				this.nomTableTemporairePrepUnion = "prep_union";
				this.nomTableTemporaireIdTable = "table_id";

				creerTablePrepUnion(requeteGlobale, table);
				insererTablePrepUnion(requeteGlobale, table, reglesIdentifiantes);
				
				// compute sql expression
				table.prepareSQLVariablesArrayAgg();
				
				calculerRequeteArrayAggGroup(table.getNomTableTemporaire(), requeteGlobale, table);
				requeteGlobale.append(FormatSQL.dropTable(this.nomTableTemporairePrepUnion, this.nomTableTemporaireIdTable));
				requeteGlobale.append(ModeRequete.NESTLOOP_ON);

				queryByTable.put(table, requeteGlobale);
			}


			return queryByTable;
			
	}

	/**
	 * Delete empty records from model tables
	 * @param aNomFichier
	 * @return
	 * @throws ArcException
	 */
	public String deleteEmptyRecords(String aNomFichier) throws ArcException {
		
		// tables must be sorted from children to parent in order to delete records in the right order
		Map<TableMapping, List<TableMapping>> tablesFilles = ordonnerTraitementTable();

		StringBuilder requeteGlobale = new StringBuilder();
		
		for (TableMapping table : this.ensembleTableMapping) {
		
			deleteEmptyRecords(table.getNomTableTemporaire(), requeteGlobale, table, tablesFilles);

		}
		requeteGlobale.append(FormatSQL.dropTable(this.tableLienIdentifiants));

		return requeteGlobale.toString().replace(TOKEN_ID_SOURCE, aNomFichier);
		
	}
	
	
	/**
	 *
	 * @param returned
	 * @param table
	 * @param nomsVariablesIdentifiantes
	 * @param reglesIdentifiantes
	 * @return La requête {@code returned}, augmentée de la requête d'insertion dans
	 *         la table {@code prep_union}.
	 * @throws ArcException
	 */
	private StringBuilder insererTablePrepUnion(StringBuilder returned, TableMapping table,
			Map<String, String> reglesIdentifiantes) throws ArcException {

		String requete = "";
		String requeteSav = "";

		/*
		 * Lorsqu'aucun groupe n'est défini pour aucune table, on attribue par défaut le
		 * groupe 1 à toute variable de toute table.
		 */
		if (this.ensembleGroupes.isEmpty()) {
			this.ensembleGroupes.add(TableMapping.GROUPE_UN);
		}

		for (Integer groupe : this.ensembleGroupes) {

			StringBuilder req = new StringBuilder();
			insererTablePrepUnionAvecGroupe(req, table, groupe, reglesIdentifiantes);
			requete = req.toString();

			if (!requete.equals(requeteSav)) {
				requeteSav = requete;
				returned.append(requete);
			}
		}


		StringBuilder req = new StringBuilder();
		insererTablePrepUnionAvecGroupe(req, table, null, reglesIdentifiantes);
		requete = req.toString();

		if (!requete.equals(requeteSav)) {
			requeteSav = requete;
			returned.append(requete);
		}


		return returned;
	}

	private int computeTableNumber(Map<TableMapping, Integer> order,
			Map<TableMapping, List<TableMapping>> tableTree, TableMapping table) {

		if (order.get(table) != null) {
			return order.get(table);
		}

		// default case
		if (table.getEnsembleVariableClef().size() == 1 && order.get(table) == null) {
			order.put(table, 1);
			return 1;
		}

		int k = 0;
		for (TableMapping table2 : this.ensembleTableMapping) {

			// if a foreign key is found in the table
			if (!table.equals(table2) && table.getEnsembleVariableClefString().contains(table2.getPrimaryKey())) {

				// register the table as a child of the ancestor tables having a primary key
				// corresponding its foreign keys
				addTableInTreeHierarchy(tableTree, table2, table);

				// the index of the table is the maximum index of its ancestor table + 1
				// we compute the index of ancestor tables recursively
				int kTemp = computeTableNumber(order, tableTree, table2) + 1;
				if (kTemp > k) {
					k = kTemp;
				}
			}
		}

		order.put(table, k);
		return k;
	}

	/**
	 * Add a table as child of an ancestor table in the table model tree
	 * 
	 * @param tableTree
	 * @param tableAncestor
	 * @param tableChild
	 */
	private void addTableInTreeHierarchy(Map<TableMapping, List<TableMapping>> tableTree,
			TableMapping tableAncestor, TableMapping tableChild) {
		// create the tree entry if it doesn't exist
		if (tableTree.get(tableAncestor) == null) {
			tableTree.put(tableAncestor, new ArrayList<>());
		}

		// add the son table if not already set
		if (!tableTree.get(tableAncestor).contains(tableChild)) {
			tableTree.get(tableAncestor).add(tableChild);
		}
	}

	/**
	 * Ordonne le traitements des entités métier en partant des entités feuilles et
	 * en remontant l'arbre du modèle this recursive version works with multiple
	 * fathers or sons links
	 */
	public Map<TableMapping, List<TableMapping>> ordonnerTraitementTable() {
		// Correctif modèle métier à une seule table
		if (this.ensembleTableMapping.size() == 1) {
			return new HashMap<>();
		}

		// initialisation
		Map<TableMapping, Integer> order = new HashMap<>();
		Map<TableMapping, List<TableMapping>> tableTree = new HashMap<>();

		// compute the index of every table
		for (TableMapping table : this.ensembleTableMapping) {
			computeTableNumber(order, tableTree, table);
		}

		// set the table ordered list
		// the order list corresponds to the order of the query to build the table
		// counter intuitive but in our process, the leaf tables in the data model
		// depending from others tables are computed first whereas the root tables are
		// computed last
		// mainly because it is easier to respect model integrity this way as for
		// example, when useless records must be deleted
		this.ensembleTableMapping = buildTheOrderedTablesListForProcess(order);

		return tableTree;

	}

	/**
	 * Build a linked hash set containing the list of the tables ordered for the
	 * process
	 * 
	 * @param order : the order index computed for table
	 * @return
	 */
	private Set<TableMapping> buildTheOrderedTablesListForProcess(Map<TableMapping, Integer> order) {

		// get the maximum index of ordered tables
		int k = 0;
		for (TableMapping table : this.ensembleTableMapping) {
			if (order.get(table) > k) {
				k = order.get(table);
			}
		}

		// Put ordered tables into a linkedhashmap by descending order
		Set<TableMapping> r = new LinkedHashSet<>();
		for (Integer i = k; i > Integer.MIN_VALUE; i--) {
			boolean end = true;
			for (TableMapping table : this.ensembleTableMapping) {
				if (order.get(table).equals(i)) {
					r.add(table);
					end = false;
				}
			}
			if (end) {
				break;
			}
		}

		return r;
	}

	/**
	 * Dans le cas ou la table a des colonnes avec une expression du type {{1}{expr1}{2}{expr2}{3}{expr3}}
	 * Cela signifie que les colonnes de ce type sont des tableaux
	 * On appele cela un calcul de groupe et il y a dans notre cas 3 groupes
	 * Pour faire le calcul de ces tableaux
	 * On evalue les expr1 des colonnes tableaux sur toutes les lignes de la table pour lequels au moins une variable source invoquée dans les expr1 est non null
	 * Dans la table TMP_ID, on marque les lignes pour lesquelles au moins une variable source invoquée dans les expr1 est non null
	 * On fait pareil avec expr2 puis expr3
	 * On mémorise ainsi quelles sont les lignes de la table qui ont fait l'objet d'un calcul de groupe
	 * On doit enfin évaluer les lignes pour lequels les variables etaient null pour toutes les expressions
	 * C'est le mode non groupe
	 * Si une table n'a pas de colonne tableau, on est en mode non groupe et la requete est simple ca on cacule sur toutes les colonnes de la table
	 * Si la table a des colonnes tableaux du type {{1}{expr1}{2}{expr2}{3}{expr3}}, la table tmp_id nous permet de faire ce calcul non groupe sur le reliquat des lignes qui n'ont pas fait l'object d'un calcul de groupe
	 * @param returned
	 * @param table
	 * @param groupe le numéro du groupe
	 * @param nomsVariablesIdentifiantes
	 * @param reglesIdentifiantes
	 * @return la requête passée en paramètre, augmentée de la requête d'insertion
	 *         dans la table {@code prep_union}
	 *
	 * @param modeIdentifiantGroupe : true : identifiant group, false : identifiant
	 *                              non groupe
	 * @throws ArcException
	 */
	private StringBuilder insererTablePrepUnionAvecGroupe(StringBuilder returned, TableMapping table, Integer groupe,
			Map<String, String> reglesIdentifiantes) throws ArcException {

		boolean modeIdentifiantGroupe = (groupe!=null); 
		
		// mode groupe : l'identifiant n'est pas vide ?
		if (modeIdentifiantGroupe && tableGroups.get(idTableGroupe(table,groupe))==null) {
			return returned;
		}
		
		// mode non groupe : l'identifiant n'est pas vide ?
		if (!modeIdentifiantGroupe && tableNonGroup.get(idTableNGroupe(table))==null) {
			return returned;
		}
		
		// Si on est en mode non groupe et que la table n'a pas de calcul de groupe, c'est une requete simple
		// Dans ce cas, pas besoin de considérer tmp_id et le calcul de reliquat
		boolean complexQuery = true;
		if (!modeIdentifiantGroupe && tableGroups.get(idTableGroupe(table,TableMapping.GROUPE_UN))==null) {
			complexQuery = false;
		}
	
		
		StringBuilder blocCreate = new StringBuilder();
		
		/*
		 * selectionner les identifiants à garder
		 */
		if (complexQuery)
		{
			returned.append("\n DROP TABLE IF EXISTS TMP_ID CASCADE; ");
			returned.append("\n CREATE TEMPORARY TABLE TMP_ID " + FormatSQL.WITH_NO_VACUUM + " AS ( ");
			returned.append("\n SELECT * ");
	
			/*
			 * Dans le cas des groupes on garde tout; sinon on garde les identifiants qui
			 * n'ont pas déjà été traités avant La table nomTableTemporaireIdTable stocke
			 * ces identifiants
			 */
	
			returned.append("\n FROM ");
			if (modeIdentifiantGroupe) {
				returned.append(this.tableLienIdentifiants + " d ");
	
			} else {
				// table des lien non déjà trouvés
				blocCreate.append("DROP TABLE IF EXISTS tmp_lndt;").append(ModeRequete.NESTLOOP_ON).append("CREATE TEMPORARY TABLE tmp_lndt as SELECT * FROM " + this.tableLienIdentifiants + " a where not exists (select 1 from "
						+ this.nomTableTemporaireIdTable + " b where a.id_table=b.id_table); ").append(ModeRequete.NESTLOOP_OFF);
				returned.insert(0, blocCreate);
				returned.append("tmp_lndt d ");
			}
	
			returned.append("\n WHERE ");
	
			if (modeIdentifiantGroupe) {
				returned.append(idTableGroupe(table,groupe));
			} else {
				returned.append(idTableNGroupe(table));
			}
			returned.append("\n ); ");
	
			/*
			 * On ajoute les identifiants traités à la table nomTableTemporaireIdTable qui
			 * stocke les identifiants traités
			 */
			returned.append("\n ").append(ModeRequete.NESTLOOP_ON);
			returned.append("INSERT INTO " + this.nomTableTemporaireIdTable
					+ " SELECT id_table FROM TMP_ID a WHERE NOT EXISTS (SELECT 1 from " + this.nomTableTemporaireIdTable
					+ " b where a.id_table=b.id_table); ");
			returned.append(ModeRequete.NESTLOOP_OFF);
			/*
			 * Insert dans prep union : on fait notre calcul de mise au format
			 */
		}

		returned.append("\n set work_mem='" + ModeRequeteImpl.SORT_WORK_MEM + "';");
		returned.append("\n INSERT INTO " + this.nomTableTemporairePrepUnion + " ");
		returned.append("\n SELECT ");
		returned.append(" " + listeVariablesTypesPrepUnion(new StringBuilder(), table, "::", true) + " FROM (");

		// Attention à ce distinct : faut bien garder les identifiant de rémunération
		// intermédiaire du coup et pas le final
		// Finalement NON : Utilisation d'une variable de départage
		returned.append("\n SELECT DISTINCT " + table.expressionSQLPrepUnion(groupe, reglesIdentifiantes));
		returned.append("\n FROM ");
		returned.append("\n ").append(this.nomTableSource).append(" e ");
		
		if (complexQuery)
		{
			// prise en compte de la table qui a calculé le reliquat des lignes à évaluer
			returned.append(",").append("TMP_ID").append(" d ");
			returned.append("\n WHERE e.id=d.id_table ");
		} else
		{
			returned.append(",").append(this.tableLienIdentifiants).append(" d ");
			returned.append("\n WHERE e.id=d.id_table ");
			returned.append(" AND ").append(idTableNGroupe(table));
		}
		
		returned.append("\n ) a; ");
		returned.append("\n set work_mem='" + ModeRequeteImpl.PARALLEL_WORK_MEM + "';");

		returned.append("\n DROP TABLE IF EXISTS TMP_ID CASCADE; ");

		return returned;
	}

	private StringBuilder creerTablePrepUnion(StringBuilder returned, TableMapping aTable) {
		returned.append(newline);

		returned.append("\n DROP TABLE IF EXISTS " + this.nomTableTemporairePrepUnion + "  CASCADE; ");
		returned.append("\n CREATE TEMPORARY TABLE " + this.nomTableTemporairePrepUnion + " (");

		listeVariablesTypesPrepUnion(returned, aTable, space, true);
		returned.append(") " + FormatSQL.WITH_NO_VACUUM + ";");

		returned.append("\n DROP TABLE IF EXISTS " + this.nomTableTemporaireIdTable + "  CASCADE; ");
		returned.append("\n CREATE TEMPORARY TABLE " + this.nomTableTemporaireIdTable + " (");
		returned.append(ID_TABLE + " bigint ");
		returned.append(") " + FormatSQL.WITH_NO_VACUUM + ";");

		returned.append(
				"\n create unique index idx_" + ManipString.substringAfterFirst(this.nomTableTemporaireIdTable, ".")
						+ " on " + this.nomTableTemporaireIdTable + "(id_table);");
		return returned;
	}
	
	
	private String idTableGroupe(TableMapping table, Integer groupe)
	{
		return table.getNomTableCourt()+"_"+groupe+" ";
	}
	
	private String idTableNGroupe(TableMapping table)
	{
		return table.getNomTableCourt()+"_ng"+" ";
	}

	
	private void calculerLesIndicatricesDeGroupeDeChaqueTable()
	{
		tableGroups= new HashMap<>();
		tableNonGroup= new HashMap<>();
		
		/*
		 * Lorsqu'aucun groupe n'est défini pour aucune table, on attribue par défaut le
		 * groupe 1 à toute variable de toute table.
		 */
		if (this.ensembleGroupes.isEmpty()) {
			this.ensembleGroupes.add(TableMapping.GROUPE_UN);
		}
		
		for (TableMapping table : this.ensembleTableMapping) {

		for (Integer groupe : this.ensembleGroupes) {	
		
		// i_g
		Set<String> ensembleIdentifiantsGroupesRetenus = new HashSet<>(table.getEnsembleIdentifiantsRubriques(groupe));
		ensembleIdentifiantsGroupesRetenus.remove(ColumnEnum.ID_SOURCE.getColumnName());
		
		if (!ensembleIdentifiantsGroupesRetenus.isEmpty()) {
			tableGroups.put(idTableGroupe(table,groupe), ensembleIdentifiantsGroupesRetenus);
			}
		
		}
		
		Set<String> ensembleIdentifiantsNonGroupesRetenus = this.ensembleRubriqueIdentifianteTable.get(table);
			if (!ensembleIdentifiantsNonGroupesRetenus.isEmpty())
			{
				tableNonGroup.put(idTableNGroupe(table), ensembleIdentifiantsNonGroupesRetenus);
			}
		}
	}
	
	/**
	 * Ajoute à la liste des requêtes à exécuter la requête de création de la table
	 * des identifiants techniques et enregistrements de la table pour le fichier
	 * courant.
	 *
	 * @param returned
	 * @param nomsVariablesIdentifiantes
	 * @param reglesIdentifiantes
	 * @return
	 */
	private StringBuilder construireTableIdentifiantsFichierCourant(StringBuilder returned,
			Map<String, String> reglesIdentifiantes, Map<String, String> nomsVariablesGroupe,
			Map<String, String> linkedIds) {

		
		Set<String> alreadyAdded = new HashSet<>();
		returned.append("\n CREATE "+ (this.tableLienIdentifiants.contains(".")?SQL.UNLOGGED:SQL.TEMPORARY)+" TABLE " + tableLienIdentifiants + " " + FormatSQL.WITH_NO_VACUUM + " AS ");

		// bloc 1 : calcul de l'identifiant groupe et non groupe
		
		StringBuilder blocSelect = new StringBuilder();
		blocSelect.append("\n SELECT id_table ");
		for (String nomVariable : reglesIdentifiantes.keySet()) {

			if (nomsVariablesGroupe.get(nomVariable) == null) {
				blocSelect.append(",\n  min(id_table) over (partition by " + nomVariable + ") AS " + nomVariable);
			} else if (!alreadyAdded.contains(nomsVariablesGroupe.get(nomVariable))) {
				blocSelect.append(",\n  min(id_table) over (partition by "
						+ linkedIds.get(nomsVariablesGroupe.get(nomVariable)).substring(1) + ") AS "
						+ nomsVariablesGroupe.get(nomVariable));
				alreadyAdded.add(nomsVariablesGroupe.get(nomVariable));
			}

		}
		
		// add group and non group expression
		tableGroups.keySet().stream().forEach(k -> 	blocSelect.append(",\n ").append(k));
		tableNonGroup.keySet().stream().forEach(k -> blocSelect.append(",\n ").append(k));
		
		blocSelect.append("\n FROM ttt;");

		// bloc 2 : on met les variables non groupe et les variables subalternes
		// identifiantes de groupes
		alreadyAdded = new HashSet<>();
		StringBuilder blocWith=new StringBuilder(); 
		blocWith.append("\n WITH ttt as materialized ( SELECT id AS id_table");
		for (String nomVariable : reglesIdentifiantes.keySet()) {
			String expressionVariable = reglesIdentifiantes.get(nomVariable);
			if (nomsVariablesGroupe.get(nomVariable) == null) {
				blocWith.append("\n ," + expressionVariable + " AS " + nomVariable);
			} else if (!alreadyAdded
					.contains(linkedIds.get(nomsVariablesGroupe.get(nomVariable)).replaceAll(",id\\_[^,]+", ""))) {
				blocWith.append("\n "
						+ linkedIds.get(nomsVariablesGroupe.get(nomVariable)).replaceAll(",id\\_[^,]+", "") + " ");
				alreadyAdded.add(linkedIds.get(nomsVariablesGroupe.get(nomVariable)).replaceAll(",id\\_[^,]+", ""));
			}
		}
		
		tableGroups.keySet().stream().forEach(k -> 	blocWith.append("\n , ")
				.append(checkIsNotNull(tableGroups.get(k)))
				.append(" as ").append(k)
				);
		tableNonGroup.keySet().stream().forEach(k -> 	blocWith.append("\n ,")
				.append(checkIsNotNull(tableNonGroup.get(k)))
				.append(" as ").append(k)
				);
		
		blocWith.append("\n FROM " + this.nomTableSource + " ) ");

		returned.append(blocWith).append(blocSelect);
		
		return returned;
	}

	/**
	 * Parcourt la liste des variables identifiants métiers {@code <id_xxxx>} et,
	 * pour chacune d'elle, met à jour les paramètres de méthode selon les règles
	 * suivantes :<br/>
	 * 1. Si {@code <id_xxxx>} est un nom d'identifiant métier et {@code <i>} un
	 * numéro de groupe de la table {@code <xxxx>}, alors l'entrée
	 * {@code (<id_xxxx_i>, <expression_i>)} est présente dans
	 * {@code reglesIdentifiantes} avec :<br/>
	 * &nbsp;&nbsp;1.1. {@code id_xxxx_i} est le nom de l'identifiant métier,
	 * suffixé par un underscore puis par le numéro de groupe.<br/>
	 * &nbsp;&nbsp;1.2. {@code <expression_i>} est l'expression pour cet identifiant
	 * liée au groupe {@code <i>}.<br/>
	 * 2. Si {@code <id_xxxx>} est un nom d'identifiant métier et {@code <i>} est un
	 * numéro de groupe n'appartenant pas à à la table {@code <xxxx>}, alors
	 * l'entrée {@code (<id_xxxx_i>, <id_xxxx>)} est inscrite dans
	 * {@code nomsVariablesIdentifiantes}.<br/>
	 * 3. Si {@code <id_xxxx>} est un nom d'identifiant d'une table sans groupes,
	 * alors l'entrée {@code (<id_xxxx>, <expression>)} est présente dans
	 * {@code reglesIdentifiantes}, où {@code <expression>} est l'expression de la
	 * règle pour {@code <id_xxxx>}.
	 *
	 * @param nomsVariablesIdentifiantes
	 * @param reglesIdentifiantes
	 */
	private void construireListeIdentifiants(Map<String, String> reglesIdentifiantes,
			Map<String, String> nomsVariablesGroupe, Map<String, String> linkedIds) {

		Set<RegleMappingClePrimaire> reglesClefs = new HashSet<>();
		for (TableMapping table : this.ensembleTableMapping) {
			reglesClefs.addAll(table.getEnsembleRegleMappingClefPrimaire());
			linkedIds.put(table.getPrimaryKey(), table.getGroupIdentifier());
		}

		for (RegleMappingClePrimaire regle : reglesClefs) {
			for (Integer groupe : this.ensembleGroupes) {
				if (regle.getTableMappingIdentifiee().getEnsembleGroupes().contains(groupe)) {
					reglesIdentifiantes.put(
							nomIdentifiantSuffixeGroupe(regle.getVariableMapping().getNomVariable(), groupe),
							regle.getExpressionSQL(groupe));

					nomsVariablesGroupe.put(
							nomIdentifiantSuffixeGroupe(regle.getVariableMapping().getNomVariable(), groupe),
							regle.getVariableMapping().getNomVariable());

				} else {
					nomsVariablesGroupe.put(
							nomIdentifiantSuffixeGroupe(regle.getVariableMapping().getNomVariable(), groupe), null);

				}
			}
			if (regle.getTableMappingIdentifiee().getEnsembleGroupes().isEmpty()) {
				reglesIdentifiantes.put(regle.getVariableMapping().getNomVariable(), regle.getExpressionSQL());
			}
		}
	}

	private static final String nomIdentifiantSuffixeGroupe(String nomVariable, int groupe) {
		return new StringBuilder(nomVariable + Delimiters.SQL_TOKEN_DELIMITER + groupe).toString();
	}

	private static StringBuilder listeVariablesTypesPrepUnion(StringBuilder returned, TableMapping table,
			String separateur, boolean removeArrayTypeForGroupe) {
		table.sqlListeVariablesTypes(returned, separateur, removeArrayTypeForGroupe);
		return returned;
	}

	private StringBuilder calculerRequeteArrayAggGroup(String nomTableTemporaireFinale, StringBuilder returned, TableMapping table) {

		StringBuilder select = table.getSelect();
		StringBuilder groupBy = table.getGroupBy();
		StringBuilder where = table.getWhere();
		StringBuilder allVar = table.getAllVar();

	
		returned.append(table.requeteCreation());
		
		// insertion 1 : on groupe les tableaux
		// en retirant les lignes dans lesquels les cases "tableau" sont toutes nulles
		returned.append("\n INSERT INTO " + nomTableTemporaireFinale + " (" + allVar + ") ");
		returned.append(
				"\n SELECT " + apply(select, FUNCTION_BEFORE, "array_agg(", FUNCTION_AFTER, " order by a.ctid)") + " ");
		returned.append("\n FROM " + this.nomTableTemporairePrepUnion + " a ");
		returned.append("\n " + where + " ");
		returned.append("\n GROUP BY " + apply(groupBy, ALIAS_TABLE, "a") + ";");

		// insertion 2 (s'il y a un tableau)
		// on insère les lignes distinctes avec les cases tableau toutes nulles
		if (where.length() > 0) {
			returned.append("\n INSERT INTO " + nomTableTemporaireFinale + " (" + allVar + ") ");
			returned.append("\n SELECT " + apply(select, FUNCTION_BEFORE, "ARRAY[", FUNCTION_AFTER, "]") + " ");
			returned.append("\n FROM prep_union a ");
			returned.append("\n where not exists (select 1 from " + nomTableTemporaireFinale + " b where row("
					+ apply(groupBy, ALIAS_TABLE, "a") + ")::text=row(" + apply(groupBy, ALIAS_TABLE, "b")
					+ ")::text) ");
			returned.append("\n group by " + allVar + ";");
		}

		return returned;
	}
	
	/**
	 * On supprime les enregistrements tous vides (variable non cléf a null pour type normal et à {NULL} pour les types tableaux) 
	 * et elligible à la suppression, c'est à dire aucun enregistrement n'est trouvé dans les tables fille en correspondance 
	 * avec l'enregistrement à supprimer
	 * 
	 * @param nomTableTemporaireFinale
	 * @param returned
	 * @param table
	 * @param tablesFilles
	 * @return
	 */
	private StringBuilder deleteEmptyRecords(String nomTableTemporaireFinale, StringBuilder returned, TableMapping table, Map<TableMapping, List<TableMapping>> tablesFilles)
	{
		StringBuilder whereAllNonClefNull = table.getWhereAllNonClefNull();
		
		returned.append("\n DELETE FROM " + nomTableTemporaireFinale + " a ");
		returned.append("\n " + whereAllNonClefNull + " ");
		if (tablesFilles.get(table) != null) {
			for (TableMapping fille : tablesFilles.get(table)) {
				returned.append("\n AND NOT EXISTS (SELECT 1 FROM " + fille.getNomTableTemporaire()
						+ " b WHERE a." + table.getPrimaryKey() + "=b." + table.getPrimaryKey() + ") ");
			}
		}
		returned.append("\n ;");

		return returned;
	}

	private String apply(String s, String... keyval) {

		for (int i = 0; i < keyval.length; i = i + 2) {
			s = s.replace(keyval[i], keyval[i + 1]);
		}
		return s;
	}

	private String apply(StringBuilder s, String... keyval) {
		return apply(s.toString(), keyval);
	}

	/**
	 *
	 * @param ensembleIdentifiants
	 * @param nomVariableSortie
	 * @param exclus
	 * @return
	 */
	private static String checkIsNotNull(Set<String> ensembleIdentifiants) {
		StringBuilder requete = new StringBuilder();
		requete.append("ROW(" + Format.untokenize(ensembleIdentifiants, ",") + ")::text COLLATE \"C\" !='"
				+ FormatSQL.toNullRow(ensembleIdentifiants) + "'");
		return requete.toString();

	}

	/**
	 * @param ensembleTableMapping the ensembleTableMapping to set
	 */
	public void setEnsembleTableMapping(Set<TableMapping> ensembleTableMapping) {
		this.ensembleTableMapping = ensembleTableMapping;
	}

	/**
	 * @param ensembleVariableMapping the ensembleVariableMapping to set
	 */
	public void setEnsembleVariableMapping(Set<VariableMapping> ensembleVariableMapping) {
		this.ensembleVariableMapping = ensembleVariableMapping;
	}

	/**
	 * Transfer the data of the temporary model tables to real model tables
	 * @return
	 */
	public ArcPreparedStatementBuilder requeteTransfertVersTablesMetierDefinitives() {
		ArcPreparedStatementBuilder returned = new ArcPreparedStatementBuilder();
		for (TableMapping table : this.ensembleTableMapping) {
			returned.append(table.requeteTransfererVersTableFinale());
		}
		return returned;
	}

	public void setTableLienIdentifiants(String nomTableFichierCourant) {
		this.tableLienIdentifiants = nomTableFichierCourant;
	}

	public Set<TableMapping> getEnsembleTableMapping() {
		return ensembleTableMapping;
	}

}
