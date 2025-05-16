package com.example.springai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@RestController
public class ChatController {
	private final OpenAiChatModel openAiChatModel;

	private final ChatClient chatClient;

	@Autowired
	public ChatController(OpenAiChatModel openAiChatModel, ChatClient.Builder chatClientBuilder) {
		this.openAiChatModel = openAiChatModel;
		this.chatClient = chatClientBuilder.defaultSystem("中文回答").build();
	}

	@GetMapping("/ai/generate")
	public Map generate(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
		return Map.of("generation", this.openAiChatModel.call(message));
	}

	@GetMapping("/ai/generateStream")
	public Flux<ChatResponse> generateStream(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
		Prompt prompt = new Prompt(new UserMessage(message));
		return this.openAiChatModel.stream(prompt);
	}

	@GetMapping("/ai/simpleString")
	String generation(@RequestParam(value = "message") String message) {
		return this.chatClient.prompt()
				.user(message)
				.call()
				.content();
	}
}
