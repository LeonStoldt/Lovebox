package de.stoldt.lovebox.persistence.dao;

import de.stoldt.lovebox.bo.Box;
import de.stoldt.lovebox.persistence.entity.BoxEntity;
import de.stoldt.lovebox.persistence.repository.BoxRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Stream;

@Component
public class BoxDao {

    private final BoxRepository repository;

    @Autowired
    public BoxDao(BoxRepository repository) {
        this.repository = repository;
    }

    public Optional<Box> getBoxBy(String token) {
        return repository.findBoxEntityByGeneratedToken(token).map(Box::new);
    }

    public Optional<Box> getBoxBy(Long publisherId) {
        return repository.findBoxEntityByPublisherId(publisherId).map(Box::new);
    }

    public Stream<Box> getAll() {
        return repository
                .findAll()
                .stream()
                .map(Box::new);
    }

    public String getNextAvailableToken() {
        return repository.findFirstByPublisherIdIsNull().map(BoxEntity::getGeneratedToken).orElse(createNewBox().getToken());
    }

    private Box createNewBox() {
        Box box = new Box();
        save(box);
        return box;
    }

    public void save(@NonNull Box box) {
        repository.save(new BoxEntity(box));
    }

    public void remove(Long publisherId) {
        repository.findBoxEntityByPublisherId(publisherId).ifPresent(repository::delete);
    }
}
