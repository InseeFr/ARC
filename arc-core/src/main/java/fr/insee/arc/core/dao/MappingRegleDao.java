package fr.insee.arc.core.dao;

import java.util.List;

import fr.insee.arc.core.model.RegleMappingEntity;
import fr.insee.arc.utils.dao.EntityDao;

public class MappingRegleDao extends EntityDao<RegleMappingEntity> {

    public MappingRegleDao(String aTableName, String someNames, String someTypes, String aSeparator, boolean anIsEOLSeparator) {
        super(aTableName, someNames, someTypes, aSeparator, anIsEOLSeparator);

    }

    public MappingRegleDao(String aTableName, String someNames, String someTypes, String separator) {
        super(aTableName, someNames, someTypes, separator);
    }

    public MappingRegleDao() {
        super();
    }

    @Override
    public RegleMappingEntity get(List<String> someValues) {
        return new RegleMappingEntity(this.getNames(), someValues);
    }

}
