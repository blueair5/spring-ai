package com.example.springai.chatclient.advisor;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/simpleChatMemory")
public class ChatMemoryAdvisorTestController {
	private final ChatClient chatClient;

	public ChatMemoryAdvisorTestController(ChatClient.Builder chatClientBuilder) {
		this.chatClient = chatClientBuilder.build();
	}

	// 测试一下 logger 的作用
	@RequestMapping("/testLogger")
	public String ChatLoggerTestController(@RequestParam("userInput") String userInput) {
		String userResponse = chatClient.prompt()
			.advisors(new SimpleLoggerAdvisor())
			.user(userInput)
			.call()
			.content();
		return userResponse;
	}
}
