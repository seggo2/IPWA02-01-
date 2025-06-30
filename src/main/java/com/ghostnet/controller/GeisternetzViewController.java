package com.ghostnet.controller;

import java.util.List;

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

    @GetMapping("/geisternetze/reservierung")
    public String zeigeBergungsAktionen(Model model) {
        List<Geisternetz> offeneNetze = geisternetzRepository.findByStatus(Geisternetz.Status.GEMELDET);
        model.addAttribute("offeneNetze", offeneNetze);
        return "geisternetze-condition";
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

        // --- Netz erfassen ---
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

    @PostMapping("/geisternetze/reservierung")
    public String bearbeiteNetzAktion(@RequestParam Long netzId, @RequestParam String telefonnummer, @RequestParam String aktion, Model model) {

        Person person = personRepository.findByTelefonnummer(telefonnummer)
                .orElse(null);

        if (person == null || person.getRolle() != Person.Rolle.BERGEND) {
            model.addAttribute("error",
                    "Nur bergende Personen mit gültiger Telefonnummer dürfen Aktionen durchführen.");
            return "geisternetze-condition";
        }

        Geisternetz netz = geisternetzRepository.findById(netzId).orElse(null);
        if (netz == null) {
            model.addAttribute("error", "Netz nicht gefunden.");
            return "geisternetze-condition";
        }

        switch (aktion) {
            case "reservieren" -> {
                if (netz.getStatus() == Geisternetz.Status.GEMELDET) {
                    netz.setBergendePerson(person);
                    netz.setStatus(Geisternetz.Status.BERGUNG_BEVORSTEHEND);
                } else {
                    model.addAttribute("error", "Nur GEMELDETE Netze können reserviert werden.");
                    return "geisternetze-condition";
                }
            }

            case "geborgen" -> {
                if (netz.getBergendePerson() != null && netz.getBergendePerson().equals(person)) {
                    netz.setStatus(Geisternetz.Status.GEBORGEN);
                } else {
                    model.addAttribute("error", "Nur die reservierende Person darf das Netz als geborgen melden.");
                    return "geisternetze-condition";
                }
            }

            case "verschollen" -> {
                if (netz.getStatus() == Geisternetz.Status.GEMELDET ||
                        (netz.getStatus() == Geisternetz.Status.BERGUNG_BEVORSTEHEND
                                && person.equals(netz.getBergendePerson()))) {
                    netz.setStatus(Geisternetz.Status.VERSCHOLLEN);
                } else {
                    model.addAttribute("error", "Bedingungen für verschollen melden nicht erfüllt.");
                    return "geisternetze-condition";
                }
            }

            case "zuruecksetzen" -> {
                if (netz.getStatus() == Geisternetz.Status.BERGUNG_BEVORSTEHEND &&
                        person.equals(netz.getBergendePerson())) {
                    netz.setBergendePerson(null);
                    netz.setStatus(Geisternetz.Status.GEMELDET);
                } else {
                    model.addAttribute("error", "Nur die reservierende Person kann das Netz zurücksetzen.");
                    return "geisternetze-reservierung";
                }
            }
        }

        geisternetzRepository.save(netz);
        return "redirect:/geisternetze";
    }

}
