package fr.insee.arc_essnet.utils.textUtils;

import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

public class XMLUtil
{
    public static final int DEFAULT_INDENT_SIZE = 2;
    private String indent = StringUtils.repeat(" ", DEFAULT_INDENT_SIZE);
    private int level = 0;
    private StringBuilder returned = new StringBuilder();
    private Stack<String> names = new Stack<String>();

    public XMLUtil(int anIndent)
    {
        this.indent = StringUtils.repeat(" ", anIndent);
    }

    public XMLUtil(int anIndent, int level)
    {
        this(anIndent);
        this.level = level;
    }

    public void startElement(String name)
    {
        this.level++;
        this.names.push(name);
        indent();
        append("<");
        append(name);
        append(">");
    }

    public void startElement(String name, List<Pair<String, String>> attrs)
    {
        this.level++;
        this.names.push(name);
        indent();
        append("<");
        append(name);
        attrs(attrs);
        append(">");
    }

    public void cdata(String data)
    {
        append(data);
    }

    public void endElement()
    {
        indent();
        append("<");
        append(this.names.pop());
        append("/>");
    }

    private void attrs(List<Pair<String, String>> attrs)
    {
        if (attrs == null || attrs.isEmpty()) { return; }
        append(" " + attrs.stream().map(t -> attr(t.getKey(), t.getValue())).collect(Collectors.joining(" ")));
    }

    private static String attr(String key, String value)
    {
        return key + "=\"" + value + "\"";
    }

    public void append(String string)
    {
        this.returned.append(string);
    }

    private void indent()
    {
        append("\n" + StringUtils.repeat(this.indent, this.level));
    }

    public String toString()
    {
        return this.returned.toString();
    }

    public void emptyElement(String name)
    {
        indent();
        append("<");
        append(name);
        append("/>");
    }

    /**
     * @return the level
     */
    public final int getLevel()
    {
        return this.level;
    }
}
