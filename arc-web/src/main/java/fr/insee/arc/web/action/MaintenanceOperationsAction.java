package fr.insee.arc.web.action;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tools.ant.taskdefs.Delete;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;

import fr.insee.arc.core.model.TestLoggers;
import fr.insee.arc.utils.dao.PreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.web.model.DeleteRequestModel;
import fr.insee.arc.web.model.MaintenanceOperationsModel;
import fr.insee.arc.web.util.VObject;


@Controller
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class MaintenanceOperationsAction extends ArcAction<MaintenanceOperationsModel>  {

	private static final String RESULT_SUCCESS = "/jsp/maintenanceOperations.jsp";

	private static final Logger LOGGER = LogManager.getLogger(MaintenanceOperationsAction.class);

	private static final String IHM_CLIENTS = "ihmClients";
	private static final String IHM_ENVIRONMENTS = "environments";

    private static final String IHM_DELETE_REQUEST = "deleteRequest";
    
    private static final List<String> COL_PILOTAGE_FICHIER = Arrays.asList(
        "id_source", "id_norme", "validite", "periodicite", "phase_traitement", "etat_traitement", "date_traitement",
        "rapport", "taux_ko", "nb_enr", "nb_essais", "etape", "validite_inf", "validite_sup", "version", "date_entree",
        "container", "v_container", "o_container", "to_delete", "client", "date_client", "jointure",
        "generation_composite"
    );
    
    private static final List<String> COL_PILOTAGE_FICHIER_CLIENT_NULL = Arrays.asList(
        "id_source", "id_norme", "validite", "periodicite", "phase_traitement", "etat_traitement", "date_traitement",
        "rapport", "taux_ko", "nb_enr", "nb_essais", "etape", "validite_inf", "validite_sup", "version", "date_entree",
        "container", "v_container", "o_container", "to_delete", "NULL", "NULL", "jointure",
        "generation_composite"
    );
    
    private static final List<String> COL_PILOTAGE_FICHIER_UNNESTED = Arrays.asList(
        "id_source", "id_norme", "validite", "periodicite", "phase_traitement", "etat_traitement", "date_traitement",
        "rapport", "taux_ko", "nb_enr", "nb_essais", "etape", "validite_inf", "validite_sup", "version", "date_entree",
        "container", "v_container", "o_container", "to_delete", "unnest(client) AS client", "unnest(date_client) AS date_client", "jointure",
        "generation_composite"
    );
    
    private static final List<String> COL_PILOTAGE_FICHIER_AGG = Arrays.asList(
        "id_source", "max(id_norme) AS id_norme", "max(validite) AS validite", "max(periodicite) AS periodicite",
        "max(phase_traitement) AS phase_traitement", "max(etat_traitement) AS etat_traitement",
        "max(date_traitement) AS date_traitement", "max(rapport) AS rapport", "max(taux_ko) AS taux_ko",
        "max(nb_enr) AS nb_enr", "max(nb_essais) AS nb_essais", "max(etape) AS etape",
        "max(validite_inf) AS validite_inf", "max(validite_sup) AS validite_sup", "max(version) AS version",
        "max(date_entree) AS date_entree", "max(container) AS container", "max(v_container) AS v_container",
        "max(o_container) AS o_container", "max(to_delete) AS to_delete", "array_agg(client) AS client",
        "array_agg(date_client) AS date_client", "max(jointure) AS jointure",
        "max(generation_composite) AS generation_composite"
    );



    private VObject viewOperations;
    private List<String> ihmClients;
    private List<String> environments;

	@Override
	protected void putAllVObjects(MaintenanceOperationsModel arcModel) {
		setViewOperations(this.vObjectService.preInitialize(arcModel.getViewOperations()));
		putVObject(getViewOperations(), t -> initializeOperations());
		setIhmClients(UtilitaireDao.get("arc").getList(null, "SELECT DISTINCT id_application FROM arc.ihm_client", new ArrayList<>()));
		setEnvironments(UtilitaireDao.get("arc").getList(null, "SELECT DISTINCT schema_name FROM information_schema.schemata WHERE schema_name LIKE 'arc_%' ORDER BY schema_name ASC", new ArrayList<>()));
	}

	public void initializeOperations() {
        HashMap<String, String> defaultInputFields = new HashMap<>();
        this.vObjectService.initialize(viewOperations, new PreparedStatementBuilder("SELECT true"),  "arc.operations", defaultInputFields);
        
    }
	
	@Override
    public void extraModelAttributes(Model model) {
        model.addAttribute(IHM_DELETE_REQUEST, new DeleteRequestModel());
        model.addAttribute(IHM_CLIENTS, ihmClients);
        model.addAttribute(IHM_ENVIRONMENTS, environments);
    }
	
	private static final String ORIGIN="WEB GUI";

    @RequestMapping("/generateErrorMessageInLogsOperations")
    public String generateErrorMessageInLogsOperations(Model model) {
    	TestLoggers.sendLoggersTest(ORIGIN);
		return generateDisplay(model, RESULT_SUCCESS);
    }
    
    private static String fromTable(DeleteRequestModel deleteRequest) {
        return deleteRequest.getEnvironment() + ".pilotage_fichier";
    }
    
    private static String dropTable(String tableName) {
        StringBuilder returned = new StringBuilder();
        returned.append("\nDROP TABLE IF EXISTS " + tableName + ";");
        return returned.toString();
    }
    
    private static String createWorkTableDeleteLastImportRequestOperations(DeleteRequestModel deleteRequest, String fromTableName, String toTableName) {
        StringBuilder returned = new StringBuilder();
        returned.append("\nCREATE TEMPORARY TABLE IF NOT EXISTS " + toTableName + " AS");
        returned.append("\nSELECT " + COL_PILOTAGE_FICHIER_UNNESTED.stream().collect(Collectors.joining(", ")));
        returned.append("\nFROM " + fromTableName);
        returned.append("\nWHERE " + FormatSQL.textToSql(deleteRequest.getIhmClient()) + " = ANY(client)");
        returned.append("\n    AND phase_traitement = 'MAPPING'");
        returned.append("\n;");
        return returned.toString();
    }
    
    private static String createNewTableDeleteLastImportRequestOperations(DeleteRequestModel deleteRequest, String fromTableName, String toTableName) {
        StringBuilder returned = new StringBuilder();
        returned.append("\nCREATE TABLE IF NOT EXISTS " + toTableName + " AS");
        returned.append(
            "\nSELECT " + COL_PILOTAGE_FICHIER_AGG.stream().collect(Collectors.joining(", "))
        );
        returned.append("\nFROM (" );
        
        returned.append("\n    SELECT " + COL_PILOTAGE_FICHIER.stream().collect(Collectors.joining(", ")));
        returned.append("\n    FROM " + fromTableName);
        returned.append("\n    WHERE NOT (" + deleteCondition(deleteRequest) + ")");
        returned.append("\n    UNION ALL ");
        returned.append("\n    SELECT " + COL_PILOTAGE_FICHIER_CLIENT_NULL.stream().collect(Collectors.joining(", ")));
        returned.append("\n    FROM " + fromTableName);
        returned.append("\n    WHERE (" + deleteCondition(deleteRequest) + ")");
        returned.append("\n) foo");
        returned.append("\nGROUP BY id_source");
        returned.append("\n;");
        return returned.toString();
    }
    
    private static String deleteCondition(DeleteRequestModel deleteRequest) {
        return Arrays.asList(
            FormatSQL.textToSql(deleteRequest.getIhmClient()) + " = client",
            FormatSQL.textToSql(deleteRequest.getLowDate()) + "::date <= date_client",
            "date_client <= " + FormatSQL.textToSql(deleteRequest.getHighDate()) + "::date"
        ).stream().collect(Collectors.joining(" AND "));
    }
    
    private static String insertNotDeletedRequestOperations(
        DeleteRequestModel deleteRequest, String fromTable, String newPilotageFichierTableName
    ) {
        StringBuilder returned = new StringBuilder();
        returned.append(
            "\nINSERT INTO " + newPilotageFichierTableName + " ("
                + COL_PILOTAGE_FICHIER.stream().collect(Collectors.joining(", ")) + ")"
        );
        returned.append("\nSELECT " + COL_PILOTAGE_FICHIER.stream().collect(Collectors.joining(", ")));
        returned.append("\nFROM " + fromTable);
        returned.append("\nWHERE " + FormatSQL.textToSql(deleteRequest.getIhmClient()) + " != ANY(client)");
        returned.append("\n    OR phase_traitement != 'MAPPING'");
        returned.append("\n;");
        return returned.toString();
    }

    private static String renameTable(String oldTableName, String newTableName) {
        StringBuilder returned = new StringBuilder();
        returned.append("\nALTER TABLE IF EXISTS " + oldTableName);
        returned.append("\nRENAME TO " + newTableName);
        returned.append("\n;");
        return returned.toString();
    }

    @RequestMapping("/deleteLastImportRequestOperations")
    public String deleteLastImportRequestOperations(@ModelAttribute("deleteRequest") DeleteRequestModel deleteRequete, Model model) {
        StringBuilder query = new StringBuilder();
        String temporaryTableName = FormatSQL.temporaryTableName("pilotage_fichier");
        query.append(dropTable(temporaryTableName));
        query.append(
            createWorkTableDeleteLastImportRequestOperations(
                deleteRequete, fromTable(deleteRequete), temporaryTableName
            )
        );
        String newPilotageFichierTableName = FormatSQL.temporaryTableName(fromTable(deleteRequete));
        query.append(dropTable(newPilotageFichierTableName));
        query.append(
            createNewTableDeleteLastImportRequestOperations(
                deleteRequete, temporaryTableName, newPilotageFichierTableName
            )
        );
        query.append(insertNotDeletedRequestOperations(deleteRequete, fromTable(deleteRequete), newPilotageFichierTableName));
        query.append(dropTable(fromTable(deleteRequete)));
        query.append(renameTable(newPilotageFichierTableName, "pilotage_fichier"));
        try {
//            UtilitaireDao.get("arc").executeImmediate(null, query.toString());
            System.out.println(query.toString());
        } catch (RuntimeException e) {
            String message = "Erreur lors de la suppression des opérations pour l'application cliente "
                + deleteRequete.getIhmClient() + " entre le " + deleteRequete.getLowDate() + "(inclus) et le "
                + deleteRequete.getHighDate() + " (exclu)";
            this.viewOperations.setMessage(
                message+" :\n"+e.getMessage()
            );
            LoggerHelper.error(LOGGER, message, e);
        }        
        return generateDisplay(model, RESULT_SUCCESS);
    }

    @RequestMapping("/selectOperations")
    public String selectOperations(Model model) {

		return generateDisplay(model, RESULT_SUCCESS);
    }

    @RequestMapping("/addOperations")
    public String addOperations(Model model) {
        this.vObjectService.insert(viewOperations);
        return generateDisplay(model, RESULT_SUCCESS);
    }

    @RequestMapping("/deleteOperations")
    public String deleteOperations(Model model) {
         this.vObjectService.delete(viewOperations);
        return generateDisplay(model, RESULT_SUCCESS);
    }

    @RequestMapping("/updateOperations")
    public String updateOperations(Model model) {
        this.vObjectService.update(viewOperations);
        return generateDisplay(model, RESULT_SUCCESS);
    }

    @RequestMapping("/sortOperations")
    public String sortOperations(Model model) {
        this.vObjectService.sort(viewOperations);
        return generateDisplay(model, RESULT_SUCCESS);
    }

    @RequestMapping("/startOperations")
    public String startOperations(Model model) throws Exception {
          	
    	
        return generateDisplay(model, RESULT_SUCCESS);
    }

    public VObject getViewOperations() {
        return this.viewOperations;
    }

    public void setViewOperations(VObject viewOperations) {
        this.viewOperations = viewOperations;
    }

	@Override
	public String getActionName() {
		return "manageOperations";
	}

    
    /**
     * @return the ihmClients
     */
	@ModelAttribute("ihmClients")
    public List<String> getIhmClients() {
        return ihmClients;
    }

    
    /**
     * @param ihmClients the ihmClients to set
     */
    public void setIhmClients(List<String> ihmClients) {
        this.ihmClients = ihmClients;
    }

    
    /**
     * @return the environments
     */
    public List<String> getEnvironments() {
        return environments;
    }

    
    /**
     * @param environments the environments to set
     */
    public void setEnvironments(List<String> environments) {
        this.environments = environments;
    }

//    public VObject getViewIhmClient() {
//        return this.viewIhmClient;
//    }

//    public void setViewIhmClient(VObject viewIhmClient) {
//        this.viewIhmClient = viewIhmClient;
//    }

    
    
}