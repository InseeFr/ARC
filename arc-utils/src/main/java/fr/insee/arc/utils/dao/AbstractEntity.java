package fr.insee.arc.utils.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * A reccord form a table
 *
 */
public abstract class AbstractEntity {

    private Map<String, String> map;
    private String table;

    public AbstractEntity() {
        this(new HashMap<String, ArrayList<String>>());

    }

    public AbstractEntity(Map<String, ArrayList<String>> mapInputFields) {
        this.map = new HashMap<>();
    }

    public AbstractEntity(List<String> someNames, List<String> someValues) {
        this();
        if (someNames.size() != someValues.size()) {
            throw new IllegalArgumentException("Le nombre de valeurs doit être égal au nombre de noms de colonnes.");
        }
        for (int i = 0; i < someNames.size(); i++) {
            if (colNames().contains(someNames.get(i))) {
                this.map.put(someNames.get(i), someValues.get(i));
            }
        }
    }

    /**
     * @param colName
     *            le nom de la variable
     * @return la valeur de la variable
     */
    public String get(String colName) {
        return this.getMap().get(colName);
    }

    public abstract Set<String> colNames();

    /**
     * @return the map
     */
    public Map<String, String> getMap() {
        return this.map;
    }

    /**
     * @return this.table
     */
    public String getTable() {
        return this.table;
    }

    /**
     * @param table
     *            the table to set
     */
    public void setTable(String table) {
        this.table = table;
    }

    public String toString() {
        return getMap().toString();
    }

}
