package fr.insee.arc.utils.textUtils;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Function;

/**
 *
 * From a collection of objects, builds a string representation by concatenating them. <br/>
 *
 * You must provide to this function:<br/>
 *
 * 1. a transformation function (for example {@code (t) -> t.toString()}),<br/>
 * 2. the first character of the resulting string,
 * 3. the last character of the resulting string,
 * 4. the separating character. <br/>
 *
 * The default implementation transforms an object collection {@code obj1, obj2, ..., objN} to a string representation
 * using {@code objI.toString()} with {@code ", "} as a separator.
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
