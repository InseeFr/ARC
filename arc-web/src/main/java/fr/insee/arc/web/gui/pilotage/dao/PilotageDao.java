package fr.insee.arc.web.gui.pilotage.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.DataObjectService;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.web.util.VObject;
import fr.insee.arc.web.util.VObjectHelperDao;
import fr.insee.arc.web.util.VObjectService;

public class PilotageDao extends VObjectHelperDao {
	
	private VObjectService vObjectService;
	private DataObjectService dataObjectService;

	public PilotageDao(VObjectService vObjectService, DataObjectService dataObjectService) {
		super();
		this.vObjectService = vObjectService;
		this.dataObjectService = dataObjectService;
	}
	
	public void initializePilotageBAS(VObject viewPilotageBAS) {		

		// the most recent files processed must be shown first by default
        // set this default order
        if (viewPilotageBAS.getHeaderSortDLabels() == null) {
        	viewPilotageBAS.setHeaderSortDLabels(new ArrayList<>(Arrays.asList(ColumnEnum.DATE_ENTREE.getColumnName())));
        	viewPilotageBAS.setHeaderSortDOrders(new ArrayList<>(Arrays.asList(false)));
        }
		
        viewPilotageBAS.setNoCount(true);
        viewPilotageBAS.setNoLimit(true);
        
		
		HashMap<String, String> defaultInputFields = new HashMap<>();
		
    	ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();		
    	requete.append("SELECT date_entree ");
		for (TraitementPhase phase:TraitementPhase.listPhasesAfterPhase(TraitementPhase.RECEPTION))
		{
			for (TraitementEtat etat:new ArrayList<>(Arrays.asList(TraitementEtat.valuesByOrdreAffichage())))
			{
				String columnName=phase.toString().toLowerCase()+"_"+etat.toString().toLowerCase();
				requete.append("\n, max(CASE WHEN phase_traitement='"+phase+"' and etat_traitement='"+etat.getSqlArrayExpression()+"' THEN n ELSE 0 END) as "+columnName+" ");
			}
			
		}
		requete.append("\n FROM (");
        requete.append("\n SELECT date_entree, phase_traitement, etat_traitement, count(*) as n ");
		requete.append("\n FROM "+dataObjectService.getView(ViewEnum.PILOTAGE_FICHIER)+" b ");
		requete.append("\n WHERE date_entree IN ( ");
		requete.append("\n SELECT DISTINCT date_entree FROM "+dataObjectService.getView(ViewEnum.PILOTAGE_FICHIER)+" a ");
        requete.append(this.vObjectService.buildFilter(viewPilotageBAS.getFilterFields(), viewPilotageBAS.getHeadersDLabel()));
        requete.append("\n AND phase_traitement='"+TraitementPhase.RECEPTION+"' ");
        requete.append(this.vObjectService.buildOrderBy(viewPilotageBAS.getHeaderSortDLabels(), viewPilotageBAS.getHeaderSortDOrders()));
        requete.append(this.vObjectService.buildLimit(viewPilotageBAS, this.vObjectService.pageManagement(null, viewPilotageBAS)));
		requete.append("\n ) ");
		requete.append("\n GROUP BY date_entree, phase_traitement, etat_traitement ");
		requete.append(") ttt ");
		requete.append("group by date_entree ");

		this.vObjectService.initialize(
				viewPilotageBAS, requete, null, defaultInputFields);
		
	}

	
}
