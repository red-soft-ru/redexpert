package org.executequery.gui.editor.history;

import java.nio.file.Path;
import java.nio.file.Paths;

/// @author Aleksey Kozlov
public class EditorData {
    private boolean autosaveEnabled;
    private int splitLocation;
    private Path editorPath;
    private int number;

    public EditorData() {
    }

    public EditorData(String editorPath, String number, String splitLocation, String autosaveEnabled) {
        this(
                editorPath,
                number != null ? Integer.parseInt(number) : -1,
                splitLocation != null ? Integer.parseInt(splitLocation) : 0,
                Boolean.parseBoolean(autosaveEnabled)
        );
    }

    public EditorData(String editorPath, int number, int splitLocation, boolean autosaveEnabled) {
        this.editorPath = Paths.get(editorPath).normalize();
        this.autosaveEnabled = autosaveEnabled;
        this.splitLocation = splitLocation;
        this.number = number;
    }

    public boolean useSameFile(Path pathToFile) {
        return editorPath != null && editorPath.equals(pathToFile);
    }

    // ---

    public int getSplitLocation() {
        return splitLocation;
    }

    public void setSplitLocation(int splitLocation) {
        this.splitLocation = splitLocation;
    }

    public boolean isAutosaveEnabled() {
        return autosaveEnabled;
    }

    public void setAutosaveEnabled(boolean autosaveEnabled) {
        this.autosaveEnabled = autosaveEnabled;
    }

    public Path getEditorPath() {
        return editorPath;
    }

    public void setEditorPath(Path editorPath) {
        this.editorPath = editorPath;
    }

    public void setEditorPath(String editorPath) {
        setEditorPath(Paths.get(editorPath).normalize());
    }

    public int getNumber() {
        return number;
    }

}