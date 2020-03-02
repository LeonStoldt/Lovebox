package de.stoldt.lovebox.telegram.message;

public class TextMessage extends AbstractMessage {

    private final String text;

    public TextMessage(String text) {
        super(MessageType.TEXT);
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
