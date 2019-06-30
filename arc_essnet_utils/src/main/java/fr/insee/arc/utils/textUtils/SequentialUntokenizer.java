package fr.insee.arc.utils.textUtils;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Function;

/**
 * Parcourt une collection d'objets et construit une chaîne de caractère en concaténant la représentation de tous ces objets. Les éléments à
 * fournir sont :<br/>
 * 1. Une fonction qui transforme l'objet en sa représentation (par exemple {@code (t) -> t.toString()}).<br/>
 * 2. Le début de la chaîne de caractère en sortie.<br/>
 * 3. La fin de la chaîne de caractère en sortie.<br/>
 * 4. Le token qui sera inséré entre deux objets concaténés.<br/>
 * Une implémentation par défaut transforme une collection d'objets {@code obj1, obj2, ..., objN} en la concaténation des
 * {@code objI.toString()} séparés par {@code ", "}.
 *
 * @param <T>
 */
public class SequentialUntokenizer<T> implements Untokenizer<T> {

    public final Function<T, String> toStringOperator = new Function<T, String>() {
        @Override
        public String apply(T input) {
            return input.toString();
        }
    };

    private Function<T, String> onToken;
    private String beforeAll;
    private String betweenTokens;
    private String afterAll;

    public SequentialUntokenizer(String beforeAll, String betweenTokens, String afterAll) {
        this.beforeAll = beforeAll;
        this.betweenTokens = betweenTokens;
        this.afterAll = afterAll;
        this.onToken = this.toStringOperator;
    }

    public SequentialUntokenizer() {
        this(EMPTY_STRING, ", ", EMPTY_STRING);
    }

    public SequentialUntokenizer(Function<T, String> onToken, String beforeAll, String betweenTokens, String afterAll) {
        this(beforeAll, betweenTokens, afterAll);
        this.onToken = onToken;
    }

    @Override
    public String untokenize(Collection<? extends T> objects) {
        return untokenize(objects.iterator());
    }

    public String untokenize(Iterator<? extends T> iterator) {
        StringBuilder returned = new StringBuilder();
        returned.append(this.beforeAll);
        boolean isFirst = true;
        while (iterator.hasNext()) {
            if (isFirst) {
                isFirst = false;
            } else {
                returned.append(this.betweenTokens);
            }
            returned.append(this.onToken.apply(iterator.next()));
        }
        returned.append(this.afterAll);
        return returned.toString();
    }
}
