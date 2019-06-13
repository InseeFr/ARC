package fr.insee.arc_essnet.web.model;

import java.util.ArrayList;
import java.util.HashMap;

import fr.insee.arc_essnet.web.util.ConstantVObject;
import fr.insee.arc_essnet.web.util.ConstantVObject.ColumnRendering;
import fr.insee.arc_essnet.web.util.VObject;

public class ViewPilotageBAS8 extends VObject {
    /**
     * 
     */
    private static final long serialVersionUID = -8577430079827164584L;

    /**
	 *
	 */

    public ViewPilotageBAS8() {
        super();
        this.constantVObject = new ConstantVObject(new HashMap<String, ColumnRendering>() {

            /**
             *
             */
            private static final long serialVersionUID = 3294598955186326004L;

            {
                put("date_entree", new ColumnRendering(true, "Date d'entrée", "", "text", null, true));
                put("register_ok", new ColumnRendering(true, "Register OK", "", "text", null, true));
                put("register_ko", new ColumnRendering(true, "Register KO", "", "text", null, true));
                put("register_encours", new ColumnRendering(true, "Register RUNNING", "", "text", null, true));
                put("identify_encours", new ColumnRendering(true, "Identify RUNNING", "", "text", null, true));
                put("identify_ok", new ColumnRendering(true, "Identify OK", "", "text", null, true));
                put("identify_ko", new ColumnRendering(true, "Identify KO", "", "text", null, true));
                put("load_encours", new ColumnRendering(true, "Load RUNNING", "", "text", null, true));
                put("load_ko", new ColumnRendering(true, "Load KO", "", "text", null, true));
                put("load_ok", new ColumnRendering(true, "Load OK", "", "text", null, true));
                put("structurize_xml_encours", new ColumnRendering(true, "Struct. RUNNING", "", "text", null, true));
                put("structurize_xml_ko", new ColumnRendering(true, "Struct. KO", "", "text", null, true));
                put("structurize_xml_ok", new ColumnRendering(true, "Struct. OK", "", "text", null, true));
                put("control_encours", new ColumnRendering(true, "Control RUNNING", "", "text", null, true));
                put("control_ok$ko", new ColumnRendering(true, "Control OK/KO", "", "text", null, true));
                put("control_ok", new ColumnRendering(true, "Control OK", "", "text", null, true));
                put("control_ko", new ColumnRendering(true, "Control KO", "", "text", null, true));
                put("filter_ok", new ColumnRendering(true, "Filter OK", "", "text", null, true));
                put("filter_ko", new ColumnRendering(true, "Filter KO", "", "text", null, true));
                put("filter_ok$ko", new ColumnRendering(true, "Filtre OK/KO", "", "text", null, true));
                put("format_to_model_ok", new ColumnRendering(true, "Format OK", "", "text", null, true));
                put("format_to_model_ko", new ColumnRendering(true, "Format KO", "", "text", null, true));
                put("format_to_model_ENCOURS", new ColumnRendering(true, "Format RUNNING", "", "text", null, true));


            }
        }

        );
    }

    @Override
    public ArrayList<ArrayList<String>> reworkContent(ArrayList<ArrayList<String>> content) {
        return ViewPilotage.reworkContentPilotage(content);

    }

}