package fr.insee.arc.web.model;

import java.util.ArrayList;
import java.util.HashMap;

import fr.insee.arc.web.util.ConstantVObject;
import fr.insee.arc.web.util.VObject;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;

public class ViewPilotagePROD extends VObject {
	public ViewPilotagePROD() {
        super();
        this.constantVObject = new ConstantVObject(new HashMap<String, ColumnRendering>() {
            /**
			 * 
			 */
            private static final long serialVersionUID = -1723105359892169582L;

			{
                put("date_entree", new ColumnRendering(true, "Date d'entrée", "100px", "text", null, true));
                put("reception_ok", new ColumnRendering(true, "Reception OK", "60px", "text", null, true));
                put("reception_ko", new ColumnRendering(true, "Reception KO", "60px", "text", null, true));
                put("reception_encours", new ColumnRendering(true, "Reception ENCOURS", "60px", "text", null, true));
                put("chargement_ok", new ColumnRendering(true, "Charger OK", "60px", "text", null, true));
                put("chargement_ko", new ColumnRendering(true, "Charger KO", "60px", "text", null, true));
                put("normage_ok", new ColumnRendering(true, "Norme OK", "60px", "text", null, true));
                put("normage_ko", new ColumnRendering(true, "Norme KO", "60px", "text", null, true));
                put("controle_ok", new ColumnRendering(true, "Controle OK", "60px", "text", null, true));
                put("controle_ko", new ColumnRendering(true, "Controle KO", "60px", "text", null, true));
                put("controle_ok$ko", new ColumnRendering(true, "Controle OK/KO", "60px", "text", null, true));
                put("filtrage_ok", new ColumnRendering(true, "Filtre OK", "60px", "text", null, true));
                put("filtrage_ko", new ColumnRendering(true, "Filtre KO", "60px", "text", null, true));
                put("filtrage_ok$ko", new ColumnRendering(true, "Filtre OK/KO", "60px", "text", null, true));
                put("mapping_ok", new ColumnRendering(true, "Mapping OK", "60px", "text", null, true));
                put("mapping_ko", new ColumnRendering(true, "Mapping KO", "60px", "text", null, true));

            }
        }

        );
    }
    
    
    public ArrayList<ArrayList<String>> reworkContent(ArrayList<ArrayList<String>> content)
    {
    	return ViewPilotage.reworkContentPilotage(content);
    	
    }
    
    
}