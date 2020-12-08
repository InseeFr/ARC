package fr.insee.arc.web.model.viewobjects;

import java.util.HashMap;

import fr.insee.arc.utils.dao.PreparedStatementBuilder;
import fr.insee.arc.web.util.ConstantVObject;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;
import fr.insee.arc.web.util.VObject;

public class ViewJeuxDeReglesCopie extends VObject {
    public ViewJeuxDeReglesCopie() {
        super();
        
        this.setTitle("normManagement.copyRules");

        this.setSessionName("viewJeuxDeReglesCopie");

        this.setConstantVObject(new ConstantVObject(new HashMap<String, ColumnRendering>() {
            /**
			 * 
			 */
            private static final long serialVersionUID = 7482785414572296062L;

			{
                put("id_norme", new ColumnRendering(true, "label.norm", "100px", "text", null, false));
                put("periodicite", new ColumnRendering(true, "label.periodicity", "100px", "text", null, false));
                put("validite_inf", new ColumnRendering(true, "label.validity.min", "100px", "text", null, false));
                put("validite_sup", new ColumnRendering(true, "label.validity.max", "100px", "text", null, false));
                put("version", new ColumnRendering(true, "label.version", "50px", "text", null, false));
                put("etat", new ColumnRendering(true, "label.state", "120px", "select", new PreparedStatementBuilder("select id, val from arc.ext_etat_jeuderegle order by id"), false));

            }

        }

        ));
    }
}