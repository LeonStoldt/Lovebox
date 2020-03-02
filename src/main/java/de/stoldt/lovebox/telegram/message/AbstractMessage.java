package de.stoldt.lovebox.telegram.message;

public abstract class AbstractMessage {

    private final MessageType messageType;

    public AbstractMessage(MessageType messageType) {
        this.messageType = messageType;
    }

    public MessageType getMessageType() {
        return messageType;
    }
}
