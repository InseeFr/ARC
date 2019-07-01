package fr.insee.arc.utils.sqlengine;

import javax.naming.CompoundName;

import fr.insee.arc.utils.sqlengine.model.IToken;
import fr.insee.arc.utils.textUtils.MapUntokenizer;


/**
 *
 * Un nom lié à un contexte.</br>
 * Interface permettant de combiner un {@link Naming} avec son {@link MapUntokenizer}.</br>
 * Comme elle étend {@link IToken}, les implémentations doivent pouvoir renvoyer un nom.</br>
 * 
 * Pour une implémentation
 * @see CompoundName
 */
public interface ContextName extends IToken {

    void setUntokenizer(MapUntokenizer<IToken, IToken> untokenizer);

    MapUntokenizer<IToken, IToken> getUntokenizer();

    void setNaming(Naming naming);

    Naming getNaming();

    ContextName deepClone();

}
