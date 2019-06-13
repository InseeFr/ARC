package fr.insee.arc_essnet.utils.dao;

public interface IWrapper<T>
{
    T getWrapped();
    
    void setWrapped(T t);
}
