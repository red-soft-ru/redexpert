package org.executequery.gui.browser.managment.tracemanager.net;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.underworldlabs.traceparser.RedTraceBaseListener;
import org.underworldlabs.traceparser.RedTraceLexer;
import org.underworldlabs.traceparser.RedTraceParser;

import java.util.List;

public class SessionInfo {
    private String id;
    private String name;
    private String user;
    private String datetime;
    private String flags;
    private String body;

    public SessionInfo(String body) {
        init(body);
    }

    private void init(String body) {
        this.setBody(body);
        RedTraceParser parser = buildParser(body);
        try {
            ParseTree tree = parser.session_info();
            ParseTreeWalker walker = new ParseTreeWalker();
            walker.walk(new RedTraceBaseListener() {

                @Override
                public void enterSession_info(RedTraceParser.Session_infoContext ctx) {
                    setId(textFromRuleContext(ctx.id()));
                    setName(textFromRuleContext(ctx.name_session()));
                    setUser(textFromRuleContext(ctx.username()));
                    setDatetime(textFromRuleContext(ctx.datetime()));
                    setFlags(textFromRuleContext(ctx.flags()));
                }
            }, tree);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getFlags() {
        return flags;
    }

    public void setFlags(String flags) {
        this.flags = flags;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
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

    @Override
    public String toString() {
        return getName();
    }
}
