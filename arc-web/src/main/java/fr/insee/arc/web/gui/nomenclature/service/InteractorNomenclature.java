package fr.insee.arc.web.gui.nomenclature.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.model.IDbConstant;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.web.gui.ArcWebGenericService;
import fr.insee.arc.web.gui.nomenclature.model.ModelNomenclature;
import fr.insee.arc.web.util.VObject;
import fr.insee.arc.web.util.VObjectService;

@Service
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class InteractorNomenclature extends ArcWebGenericService<ModelNomenclature> implements IDbConstant{

	protected static final String RESULT_SUCCESS = "/jsp/gererNomenclature.jsp";

    private static final Logger LOGGER = LogManager.getLogger(InteractorNomenclature.class);

    @Autowired
    protected ModelNomenclature views;
    
    @Autowired
    private VObjectService viewObject;

    
    @Override
    public void putAllVObjects(ModelNomenclature model) {
    	loggerDispatcher.debug("putAllVObjects()", LOGGER);
    	views.setViewListNomenclatures(vObjectService.preInitialize(model.getViewListNomenclatures()));
    	views.setViewNomenclature(vObjectService.preInitialize(model.getViewNomenclature()));
    	views.setViewSchemaNmcl(vObjectService.preInitialize(model.getViewSchemaNmcl()));
  	
    	putVObject(views.getViewListNomenclatures(), t -> initializeViewListNomenclatures(t,
    			dataObjectService.getView(ViewEnum.IHM_NMCL)));

    	putVObject(views.getViewNomenclature(), t -> initializeViewNomenclature(t,
    			views.getViewListNomenclatures()));

    	putVObject(views.getViewSchemaNmcl(), t -> intializeViewSchemaNmcl(t, views.getViewListNomenclatures()));

    	loggerDispatcher.debug("putAllVObjects() end", LOGGER);
    }
    
    @Override
	public String getActionName() {
		return "externalFileManagement";
	}
    

    private static final String NOM_TABLE = "nom_table";
    
    public void initializeViewListNomenclatures(VObject viewListNomenclatures, String table) {
        loggerDispatcher.debug("/* initializeListeNomenclatures */", LOGGER);
        
        HashMap<String, String> defaultInputFields = new HashMap<>();
        ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
        requete.append(SQL.SELECT);
        requete.append(requete.sqlListeOfColumnsFromModel(ViewEnum.IHM_NMCL));
        requete.append(SQL.FROM).append(table);

        viewObject.initialize(viewListNomenclatures, requete, table, defaultInputFields);
    }

    
    public void initializeViewNomenclature(VObject viewNomenclature, VObject viewListNomenclatures) {
    	loggerDispatcher.debug( "/* initializeViewNomenclature */", LOGGER);

    	Map<String, ArrayList<String>> selection = viewListNomenclatures.mapContentSelected();

        if (!selection.isEmpty() && Boolean.TRUE.equals(UtilitaireDao.get(poolName).isTableExiste(null, "arc." + selection.get(NOM_TABLE).get(0)))) {
        	ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
            requete.append("select * from arc." + selection.get(NOM_TABLE).get(0) + " ");

            HashMap<String, String> defaultInputFields = new HashMap<>();
            defaultInputFields.put(NOM_TABLE, selection.get(NOM_TABLE).get(0));

            viewObject.initialize(viewNomenclature, requete, "arc." + selection.get(NOM_TABLE).get(0), defaultInputFields);
        } else {
        	viewObject.destroy(viewNomenclature);
        }

    }
    
    public void intializeViewSchemaNmcl(VObject viewSchemaNmcl, VObject viewListNomenclatures) {
        loggerDispatcher.debug("/* initializeSchemaNmcl */", LOGGER);
        Map<String, ArrayList<String>> selection = viewListNomenclatures.mapContentSelected();

        if (!selection.isEmpty()) {
        	ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
        	requete.append(SQL.SELECT);
        	requete.append(requete.sqlListeOfColumnsFromModel(ViewEnum.IHM_SCHEMA_NMCL));
        	requete.append(SQL.FROM);
        	requete.append("arc.ihm_schema_nmcl");
        	requete.append(SQL.WHERE);
        	requete.append(ColumnEnum.TYPE_NMCL).append("=");
            requete.append(requete.quoteText(typeNomenclature(selection.get(NOM_TABLE).get(0))));
         
            HashMap<String, String> defaultInputFields = new HashMap<>();
            defaultInputFields.put(ColumnEnum.TYPE_NMCL.getColumnName(), typeNomenclature(selection.get(NOM_TABLE).get(0)));
            viewObject.initialize(viewSchemaNmcl, requete, "arc.ihm_schema_nmcl", defaultInputFields);
            
        } else {
        	viewObject.destroy(viewSchemaNmcl);
        }
    }

    /**
     * 
     * @param nomTable
     * @return
     */
    private static String typeNomenclature(String nomTable) {
        String[] tokens = nomTable.split(fr.insee.arc.utils.textUtils.IConstanteCaractere.underscore);
        StringBuilder typeNomenclature = new StringBuilder();
        for (int i = 0; i < tokens.length - 1; i++) {
            typeNomenclature.append((i > 0 ? fr.insee.arc.utils.textUtils.IConstanteCaractere.underscore : "") + tokens[i]);
        }
        return typeNomenclature.toString();
    }

}