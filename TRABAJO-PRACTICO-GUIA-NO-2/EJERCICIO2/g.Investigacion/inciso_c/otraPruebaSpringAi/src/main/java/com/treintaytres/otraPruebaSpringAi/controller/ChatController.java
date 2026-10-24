package com.treintaytres.otraPruebaSpringAi.controller;

import com.treintaytres.otraPruebaSpringAi.dto.ChatFullResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatClient client;

    @GetMapping
    public String chat(@RequestParam String message){
        return client.prompt().user(message).call().content();
    }

    @GetMapping("/full")
    public ChatFullResponse chatFullResponse(@RequestParam String message){
        //De esta manera creo el dto que tengo, para poder visualizar los demas metadatos.
        ChatResponse response = client.prompt().user(message).call().chatResponse();

        String text = response.getResult().getOutput().getText();
        long promptTokens = response.getMetadata().getUsage().getPromptTokens();
        long completionTokens = response.getMetadata().getUsage().getCompletionTokens();
        long totalTokens = response.getMetadata().getUsage().getTotalTokens();

        return new ChatFullResponse(text, promptTokens, completionTokens, totalTokens);
    }

    //Para mostrar el mensaje "por intervalos", averiguar Flux y Stream.
    @GetMapping(value = "/stream", produces = "text/event-stream")
    public Flux<String> chatStream(@RequestParam String message){
        return client.prompt().user(message).stream().content();
    }
}
