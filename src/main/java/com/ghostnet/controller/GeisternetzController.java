package com.ghostnet.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ghostnet.model.Geisternetz;
import com.ghostnet.model.Person;
import com.ghostnet.repository.GeisternetzRepository;
import com.ghostnet.repository.PersonRepository;

@RestController
@RequestMapping("/api/netze")
public class GeisternetzController {
    
    private final GeisternetzRepository repo;
    private final PersonRepository personRepo;


    public GeisternetzController(GeisternetzRepository repo, PersonRepository personRepo){
        this.repo = repo;
        this.personRepo = personRepo;
    }

    @PostMapping
    public Geisternetz create(@RequestBody Geisternetz netz){
        return repo.save(netz);
    }

    @GetMapping
    public List<Geisternetz> findAll(){
        return repo.findAll();
    }

    @PutMapping("/{netzId}/reservieren/{personId}")
    public Geisternetz reservieren(@PathVariable Long netzId,@PathVariable Long personId) {
        Geisternetz netz = repo.findById(netzId)
        .orElseThrow(() -> new RuntimeException("Netz nicht gefunden"));

        Person person = personRepo.findById(personId)
        .orElseThrow(() -> new RuntimeException("Person nicht gefunden"));


        if (netz.getBergendePerson() != null) {
            throw new RuntimeException("Netz ist bereits reserviert.");
        }

        if (person.getRolle() != Person.Rolle.BERGEND) {
            throw new RuntimeException("Nur bergende Personen dürfen reservieren.");
        }

        netz.setBergendePerson(person);
        netz.setStatus(Geisternetz.Status.BERGUNG_BEVORSTEHEND);
        return repo.save(netz);
    }

    @PutMapping("/{netzId}/geborgen")
    public Geisternetz alsGeborgenMelden(@PathVariable Long netzId){
        Geisternetz netz = repo.findById(netzId).orElseThrow();

        if (netz.getBergendePerson() == null) {
            throw new RuntimeException("Nur reservierte Netze können als geborgen gemeldet werde");
        }

        netz.setStatus(Geisternetz.Status.GEBORGEN);
        return repo.save(netz);
    }

    @PutMapping("/{netzId}/verschollen/{personId}")
    public Geisternetz alsVerschollenMelden(@PathVariable Long netzId,@PathVariable Long personId) {

        Geisternetz netz = repo.findById(netzId).orElseThrow();
        Person person = personRepo.findById(personId).orElseThrow();

        if (person.getTelefonnummer() == null || person.getTelefonnummer().isBlank()) {
            throw new RuntimeException("Anonyme Personen dürfen kein Netz als verschollen melden.");
        }

        netz.setStatus(Geisternetz.Status.VERSCHOLLEN);
        return repo.save(netz);
    }

}
