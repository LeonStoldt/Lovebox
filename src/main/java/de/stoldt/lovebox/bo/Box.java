package de.stoldt.lovebox.bo;

import de.stoldt.lovebox.TokenGenerator;
import de.stoldt.lovebox.persistence.entity.BoxEntity;

public class Box {

    private String token;
    private Long publisherId;

    public Box() {
        this.token = TokenGenerator.generate();
    }

    public Box(BoxEntity entity) {
        this.token = entity.getGeneratedToken();
        this.publisherId = entity.getPublisherId();
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(Long publisherId) {
        this.publisherId = publisherId;
    }
}
