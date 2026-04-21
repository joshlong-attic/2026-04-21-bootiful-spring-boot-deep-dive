package com.example.modulith.dogs;

import com.example.modulith.dogs.validation.Validation;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@ResponseBody
class DogsAdoptionController {

    private final AdoptionService adoptionService;
    private final Validation validation ;

    DogsAdoptionController(AdoptionService adoptionService, Validation validation) {
        this.adoptionService = adoptionService;
        this.validation = validation;
    }

    @PostMapping("/dogs/{dogId}/adoptions")
    void adopt(@PathVariable int dogId, @RequestParam String owner) {
        this.adoptionService.adopt(dogId, owner);
    }
}

// look mom, no Lombok!
record Dog(@Id int id, String name, String description, String owner) {
}

interface DogRepository extends ListCrudRepository<Dog, Integer> {
}

@Service
@Transactional
class AdoptionService {

    private final DogRepository repository;

    private final ApplicationEventPublisher applicationEventPublisher;

    AdoptionService(DogRepository repository, ApplicationEventPublisher applicationEventPublisher) {
        this.repository = repository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    void adopt(int dogId, String owner) {
        this.repository.findById(dogId).ifPresent(dog -> {
            var updated = repository.save(new Dog(
                    dog.id(), dog.name(), dog.description(), owner));
            this.applicationEventPublisher.publishEvent(new DogAdoptedEvent(dogId));
            IO.println("adopted " + updated);
        });
    }
}
