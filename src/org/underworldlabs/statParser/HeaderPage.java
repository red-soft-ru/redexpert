package org.underworldlabs.statParser;

import java.util.ArrayList;
import java.util.List;

public class HeaderPage {
    List<String> parameterList;

    public HeaderPage() {
        parameterList = new ArrayList<>();
        parameterList.add("Flags");
        parameterList.add("Checksum");
        parameterList.add("Generation");
        parameterList.add("System Change Number");
        parameterList.add("Page size");
        parameterList.add("Server");
        parameterList.add("ODS version");
        parameterList.add("Oldest transaction");
        parameterList.add("Oldest active");
        parameterList.add("Oldest snapshot");
        parameterList.add("Next transaction");
        parameterList.add("Autosweep gap");
        parameterList.add("Bumped transaction");
        parameterList.add("Sequence number");
        parameterList.add("Next attachment ID");
        parameterList.add("Implementation ID");
        parameterList.add("Implementation");
        parameterList.add("Shadow count");
        parameterList.add("Page buffers");
        parameterList.add("Next header page");
        parameterList.add("Database dialect");
        parameterList.add("Creation date");
    }


}
