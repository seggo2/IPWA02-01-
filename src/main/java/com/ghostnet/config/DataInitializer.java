package com.ghostnet.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ghostnet.model.Geisternetz;
import com.ghostnet.model.Geisternetz.Status;
import com.ghostnet.model.Person;
import com.ghostnet.model.Person.Rolle;
import com.ghostnet.repository.GeisternetzRepository;
import com.ghostnet.repository.PersonRepository;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(GeisternetzRepository netzRepo, PersonRepository personRepo) {
        return args -> {
            if (personRepo.count() == 0) {
                personRepo.save(new Person("Lisa Berger", "0123-111", Rolle.MELDEND));
                personRepo.save(new Person("Ali Yilmaz", "0123-222", Rolle.BERGEND));
                personRepo.save(new Person("Nina Lorenz", null, Rolle.MELDEND));
            }
    
            if (netzRepo.count() == 0) {
                Person ali = personRepo.findById(2L).orElseThrow();
    
                Geisternetz netz1 = new Geisternetz("53.123, 10.987", "5x5m", Status.BERGUNG_BEVORSTEHEND);
                netz1.setBergendePerson(ali);
                netzRepo.save(netz1);
    
                netzRepo.save(new Geisternetz("54.001, 11.222", "3x4m", Status.GEMELDET));
                netzRepo.save(new Geisternetz("55.111, 9.999", "7x3m", Status.GEMELDET));
            }
        };
    }
    
}


