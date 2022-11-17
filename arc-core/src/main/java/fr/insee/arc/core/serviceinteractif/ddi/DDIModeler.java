package fr.insee.arc.core.serviceinteractif.ddi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.insee.arc.core.model.ddi.DDIDatabase;
import fr.insee.arc.core.model.ddi.DDIRepresentedVariable;
import fr.insee.arc.core.model.ddi.DDITable;
import fr.insee.arc.core.model.ddi.DDIVariable;
import fr.insee.arc.core.model.ddi.DDIVariableOfTable;
import fr.insee.arc.core.model.famille.ModelTable;
import fr.insee.arc.core.model.famille.ModelVariable;

public class DDIModeler {

    public List<ModelTable> getModelTables() {
        return modelTables;
    }

    public List<ModelVariable> getModelVariables() {
        return modelVariables;
    }

    private final List<ModelTable> modelTables = new ArrayList<>();
    private final List<ModelVariable> modelVariables = new ArrayList<>();

    private static final Map<String, String> datatypes;
    static {
        datatypes = new HashMap<>();
        datatypes.put("Text", "text");
        datatypes.put("Numeric", "float"); // tous entiers a priori (identifiants uniquement?)
        datatypes.put("Category", "text");
        datatypes.put("DateTime", "date");
        datatypes.put("undefined", "text"); // si pas de r variable
        datatypes.put("[type]", "text"); // si r variable n'a pas de type
        // obligatoire de préciser une taille pour VARCHAR, sinon défaut à 1 et la chaîne de caractères est tronquée
    }

    public void model(DDIHandler h) {
        List<DDIDatabase> listDbs = h.getListDDIDatabases();
        List<DDITable> listDDITables = h.getListTables();
        List<DDIVariableOfTable> listVariablesOfTable = h.getListDDIVariableOfTables();
        List<DDIVariable> listDDIVariables = h.getListVariables();
        List<DDIRepresentedVariable> listDDIRepresentedVariables = h.getListRepresentedVariables();
        // build modelTables
        for (DDITable t : listDDITables) {
            ModelTable mt = new ModelTable();
            int i = 0;
            while (i < listDbs.size() && !t.getIdDatabase().equals(listDbs.get(i).getId())) { // retrouver la database
                i++;
            }
            DDIDatabase db = listDbs.get(i);
            mt.setIdFamille(db.getDbName());
            mt.setNomTableMetier(t.getTableName());
            mt.setDescriptionTable(t.getDescription());
            modelTables.add(mt);
        }
        // build modelVariables
        for (DDIVariableOfTable vt : listVariablesOfTable) {
            ModelVariable mv = new ModelVariable();
            int ja = 0;
            while (ja < listDDITables.size() && !vt.getIdTable().equals(listDDITables.get(ja).getIdTable())) { // retrouver la table
                ja++;
            }
            DDITable t = listDDITables.get(ja);
            int jb = 0;
            while (jb < listDbs.size() && !t.getIdDatabase().equals(listDbs.get(jb).getId())) { // retrouver la database
                jb++;
            }
            DDIDatabase db = listDbs.get(jb);
            int jc = 0;
            while (jc < listDDIVariables.size() && !vt.getIdVariable().equals(listDDIVariables.get(jc).getIdVariable())) { // retrouver la variable
                jc++;
            }
            DDIVariable v = listDDIVariables.get(jc);
            int jd = 0;
            while (jd < listDDIRepresentedVariables.size() && !v.getIdRepresentedVariable().equals(listDDIRepresentedVariables.get(jd).getId())) { // retrouver la represented variable
                jd++;
            }
            DDIRepresentedVariable rv = listDDIRepresentedVariables.get(jd);
            mv.setIdFamille(db.getDbName());
            mv.setNomTableMetier(t.getTableName());
            mv.setNomVariableMetier(v.getVariableName());
            mv.setTypeVariableMetier(datatypes.get(rv.getType()));
            mv.setDescriptionVariableMetier(rv.getDescription());
            modelVariables.add(mv);
        }
    }

}