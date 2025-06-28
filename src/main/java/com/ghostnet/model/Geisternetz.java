package com.ghostnet.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Geisternetz {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    private String standort;
    private String groesse;

    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status{
        GEMELDET,
        BERGUNG_BEVORSTEHEND,
        GEBORGEN,
        VERSCHOLLEN
    }

    public Geisternetz() {}

    public Geisternetz(String standort, String groesse, Status status){
        this.standort = standort;
        this.groesse = groesse;
        this.status = status;
    }

    public Long getId() { return id; }
    
    public String getStandort() { return standort; }
    public void setStandort(String standort) { this.standort = standort; }

    public String getGroesse() { return groesse; }
    public void setGroesse(String groesse) { this.groesse = groesse; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }


}
