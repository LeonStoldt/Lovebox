package de.stoldt.lovebox.persistence.dao;

import de.stoldt.lovebox.bo.Box;
import de.stoldt.lovebox.persistence.entity.BoxEntity;
import de.stoldt.lovebox.persistence.repository.BoxRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class BoxDao {

    private final BoxRepository repository;
    private Box box;

    @Autowired
    public BoxDao(BoxRepository repository) {
        this.repository = repository;
    }

    public Box getBox() {
        if (box == null) {
            box = repository
                    .findAll()
                    .stream()
                    .findFirst()
                    .map(Box::new)
                    .orElse(null);
        }
        return box;
    }

    public void save(@NonNull Box newBox) {
        repository.save(new BoxEntity(box));
    }

}
