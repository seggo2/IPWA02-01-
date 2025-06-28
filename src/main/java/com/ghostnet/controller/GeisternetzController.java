package com.ghostnet.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ghostnet.model.Geisternetz;
import com.ghostnet.repository.GeisternetzRepository;

@RestController
@RequestMapping("/api/netze")
public class GeisternetzController {
    
    private final GeisternetzRepository repo;

    public GeisternetzController(GeisternetzRepository repo){
        this.repo = repo;
    }

    @PostMapping
    public Geisternetz create(@RequestBody Geisternetz netz){
        return repo.save(netz);
    }

    @GetMapping
    public List<Geisternetz> findAll(){
        return repo.findAll();
    }
}
