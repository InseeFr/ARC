package fr.insee.arc.utils.sqlengine;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fr.insee.arc.utils.sqlengine.model.IAttribute;

/**
 *
 *
 *
 */
public interface IModel<T extends IAttribute> extends Iterable<T>
{
    default T putAttribute(T attr)
    {
        T returned = modelAsMap().put(attr.name(), attr);
        if (returned == null)
        {
            modelAsList().add(attr);
        }
        else
        {
            int index = modelAsList().indexOf(attr);
            modelAsList().set(index, attr);
        }
        return returned;
    }

    default T removeAttribute(T attr)
    {
        T returned = modelAsMap().remove(attr.name());
        if (returned != null)
        {
            modelAsList().remove(attr);
        }
        return returned;
    }

    Map<String, T> modelAsMap();

    default IAttribute getAttribute(String id)
    {
        return modelAsMap().get(id);
    }

    List<T> modelAsList();

    default T getAttribute(int index)
    {
        return modelAsList().get(index);
    }

    default Iterator<T> iterator()
    {
        return modelAsList().iterator();
    }

    int hashCode();

    boolean equals(Object other);
}
