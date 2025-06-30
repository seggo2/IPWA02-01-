package com.ghostnet.config;

import java.util.Locale;
import java.util.Random;

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
    @SuppressWarnings("unused")
    CommandLineRunner initData(GeisternetzRepository netzRepo, PersonRepository personRepo) {
        return args -> {

            if (personRepo.count() == 0) {
                for (int i = 1; i <= 20; i++) {
                    Rolle rolle = (i % 2 == 0) ? Rolle.BERGEND : Rolle.MELDEND;
                    String telefon = rolle == Rolle.MELDEND && i % 3 == 0 ? null : "0123-" + (100 + i);
                    personRepo.save(new Person("Person " + i, telefon, rolle));
                }
            }

            if (netzRepo.count() == 0) {
                Random random = new Random();
                for (int i = 1; i <= 20; i++) {
                    double lat = 50 + random.nextDouble() * 10; 
                    double lon = 5 + random.nextDouble() * 10;  

                    String standort = String.format(Locale.US, "%.4f, %.4f", lat, lon);

                    String groesse = (2 + random.nextInt(5)) + "x" + (2 + random.nextInt(5)) + "m";
                    Status status = Status.values()[random.nextInt(Status.values().length)];

                    Geisternetz netz = new Geisternetz(standort, groesse, status);

                    if (status != Status.GEMELDET) {
                        Person person = personRepo.findById((long) (2 + random.nextInt(10))).orElse(null);
                        netz.setBergendePerson(person);
                    }

                    netzRepo.save(netz);
                }
            }
        };
    }
}
