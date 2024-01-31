package org.executequery.gui.browser.managment.tracemanager.net;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.executequery.gui.browser.managment.tracemanager.LogConstants;
import org.executequery.log.Log;
import org.underworldlabs.traceparser.RedTraceBaseListener;
import org.underworldlabs.traceparser.RedTraceLexer;
import org.underworldlabs.traceparser.RedTraceParser;
import org.underworldlabs.util.MiscUtils;

import java.sql.Timestamp;
import java.util.List;
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
    private Long timeExecution;
    private Long countReads;
    private Long countWrites;
    private Long countFetches;
    private Long countMarks;
    private String idStatement;
    private Long fetchedRecords;
    private String statementText;
    private String paramText;
    private String planText;
    private String tableCounters;
    private String declareContextVariablesText;
    private String executor;
    private String grantor;
    private String privilege;
    private String privilegeObject;
    private String privilegeUsername;
    private String privilegeAttachment;
    private String privilegeTransaction;
    private String procedureName;
    private String returnValue;
    private String failedText;
    private String triggerInfo;
    private String sentData;
    private String receivedData;
    private String errorMessage;
    private String oldestInteresting;
    private String oldestActive;
    private String oldestSnapshot;
    private String nextTransaction;
    private boolean failed;
    private boolean highlight;
    private long totalCacheMemory;
    private long ramCacheMemory;
    private long diskCacheMemory;

    public LogMessage() {
    }

    public LogMessage(String body) {
        init(body);
    }

    private void init(String body) {
        this.setBody(body);
        RedTraceParser parser = buildParser(body);
        try {
            ParseTree tree = parser.parse();
            ParseTreeWalker walker = new ParseTreeWalker();
            walker.walk(new RedTraceBaseListener() {
                @Override
                public void enterDatabase_event(RedTraceParser.Database_eventContext ctx) {
                    setTypeEvent(ctx.type_database_event().getText());
                    setTypeEventTrace(TypeEventTrace.DATABASE_EVENT);
                    setHeader(ctx.header_event());
                    setClientProcessInfo(ctx.client_process_info());
                    setConnectionInfo(ctx.connection_info());
                }

                @Override
                public void enterStart_service_event(RedTraceParser.Start_service_eventContext ctx) {
                    setTypeEvent(textFromRuleContext(ctx.type_start_service_event()));
                    setTypeEventTrace(TypeEventTrace.START_SERVICE_EVENT);
                    setHeader(ctx.header_event());
                    setServiceID(textFromRuleContext(ctx.id_service().id()));
                    setUserName(textFromRuleContext(ctx.username()));
                    setProtocolConnection(textFromRuleContext(ctx.protocol()));
                    setClientAddress(textFromRuleContext(ctx.client_address()));
                    setClientProcessInfo(ctx.client_process_info());
                    setTypeQueryService(textFromRuleContext(ctx.type_query_service()));
                    setOptionsStartService(textFromRuleContext(ctx.options_service()));
                }

                @Override
                public void enterStatement_event(RedTraceParser.Statement_eventContext ctx) {
                    setTypeEvent(textFromRuleContext(ctx.type_statement_event()));
                    setTypeEventTrace(TypeEventTrace.STATEMENT_EVENT);
                    setHeader(ctx.header_event());
                    setConnectionInfo(ctx.connection_info());
                    setClientProcessInfo(ctx.client_process_info());
                    setTransactionInfo(ctx.transaction_info());
                    if (ctx.id_statement() != null)
                        setIdStatement(textFromRuleContext(ctx.id_statement().id()));
                    setQueryAndParams(ctx.query_and_params());
                }

                @Override
                public void enterStatement_prepare_event(RedTraceParser.Statement_prepare_eventContext ctx) {
                    setTypeEvent(textFromRuleContext(ctx.type_statement_prepare_event()));
                    setTypeEventTrace(TypeEventTrace.STATEMENT_PREPARE_EVENT);
                    setHeader(ctx.header_event());
                    setConnectionInfo(ctx.connection_info());
                    setClientProcessInfo(ctx.client_process_info());
                    setTransactionInfo(ctx.transaction_info());
                    if (ctx.id_statement() != null)
                        setIdStatement(textFromRuleContext(ctx.id_statement().id()));
                    setQueryAndParams(ctx.query_and_params());

                }

                @Override
                public void enterTrace_event(RedTraceParser.Trace_eventContext ctx) {
                    setTypeEvent(ctx.type_trace_event().getText());
                    setTypeEventTrace(TypeEventTrace.TRACE_EVENT);
                    setHeader(ctx.header_event());
                    String id = textFromRuleContext(ctx.id_session());
                    if (id != null)
                        setSessionID(id.trim().replace("SESSION_", ""));
                    setSessionName(textFromRuleContext(ctx.name_session()));
                }

                @Override
                public void enterTransaction_event(RedTraceParser.Transaction_eventContext ctx) {
                    setTypeEvent(ctx.type_transaction_event().getText());
                    setTypeEventTrace(TypeEventTrace.TRANSACTION_EVENT);
                    setHeader(ctx.header_event());
                    setConnectionInfo(ctx.connection_info());
                    setClientProcessInfo(ctx.client_process_info());
                    setTransactionInfo(ctx.transaction_info());
                    setGlobalCounters(ctx.global_counters());
                    setTableCounters(textFromRuleContext(ctx.table_counters()));
                }

                @Override
                public void enterFree_statement_event(RedTraceParser.Free_statement_eventContext ctx) {
                    setTypeEvent(textFromRuleContext(ctx.type_free_statement_event()));
                    setTypeEventTrace(TypeEventTrace.STATEMENT_FREE_EVENT);
                    setHeader(ctx.header_event());
                    setConnectionInfo(ctx.connection_info());
                    setClientProcessInfo(ctx.client_process_info());
                    if (ctx.id_statement() != null)
                        setIdStatement(textFromRuleContext(ctx.id_statement().id()));
                    setQueryAndParams(ctx.query_and_params());
                }

                @Override
                public void enterPrivileges_change_event(RedTraceParser.Privileges_change_eventContext ctx) {
                    setTypeEvent(textFromRuleContext(ctx.type_privileges_change_event()));
                    setTypeEventTrace(TypeEventTrace.PRIVILEGES_CHANGE_EVENT);
                    setHeader(ctx.header_event());
                    setConnectionInfo(ctx.connection_info());
                    setClientProcessInfo(ctx.client_process_info());
                    setTransactionInfo(ctx.transaction_info());
                    setPrivilegesChangeInfo(ctx.privileges_change_info());
                }

                @Override
                public void enterProcedure_function_event(RedTraceParser.Procedure_function_eventContext ctx) {
                    setTypeEvent(textFromRuleContext(ctx.type_procedure_event()));
                    setTypeEventTrace(TypeEventTrace.PROCEDURE_FUNCTION_EVENT);
                    setHeader(ctx.header_event());
                    setConnectionInfo(ctx.connection_info());
                    setClientProcessInfo(ctx.client_process_info());
                    setTransactionInfo(ctx.transaction_info());
                    setProcedureInfo(ctx.procedure_info());
                }

                @Override
                public void enterTrigger_event(RedTraceParser.Trigger_eventContext ctx) {
                    setTypeEvent(textFromRuleContext(ctx.type_trigger_event()));
                    setTypeEventTrace(TypeEventTrace.TRIGGER_EVENT);
                    setHeader(ctx.header_event());
                    setConnectionInfo(ctx.connection_info());
                    setClientProcessInfo(ctx.client_process_info());
                    setTransactionInfo(ctx.transaction_info());
                    setTriggerInfo(textFromRuleContext(ctx.trigger_info()));
                    setGlobalCounters(ctx.global_counters());
                    setTableCounters(textFromRuleContext(ctx.table_counters()));
                }

                @Override
                public void enterCompile_blr_event(RedTraceParser.Compile_blr_eventContext ctx) {
                    setTypeEvent(textFromRuleContext(ctx.type_compile_blr_event()));
                    setTypeEventTrace(TypeEventTrace.COMPILE_BLR_EVENT);
                    setHeader(ctx.header_event());
                    setConnectionInfo(ctx.connection_info());
                    setClientProcessInfo(ctx.client_process_info());
                    setTransactionInfo(ctx.transaction_info());
                    if (ctx.id_statement() != null)
                        setIdStatement(textFromRuleContext(ctx.id_statement().id()));
                    setQueryAndParams(ctx.query_and_params());
                }

                @Override
                public void enterExecute_blr_event(RedTraceParser.Execute_blr_eventContext ctx) {
                    setTypeEvent(textFromRuleContext(ctx.type_execute_blr_event()));
                    setTypeEventTrace(TypeEventTrace.EXECUTE_BLR_EVENT);
                    setHeader(ctx.header_event());
                    setConnectionInfo(ctx.connection_info());
                    setClientProcessInfo(ctx.client_process_info());
                    setTransactionInfo(ctx.transaction_info());
                    if (ctx.id_statement() != null)
                        setIdStatement(textFromRuleContext(ctx.id_statement().id()));
                    setQueryAndParams(ctx.query_and_params());
                }

                @Override
                public void enterExecute_dyn_event(RedTraceParser.Execute_dyn_eventContext ctx) {
                    setTypeEvent(textFromRuleContext(ctx.type_execute_dyn_event()));
                    setTypeEventTrace(TypeEventTrace.EXECUTE_DYN_EVENT);
                    setHeader(ctx.header_event());
                    setConnectionInfo(ctx.connection_info());
                    setClientProcessInfo(ctx.client_process_info());
                    setTransactionInfo(ctx.transaction_info());
                    setQueryAndParams(ctx.query_and_params());
                }

                @Override
                public void enterService_event(RedTraceParser.Service_eventContext ctx) {
                    setTypeEvent(textFromRuleContext(ctx.type_service_event()));
                    setTypeEventTrace(TypeEventTrace.SERVICE_EVENT);
                    setHeader(ctx.header_event());
                    setServiceID(textFromRuleContext(ctx.id_service().id()));
                    setUserName(textFromRuleContext(ctx.username()));
                    setProtocolConnection(textFromRuleContext(ctx.protocol()));
                    setClientAddress(textFromRuleContext(ctx.client_address()));
                    setClientProcessInfo(ctx.client_process_info());
                }

                @Override
                public void enterService_query_event(RedTraceParser.Service_query_eventContext ctx) {
                    setTypeEvent(textFromRuleContext(ctx.type_query_service_event()));
                    setTypeEventTrace(TypeEventTrace.QUERY_SERVICE_EVENT);
                    setHeader(ctx.header_event());
                    setServiceID(textFromRuleContext(ctx.id_service().id()));
                    setUserName(textFromRuleContext(ctx.username()));
                    setProtocolConnection(textFromRuleContext(ctx.protocol()));
                    setClientAddress(textFromRuleContext(ctx.client_address()));
                    setClientProcessInfo(ctx.client_process_info());
                    setTypeQueryService(textFromRuleContext(ctx.type_query_service()));
                    setSentData(textFromRuleContext(ctx.sended_data()));
                    setReceivedData(textFromRuleContext(ctx.received_data()));
                }

                @Override
                public void enterError_event(RedTraceParser.Error_eventContext ctx) {
                    setTypeEvent(textFromRuleContext(ctx.type_error_event()));
                    setTypeEventTrace(TypeEventTrace.ERROR_WARNING_EVENT);
                    setHeader(ctx.header_event());
                    setConnectionInfo(ctx.connection_info());
                    setClientProcessInfo(ctx.client_process_info());
                    setErrorMessage(textFromRuleContext(ctx.error_message()));
                }

                @Override
                public void enterSweep_event(RedTraceParser.Sweep_eventContext ctx) {
                    setTypeEvent(textFromRuleContext(ctx.type_sweep_event()));
                    setTypeEventTrace(TypeEventTrace.SWEEP_EVENT);
                    setHeader(ctx.header_event());
                    setConnectionInfo(ctx.connection_info());
                    setClientProcessInfo(ctx.client_process_info());
                    setOldestInteresting(textFromRuleContext(ctx.oldest_interesting()));
                    setOldestActive(textFromRuleContext(ctx.oldest_active()));
                    setOldestSnapshot(textFromRuleContext(ctx.oldest_snapshot()));
                    setGlobalCounters(ctx.global_counters());
                    setTableCounters(textFromRuleContext(ctx.table_counters()));
                }

            }, tree);
        } catch (Exception e) {
            e.printStackTrace();
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

    public void setHeader(RedTraceParser.Header_eventContext ctx) {
        setTimestamp(Timestamp.valueOf(textFromRuleContext(ctx.timestamp()).replace("T", " ")));
        setIdProcess(textFromRuleContext(ctx.id_process()));
        setIdThread(textFromRuleContext(ctx.id_thread()));
        setFailed(ctx.failed() != null);
        setFailedText(textFromRuleContext(ctx.failed()));
    }

    public void setClientProcessInfo(RedTraceParser.Client_process_infoContext ctx) {
        if (ctx != null) {
            setClientProcess(textFromRuleContext(ctx.client_process()));
            setIdClientProcess(textFromRuleContext(ctx.id_client_process()));
        }
    }

    public void setConnectionInfo(RedTraceParser.Connection_infoContext ctx) {
        if (ctx != null) {
            setCharset(textFromRuleContext(ctx.charset()));
            setDatabase(textFromRuleContext(ctx.database()));
            idConnection = textFromRuleContext(ctx.id_connection());
            if (idConnection != null) {
                setIdConnection(idConnection.replace("ATT_", ""));
            }
            setClientAddress(textFromRuleContext(ctx.client_address()));
            setProtocolConnection(textFromRuleContext(ctx.protocol()));
            setUserName(textFromRuleContext(ctx.username()));
            setRole(textFromRuleContext(ctx.rolename()));
        }
    }

    public void setTransactionInfo(RedTraceParser.Transaction_infoContext ctx) {
        if (ctx != null) {
            String id = textFromRuleContext(ctx.id_transaction());
            if (id != null)
                setIdTransaction(id.replace("TRA_", ""));
            setLevelIsolation(textFromRuleContext(ctx.level_isolation()));
            setModeOfBlock(textFromRuleContext(ctx.mode_of_block()));
            setModeOfAccess(textFromRuleContext(ctx.mode_of_access()));
        }
    }

    public void setGlobalCounters(RedTraceParser.Global_countersContext ctx) {
        if (ctx != null) {
            try {
                setTimeExecution(getLongFromString(textFromRuleContext(ctx.time_execution())));
                setCountReads(getLongFromString(textFromRuleContext(ctx.reads())));
                setCountWrites(getLongFromString(textFromRuleContext(ctx.writes())));
                setCountFetches(getLongFromString(textFromRuleContext(ctx.fetches())));
                setCountMarks(getLongFromString(textFromRuleContext(ctx.marks())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    Long getLongFromString(String str) {
        if (!MiscUtils.isNull(str))
            try {
                return Long.parseLong(str);
            } catch (Exception e) {
                e.printStackTrace();
            }
        return null;
    }

    public void setQueryAndParams(RedTraceParser.Query_and_paramsContext ctx) {
        if (ctx != null) {
            setPlanText(textFromRuleContext(ctx.plan()));
            setParamText(textFromRuleContext(ctx.params()));
            if (ctx.records_fetched() != null)
                setFetchedRecords(getLongFromString(textFromRuleContext(ctx.records_fetched().id())));
            setGlobalCounters(ctx.global_counters());
            setTableCounters(textFromRuleContext(ctx.table_counters()));
            setStatementText(textFromRuleContext(ctx.query()));
            if (ctx.memory_size_rule() != null) {
                RedTraceParser.Memory_size_ruleContext context = ctx.memory_size_rule();
                if (context.sum_cache() != null)
                    setTotalCacheMemory(getLongFromString(textFromRuleContext(context.sum_cache().cache().size_cache())));
                if (context.ram_cache() != null)
                    setRamCacheMemory(getLongFromString(textFromRuleContext(context.ram_cache().cache().size_cache())));
                if (context.disk_cache() != null)
                    setDiskCacheMemory(getLongFromString(textFromRuleContext(context.disk_cache().cache().size_cache())));
            }
        }
        if (ctx != null && (ctx.global_counters() == null && ctx.plan() == null && ctx.params() == null && ctx.table_counters() == null)) {
            String query = textFromRuleContext(ctx);
            if (query != null) {
                if (isFindOfRegex("param0 = .+\n", query)) {
                    paramText = findOfRegex("(param.+\n)+", query);
                    if (paramText != null)
                        query = query.replace(paramText, "").trim();
                }
                if (isFindOfRegex("[\\d]+ ms.+\n", query)) {
                    String global_counters = findOfRegex("[\\d]+ ms.+\n", query);
                    RedTraceParser redTraceParser = buildParser(global_counters);
                        ParseTree redTree = redTraceParser.global_counters();
                        ParseTreeWalker redWalker = new ParseTreeWalker();
                        redWalker.walk(new RedTraceBaseListener() {
                            @Override
                            public void enterGlobal_counters(RedTraceParser.Global_countersContext ctx) {
                                setGlobalCounters(ctx);
                            }
                        }, redTree);
                        query = query.replace(global_counters, "").trim();
                    }
                    if (isFindOfRegex("[\\d]+ records fetched", query)) {
                        String records_fetched = findOfRegex("[\\d]+ records fetched", query);
                        RedTraceParser redTraceParser = buildParser(records_fetched);
                        ParseTree redTree = redTraceParser.records_fetched();
                        ParseTreeWalker redWalker = new ParseTreeWalker();
                        redWalker.walk(new RedTraceBaseListener() {
                            @Override
                            public void enterRecords_fetched(RedTraceParser.Records_fetchedContext ctx) {
                                setFetchedRecords(getLongFromString(textFromRuleContext(ctx.id())));
                            }
                        }, redTree);
                        query = query.replace(records_fetched, "").trim();
                    }
                    setStatementText(query);
                }
            }
        }

    public void setPrivilegesChangeInfo(RedTraceParser.Privileges_change_infoContext ctx) {
        if (ctx != null) {
            setExecutor(textFromRuleContext(ctx.executor()));
            setGrantor(textFromRuleContext(ctx.grantor()));
            setPrivilege(textFromRuleContext(ctx.privilege()));
            setPrivilegeObject(textFromRuleContext(ctx.object()));
            setPrivilegeUsername(textFromRuleContext(ctx.username()));
            RedTraceParser.AttachmentContext attachment = ctx.attachment();
            if (attachment != null)
                setPrivilegeAttachment(textFromRuleContext(attachment.id()));
            RedTraceParser.TransactionContext transaction = ctx.transaction();
            if (transaction != null)
                setPrivilegeTransaction(textFromRuleContext(transaction.id()));

        }

    }

    public void setProcedureInfo(RedTraceParser.Procedure_infoContext ctx) {
        setProcedureName(textFromRuleContext(ctx.procedure_name()));
        setReturnValue(textFromRuleContext(ctx.return_value()));
        setParamText(textFromRuleContext(ctx.params()));
        setGlobalCounters(ctx.global_counters());
        if (ctx.records_fetched() != null)
            setFetchedRecords(getLongFromString(textFromRuleContext(ctx.records_fetched().id())));
        setTableCounters(textFromRuleContext(ctx.table_counters()));
    }
    private String textFromRuleContext(ParserRuleContext ctx) {
        try {
            return ctx.getText();
        } catch (NullPointerException e) {
            return null;
        }
    }

    private String textFromRuleContext(TerminalNode ctx) {
        try {
            return ctx.getText();
        } catch (NullPointerException e) {
            return null;
        }
    }

    private boolean isFindOfRegex(String regex, String str) {
        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(str);
        return (m.find());
    }

    private RedTraceParser buildParser(String str) {
        RedTraceLexer lexer = new RedTraceLexer(CharStreams.fromString(str));
        List<? extends ANTLRErrorListener> listeners = lexer.getErrorListeners();
        for (int i = 0; i < listeners.size(); i++) {
            if (listeners.get(i) instanceof ConsoleErrorListener)
                lexer.removeErrorListener(listeners.get(i));
        }
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        RedTraceParser parser = new RedTraceParser(tokens);
        listeners = parser.getErrorListeners();
        for (int i = 0; i < listeners.size(); i++) {
            if (listeners.get(i) instanceof ConsoleErrorListener)
                parser.removeErrorListener(listeners.get(i));
        }
        return parser;
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

    public Long getTimeExecution() {
        return timeExecution;
    }

    public void setTimeExecution(Long timeExecution) {
        this.timeExecution = timeExecution;
    }

    public Long getCountReads() {
        return countReads;
    }

    public void setCountReads(Long countReads) {
        this.countReads = countReads;
    }

    public Long getCountWrites() {
        return countWrites;
    }

    public void setCountWrites(Long countWrites) {
        this.countWrites = countWrites;
    }

    public Long getCountFetches() {
        return countFetches;
    }

    public void setCountFetches(Long countFetches) {
        this.countFetches = countFetches;
    }

    public Long getCountMarks() {
        return countMarks;
    }

    public void setCountMarks(Long countMarks) {
        this.countMarks = countMarks;
    }

    public String getIdStatement() {
        return idStatement;
    }

    public void setIdStatement(String idStatement) {
        this.idStatement = idStatement;
    }

    public Long getFetchedRecords() {
        return fetchedRecords;
    }

    public void setFetchedRecords(Long fetchedRecords) {
        this.fetchedRecords = fetchedRecords;
    }

    public String getStatementText() {
        return statementText;
    }

    public void setStatementText(String statementText) {
        this.statementText = statementText;
    }

    public String getParamText() {
        return paramText;
    }

    public void setParamText(String paramText) {
        this.paramText = paramText;
    }

    public String getPlanText() {
        return planText;
    }

    public void setPlanText(String planText) {
        this.planText = planText;
    }

    public String getTableCounters() {
        return tableCounters;
    }

    public void setTableCounters(String tableCounters) {
        this.tableCounters = tableCounters;
    }

    public String getDeclareContextVariablesText() {
        return declareContextVariablesText;
    }

    public void setDeclareContextVariablesText(String declareContextVariablesText) {
        this.declareContextVariablesText = declareContextVariablesText;
    }

    public String getExecutor() {
        return executor;
    }

    public void setExecutor(String executor) {
        this.executor = executor;
    }

    public String getGrantor() {
        return grantor;
    }

    public void setGrantor(String grantor) {
        this.grantor = grantor;
    }

    public String getPrivilege() {
        return privilege;
    }

    public void setPrivilege(String privilege) {
        this.privilege = privilege;
    }

    public String getPrivilegeObject() {
        return privilegeObject;
    }

    public void setPrivilegeObject(String privilegeObject) {
        this.privilegeObject = privilegeObject;
    }

    public String getPrivilegeUsername() {
        return privilegeUsername;
    }

    public void setPrivilegeUsername(String privilegeUsername) {
        this.privilegeUsername = privilegeUsername;
    }

    public String getPrivilegeAttachment() {
        return privilegeAttachment;
    }

    public void setPrivilegeAttachment(String privilegeAttachment) {
        this.privilegeAttachment = privilegeAttachment;
    }

    public String getPrivilegeTransaction() {
        return privilegeTransaction;
    }

    public void setPrivilegeTransaction(String privilegeTransaction) {
        this.privilegeTransaction = privilegeTransaction;
    }

    public boolean isFailed() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    public boolean isHighlight() {
        return highlight;
    }

    public void setHighlight(boolean highlight) {
        this.highlight = highlight;
    }

    public String getProcedureName() {
        return procedureName;
    }

    public void setProcedureName(String procedureName) {
        this.procedureName = procedureName;
    }

    public String getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(String returnValue) {
        this.returnValue = returnValue;
    }

    public String getFailedText() {
        return failedText;
    }

    public void setFailedText(String failedText) {
        this.failedText = failedText;
    }

    public String getTriggerInfo() {
        return triggerInfo;
    }

    public void setTriggerInfo(String triggerInfo) {
        this.triggerInfo = triggerInfo;
    }

    public String getSentData() {
        return sentData;
    }

    public void setSentData(String sentData) {
        this.sentData = sentData;
    }

    public String getReceivedData() {
        return receivedData;
    }

    public void setReceivedData(String receivedData) {
        this.receivedData = receivedData;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getOldestInteresting() {
        return oldestInteresting;
    }

    public void setOldestInteresting(String oldestInteresting) {
        this.oldestInteresting = oldestInteresting;
    }

    public String getOldestActive() {
        return oldestActive;
    }

    public void setOldestActive(String oldestActive) {
        this.oldestActive = oldestActive;
    }

    public String getOldestSnapshot() {
        return oldestSnapshot;
    }

    public void setOldestSnapshot(String oldestSnapshot) {
        this.oldestSnapshot = oldestSnapshot;
    }

    public String getNextTransaction() {
        return nextTransaction;
    }

    public void setNextTransaction(String nextTransaction) {
        this.nextTransaction = nextTransaction;
    }

    private String addField(String body, String regex, String excludedRegex, String colName) {
        return addField(body, regex, new String[]{excludedRegex}, colName);
    }

    public long getTotalCacheMemory() {
        return totalCacheMemory;
    }

    public void setTotalCacheMemory(long totalCacheMemory) {
        this.totalCacheMemory = totalCacheMemory;
    }

    public long getRamCacheMemory() {
        return ramCacheMemory;
    }

    public void setRamCacheMemory(long ramCacheMemory) {
        this.ramCacheMemory = ramCacheMemory;
    }

    public long getDiskCacheMemory() {
        return diskCacheMemory;
    }

    public void setDiskCacheMemory(long diskCacheMemory) {
        this.diskCacheMemory = diskCacheMemory;
    }

    private String addField(String body, String regex, String[] excludedRegex, String colName) {
        Field field = parseField(body, regex, excludedRegex);
        try {
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
                case LogConstants.TIME_EXECUTION_COLUMN:
                    setTimeExecution(Long.parseLong(field.field));
                    break;
                case LogConstants.COUNT_READS_COLUMN:
                    setCountReads(Long.parseLong(field.field));
                    break;
                case LogConstants.COUNT_WRITES_COLUMN:
                    setCountWrites(Long.parseLong(field.field));
                    break;
                case LogConstants.COUNT_FETCHES_COLUMN:
                    setCountFetches(Long.parseLong(field.field));
                    break;
                case LogConstants.COUNT_MARKS_COLUMN:
                    setCountMarks(Long.parseLong(field.field));
                    break;
                case LogConstants.ID_STATEMENT_COLUMN:
                    setIdStatement(field.field);
                    break;
                case LogConstants.RECORDS_FETCHED_COLUMN:
                    setFetchedRecords(Long.parseLong(field.field));
                    break;
                case LogConstants.STATEMENT_TEXT_COLUMN:
                    setStatementText(field.field);
                    break;
                case LogConstants.PARAMETERS_TEXT_COLUMN:
                    setParamText(field.field);
                    break;
                case LogConstants.SORT_MEMORY_USAGE_TOTAL_COLUMN:
                    setTotalCacheMemory(Long.parseLong(field.field));
                    break;
                case LogConstants.SORT_MEMORY_USAGE_CACHED_COLUMN:
                    setRamCacheMemory(Long.parseLong(field.field));
                    break;
                case LogConstants.SORT_MEMORY_USAGE_ON_DISK_COLUMN:
                    setDiskCacheMemory(Long.parseLong(field.field));
                    break;
                default:
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
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
            case LogConstants.TIME_EXECUTION_COLUMN:
                return getTimeExecution();
            case LogConstants.COUNT_READS_COLUMN:
                return getCountReads();
            case LogConstants.COUNT_WRITES_COLUMN:
                return getCountWrites();
            case LogConstants.COUNT_FETCHES_COLUMN:
                return getCountFetches();
            case LogConstants.COUNT_MARKS_COLUMN:
                return getCountMarks();
            case LogConstants.ID_STATEMENT_COLUMN:
                return getIdStatement();
            case LogConstants.RECORDS_FETCHED_COLUMN:
                return getFetchedRecords();
            case LogConstants.STATEMENT_TEXT_COLUMN:
                return getStatementText();
            case LogConstants.PARAMETERS_TEXT_COLUMN:
                return getParamText();
            case LogConstants.PLAN_TEXT_COLUMN:
                return getPlanText();
            case LogConstants.TABLE_COUNTERS_COLUMN:
                return getTableCounters();
            case LogConstants.DECLARE_CONTEXT_VARIABLES_TEXT_COLUMN:
                return getDeclareContextVariablesText();
            case LogConstants.EXECUTOR_COLUMN:
                return getExecutor();
            case LogConstants.GRANTOR_COLUMN:
                return getGrantor();
            case LogConstants.PRIVILEGE_COLUMN:
                return getPrivilege();
            case LogConstants.PRIVILEGE_OBJECT_COLUMN:
                return getPrivilegeObject();
            case LogConstants.PRIVILEGE_USERNAME_COLUMN:
                return getPrivilegeUsername();
            case LogConstants.PRIVILEGE_ATTACHMENT_COLUMN:
                return getPrivilegeAttachment();
            case LogConstants.PRIVILEGE_TRANSACTION_COLUMN:
                return getPrivilegeTransaction();
            case LogConstants.FAILED_COLUMN:
                return getFailedText();
            case LogConstants.PROCEDURE_NAME_COLUMN:
                return getProcedureName();
            case LogConstants.RETURN_VALUE_COLUMN:
                return getReturnValue();
            case LogConstants.TRIGGER_INFO_COLUMN:
                return getTriggerInfo();
            case LogConstants.SENT_DATA_COLUMN:
                return getSentData();
            case LogConstants.RECEIVED_DATA_COLUMN:
                return getReceivedData();
            case LogConstants.ERROR_MESSAGE_COLUMN:
                return getErrorMessage();
            case LogConstants.OLDEST_INTERESTING_COLUMN:
                return getOldestInteresting();
            case LogConstants.OLDEST_ACTIVE_COLUMN:
                return getOldestActive();
            case LogConstants.OLDEST_SNAPSHOT_COLUMN:
                return getOldestSnapshot();
            case LogConstants.NEXT_TRANSACTION_COLUMN:
                return getNextTransaction();
            case LogConstants.SORT_MEMORY_USAGE_TOTAL_COLUMN:
                return getTotalCacheMemory();
            case LogConstants.SORT_MEMORY_USAGE_CACHED_COLUMN:
                return getRamCacheMemory();
            case LogConstants.SORT_MEMORY_USAGE_ON_DISK_COLUMN:
                return getDiskCacheMemory();
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

    @Override
    public String toString() {
        return "LogMessage{" +
                "body='" + body + '\'' +
                '}';
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
