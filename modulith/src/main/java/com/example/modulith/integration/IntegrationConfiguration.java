package com.example.modulith.integration;

import com.example.modulith.Channels;
import com.example.modulith.dogs.DogAdoptedEvent;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.core.GenericHandler;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;

@Configuration
class IntegrationConfiguration {

    // event driven applications
    // enterprise application integration (EAI)

    @Bean
    IntegrationFlow flow(@Qualifier(Channels.ADOPTION_CHANNEL_NAME) MessageChannel inbound) {
        return IntegrationFlow
                .from(inbound)
                .handle(new GenericHandler<DogAdoptedEvent>() {
                    @Override
                    public @Nullable Object handle(DogAdoptedEvent payload, MessageHeaders headers) {
                        IO.println("got the event and am going " +
                                "to do something important with it " + payload);
                        headers.forEach((key, value) -> IO.println("\t" + key + " -> " + value));
                        return null;
                    }
                })
//                .split()
//                .gateway("dog-vet")
//                .enrich()
//                .enrichHeaders()
//                .transform()
                .get();
    }
}
