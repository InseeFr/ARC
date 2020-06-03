package fr.insee.arc.utils.utils;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import fr.insee.arc.utils.textUtils.IConstanteCaractere;

/**
 *
 * 1. Créez un {@link Logger} dans votre classe.<br/>
 * 2. Au lieu de
 * <code>if LOGGER.isXXXXEnabled()<br/>{<br/>LOGGER.XXXX(des trucs);<br/>}</code>
 * écrire :<br/>
 * <code>LoggerHelper.XXXX(LOGGER, des trucs);</code><br/>
 * Fonctionne avec tous niveaux de log, et s'occupe de la concaténation tardive
 * des bouts de chaînes.<br/>
 * Exemple :<br/>
 * <code>LoggerHelper.debug(LOGGER, t, "Problème", unObjet, "est mal instancié"</code>
 * équivaut à l'appel :<br/>
 * <code>if (LOGGER.isDebugEnabled())<br/>{LOGGER.debug("Problème"+" "+unObjet+" "+"est mal instancié", t);<br/>}</code>
 */
public class LoggerHelper
{
    /**
     * @see Logger#log(Priority, Object)
     * @param aLogger
     * @param aPriority
     * @param tokens
     */
    public static final void log(Logger aLogger, Priority aPriority, Object... tokens)
    {
        if (aLogger.isEnabledFor(aPriority))
        {
            aLogger.log(aPriority, concat(tokens));
        }
    }

    /**
     * @see Logger#log(Priority, Object)
     * @param aLogger
     * @param aPriority
     * @param t
     * @param tokens
     */
    public static final void log(Logger aLogger, Priority aPriority, Throwable t, Object... tokens)
    {
        if (aLogger.isEnabledFor(aPriority))
        {
            aLogger.log(aPriority, concat(tokens), t);
        }
    }

    private static final StringBuilder concat(Object... tokens)
    {
        StringBuilder returned = new StringBuilder();
        for (int i = 0; i < tokens.length; i++)
        {
            if (i > 0)
            {
                returned.append(IConstanteCaractere.space);
            }
            returned.append(tokens[i]);
        }
        return returned;
    }

    /**
     * Préférer le formattage du message dans les paramètres de
     *             configuration des appender dans log4j.xml
     * @see Logger#log(Priority, Object, Throwable)
     * @param aLogger
     * @param aPriority
     * @param tokens
     */
    public static final void logAsComment(Logger aLogger, Priority aPriority, Object... tokens)
    {
        if (aLogger.isEnabledFor(aPriority))
        {
            log(aLogger, aPriority, "/* ", concat(tokens).toString(), " */");
        }
    }

    /**
     * Préférer le formattage du message dans les paramètres de
     *             configuration des appender dans log4j.xml
     * @see Logger#log(Priority, Object, Throwable)
     * @param aLogger
     * @param aPriority
     * @param t
     * @param tokens
     */
    public static final void logAsComment(Logger aLogger, Priority aPriority, Throwable t, Object... tokens)
    {
        if (aLogger.isEnabledFor(aPriority))
        {
            log(aLogger, aPriority, t, "/* ", concat(tokens).toString(), " */");
        }
    }

    /**
     * @see Logger#trace(Object)
     * @param aLogger
     * @param tokens
     */
    public static final void trace(Logger aLogger, Object... tokens)
    {
        log(aLogger, Level.TRACE, tokens);
    }

    /**
     * Préférer le formattage du message dans les paramètres de
     *             configuration des appender dans log4j.xml
     * @see Logger#trace(Object)
     * @param aLogger
     * @param tokens
     */
    public static final void traceAsComment(Logger aLogger, Object... tokens)
    {
        logAsComment(aLogger, Level.TRACE, tokens);
    }

    /**
     * @see Logger#trace(Object, Throwable)
     * @param aLogger
     * @param t
     * @param tokens
     */
    public static final void trace(Logger aLogger, Throwable t, Object... tokens)
    {
        log(aLogger, Level.TRACE, t, tokens);
    }

    /**
     * Préférer le formattage du message dans les paramètres de
     *             configuration des appender dans log4j.xml
     * @see Logger#trace(Object, Throwable)
     * @param aLogger
     * @param t
     * @param tokens
     */
    public static final void traceAsComment(Logger aLogger, Throwable t, Object... tokens)
    {
        logAsComment(aLogger, Level.TRACE, t, tokens);
    }

    /**
     * @see Logger#debug(Object)
     * @param aLogger
     * @param tokens
     */
    public static final void debug(Logger aLogger, Object... tokens)
    {
        log(aLogger, Level.DEBUG, tokens);
    }

    /**
     * Préférer le formattage du message dans les paramètres de
     *             configuration des appender dans log4j.xml
     * @see Logger#debug(Object)
     * @param aLogger
     * @param tokens
     */
    public static final void debugAsComment(Logger aLogger, Object... tokens)
    {
        logAsComment(aLogger, Level.DEBUG, tokens);
    }

    /**
     *
     * @param aLogger
     * @param t
     * @param tokens
     */
    public static final void debug(Logger aLogger, Throwable t, Object... tokens)
    {
        log(aLogger, Level.DEBUG, t, tokens);
    }

    /**
     * Préférer le formattage du message dans les paramètres de
     *             configuration des appender dans log4j.xml
     * @param aLogger
     * @param t
     * @param tokens
     */
    public static final void debugAsComment(Logger aLogger, Throwable t, Object... tokens)
    {
        logAsComment(aLogger, Level.DEBUG, t, tokens);
    }

    /**
     * Préférer le formattage du message dans les paramètres de
     *             configuration des appender dans log4j.xml
     * @param clazz
     * @param method
     * @param aLogger
     */
    public static void debugDebutMethodeAsComment(Class<?> clazz, String method, Logger aLogger)
    {
        logAsComment(aLogger, Level.DEBUG, clazz.getCanonicalName(), IConstanteCaractere.sharp, method, "(début)");
    }

    /**
     * Préférer le formattage du message dans les paramètres de
     *             configuration des appender dans log4j.xml
     * @param clazz
     * @param method
     * @param aLogger
     */
    public static void debugFinMethodeAsComment(Class<?> clazz, String method, Logger aLogger)
    {
        logAsComment(aLogger, Level.DEBUG, clazz.getCanonicalName(), IConstanteCaractere.sharp, method, "(fin)");
    }

    /**
     *
     * @param aLogger
     * @param tokens
     */
    public static final void info(Logger aLogger, Object... tokens)
    {
        log(aLogger, Level.INFO, tokens);
    }

    /**
     * Préférer le formattage du message dans les paramètres de
     *             configuration des appender dans log4j.xml
     * @param aLogger
     * @param tokens
     */
    public static final void infoAsComment(Logger aLogger, Object... tokens)
    {
        logAsComment(aLogger, Level.INFO, tokens);
    }

    /**
     *
     * @param aLogger
     * @param t
     * @param tokens
     */
    public static final void info(Logger aLogger, Throwable t, Object... tokens)
    {
        log(aLogger, Level.INFO, t, tokens);
    }

    /**
     * Préférer le formattage du message dans les paramètres de
     *             configuration des appender dans log4j.xml
     * @param aLogger
     * @param t
     * @param tokens
     */
    public static final void infoAsComment(Logger aLogger, Throwable t, Object... tokens)
    {
        logAsComment(aLogger, Level.INFO, t, tokens);
    }

    /**
     *
     * @param aLogger
     * @param tokens
     */
    public static final void warn(Logger aLogger, Object... tokens)
    {
        log(aLogger, Level.WARN, tokens);
    }

    /**
     * Préférer le formattage du message dans les paramètres de
     *             configuration des appender dans log4j.xml
     * @param aLogger
     * @param tokens
     */
    public static final void warnAsComment(Logger aLogger, Object... tokens)
    {
        logAsComment(aLogger, Level.WARN, tokens);
    }

    /**
     *
     * @param aLogger
     * @param t
     * @param tokens
     */
    public static final void warn(Logger aLogger, Throwable t, Object... tokens)
    {
        log(aLogger, Level.WARN, t, tokens);
    }

    /**
     * Préférer le formattage du message dans les paramètres de
     *             configuration des appender dans log4j.xml
     * @param aLogger
     * @param t
     * @param tokens
     */
    public static final void warnAsComment(Logger aLogger, Throwable t, Object... tokens)
    {
        logAsComment(aLogger, Level.WARN, t, tokens);
    }

    /**
     *
     * @param aLogger
     * @param tokens
     */
    public static final void error(Logger aLogger, Object... tokens)
    {
        log(aLogger, Level.ERROR, tokens);
    }

    /**
     * Préférer le formattage du message dans les paramètres de
     *             configuration des appender dans log4j.xml
     * @param aLogger
     * @param tokens
     */
    public static final void errorAsComment(Logger aLogger, Object... tokens)
    {
        logAsComment(aLogger, Level.ERROR, tokens);
    }

    /**
     *
     * @param aLogger
     * @param t
     * @param tokens
     */
    public static final void error(Logger aLogger, Throwable t, Object... tokens)
    {
        log(aLogger, Level.ERROR, t, tokens);
    }

    /**
     * Préférer le formattage du message dans les paramètres de
     *             configuration des appender dans log4j.xml
     * @param aLogger
     * @param t
     * @param tokens
     */
    public static final void errorAsComment(Logger aLogger, Throwable t, Object... tokens)
    {
        logAsComment(aLogger, Level.ERROR, t, tokens);
    }

    /**
     * Préférer le formattage du message dans les paramètres de
     *             configuration des appender dans log4j.xml
     * @param aLogger
     * @param t
     * @param tokens
     */
    public static final void errorGenTextAsComment(Class<?> clazz, String method, Logger aLogger, Throwable t)
    {
        logAsComment(aLogger, Level.ERROR, t, "Lors de l'exécution de", clazz.getCanonicalName(),
                IConstanteCaractere.sharp, method);
    }
}
