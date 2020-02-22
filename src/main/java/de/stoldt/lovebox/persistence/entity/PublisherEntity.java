package de.stoldt.lovebox.persistence.entity;

import com.pengrad.telegrambot.model.Chat.Type;
import de.stoldt.lovebox.bo.Publisher;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "publisher")
public class PublisherEntity {

    @Id
    private Long chatId;

    private String userName;

    @Enumerated(EnumType.STRING)
    private Type type;

    private String firstName;

    private String lastName;

    private String token;

    public PublisherEntity() {
    }

    public PublisherEntity(Publisher publisher) {
        this.chatId = publisher.getChatId();
        this.userName = publisher.getUserName();
        this.type = publisher.getType();
        this.firstName = publisher.getFirstName();
        this.lastName = publisher.getLastName();
        this.token = publisher.getToken();
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
