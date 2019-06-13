package fr.insee.arc_essnet.core.dao;

import java.util.List;

import fr.insee.arc_essnet.core.model.RegleControleEntity;
import fr.insee.arc_essnet.utils.dao.EntityDao;

/**
 * DAO to get the control rules
 * @author Pépin Rémi
 *
 */
public class RegleControleDao extends EntityDao<RegleControleEntity> {

    public RegleControleDao(String aTableName, String someNames, String someTypes, String aSeparator, boolean anIsEOLSeparator) {
        super(aTableName, someNames, someTypes, aSeparator, anIsEOLSeparator);
    }

    public RegleControleDao(String aTableName, String someNames, String someTypes, String separator) {
        super(aTableName, someNames, someTypes, separator);
    }

    public RegleControleDao() {
        super();
    }

    @Override
    public RegleControleEntity get(List<String> someValues) {
        return new RegleControleEntity(this.getNames(), someValues);
    }

}
