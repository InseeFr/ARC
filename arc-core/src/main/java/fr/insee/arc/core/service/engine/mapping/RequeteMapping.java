package fr.insee.arc.core.service.engine.mapping;

import java.sql.Connection;
import java.sql.SQLException;
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

import fr.insee.arc.core.model.IDbConstant;
import fr.insee.arc.core.model.JeuDeRegle;
import fr.insee.arc.core.service.ApiService;
import fr.insee.arc.core.service.engine.mapping.regles.RegleMappingClePrimaire;
import fr.insee.arc.utils.dao.PreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.format.Format;
import fr.insee.arc.utils.textUtils.IConstanteCaractere;
import fr.insee.arc.utils.textUtils.IConstanteNumerique;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.ManipString;

/**
 *
 * Objet contenant toutes les informations nécessaires à la génération d'une requête SQL de mapping pour un jeu de règle. Cette requête est
 * paramétrée par le nom du fichier auquel elle s'applique.<br/>
 * Dépend de :<br/>
 * 1. Jeu de règles pour le choix du mapping.<br/>
 * 2. Famille de normes pour le modèle relationnel.<br/>
 * La génération de la requête se fait en plusieurs étapes :<br/>
 * 1. Construction du modèle relationnel (voir {@link #construireTablesEtVariablesMapping()}).<br/>
 * 2. Attribution aux variables du modèle des règles non dérivées (syntaxe pseudo-SQL qu'il faudra interpréter) (voir
 * {@link #attribuerExpressionRegleMapping(Map)}). 3. Dérivation des règles en composition d'éléments terminaux. La requête textuelle SQL de
 * mapping s'obtient par l'invocation de la méthode {@link #getRequete(String)}. La première invocation calcule la requête, les suivantes la
 * restituent telle que calculée la première fois.
 */
public class RequeteMapping implements IDbConstant, IConstanteCaractere, IConstanteNumerique {
    public static final String varIdSource = "id_source";
    public static final String tokenIdSource = "{:idSource}";
    public static final String aliasTable = "{:alias}";
    public static final String functionBefore = "{:functionBefore}";
    public static final String functionAfter = "{:functionAfter}";

    
    
    // MAJ : si toutes les regles de la table ne concernent que des variables non renseignées (sauf regles sur les id_ et les  fk_ )
    // alors on n'insere pas de ligne dans la table
    
    // fk_= clé etrangere
    // Contrairement aux champs commençant par "id_...", ce ne sont pas des clés techniques intervenant dans la capitalisation
    // Les Clé étrangère peuvent donc être conservés mais comme pour les "id_",
    // on ignore leur contenu pour déterminer si on doit ajouter une ligne à une table ou pas
    
    private static final String foreignKeyPrefix = "fk_";
    // identifiant lien direct
    private static final String idKeyPrefix = "id_";
    
    private static final String SORT_WORK_MEM="128MB";
    
    private Set<TableMapping> ensembleTableMapping;
    private Set<VariableMapping> ensembleVariableMapping;
    private SortedSet<Integer> ensembleGroupes;
    
    private HashMap<TableMapping,HashSet<String>> ensembleRubriqueIdentifianteTable;
    /*
     * LA REQUÊTE TEXTUELLE
     */
    private String requeteTextuelleInsertion;
    private boolean isRequeteCalculee;
    private String idFamille;
    private JeuDeRegle jeuDeRegle;
    private String environnement;
    private Connection connexion;
    /*
     * LA FACTORY POUR LES REGLES DE MAPPING
     */
    private RegleMappingFactory regleMappingFactory;
    /*
     * LES NOMS DES TABLES UTILES
     */
    private String nomTableModVariableMetier;
    private String nomTablePrecedente;
    private String nomTableSource;
    private String nomTableRegleMapping;
    private String nomTableTemporairePrepUnion;
    private String nomTableTemporaireIdTable;
    private String nomTableTemporaireFinale;
    private String nomTableFichierCourant;

    // thread number is necesary to be sure that table name don't collide
    private int threadId;
    
    public static final Set<String> setIdSource = new HashSet<String>() {
        private static final long serialVersionUID = 9031984127160616029L;
        {
            this.add("id_source");
        }
    };

    private static final String ID_TABLE = "id_table";

    private RequeteMapping() {
        this.isRequeteCalculee = false;
        this.ensembleTableMapping = new HashSet<>();
        this.ensembleVariableMapping = new HashSet<>();
        this.ensembleGroupes = new TreeSet<>();
    }

    
    public RequeteMapping(Connection aConnexion, RegleMappingFactory aRegleMappingFactory, String anIdFamille,
            JeuDeRegle aJeuDeRegle, String anEnvironnement, String aNomTablePrecedente, int threadId) {
        this();
        this.regleMappingFactory = aRegleMappingFactory;
        /**
         * La fabrique de règles a besoin de la liste des tables de mapping, qui dépendent de l'id_famille.<br/>
         * Or, comme fait une requête par jeu de règles, on peut changer d'id_famille de temps en temps.
         */
        this.idFamille = anIdFamille;
        this.regleMappingFactory.setEnsembleTableMapping(this.ensembleTableMapping);
        this.regleMappingFactory.setIdFamille(this.idFamille);
        this.connexion = aConnexion;
        this.jeuDeRegle = aJeuDeRegle;
        this.environnement = anEnvironnement;
        this.nomTablePrecedente = aNomTablePrecedente;
        this.nomTableModVariableMetier = ApiService.dbEnv(this.environnement) + "mod_variable_metier";
        this.nomTableRegleMapping = ApiService.dbEnv(this.environnement) + "mapping_regle";
        this.threadId=threadId;
    }

    public void construire() throws Exception {
        /*
         * Récupération de l'ensemble des tables et variables de mapping, mapping objet-relationnel
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
     * Cette méthode récupère les expressions de règles de mapping, et les attribue aux variables qu'elles concernent
     *
     * @param mapVariable
     * @throws SQLException
     */
    private void attribuerExpressionRegleMapping(Map<String, VariableMapping> mapVariable) throws Exception {
        PreparedStatementBuilder requete = new PreparedStatementBuilder();
        requete
        	.append("SELECT DISTINCT variable_sortie as variable_sortie, expr_regle_col as expr_regle_col FROM ")
        	.append(this.nomTableRegleMapping)
        	.append("\n WHERE ")
        	.append(this.jeuDeRegle.getSqlEquals())
        	.append(";")
        ;
        
        List<List<String>> resultTemp = Format.patch(UtilitaireDao.get(poolName).executeRequest(this.connexion, requete));
        if (resultTemp.size() == 2) {
        	throw new Exception("No mapping rules found for this ruleset.");
        }
        ArrayList<ArrayList<String>> result= new ArrayList<>();
        

        for (int i = 0; i < resultTemp.size(); i++) {
            // mise en minuscule des rubriques
            ArrayList<String> temp=new ArrayList<>();
            temp.add(resultTemp.get(i).get(0).toLowerCase());
            
            String exprCol=resultTemp.get(i).get(1);
            if (exprCol==null)
            {
                exprCol="";
            }

            
            Matcher m = Pattern.compile("\\{[^\\{\\} ]*\\}").matcher(exprCol);
            
            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                m.appendReplacement(sb, m.group().toLowerCase());
            }
            m.appendTail(sb);
            temp.add(sb.toString());
            result.add(temp);
        }
        
        
        for (int i = ARRAY_THIRD_COLUMN_INDEX; i < result.size(); i++) {
            mapVariable.get(result.get(i).get(ARRAY_FIRST_COLUMN_INDEX)).setExpressionRegle(result.get(i)
                    .get(ARRAY_SECOND_COLUMN_INDEX));
        }
    }

    /**
     * 1. Dériver chacune des variables : après quoi l'expression SQL n'est qu'une concaténation de petits bouts<br/>
     * 2. Premier passage dans TableMapping#construireEnsembleRubrique() : traitement de tous les ensembles rubriques sauf ceux liés aux
     * clefs étrangères.<br/>
     * 3. Second passage dans TableMapping#construireEnsembleRubrique() : traitement de tous les ensembles rubriques liés aux clefs
     * étrangères.<br/>
     *
     * @throws Exception
     */
    private void deriver() throws Exception {
        /*
         * Dériver chacune des variables : après quoi l'expression SQL n'est qu'une concaténation de petits bouts
         */
        for (VariableMapping variable : this.ensembleVariableMapping) {
            variable.deriver();

        }
        
        // ajout Manu : normalement faudrait recursif vers les feuilles
        construireListeRubriqueParTable();
        
        /*
         * Premier passage dans TableMapping#construireEnsembleRubrique() : traitement de tous les ensembles rubriques sauf ceux liés aux
         * clefs étrangères.
         */
        for (TableMapping table : this.ensembleTableMapping) {
            table.construireEnsembleRubrique();
            this.ensembleGroupes.addAll(table.getEnsembleGroupes());
        }
        /*
         * Second passage dans TableMapping#construireEnsembleRubrique() : traitement de tous les ensembles rubriques liés aux clefs
         * étrangères.
         */
        for (TableMapping table : this.ensembleTableMapping) {
            table.construireEnsembleRubrique();
            table.construireEnsembleVariablesTypes();
        }
    }
    
    /**
     * y'a plus simple Seb ? je n'ai pas tout le temps envie de l'ensemble recursif dans certains cas.
     * d'autant que la recursion vers le haut de l'arbre ne suffit pas : il la faut aussi dans l'autre sens.
     * vers le haut : calcul de l'identifiant
     * vers le bas : indique ligne null ou pas (comme une verification de contraintes cascade)
     */
    private void construireListeRubriqueParTable()
    {
        this.ensembleRubriqueIdentifianteTable=new HashMap<>();

        for (TableMapping table : this.ensembleTableMapping) {

            HashSet<String> s=new HashSet<>();
            
            for (VariableMapping var : this.ensembleVariableMapping) {
                
                if (table.getEnsembleVariableMapping().contains(var) 
                        && !var.toString().startsWith(idKeyPrefix)
                        && !var.toString().startsWith(foreignKeyPrefix) 
                    ||  var.toString().equals(table.getPrimaryKey())
                    )

                {
                    s.addAll(var.getEnsembleIdentifiantsRubriques());
                }

            }
            this.ensembleRubriqueIdentifianteTable.put(table,s);
        }
    }
    
    

    /**
     * Construit les objets de type {@link TableMapping} et {@link VariableMapping} en respectant les contraintes suivantes :<br/>
     * 1. Une variable peut appartenir à plusieurs tables.<br/>
     * 2. Une variable est traitée identiquement dans les différentes tables.<br/>
     *
     * @param construireAssociationEntreVariableEtTableMapping
     *            une &laquo;&nbsp;map&nbsp;&raquo; qui à chaque nom de variable associe la liste des tables qui ont cette variable.
     */
    private Map<String, VariableMapping> construireTablesEtVariablesMapping() throws SQLException {
        Map<String, VariableMapping> mapVariable = new HashMap<>();
        Map<String, TableMapping> mapTable = new HashMap<>();
        
        PreparedStatementBuilder requete = new PreparedStatementBuilder();
        requete.append("SELECT DISTINCT nom_variable_metier, nom_table_metier, type_variable_metier FROM ");
        requete.append(this.nomTableModVariableMetier);
        requete.append("\n WHERE id_famille = " + requete.quoteText(this.idFamille) + ";");
        
        List<List<String>> result = Format.patch(UtilitaireDao.get(poolName).executeRequest(this.connexion, requete));
        
        for (int i = ARRAY_THIRD_COLUMN_INDEX; i < result.size(); i++) {
            /*
             * Mise à jour de mes variables
             */
            if (!mapVariable.containsKey(result.get(i).get(ARRAY_FIRST_COLUMN_INDEX))) {
                mapVariable.put(result.get(i).get(ARRAY_FIRST_COLUMN_INDEX), new VariableMapping(
                        this.regleMappingFactory, result.get(i).get(ARRAY_FIRST_COLUMN_INDEX), result.get(i)
                                .get(ARRAY_THIRD_COLUMN_INDEX)));
            }
            /*
             * Mise à jour de mes tables
             */
            if (!mapTable.containsKey(result.get(i).get(ARRAY_SECOND_COLUMN_INDEX))) {
                mapTable.put(result.get(i).get(ARRAY_SECOND_COLUMN_INDEX), new TableMapping(this.environnement, result
                        .get(i).get(ARRAY_SECOND_COLUMN_INDEX), this.threadId));
            }
            /*
             * Ajout de la variable à la table
             */
            mapTable.get(result.get(i).get(ARRAY_SECOND_COLUMN_INDEX)).ajouterVariable(mapVariable.get(result.get(i)
                    .get(ARRAY_FIRST_COLUMN_INDEX)));
            /*
             * Ajout de la table à la variable
             */
            mapVariable.get(result.get(i).get(ARRAY_FIRST_COLUMN_INDEX)).ajouterTable(mapTable.get(result.get(i)
                    .get(ARRAY_SECOND_COLUMN_INDEX)));
        }
                
        this.ensembleTableMapping.addAll(mapTable.values());
        this.ensembleVariableMapping.addAll(mapVariable.values());

        return mapVariable;
    }


	/**
	 *
	 * @param aNomFichier
	 * @return la requête de mapping pour le fichier {@code aNomFichier}
	 */
	public String getRequete(String aNomFichier) {

		if (!this.isRequeteCalculee) {
			StringBuilder requeteGlobale = new StringBuilder("");
//			if(discardTemp) {
//				requeteGlobale.append("DISCARD TEMP; ");
//			}
			requeteGlobale.append("SET ENABLE_HASHAGG=ON; SET ENABLE_BITMAPSCAN=OFF; \n");

			construireTablePrecedente(requeteGlobale);

			Map<String, String> nomsVariablesIdentifiantes = new HashMap<>();
			Map<String, String> reglesIdentifiantes = new HashMap<>();
			Map<String, String> nomsVariablesGroupe  = new HashMap<>();
			HashMap<String,String>  linkedIds  = new  HashMap<> ();

			construireListeIdentifiants(nomsVariablesIdentifiantes, reglesIdentifiantes, nomsVariablesGroupe, linkedIds);
			construireTableFiltrageCalculIdentifiantsFichierCourant(requeteGlobale, nomsVariablesIdentifiantes, reglesIdentifiantes, nomsVariablesGroupe, linkedIds);
			HashMap<TableMapping,ArrayList<TableMapping>> tablesFilles=ordonnerTraitementTable();
					
			for (TableMapping table : this.ensembleTableMapping) {
				this.nomTableTemporairePrepUnion="prep_union";
				this.nomTableTemporaireIdTable="table_id";
				this.nomTableTemporaireFinale="table_finale_"+table.getNomTableCourt();

				creerTablePrepUnion(requeteGlobale, table, nomsVariablesIdentifiantes, reglesIdentifiantes);
				insererTablePrepUnion(requeteGlobale, table, nomsVariablesIdentifiantes, reglesIdentifiantes);
				calculerRequeteArrayAggGroup(requeteGlobale, table, tablesFilles);
				calculerRequeteFinale(requeteGlobale, table);
				
				requeteGlobale.append(FormatSQL.dropTable(this.nomTableTemporairePrepUnion));
				requeteGlobale.append(FormatSQL.dropTable(this.nomTableTemporaireIdTable));
			}
			requeteGlobale.append(FormatSQL.dropTable(this.nomTableFichierCourant));

			
//			System.out.println(requeteGlobale);

			this.requeteTextuelleInsertion = requeteGlobale.toString();
			this.isRequeteCalculee = true;
		}
		return this.requeteTextuelleInsertion.replace(tokenIdSource, aNomFichier);
	}

	/**
	 *
	 * @param returned
	 * @param table
	 * @param nomsVariablesIdentifiantes
	 * @param reglesIdentifiantes
	 * @return La requête {@code returned}, augmentée de la requête d'insertion dans la table {@code prep_union}.
	 */
	private StringBuilder insererTablePrepUnion(StringBuilder returned, TableMapping table,
	        Map<String, String> nomsVariablesIdentifiantes, Map<String, String> reglesIdentifiantes) {

		String requete="";
		String requeteSav="";

		/*
		 * Lorsqu'aucun groupe n'est défini pour aucune table, on attribue par défaut le groupe 1 à toute variable de toute table.
		 */
		if (this.ensembleGroupes.size()==0)
		{
			this.ensembleGroupes.add(TableMapping.GROUPE_UN);
		}
		
		for (Integer groupe : this.ensembleGroupes) {

			StringBuilder req=new StringBuilder();
			insererTablePrepUnionAvecGroupe(req, table, groupe, nomsVariablesIdentifiantes, reglesIdentifiantes, true);
			requete=req.toString();

			if (!requete.equals(requeteSav))
			{
				requeteSav=requete;
				returned.append(requete);
			}
		}



		for (Integer groupe : this.ensembleGroupes) {

			StringBuilder req=new StringBuilder();
			insererTablePrepUnionAvecGroupe(req, table, groupe, nomsVariablesIdentifiantes, reglesIdentifiantes, false);
			requete=req.toString();

			if (!requete.equals(requeteSav))
			{
				requeteSav=requete;
				returned.append(requete);
			}

		}

		return returned;
	}
	
	
	
	public int computeTableNumber(HashMap<TableMapping,Integer> order, HashMap<TableMapping,ArrayList<TableMapping>> tableTree, TableMapping table)
	{
		
		if (order.get(table)!=null)
				{
				return order.get(table);
				}
		
		// default case
		if (table.getEnsembleVariableClef().size()==1 && order.get(table)==null)
		{
			order.put(table, 1);
			return 1;
		}

		int k=0;
		for (TableMapping table2 : this.ensembleTableMapping) {

			// if a foreign key is found in the table
				if (!table.equals(table2) && table.getEnsembleVariableClefString().contains(table2.getPrimaryKey()))
					{
					
						// register the table as a child of the ancestor tables having a primary key corresponding its foreign keys
						addTableInTreeHierarchy(tableTree,table2,table);
					
						// the index of the table is the maximum index of its ancestor table + 1
						// we compute the index of ancestor tables recursivly
						int kTemp=computeTableNumber(order,tableTree,table2)+1;
						if (kTemp>k)
						{
							k=kTemp;
						}
					}
			}		
		
		order.put(table, k);
		return k;
	}
	
	/**
	 * Add a table as child of an ancestor table in the table model tree
	 * @param tableTree
	 * @param tableAncestor
	 * @param tableChild
	 */
	public void addTableInTreeHierarchy(HashMap<TableMapping,ArrayList<TableMapping>> tableTree, TableMapping tableAncestor, TableMapping tableChild)
	{
		// create the tree entry if it doesn't exist
		if (tableTree.get(tableAncestor)==null)
		{
			tableTree.put(tableAncestor, new ArrayList<TableMapping>());
		}
		// add the son table if not already set
		if (!tableTree.get(tableAncestor).contains(tableChild))
		{
			tableTree.get(tableAncestor).add(tableChild);
		}	
	}
	
	/**
	 * Ordonne le traitements des entités métier en partant des entités feuilles et en remontant l'arbre du modèle
	 * this recursive version works with multiple fathers or sons links
	 */
	public HashMap<TableMapping,ArrayList<TableMapping>> ordonnerTraitementTable()
	{
		// Correctif modèle métier à une seule table
		if (this.ensembleTableMapping.size()==1)
		{
			return new HashMap<>();
		}

		// initialisation
		HashMap<TableMapping,Integer> order=new HashMap<>();
		HashMap<TableMapping,ArrayList<TableMapping>> tableTree=new HashMap<>();
		
		// compute the index of every table
		for (TableMapping table : this.ensembleTableMapping) {
			computeTableNumber(order,tableTree,table);
		}

		// set the table ordered list
		// the order list corresponds to the order of the query to build the table
		// counter intuitive but in our process, the leaf tables in the data model depending from others tables are computed first whereas the root tables are computed last
		// mainly because it is easier to respect model integrity this way as for example, when useless records must be deleted
		this.ensembleTableMapping=buildTheOrderedTablesListForProcess(order);
		
		return tableTree;
		
	}
	
	/**
	 * Build a linked hash set containing the list of the tables ordered for the process
	 * @param order : the order index computed for table
	 * @return
	 */
	public Set<TableMapping> buildTheOrderedTablesListForProcess(HashMap<TableMapping,Integer> order)
	{

		// get the maximum index of ordered tables 
		int k=0;
		for (TableMapping table : this.ensembleTableMapping) {
			if (order.get(table)>k)
			{
				k=order.get(table);
			}
		}
		
		// Put ordered tables into a linkedhashmap by descending order
		Set<TableMapping> r=new LinkedHashSet<>();
		for (Integer i=k;i>Integer.MIN_VALUE;i--)
		{
			boolean end=true;			
			for (TableMapping table : this.ensembleTableMapping) {
				if (order.get(table).equals(i))
				{
					r.add(table);
					end=false;
				}
			}
			if (end)
			{
				break;
			}
		}
		
		return r;
	}
	
	
	/**
	 *
	 * @param returned
	 *            la requête
	 * @param table
	 *            la table
	 * @param groupe
	 * @param nomsVariablesIdentifiantes
	 * @param reglesIdentifiantes
	 * @return la requête passée en paramètre, augmentée de la requête d'insertion dans la table {@code prep_union}
	 *
	 * @param modeIdentifiantGroupe : true : identifiant group, false : identifiant non groupe
	 */
	private StringBuilder insererTablePrepUnionAvecGroupe(StringBuilder returned, TableMapping table, Integer groupe,
	        Map<String, String> nomsVariablesIdentifiantes, Map<String, String> reglesIdentifiantes, boolean modeIdentifiantGroupe) {

		// i_g
		Set<String> ensembleIdentifiantsGroupesRetenus = new HashSet<>(table.getEnsembleIdentifiantsRubriques(groupe));
		ensembleIdentifiantsGroupesRetenus.removeAll(setIdSource);

		// v_g : a quoi cela sert ?
	//	Set<String> ensembleNomsRubriquesGroupes = new HashSet<String>(table.getEnsembleNomsRubriques(groupe));

		// on concatene i_g et v_g
		Set<String> keys = new HashSet<>();
		keys.addAll(ensembleIdentifiantsGroupesRetenus);
	//	keys.addAll(ensembleNomsRubriquesGroupes);


		//System.out.println(keys);


//		Set<String> ensembleIdentifiantsNonGroupesRetenus = new HashSet<>(table.getEnsembleIdentifiantsRubriques());
//		ensembleIdentifiantsNonGroupesRetenus.removeAll(setIdSource);

		Set<String> ensembleIdentifiantsNonGroupesRetenus=this.ensembleRubriqueIdentifianteTable.get(table);



		/*
		 * selectionner les identifiants à garder
		 */
		returned.append("\n DROP TABLE IF EXISTS TMP_ID CASCADE; ");
		returned.append("\n CREATE TEMPORARY TABLE TMP_ID "+FormatSQL.WITH_NO_VACUUM+" AS ( ");
		returned.append("\n SELECT "+ID_TABLE+" ");

		/*
		 * Dans le cas des groupes on garde tout; sinon on garde les identifiants qui n'ont pas déjà été traités avant
		 * La table nomTableTemporaireIdTable stocke ces identifiants
		 */

		returned.append("\n FROM ");
		if (modeIdentifiantGroupe)
		{
			returned.append(this.nomTableFichierCourant + " d ");

		}
		else
		{
			returned.append("(select * from " + this.nomTableFichierCourant + " a where not exists (select 1 from " + this.nomTableTemporaireIdTable+" b where a.id_table=b.id_table)) d " );
		}

		returned.append("," + this.nomTableSource + " e ");

		returned.append("\n WHERE d.id_table=e.id ");

		if (modeIdentifiantGroupe)
		{
			/*
			 * Est-ce que les identifiants de rubriques pour les règles de groupes ne sont pas tous vides ?
			 */
			if (!keys.isEmpty()) {
				returned.append("\n AND "+checkIsNotNull(keys));
			}
			else
			{
				returned.append("\n AND false ");
			}
		}
		else
		{
			// mode non groupe : on va insérer des que tout le monde est non null
			// aucune raison pour que les clés étrangères comptent...
			// les clés étrangeres servent pour avoir un identifiant unique mais pas pour savoir
			// si on met la ligne ou pas sinon bonjour les fausses lignes
			// L'idéal serait de vérifier aussi la nullité des rubriques des blocs filles de la tables (et pas des peres)
			// pour déclarer qu'on insere pas mais c'est plus dur

			if (!ensembleIdentifiantsNonGroupesRetenus.isEmpty()) {
				returned.append("\n AND "+ checkIsNotNull(ensembleIdentifiantsNonGroupesRetenus));
			}
			else
			{
				returned.append("\n AND false ");
			}
		}
		returned.append("\n ); ");

		/*
		 * On ajoute les identifiants traités à la table nomTableTemporaireIdTable qui stocke les identifiants traités
		 */
		returned.append("\n INSERT INTO " + this.nomTableTemporaireIdTable +" SELECT id_table FROM TMP_ID a WHERE NOT EXISTS (SELECT 1 from " + this.nomTableTemporaireIdTable+" b where a.id_table=b.id_table); ");


		/*
		 * selectionner les enregistrements à garder
		 */
		returned.append("\n DROP TABLE IF EXISTS TMP_DATA CASCADE; ");
		returned.append("\n CREATE TEMPORARY TABLE TMP_DATA "+FormatSQL.WITH_NO_VACUUM+" AS (SELECT g.* FROM fichier g, TMP_ID f where f.id_table=g.id_table); ");

		/*
		 * Insert dans prep union : on fait notre calcul de mise au format
		 */

		returned.append("\n set local work_mem='"+SORT_WORK_MEM+"';");
		returned.append("\n INSERT INTO " + this.nomTableTemporairePrepUnion + " ");
		returned.append("\n SELECT ");
//		returned.append(groupe+"::smallint "+NUMERO_GROUPE);
//		returned.append(", " + this.ensembleGroupes.size()+"::smallint "+NB_GROUPES);
		returned.append(" " + listeVariablesTypesPrepUnion(new StringBuilder(), table, "::" , true) + " FROM (");

		// Attention à ce distinct : faut bien garder les identifiant de rémunération intermédiaire du coup et pas le final
		// Finalement NON : Utilisation d'une variable de départage
		returned.append("\n SELECT DISTINCT "+table.expressionSQLPrepUnion(groupe, nomsVariablesIdentifiantes, reglesIdentifiantes));
		returned.append("\n FROM ");
		returned.append("\n TMP_DATA d ," + this.nomTableSource + " e ");
		returned.append("\n WHERE e.id=d.id_table ");
		returned.append("\n ) a; ");
		returned.append("\n set local work_mem='"+FormatSQL.PARALLEL_WORK_MEM+"';");

		returned.append("\n DROP TABLE IF EXISTS TMP_ID CASCADE; ");
		returned.append("\n DROP TABLE IF EXISTS TMP_DATA CASCADE; ");

		return returned;
	}

	private StringBuilder creerTablePrepUnion(StringBuilder returned, TableMapping aTable,
	        Map<String, String> nomsVariablesIdentifiantes, Map<String, String> reglesIdentifiantes) {
		returned.append(newline);


		returned.append("\n DROP TABLE IF EXISTS " + this.nomTableTemporairePrepUnion + "  CASCADE; ");
		returned.append("\n CREATE temporary TABLE " + this.nomTableTemporairePrepUnion + " (");

//		returned.append(NUMERO_GROUPE +  " smallint");
//		returned.append(", ");
//		returned.append(NB_GROUPES + " smallint");
//		returned.append(", ");

		listeVariablesTypesPrepUnion(returned, aTable, space, true);
		returned.append(") "+FormatSQL.WITH_NO_VACUUM+";");

		returned.append("\n DROP TABLE IF EXISTS " + this.nomTableTemporaireIdTable + "  CASCADE; ");
		returned.append("\n CREATE temporary TABLE " + this.nomTableTemporaireIdTable + " (");
		returned.append(ID_TABLE+" bigint ");
		returned.append(") "+FormatSQL.WITH_NO_VACUUM+";");

		returned.append("\n create unique index idx_"+ManipString.substringAfterFirst(this.nomTableTemporaireIdTable,".")+" on "+this.nomTableTemporaireIdTable+"(id_table);");
		return returned;
	}


	private StringBuilder construireTablePrecedente(StringBuilder returned)
	{
		this.nomTableSource="parallel_mapping";
		returned.append("DROP TABLE IF EXISTS "+nomTableSource+";");
		returned.append("\n CREATE TEMPORARY TABLE "+nomTableSource+" "+FormatSQL.WITH_NO_VACUUM+" AS ");
		returned.append("\n SELECT * from "+this.nomTablePrecedente+" where id_source='"+tokenIdSource+"' ; ");
		return returned;
	}

	/**
	 * f Ajoute à la liste des requêtes à exécuter la requête de création de la table des identifiants techniques et enregistrements de la
	 * table filtrage pour le fichier courant.
	 *
	 * @param returned
	 * @param nomsVariablesIdentifiantes
	 * @param reglesIdentifiantes
	 * @return
	 */
	private StringBuilder construireTableFiltrageCalculIdentifiantsFichierCourant(StringBuilder returned,
	        Map<String, String> nomsVariablesIdentifiantes, Map<String, String> reglesIdentifiantes, Map<String, String> nomsVariablesGroupe, HashMap<String,String>  linkedIds
	        ) {
		this.nomTableFichierCourant = "fichier";

//		System.out.println(nomsVariablesIdentifiantes);
//		System.out.println(reglesIdentifiantes);
//		System.out.println(nomsVariablesGroupe);
//		System.out.println(linkedIds);

//		StringBuilder requeteIndex=new StringBuilder();


		HashSet<String> alreadyAdded=new HashSet<>();


		returned.append("\n DROP TABLE IF EXISTS "+nomTableFichierCourant+" CASCADE;");
		returned.append("\n CREATE temporary TABLE " + nomTableFichierCourant + " "+FormatSQL.WITH_NO_VACUUM+" AS ");


		// bloc 1 : calcul de l'identifiant group et non groupe

		returned.append("\n SELECT id_table ");
		for (String nomVariable : reglesIdentifiantes.keySet()) {

			if (nomsVariablesGroupe.get(nomVariable)==null)
			{
				returned.append(",\n  min(id_table) over (partition by " + nomVariable + ") AS " + nomVariable);
			}
			else if (!alreadyAdded.contains(nomsVariablesGroupe.get(nomVariable)))
			{
				returned.append(",\n  min(id_table) over (partition by " + linkedIds.get(nomsVariablesGroupe.get(nomVariable)).substring(1) + ") AS " + nomsVariablesGroupe.get(nomVariable));
				alreadyAdded.add(nomsVariablesGroupe.get(nomVariable));
			}

		}

		// bloc 2 : on met les variables non groupe et les variables subalternes identifiantes de groupes
		alreadyAdded=new HashSet<>();
		returned.append("\n FROM ( SELECT id AS id_table");
		for (String nomVariable : reglesIdentifiantes.keySet()) {
			String expressionVariable = reglesIdentifiantes.get(nomVariable);
			if (nomsVariablesGroupe.get(nomVariable)==null)
			{
				returned.append("\n ," + expressionVariable + " AS " + nomVariable);
			}
			else if (!alreadyAdded.contains(linkedIds.get(nomsVariablesGroupe.get(nomVariable)).replaceAll(",id\\_[^,]+", "")))
			{
				returned.append("\n " + linkedIds.get(nomsVariablesGroupe.get(nomVariable)).replaceAll(",id\\_[^,]+", "") +" ");
				alreadyAdded.add(linkedIds.get(nomsVariablesGroupe.get(nomVariable)).replaceAll(",id\\_[^,]+", ""));
			}

		}
		returned.append("\n FROM " + this.nomTableSource + " ) ww ; ");
		
		return returned;
	}

	/**
	 * Parcourt la liste des variables identifiants métiers {@code <id_xxxx>} et, pour chacune d'elle, met à jour les paramètres de méthode
	 * selon les règles suivantes :<br/>
	 * 1. Si {@code <id_xxxx>} est un nom d'identifiant métier et {@code <i>} un numéro de groupe de la table {@code <xxxx>}, alors l'entrée
	 * {@code (<id_xxxx_i>, <expression_i>)} est présente dans {@code reglesIdentifiantes} avec :<br/>
	 * &nbsp;&nbsp;1.1. {@code id_xxxx_i} est le nom de l'identifiant métier, suffixé par un underscore puis par le numéro de groupe.<br/>
	 * &nbsp;&nbsp;1.2. {@code <expression_i>} est l'expression pour cet identifiant liée au groupe {@code <i>}.<br/>
	 * 2. Si {@code <id_xxxx>} est un nom d'identifiant métier et {@code <i>} est un numéro de groupe n'appartenant pas à à la table
	 * {@code <xxxx>}, alors l'entrée {@code (<id_xxxx_i>, <id_xxxx>)} est inscrite dans {@code nomsVariablesIdentifiantes}.<br/>
	 * 3. Si {@code <id_xxxx>} est un nom d'identifiant d'une table sans groupes, alors l'entrée {@code (<id_xxxx>, <expression>)} est
	 * présente dans {@code reglesIdentifiantes}, où {@code <expression>} est l'expression de la règle pour {@code <id_xxxx>}.
	 *
	 * @param nomsVariablesIdentifiantes
	 * @param reglesIdentifiantes
	 */
	private void construireListeIdentifiants(Map<String, String> nomsVariablesIdentifiantes,
	        Map<String, String> reglesIdentifiantes, Map<String, String> nomsVariablesGroupe, HashMap<String,String> linkedIds) {
		
		
		Set<RegleMappingClePrimaire> reglesClefs = new HashSet<>();
		for (TableMapping table : this.ensembleTableMapping) {
			reglesClefs.addAll(table.getEnsembleRegleMappingClefPrimaire());
			linkedIds.put(table.getPrimaryKey(),table.getGroupIdentifier());
		}
		
		for (RegleMappingClePrimaire regle : reglesClefs) {
			for (Integer groupe : this.ensembleGroupes) {
				if (regle.getTableMappingIdentifiee().getEnsembleGroupes().contains(groupe)) {
					reglesIdentifiantes
					        .put(nomIdentifiantSuffixeGroupe(regle.getVariableMapping().getNomVariable(), groupe), regle
					                .getExpressionSQL(groupe));

					nomsVariablesGroupe.put(
							nomIdentifiantSuffixeGroupe(regle.getVariableMapping().getNomVariable(), groupe)
							, regle.getVariableMapping().getNomVariable());

				} else {
					nomsVariablesIdentifiantes.put(nomIdentifiantSuffixeGroupe(regle.getVariableMapping()
					        .getNomVariable(), groupe), regle.getVariableMapping().getNomVariable());

					nomsVariablesGroupe.put(
							nomIdentifiantSuffixeGroupe(regle.getVariableMapping().getNomVariable(), groupe)
							, null);

				}
			}
			if (regle.getTableMappingIdentifiee().getEnsembleGroupes().isEmpty()) {
				reglesIdentifiantes.put(regle.getVariableMapping().getNomVariable(), regle.getExpressionSQL());
			}
		}
	}

	private static final String nomIdentifiantSuffixeGroupe(String nomVariable, int groupe) {
		return new StringBuilder(nomVariable + underscore + groupe).toString();
	}

	/**
	 *
	 * @return {@code true} si et seulement si l'ensemble des groupes est non vide.
	 */
	private boolean isRequeteAGroupes() {
		return !this.ensembleGroupes.isEmpty();
	}


	/**
	 * 
	 * @param returned
	 * @param table
	 * @return
	 */
	private StringBuilder calculerRequeteFinale(StringBuilder returned, TableMapping table) {

		
		returned.append("\n INSERT INTO " + table.getNomTableTemporaire() + " (" + sqlListeVariablesOrdonnee(table)
		        + ")");
		returned.append("\n SELECT " + sqlListeVariablesOrdonnee(table));
		returned.append("\n FROM "+this.nomTableTemporaireFinale+" ");
		returned.append(";\n");
		return returned;
	}
	
	/**
	 *
	 * @param returned
	 * @param table
	 * @return
	 */
	private static StringBuilder calculerRequeteFinale(StringBuilder returned, TableMapping table, StringBuilder requeteArrayAggGroup) {
		returned.append("\n INSERT INTO " + table.getNomTableTemporaire() + " (" + sqlListeVariablesOrdonnee(table)
		        + ")");
		returned.append("\n SELECT " + sqlListeVariablesOrdonnee(table));
		returned.append("\n FROM ");
		returned.append(requeteArrayAggGroup);
		returned.append(";\n");
		return returned;
	}

	/**
	 *
	 * @param table
	 * @return la liste des variables de la table, énumérées dans un ordre immuable
	 */
	private static final StringBuilder sqlListeVariablesOrdonnee(TableMapping table) {
		return new StringBuilder(Format.untokenize(table.getEnsembleVariableClef(), ", "))//
		        .append(table.getEnsembleVariableClef().isEmpty() ? empty : ", ")//
		        .append(Format.untokenize(table.getEnsembleVariableNonClef(), ", "));
	}

	private static StringBuilder listeVariablesPrepUnion(StringBuilder returned, TableMapping table) {
		table.sqlListeVariables(returned);
		return returned;
	}

	private static StringBuilder listeVariablesTypesPrepUnion(StringBuilder returned, TableMapping table,
	        String separateur, boolean removeArrayTypeForGroupe) {
		table.sqlListeVariablesTypes(returned, separateur, removeArrayTypeForGroupe);
		return returned;
	}

	private StringBuilder calculerRequeteArrayAggGroup(StringBuilder returned, TableMapping table, HashMap<TableMapping,ArrayList<TableMapping>> tablesFilles) {

		StringBuilder select=new StringBuilder();
		StringBuilder groupBy=new StringBuilder();
		StringBuilder where=new StringBuilder();
		StringBuilder whereAllNonClefNull=new StringBuilder();
		StringBuilder allVar=new StringBuilder();

		table.sqlListeVariablesArrayAgg(select, groupBy, where, whereAllNonClefNull, allVar, this.nomTableTemporairePrepUnion);


		returned.append("\n DROP TABLE IF EXISTS "+this.nomTableTemporaireFinale+" ; ");

		returned.append("\n CREATE TEMPORARY TABLE "+this.nomTableTemporaireFinale+" "+FormatSQL.WITH_NO_VACUUM+" AS ");
		returned.append("\n SELECT * from " + table.getNomTableTemporaire() + " where false; ");
		
		// insertion 1 : on groupe les tableaux 
		// en retirant les lignes dans lesquels les cases "tableau" sont toutes nulles
		returned.append("\n INSERT INTO "+this.nomTableTemporaireFinale+" ("+allVar+") ");
		returned.append("\n SELECT "+
				apply(select 
						, functionBefore, "array_agg("
						, functionAfter," order by a.ctid)"
						)+" ");
		returned.append("\n FROM " + this.nomTableTemporairePrepUnion + " a ");
		returned.append("\n "+where+" ");
		returned.append("\n GROUP BY "+apply(groupBy,aliasTable, "a")+";");

		// insertion 2 (s'il y a un tableau) 
		// on insère les lignes distinctes avec les cases tableau toutes nulles
		if (where.length()>0)
		{
			returned.append("\n INSERT INTO "+this.nomTableTemporaireFinale+" ("+allVar+") ");
			returned.append("\n SELECT "+
					apply(select 
							, functionBefore, "ARRAY["
							, functionAfter,"]"
							)+" ");
			returned.append("\n FROM prep_union a ");
			returned.append("\n where not exists (select 1 from "+this.nomTableTemporaireFinale+" b where row("+apply(groupBy,aliasTable, "a")+")::text=row("+apply(groupBy,aliasTable, "b")+")::text) ");
			returned.append("\n group by "+allVar+";");
		}
		
		
		
		/* On supprime les enregistrements tout vide (variable non cléf a null pour type normal et à {NULL} pour les types tableaux) et elligible à la suppression (pas de lien vers table fille dans le modèle) */
		returned.append("\n DELETE FROM "+this.nomTableTemporaireFinale+" a ");
		returned.append("\n "+whereAllNonClefNull+" ");
		if (tablesFilles.get(table)!=null)
		{
			for (TableMapping fille: tablesFilles.get(table))
			{
				returned.append("\n AND NOT EXISTS (SELECT 1 FROM table_finale_"+fille.getNomTableCourt()+" b WHERE a."+table.getPrimaryKey()+"=b."+table.getPrimaryKey()+") ");
			}
		}
		returned.append("\n ;");
		
		

		return returned;
	}

	
	private String apply (String s, String... keyval)
	{
		
		for (int i=0;i<keyval.length;i=i+2)
		{
			s=s.replace(keyval[i], keyval[i+1]);			
		}
		return s;
	}
	
	private String apply (StringBuilder s, String... keyval)
	{
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
        StringBuilder requete=new StringBuilder();
        requete.append("ROW("+Format.untokenize(ensembleIdentifiants, ",")+")::text COLLATE \"C\" !='"+FormatSQL.toNullRow(ensembleIdentifiants)+"'");
        return requete.toString();
                
    }

    /**
     * @param ensembleTableMapping
     *            the ensembleTableMapping to set
     */
    public void setEnsembleTableMapping(Set<TableMapping> ensembleTableMapping) {
        this.ensembleTableMapping = ensembleTableMapping;
    }

    /**
     * @param ensembleVariableMapping
     *            the ensembleVariableMapping to set
     */
    public void setEnsembleVariableMapping(Set<VariableMapping> ensembleVariableMapping) {
        this.ensembleVariableMapping = ensembleVariableMapping;
    }

    /**
     *
     * @return la requête de destruction puis création des tables de mapping temporaires
     */
    public String requeteCreationTablesTemporaires() {
        StringBuilder returned = new StringBuilder();
        boolean isFirstTable = true;
        for (TableMapping table : this.ensembleTableMapping) {
            String requeteTable = table.requeteCreation();
            if (isFirstTable) {
                isFirstTable = false;
            } else {
                returned.append("\n");
            }
            returned.append(requeteTable);
        }
        return returned.toString();
    }

    
//  public void showAllrubrique() {
//      StringBuilder returned = new StringBuilder();
//      boolean isFirstTable = true;
//      for (TableMapping table : this.ensembleTableMapping) {
//          System.out.println(table.getEnsembleNomsRubriques());
//      }
//  }
    
    
    
    public String requeteTransfertVersTablesMetierDefinitives() {
        StringBuilder returned = new StringBuilder();
        for (TableMapping table : this.ensembleTableMapping) {
            returned.append(table.requeteTransfererVersTableFinale());
        }
        return returned.toString();
    }

    public String[] tableauNomsTablesTemporaires() {
        List<String> returned = new ArrayList<>();
        for (TableMapping table : this.ensembleTableMapping) {
            returned.add(table.getNomTableTemporaire());
        }
        return returned.toArray(new String[0]);
    }


}
