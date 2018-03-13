package org.underworldlabs.sqlParser;

import org.executequery.GUIUtilities;
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
    private List<Parameter> parameters;
    private List<Parameter> displayParameters;
    private String processedSql;
    private boolean executeBlock;

    public SqlParser(String sql) {
        executeBlock = false;
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
            try {
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
                        if (Character.isDigit(curChar) || Character.isAlphabetic(curChar) || curChar == '_' || curChar == '$') {
                            parameter.append(curChar);
                            if (i == len - 1) {
                                createParameter(parameter);
                            }
                        } else {
                            state = DEFAULT_STATE;
                            createParameter(parameter);
                        }
                        break;
                    case EXECUTE_BLOCK:
                        state = DEFAULT_STATE;
                        processed.append(curChar);
                        if (sb.toString().toLowerCase().indexOf("execute block") == i - 1)
                            executeBlock = true;
                        break;

                }
            } catch (Exception e) {
                GUIUtilities.displayExceptionErrorDialog("Error parsing query", e);
            }

        }
        processedSql = processed.toString();
    }

    public boolean isExecuteBlock() {
        return executeBlock;
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

    private void createParameter(StringBuilder parameter) {
        String name = parameter.toString();
        boolean contains = false;
        Parameter old = null;
        for (int g = 0; g < parameters.size(); g++) {
            if (parameters.get(g).getName().contentEquals(name)) {
                contains = true;
                old = parameters.get(g);
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
}
