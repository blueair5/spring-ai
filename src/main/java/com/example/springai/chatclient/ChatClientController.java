package com.example.springai.chatclient;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/chatclient")
public class ChatClientController {
	private final ChatClient chatClient;

	public ChatClientController(ChatClient.Builder chatClientBuilder) {
		this.chatClient = chatClientBuilder.build();
	}

	@GetMapping("/ai")
	String generation(String userInput) {
		return this.chatClient.prompt()
			.user(userInput)
			.call() // sent a request to ai model
			.content(); // return the AI model response
	}

	@GetMapping("/ai/{language}")
	String generation(@RequestParam("userInput") String userInput, @PathVariable("language") String language) {
		return this.chatClient.prompt("请你用" + language +"回答下面的问题")
			.user(userInput)
			.call() // sent a request to ai model
			.content(); // return the AI model response
	}

	@GetMapping("/ai/chatResponse")
	String generationChatResponse(String userInput) {
		// https://www.cnblogs.com/kohler21/p/17555985.html 参考 record 的用法
		record ActorFilms(String actor, List<String> movies) {
		}
		ChatResponse chatResponse = chatClient.prompt()
			.user(userInput)
			.call()
			.chatResponse();
		// 携带一些元数据信息，可以用来进行一些计费的控制
		if (Objects.nonNull(chatResponse) && chatResponse.getMetadata().getUsage().getPromptTokens() > 100) {
			return "超出当前的额度限制，请充值";

		}
		return chatResponse.getResult().getOutput().getText();
	}

}
