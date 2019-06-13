package fr.insee.arc_essnet.utils.sqlengine;

import fr.insee.config.InseeConfig;

public class ParallelizationSuffix
{
    private static final String TN = InseeConfig.getConfig().getString("fr.insee.siera.sqlengine.parallelize.suffix",
            "$TN");
    private static final ThreadLocal<String> INSTANCE = ThreadLocal.withInitial(() -> TN);

    public static final String get()
    {
        return INSTANCE.get();
    }
}
