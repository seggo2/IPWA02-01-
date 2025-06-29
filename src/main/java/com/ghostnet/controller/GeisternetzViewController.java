package com.ghostnet.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ghostnet.model.Geisternetz;
import com.ghostnet.model.Person;
import com.ghostnet.repository.GeisternetzRepository;
import com.ghostnet.repository.PersonRepository;

@Controller
public class GeisternetzViewController {

    private final GeisternetzRepository geisternetzRepository;
    private final PersonRepository personRepository;

    public GeisternetzViewController(GeisternetzRepository geisternetzRepository, PersonRepository personRepository) {
        this.geisternetzRepository = geisternetzRepository;
        this.personRepository = personRepository;
    }

    @GetMapping("/geisternetze")
    public String showGeisternetze(Model model) {
        model.addAttribute("netze", geisternetzRepository.findAll());
        return "geisternetze";
    }

    @GetMapping("/geisternetze/neu")
    public String showErfassenForm(Model model) {
        model.addAttribute("netz", new Geisternetz());
        return "geisternetz-form";
    }

    @PostMapping("/geisternetze/neu")
    public String erfasseGeisternetz(@RequestParam String standort,
                                     @RequestParam String groesse,
                                     @RequestParam(required = false) String name,
                                     @RequestParam(required = false) String telefonnummer,
                                     Model model) {
        Geisternetz netz = new Geisternetz();
        netz.setStandort(standort);
        netz.setGroesse(groesse);
        netz.setStatus(Geisternetz.Status.GEMELDET);

        if (name != null && !name.isBlank()) {
            Person meldendePerson = new Person();
            meldendePerson.setName(name);
            meldendePerson.setTelefonnummer(telefonnummer);
            meldendePerson.setRolle(Person.Rolle.MELDEND);
            personRepository.save(meldendePerson);
            netz.setMeldendePerson(meldendePerson);
        }

        geisternetzRepository.save(netz);
        return "redirect:/geisternetze";
    }
}
