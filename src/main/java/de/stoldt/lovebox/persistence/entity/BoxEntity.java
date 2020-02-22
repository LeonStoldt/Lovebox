package de.stoldt.lovebox.persistence.entity;

import de.stoldt.lovebox.bo.Box;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "box")
public class BoxEntity {

    @Id
    private String generatedToken;
    private Long publisherId;

    public BoxEntity() {
    }

    public BoxEntity(Box box) {
        this.publisherId = box.getPublisherId();
        this.generatedToken = box.getToken();
    }

    public String getGeneratedToken() {
        return generatedToken;
    }

    public void setGeneratedToken(String generatedToken) {
        this.generatedToken = generatedToken;
    }

    public Long getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(Long publisherId) {
        this.publisherId = publisherId;
    }
}
