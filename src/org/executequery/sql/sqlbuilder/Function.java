package org.executequery.sql.sqlbuilder;

import java.util.ArrayList;
import java.util.List;

public class Function {

    String name;
    List<String> arguments;

    public static Function createFunction() {
        return new Function();
    }

    public static Function createFunction(String name) {
        return createFunction().setName(name);
    }

    public String getName() {
        return name;
    }

    public Function setName(String name) {
        this.name = name;
        return this;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }

    public Function setArgument(int index, String argument) {
        arguments.set(index, argument);
        return this;
    }

    public Function appendArgument(String argument) {
        if (arguments == null)
            arguments = new ArrayList<>();
        arguments.add(argument);
        return this;
    }

    public String getStatement() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("(");
        if (arguments != null) {
            boolean first = true;
            for (String arg : arguments) {
                if (!first)
                    sb.append(", ");
                first = false;
                sb.append(arg);
            }

        }
        sb.append(")");
        return sb.toString();
    }
}
