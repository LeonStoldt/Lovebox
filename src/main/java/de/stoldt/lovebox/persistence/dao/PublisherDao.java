package de.stoldt.lovebox.persistence.dao;

import de.stoldt.lovebox.bo.Publisher;
import de.stoldt.lovebox.persistence.entity.PublisherEntity;
import de.stoldt.lovebox.persistence.repository.PublisherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PublisherDao {

    private final PublisherRepository repository;

    @Autowired
    public PublisherDao(PublisherRepository repository) {
        this.repository = repository;
    }

    public Publisher getPublisher() {
        List<PublisherEntity> allPublisher = repository.findAll();
        return !allPublisher.isEmpty()
                ? new Publisher(allPublisher.get(0))
                : null;
    }

    public void save(@NonNull Publisher newPublisher) {
        repository.deleteAll();
        repository.save(new PublisherEntity(newPublisher));
    }
}
