package com.ghostnet.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ghostnet.model.Person;

public interface PersonRepository extends JpaRepository<Person, Long> {

    Object findByName(String string);
}
