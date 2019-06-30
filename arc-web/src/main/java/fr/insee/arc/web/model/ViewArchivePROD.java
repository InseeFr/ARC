package fr.insee.arc.web.model;

import java.util.HashMap;

import fr.insee.arc.web.util.ConstantVObject;
import fr.insee.arc.web.util.VObject;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;

public class ViewArchivePROD extends VObject {
	public ViewArchivePROD() {
        super();
        this.constantVObject = new ConstantVObject(new HashMap<String, ColumnRendering>() {
            /**
			 * 
			 */
            private static final long serialVersionUID = -5756277500927845603L;

			{
                put("entrepot", new ColumnRendering(true, "Entrepot de dépôt", "120px", "text", null, true));
                put("nom_archive", new ColumnRendering(true, "Nom de l'archive", "250px", "text", null, true));

            }
        }

        );
    }
}