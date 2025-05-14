package com.example.springai.chatclient;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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

	@GetMapping("/ai/structureData")
	void generateStructureData(String userInput) {
//		// record
//		ActorFilms actorFilms = chatClient.prompt()
//			.user("Generate the filmography for a random actor.")
//			.call()
//			.entity(ActorFilms.class);
//		System.out.println(actorFilms.toString());
//
//		// 如果不用 record，用普通的 class 类
//		ActorFilmsCommon actorFilmsCommon = chatClient.prompt()
//			.user("Generate the filmography for a random actor.")
//			.call()
//			.entity(ActorFilmsCommon.class);
//		System.out.println(actorFilmsCommon.toString());

		// lombok 的写法
		ActorFilmsLombok actorFilmsLombok = chatClient.prompt()
			.user("Generate the filmography for a random actor.")
			.call()
			.entity(ActorFilmsLombok.class);
	}

	// https://www.cnblogs.com/kohler21/p/17555985.html 参考 record 的用法
	record ActorFilms(String actor, List<String> movies) {
	}

	/**
	 * jackson 针对 @Data 注解的类，注入不到值，这个需要看看
	 */
	static class ActorFilmsCommon {
		private String actor;
		private List<String> movies;

		public ActorFilmsCommon() {
		}

		public ActorFilmsCommon(String actor, List<String> movies) {
			this.actor = actor;
			this.movies = movies;
		}

		public String getActor() {
			return actor;
		}

		public void setActor(String actor) {
			this.actor = actor;
		}

		public List<String> getMovies() {
			return movies;
		}

		public void setMovies(List<String> movies) {
			this.movies = movies;
		}

		@Override
		public String toString() {
			return "ActorFilmsCommon{" +
				"actor='" + actor + '\'' +
				", movies=" + movies +
				'}';
		}
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	static class ActorFilmsLombok {
		private String actor;
		private List<String> movies;
	}

}
