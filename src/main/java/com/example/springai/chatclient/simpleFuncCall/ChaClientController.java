package com.example.springai.chatclient.simpleFuncCall;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 响应客户的请求
 */
@RestController
@RequestMapping("/simpleFuncCall")
public class ChaClientController {
	private final ChatClient chatClient;
	private final RestTemplate restTemplate;

	public ChaClientController(ChatClient.Builder chatClientBuilder, RestTemplate restTemplate) {
		this.chatClient = chatClientBuilder.build();
		this.restTemplate = restTemplate;
	}

	@GetMapping("/responseUser")
	public String responseUser(@RequestParam("userInput") String userInput,
							   @RequestParam("userCode") String userCode)
			throws JsonProcessingException {
		// 1. 获取函数库信息
		Map<String, String> functionLibrary = getFunctionLibrary();
		// 2. 将客户的信息和函数库组装
		String prompt = getFuncPromptInfo(functionLibrary);
		// 3. 信息给 AI, 拿到返回的结构化数据
		CallRecord callRecord = chatClient.prompt(prompt)
				.user(userInput)
				.call()
				.entity(CallRecord.class);

		// 4. 获取返回的 url
		BusinessRecord businessResponse = callBusinessServer(callRecord, userCode);
		if (businessResponse.code() != 200) {
			return businessResponse.message();
		}

		// 5. 调用业服务获取业务数据
		String embellishPrompt = embellishBusinessResponse(businessResponse.message());
		// 6. AI 润色
		String embellishResponse = chatClient.prompt(embellishPrompt)
				.user(userInput)
				.call()
				.content();
		// 7. 返回给客户
		return embellishResponse;
	}

	/**
	 * 获取函数库
	 * @return
	 */
	private Map<String, String> getFunctionLibrary() {
		return Map.of("获取一个人这个月的报销单数据", "http://localhost:8088/pty/pex/labour/ai/idNo",
			          "获取一个人这个月的差旅费单据数据", "http://localhost:8088/pty/pex/travel/ai/travel"
					);
	}

	/**
	 * 根据函数库生成对应的 prompt
	 * @param functionLibrary
	 * @return
	 * @throws JsonProcessingException
	 */
	private String getFuncPromptInfo(Map<String, String> functionLibrary) throws JsonProcessingException {
		StringBuilder sb = new StringBuilder();
		sb.append("我有一个函数库，你根据用户的需求，从给定的函数库选择出唯一的最有可能的 URL 链接: ");
		sb.append("函数库如下：");
		ObjectMapper objectMapper = new ObjectMapper();
		String functionLibraryStr = objectMapper.writeValueAsString(functionLibrary);
		sb.append(functionLibraryStr);
		sb.append("并且，返回的信息里面, 设置 fiscal 字段为当前年的数字字符，比如 fiscal = 2025");
		return sb.toString();
	}

	/**
	 * 对业务模型返回的信息进行润色
	 * @param businessResponse
	 * @return
	 */
	private String embellishBusinessResponse(String businessResponse) {
		StringBuilder sb = new StringBuilder();
		sb.append("假设你是一个财务专家，你得到了用户的下面的数据:");
		sb.append(businessResponse);
		sb.append("请给出结构清晰的，带汇总的信息数据");
		return sb.toString();

	}

	/**
	 * 调用业务服务，获取结构化的返回
	 * @param callRecord
	 */
	private BusinessRecord callBusinessServer(CallRecord callRecord, String userCode) {
		if (StringUtils.isEmpty(callRecord.url())) {
			return new BusinessRecord(500, "没有找到对应的功能函数");
		}
		HttpHeaders headers = new HttpHeaders();
		MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
		headers.setContentType(type);
		headers.add("Accept", MediaType.APPLICATION_JSON.toString());
		HashMap<String, Object> queryBody = new HashMap<>();
		queryBody.put("idNo", userCode);
		// 这里对年度进行修正, AI 模型给出的年度可能是因为模型的时间问题, 需要进行修正
		queryBody.put("fiscal", "2025");
		String stu = JSON.toJSONString(queryBody);
		HttpEntity<String> formEntity = new HttpEntity<String>(stu, headers);
		String businessResponse = restTemplate.postForObject(callRecord.url(), formEntity, String.class);
		System.out.println("businessResponse = " + businessResponse);
		return new BusinessRecord(200, businessResponse);
	}

	record CallRecord(String url, Integer fiscal) {

	}

	record BusinessRecord(Integer code, String message) {

	}

	@GetMapping("/ai")
	Map<String, String> completion(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message, String voice) {
		return Map.of("completion",
			this.chatClient.prompt()
				.system(sp -> sp.param("voice", voice))
				.user(message)
				.call()
				.content());
	}
}
