package de.stoldt.lovebox.persistence.dao;

import de.stoldt.lovebox.bo.Box;
import de.stoldt.lovebox.persistence.entity.BoxEntity;
import de.stoldt.lovebox.persistence.repository.BoxRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BoxDao {

    private final BoxRepository repository;

    @Autowired
    public BoxDao(BoxRepository repository) {
        this.repository = repository;
    }

    public @NonNull Box getBox() {
        List<BoxEntity> allBoxes = repository.findAll();
        return !allBoxes.isEmpty()
                ? new Box(allBoxes.get(0))
                : initializeAndSaveNewBox();
    }

    public Box initializeAndSaveNewBox() {
        Box emptyBox = new Box();
        save(emptyBox);
        return emptyBox;
    }

    public void save(@NonNull Box newBox) {
        repository.deleteAll();
        repository.save(new BoxEntity(newBox));
    }

}
