/*
 * InformationDialog.java
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

package org.executequery.gui;

import org.executequery.log.Log;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * @author Takis Diakoumis
 */
public class InformationDialog extends ActionDialog {

    public static final int RESOURCE_PATH_VALUE = 0;
    public static final int TEXT_CONTENT_VALUE = 1;

    protected final JEditorPane editorPane;
    protected final String loadedText;

    public InformationDialog(String name, String value, int valueType, String charSet) {
        this(name, value, valueType, charSet, "text/plain");
    }

    public InformationDialog(String name, String value, int valueType, String charSet, String contentType) {
        super(name, true);

        loadedText = valueType == RESOURCE_PATH_VALUE ?
                loadText(value, charSet) :
                value;

        editorPane = new JEditorPane();
        editorPane.setContentType(contentType);
        editorPane.setText(loadedText);
        editorPane.setCaretPosition(0);
        editorPane.setEditable(false);
    }

    protected JPanel buildDisplayComponent() {

        JPanel viewPanel = new JPanel(new GridBagLayout());
        viewPanel.setPreferredSize(new Dimension(650, 500));
        viewPanel.add(
                new JScrollPane(editorPane),
                new GridBagHelper().setInsets(5, 5, 5, 5).fillBoth().spanX().spanY().get()
        );

        return viewPanel;
    }

    private String loadText(String value, String charSet) {
        try {
            if (charSet != null)
                return FileUtils.loadResource(value, charSet);
            return FileUtils.loadResource(value);

        } catch (IOException e) {
            Log.error(e.getMessage(), e);
        }

        return value;
    }

    @Override
    public final void display() {
        JPanel viewPanel = buildDisplayComponent();
        addDisplayComponent(viewPanel);
        super.display();
    }

}
