package com.example.springai.controller;

import com.example.springai.entity.MovesEntity;
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

	/**
	 * 将 AI 生成的回答，映射到实体的 entity 上
	 * <p>
	 * 猜测的实现方法：<br/>
	 * 1.在调用 AI 模型之前，先读取实体的实体类型，然后让 AI 根据实体的类型返回对应的结构 <br/>
	 * 2.调用 Jackson 的反序列化，将内容映射到实体上。 <br/>
	 * <p/>
	 * <p>
	 * 疑问：<br/>
	 * 1. 使用 Lombok 的 @Data 注解，不能被 Jackson 识别，反序列化 <br/>
	 * 2. SpringAI 应该是判断了实体的结构，但是这个结构也受到 getter/setter 方法的影响 <br/>
	 * <p/>
	 *
	 * <p>
	 * 思路：<br/>
	 * 在这，Spring Ai 都是作为客户端访问大模型，那和企业的结合，就是 Spring-AI 访问大模型，然后大模型调用 MCP 访问业务服务，然后返回给 Spring-Ai, 展示客户。<br/>
	 * </p>
	 * @param message
	 */
	@GetMapping("/ai/movies")
	void generationMovies(@RequestParam(value = "message") String message) {
		 List<MovesEntity> movesEntities = this.chatClient.prompt()
				.user(message)
				.call()
				.entity(new ParameterizedTypeReference<List<MovesEntity>>() {});

		System.out.println(movesEntities.toString());
	}
}
