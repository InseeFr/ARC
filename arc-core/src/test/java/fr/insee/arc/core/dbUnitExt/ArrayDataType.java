package fr.insee.arc.core.dbUnitExt;

import java.lang.invoke.MethodHandles;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.dbunit.dataset.ITable;
import org.dbunit.dataset.datatype.AbstractDataType;
import org.dbunit.dataset.datatype.TypeCastException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class ArrayDataType extends AbstractDataType {

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final Class<Array> CLASS_TYPE = Array.class;

    public ArrayDataType(String name, int sqlType, boolean isNumber) {
        super(name, sqlType, CLASS_TYPE, isNumber);
    }

    @Override
    public Object typeCast(Object value) throws TypeCastException {
        if (value == null || value == ITable.NO_VALUE) {
            return null;
        }

        if (value instanceof String) {
            return new String[]{(String) value};
        }
        if (value instanceof String[]) {
            return value;
        }

        if (value instanceof Date ||
                value instanceof Time ||
                value instanceof Timestamp) {
            return new String[]{value.toString()};
        }

        if (value instanceof Boolean) {
            return new String[]{value.toString()};
        }

        if (value instanceof Number) {
            try {
                return new String[]{value.toString()};
            } catch (NumberFormatException e) {
                throw new TypeCastException(value, this, e);
            }
        }

        if (value instanceof Array) {
            try {
                Array a = (Array) value;
                return a.getArray();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (value instanceof Blob) {
            try {
                Blob blob = (Blob) value;
                byte[] blobValue = blob.getBytes(1, (int) blob.length());
                return typeCast(blobValue);
            } catch (SQLException e) {
                throw new TypeCastException(value, this, e);
            }
        }

        if (value instanceof Clob) {
            try {
                Clob clobValue = (Clob) value;
                int length = (int) clobValue.length();
                if (length > 0) {
                    return clobValue.getSubString(1, length);
                }
                return "";
            } catch (SQLException e) {
                throw new TypeCastException(value, this, e);
            }
        }

        log.warn("Unknown/unsupported object type '{}' - " +
                        "will invoke toString() as last fallback which " +
                        "might produce undesired results",
                value.getClass().getName());
        return value.toString();
    }

    @Override
    public Object getSqlValue(int column, ResultSet resultSet)
            throws SQLException, TypeCastException {
        if (log.isDebugEnabled())
            log.debug("getSqlValue(column={}, resultSet={}) - start", column, resultSet);

        String value = resultSet.getString(column);
        if (value == null || resultSet.wasNull()) {
            return null;
        }
        return value;
    }

    @Override
    public void setSqlValue(Object value, int column, PreparedStatement statement)
            throws SQLException, TypeCastException {
        if (log.isDebugEnabled())
            log.debug("setSqlValue(value={}, column={}, statement={}) - start",
                    value, column, statement);

        Array array = isNumber() ? statement.getConnection().createArrayOf("integer", toArray(value)) :
                statement.getConnection().createArrayOf("text", toArray(value));

        statement.setObject(column, array);
    }


    private Object[] toArray(Object value) {
        ArrayList<Object> list = new ArrayList<Object>(0);
        if (value instanceof String) {
            String valueStr = (String) value;
            if (!StringUtils.isEmpty(valueStr)) {
                valueStr = valueStr.replaceAll("[{}]", "");
                return valueStr.split(",");
            }
        }
        return list.toArray();

    }

}