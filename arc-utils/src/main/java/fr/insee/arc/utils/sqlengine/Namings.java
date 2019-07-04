package fr.insee.arc.utils.sqlengine;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.format.datetime.joda.DateTimeFormatterFactory;

import fr.insee.arc.utils.sqlengine.model.DefaultAttribute;
import fr.insee.arc.utils.sqlengine.model.IAttribute;
import fr.insee.arc.utils.sqlengine.model.IToken;
import fr.insee.arc.utils.sqlengine.model.IType;
import fr.insee.arc.utils.sqlengine.model.PostgreSQLTypes;
import fr.insee.arc.utils.sqlengine.model.SQLTable;
import fr.insee.arc.utils.sqlengine.model.TempNameSuffix;
import fr.insee.arc.utils.sqlengine.model.TempNameSuffixShort;
import fr.insee.arc.utils.textUtils.Untokenizer;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.ManipString;

/**
 * Utility class for handling names sent to the SQL engine.teur SQL.
 */
public final class Namings {
    private static final Logger LOGGER = Logger.getLogger(Namings.class);
    private static final DateTimeFormatter DTF_TIMESTAMP = new DateTimeFormatterFactory("yyyyMMddHHmmss")
            .createDateTimeFormatter();

    private Namings() {
    }

    ;

    public static final IToken PREFIX_ID = new StringToken("id_");
    public static final BiFunction<String, String, Naming> GET_DEFAULT_NAMING = (schema,
                                                                                 name) -> new DefaultNaming(new HashMap<IToken, IToken>() {
        /**
         *
         */
        private static final long serialVersionUID = 4488191035625983135L;

        {
            this.put(Tokens.TOK_SCHEMA, new StringToken(schema));
            this.put(Tokens.TOK_TABLE_NAME, new StringToken(name));
        }
    });

    /**
     * @param schema
     * @param campaignId
     * @param sampleId
     * @param phaseName
     * @param name
     * @return ... PROCESSUS
     */
    public static final Naming getDefaultNamingProcessus(String schema, String campaignId, String sampleId,
                                                         String phaseName, String name) {
        return new DefaultNaming(//
                Tokens.TOK_SCHEMA, new StringToken(schema), //
                Tokens.TOK_CAMPAGNE, new StringToken(campaignId), //
                Tokens.TOK_SAMPLE, new StringToken(sampleId), //
                Tokens.TOK_NOM_PHASE, new StringToken(phaseName), //
                Tokens.TOK_TABLE_NAME, new StringToken(name));
    }

    public static final BiFunction<String, String, ContextName> GET_TABLE_NAME = (schema, name) -> new CompoundName(
            GET_DEFAULT_NAMING.apply(schema, name), SQLTable.SQL_QUALIFIED_TABLENAME_ASSEMBLER);

    public static final ContextName getDefaultTableNameProcessus(String schema, String idCampagne, String idSample,
                                                                 String nomPhase, String name) {
        return new CompoundName(getDefaultNamingProcessus(schema, idCampagne, idSample, nomPhase, name),
                SQLTable.SQL_QUALIFIED_TABLENAME_ASSEMBLER);
    }

    /**
     * @param naming
     * @param key
     * @param value
     * @return
     */
    public static final Naming cloneAndChange(Naming naming, IToken key, IToken value) {
        Naming returned = naming.deepClone();
        returned.tokens().put(key, value);
        return returned;
    }

    /**
     * @param contextName
     * @param key
     * @param value
     * @return
     */
    public static final ContextName cloneAndChange(ContextName contextName, IToken key, IToken value) {
        ContextName returned = contextName.deepClone();
        returned.getNaming().tokens().put(key, value);
        return returned;
    }

    /**
     * Modifie un sous morceau d'un token.
     *
     * @param contextName
     * @param key
     * @param regexPatternBefore
     * @param after
     * @return
     */
    public static ContextName cloneAndReplaceWithin(ContextName contextName, IToken key, String regexPatternBefore,
                                                    String after) {
        String newName = contextName.getNaming().tokens().get(key).name().replaceAll(regexPatternBefore, after);
        return Namings.cloneAndChange(contextName, key, new StringToken(newName));
    }

    public static final String stringTemporaryName(String aPrefix, String anExtension) {
        return aPrefix + "_" + new DateTime().toString(DTF_TIMESTAMP) + "." + anExtension;
    }

    /**
     * @param aName
     * @return Un nouveau {@link ContextName} dont le {@link Naming} a été
     * augmenté/remplacé d'un suffixe d'identifiant temporaire et dont
     * le {@link Untokenizer} peut calculer le nom.
     */
    public static final ContextName newAsWithTemporarySuffix(ContextName aName) {
        LoggerHelper.trace(LOGGER, "Création d'un nouveau nom depuis", aName.name());
        ContextName returned = cloneAndChange(aName, Tokens.TOK_SUFFIX_TEMP, new TempNameSuffix());
        returned.setUntokenizer(SQLTable.TO_TEMPORARY.apply(returned.getUntokenizer()));
        return returned;
    }


    /**
     * @param aName
     * @return Un nouveau {@link ContextName} dont le {@link Naming} a été
     * augmenté/remplacé d'un suffixe d'identifiant temporaire et dont
     * le {@link Untokenizer} peut calculer le nom.
     */
    public static final ContextName newAsWithTemporarySuffixShort(ContextName aName) {
        LoggerHelper.trace(LOGGER, "Création d'un nouveau nom depuis", aName.name());
        ContextName returned = cloneAndChange(aName, Tokens.TOK_SUFFIX_TEMP, new TempNameSuffixShort());
        returned.setUntokenizer(SQLTable.TO_TEMPORARY.apply(returned.getUntokenizer()));
        return returned;
    }


    /**
     * @param aName
     * @return Un nouveau {@link ContextName} sans schéma dont le {@link Naming}
     * a été augmenté/remplacé d'un suffixe d'identifiant temporaire et
     * dont le {@link Untokenizer} peut calculer le nom.
     */
    public static final ContextName newAsTemporary(ContextName aName) {
        return cloneAndChange(newAsWithTemporarySuffix(aName), Tokens.TOK_SCHEMA, null);
    }

    /**
     * Générer un nom de {@link TechnicalId} à partir du nom de la table
     *
     * @param aName
     * @return
     */
    public static final ContextName newIdNameFrom(ContextName aName) {
        LoggerHelper.trace(LOGGER, "Création d'un nouveau nom depuis", aName.name());
        ContextName returned = cloneAndChange(aName, Tokens.TOK_PREFIX, PREFIX_ID);
        returned.setUntokenizer(SQLTable.SQL_TECHNICALID_ASSEMBLER);
        return returned;
    }

    /**
     * @return un nom d'id temporaire avec un timestamp sur mesure
     */
    public static IAttribute newTemporaryId() {
        return new DefaultAttribute(new StringToken(PREFIX_ID.name() + new TempNameSuffix()), PostgreSQLTypes.BIGINT);
    }

    /**
     * @param aTableName
     * @return le nom d'id correspondant au timestamp de {@code aTableName}
     */
    public static IAttribute newTemporaryId(ContextName aTableName) {
        return newTemporaryName(aTableName, "id", PostgreSQLTypes.BIGINT);
    }

    public static IAttribute newTemporaryUnnestOrder(ContextName aTableName) {
        return newTemporaryName(aTableName, "unnest_order", PostgreSQLTypes.BIGINT);
    }

    /**
     *
     */
    public static Function<Supplier<ContextName>, Supplier<IAttribute>> NEW_TEMPORARY_ORDER = (
            t) -> () -> newTemporaryName(t.get(), "order", PostgreSQLTypes.BIGINT);

    public static IAttribute newTemporaryOrder(ContextName aTableName) {
        return newTemporaryName(aTableName, "order", PostgreSQLTypes.BIGINT);
    }

    public static StringToken newTemporaryIndex(ContextName aTableName) {
        return newTemporaryName(aTableName, "idx");
    }

    public static IAttribute newTemporaryClassId(ContextName aTableName) {
        return newTemporaryName(aTableName, "class_id", PostgreSQLTypes.BIGINT);
    }

    public static IAttribute newTemporaryClassOrder(ContextName aTableName) {
        return newTemporaryName(aTableName, "class_order", PostgreSQLTypes.BIGINT);
    }

    /**
     * Crée un attribut dont le suffixe est celui de {@code aName}
     *
     * @param aName
     * @param prefix
     * @param type
     * @return
     */
    public static IAttribute newTemporaryName(ContextName aName, String prefix, IType type) {
        LoggerHelper.trace(LOGGER, "Création d'un nouveau nom depuis", aName.name());
        return new DefaultAttribute(newTemporaryName(aName, prefix), type);
    }

    /**
     * @param aName
     * @param prefix
     * @return
     */
    private static StringToken newTemporaryName(ContextName aName, String prefix) {
        return new StringToken(prefix + "_" + aName.getNaming().get(Tokens.TOK_SUFFIX_TEMP));
    }

    /**
     * Renvoie le nom de la table bien qualifiée.</br>
     * Attention,</br>
     * si <code>aTableName</code> contient un nom qualifié (schema+nom), </br>
     * le schema par défaut est inutile.
     *
     * @param aSchema    le schema par défaut
     * @param aTableName
     * @return
     */
    public static ContextName tableTransformation(IToken aSchema, String aTableName) {
        Map<IToken, IToken> map = new HashMap<IToken, IToken>();
        /*
         * Le nom contient-il déjà un schema
         */
        IToken zeSchema = (!aTableName.contains(".")) ? aSchema : new StringToken(ManipString.substringBeforeFirst(aTableName, "."));
        IToken zeTableName = (!aTableName.contains(".")) ? new StringToken(aTableName) : new StringToken(ManipString.substringAfterFirst(aTableName, "."));
        map.put(Tokens.TOK_SCHEMA, zeSchema);
        map.put(Tokens.TOK_TABLE_NAME, zeTableName);

//        map.put(Tokens.TOK_SCHEMA, aSchema);
//        map.put(Tokens.TOK_TABLE_NAME, new StringToken(aTableName));


        return new CompoundName(new DefaultNaming(map), SQLTable.SQL_QUALIFIED_TABLENAME_ASSEMBLER);
    }


}
