package org.executequery.gui.browser.managment.tracemanager.net;

import org.executequery.gui.browser.managment.tracemanager.LogConstants;
import org.executequery.log.Log;

import java.sql.Timestamp;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LogMessage {
    private int id;
    private Timestamp timestamp;
    private String idProcess;
    private String idThread;
    private String typeEvent;
    private TypeEventTrace typeEventTrace;
    private String body;
    private String sessionID;
    private String sessionName;
    private String database;
    private String serviceID;
    private String userName;
    private String protocolConnection;
    private String clientAddress;
    private String typeQueryService;
    private String optionsStartService;
    private String idConnection;
    private String role;
    private String charset;
    private String clientProcess;
    private String idClientProcess;
    private String idTransaction;
    private String levelIsolation;
    private String modeOfBlock;
    private String modeOfAccess;

    public LogMessage(String body) {
        body = addField(body, "(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d+)", new String[]{}, LogConstants.TSTAMP_COLUMN);
        body = addField(body, "^\\([\\d\\w]+:", new String[]{"(", ":"}, LogConstants.ID_PROCESS_COLUMN);
        body = addField(body, "^[\\w\\d]+\\)", ")", LogConstants.ID_THREAD_COLUMN);
        body = addField(body, "^[\\w\\d_]+\\s", new String[]{"\t", "\n", "\r"}, LogConstants.EVENT_TYPE_COLUMN);
        init(body);
    }

    private void init(String body) {
        this.setBody(body);
        if (typeEvent.contentEquals("TRACE_INIT") || typeEvent.contentEquals(" TRACE_FINI")) {
            typeEventTrace = TypeEventTrace.TRACE_EVENT;
            body = addField(body, "^SESSION_[\\w\\d]+ ", "SESSION_", LogConstants.ID_SESSION_COLUMN);
            body = addField(body, "^[\\w\\d_]+ ", " ", LogConstants.NAME_SESSION_COLUMN);
        } else if (typeEvent.contentEquals("CREATE_DATABASE")
                || typeEvent.contentEquals("ATTACH_DATABASE")
                || typeEvent.contentEquals("DROP_DATABASE")
                || typeEvent.contentEquals("DETACH_DATABASE")) {
            typeEventTrace = TypeEventTrace.DATABASE_EVENT;
            body = addField(body, "^.+ \\(ATT_", "(ATT_", LogConstants.DATABASE_COLUMN);
            body = addField(body, "^[\\w\\d]+,", ",", LogConstants.ID_CONNECTION_COLUMN);
            body = addField(body, "^[\\w\\d_]+:", ":", LogConstants.USERNAME_COLUMN);
            body = addField(body, "^[\\w\\d_]+,", ",", LogConstants.ROLE_COLUMN);
            if (!body.contentEquals("<internal>)")) {
                body = addField(body, "^[\\w\\d_]+,", ",", LogConstants.CHARSET_COLUMN);
                body = addField(body, "^[\\w\\d_]+:", ":", LogConstants.PROTOCOL_CONNECTION_COLUMN);
                body = addField(body, "^[\\w\\d_\\./]+\\)", ")", LogConstants.CLIENT_ADDRESS_COLUMN);
                body = addField(body, ":[\\w\\d]+$", ":", LogConstants.ID_CLIENT_PROCESS_COLUMN);
                setClientProcess(body);
            }

        } else if (typeEvent.contentEquals("START_TRANSACTION")
                || typeEvent.contentEquals("COMMIT_RETAINING")
                || typeEvent.contentEquals("COMMIT_TRANSACTION")
                || typeEvent.contentEquals("ROLLBACK_RETAINING")
                || typeEvent.contentEquals("ROLLBACK_TRANSACTION")) {
            typeEventTrace = TypeEventTrace.TRANSACTION_EVENT;
            body = addField(body, "^.+ \\(ATT_", "(ATT_", LogConstants.DATABASE_COLUMN);
            body = addField(body, "^[\\w\\d]+,", ",", LogConstants.ID_CONNECTION_COLUMN);
            body = addField(body, "^[\\w\\d_]+:", ":", LogConstants.USERNAME_COLUMN);
            body = addField(body, "^[\\w\\d_]+,", ",", LogConstants.ROLE_COLUMN);
            if (!body.startsWith("<internal>)")) {
                body = addField(body, "^[\\w\\d_]+,", ",", LogConstants.CHARSET_COLUMN);
                body = addField(body, "^[\\w\\d_]+:", ":", LogConstants.PROTOCOL_CONNECTION_COLUMN);
                body = addField(body, "^[\\w\\d_\\./]+\\)", ")", LogConstants.CLIENT_ADDRESS_COLUMN);
            } else {
                body = body.replace("<internal>)", "").trim();
            }
            body = addField(body, "^\\(TRA_[\\d]+,", new String[]{"(TRA_", ","}, LogConstants.ID_TRANSACTION_COLUMN);
            body = addField(body, "^[\\w_]+ \\|", new String[]{" ", "|"}, LogConstants.LEVEL_ISOLATION_COLUMN);
            if (levelIsolation.contentEquals("READ_COMMITTED")) {
                body = addField(body, "^[\\w_]+ \\|", new String[]{" ", "|"}, LogConstants.LEVEL_ISOLATION_COLUMN);
                levelIsolation = "READ_COMMITTED | " + levelIsolation;
            }
            body = addField(body, "^[\\w_]+ \\|", new String[]{" ", "|"}, LogConstants.MODE_OF_BLOCK_COLUMN);
            body = addField(body, "^[\\w_]+\\)", ")", LogConstants.MODE_OF_ACCESS_COLUMN);

        } else if (typeEvent.contentEquals(" PREPARE_STATEMENT"))

        {
            typeEventTrace = TypeEventTrace.TRANSACTION_EVENT;
        } else if (typeEvent.contentEquals("FREE_STATEMENT")
                || typeEvent.contentEquals("CLOSE_CURSOR"))

        {
            typeEventTrace = TypeEventTrace.STATEMENT_FREE_EVENT;
        } else if (typeEvent.contentEquals("EXECUTE_STATEMENT_START")
                || typeEvent.contentEquals("EXECUTE_STATEMENT_FINISH"))

        {
            typeEventTrace = TypeEventTrace.STATEMENT_EVENT;
        } else if (typeEvent.contentEquals("SET_CONTEXT"))

        {
            typeEventTrace = TypeEventTrace.CONTEXT_EVENT;
        } else if (typeEvent.contentEquals("PRIVILEGES_CHANGE"))

        {
            typeEventTrace = TypeEventTrace.PRIVILEGES_CHANGE_EVENT;
        } else if (typeEvent.contentEquals("EXECUTE_PROCEDURE_START")
                || typeEvent.contentEquals("EXECUTE_FUNCTION_START")
                || typeEvent.contentEquals("EXECUTE_PROCEDURE_FINISH")
                || typeEvent.contentEquals("EXECUTE_FUNCTION_FINISH"))

        {
            typeEventTrace = TypeEventTrace.PROCEDURE_FUNCTION_EVENT;
        } else if (typeEvent.contentEquals("EXECUTE_TRIGGER_START")
                || typeEvent.contentEquals("EXECUTE_TRIGGER_FINISH"))

        {
            typeEventTrace = TypeEventTrace.TRIGGER_EVENT;
        } else if (typeEvent.contentEquals("COMPILE_BLR"))

        {
            typeEventTrace = TypeEventTrace.COMPILE_BLR_EVENT;
        } else if (typeEvent.contentEquals("EXECUTE_BLR"))

        {
            typeEventTrace = TypeEventTrace.EXECUTE_BLR_EVENT;
        } else if (typeEvent.contentEquals("EXECUTE_DYN"))

        {
            typeEventTrace = TypeEventTrace.EXECUTE_DYN_EVENT;
        } else if (typeEvent.contentEquals("ATTACH_SERVICE")
                || typeEvent.contentEquals("DETACH_SERVICE"))

        {
            typeEventTrace = TypeEventTrace.SERVICE_EVENT;
        } else if (typeEvent.contentEquals("START_SERVICE"))

        {
            typeEventTrace = TypeEventTrace.START_SERVICE_EVENT;
            body = addField(body, "^service_mgr, \\(Service [\\w\\d]+,", new String[]{"service_mgr, (Service ", ","}, LogConstants.ID_SERVICE_COLUMN);
            body = addField(body, "^[\\w\\d_]+,", ",", LogConstants.USERNAME_COLUMN);
            body = addField(body, "^[\\w\\d_]+:", ":", LogConstants.PROTOCOL_CONNECTION_COLUMN);
            body = addField(body, "^[\\w\\d_\\./]+\\)", ")", LogConstants.CLIENT_ADDRESS_COLUMN);
            body = addField(body, "^\"[\\w\\d_\\./ ]+\"", "\"", LogConstants.TYPE_QUERY_SERVICE_COLUMN);
            setOptionsStartService(body);
        } else if (typeEvent.contentEquals(" QUERY_SERVICE"))

        {
            typeEventTrace = TypeEventTrace.QUERY_SERVICE_EVENT;
        } else if (typeEvent.contentEquals("ERROR") || typeEvent.contentEquals("WARNING"))

        {
            typeEventTrace = TypeEventTrace.ERROR_WARNING_EVENT;
        } else if (typeEvent.contentEquals("{SWEEP_START")
                || typeEvent.contentEquals("SWEEP_FINISH")
                || typeEvent.contentEquals("SWEEP_FAILED")
                || typeEvent.contentEquals("SWEEP_PROGRESS"))

        {
            typeEventTrace = TypeEventTrace.SWEEP_EVENT;
        }
    }

    private String findOfRegex(String regex, String str) {
        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(str);
        if (m.find()) {

            return m.group();

        } else {
            return null;
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getIdProcess() {
        return idProcess;
    }

    public void setIdProcess(String idProcess) {
        this.idProcess = idProcess;
    }

    public String getIdThread() {
        return idThread;
    }

    public void setIdThread(String idThread) {
        this.idThread = idThread;
    }

    public String getTypeEvent() {
        return typeEvent;
    }

    public void setTypeEvent(String typeEvent) {
        this.typeEvent = typeEvent;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public TypeEventTrace getTypeEventTrace() {
        return typeEventTrace;
    }

    public void setTypeEventTrace(TypeEventTrace typeEventTrace) {
        this.typeEventTrace = typeEventTrace;
    }

    public String getSessionID() {
        return sessionID;
    }

    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getServiceID() {
        return serviceID;
    }

    public void setServiceID(String serviceID) {
        this.serviceID = serviceID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getProtocolConnection() {
        return protocolConnection;
    }

    public void setProtocolConnection(String protocolConnection) {
        this.protocolConnection = protocolConnection;
    }

    public String getClientAddress() {
        return clientAddress;
    }

    public void setClientAddress(String clientAddress) {
        this.clientAddress = clientAddress;
    }

    public String getTypeQueryService() {
        return typeQueryService;
    }

    public void setTypeQueryService(String typeQueryService) {
        this.typeQueryService = typeQueryService;
    }

    public String getOptionsStartService() {
        return optionsStartService;
    }

    public void setOptionsStartService(String optionsStartService) {
        this.optionsStartService = optionsStartService;
    }

    public String getIdConnection() {
        return idConnection;
    }

    public void setIdConnection(String idConnection) {
        this.idConnection = idConnection;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getClientProcess() {
        return clientProcess;
    }

    public void setClientProcess(String clientProcess) {
        this.clientProcess = clientProcess;
    }

    public String getIdClientProcess() {
        return idClientProcess;
    }

    public void setIdClientProcess(String idClientProcess) {
        this.idClientProcess = idClientProcess;
    }

    public String getIdTransaction() {
        return idTransaction;
    }

    public void setIdTransaction(String idTransaction) {
        this.idTransaction = idTransaction;
    }

    public String getLevelIsolation() {
        return levelIsolation;
    }

    public void setLevelIsolation(String levelIsolation) {
        this.levelIsolation = levelIsolation;
    }

    public String getModeOfBlock() {
        return modeOfBlock;
    }

    public void setModeOfBlock(String modeOfBlock) {
        this.modeOfBlock = modeOfBlock;
    }

    public String getModeOfAccess() {
        return modeOfAccess;
    }

    public void setModeOfAccess(String modeOfAccess) {
        this.modeOfAccess = modeOfAccess;
    }

    private String addField(String body, String regex, String excludedRegex, String colName) {
        return addField(body, regex, new String[]{excludedRegex}, colName);
    }

    private String addField(String body, String regex, String[] excludedRegex, String colName) {
        Field field = parseField(body, regex, excludedRegex);
        switch (colName) {
            case LogConstants.ID_PROCESS_COLUMN:
                setIdProcess(field.field);
                break;
            case LogConstants.ID_THREAD_COLUMN:
                setIdThread(field.field);
                break;
            case LogConstants.EVENT_TYPE_COLUMN:
                setTypeEvent(field.field);
                break;
            case LogConstants.ID_COLUMN:
                setId(Integer.parseInt(field.field));
                break;
            case LogConstants.TSTAMP_COLUMN:
                setTimestamp(Timestamp.valueOf(field.field.replace("T", " ")));
                break;
            case LogConstants.ID_SESSION_COLUMN:
                setSessionID(field.field);
                break;
            case LogConstants.NAME_SESSION_COLUMN:
                setSessionName(field.field);
                break;
            case LogConstants.ID_SERVICE_COLUMN:
                setServiceID(field.field);
                break;
            case LogConstants.USERNAME_COLUMN:
                setUserName(field.field);
                break;
            case LogConstants.PROTOCOL_CONNECTION_COLUMN:
                setProtocolConnection(field.field);
                break;
            case LogConstants.CLIENT_ADDRESS_COLUMN:
                setClientAddress(field.field);
                break;
            case LogConstants.TYPE_QUERY_SERVICE_COLUMN:
                setTypeQueryService(field.field);
                break;
            case LogConstants.OPTIONS_START_SERVICE_COLUMN:
                setOptionsStartService(field.field);
                break;
            case LogConstants.ROLE_COLUMN:
                setRole(field.field);
                break;
            case LogConstants.DATABASE_COLUMN:
                setDatabase(field.field);
                break;
            case LogConstants.CHARSET_COLUMN:
                setCharset(field.field);
                break;
            case LogConstants.ID_CONNECTION_COLUMN:
                setIdConnection(field.field);
                break;
            case LogConstants.CLIENT_PROCESS_COLUMN:
                setClientProcess(field.field);
                break;
            case LogConstants.ID_CLIENT_PROCESS_COLUMN:
                setIdClientProcess(field.field);
                break;
            case LogConstants.LEVEL_ISOLATION_COLUMN:
                setLevelIsolation(field.field);
                break;
            case LogConstants.ID_TRANSACTION_COLUMN:
                setIdTransaction(field.field);
                break;
            case LogConstants.MODE_OF_BLOCK_COLUMN:
                setModeOfBlock(field.field);
                break;
            case LogConstants.MODE_OF_ACCESS_COLUMN:
                setModeOfAccess(field.field);
                break;
            default:
                break;
        }
        return field.body;
    }

    public Object getFieldOfName(String colName) {
        switch (colName) {
            case LogConstants.ID_PROCESS_COLUMN:
                return getIdProcess();
            case LogConstants.ID_THREAD_COLUMN:
                return getIdThread();
            case LogConstants.EVENT_TYPE_COLUMN:
                return getTypeEvent();
            case LogConstants.ID_COLUMN:
                return getId();
            case LogConstants.TSTAMP_COLUMN:
                return getTimestamp();
            case LogConstants.ID_SESSION_COLUMN:
                return getSessionID();
            case LogConstants.NAME_SESSION_COLUMN:
                return getSessionName();
            case LogConstants.ID_SERVICE_COLUMN:
                return getServiceID();
            case LogConstants.USERNAME_COLUMN:
                return getUserName();
            case LogConstants.PROTOCOL_CONNECTION_COLUMN:
                return getProtocolConnection();
            case LogConstants.CLIENT_ADDRESS_COLUMN:
                return getClientAddress();
            case LogConstants.TYPE_QUERY_SERVICE_COLUMN:
                return getTypeQueryService();
            case LogConstants.OPTIONS_START_SERVICE_COLUMN:
                return getOptionsStartService();
            case LogConstants.ROLE_COLUMN:
                return getRole();
            case LogConstants.DATABASE_COLUMN:
                return getDatabase();
            case LogConstants.CHARSET_COLUMN:
                return getCharset();
            case LogConstants.ID_CONNECTION_COLUMN:
                return getIdConnection();
            case LogConstants.CLIENT_PROCESS_COLUMN:
                return getClientProcess();
            case LogConstants.ID_CLIENT_PROCESS_COLUMN:
                return getIdClientProcess();
            case LogConstants.LEVEL_ISOLATION_COLUMN:
                return getLevelIsolation();
            case LogConstants.ID_TRANSACTION_COLUMN:
                return getIdTransaction();
            case LogConstants.MODE_OF_BLOCK_COLUMN:
                return getModeOfBlock();
            case LogConstants.MODE_OF_ACCESS_COLUMN:
                return getModeOfAccess();
            default:
                return null;
        }
    }


    private Field parseField(String body, String regex, String[] excludedRegex) {
        Field field = new Field();
        try {
            field.field = findOfRegex(regex, body);
            field.body = body.replace(field.field, "").trim();
            for (int i = 0; i < excludedRegex.length; i++)
                field.field = field.field.replace(excludedRegex[i], "");
        } catch (Exception e) {
            Log.error("Error trace manager id_message = " + id + ", " + getTypeEvent() + " " + timestamp + ":", e);
            field.field = "";
            field.body = body;
        } finally {
            return field;
        }

    }


    public enum TypeEventTrace {
        TRACE_EVENT,
        DATABASE_EVENT,
        TRANSACTION_EVENT,
        STATEMENT_PREPARE_EVENT,
        STATEMENT_FREE_EVENT,
        STATEMENT_EVENT,
        CONTEXT_EVENT,
        PRIVILEGES_CHANGE_EVENT,
        PROCEDURE_FUNCTION_EVENT,
        TRIGGER_EVENT,
        COMPILE_BLR_EVENT,
        EXECUTE_BLR_EVENT,
        EXECUTE_DYN_EVENT,
        SERVICE_EVENT,
        START_SERVICE_EVENT,
        QUERY_SERVICE_EVENT,
        ERROR_WARNING_EVENT,
        SWEEP_EVENT
    }

    class Field {
        public String field;
        public String body;
    }
}
