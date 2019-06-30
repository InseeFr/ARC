package fr.insee.arc.utils.utils;

public class Pair<E, F> implements IPair<E, F>
{
    private E first;
    private F second;

    public Pair(E aFirst, F aSecond)
    {
        this.first = aFirst;
        this.second = aSecond;
    }

    /**
     * @return the first
     */
    public final E getFirst()
    {
        return this.first;
    }

    /**
     * @param first
     *            the first to set
     */
    public final void setFirst(E first)
    {
        this.first = first;
    }

    /**
     * @return the second
     */
    public final F getSecond()
    {
        return this.second;
    }

    /**
     * @param second
     *            the second to set
     */
    public final void setSecond(F second)
    {
        this.second = second;
    }

    public Pair<E, F> deepClone()
    {
        return new Pair<>(this.getFirst(), this.getSecond());
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.first == null) ? 0 : this.first.hashCode());
        result = prime * result + ((this.second == null) ? 0 : this.second.hashCode());
        return result;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) { return true; }
        if (obj == null) { return false; }
        if (!(obj instanceof Pair)) { return false; }
        Pair<?, ?> other = (Pair<?, ?>) obj;
        if (this.first == null)
        {
            if (other.first != null) { return false; }
        }
        else if (!this.first.equals(other.first)) { return false; }
        if (this.second == null)
        {
            if (other.second != null) { return false; }
        }
        else if (!this.second.equals(other.second)) { return false; }
        return true;
    }

    public String toString()
    {
        return new StringBuilder("(" + this.getFirst() + ", " + this.getSecond() + ")").toString();
    }
}
