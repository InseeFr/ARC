package fr.insee.arc_essnet.core.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.insee.arc_essnet.core.model.RuleSets;
import fr.insee.arc_essnet.core.service.thread.ThreadControleService;
import fr.insee.arc_essnet.utils.utils.LoggerDispatcher;

/**
 * ApiChargementService
 *
 * 1- Créer les tables de reception du chargement</br>
 * 2- Récupérer la liste des fichiers à traiter et le nom de leur entrepôt</br>
 * 3- Pour chaque fichier, determiner son format de ***REMOVED*** (zip, tgz, raw) et le chargeur à utlisé (voir entrepot)</br> 
 * 4- Pour chaque fichier, invoquer le chargeur</br> 
 *  4-1 Parsing du fichier</br> 
 *  4-2 Insertion dans les tables I et A des données lues dans le fichier</br> 
 *  4-3 Fin du parsing. Constituer la requete de mise en relation des données chargées et la stocker pour son utilisation ultérieure au normage</br>
 * 5- Fin chargement. Insertion dans la table applicative CHARGEMENT_OK. Mise à jour de la table de pilotage</br>
 *
 * @author Manuel SOULIER
 *
 */
@Component
public class ApiControleService extends AbstractThreadRunnerService<ThreadControleService>
	implements IApiServiceWithOutputTable {

    private static final Logger LOGGER = Logger.getLogger(ApiControleService.class);
    private static final Class<ThreadControleService> THREAD_TYPE = ThreadControleService.class ;

    @Autowired
    public ServiceRuleSets sjdr;

    protected ArrayList<RuleSets> listJdr;

    private int currentIndice;

    public ApiControleService() {
	super();
    }

    public ApiControleService(String aCurrentPhase, String anParametersEnvironment, String aEnvExecution, String aDirectoryRoot, Integer aNbEnr,
            String... paramBatch) {
        super(THREAD_TYPE, aCurrentPhase, anParametersEnvironment, aEnvExecution, aDirectoryRoot, aNbEnr, paramBatch);
        
        // fr.insee.arc.threads.controle
        this.nbThread = 3;

    }


    public ApiControleService(Connection connexion, String aCurrentPhase, String anParametersEnvironment, String aEnvExecution, String aDirectoryRoot, Integer aNbEnr,
            String... paramBatch) {
        super(connexion,THREAD_TYPE, aCurrentPhase, anParametersEnvironment, aEnvExecution, aDirectoryRoot, aNbEnr, paramBatch);
        
        // fr.insee.arc.threads.controle
        this.nbThread = 3;
    }

    /**
     * Méthode pour controler une table
     *
     * @param connexion
     *
     * @param tableControle
     *            la table à controler
     *
     * @throws SQLException
     */
    public void execute(Connection connexion, String env, String phase, String tableControle) throws Exception {
        LoggerDispatcher.info("** execute CONTROLE sur la table : " + tableControle + " **", LOGGER);

        for (RuleSets jdr : this.getListJdr()) {
            this.sjdr.executeJeuDeRegle(connexion, jdr, tableControle);
        }

    }

    public ArrayList<RuleSets> getListJdr() {
        return listJdr;
    }

    public void setListJdr(ArrayList<RuleSets> listJdr) {
        this.listJdr = listJdr;
    }

    public ServiceRuleSets getSjdr() {
        return sjdr;
    }

    public void setSjdr(ServiceRuleSets sjdr) {
        this.sjdr = sjdr;
    }

}
