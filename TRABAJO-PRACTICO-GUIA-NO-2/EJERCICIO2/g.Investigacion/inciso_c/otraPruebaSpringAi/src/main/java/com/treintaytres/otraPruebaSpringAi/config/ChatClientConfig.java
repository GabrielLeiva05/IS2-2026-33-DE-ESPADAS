package com.treintaytres.otraPruebaSpringAi.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient client(ChatClient.Builder builder){
        String assistant = "Sos un bot de Goku, personaje de Dragon Ball Z." +
                "Respondes en español, y solo hablas de temas relacionados a peleas y superpoderes," +
                "Si te preguntan de otra cosa respondes que no podes ayudar con eso, que solo vives para luchar.";
        return builder.defaultSystem(assistant).build();
    }
}
