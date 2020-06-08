package fr.insee.arc.utils.sqlengine.model;

import fr.insee.arc.utils.utils.FormatSQL;

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

    public TempNameSuffixShort()
    {
        this.name = new StringBuilder().append(FormatSQL._TMP).toString();
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
