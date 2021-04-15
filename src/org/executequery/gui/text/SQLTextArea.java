package org.executequery.gui.text;

import org.executequery.GUIUtilities;
import org.executequery.gui.BaseDialog;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.gui.browser.TreeFindAction;
import org.executequery.gui.browser.tree.SchemaTree;
import org.executequery.gui.text.syntax.SyntaxStyle;
import org.executequery.gui.text.syntax.TokenTypes;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Token;
import org.underworldlabs.sqlLexer.CustomTokenMakerFactory;
import org.underworldlabs.sqlLexer.SqlLexerTokenMaker;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.TreeSet;

public class SQLTextArea extends RSyntaxTextArea {
    private CustomTokenMakerFactory tokenMakerFactory = new CustomTokenMakerFactory();
    public SQLTextArea()
    {
        super();
        setDocument(new RSyntaxDocument(tokenMakerFactory,"antlr/sql"));
        setSyntaxEditingStyle("antlr/sql");
        initialiseStyles();
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.isControlDown()||e.getClickCount()>1) {
                    int cursor = getCaretPosition();
                    Token token = getTokenListFor(cursor,cursor);
                    if(token.getType()==Token.PREPROCESSOR) {
                        String s = token.getLexeme();
                        if (s != null) {
                           /* s = s.replace("$", "\\$");
                            TreeFindAction action = new TreeFindAction();
                            SchemaTree tree = ((ConnectionsTreePanel) GUIUtilities.getDockedTabComponent(ConnectionsTreePanel.PROPERTY_KEY)).getTree();
                            action.install(tree);
                            action.findString(tree, s, ((ConnectionsTreePanel) GUIUtilities.getDockedTabComponent(ConnectionsTreePanel.PROPERTY_KEY)).getHostNode());
                            BaseDialog dialog = new BaseDialog("find", false);
                            JPanel panel = new JPanel();
                            JList jList = action.getResultsList();
                            if (jList.getModel().getSize() == 1) {
                                jList.setSelectedIndex(0);
                                action.listValueSelected((TreePath) jList.getSelectedValue());
                            } else {
                                jList.addPropertyChangeListener(new PropertyChangeListener() {
                                    @Override
                                    public void propertyChange(PropertyChangeEvent evt) {
                                        if (jList.getModel().getSize() == 0)
                                            dialog.finished();
                                    }
                                });
                                JScrollPane scrollPane = new JScrollPane(jList);
                                panel.add(scrollPane);
                                dialog.addDisplayComponent(panel);
                                dialog.display();
                            }*/
                        }
                    }
                }
            }
        });

    }

    private void createStyle(int type, Color fcolor,
                             Color bcolor,String fontname,int style,int fontSize,boolean underline) {
        getSyntaxScheme().getStyle(type).foreground=fcolor;
        getSyntaxScheme().getStyle(type).background=bcolor;
        getSyntaxScheme().getStyle(type).underline = underline;
        getSyntaxScheme().getStyle(type).font=new Font(fontname,style,fontSize);
    }

    private void initialiseStyles() {
        int fontSize = SystemProperties.getIntProperty("user", "sqlsyntax.font.size");
        String fontName = SystemProperties.getProperty("user", "sqlsyntax.font.name");

        createStyle(Token.ERROR_CHAR, Color.red, null,fontName,Font.PLAIN,fontSize,false);
        createStyle(Token.RESERVED_WORD_2, Color.blue, null,fontName,Font.PLAIN,fontSize,false);

        // -----------------------------
        // user defined styles
        int fontStyle = SystemProperties.getIntProperty("user", "sqlsyntax.style.multicomment");
        Color color = SystemProperties.getColourProperty("user", "sqlsyntax.colour.multicomment");
        createStyle(Token.COMMENT_MULTILINE, color,  null,fontName,fontStyle,fontSize,false);

        color = SystemProperties.getColourProperty("user", "sqlsyntax.colour.normal");
        fontStyle = SystemProperties.getIntProperty("user", "sqlsyntax.style.normal");
        createStyle(Token.IDENTIFIER, color,  null,fontName,fontStyle,fontSize,false);

        color = SystemProperties.getColourProperty("user", "sqlsyntax.colour.singlecomment");
        fontStyle = SystemProperties.getIntProperty("user", "sqlsyntax.style.singlecomment");
        createStyle(Token.COMMENT_EOL, color,  null,fontName,fontStyle,fontSize,false);

        color = SystemProperties.getColourProperty("user", "sqlsyntax.colour.keyword");
        fontStyle = SystemProperties.getIntProperty("user", "sqlsyntax.style.keyword");
        createStyle(Token.RESERVED_WORD, color,  null,fontName,fontStyle,fontSize,false);

        color = SystemProperties.getColourProperty("user", "sqlsyntax.colour.quote");
        fontStyle = SystemProperties.getIntProperty("user", "sqlsyntax.style.quote");
        createStyle(Token.LITERAL_STRING_DOUBLE_QUOTE, color,  null,fontName,fontStyle,fontSize,false);

        color = SystemProperties.getColourProperty("user", "sqlsyntax.colour.number");
        fontStyle = SystemProperties.getIntProperty("user", "sqlsyntax.style.number");
        createStyle(Token.LITERAL_BOOLEAN, color,  null,fontName,fontStyle,fontSize,false);

        color = SystemProperties.getColourProperty("user", "sqlsyntax.colour.literal");
        fontStyle = SystemProperties.getIntProperty("user", "sqlsyntax.style.literal");
        createStyle(Token.LITERAL_BOOLEAN, color,  null,fontName,fontStyle,fontSize,false);

        color = SystemProperties.getColourProperty("user", "sqlsyntax.colour.operator");
        fontStyle = SystemProperties.getIntProperty("user", "sqlsyntax.style.operator");
        createStyle(Token.OPERATOR, color,  null,fontName,fontStyle,fontSize,false);

        color = SystemProperties.getColourProperty("user", "sqlsyntax.colour.dbobjects");
        fontStyle = SystemProperties.getIntProperty("user", "sqlsyntax.style.dbobjects");
        createStyle(Token.PREPROCESSOR, color,  null,fontName,fontStyle,fontSize,true);
        setCurrentLineHighlightColor(SystemProperties.getColourProperty("user", "editor.display.linehighlight.colour"));
    }

    public void setDbobjects(TreeSet<String> dbobjects) {
        SqlLexerTokenMaker maker = (SqlLexerTokenMaker) tokenMakerFactory.getTokenMaker("antlr/sql");
        maker.setDbobjects(dbobjects);
    }
}
