package de.stoldt.lovebox.telegram.message;

import com.pengrad.telegrambot.request.GetFile;

public class DataMessage extends AbstractMessage {

    private final GetFile file;

    public DataMessage(MessageType messageType, GetFile file) {
        super(messageType);
        this.file = file;
    }

    public GetFile getFile() {
        return file;
    }
}
