package fr.insee.arc_essnet.utils.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import fr.insee.arc_essnet.utils.dao.UtilitaireDao;

/**
 * This marker annotation marks the method which want to execute sql request in the stack to find them for logging. Make it easier for debug and maintenance
 * {@link UtilitaireDao#executeRequest(java.sql.Connection, String, fr.insee.arc_essnet.utils.dao.UtilitaireDao.EntityProvider, fr.insee.arc_essnet.utils.dao.ModeRequete...)}
 * @author Pépin Rémi
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SQLExecutor {

}
