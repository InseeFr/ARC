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
import fr.insee.arc.core.model.famille.ModelVariableTypeEnum;

/**
 * Classe intégrant un modèle de données en sortie de {@link DDIHandler} dans le modèle {@code famille} de ARC
 * 
 * @author Z84H10
 *
 */
public class DDIModeler {

    public List<ModelTable> getModelTables() {
        return modelTables;
    }

    public List<ModelVariable> getModelVariables() {
        return modelVariables;
    }

    /**
     * Liste alimentée des tables lues par le modeler.
     */
    private final List<ModelTable> modelTables = new ArrayList<>();
    /**
     * Liste alimentée des variables lues par le modeler.
     */
    private final List<ModelVariable> modelVariables = new ArrayList<>();
    /**
     * Réalise le lien entre les types DDI et les types {@link ModelVariableTypeEnum}.
     */
    private static final Map<String, ModelVariableTypeEnum> datatypes;
    static {
        datatypes = new HashMap<>();
        datatypes.put("Text", ModelVariableTypeEnum.TEXT);
        datatypes.put("Code", ModelVariableTypeEnum.TEXT);
        datatypes.put("Float", ModelVariableTypeEnum.FLOAT);
        datatypes.put("Decimal", ModelVariableTypeEnum.FLOAT);
        datatypes.put("Double", ModelVariableTypeEnum.FLOAT);
        datatypes.put("Integer", ModelVariableTypeEnum.BIGINT);
        datatypes.put("Long", ModelVariableTypeEnum.BIGINT);
        datatypes.put("Short", ModelVariableTypeEnum.BIGINT);
        datatypes.put("DateTime", ModelVariableTypeEnum.DATE);
        datatypes.put("undefined", ModelVariableTypeEnum.TEXT); // si pas de r variable
    }

    /**
     * Permet de convertir une sortie de {@link DDIHandler} en une famille de norme selon le modèle de ARC.
     * @param h {@code DDIHandler} préalablement alimenté par un fichier XML DDI
     */
    public void model(DDIHandler h) {
        // build modelTables
        for (DDITable t : h.getListDDITables()) {
            ModelTable mt = new ModelTable();
            DDIDatabase db = h.getDDIDatabaseByID(t.getIdDatabase());
            mt.setIdFamille(db.getDbName());
            mt.setNomTableMetier(t.getTableName());
            mt.setDescriptionTable(t.getDescription());
            modelTables.add(mt);
        }
        // build modelVariables
        for (DDIVariableOfTable vt : h.getListDDIVariableOfTables()) { // car dans le modèle ARC une variable est propre à une table
            ModelVariable mv = new ModelVariable();
            DDITable t = h.getDDITableByID(vt.getIdTable());
            DDIDatabase db = h.getDDIDatabaseByID(t.getIdDatabase());
            DDIVariable v = h.getDDIVariableByID(vt.getIdVariable());
            DDIRepresentedVariable rv = h.getDDIRepresentedVariableByID(v.getIdRepresentedVariable());
            mv.setIdFamille(db.getDbName());
            mv.setNomTableMetier(t.getTableName());
            mv.setNomVariableMetier(v.getVariableName());
            mv.setTypeVariableMetier(datatypes.get(rv.getType()).getTypeName());
            mv.setDescriptionVariableMetier(rv.getDescription());
            modelVariables.add(mv);
        }
    }

    
    @Override
    public String toString()
    {
    	return this.modelTables.get(0).toString();
    }
}