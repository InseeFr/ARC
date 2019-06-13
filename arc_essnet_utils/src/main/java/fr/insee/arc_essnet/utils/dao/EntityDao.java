package fr.insee.arc_essnet.utils.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import fr.insee.arc_essnet.utils.textUtils.IConstanteCaractere;
import fr.insee.arc_essnet.utils.textUtils.IConstanteNumerique;

/**
 *
 * Échappez les expressions SQL {@code <expression>} comme ça : {@code "(" <expression> ")"}.
 *
 * @param <T>
 */
public abstract class EntityDao<T extends AbstractEntity> implements IConstanteNumerique, IConstanteCaractere {

    private List<String> names;
    private List<String> types;
    private Map<String, Integer> mapNameToIndex;
    private String tableName;
    private String separator;
    private boolean isEOLSeparator;

    protected EntityDao() {
        this.mapNameToIndex = new HashMap<>();
        this.names = new ArrayList<String>();
        this.types = new ArrayList<String>();
    }

    public EntityDao(String aTableName, String someNames, String someTypes, String aSeparator) {
        this(aTableName, someNames, someTypes, aSeparator, false);
    }

    /**
     * Point d'entrée final !
     *
     * @param aTableName
     * @param someNames
     * @param someTypes
     * @param aSeparator
     * @param anIsEOLSeparator
     */
    public EntityDao(String aTableName, String someNames, String someTypes, String aSeparator, boolean anIsEOLSeparator) {
        this();
        this.isEOLSeparator = anIsEOLSeparator;
        this.setSeparator(aSeparator);
        this.setTableName(aTableName);
        this.setNames(someNames);
        this.setTypes(someTypes);
        if (this.names.size() != this.types.size()) {
            throw new IllegalArgumentException("Le nombre de noms de colonnes doit être le même que le nombre de types pour ces colonnes.");
        }
        buildIndex();
    }

    public void buildIndex() {
        for (int i = 0; i < this.names.size(); i++) {
            this.mapNameToIndex.put(this.names.get(i), i);
        }
    }

    public T get(String someValues) {
        List<String> returned = Arrays.asList(someValues.split(this.separator, Integer.MIN_VALUE));
        returned = handleNulls(returned);
        return get(returned.subList(ARRAY_FIRST_COLUMN_INDEX, returned.size() - (isEOLSeparator() ? 1 : 0)));
    }

    public abstract T get(List<String> someValues);

    /**
     * @return the tableName
     */
    public String getTableName() {
        return this.tableName;
    }

    /**
     * @param tableName
     *            the tableName to set
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * @return the names
     */
    public List<String> getNames() {
        return new ArrayList<>(this.names);
    }

    /**
     * @param names
     *            the names to set
     */
    public void setNames(List<String> names) {
        this.names = names;
    }

    /**
     * @param types
     *            the types to set
     */
    public void setTypes(List<String> types) {
        this.types = types;
    }

    /**
     * @param separator
     *            the separator to set
     */
    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public void setNames(String someNames) {
        List<String> rNames = Arrays.asList(someNames.split(this.separator, Integer.MIN_VALUE));
        rNames = handleNulls(rNames);
        this.setNames(rNames.subList(ARRAY_FIRST_COLUMN_INDEX, rNames.size() - (isEOLSeparator() ? 1 : 0)));
    }

    private static List<String> handleNulls(List<String> aList) {
        List<String> returned = new ArrayList<>(aList.size());
        for (int i = 0; i < aList.size(); i++) {
            returned.add(StringUtils.isEmpty(aList.get(i)) ? null : aList.get(i));
        }
        return returned;
    }

    /**
     * @return the types
     */
    public List<String> getTypes() {
        return this.types;
    }

    public void setTypes(String someTypes) {
        List<String> rTypes = Arrays.asList(someTypes.split(this.separator, Integer.MIN_VALUE));
        rTypes = handleNulls(rTypes);
        this.setTypes(rTypes.subList(ARRAY_FIRST_COLUMN_INDEX, rTypes.size() - (isEOLSeparator() ? 1 : 0)));
    }

    public String getInsert(T regleMappingEntity, Map<String, String> map) {
        StringBuilder returned = new StringBuilder("INSERT INTO " + this.tableName + " (");
        boolean isFirst = true;
        for (int i = 0; i < this.names.size(); i++) {
            // if (map.get(this.names.get(i)) != null) {
            if (isFirst) {
                isFirst = false;
            } else {
                returned.append(", ");
            }
            returned.append(this.names.get(i));
            // }
        }
        returned.append(") VALUES (");
        isFirst = true;
        for (int i = 0; i < this.names.size(); i++) {
            if (map.get(this.names.get(i)) != null) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    returned.append(", ");
                }
                returned.append(quotify(map.get(this.names.get(i))) + "::" + this.types.get(i));
            } else {
                if (isFirst) {
                    isFirst = false;
                } else {
                    returned.append(", ");
                }
                returned.append(quotify(regleMappingEntity.get(this.names.get(i))) + "::" + this.types.get(i));
            }
        }
        returned.append(");");
        return returned.toString();
    }

    /**
     * C'est l'éternel problème du "Et si je veux valoriser ma variable avec une expression SQL ? Hein ?".<br/>
     * La règle en la matière est la suivante :<br/>
     * 1. Tout ce qui n'est pas entre parenthèses est mis entre {@code "'"} puis casté.<br/>
     * 2. Tout ce qui ressemble à {@code "\(" <expression> "\)"} est casté, mais pas mis entre cotes.
     *
     * @param anExpression
     * @return
     */
    private static final String quotify(String anExpression) {
        if (anExpression == null) {
            return "null";
        }
        return anExpression.matches("^\\(.*\\)$") ? anExpression : new StringBuilder(QUOTE).append(anExpression.replace(QUOTE, QUOTE_QUOTE))
                .append(QUOTE).toString();
    }

    /**
     * @return the isEOLSeparator
     */
    public boolean isEOLSeparator() {
        return this.isEOLSeparator;
    }

    /**
     * @param isEOLSeparator
     *            the isEOLSeparator to set
     */
    public void setEOLSeparator(boolean isEOLSeparator) {
        this.isEOLSeparator = isEOLSeparator;
    }

    public String toString() {
        return new StringBuilder(this.tableName + NEWLINE)//
                .append(this.getNames() + NEWLINE)//
                .append(this.getTypes())//
                .toString();
    }
}
