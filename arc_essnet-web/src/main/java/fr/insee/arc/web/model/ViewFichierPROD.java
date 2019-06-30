package fr.insee.arc.web.model;

import java.util.HashMap;

import fr.insee.arc.web.util.ConstantVObject;
import fr.insee.arc.web.util.VObject;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;

public class ViewFichierPROD extends VObject {
    public ViewFichierPROD() {
        super();
        this.constantVObject = new ConstantVObject(new HashMap<String, ColumnRendering>() {
            /**
			 * 
			 */
            private static final long serialVersionUID = 8536954953281001177L;

			{
                put("container", new ColumnRendering(true, "Archive", "150px", "text", null, true));
                put("id_source", new ColumnRendering(true, "Nom de fichier", "200px", "text", null, true));
                put("id_norme", new ColumnRendering(true, "Norme", "80px", "text", null, true));
                put("validite", new ColumnRendering(true, "Validite", "60px", "text", null, true));
                put("periodicite", new ColumnRendering(true, "Periodicite", "40px", "text", null, true));
                put("phase_traitement", new ColumnRendering(true, "Phase", "80px", "text", null, true));
                put("etat_traitement", new ColumnRendering(true, "Etat", "50px", "text", null, true));
                put("taux_ko", new ColumnRendering(true, "Erreur en %", "50px", "text", null, true));
                put("date_traitement", new ColumnRendering(true, "Date de traitement", "80px", "text", null, true));
                put("rapport", new ColumnRendering(true, "Rapport d'anomalie", "80px", "text", null, true));
                put("nb_enr", new ColumnRendering(true, "Nb obs", "40px", "text", null, true));
                put("to_delete", new ColumnRendering(true, "A supprimer", "40px", "text", null, true));
                put("jointure", new ColumnRendering(true, "Jointure XML", "80px", "text", null, true));

            }
        }

        );
    }

}
