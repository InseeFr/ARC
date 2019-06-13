package fr.insee.arc_essnet.utils.dao;

public interface IDelegate<T>
{
    T getDelegate();
    
    void setDelegate(T t);
}
