package com.treintaytres.PrimerProyectoSpringAI.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class IaService {

        //la forma de comunicarse con el modelo de IA (cualquiera)
        private final ChatClient chatClient;

    public IaService(ChatClient.Builder builder) {
        //Se construye el cliente de la config que tengo en application Properties
        //Builder es un patron GoF para separar la responsabilidad de la creacion de objetos.
        //puedo realizar cosas como .defaultsystem
        this.chatClient = builder.build();
    }

    public String preguntar(String pregunta){
        //Con el System estamos guardando como un contexto antes de darle la pregunta del usuario
        return chatClient
                .prompt()
                .system("Sos un profesor especializado en programacion."
                + "Responde siempre en español." + "Explica los conceptos de forma sencilla")
                .user(pregunta)
                .call()
                .content();
    }
}
