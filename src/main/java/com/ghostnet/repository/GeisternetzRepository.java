package com.ghostnet.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ghostnet.model.Geisternetz;
import com.ghostnet.model.Person;

@Repository
public interface GeisternetzRepository extends JpaRepository<Geisternetz, Long> {
    
    List<Geisternetz> findByStatus(Geisternetz.Status status);
    List<Geisternetz> findByBergendePersonAndStatus(Person person, Geisternetz.Status status);


}
