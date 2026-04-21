package com.example.modulith.dogs;

import com.example.modulith.Channels;
import org.springframework.modulith.events.Externalized;

@Externalized (Channels.ADOPTION_CHANNEL_NAME)
public record DogAdoptedEvent(int dogId) {
}
