package com.ghostnet.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ghostnet.model.Geisternetz;

public interface  GeisternetzRepository extends JpaRepository<Geisternetz, Long> {
    
}
