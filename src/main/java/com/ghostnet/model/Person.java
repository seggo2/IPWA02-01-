package com.ghostnet.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class Person {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private  Long id;
    
    private String name;

    private String telefonnummer;

    @Enumerated(EnumType.STRING)
    private Rolle rolle;

    @JsonIgnore
    @OneToMany(mappedBy="bergendePerson")
    private List<Geisternetz> geisternetze;

    public enum Rolle{
        MELDEND,
        BERGEND
    }

    public Person() {}

    public Person(String name, String telefonnummer, Rolle rolle){
        this.name = name;
        this.telefonnummer = telefonnummer;
        this.rolle = rolle;
    }

    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTelefonnummer() { return telefonnummer; }
    public void setTelefonnummer(String telefonnummer) { this.telefonnummer = telefonnummer; }

    public Rolle getRolle() { return rolle; }
    public void setRolle(Rolle rolle) { this.rolle = rolle; }

    public List<Geisternetz> getGeisternetze() { return geisternetze; }
    public void setGeisternetze(List<Geisternetz> geisternetze) { this.geisternetze = geisternetze; }

}
