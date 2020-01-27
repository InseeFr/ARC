package fr.insee.arc.core.service.mapping;

import java.sql.Connection;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import fr.insee.arc.core.dao.JeuDeRegleDao;
import fr.insee.arc.core.service.AbstractPhaseService;
import fr.insee.arc.core.service.AbstractThreadRunnerService;
import fr.insee.arc.core.service.ApiFiltrageService;
import fr.insee.arc.core.service.IApiServiceWithOutputTable;
import fr.insee.arc.core.service.thread.ThreadMappingService;
import fr.insee.arc.utils.utils.FormatSQL;

/**
 * Le mapping récupère les données filtrées (voir {@link ApiFiltrageService})
 * qui sont dans la table {@code <environnement>_filtrage_ok} et transforme les
 * variables administratives qu'elle contient en variables statistiques
 * réparties dans des tables métier de nom
 * {@code <environnement>_mapping_<application_cliente>}_ok}, en respectant un
 * modèle relationnel :<br/>
 * 1. Le modèle relationnel est dépendant de la famille (donc de l'application
 * cliente qui récupère le produit du mapping), et est stocké dans les tables
 * {@code <environnement>_mod_table_metier} et
 * {@code <environnement>_mod_variable_metier}<br/>
 * 2. Le modèle relationnel décrit dans ces tables décrit également un type de
 * consolidation qui impacte les règles de gestion du mapping.<br/>
 * 3. Les transformations des variables administratives en variables
 * statistiques sont décrites dans la table
 * {@code <environnement>_mapping_regle}.<br/>
 * Dans l'ordre, ce service :<br/>
 * 1. Instancie le contexte de travail (voir {@link AbstractPhaseService#initialize()} et
 * {@link #preparerExecution()}).<br/>
 * 2. Exécute les traitements :<br/>
 * 2.1. Liste les jeux de règles et itère sur eux.<br/>
 * 2.2. Construit la requête de mapping pour ce jeu de règles.<br/>
 * 2.3. Construit la liste de fichiers associés au jeu de règles.<br/>
 * 2.4. Stocke la requête de mapping pour ce jeu de règles et ce fichier dans un
 * buffer à requêtes {@link RequeteMappingCalibree}.<br/>
 * 3. Sauvegarde le contexte de travail (voir
 * {@link AbstractPhaseService#finalizePhase()})<br/>
 * Le buffer à requête exécute toutes les requêtes qu'il contient chaque fois
 * qu'un nombre total de caractères est dépassé.
 */

@Component
public class MappingService extends AbstractThreadRunnerService<ThreadMappingService>
	implements IApiServiceWithOutputTable {

    // maximum number of workers allocated to the service processing
    private static int MAX_PARALLEL_WORKERS=4;

    @SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(MappingService.class);
    private static final String prefixIdentifiantRubrique = "i_";

    protected RequeteMappingCalibree requeteSQLCalibree;

    protected JeuDeRegleDao jdrDAO;
    protected RegleMappingFactory regleMappingFactory;


    /**
     * Liste des colonnes jamais null par construction dans ARC.<br/>
     * Permet un pseudo test fonctionnel des règles de mapping sur le critère "ma
     * règle renvoie pas null".
     */
    public static final Set<String> colNeverNull = new HashSet<String>() {

	/**
	 *
	 */
	private static final long serialVersionUID = 6959692110781102988L;

	{
	    add("id");
	    add("id_source");
	}

    };

    public MappingService() {
	super();
    }
    /**
     * @param anParametersEnvironment
     *            inutile
     * @param anEnvironnementExecution
     * @param aDirectoryRoot
     *            inutile
     * @param aCurrentPhase
     * @param aNbEnr
     */
    public MappingService(String aCurrentPhase, String anParametersEnvironment, String anEnvironnementExecution,
	    String aDirectoryRoot, Integer aNbEnr, String... paramBatch) {
	super(ThreadMappingService.class, aCurrentPhase, anParametersEnvironment, anEnvironnementExecution, null, aNbEnr, paramBatch);

	this.nbThread = MAX_PARALLEL_WORKERS;
	
	this.requeteSQLCalibree = new RequeteMappingCalibree(this.connection, FormatSQL.TAILLE_MAXIMAL_BLOC_SQL,
		this.getTablePilTemp());
	this.jdrDAO = new JeuDeRegleDao();
    }

    public MappingService(Connection connexion, String aCurrentPhase, String anParametersEnvironment, String anEnvironnementExecution,
	    String aDirectoryRoot, Integer aNbEnr, String... paramBatch) {
	super(connexion,ThreadMappingService.class, aCurrentPhase, anParametersEnvironment, anEnvironnementExecution, null, aNbEnr, paramBatch);

	// fr.insee.arc.threads.mapping
	this.nbThread = 3;
	this.requeteSQLCalibree = new RequeteMappingCalibree(this.connection, FormatSQL.TAILLE_MAXIMAL_BLOC_SQL,
		this.getTablePilTemp());
	this.jdrDAO = new JeuDeRegleDao();
    }
    public static String getPrefixidentifiantrubrique() {
	return prefixIdentifiantRubrique;
    }

}
