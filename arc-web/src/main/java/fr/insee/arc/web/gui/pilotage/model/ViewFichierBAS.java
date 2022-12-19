package fr.insee.arc.web.gui.pilotage.model;

import java.util.HashMap;

import fr.insee.arc.web.util.ConstantVObject;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;
import fr.insee.arc.web.util.VObject;

public class ViewFichierBAS extends VObject {
    public ViewFichierBAS() {
        super();
     	
		this.setTitle("view.envManagement.detail");
		this.setSessionName("viewFichierBAS");
		this.setDefaultPaginationSize(10);
        
        this.setConstantVObject(new ConstantVObject(new HashMap<String, ColumnRendering>() {
        	
            private static final long serialVersionUID = -2866717984595504329L;

            {
                put("container", new ColumnRendering(true, "label.file.archive", "15%", "text", null, true));
                put("id_source", new ColumnRendering(true, "label.file.name", "15%", "text", null, true));
                put("id_norme", new ColumnRendering(true, "label.norm", "6%", "text", null, true));
                put("validite", new ColumnRendering(true, "label.validity", "6%", "text", null, true));
                put("periodicite", new ColumnRendering(true, "label.periodicity", "6%", "text", null, true));
                put("phase_traitement", new ColumnRendering(true, "label.step.name", "8%", "text", null, true));
                put("etat_traitement", new ColumnRendering(true, "label.step.status", "5%", "text", null, true));
                put("taux_ko", new ColumnRendering(true, "label.step.error", "5%", "text", null, true));
                put("date_traitement", new ColumnRendering(true, "label.date.processing", "10%", "text", null, true));
                put("rapport", new ColumnRendering(true, "label.step.report", "8%", "text", null, true));
                put("nb_enr", new ColumnRendering(true, "label.step.count.line", "4%", "text", null, true));
                put("to_delete", new ColumnRendering(true, "label.step.todo", "4%", "text", null, true));
                put("jointure", new ColumnRendering(true, "label.step.XML", "8%", "text", null, true));
            }
        }

        ));
    }

}
