package com.ghostnet.controller;

import java.util.List;
import java.util.Optional;

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
        return "geisternetze-form";
    }

    @PostMapping("/geisternetze/neu")
    public String erfasseGeisternetz(@RequestParam String standort,
            @RequestParam String groesse,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String telefonnummer,
            Model model) {

        // --- Validierung ---
        String koordinatenRegex = "^-?\\d{1,2}\\.\\d{3,10},\\s*-?\\d{1,3}\\.\\d{3,10}$";
        String groesseRegex = "^\\d+(\\.\\d+)?x\\d+(\\.\\d+)?$";

        if (!standort.matches(koordinatenRegex)) {
            model.addAttribute("fehler", "Ungültige Koordinaten. Beispiel: 47.1234, 8.1234");
            model.addAttribute("netz", new Geisternetz()); // damit das Formular wieder ein Objekt hat
            return "geisternetze-form";
        }

        if (!groesse.matches(groesseRegex)) {
            model.addAttribute("fehler", "Ungültige Größe. Beispiel: 5x2 oder 3.5x1.2");
            model.addAttribute("netz", new Geisternetz());
            return "geisternetze-form";
        }

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

    @GetMapping("/geisternetze/auth")
    public String zeigeAuthForm() {
        return "geisternetze-auth";
    }

    @PostMapping("/geisternetze/auth")
    public String pruefeBergendePerson(@RequestParam String telefonnummer, Model model) {
        Optional<Person> optPerson = personRepository.findByTelefonnummerAndRolle(telefonnummer, Person.Rolle.BERGEND);

        if (optPerson.isPresent()) {
            return "redirect:/geisternetze/reservierung?telefonnummer=" + telefonnummer;
        } else {
            model.addAttribute("error", "Ungültige Telefonnummer oder keine bergende Person gefunden.");
            return "geisternetze-auth";
        }
    }

    @GetMapping("/geisternetze/reservierung")
    public String zeigeReservierungsseite(@RequestParam String telefonnummer, Model model) {
        Optional<Person> optPerson = personRepository.findByTelefonnummerAndRolle(telefonnummer, Person.Rolle.BERGEND);

        if (optPerson.isEmpty()) {
            model.addAttribute("error", "Ungültige Telefonnummer.");
            return "geisternetze-auth";
        }

        Person person = optPerson.get();
        List<Geisternetz> offeneNetze = geisternetzRepository.findByStatus(Geisternetz.Status.GEMELDET);
        List<Geisternetz> eigeneNetze = geisternetzRepository.findByBergendePersonAndStatus(person,
                Geisternetz.Status.BERGUNG_BEVORSTEHEND);

        model.addAttribute("telefonnummer", telefonnummer);
        model.addAttribute("offeneNetze", offeneNetze);
        model.addAttribute("eigeneNetze", eigeneNetze);
        return "geisternetze-reservierung";
    }

    @PostMapping("/geisternetze/reservierung")
    public String bearbeiteReservierung(
            @RequestParam Long netzId,
            @RequestParam String telefonnummer,
            @RequestParam String aktion,
            Model model) {
    
        Optional<Person> optPerson = personRepository.findByTelefonnummerAndRolle(telefonnummer, Person.Rolle.BERGEND);
        if (optPerson.isEmpty()) {
            model.addAttribute("error", "Ungültige Telefonnummer oder keine Berechtigung.");
            model.addAttribute("telefonnummer", telefonnummer);
            return "geisternetze-auth";
        }
    
        Person person = optPerson.get();
        Optional<Geisternetz> optNetz = geisternetzRepository.findById(netzId);
        if (optNetz.isEmpty()) {
            model.addAttribute("error", "Netz nicht gefunden.");
            model.addAttribute("telefonnummer", telefonnummer);
            ladeNetzListen(model, person);
            return "geisternetze-reservierung";
        }
    
        Geisternetz netz = optNetz.get();
    
        switch (aktion) {
            case "reservieren" -> {
                if (netz.getStatus() == Geisternetz.Status.GEMELDET) {
                    netz.setBergendePerson(person);
                    netz.setStatus(Geisternetz.Status.BERGUNG_BEVORSTEHEND);
                } else {
                    model.addAttribute("error", "Nur GEMELDETE Netze können reserviert werden.");
                    model.addAttribute("telefonnummer", telefonnummer);
                    ladeNetzListen(model, person);
                    return "geisternetze-reservierung";
                }
            }
    
            case "geborgen" -> {
                if (person.equals(netz.getBergendePerson())) {
                    netz.setStatus(Geisternetz.Status.GEBORGEN);
                } else {
                    model.addAttribute("error", "Nur du darfst dieses Netz als geborgen melden.");
                    model.addAttribute("telefonnummer", telefonnummer);
                    ladeNetzListen(model, person);
                    return "geisternetze-reservierung";
                }
            }
    
            case "verschollen" -> {
                if (netz.getStatus() == Geisternetz.Status.GEMELDET ||
                    (netz.getStatus() == Geisternetz.Status.BERGUNG_BEVORSTEHEND && person.equals(netz.getBergendePerson()))) {
                    netz.setStatus(Geisternetz.Status.VERSCHOLLEN);
                } else {
                    model.addAttribute("error", "Du darfst dieses Netz nicht als verschollen melden.");
                    model.addAttribute("telefonnummer", telefonnummer);
                    ladeNetzListen(model, person);
                    return "geisternetze-reservierung";
                }
            }
    
            case "zuruecksetzen" -> {
                if (netz.getStatus() == Geisternetz.Status.BERGUNG_BEVORSTEHEND &&
                    person.equals(netz.getBergendePerson())) {
                    netz.setStatus(Geisternetz.Status.GEMELDET);
                    netz.setBergendePerson(null);
                } else {
                    model.addAttribute("error", "Nur du darfst das Netz zurücksetzen.");
                    model.addAttribute("telefonnummer", telefonnummer);
                    ladeNetzListen(model, person);
                    return "geisternetze-reservierung";
                }
            }
        }
    
        geisternetzRepository.save(netz);
        // Weiterleitung nach erfolgreicher Aktion (mit Listen neu laden)
        model.addAttribute("telefonnummer", telefonnummer);
        ladeNetzListen(model, person);
        model.addAttribute("success", "Aktion erfolgreich durchgeführt.");
        return "geisternetze-reservierung";
    }

    private void ladeNetzListen(Model model, Person person) {
        List<Geisternetz> offeneNetze = geisternetzRepository.findByStatus(Geisternetz.Status.GEMELDET);
        List<Geisternetz> eigeneNetze = geisternetzRepository.findAll().stream()
            .filter(n -> person.equals(n.getBergendePerson()))
            .toList();
        model.addAttribute("offeneNetze", offeneNetze);
        model.addAttribute("eigeneNetze", eigeneNetze);
    }
    
}
