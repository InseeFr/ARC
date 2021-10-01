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

    private static final String COL_DATE_CLIENT = "date_client";
    private static final String COL_CLIENT = "client";
    private static final String COL_PHASE_TRAITEMENT = "phase_traitement";
    private static final String COL_ID_SOURCE = "id_source";

    private static final List<String> COL_PILOTAGE_FICHIER = Arrays
        .asList(COL_ID_SOURCE, COL_PHASE_TRAITEMENT, COL_CLIENT, COL_DATE_CLIENT);
    private static final List<String> COL_PILOTAGE_FICHIER_CLIENT_NULL = Arrays
        .asList(COL_ID_SOURCE, COL_PHASE_TRAITEMENT, "NULL", "NULL");
    private static final List<String> COL_PILOTAGE_FICHIER_UNNESTED = Arrays
        .asList(COL_ID_SOURCE, COL_PHASE_TRAITEMENT, "unnest(client) AS client", "unnest(date_client) AS date_client");
    private static final List<String> COL_PILOTAGE_FICHIER_AGG = Arrays.asList(
        COL_ID_SOURCE, "max(phase_traitement) AS phase_traitement", "array_remove(array_agg(client), NULL) AS client",
        "array_remove(array_agg(date_client), NULL) AS date_client"
    );

    private static final long NB_LINES_PER_PARTITION = 100_000L;

    private VObject viewOperations;
    private List<String> ihmClients;
    private List<String> environments;

    @Override
    protected void putAllVObjects(MaintenanceOperationsModel arcModel) {
        setViewOperations(this.vObjectService.preInitialize(arcModel.getViewOperations()));
        putVObject(getViewOperations(), t -> initializeOperations());
        setIhmClients(
            UtilitaireDao.get("arc")
                .getList(null, "SELECT DISTINCT id_application FROM arc.ihm_client", new ArrayList<>())
        );
        setEnvironments(
            UtilitaireDao.get("arc").getList(
                null,
                "SELECT DISTINCT schema_name FROM information_schema.schemata WHERE schema_name LIKE 'arc_%' ORDER BY schema_name ASC",
                new ArrayList<>()
            )
        );
    }

    public void initializeOperations() {
        HashMap<String, String> defaultInputFields = new HashMap<>();
        this.vObjectService.initialize(
            this.viewOperations, new PreparedStatementBuilder("SELECT true"), "arc.operations", defaultInputFields
        );
    }
	
	@Override
    public void extraModelAttributes(Model model) {
        model.addAttribute(IHM_DELETE_REQUEST, new DeleteRequestModel());
        model.addAttribute(IHM_CLIENTS, this.ihmClients);
        model.addAttribute(IHM_ENVIRONMENTS, this.environments);
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
        returned.append("\nCREATE UNLOGGED TABLE IF NOT EXISTS " + toTableName + " AS");
        returned.append("\nSELECT " + COL_PILOTAGE_FICHIER_UNNESTED.stream().collect(Collectors.joining(", ")));
        returned.append("\nFROM " + fromTableName);
        returned.append("\nWHERE " + FormatSQL.textToSql(deleteRequest.getIhmClient()) + " = ANY(client)");
        returned.append("\n    AND phase_traitement = 'MAPPING'");
        returned.append("\n;");
        return returned.toString();
    }
    
    private static String createNewTableDeleteLastImportRequestOperations(DeleteRequestModel deleteRequest, String fromTableName, String toTableName) {
        StringBuilder returned = new StringBuilder();
        returned.append("\nCREATE UNLOGGED TABLE IF NOT EXISTS " + toTableName + " AS");
        returned.append(
            "\nSELECT " + COL_PILOTAGE_FICHIER.stream().collect(Collectors.joining(", "))
        );
        returned.append("\nFROM " + fromTableName);
        returned.append("\nWHERE NOT (" + deleteCondition(deleteRequest) + ")");
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
        String fromTable, String toTable
    ) {
        StringBuilder returned = new StringBuilder();
        returned.append(
            "\nINSERT INTO " + toTable + " ("
                + COL_PILOTAGE_FICHIER.stream().collect(Collectors.joining(", ")) + ")"
        );
        returned.append("\nSELECT " + COL_PILOTAGE_FICHIER_CLIENT_NULL.stream().collect(Collectors.joining(", ")));
        returned.append("\nFROM " + fromTable + " lefty");
        returned.append("\nWHERE NOT EXISTS (");
        returned.append("\n    SELECT 1");
        returned.append("\n    FROM " + toTable + " righty");
        returned.append("\n    WHERE lefty.id_source = righty.id_source");
        returned.append("\n)");
        returned.append("\n;");
        return returned.toString();
    }
    
   private static String createTableAggregatedLines(
        String fromTable, String toTable
    ) {
        StringBuilder returned = new StringBuilder();
        returned.append("\nCREATE UNLOGGED TABLE IF NOT EXISTS " + toTable + " AS");
        returned.append("\nSELECT " + COL_PILOTAGE_FICHIER_AGG.stream().collect(Collectors.joining(", ")));
        returned.append("\nFROM " + fromTable);
        returned.append("\nGROUP BY id_source");
        returned.append("\n;");
        return returned.toString();
    }
   
   private static String updateOnePartition(
       String fromTable, String toTable, long partnb, long partnum
   ) {
       StringBuilder returned = new StringBuilder();
       returned.append("\nUPDATE " + toTable + " lefty");
       returned.append("\nSET client = righty.client, date_client = righty.date_client");
       returned.append("\nFROM " + fromTable + " righty");
       returned.append("\nWHERE lefty.id_source = righty.id_source");
       returned.append("\n    AND abs(hashtext(lefty.id_source) % " + partnb + ") = " + partnum);
       returned.append("\n    AND abs(hashtext(righty.id_source) % " + partnb + ") = " + partnum);
       returned.append("\n    AND lefty.phase_traitement = 'MAPPING'");
       returned.append("\n;");
       return returned.toString();
   }

    @RequestMapping("/deleteLastImportRequestOperations")
    public String deleteLastImportRequestOperations(
        @ModelAttribute("deleteRequest") DeleteRequestModel deleteRequest, Model model
    ) {
        /*
         * 
         */
        try {
            StringBuilder query = new StringBuilder();
            String tempTableCopyEligibleLines = FormatSQL.temporaryTableName(fromTable(deleteRequest));
            query.append(dropTable(tempTableCopyEligibleLines));
            query.append(
                createWorkTableDeleteLastImportRequestOperations(
                    deleteRequest, fromTable(deleteRequest), tempTableCopyEligibleLines
                )
            );
            UtilitaireDao.get("arc").executeBlock(null, query.toString());
            long nbOfLines = UtilitaireDao.get("arc").getCount(null, tempTableCopyEligibleLines);
            long nbOfPartitions = 1 + (nbOfLines / NB_LINES_PER_PARTITION);
            /*
             * 
             */
            query = new StringBuilder();
            String tempTableDeleteLines = FormatSQL.temporaryTableName(fromTable(deleteRequest));
            query.append(dropTable(tempTableDeleteLines));
            query.append(
                createNewTableDeleteLastImportRequestOperations(
                    deleteRequest, tempTableCopyEligibleLines, tempTableDeleteLines
                )
            );
            query.append(
                insertNotDeletedRequestOperations(tempTableCopyEligibleLines, tempTableDeleteLines)
            );
            /*
             * 
             */
            String tempTableNameAggregatedLines = FormatSQL.temporaryTableName(fromTable(deleteRequest));
            query.append(dropTable(tempTableNameAggregatedLines));
            query.append(createTableAggregatedLines(tempTableDeleteLines, tempTableNameAggregatedLines));
            for (long i = 0; i < nbOfPartitions; i++) {
                query.append(
                    updateOnePartition(
                        tempTableNameAggregatedLines, fromTable(deleteRequest), nbOfPartitions, i
                    )
                );
            }
            query.append(dropTable(tempTableDeleteLines));
            query.append(dropTable(tempTableCopyEligibleLines));
            query.append(dropTable(tempTableNameAggregatedLines));
            UtilitaireDao.get("arc").executeBlock(null, query.toString());
        } catch (SQLException e) {
            String message = "Erreur lors de la suppression des opérations pour l'application cliente "
                + deleteRequest.getIhmClient() + " entre le " + deleteRequest.getLowDate() + "(inclus) et le "
                + deleteRequest.getHighDate() + " (exclu)";
            this.viewOperations.setMessage(message + " :\n" + e.getMessage());
            LoggerHelper.error(LOGGER, message, e);
        }
        model.addAttribute(IHM_DELETE_REQUEST, new DeleteRequestModel());
        return generateDisplay(model, RESULT_SUCCESS);
    }

    @RequestMapping("/selectOperations")
    public String selectOperations(Model model) {

		return generateDisplay(model, RESULT_SUCCESS);
    }

    @RequestMapping("/addOperations")
    public String addOperations(Model model) {
        this.vObjectService.insert(this.viewOperations);
        return generateDisplay(model, RESULT_SUCCESS);
    }

    @RequestMapping("/deleteOperations")
    public String deleteOperations(Model model) {
         this.vObjectService.delete(this.viewOperations);
        return generateDisplay(model, RESULT_SUCCESS);
    }

    @RequestMapping("/updateOperations")
    public String updateOperations(Model model) {
        this.vObjectService.update(this.viewOperations);
        return generateDisplay(model, RESULT_SUCCESS);
    }

    @RequestMapping("/sortOperations")
    public String sortOperations(Model model) {
        this.vObjectService.sort(this.viewOperations);
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

	@ModelAttribute("ihmClients")
    public List<String> getIhmClients() {
        return this.ihmClients;
    }

    public void setIhmClients(List<String> ihmClients) {
        this.ihmClients = ihmClients;
    }

    public List<String> getEnvironments() {
        return this.environments;
    }

    public void setEnvironments(List<String> environments) {
        this.environments = environments;
    }
    
}