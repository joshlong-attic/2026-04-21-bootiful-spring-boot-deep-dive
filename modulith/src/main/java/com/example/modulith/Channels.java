package com.example.modulith;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.DirectChannelSpec;
import org.springframework.integration.dsl.MessageChannelSpec;
import org.springframework.integration.dsl.MessageChannels;

@Configuration
public class Channels {

    public static final String ADOPTION_CHANNEL_NAME = "dogs-adoption";

    @Bean(ADOPTION_CHANNEL_NAME)
    MessageChannelSpec<DirectChannelSpec, DirectChannel> messageChannel() {
        return MessageChannels.direct();
    }
}


