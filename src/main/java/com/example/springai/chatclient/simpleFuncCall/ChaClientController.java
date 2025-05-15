package com.example.springai.chatclient.simpleFuncCall;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 响应客户的请求
 */
@RestController
@RequestMapping("/simpleFuncCall")
public class ChaClientController {
	private final ChatClient chatClient;

	public ChaClientController(ChatClient.Builder chatClientBuilder) {
		this.chatClient = chatClientBuilder.build();
	}

	@GetMapping("/responseUser")
	public String responseUser(@RequestParam("userInput") String userInput) throws JsonProcessingException {
		// 1. 获取函数库信息
		Map<String, String> functionLibrary = getFunctionLibrary();
		// 2. 将客户的信息和函数库组装
		String prompt = getPromptInfo(functionLibrary);
		// 3. 信息给 AI
		// 4. 获取返回的 url
		// 5. 调用业务服务获取业务数据
		// 6. AI 润色
		// 7. 返回给客户
	}

	private Map<String, String> getFunctionLibrary() {
		return Map.of("获取一个人这个月的报销单数据", "http://getBills");
	}

	private String getPromptInfo(Map<String, String> functionLibrary) throws JsonProcessingException {
		StringBuilder sb = new StringBuilder();
		sb.append("我有一个函数库，你根据用户的需求，选择出唯一的最有可能的 URL 链接: ");
		sb.append("函数库如下：");
		ObjectMapper objectMapper = new ObjectMapper();
		String functionLibraryStr = objectMapper.writeValueAsString(functionLibrary);
		sb.append(functionLibraryStr);
		return sb.toString();
	}
}
