package fr.insee.arc.utils.sqlengine.model;

import org.apache.commons.lang3.StringUtils;
import fr.insee.arc.utils.utils.FormatSQL;

/**
 *
 * Builds a tuple with:
 *
 * 1. current thread identifier
 * 2. unique sequential number for a given thread
 *
 */
public class TempNameSuffix implements IToken
{
    private final String name;
    private static ThreadLocal<Integer> rank = ThreadLocal.withInitial(() -> 0);

    public TempNameSuffix()
    {
        this.name = new StringBuilder().append(FormatSQL._TMP).append(Thread.currentThread().getId()).append("$")
                .append(StringUtils.leftPad(String.valueOf(rank.get()), 4, '0')).toString();
        rank.set(rank.get() + 1);
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
