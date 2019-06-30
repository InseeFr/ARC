package fr.insee.arc.utils.sqlengine;

import org.springframework.beans.factory.annotation.Autowired;

import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;

public class ParallelizationSuffix
{
    
    @Autowired
    private static PropertiesHandler propertiesHandler;
    
    private static String TN = propertiesHandler.getTn();
    
    private static final ThreadLocal<String> INSTANCE = ThreadLocal.withInitial(() -> TN);

    public static final String get()
    {
        return INSTANCE.get();
    }
}