package de.stoldt.lovebox.persistence.dao;

import de.stoldt.lovebox.bo.Publisher;
import de.stoldt.lovebox.persistence.entity.PublisherEntity;
import de.stoldt.lovebox.persistence.repository.PublisherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class PublisherDao {

    private final PublisherRepository repository;
    private Publisher publisher;

    @Autowired
    public PublisherDao(PublisherRepository repository) {
        this.repository = repository;
    }

    public Publisher getPublisher() {
        if (publisher == null) {
            publisher = repository
                    .findAll()
                    .stream()
                    .findFirst()
                    .map(Publisher::new)
                    .orElse(null);
        }
        return publisher;
    }

    public void save(@NonNull Publisher newPublisher) {
        repository.save(new PublisherEntity(newPublisher));
    }
}
