package org.underworldlabs.statParser;

public class Encryption {
    private final int pages;
    private final int encrypted;
    private final int unencrypted;

    public Encryption(int pages, int encrypted, int unencrypted) {
        this.pages = pages;
        this.encrypted = encrypted;
        this.unencrypted = unencrypted;
    }

    public int getPages() {
        return pages;
    }

    public int getEncrypted() {
        return encrypted;
    }

    public int getUnencrypted() {
        return unencrypted;
    }
}