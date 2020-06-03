package fr.insee.arc.utils.sqlengine;

import java.util.Map;

import fr.insee.arc.utils.sqlengine.model.IToken;

/**
 *
 * Provides naming elements for an object. These elements are uniquely identified:
 *
 * 1. keys are ids for name parts (and are ordered)
 * 2. values are name parts
 *
 * For example, a {@code Naming} used for a relational table could be:
 * <code>"schema" ::= "sch", "separator" ::= ".", "table_name" ::= "my_table"</code>
 *
 * A name is made of the aggregation of multiple tokens.
 *
 * This aggregation is obtained through concatenation ou other methods implemented by
 * the class using {@code Naming}.
 *
 * For example:
 *
 * 1. for a token list of <code>{"table_name}</code>, the generated name
 *    will be <code>"table_name"</code>
 * 2. for a token list of <code>{"schema", "separator", "table_name"}</code>,
 *    the generated name will be <code>"sch.my_table</code>
 *
 * We use this method to easily get object names through predefined and ordered template.
 *
 * @see DefaultNaming
 *
 */
public interface Naming {

    Map<IToken, IToken> tokens();

    IToken get(IToken token);

    void set(IToken key, IToken value);

    Integer size();

    Naming clone();

}
