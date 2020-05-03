package de.stoldt.lovebox.persistence.repository;

import de.stoldt.lovebox.persistence.entity.BoxEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BoxRepository extends JpaRepository<BoxEntity, String> {

    Optional<BoxEntity> findBoxEntityByPublisherId(Long publisherId);

    Optional<BoxEntity> findBoxEntityByGeneratedToken(String token);

    Optional<BoxEntity> findFirstByPublisherIdIsNull();

}
