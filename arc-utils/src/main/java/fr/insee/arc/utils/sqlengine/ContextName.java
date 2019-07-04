package fr.insee.arc.utils.sqlengine;

import javax.naming.CompoundName;

import fr.insee.arc.utils.sqlengine.model.IToken;
import fr.insee.arc.utils.textUtils.MapUntokenizer;


/**
 * A name belonging to a context. </br>
 * This interface allows the combination of a {@link Naming} with a {@link MapUntokenizer}.</br>
 * It extends {@link IToken}, implementations must return a name.
 *
 * As an implementation :
 * @see CompoundName
 */
public interface ContextName extends IToken {

    void setUntokenizer(MapUntokenizer<IToken, IToken> untokenizer);

    MapUntokenizer<IToken, IToken> getUntokenizer();

    void setNaming(Naming naming);

    Naming getNaming();

    ContextName deepClone();

}
