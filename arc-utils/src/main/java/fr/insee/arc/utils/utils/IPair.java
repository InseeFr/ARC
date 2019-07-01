package fr.insee.arc.utils.utils;

public interface IPair<E, F>
{
    /**
     * @return the first
     */
    E getFirst();

    /**
     * @param first
     *            the first to set
     */
    void setFirst(E first);

    /**
     * @return the second
     */
    F getSecond();

    /**
     * @param second
     *            the second to set
     */
    void setSecond(F second);
}
