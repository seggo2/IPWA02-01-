package com.ghostnet.controller;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ghostnet.model.Person;
import com.ghostnet.repository.PersonRepository;
;

@RestController
@RequestMapping("/api/person")
public class PersonController {
    
    private final PersonRepository repo;

    public PersonController(PersonRepository repo){
        this.repo = repo;
    }

    @PostMapping
    public Person create(@RequestBody Person person){
        return repo.save(person);
    }
    
    @GetMapping
    public List<Person> findAll(){
        return repo.findAll();
    }

}
