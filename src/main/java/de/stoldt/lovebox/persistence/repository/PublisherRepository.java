package de.stoldt.lovebox.persistence.repository;

import de.stoldt.lovebox.persistence.entity.PublisherEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PublisherRepository extends JpaRepository<PublisherEntity, Long> {

}
