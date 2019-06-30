package fr.insee.arc.utils.dao;

public interface IDelegate<T>
{
    T getDelegate();
    
    void setDelegate(T t);
}
