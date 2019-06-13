package fr.insee.arc_essnet.utils.sqlengine;

import java.util.Map;

import fr.insee.arc_essnet.utils.sqlengine.model.IToken;

/**
 *
 * Fournit les éléments de nommage d'un objet. Ces éléments sont identifiés de
 * manière unique :<br/>
 * 1. Les clés sont des identifiants de parties de noms, ordonnées.<br/>
 * 2. Les valeurs sont les parties de noms.<br/>
 * Par exemple un {@code Naming} pour une table relationnelle pourra être :<br/>
 * <code>"schema" ::= "sch", "separateur" ::= ".", "nom_table" ::= "ma_table"</code>
 * <br/>
 * Le nom est obtenu (par ailleurs) par agrégation de certains des tokens.
 * L'agrégation peut être obtenue par concaténation ou par une autre méthode
 * implémentée dans la classe utilisatrice de {@code Naming}.<br/>
 * Par exemple :<br/>
 * 1. Si la liste des tokens est <code>{"nom_table"}</code>, le nom généré est
 * simplement <code>"ma_table"</code>.<br/>
 * 2. Si la liste des tokens est
 * <code>{"nom_table", "separateur", "nom_table"}</code>, le nom obtenu pourra
 * être <code>"sch.ma_table"</code>.<br/>
 * L'intérêt de cette méthode est de pouvoir obtenir facilement des noms selon
 * un patron prédéfini et ordonné.
 * Pour une implémentation
 * @see DefaultNaming
 */
public interface Naming {

    Map<IToken, IToken> tokens();

    IToken get(IToken token);

    void set(IToken key, IToken value);

    Integer size();

    Naming deepClone();

}
