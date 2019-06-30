package fr.insee.arc.utils.sqlengine.model;

import fr.insee.arc.utils.sqlengine.StringToken;

public class AttributeType implements IType
{

    private IToken name;

    public AttributeType(IToken name)
    {
        this.name = name;
    }

    public AttributeType(String aType)
    {
        this(new StringToken(aType));
    }

    @Override
    public IToken name()
    {
        return this.name;
    }

    public String toString()
    {
        return this.name.toString();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (!(obj instanceof AttributeType))
        {
            return false;
        }
        AttributeType other = (AttributeType) obj;
        if (this.name == null)
        {
            if (other.name != null)
            {
                return false;
            }
        }
        else if (!this.name.equals(other.name))
        {
            return false;
        }
        return true;
    }
}
