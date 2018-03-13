package org.underworldlabs.sqlParser;

import org.executequery.gui.editor.autocomplete.Parameter;

import java.util.ArrayList;
import java.util.List;

public class SqlParser {
    private static final int DEFAULT_STATE = 0;
    private static final int QUOTE_STATE = 1;
    private static final int COMMENT_LINE_STATE = 2;
    private static final int COMMENT_MULTILINE_STATE = 3;
    private static final int ARRAY_STATE = 4;
    private static final int PARAMETER_STATE = 6;
    private static final int EXECUTE_BLOCK = 7;
    List<Parameter> parameters;
    List<Parameter> displayParameters;
    String processedSql;
    private boolean execute_block;

    public SqlParser(String sql) {
        execute_block = false;
        StringBuilder sb = new StringBuilder(sql);
        StringBuilder processed = new StringBuilder();
        displayParameters = new ArrayList<>();
        StringBuilder parameter = new StringBuilder();
        parameters = new ArrayList<>();
        int state = DEFAULT_STATE;
        int len = sql.length();
        boolean first = true;
        Character openChar = null;
        for (int i = 0; i < len; i++) {
            char curChar = sb.charAt(i);
            Character nextChar = null;
            if (i + 1 < len - 1)
                nextChar = sb.charAt(i + 1);
            switch (state) {
                case DEFAULT_STATE:
                    switch (curChar) {
                        case '\'':
                        case '"':
                            processed.append(curChar);
                            state = QUOTE_STATE;
                            openChar = curChar;
                            first = false;
                            break;
                        case '-':
                            if (nextChar == '-') {
                                state = COMMENT_LINE_STATE;
                            }
                            processed.append(curChar);
                            break;
                        case '/':
                            if (nextChar == '*') {
                                state = COMMENT_MULTILINE_STATE;
                            }
                            processed.append(curChar);
                            break;
                        case '[':
                            processed.append(curChar);
                            state = ARRAY_STATE;
                            first = false;
                            break;
                        case '?':
                            Parameter p = new Parameter("№" + (displayParameters.size() + 1));
                            parameters.add(p);
                            displayParameters.add(p);
                            processed.append(curChar);
                            first = false;
                            break;
                        case ':':
                            processed.append('?');
                            state = PARAMETER_STATE;
                            parameter.setLength(0);
                            first = false;
                            break;
                        case 'e':
                        case 'E':
                            if (first) {
                                state = EXECUTE_BLOCK;
                            }
                            processed.append(curChar);
                            first = false;
                            break;
                        default:
                            processed.append(curChar);
                            first = false;
                            break;
                    }
                    break;
                case QUOTE_STATE:
                    if (curChar == openChar)
                        if (nextChar != openChar)
                            state = DEFAULT_STATE;
                    processed.append(curChar);
                    break;
                case COMMENT_LINE_STATE:
                    if (curChar == '\n')
                        state = DEFAULT_STATE;
                    processed.append(curChar);
                    break;
                case COMMENT_MULTILINE_STATE:
                    if (curChar == '*')
                        if (nextChar == '/') {
                            state = DEFAULT_STATE;
                        }
                    processed.append(curChar);
                    break;
                case ARRAY_STATE:
                    if (Character.isDigit(curChar) || curChar == ':' || curChar == ' ' || curChar == ',' || curChar == '\t' || curChar == '\n' || curChar == '\r') {
                        processed.append(curChar);
                    } else {
                        state = DEFAULT_STATE;
                        processed.append(curChar);
                    }
                    break;
                case PARAMETER_STATE:
                    if (Character.isDigit(curChar) || Character.isAlphabetic(curChar) || curChar == '_' || curChar == '$')
                        parameter.append(curChar);
                    else {
                        state = DEFAULT_STATE;
                        String name = parameter.toString();
                        boolean contains = false;
                        Parameter old = null;
                        for (int g = 0; g < parameters.size(); g++) {
                            if (parameters.get(i).getName().contentEquals(name)) {
                                contains = true;
                                old = parameters.get(i);
                                break;
                            }
                        }
                        if (contains)
                            parameters.add(old);
                        else {
                            Parameter p = new Parameter(name);
                            parameters.add(p);
                            displayParameters.add(p);
                        }
                    }
                    break;
                case EXECUTE_BLOCK:
                    state = DEFAULT_STATE;
                    processed.append(curChar);
                    if (sb.toString().toLowerCase().indexOf("execute block") == i - 1)
                        execute_block = true;
                    break;

            }

        }
        processedSql = processed.toString();


    }

    public boolean isExecute_block() {
        return execute_block;
    }

    public List<Parameter> getDisplayParameters() {
        return displayParameters;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public String getProcessedSql() {
        return processedSql;
    }
}
