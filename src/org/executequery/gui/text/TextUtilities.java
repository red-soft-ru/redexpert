/*
 * TextUtilities.java
 *
 * Copyright (C) 2002-2017 Takis Diakoumis
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.executequery.gui.text;

import org.apache.commons.lang.StringUtils;
import org.executequery.GUIUtilities;
import org.executequery.components.FileChooserDialog;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.io.*;

/**
 * @author Takis Diakoumis
 */
public class TextUtilities {

    public static int save(JTextComponent textComponent) {
        TextFileWriter writer = new TextFileWriter(textComponent.getText());
        int result = writer.write();
        writer = null;
        return result;
    }

    public static void selectAll(JTextComponent textComponent) {
        textComponent.selectAll();
        //    textComponent.setCaretPosition(0);
        //    textComponent.moveCaretPosition(textComponent.getText().length());
    }

    public static void selectNone(JTextComponent textComponent) {
        if (textComponent.getSelectionStart() >= 0)
            textComponent.setCaretPosition(textComponent.getSelectionStart());
    }

    public static void changeSelectionToCamelCase(JTextComponent textComponent) {

        String selectedText = textComponent.getSelectedText();
        if (StringUtils.isBlank(selectedText)) {

            return;
        }

        String breakChars = "-_ \t";

        boolean nextIsUpper = false;
        StringBuilder sb = new StringBuilder();
        char[] chars = selectedText.toCharArray();
        for (int i = 0; i < chars.length; i++) {

            char _char = chars[i];
            if (breakChars.indexOf(_char) != -1) {

                if (i > 0) {

                    nextIsUpper = true;
                }

            } else {

                if (nextIsUpper) {

                    sb.append(Character.toUpperCase(_char));
                    nextIsUpper = false;

                } else {

                    sb.append(Character.toLowerCase(_char));
                }
            }
        }

        textComponent.replaceSelection(sb.toString());
    }

    public static void changeSelectionToUnderscore(JTextComponent textComponent) {

        String selectedText = textComponent.getSelectedText();
        if (StringUtils.isBlank(selectedText)) {

            return;
        }

        String breakChars = "-_ \t";

        boolean lastCharWasUnderscore = false;
        StringBuilder sb = new StringBuilder();
        char[] chars = selectedText.toCharArray();
        for (int i = 0; i < chars.length; i++) {

            char _char = chars[i];
            if (breakChars.indexOf(_char) != -1) {

                sb.append("_");
                lastCharWasUnderscore = true;

            } else if (Character.isUpperCase(_char)) {

                if (!lastCharWasUnderscore) {

                    sb.append("_");
                }
                sb.append(_char);
                lastCharWasUnderscore = false;

            } else {

                sb.append(_char);
                lastCharWasUnderscore = false;
            }

        }

        textComponent.replaceSelection(sb.toString().toLowerCase());
    }

    public static void changeSelectionCase(JTextComponent textComponent, boolean upper) {

        String selectedText = textComponent.getSelectedText();
        if (StringUtils.isBlank(selectedText)) {

            return;
        }

        if (upper) {
            selectedText = selectedText.toUpperCase();

        } else {

            selectedText = selectedText.toLowerCase();
        }
        textComponent.replaceSelection(selectedText);

    }

    public static void deleteLine(JTextComponent textComponent) {
        char newLine = '\n';
        String _newLine = "\n";
        int caretIndex = textComponent.getCaretPosition();

        String text = textComponent.getText();
        StringBuilder sb = new StringBuilder(text);

        int endOfLineIndexBefore = -1;
        int endOfLineIndexAfter = sb.indexOf(_newLine, caretIndex);

        char[] textChars = text.toCharArray();

        for (int i = 0; i < textChars.length; i++) {

            if (i >= caretIndex) {
                break;
            } else {

                if (textChars[i] == newLine)
                    endOfLineIndexBefore = i;

            }

        }

        if (endOfLineIndexBefore == -1) {
            endOfLineIndexBefore = 0;
        }

        if (endOfLineIndexAfter == -1) {
            sb.delete(endOfLineIndexBefore, sb.length());
        } else if (endOfLineIndexBefore == -1) {
            sb.delete(0, endOfLineIndexAfter + 1);
        } else if (endOfLineIndexBefore == 0 && endOfLineIndexAfter == 0) {
            sb.deleteCharAt(0);
        } else {
            sb.delete(endOfLineIndexBefore, endOfLineIndexAfter);
        }

        textComponent.setText(sb.toString());

        if (endOfLineIndexBefore + 1 > sb.length()) {

            textComponent.setCaretPosition(endOfLineIndexBefore == -1 ? 0 : endOfLineIndexBefore);

        } else {

            textComponent.setCaretPosition(endOfLineIndexBefore + 1);
        }

    }

    public static void deleteWord(JTextComponent textComponent) {
        char space = ' ';
        String _space = " ";
        int caretIndex = textComponent.getCaretPosition();

        String text = textComponent.getText();
        StringBuilder sb = new StringBuilder(text);

        int startOfWordIndex = -1;
        int endOfWordIndex = sb.indexOf(_space, caretIndex);

        char[] textChars = text.toCharArray();

        for (int i = 0; i < textChars.length; i++) {

            if (i >= caretIndex) {
                break;
            } else {

                if (textChars[i] == space)
                    startOfWordIndex = i;

            }

        }

        if (endOfWordIndex == -1)
            return;

        else if (startOfWordIndex == 0 && endOfWordIndex == 0)
            return;

        else if (startOfWordIndex == endOfWordIndex)
            return;

        sb.delete(startOfWordIndex + 1, endOfWordIndex);
        textComponent.setText(sb.toString());
        textComponent.setCaretPosition(startOfWordIndex + 1);
    }

    public static void deleteSelection(JTextComponent textComponent) {
        textComponent.replaceSelection("");
    }

    public static void insertFromFile(JTextComponent textComponent) {
        StringBuffer buf = null;
        String text = null;

        FileChooserDialog fileChooser = new FileChooserDialog();
        fileChooser.setDialogTitle("Insert from file");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        int result = fileChooser.showDialog(GUIUtilities.getInFocusDialogOrWindow(), "Insert");

        if (result == JFileChooser.CANCEL_OPTION)
            return;

        File file = fileChooser.getSelectedFile();

        try {
            FileInputStream input = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            buf = new StringBuffer(10000);

            char newLine = '\n';

            while ((text = reader.readLine()) != null)
                buf.append(text).append(newLine);

            reader.close();
            reader = null;
            input.close();
            input = null;

            int index = textComponent.getCaretPosition();
            StringBuffer sb = new StringBuffer(textComponent.getText());
            sb.insert(index, buf);
            textComponent.setText(sb.toString());
            textComponent.setCaretPosition(index + buf.length());

        } catch (OutOfMemoryError e) {
            buf = null;
            text = null;
            System.gc();
            GUIUtilities.displayErrorMessage("Out of Memory.\nThe file is " +
                    "too large to\nopen for viewing.");
        } catch (IOException e) {
            e.printStackTrace();
            String sb = "An error occurred opening the selected file." +
                    "\n\nThe system returned:\n" +
                    e.getMessage();
            GUIUtilities.displayExceptionErrorDialog(sb, e);
        }

    }

    public static void insertLineAfter(JTextComponent textComponent) {
        String newLine = "\n";
        int caretIndex = textComponent.getCaretPosition();

        StringBuilder sb = new StringBuilder(textComponent.getText());

        int endOfLineIndex = sb.indexOf(newLine, caretIndex);

        int length = sb.length();

        if (caretIndex == length || endOfLineIndex == length)
            sb.append(newLine);
        else
            sb.insert(endOfLineIndex == -1 ? 0 : endOfLineIndex, newLine);

        textComponent.setText(sb.toString());
        textComponent.setCaretPosition(endOfLineIndex == -1 ? length : endOfLineIndex + 1);
        sb = null;
    }

    public static void insertLineBefore(JTextComponent textComponent) {
        int caretIndex = textComponent.getCaretPosition();
        int insertIndex = -1;
        char newLine = '\n';

        String text = textComponent.getText();
        char[] textChars = text.toCharArray();

        for (int i = 0; i < textChars.length; i++) {

            if (i > caretIndex) {
                break;
            } else {

                if (textChars[i] == newLine)
                    insertIndex = i;

            }

        }

        StringBuilder sb = new StringBuilder(text);
        sb.insert(insertIndex == -1 ? 0 : insertIndex, newLine);

        textComponent.setText(sb.toString());
        textComponent.setCaretPosition(insertIndex + 1);
    }

}











