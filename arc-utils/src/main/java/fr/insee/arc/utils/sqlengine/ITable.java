package fr.insee.arc.utils.sqlengine;

import fr.insee.arc.utils.sqlengine.model.IAttribute;
import fr.insee.arc.utils.sqlengine.model.IToken;

/**
 * {@link ITable} describes a relational type defined by a name ({@link IToken}) and a model (({@link IModel}))
 */
public interface ITable<T extends IAttribute>
{
    /**
     *
     * @return the table name
     */
    IToken name();

    /**
     *
     * @return the table model
     */
    IModel<T> model();

    int hashCode();

    boolean equals(Object obj);
}
