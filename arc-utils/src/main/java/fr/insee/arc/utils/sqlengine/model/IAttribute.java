package fr.insee.arc.utils.sqlengine.model;

public interface IAttribute extends IToken {
    /**
     * @return the serialization of the type which depends on the target (database, file, stream...)
     */
    IType getSerializedType();

    default String serializedTypeName() {
        return getSerializedType().name().name();
    }
}
