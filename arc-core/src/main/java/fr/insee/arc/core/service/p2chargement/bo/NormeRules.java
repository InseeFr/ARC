package fr.insee.arc.core.service.p2chargement.bo;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.utils.LoggerHelper;


/**
 * classe permettant de gérer les normes
 * 
 * @author S4LWO8
 *
 */
public class NormeRules {
    
    private String idNorme;
    private String periodicite;
    private String defNorme; 
    private String defValidite;
    
	private static final Logger LOGGER = LogManager.getLogger(NormeRules.class);
    
    public NormeRules(String idNorme, String periodicite, String defNorme, String defValidite) {
        super();
        this.idNorme = idNorme;
        this.periodicite = periodicite;
        this.defNorme = defNorme;
        this.defValidite = defValidite;
    }
    
    public NormeRules() {
    }

    public String getIdNorme() {
        return idNorme;
    }
    public void setIdNorme(String idNorme) {
        this.idNorme = idNorme;
    }
    public String getPeriodicite() {
        return periodicite;
    }
    public void setPeriodicite(String periodicite) {
        this.periodicite = periodicite;
    }
    public String getDefNorme() {
        return defNorme;
    }
    public void setDefNorme(String defNorme) {
        this.defNorme = defNorme;
    }
    public String getDefValidite() {
        return defValidite;
    }
    public void setDefValidite(String defValidite) {
        this.defValidite = defValidite;
    }

    
    /**
     * va chercher en base les normes et les renvoie sous forme d'un array
     * @param connexion
     * @param tableNorme
     * @return
     * @throws ArcException
     */
    public static List<NormeRules> getNormesBase(Connection connexion, String envExecution) {

        List<NormeRules> output = new ArrayList<NormeRules>() ;
        // Récupérer les régles de définition de normes
        List<List<String>> normes = new ArrayList<>();
        try {
            normes = new GenericBean(UtilitaireDao.get(0).executeRequest(connexion,
            		new ArcPreparedStatementBuilder( "select id_norme, periodicite, def_norme, def_validite from " + ViewEnum.NORME.getFullName(envExecution) + ";"))).content;
        } catch (ArcException e) {
            LoggerHelper.errorAsComment(LOGGER, "Norme.getNormesBase - norms retrieval in database failed ");
        }

        //boucle sur les normes
        for (int i=0; i<normes.size(); i++) {
            output.add(new NormeRules(normes.get(i).get(0), normes.get(i).get(1), normes.get(i).get(2), normes.get(i).get(3)));
        }
        
        return output;
    }
    
    
    
}
