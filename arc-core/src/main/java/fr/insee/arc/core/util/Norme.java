package fr.insee.arc.core.util;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.model.IDbConstant;
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
public class Norme implements IDbConstant{
    
    private String idNorme;
    private String periodicite;
    private String defNorme; 
    private String defValidite;
    private RegleChargement regleChargement;
    
	private static final Logger LOGGER = LogManager.getLogger(Norme.class);
    
    public Norme(String idNorme, String periodicite, String defNorme, String defValidite) {
        super();
        this.idNorme = idNorme;
        this.periodicite = periodicite;
        this.defNorme = defNorme;
        this.defValidite = defValidite;
    }
    
    public Norme() {
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
    public RegleChargement getRegleChargement() {
        return regleChargement;
    }
    public void setRegleChargement(RegleChargement regleChargement) {
        this.regleChargement = regleChargement;
    }
    
    /**
     * va chercher en base les normes et les renvoie sous forme d'un array
     * @param connexion
     * @param tableNorme
     * @return
     * @throws ArcException
     */
    public static List<Norme> getNormesBase(Connection connexion, String tableNorme) {

        List<Norme> output = new ArrayList<Norme>() ;
        // Récupérer les régles de définition de normes
        ArrayList<ArrayList<String>> normes = new ArrayList<ArrayList<String>>();
        try {
            normes = new GenericBean(UtilitaireDao.get(poolName).executeRequest(connexion,
            		new ArcPreparedStatementBuilder( "select id_norme, periodicite, def_norme, def_validite from " + tableNorme + ";"))).content;
        } catch (ArcException e) {
            LoggerHelper.errorAsComment(LOGGER, "Norme.getNormesBase - norms retrieval in database failed ");
        }

        //boucle sur les normes
        for (int i=0; i<normes.size(); i++) {
            output.add(new Norme(normes.get(i).get(0), normes.get(i).get(1), normes.get(i).get(2), normes.get(i).get(3)));
        }
        
        return output;
    }
    
    
    
}
