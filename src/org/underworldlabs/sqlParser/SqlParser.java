package org.underworldlabs.sqlParser;

import org.executequery.GUIUtilities;
import org.executequery.gui.editor.autocomplete.Parameter;
import org.executequery.log.Log;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SqlParser {
    private static final int DEFAULT_STATE = 0;
    private static final int QUOTE_STATE = 1;
    private static final int COMMENT_LINE_STATE = 2;
    private static final int COMMENT_MULTILINE_STATE = 3;
    private static final int ARRAY_STATE = 4;
    private static final int PARAMETER_STATE = 6;
    private static final int EXECUTE = 7;
    private static final int BLOCK = 8;
    private static final int DECLARE = 9;
    private static final int VARIABLE = 10;
    private static final int BLOB_PARAMETER_STATE = 11;

    private final List<Parameter> parameters;
    private final List<Parameter> displayParameters;
    private final String processedSql;
    private boolean executeBlock;
    private final String variables;

    public SqlParser(String sql) {
        this(sql, "");
    }

    public SqlParser(String sql, String variables) {
        this.variables = variables;
        String execute = "execute";
        String block = "block";
        executeBlock = false;
        StringBuilder sb = new StringBuilder(sql);
        StringBuilder processed = new StringBuilder();
        displayParameters = new ArrayList<>();
        StringBuilder parameter = new StringBuilder();
        StringBuilder blobParameter = new StringBuilder();
        parameters = new ArrayList<>();
        int state = DEFAULT_STATE;
        int len = sql.length();
        int cur_exec = 1;
        boolean first = true;
        boolean second = false;
        boolean blobStart = false;
        Character openChar = null;
        for (int i = 0; i < len; i++) {
            char curChar = sb.charAt(i);
            Character nextChar = '\0';
            if (i + 1 < len)
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
                                second = false;
                                break;
                            case '?':
                                if (nextChar == '\'') {
                                    state = BLOB_PARAMETER_STATE;
                                    blobStart = true;
                                }
                                Parameter p = new Parameter("№" + (displayParameters.size() + 1));
                                parameters.add(p);
                                displayParameters.add(p);
                                processed.append(curChar);
                                first = false;
                                second = false;
                                break;
                            case ':':
                                if (Character.isAlphabetic(nextChar) || Character.isDigit(nextChar) || nextChar == '$' || nextChar == '_') {
                                    processed.append('?');
                                    state = PARAMETER_STATE;
                                    parameter.setLength(0);
                                } else processed.append(curChar);
                                first = false;
                                second = false;
                                break;
                            case 'e':
                            case 'E':
                                if (first) {
                                    state = EXECUTE;
                                }
                                processed.append(curChar);
                                first = false;
                                second = false;
                                break;
                            case 'b':
                            case 'B':
                                if (second) {
                                    state = BLOCK;
                                }
                                processed.append(curChar);
                                first = false;
                                second = false;
                                break;
                            case ' ':
                            case '\n':
                            case '\t':
                            case '\r':
                                processed.append(curChar);
                                break;
                            default:
                                processed.append(curChar);
                                first = false;
                                second = false;
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
                                createParameter(parameter, processed);
                            }
                        } else {
                            state = DEFAULT_STATE;
                            createParameter(parameter, processed);
                            i--;
                        }
                        break;
                    case BLOB_PARAMETER_STATE:
                        if (curChar == '\'' && !blobStart) {
                            state = DEFAULT_STATE;
                            parameters.get(parameters.size() - 1).setValue(new File(blobParameter.toString()));
                            displayParameters.get(displayParameters.size() - 1).setValue(new File(blobParameter.toString()));
                            parameters.get(parameters.size() - 1).setNeedUpdateValue(false);
                            displayParameters.get(displayParameters.size() - 1).setNeedUpdateValue(false);
                            blobParameter.setLength(0);
                        }
                        blobStart = false;
                        if (curChar != '\'')
                            blobParameter.append(curChar);
                        break;
                    case EXECUTE:
                        processed.append(curChar);
                        if (Character.toLowerCase(curChar) == execute.charAt(cur_exec)) {
                            cur_exec++;
                            if (cur_exec == execute.length()) {
                                second = true;
                                cur_exec = 1;
                                state = DEFAULT_STATE;
                            }
                        } else state = DEFAULT_STATE;
                        break;
                    case BLOCK:
                        processed.append(curChar);
                        if (Character.toLowerCase(curChar) == block.charAt(cur_exec)) {
                            cur_exec++;
                            if (cur_exec == block.length()) {
                                executeBlock = true;
                                state = DEFAULT_STATE;
                            }
                        } else state = DEFAULT_STATE;
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

    private void createParameter(StringBuilder parameter, StringBuilder processed) {

        String name = parameter.toString();
        if (variables.toLowerCase().contains("<" + name.toLowerCase() + ">")) {
            processed.replace(processed.length() - 1, processed.length(), ":" + name);

        } else if (variables.contains("blobfile=")) {
            try {

                int startIndex = Integer.parseInt(name.substring(1).split("_")[0], 16);
                int endIndex = startIndex + Integer.parseInt(name.split("_")[1], 16);

                byte[] fileData = Files.readAllBytes(Paths.get(variables.substring(9)));
                byte[] parameterData = Arrays.copyOfRange(fileData, startIndex, endIndex);

                Parameter newParameter = new Parameter(name);
                newParameter.setValue(parameterData);
                newParameter.setNeedUpdateValue(false);

                parameters.add(newParameter);
                displayParameters.add(newParameter);

            } catch (IOException e) {
                Log.error(String.format("Error reading .lob file (%s)", variables.substring(9)), e);
            }

        } else {
            boolean contains = false;
            Parameter old = null;
            for (int g = 0; g < parameters.size(); g++) {
                if (parameters.get(g).getName().equalsIgnoreCase(name)) {
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
}
