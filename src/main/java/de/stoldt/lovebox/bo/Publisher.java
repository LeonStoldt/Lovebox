package de.stoldt.lovebox.bo;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.User;
import de.stoldt.lovebox.persistence.entity.PublisherEntity;

public class Publisher {

    private final Long chatId;
    private final String userName;
    private final Chat.Type type;
    private final String firstName;
    private final String lastName;
    private String token;

    public Publisher(PublisherEntity entity) {
        this.chatId = entity.getChatId();
        this.userName = entity.getUserName();
        this.type = entity.getType();
        this.firstName = entity.getFirstName();
        this.lastName = entity.getLastName();
        this.token = entity.getToken();
    }

    public Publisher(Chat chat) throws IllegalAccessException {
        if (chat.type().equals(Chat.Type.Private)) {
            this.chatId = chat.id();
            this.userName = chat.username();
            this.type = chat.type();
            this.firstName = chat.firstName();
            this.lastName = chat.lastName();
        } else {
            throw new IllegalAccessException("Invalid Chat Type of Request");
        }
    }

    public Long getChatId() {
        return chatId;
    }

    public String getUserName() {
        return userName;
    }

    public Chat.Type getType() {
        return type;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
