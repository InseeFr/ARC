package fr.insee.arc.utils.sqlengine.model;

import fr.insee.arc.utils.sqlengine.StringToken;

public class DefaultAttribute implements IAttribute
{
    private IToken name;
    private IType type;

    public DefaultAttribute(IToken aName, IType aType)
    {
        this.name = aName;
        this.type = aType;
    }

    public DefaultAttribute(String aName, String aType)
    {
        this(new StringToken(aName), new AttributeType(aType));
    }

    @Override
    public String name()
    {
        return this.name.name();
    }

    @Override
    public IType getSerializedType()
    {
        return this.type;
    }

    public String toString()
    {
        StringBuilder returned = new StringBuilder();
        returned.append("{");
        returned.append("name = " + this.name);
        returned.append(", ");
        returned.append("type = " + this.type);
        returned.append("}");
        return returned.toString();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
//        return ((name() == null) ? 0 : name().hashCode());
        return defaultHashCode();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
//        if (this == obj) { return true; }
//        if (obj == null) { return false; }
//        if (!(obj instanceof IAttribute)) { return false; }
//        IAttribute other = (IAttribute) obj;
//        if (name() == null)
//        {
//            if (other.name() != null) { return false; }
//        }
//        else if (!name().equals(other.name())) { return false; }
//        return true;
        
        if (this == obj) { return true; }
        if (obj == null) { return false; }
        if (!(obj instanceof IToken))
        {
            return false;
        }
        IToken asAttr = (IToken) obj;
        
        return this.name.equals(asAttr);
        
        
    }
}
