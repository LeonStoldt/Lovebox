package de.stoldt.lovebox.bo;

import de.stoldt.lovebox.persistence.entity.BoxEntity;

import java.util.UUID;

public class Box {

    private final String token;
    private Long publisherId;

    public Box() {
        this.token = UUID.randomUUID().toString();
    }

    public Box(BoxEntity entity) {
        this.token = entity.getGeneratedToken();
        this.publisherId = entity.getPublisherId();
    }

    public String getToken() {
        return token;
    }

    public Long getPublisherId() {
        return publisherId;
    }

    public Box withPublisherId(Long publisherId) {
        this.publisherId = publisherId;
        return this;
    }
}
