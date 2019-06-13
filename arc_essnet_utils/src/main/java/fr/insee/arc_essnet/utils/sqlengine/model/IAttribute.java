package fr.insee.arc_essnet.utils.sqlengine.model;

public interface IAttribute extends IToken
{
    /**
     *
     * @return le type sérialisé, qui dépend donc du support pour la
     *         sérialisation (Base de données, fichier, flux ...)
     */
    IType getSerializedType();

    default String serializedTypeName()
    {
        return getSerializedType().name().name();
    }
}
