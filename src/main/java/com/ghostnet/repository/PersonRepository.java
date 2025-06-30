package com.ghostnet.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ghostnet.model.Person;

public interface PersonRepository extends JpaRepository<Person, Long> {
    
    Optional<Person> findByTelefonnummer(String telefonnummer);
    Optional<Person> findByTelefonnummerAndRolle(String telefonnummer, Person.Rolle rolle);
    Object findByName(String string);
}
