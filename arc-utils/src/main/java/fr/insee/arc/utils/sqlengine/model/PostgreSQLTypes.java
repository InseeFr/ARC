package fr.insee.arc.utils.sqlengine.model;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import fr.insee.arc.utils.textUtils.IConstanteCaractere;

public class PostgreSQLTypes
{
    private static final Map<IType, Function<String, String>> HOW_TO_PRINT = new HashMap<IType, Function<String, String>>();
    public static final String VALUE_NULL = "NULL";
    public static final String OPERATOR_CAST = "::";
    public static final IType BIGINT = new AttributeType("BIGINT");
    public static final IType TEXT = new AttributeType("TEXT");
    public static final IType DATE = new AttributeType("DATE");
    public static final IType UNKNOWN = new AttributeType("UNKNOWN");
    public static final IType BOOLEAN = new AttributeType("BOOLEAN");
    @Deprecated
    public static final IType CHANGE_THIS_TO_EFFECTIVE_TYPE_PLEASE = new AttributeType("TEXT");

    /**
     * @return a map which keys are types and values are functions used to properly
     * write the value.
     *
     */
    public static final Map<IType, Function<String, String>> prettySQLPrinter()
    {
        if (HOW_TO_PRINT.isEmpty())
        {
            fillHowToPrint();
        }
        return HOW_TO_PRINT;
    }

    private static void fillHowToPrint()
    {
        HOW_TO_PRINT.put(TEXT,
                (t) -> (t.equalsIgnoreCase(VALUE_NULL) ? VALUE_NULL
                        : IConstanteCaractere.QUOTE + t + IConstanteCaractere.QUOTE) + OPERATOR_CAST
                        + TEXT.name().name());
        HOW_TO_PRINT.put(DATE,
                (t) -> (t.equalsIgnoreCase(VALUE_NULL) ? VALUE_NULL
                        : IConstanteCaractere.QUOTE + t + IConstanteCaractere.QUOTE) + OPERATOR_CAST
                        + DATE.name().name());
        HOW_TO_PRINT.put(BIGINT, (t) -> t + OPERATOR_CAST + BIGINT.name().name());
    }
}
