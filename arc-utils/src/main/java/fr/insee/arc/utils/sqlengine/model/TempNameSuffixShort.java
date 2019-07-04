package fr.insee.arc.utils.sqlengine.model;

/**
 *
 * Builds a tuple with:
 *
 * 1. current thread identifier
 * 2. unique sequential number for a given thread
 *
 */
public class TempNameSuffixShort implements IToken
{
    private final String name;
    private static ThreadLocal<Integer> rank = ThreadLocal.withInitial(() -> 0);

    public TempNameSuffixShort()
    {
        this.name = new StringBuilder().append("$tmp$").toString();
    }
    
    @Override
    public String name()
    {
        return this.name;
    }

    public String toString()
    {
        return name();
    }
}
