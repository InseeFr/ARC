package fr.insee.arc_essnet.utils.sqlengine;

import fr.insee.arc_essnet.utils.sqlengine.model.IAttribute;
import fr.insee.arc_essnet.utils.sqlengine.model.IToken;

/**
 *
 * La {@link ITable} est un type relationnel défini par un nom ({@link IToken})
 * et un modèle ({@link IModel}).
 */
public interface ITable<T extends IAttribute>
{
    /**
     *
     * @return le nom de cette table
     */
    IToken name();

    /**
     *
     * @return le modèle de cette table
     */
    IModel<T> model();

    int hashCode();

    boolean equals(Object obj);
}
