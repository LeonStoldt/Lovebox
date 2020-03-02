package de.stoldt.lovebox.telegram.message;

public enum MessageType {
    TEXT("text"),
    PICTURE("picture"),
    VIDEO("video"),
    AUDIO("audio");

    private final String templateName;

    MessageType(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateName() {
        return templateName;
    }
}
