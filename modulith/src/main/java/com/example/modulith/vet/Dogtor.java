package com.example.modulith.vet;

import com.example.modulith.dogs.DogAdoptedEvent;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class Dogtor {

    @ApplicationModuleListener
    void checkup(DogAdoptedEvent dogId) throws Exception {
        Thread.sleep(5000);
        IO.println("checking up on dog " + dogId);
    }
}
