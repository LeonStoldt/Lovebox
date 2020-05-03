package de.stoldt.lovebox.persistence.dao;

import de.stoldt.lovebox.bo.Publisher;
import de.stoldt.lovebox.persistence.entity.PublisherEntity;
import de.stoldt.lovebox.persistence.repository.PublisherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PublisherDao {

    private final PublisherRepository repository;

    @Autowired
    public PublisherDao(PublisherRepository repository) {
        this.repository = repository;
    }

    public Optional<Publisher> getPublisherFor(Long chatId) {
        return repository.findPublisherEntityByChatId(chatId).map(Publisher::new);
    }

    public Collection<Publisher> getAll() {
        return repository.findAll().stream().map(Publisher::new).collect(Collectors.toList());
    }

    public void save(@NonNull Publisher newPublisher) {
        repository.save(new PublisherEntity(newPublisher));
    }
}
