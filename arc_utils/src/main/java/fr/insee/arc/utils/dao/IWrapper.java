package fr.insee.arc.utils.dao;

public interface IWrapper<T>
{
    T getWrapped();
    
    void setWrapped(T t);
}
