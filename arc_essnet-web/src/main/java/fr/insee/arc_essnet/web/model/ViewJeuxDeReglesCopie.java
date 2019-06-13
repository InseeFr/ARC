package fr.insee.arc_essnet.web.model;

import java.util.HashMap;

import fr.insee.arc_essnet.web.util.ConstantVObject;
import fr.insee.arc_essnet.web.util.ConstantVObject.ColumnRendering;
import fr.insee.arc_essnet.web.util.VObject;

public class ViewJeuxDeReglesCopie extends VObject {
    public ViewJeuxDeReglesCopie() {
        super();
        this.constantVObject = new ConstantVObject(new HashMap<String, ColumnRendering>() {
            /**
			 * 
			 */
            private static final long serialVersionUID = 7482785414572296062L;

			{
                put("id_norme", new ColumnRendering(true, "Norme", "100px", "text", null, false));
                put("periodicite", new ColumnRendering(true, "Periodicite", "100px", "text", null, false));
                put("validite_inf", new ColumnRendering(true, "Debut Validite", "100px", "text", null, false));
                put("validite_sup", new ColumnRendering(true, "Fin Validite", "100px", "text", null, false));
                put("version", new ColumnRendering(true, "Version", "50px", "text", null, false));
                put("etat", new ColumnRendering(true, "Statut", "120px", "select", "select id, val from arc.ext_etat_jeuderegle order by id", false));

            }

        }

        );
    }
}