package fr.insee.arc_essnet.web;

import java.util.ArrayList;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {
        ArrayList<String> array = new ArrayList<String>();
        array.add("one");
        array.add("two");
        array.add("threee");
        array.add("four");
        array.add("five");
        array.listIterator(3);

        System.out.println("Hello World!");
        // String regex =
        // "({\\[ *[0-9]* *( *, *[0-9]* *)*\\]((([^\\{\\}]*\\{:([^\\{\\}]*(\\{:[^\\{\\}]*\\})?[^\\{\\}]*)*\\}[^\\{\\}]*)*)?(([^\\{\\}]*(\\{[^\\{\\}]*\\})?)*)?)*})*";
        // {[1]'oui'}{[2]'non'}{[3]'peut-être'}{[4]'ouf'}{[5]'être'}
        // {[3]'peut-être'}{[4]'ouf'}{[5]'être'}
        // {{1}'oui'}#{{2}'non'}#{{3}'peut-être'}#{{4}'ouf'}#{{5}'être'}
        // System.out.println("{{1}'oui'}{{2}'non'}{{3}{'peut-être'}".matches("^"+ApiMappingService.regexpSelectionGroupeRegleMultiple+"$"));
        // System.out.println("{{1} ".matches(ApiMappingService.regexpSelectionRegleLocaleAvecRubriqueEchappee));
        System.out.println(System.currentTimeMillis());
    }
}
