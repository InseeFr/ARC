package fr.insee.arc_essnet.web.model;

import java.util.HashMap;

import fr.insee.arc_essnet.web.util.ConstantVObject;
import fr.insee.arc_essnet.web.util.ConstantVObject.ColumnRendering;
import fr.insee.arc_essnet.web.util.VObject;

public class ViewVariableMetier  extends VObject {

    /**
     * 
     */
    private static final long serialVersionUID = 8504910539544679986L;

    public ViewVariableMetier() {
        super();
        this.constantVObject = new ConstantVObject(new HashMap<String, ColumnRendering>() {
            private static final long serialVersionUID = -6829848051813971382L;
            {
                /**
                 * Deux moyens d'initialiser cette view, donc voilà...
                 */
            }
        });
    }
}